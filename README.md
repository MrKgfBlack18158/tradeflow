# TradeFlow

An event-driven **trade ingestion pipeline**: Spring Boot microservices, Apache Kafka and MongoDB, with Kubernetes deployment coming later.

```
POST /api/trades ─▶ trade-gateway ──▶ Kafka: trades.raw ──▶ trade-processor ──▶ MongoDB "trades"
                    (8081, REST,       (3 partitions,        (8082, validate,        │
                     validation)        key = account)         risk limits,           │
                                                               notional, idempotent)  │
                                        GET /api/accounts/{acc}/positions ◀── trade-query (8083)
```

| Service | What it does | Key ideas |
|---|---|---|
| `trade-gateway` | Accepts trades over REST, validates input, publishes to Kafka, returns `202 Accepted` + `tradeId` | Bean Validation, Kafka producer (`acks=all`, idempotence), keying by account for ordering |
| `trade-processor` | Consumes `trades.raw`, computes notional, applies risk limits, stores ACCEPTED/REJECTED trades | `@KafkaListener`, consumer groups, poison-pill-safe deserialization, idempotent upsert (`_id = tradeId`), Decimal128 for money |
| `trade-query` | Read API: trade by id, trades per account, positions per account | Spring Data derived queries, read model (light CQRS) |

---

## Run it locally (any computer)

**You need:** JDK 21, Maven 3.9+, Docker (Docker Desktop on Windows/Mac). On an 8 GB machine, close browsers/IDEs you don't need. Kafka and MongoDB are capped at about 1 GB together in `docker-compose.yml`.

```bash
# 1. Start Kafka + MongoDB
docker compose up -d
docker compose ps                     # wait until both show "healthy"

# 2. Build everything and run the unit tests (no Kafka/Mongo needed for the tests)
mvn verify

# 3. Start the three services, each in its own terminal
mvn -pl trade-gateway   spring-boot:run
mvn -pl trade-processor spring-boot:run
mvn -pl trade-query     spring-boot:run

# 4. Send a trade
curl -s -X POST localhost:8081/api/trades -H 'Content-Type: application/json' \
  -d '{"account":"ACC-1","symbol":"INFY","side":"BUY","quantity":100,"price":1520.50}'

# 5. See it come out the other end
curl -s localhost:8083/api/accounts/ACC-1/trades
curl -s localhost:8083/api/accounts/ACC-1/positions
```

More example requests are in `requests.http`. In VS Code, the REST Client extension gives each request a "Send Request" button.

Optional Kafka UI to watch topics and messages: `docker compose --profile ui up -d`, then open http://localhost:8080.

**Done when:** a POST to the gateway shows a "Published trade" log in the gateway, a "Trade … ACCEPTED" log in the processor, and the trade appears in `trade-query`.

### Working from more than one computer

- GitHub is the source of truth. **Start** every session with `git pull` and **end** it with `git add -A && git commit -m "..." && git push`.
- Everything needed to run is in the repo (`docker-compose.yml`, `application.yml`), so a fresh clone works the same everywhere.
- Never commit secrets. Use environment variables (`KAFKA_BOOTSTRAP_SERVERS`, `MONGODB_URI` are already supported).

---

## Roadmap

Each session lists its tasks, a "done when" check, and the interview questions it prepares you for. **Understand every line before moving on.** Interviewers will ask "why" about anything on your resume.

### Session 1: run the pipeline end to end
- [ ] `docker compose up -d`, `mvn verify` passes, all three services start
- [ ] Send valid, over-limit and invalid trades (see `requests.http`); watch the logs
- [ ] Open the Kafka UI and find your messages and their partitions
- [ ] Read every class; write down anything you can't explain and look it up
- **Interview prep:** Why 202 and not 201? Why key by account? What does `acks=all` do? What is a consumer group?

### Session 2: resilience
- [ ] In `trade-processor`, add a `DefaultErrorHandler` with `FixedBackOff` (or exponential) + `DeadLetterPublishingRecoverer`, so failed messages go to `trades.raw.DLT`
- [ ] Prove it: temporarily throw an exception for symbol `"FAIL"` and watch the message land in the DLT
- [ ] Prove idempotency: replay the same message twice (Kafka UI or reset the consumer group offset) and confirm there's still one document
- **Interview prep:** at-least-once vs exactly-once; what's a poison pill; why the DLT; how idempotency works here

### Session 3: MongoDB depth
- [ ] Replace `PositionCalculator` with a MongoDB aggregation (`$match` → `$group` with `$cond` for BUY/SELL) using `MongoTemplate`
- [ ] Add a compound index (`account` + `receivedAt`) and check it's used with `explain()` in `mongosh`
- [ ] Add pagination to `/accounts/{account}/trades` (`Pageable`)
- **Interview prep:** document vs relational modelling (compare with your Oracle schema at NAL), indexes, aggregation pipeline

### Session 4: testing + CI
- [ ] Add Testcontainers (`org.testcontainers:kafka`, `org.testcontainers:mongodb`, `spring-boot-testcontainers`): an integration test that publishes a trade and asserts it lands in MongoDB
- [ ] Push to GitHub and get the Actions badge green (`.github/workflows/ci.yml` is ready)
- [ ] Add the badge to this README
- **Interview prep:** unit vs integration tests, mocks vs real dependencies, what CI catches

### Session 5: containers + security
- [ ] A `Dockerfile` per service (multi-stage: build with Maven, run on a JRE image), or `mvn spring-boot:build-image`
- [ ] Add the three services to `docker-compose.yml` (they'll use `kafka:29092` and `mongodb:27017` inside Docker)
- [ ] Secure the gateway with JWT (`spring-boot-starter-oauth2-resource-server`) and reuse what you built at NAL
- [ ] Add OpenAPI docs (springdoc) to gateway and query
- **Interview prep:** image layers, why multi-stage, how JWT validation works

### Session 6: Kubernetes + load test
- [ ] Local cluster with `kind` (lighter than minikube on 8 GB); load images with `kind load docker-image`
- [ ] `k8s/` manifests: Deployment + Service per service, ConfigMap for config, liveness/readiness probes on `/actuator/health/liveness` and `/readiness`
- [ ] Scale `trade-processor` to 3 replicas and watch Kafka split the 3 partitions between them; add an HPA
- [ ] JMeter test plan in `jmeter/` hitting `POST /api/trades`; record throughput → this becomes the real number on your resume
- **Interview prep:** Deployment vs Pod vs Service, probes, why partitions cap consumer parallelism, HPA

### Session 7: polish
- [ ] Architecture diagram + measured results in this README
- [ ] Add the project to the resume (fill the [X] numbers from your JMeter run)
- [ ] Optional: small Angular dashboard for positions (your front-end strength)
