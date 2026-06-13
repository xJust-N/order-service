package com.parnas.orderservice.exception;

public abstract class OrderServiceException extends RuntimeException {

    protected OrderServiceException(String message) {
        super(message);
    }
}
