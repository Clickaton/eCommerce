/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ventas.eCommerce.Services;

import com.ventas.eCommerce.entities.Cart;
import com.ventas.eCommerce.entities.Transaction;
import com.ventas.eCommerce.repositories.TransactionRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author chris
 */
@Service
public class TransactionService {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    public void register(LocalDateTime localDateTime, String dni, String cardNumber, LocalDate expirationDate, Cart cart, String addres){
        Transaction transaction = new Transaction();
        
        transaction.setDni(dni);
        transaction.setLocalDateTime(localDateTime);
        transaction.setAddres(addres);
        transaction.setCardNumber(cardNumber);
        transaction.setExpirationDate(expirationDate);
        transaction.setCart(cart);
        
        transactionRepository.save(transaction);
    }
    
}
