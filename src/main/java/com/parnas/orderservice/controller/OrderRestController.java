package com.parnas.orderservice.controller;

import com.parnas.orderservice.dto.req.CreateOrderRequest;
import com.parnas.orderservice.dto.req.UpdateStatusRequest;
import com.parnas.orderservice.dto.resp.OrderDetailResponse;
import com.parnas.orderservice.dto.resp.OrderResponse;
import com.parnas.orderservice.dto.resp.TotalAmountResponse;
import com.parnas.orderservice.model.OrderStatus;
import com.parnas.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderRestController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDetailResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderDetailResponse created = orderService.createOrder(request);
        return ResponseEntity.created(URI.create("/api/orders/" + created.id())).body(created);
    }

    @GetMapping
    public PagedModel<OrderResponse> getOrders(
            @RequestParam(required = false) OrderStatus status,
            @ParameterObject @PageableDefault(sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(orderService.getOrders(status, pageable));
    }

    @GetMapping("/{id}")
    public OrderDetailResponse getOrder(@PathVariable UUID id) {
        return orderService.getOrderById(id);
    }

    @PutMapping("/{id}/status")
    public OrderDetailResponse updateStatus(@PathVariable UUID id,
                                            @Valid @RequestBody UpdateStatusRequest request) {
        return orderService.updateStatus(id, request.status());
    }

    @GetMapping("/stats/total-amount")
    public TotalAmountResponse totalAmount(@RequestParam String customerName) {
        return new TotalAmountResponse(customerName, orderService.totalAmountByCustomer(customerName));
    }
}
