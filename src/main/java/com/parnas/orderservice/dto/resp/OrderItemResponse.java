package com.parnas.orderservice.dto.resp;

import java.io.Serializable;
import java.math.BigDecimal;

public record OrderItemResponse(

        Long id,

        String productName,

        int quantity,

        BigDecimal price

) implements Serializable {
}
