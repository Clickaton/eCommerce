package com.ventas.eCommerce.repositories;

import com.ventas.eCommerce.entities.Product;
import com.ventas.eCommerce.enums.Category;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("""
            SELECT p FROM Product p
            WHERE (:onlyActive = false OR p.active = true)
              AND (:category IS NULL OR p.category = :category)
              AND (:q IS NULL
                   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<Product> search(@Param("q") String q,
                         @Param("category") Category category,
                         @Param("onlyActive") boolean onlyActive,
                         Pageable pageable);

    List<Product> findTop8ByActiveTrueOrderByIdDesc();

    long countByActiveTrue();

    long countByActiveTrueAndStockLessThanEqual(int stock);

    List<Product> findByActiveTrueAndStockLessThanEqualOrderByStockAsc(int stock);

    /**
     * Descuenta stock de forma atómica: la condición {@code stock >= :quantity} se evalúa en la
     * misma sentencia UPDATE, por lo que dos compras simultáneas del último artículo no pueden
     * tener éxito ambas. Devuelve 0 si no había stock suficiente o el producto está inactivo.
     */
    @Modifying(flushAutomatically = true)
    @Query("""
            UPDATE Product p
            SET p.stock = p.stock - :quantity, p.version = p.version + 1
            WHERE p.id = :id AND p.active = true AND p.stock >= :quantity
            """)
    int decrementStock(@Param("id") Integer id, @Param("quantity") int quantity);

    @Modifying(flushAutomatically = true)
    @Query("UPDATE Product p SET p.stock = p.stock + :quantity, p.version = p.version + 1 WHERE p.id = :id")
    int incrementStock(@Param("id") Integer id, @Param("quantity") int quantity);
}
