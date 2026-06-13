package com.parnas.orderservice.mapper;

import com.parnas.orderservice.dto.req.OrderItemRequest;
import com.parnas.orderservice.dto.resp.OrderItemResponse;
import com.parnas.orderservice.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderItemMapper {

    OrderItemResponse toResponse(OrderItem orderItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    OrderItem toEntity(OrderItemRequest request);
}
