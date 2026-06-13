package com.parnas.orderservice.service;

import com.parnas.orderservice.config.RabbitConfig;
import com.parnas.orderservice.dto.req.CreateOrderRequest;
import com.parnas.orderservice.dto.resp.OrderDetailResponse;
import com.parnas.orderservice.dto.resp.OrderResponse;
import com.parnas.orderservice.exception.InvalidOrderStatusException;
import com.parnas.orderservice.exception.OrderNotFoundException;
import com.parnas.orderservice.mapper.OrderMapper;
import com.parnas.orderservice.messaging.OrderCreatedEvent;
import com.parnas.orderservice.model.Order;
import com.parnas.orderservice.model.OrderStatus;
import com.parnas.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public OrderDetailResponse createOrder(CreateOrderRequest request) {
        Order order = orderMapper.toEntity(request);
        order.setStatus(OrderStatus.CREATED);
        order.setOrderDate(LocalDateTime.now());
        order.getItems().forEach(item -> item.setOrder(order));

        Order saved = orderRepository.save(order);
        BigDecimal totalAmount = calculateTotalAmount(saved);
        log.info("Created order {} for customer '{}' with total {}",
                saved.getId(), saved.getCustomerName(), totalAmount);

        publishOrderCreated(new OrderCreatedEvent(saved.getId(), saved.getCustomerName(), totalAmount));

        return orderMapper.toDetailResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrders(OrderStatus status, Pageable pageable) {
        Page<Order> orders = status == null
                ? orderRepository.findAll(pageable)
                : orderRepository.findAllByStatus(status, pageable);
        return orders.map(orderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderById(UUID id) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        return orderMapper.toDetailResponse(order);
    }

    @Transactional
    public OrderDetailResponse updateStatus(UUID id, String statusValue) {
        OrderStatus status = parseStatus(statusValue);
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        order.setStatus(status);
        log.info("Order {} status updated to {}", id, status);
        return orderMapper.toDetailResponse(order);
    }

    @Transactional
    public void markProcessing(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        order.setStatus(OrderStatus.PROCESSING);
        log.info("Order {} moved to PROCESSING", id);
    }

    @Transactional(readOnly = true)
    public BigDecimal totalAmountByCustomer(String customerName) {
        return orderRepository.totalAmountByCustomer(customerName);
    }

    private void publishOrderCreated(OrderCreatedEvent event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendOrderCreated(event);
                }
            });
        } else {
            sendOrderCreated(event);
        }
    }

    private void sendOrderCreated(OrderCreatedEvent event) {
        rabbitTemplate.convertAndSend(RabbitConfig.ORDER_EXCHANGE, RabbitConfig.ORDER_CREATED_ROUTING_KEY, event);
        log.info("Published order.created event for order {}", event.orderId());
    }

    private BigDecimal calculateTotalAmount(Order order) {
        return order.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OrderStatus parseStatus(String value) {
        if (value == null) {
            throw new InvalidOrderStatusException(null);
        }
        try {
            return OrderStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidOrderStatusException(value);
        }
    }
}
