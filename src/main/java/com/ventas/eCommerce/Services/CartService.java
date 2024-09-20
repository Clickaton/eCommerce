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

        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            // Recargar el carrito desde la base de datos para asegurarnos de tener la última versión
            Cart cart = cartRepository.findById(usuario.getCart().getId()).orElseThrow(
                    () -> new IllegalStateException("No se encontró el carrito del usuario")
            ); //Identificará si existe un problema con el carrito.

            List<Product> products = cart.getProducts();

            // Verificar si la lista de productos es null
            if (products == null) {
                // Si la lista es null, inicialízala como una lista vacía
                products = new ArrayList<>();
                cart.setProducts(products);
            }

            // Verificar si el producto ya está en el carrito
            boolean productAlreadyInCart = products.stream().anyMatch(p -> p.getId().equals(product.getId()));

            if (!productAlreadyInCart) {
                products.add(product);
                cartRepository.save(cart);
            } else {
                System.out.println("El producto ya está en el carrito.");
            }
        } else {
            System.out.println("No se ha encontrado el producto.");
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

    @Transactional
    public void deleteProductFromCart(Integer productId, Integer userId) {
        // Buscar el carrito por el ID del usuario
        Cart cart = cartRepository.findCartById(userId);

        // Verificar si el carrito tiene una lista de productos
        List<Product> products = cart.getProducts();

        // Encontrar el producto por su ID
        Optional<Product> productOptional = products.stream()
                .filter(product -> product.getId().equals(productId))
                .findFirst();

        // Verificar si el producto está presente y eliminarlo de la lista
        productOptional.ifPresent(product -> products.remove(product));

        // Actualizar la lista de productos en el carrito
        cart.setProducts(products);

        // Guardar el carrito actualizado
        cartRepository.save(cart);
    }

}

