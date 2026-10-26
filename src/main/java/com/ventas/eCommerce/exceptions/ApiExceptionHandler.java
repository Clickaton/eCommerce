package com.ventas.eCommerce.exceptions;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Respuestas de error estandarizadas (RFC 9457, {@code application/problem+json}) para la API REST.
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.ventas.eCommerce.api")
public class ApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail notFound(ResourceNotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ProblemDetail insufficientStock(InsufficientStockException e) {
        return problem(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(PaymentException.class)
    public ProblemDetail payment(PaymentException e) {
        return problem(HttpStatus.BAD_GATEWAY, e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail business(BusinessException e) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail concurrentUpdate() {
        return problem(HttpStatus.CONFLICT, "El recurso fue modificado concurrentemente. Reintentá la operación.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        e.getBindingResult().getGlobalErrors()
                .forEach(error -> errors.putIfAbsent(error.getObjectName(), error.getDefaultMessage()));
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Los datos enviados no son válidos");
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ProblemDetail badRequest() {
        return problem(HttpStatus.BAD_REQUEST, "La petición está mal formada");
    }

    @ExceptionHandler({BadCredentialsException.class, DisabledException.class})
    public ProblemDetail badCredentials() {
        return problem(HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail accessDenied() {
        return problem(HttpStatus.FORBIDDEN, "Acceso denegado");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail unexpected(Exception e) {
        log.error("Error inesperado en la API", e);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }

    private static ProblemDetail problem(HttpStatus status, String detail) {
        return ProblemDetail.forStatusAndDetail(status, detail);
    }
}
