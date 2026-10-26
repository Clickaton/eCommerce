package com.ventas.eCommerce.exceptions;

/**
 * Error de regla de negocio cuyo mensaje puede mostrarse al usuario tal cual.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
