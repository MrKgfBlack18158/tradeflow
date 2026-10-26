package com.tradeflow.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Web-layer test: no Kafka needed, the publisher is a Mockito mock. */
@WebMvcTest(TradeController.class)
class TradeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TradePublisher publisher;

    @Test
    void validTradeIsAcceptedAndPublished() throws Exception {
        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"account":"ACC-1","symbol":"infy","side":"BUY","quantity":100,"price":1520.50}
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.tradeId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        verify(publisher).publish(any(TradeEvent.class));
    }

    @Test
    void invalidTradeIsRejectedWith400AndNotPublished() throws Exception {
        mockMvc.perform(post("/api/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"account":"","symbol":"INFY","side":"BUY","quantity":0,"price":-1}
                                """))
                .andExpect(status().isBadRequest());

        verify(publisher, never()).publish(any());
    }
}
