package com.ventas.eCommerce.config;

import com.ventas.eCommerce.security.AuthUser;
import com.ventas.eCommerce.security.CustomUserDetailsService;
import com.ventas.eCommerce.security.JwtAuthenticationFilter;
import com.ventas.eCommerce.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Seguridad híbrida:
 * <ul>
 *   <li>{@code /api/**}: stateless, autenticación con JWT ({@code Authorization: Bearer <token>}).</li>
 *   <li>Resto (vistas Thymeleaf): sesión con login por formulario y protección CSRF.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurity(HttpSecurity http, JwtService jwtService,
                                           CustomUserDetailsService userDetailsService) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable()) // sin cookies de sesión no hay riesgo de CSRF
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, e) ->
                                writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "No autenticado"))
                        .accessDeniedHandler((request, response, e) ->
                                writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Acceso denegado")))
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webSecurity(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/resources/**", "/favicon.ico").permitAll()
                        .requestMatchers("/", "/products", "/products/**", "/image/**").permitAll()
                        .requestMatchers("/user/register", "/user/login").permitAll()
                        .requestMatchers("/payments/webhook/**", "/payments/return").permitAll()
                        .requestMatchers("/error", "/actuator/health").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/user/login")
                        .loginProcessingUrl("/logincheck")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(loginSuccessHandler())
                        .failureUrl("/user/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/user/login?logout")
                        .permitAll())
                // Los webhooks los envía la pasarela (sin cookie ni token CSRF); se validan por firma.
                .csrf(csrf -> csrf.ignoringRequestMatchers("/payments/webhook/**"));
        return http.build();
    }

    /** Los administradores van al panel; el resto, a la página que intentaba ver o al inicio. */
    private SavedRequestAwareAuthenticationSuccessHandler loginSuccessHandler() {
        var handler = new SavedRequestAwareAuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(jakarta.servlet.http.HttpServletRequest request,
                                                HttpServletResponse response,
                                                org.springframework.security.core.Authentication authentication)
                    throws java.io.IOException, jakarta.servlet.ServletException {
                if (authentication.getPrincipal() instanceof AuthUser user && user.isAdmin()) {
                    getRedirectStrategy().sendRedirect(request, response, "/admin");
                    return;
                }
                super.onAuthenticationSuccess(request, response, authentication);
            }
        };
        handler.setDefaultTargetUrl("/");
        return handler;
    }

    private static void writeJsonError(HttpServletResponse response, int status, String detail)
            throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":" + status + ",\"detail\":\"" + detail + "\"}");
    }
}
