/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ventas.eCommerce.repositories;

import com.ventas.eCommerce.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author chris
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("SELECT u FROM User u WHERE u.eMail = :email")
    public User findByEmail(@Param("email") String email);

    @Query("SELECT COUNT(u) FROM User u WHERE u.eMail = :email")
    Long countByEMail(@Param("email") String email);

    default boolean existsByEmail(String email) {
        return countByEMail(email) > 0;
    }

    @Query("SELECT u FROM User u WHERE u.phone = :phone")
    public User findByPhone(@Param("phone") String phone);

}
