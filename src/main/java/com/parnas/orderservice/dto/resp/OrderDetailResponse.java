package com.parnas.orderservice.dto.resp;

import com.parnas.orderservice.model.OrderStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderDetailResponse(

        UUID id,

        String customerName,

        LocalDateTime orderDate,

        OrderStatus status,

        List<OrderItemResponse> items

) implements Serializable {
}
