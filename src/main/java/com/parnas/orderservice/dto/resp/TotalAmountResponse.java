package com.parnas.orderservice.dto.resp;

import java.io.Serializable;
import java.math.BigDecimal;

public record TotalAmountResponse(

        String customerName,

        BigDecimal totalAmount

) implements Serializable {
}
