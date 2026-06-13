package com.parnas.orderservice.dto.resp;

import com.parnas.orderservice.model.OrderStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrderResponse(

        UUID id,

        String customerName,

        LocalDateTime orderDate,

        OrderStatus status
        
) implements Serializable {
}
