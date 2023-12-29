/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ventas.eCommerce.Services;

import com.ventas.eCommerce.entities.Cart;
import com.ventas.eCommerce.entities.Product;
import com.ventas.eCommerce.entities.User;
import com.ventas.eCommerce.repositories.CartRepository;
import com.ventas.eCommerce.repositories.ProductRepository;
import com.ventas.eCommerce.repositories.UserRepository;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import javax.servlet.http.HttpSession;
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

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void AddProductToCart(Integer idProduct, User usuario) {
        // Buscar el producto por su ID
        Optional<Product> productOptional = productRepository.findById(idProduct);

        Cart cart = usuario.getCart();

        // Verificar si el carrito ya tiene una lista de productos
        List<Product> products = cart.getProducts();

        if (products == null) {
            // Si la lista no existe, créala
            products = new ArrayList<>();
        }

        if (productOptional.isPresent()) {
            // Agregar el producto a la lista
            products.add(productOptional.get());

            // Actualizar la lista de productos en el carrito
            cart.setProducts(products);

            // Guardar el carrito actualizado
            cartRepository.save(cart);
        } else {
            System.out.println("No se ha cargado ningún producto");
        }
    }

    public List<Product> getProductosEnCarrito(Integer userId) {
        // Buscar el carrito por el ID del usuario
        Cart cart = cartRepository.findCartById(userId);

        // Verificar si el carrito tiene una lista de productos
        List<Product> products = cart.getProducts();


        if (products != null) {
            return products;
        } else {
            return new ArrayList<>(); // Devolver una lista vacía si no hay productos
        }
    }
}

