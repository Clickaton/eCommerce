package com.ventas.eCommerce.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuración propia de la aplicación (prefijo {@code app.*} en application.properties).
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @DefaultValue("http://localhost:8080") String baseUrl,
        Jwt jwt,
        Admin admin,
        @DefaultValue Orders orders) {

    public record Jwt(String secret, @DefaultValue("2h") Duration expiration) {
    }

    /** Administrador inicial, creado al arrancar si no existe. Opcional. */
    public record Admin(String email, String password, @DefaultValue("Administrador") String name) {
    }

    /** Tiempo que un pedido pendiente reserva el stock antes de cancelarse automáticamente. */
    public record Orders(@DefaultValue("60m") Duration reservationTime) {
    }
}
