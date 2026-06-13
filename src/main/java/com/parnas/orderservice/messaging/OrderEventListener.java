package com.parnas.orderservice.messaging;

import com.parnas.orderservice.config.RabbitConfig;
import com.parnas.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderService orderService;

    @RabbitListener(queues = RabbitConfig.ORDER_CREATED_QUEUE)
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received order.created event: orderId={}, customer={}, totalAmount={}",
                event.orderId(), event.customerName(), event.totalAmount());
        orderService.markProcessing(event.orderId());
    }
}
