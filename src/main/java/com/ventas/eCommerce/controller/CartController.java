/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ventas.eCommerce.controller;

import com.ventas.eCommerce.Services.CartService;
import com.ventas.eCommerce.entities.Cart;
import com.ventas.eCommerce.entities.Product;
import com.ventas.eCommerce.entities.User;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

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
    public String AddToCart(@PathVariable Integer id, HttpSession session) {
        User logueado=(User) session.getAttribute("usuariosession");
        Integer idUsuario = logueado.getId();
        cartService.AddProductToCart(id, logueado);
        return "redirect:/product/catalogue"; // Redirige a la página de catálogo después de agregar el producto al carrito
    }

    @GetMapping("/myCart/{id}")
    public String myCart(@PathVariable Integer id, HttpSession session, ModelMap model) {
        User logueado = (User) session.getAttribute("usuariosession");
        Integer idUsuario = logueado.getId();

        List<Product> listaCarrito = cartService.getProductosEnCarrito(idUsuario);

        // Agregar la lista al modelo
        model.addAttribute("listaCarrito", listaCarrito);

        return "cart.html";
    }
}
