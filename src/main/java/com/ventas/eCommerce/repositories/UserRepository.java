package com.ventas.eCommerce.repositories;

import com.ventas.eCommerce.entities.User;
import com.ventas.eCommerce.enums.Rol;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Integer id);

    long countByRol(Rol rol);

    @Query("""
            SELECT u FROM User u
            WHERE :q IS NULL
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(u.name) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    Page<User> search(@Param("q") String q, Pageable pageable);
}
