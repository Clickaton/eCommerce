package com.ventas.eCommerce.config;

import com.ventas.eCommerce.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Crea el administrador inicial definido en {@code app.admin.email} / {@code app.admin.password}
 * (idealmente por variables de entorno). Sin esto no hay forma de entrar al panel la primera vez.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final AppProperties properties;
    private final UserService userService;

    @Override
    public void run(ApplicationArguments args) {
        AppProperties.Admin admin = properties.admin();
        if (admin == null || !StringUtils.hasText(admin.email()) || !StringUtils.hasText(admin.password())) {
            return;
        }
        if (userService.createAdminIfMissing(admin.email(), admin.password(), admin.name())) {
            log.info("Administrador inicial creado: {}", admin.email());
        }
    }
}
