package com.ventas.eCommerce.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Autentica las peticiones de la API leyendo el header {@code Authorization: Bearer <token>}.
 * No se registra como bean para que Spring Boot no lo aplique a todas las rutas: solo lo usa
 * la cadena de seguridad de {@code /api/**}.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith(BEARER_PREFIX)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = header.substring(BEARER_PREFIX.length()).trim();
            jwtService.validateAndGetSubject(token).ifPresent(email -> authenticate(email, request));
        }

        chain.doFilter(request, response);
    }

    private void authenticate(String email, HttpServletRequest request) {
        try {
            // Se recarga el usuario para respetar bajas o cambios de rol posteriores a la emisión del token.
            AuthUser user = userDetailsService.loadUserByUsername(email);
            if (!user.isEnabled()) {
                return;
            }
            var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (UsernameNotFoundException ignored) {
            // Token de un usuario que ya no existe: la petición sigue como anónima.
        }
    }
}
