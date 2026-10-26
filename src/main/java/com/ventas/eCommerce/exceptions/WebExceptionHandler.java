package com.ventas.eCommerce.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Manejo de errores para las vistas Thymeleaf. Los errores de negocio en acciones (POST) vuelven
 * a la página anterior con un mensaje; los recursos inexistentes muestran la página de error.
 */
@Slf4j
@ControllerAdvice(basePackages = "com.ventas.eCommerce.controller")
public class WebExceptionHandler {

    @ExceptionHandler({ResourceNotFoundException.class, NoResourceFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView notFound(Exception e) {
        return errorView(HttpStatus.NOT_FOUND, e instanceof ResourceNotFoundException
                ? e.getMessage() : "La página que buscás no existe");
    }

    @ExceptionHandler(BusinessException.class)
    public String business(BusinessException e, HttpServletRequest request, RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", e.getMessage());
        return redirectBack(request);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public String concurrentUpdate(HttpServletRequest request, RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", "Los datos cambiaron mientras realizabas la operación. Intentá nuevamente.");
        return redirectBack(request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String uploadTooLarge(HttpServletRequest request, RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", "El archivo es demasiado grande (máximo 2 MB)");
        return redirectBack(request);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView unexpected(Exception e, HttpServletRequest request) {
        log.error("Error inesperado en {} {}", request.getMethod(), request.getRequestURI(), e);
        return errorView(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado. Intentá nuevamente más tarde.");
    }

    private static ModelAndView errorView(HttpStatus status, String message) {
        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(status);
        mav.addObject("status", status.value());
        mav.addObject("message", message);
        return mav;
    }

    /**
     * Vuelve a la página anterior. Solo se toma el path del Referer (nunca el host) para no generar
     * open redirects, y se evita redirigir a la misma URL que falló para no entrar en un bucle.
     */
    private static String redirectBack(HttpServletRequest request) {
        String referer = request.getHeader(HttpHeaders.REFERER);
        if (referer != null) {
            try {
                java.net.URI uri = java.net.URI.create(referer);
                String path = uri.getRawPath();
                boolean sameRequest = "GET".equals(request.getMethod()) && request.getRequestURI().equals(path);
                if (path != null && path.startsWith("/") && !path.startsWith("//") && !sameRequest) {
                    return "redirect:" + path + (uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "");
                }
            } catch (IllegalArgumentException ignored) {
                // Referer inválido: se vuelve al inicio.
            }
        }
        return "redirect:/";
    }
}
