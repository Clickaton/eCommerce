package com.ventas.eCommerce.payment;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Credenciales y opciones de MercadoPago (prefijo {@code mercadopago.*}).
 *
 * @param accessToken        Access token privado de la cuenta (TEST-... en sandbox). Nunca exponerlo al frontend.
 * @param webhookSecret      Clave secreta de webhooks, para validar el header {@code x-signature}.
 * @param notificationUrl    URL pública (https) a la que MercadoPago envía los webhooks. Si se omite,
 *                           se usa {@code app.base-url + /payments/webhook/mercadopago}.
 * @param statementDescriptor Texto que ve el comprador en el resumen de su tarjeta.
 */
@ConfigurationProperties(prefix = "mercadopago")
public record MercadoPagoProperties(
        String accessToken,
        String webhookSecret,
        @DefaultValue("ARS") String currency,
        @DefaultValue("https://api.mercadopago.com") String apiUrl,
        String notificationUrl,
        @DefaultValue("ECOMMERCE") String statementDescriptor) {
}
