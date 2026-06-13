package com.parnas.orderservice.exception;

public class InvalidOrderStatusException extends OrderServiceException {

    public InvalidOrderStatusException(String value) {
        super("Invalid order status: %s".formatted(value));
    }
}
