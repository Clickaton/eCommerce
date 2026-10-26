package com.ventas.eCommerce.services;

import com.ventas.eCommerce.dto.ProfileForm;
import com.ventas.eCommerce.dto.RegistrationForm;
import com.ventas.eCommerce.dto.UserResponse;
import com.ventas.eCommerce.entities.Image;
import com.ventas.eCommerce.entities.User;
import com.ventas.eCommerce.enums.Rol;
import com.ventas.eCommerce.exceptions.BusinessException;
import com.ventas.eCommerce.exceptions.ResourceNotFoundException;
import com.ventas.eCommerce.repositories.UserRepository;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ImageService imageService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse register(RegistrationForm form) {
        String email = form.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Ya existe un usuario registrado con ese email");
        }
        if (userRepository.existsByPhone(form.getPhone())) {
            throw new BusinessException("Ya existe un usuario registrado con ese teléfono");
        }

        User user = new User();
        user.setName(form.getName().trim());
        user.setLastName(form.getLastName().trim());
        user.setEmail(email);
        user.setPhone(form.getPhone());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setRol(Rol.USER);
        user.setImage(imageService.apply(form.getImage(), null));
        return UserResponse.from(userRepository.save(user));
    }

    /** Crea un administrador si todavía no existe un usuario con ese email. */
    @Transactional
    public boolean createAdminIfMissing(String email, String rawPassword, String name) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return false;
        }
        User admin = new User();
        admin.setName(name);
        admin.setEmail(email.trim().toLowerCase(Locale.ROOT));
        admin.setPassword(passwordEncoder.encode(rawPassword));
        admin.setRol(Rol.ADMIN);
        userRepository.save(admin);
        return true;
    }

    @Transactional(readOnly = true)
    public UserResponse get(Integer id) {
        return UserResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public ProfileForm getProfileForm(Integer id) {
        return ProfileForm.from(find(id));
    }

    @Transactional
    public UserResponse updateProfile(Integer id, ProfileForm form) {
        User user = find(id);
        if (userRepository.existsByPhoneAndIdNot(form.getPhone(), id)) {
            throw new BusinessException("Ya existe otro usuario registrado con ese teléfono");
        }
        if (form.wantsPasswordChange()) {
            if (form.getCurrentPassword() == null
                    || !passwordEncoder.matches(form.getCurrentPassword(), user.getPassword())) {
                throw new BusinessException("La contraseña actual es incorrecta");
            }
            user.setPassword(passwordEncoder.encode(form.getNewPassword()));
        }
        user.setName(form.getName().trim());
        user.setLastName(form.getLastName().trim());
        user.setPhone(form.getPhone());
        user.setImage(imageService.apply(form.getImage(), user.getImage()));
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> search(String q, Pageable pageable) {
        return userRepository.search(StringUtils.hasText(q) ? q.trim() : null, pageable).map(UserResponse::from);
    }

    @Transactional
    public void changeRole(Integer id, Rol rol, Integer actingAdminId) {
        if (id.equals(actingAdminId)) {
            throw new BusinessException("No podés cambiar tu propio rol");
        }
        find(id).setRol(rol);
    }

    @Transactional
    public void setEnabled(Integer id, boolean enabled, Integer actingAdminId) {
        if (id.equals(actingAdminId)) {
            throw new BusinessException("No podés deshabilitar tu propia cuenta");
        }
        find(id).setEnabled(enabled);
    }

    @Transactional(readOnly = true)
    public Image getImage(Integer userId) {
        Image image = find(userId).getImage();
        if (image == null) {
            throw new ResourceNotFoundException("El usuario no tiene imagen");
        }
        image.getContent(); // inicializa el contenido lazy dentro de la transacción
        return image;
    }

    private User find(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }
}
