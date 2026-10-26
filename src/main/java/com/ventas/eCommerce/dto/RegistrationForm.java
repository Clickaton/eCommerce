package com.ventas.eCommerce.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class RegistrationForm {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100)
    private String lastName;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no es válido")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "\\+?[0-9]{10,15}", message = "El teléfono debe tener entre 10 y 15 dígitos, incluyendo el código de área")
    private String phone;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
    private String password;

    @NotBlank(message = "Repetí la contraseña")
    private String confirmPassword;

    private MultipartFile image;

    @AssertTrue(message = "Las contraseñas no coinciden")
    public boolean isPasswordConfirmed() {
        return password == null || password.equals(confirmPassword);
    }
}
