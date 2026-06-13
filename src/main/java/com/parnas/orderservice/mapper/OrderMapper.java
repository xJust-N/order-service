package com.parnas.orderservice.mapper;

import com.parnas.orderservice.dto.req.CreateOrderRequest;
import com.parnas.orderservice.dto.resp.OrderDetailResponse;
import com.parnas.orderservice.dto.resp.OrderResponse;
import com.parnas.orderservice.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = OrderItemMapper.class)
public interface OrderMapper {

    OrderResponse toResponse(Order order);

    OrderDetailResponse toDetailResponse(Order order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    Order toEntity(CreateOrderRequest request);
}
