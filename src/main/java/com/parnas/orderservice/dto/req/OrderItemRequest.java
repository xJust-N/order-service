package com.parnas.orderservice.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.math.BigDecimal;

public record OrderItemRequest(

        @NotBlank(message = "Order item name is blank")
        @Length(message = "Order item name is too long, 200 characters are allowed", max = 200)
        String productName,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        Integer quantity,

        @NotNull(message = "Price is required")
        @PositiveOrZero(message = "Price must be positive or zero")
        BigDecimal price

) implements Serializable {
}
