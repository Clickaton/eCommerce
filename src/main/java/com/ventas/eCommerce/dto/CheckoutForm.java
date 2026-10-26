package com.ventas.eCommerce.dto;

import com.ventas.eCommerce.entities.ShippingAddress;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Datos de envío ingresados al confirmar la compra. Los datos de pago nunca pasan por la
 * aplicación: los procesa la pasarela.
 */
@Getter
@Setter
@NoArgsConstructor
public class CheckoutForm {

    @NotBlank(message = "Indicá quién recibe el pedido")
    @Size(max = 150)
    private String recipientName;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 255)
    private String street;

    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 100)
    private String city;

    @NotBlank(message = "La provincia es obligatoria")
    @Size(max = 100)
    private String province;

    @NotBlank(message = "El código postal es obligatorio")
    @Size(max = 10)
    private String postalCode;

    @NotBlank(message = "El teléfono de contacto es obligatorio")
    @Pattern(regexp = "\\+?[0-9]{10,15}", message = "El teléfono debe tener entre 10 y 15 dígitos")
    private String phone;

    public ShippingAddress toShippingAddress() {
        return new ShippingAddress(recipientName.trim(), street.trim(), city.trim(), province.trim(),
                postalCode.trim(), phone.trim());
    }
}
