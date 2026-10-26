package com.ventas.eCommerce.repositories;

import com.ventas.eCommerce.entities.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {

    @Query("""
            SELECT c FROM User u JOIN u.cart c
            LEFT JOIN FETCH c.items i
            LEFT JOIN FETCH i.product
            WHERE u.id = :userId
            """)
    Optional<Cart> findByUserId(@Param("userId") Integer userId);

    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM User u JOIN u.cart c JOIN c.items i WHERE u.id = :userId")
    int countItemsByUserId(@Param("userId") Integer userId);
}
