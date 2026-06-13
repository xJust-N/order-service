package com.parnas.orderservice.dto.req;

import com.parnas.orderservice.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

public record UpdateStatusRequest(

        @NotNull(message = "Status is required")
        OrderStatus status

) implements Serializable {
}
