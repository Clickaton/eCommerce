/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ventas.eCommerce.Services;

import com.ventas.eCommerce.entities.Cart;
import com.ventas.eCommerce.entities.Product;
import com.ventas.eCommerce.repositories.CartRepository;
import com.ventas.eCommerce.repositories.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author chris
 */
@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public Cart getOrCreateCart() {
        // Buscar el carrito existente
        List<Cart> carts = cartRepository.findAll();

        if (!carts.isEmpty()) {
            // Si hay al menos un carrito, devuelve el primero (puedes personalizar esta lógica según tus requerimientos)
            return carts.get(0);
        } else {
            // Si no hay carritos existentes, crea uno nuevo
            Cart newCart = new Cart();
            System.out.println("Entre a la creación01");
            cartRepository.save(newCart);
            return newCart;
        }
    }

    @Transactional
    public void AddProductToCart(Integer id) {
        // Buscar el producto por su ID
        Optional<Product> productOptional = productRepository.findById(id);

        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            System.out.println("Hola soy un ");
            // Recuperar el carrito existente o crear uno nuevo
            Cart cart = getOrCreateCart();

            // Agregar el producto al carrito
            cart.getProducts().add(product);

            // Guardar el carrito actualizado
            cartRepository.save(cart);
        }
    }
}