package com.parnas.orderservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parnas.orderservice.dto.req.CreateOrderRequest;
import com.parnas.orderservice.dto.req.OrderItemRequest;
import com.parnas.orderservice.model.Order;
import com.parnas.orderservice.model.OrderStatus;
import com.parnas.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createOrder_persistsToDatabaseAndIsProcessedByListener() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest("Bob",
                List.of(new OrderItemRequest("Pen", 3, new BigDecimal("2.50"))));

        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.customerName").value("Bob"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.items[0].productName").value("Pen"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID orderId = UUID.fromString(objectMapper.readTree(response).get("id").asText());

        assertThat(orderRepository.findById(orderId)).isPresent();

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() ->
                assertThat(orderRepository.findById(orderId))
                        .get()
                        .extracting(Order::getStatus)
                        .isEqualTo(OrderStatus.PROCESSING));
    }
}
