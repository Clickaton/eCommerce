package com.ventas.eCommerce.security;

import com.ventas.eCommerce.entities.User;
import com.ventas.eCommerce.enums.Rol;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Usuario autenticado. Reemplaza al antiguo atributo de sesión "usuariosession": solo guarda
 * datos inmutables de identidad, nunca la entidad JPA (que quedaba desactualizada en sesión).
 */
public record AuthUser(Integer id, String email, String password, String name, Rol rol, boolean enabled)
        implements UserDetails {

    public static AuthUser from(User user) {
        return new AuthUser(user.getId(), user.getEmail(), user.getPassword(), user.getName(), user.getRol(),
                user.isEnabled());
    }

    public boolean isAdmin() {
        return rol == Rol.ADMIN;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return "AuthUser[id=" + id + ", email=" + email + ", rol=" + rol + "]";
    }
}
