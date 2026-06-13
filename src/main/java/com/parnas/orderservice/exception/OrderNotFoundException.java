package com.parnas.orderservice.exception;

import java.util.UUID;

public class OrderNotFoundException extends OrderServiceException {

    public OrderNotFoundException(UUID id) {
        super("Order not found: %s".formatted(id));
    }
}
