/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ventas.eCommerce.controller;

import com.ventas.eCommerce.Services.CartService;
import com.ventas.eCommerce.entities.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author chris
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/addProduct/{id}")
    public String AddToCart(@PathVariable Integer id) {
        System.out.println("error1");
        cartService.AddProductToCart(id);
        System.out.println("error2");
        return "redirect:/product/catalogue"; // Redirige a la página de catálogo después de agregar el producto al carrito
    }
}
