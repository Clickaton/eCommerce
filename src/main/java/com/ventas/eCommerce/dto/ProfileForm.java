package com.ventas.eCommerce.dto;

import com.ventas.eCommerce.entities.User;
import jakarta.validation.constraints.AssertTrue;
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
public class ProfileForm {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100)
    private String lastName;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "\\+?[0-9]{10,15}", message = "El teléfono debe tener entre 10 y 15 dígitos, incluyendo el código de área")
    private String phone;

    private MultipartFile image;

    /** Solo se exige si se quiere cambiar la contraseña. */
    private String currentPassword;

    @Size(max = 72)
    private String newPassword;

    private String confirmNewPassword;

    public boolean wantsPasswordChange() {
        return newPassword != null && !newPassword.isBlank();
    }

    @AssertTrue(message = "La nueva contraseña debe tener al menos 8 caracteres y coincidir con la confirmación")
    public boolean isNewPasswordValid() {
        return !wantsPasswordChange()
                || (newPassword.length() >= 8 && newPassword.equals(confirmNewPassword));
    }

    public static ProfileForm from(User user) {
        ProfileForm form = new ProfileForm();
        form.setName(user.getName());
        form.setLastName(user.getLastName());
        form.setPhone(user.getPhone());
        return form;
    }
}
