package com.parnas.orderservice.repository;

import com.parnas.orderservice.model.Order;
import com.parnas.orderservice.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findAllByStatus(OrderStatus status, Pageable pageable);

    @Query("select o from Order o left join fetch o.items where o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") UUID id);

    @Query("""
            select coalesce(sum(i.price * i.quantity), 0)
            from Order o
            join o.items i
            where o.customerName = :customerName
            """)
    BigDecimal totalAmountByCustomer(@Param("customerName") String customerName);
}
