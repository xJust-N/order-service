package com.parnas.orderservice.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.List;

public record CreateOrderRequest(

        @NotBlank(message = "Customer name is required")
        @Length(max = 200, message = "Customer name is too long")
        String customerName,

        @NotEmpty(message = "Order must contain at least one item")
        List<@Valid OrderItemRequest> items

) implements Serializable {
}
