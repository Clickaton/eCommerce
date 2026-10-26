package com.ventas.eCommerce.exceptions;

public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(String productName) {
        super("No hay stock suficiente de \"" + productName + "\". Revisá las cantidades de tu carrito.");
    }
}
