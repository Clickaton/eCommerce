package com.ventas.eCommerce.repositories;

import com.ventas.eCommerce.entities.Order;
import com.ventas.eCommerce.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    @EntityGraph(attributePaths = {"user", "items"})
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findDetailedById(@Param("id") Integer id);

    @EntityGraph(attributePaths = {"user"})
    Page<Order> findByUserId(Integer userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT o FROM Order o WHERE :status IS NULL OR o.status = :status")
    Page<Order> search(@Param("status") OrderStatus status, Pageable pageable);

    long countByStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.status IN :statuses")
    BigDecimal sumTotalByStatusIn(@Param("statuses") Collection<OrderStatus> statuses);

    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime createdAt);
}
