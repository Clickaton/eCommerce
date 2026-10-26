package com.ventas.eCommerce.services;

import com.ventas.eCommerce.dto.CartResponse;
import com.ventas.eCommerce.entities.Cart;
import com.ventas.eCommerce.entities.CartItem;
import com.ventas.eCommerce.entities.Product;
import com.ventas.eCommerce.exceptions.BusinessException;
import com.ventas.eCommerce.exceptions.ResourceNotFoundException;
import com.ventas.eCommerce.repositories.CartRepository;
import com.ventas.eCommerce.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * El carrito no reserva stock: solo valida contra el stock actual. La reserva real se hace al
 * confirmar la compra en {@link OrderService#checkout}.
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public CartResponse getCart(Integer userId) {
        return CartResponse.from(findCart(userId));
    }

    @Transactional(readOnly = true)
    public int countItems(Integer userId) {
        return cartRepository.countItemsByUserId(userId);
    }

    @Transactional
    public CartResponse addItem(Integer userId, Integer productId, int quantity) {
        if (quantity < 1) {
            throw new BusinessException("La cantidad debe ser al menos 1");
        }
        Product product = findAvailableProduct(productId);
        Cart cart = findCart(userId);

        cart.findItem(productId).ifPresentOrElse(
                item -> item.setQuantity(checkStock(product, item.getQuantity() + quantity)),
                () -> cart.addItem(new CartItem(product, checkStock(product, quantity))));

        return CartResponse.from(cart);
    }

    /** Cambia la cantidad de un producto del carrito. Una cantidad de 0 lo elimina. */
    @Transactional
    public CartResponse updateQuantity(Integer userId, Integer productId, int quantity) {
        Cart cart = findCart(userId);
        if (quantity <= 0) {
            cart.removeItem(productId);
            return CartResponse.from(cart);
        }
        CartItem item = cart.findItem(productId)
                .orElseThrow(() -> new ResourceNotFoundException("El producto no está en el carrito"));
        item.setQuantity(checkStock(item.getProduct(), quantity));
        return CartResponse.from(cart);
    }

    @Transactional
    public CartResponse removeItem(Integer userId, Integer productId) {
        Cart cart = findCart(userId);
        cart.removeItem(productId);
        return CartResponse.from(cart);
    }

    private int checkStock(Product product, int quantity) {
        if (quantity > product.getStock()) {
            throw new BusinessException("Solo hay " + product.getStock() + " unidad(es) disponibles de \""
                    + product.getName() + "\"");
        }
        return quantity;
    }

    private Product findAvailableProduct(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        if (!product.isAvailable()) {
            throw new BusinessException("\"" + product.getName() + "\" no está disponible en este momento");
        }
        return product;
    }

    Cart findCart(Integer userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el carrito del usuario"));
    }
}
