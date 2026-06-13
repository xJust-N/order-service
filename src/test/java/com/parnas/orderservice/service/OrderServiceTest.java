package com.parnas.orderservice.service;

import com.parnas.orderservice.config.RabbitConfig;
import com.parnas.orderservice.dto.req.CreateOrderRequest;
import com.parnas.orderservice.dto.req.OrderItemRequest;
import com.parnas.orderservice.dto.resp.OrderDetailResponse;
import com.parnas.orderservice.exception.InvalidOrderStatusException;
import com.parnas.orderservice.exception.OrderNotFoundException;
import com.parnas.orderservice.mapper.OrderMapper;
import com.parnas.orderservice.messaging.OrderCreatedEvent;
import com.parnas.orderservice.model.Order;
import com.parnas.orderservice.model.OrderItem;
import com.parnas.orderservice.model.OrderStatus;
import com.parnas.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_savesOrderAsCreatedAndPublishesEvent() {
        CreateOrderRequest request = new CreateOrderRequest("Alice",
                List.of(new OrderItemRequest("Book", 2, new BigDecimal("10.00"))));

        OrderItem item = new OrderItem();
        item.setProductName("Book");
        item.setQuantity(2);
        item.setPrice(new BigDecimal("10.00"));
        Order mapped = new Order();
        mapped.setCustomerName("Alice");
        mapped.setItems(new ArrayList<>(List.of(item)));

        UUID generatedId = UUID.randomUUID();
        when(orderMapper.toEntity(request)).thenReturn(mapped);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order toSave = invocation.getArgument(0);
            toSave.setId(generatedId);
            return toSave;
        });
        OrderDetailResponse expected = new OrderDetailResponse(generatedId, "Alice",
                mapped.getOrderDate(), OrderStatus.CREATED, List.of());
        when(orderMapper.toDetailResponse(mapped)).thenReturn(expected);

        OrderDetailResponse result = orderService.createOrder(request);

        assertThat(result).isEqualTo(expected);
        assertThat(mapped.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(mapped.getOrderDate()).isNotNull();
        assertThat(item.getOrder()).isSameAs(mapped);
        verify(orderRepository).save(mapped);

        ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(rabbitTemplate).convertAndSend(eq(RabbitConfig.ORDER_EXCHANGE),
                eq(RabbitConfig.ORDER_CREATED_ROUTING_KEY), eventCaptor.capture());
        OrderCreatedEvent published = eventCaptor.getValue();
        assertThat(published.orderId()).isEqualTo(generatedId);
        assertThat(published.customerName()).isEqualTo("Alice");
        assertThat(published.totalAmount()).isEqualByComparingTo("20.00");
    }

    @Test
    void getOrderById_whenMissing_throwsNotFound() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findByIdWithItems(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(id))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void updateStatus_whenStatusInvalid_throwsAndDoesNotTouchRepository() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> orderService.updateStatus(id, "UNKNOWN"))
                .isInstanceOf(InvalidOrderStatusException.class);

        verify(orderRepository, never()).findByIdWithItems(any());
    }

    @Test
    void markProcessing_setsStatusToProcessing() {
        UUID id = UUID.randomUUID();
        Order order = new Order();
        order.setId(id);
        order.setStatus(OrderStatus.CREATED);
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));

        orderService.markProcessing(id);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PROCESSING);
    }
}
