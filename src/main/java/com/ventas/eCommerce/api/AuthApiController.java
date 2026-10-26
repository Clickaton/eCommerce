package com.ventas.eCommerce.api;

import com.ventas.eCommerce.dto.ApiDtos.LoginRequest;
import com.ventas.eCommerce.dto.ApiDtos.TokenResponse;
import com.ventas.eCommerce.dto.UserResponse;
import com.ventas.eCommerce.security.AuthUser;
import com.ventas.eCommerce.security.JwtService;
import com.ventas.eCommerce.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    /** Valida email y contraseña y devuelve un JWT para usar en {@code Authorization: Bearer <token>}. */
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password()));
        AuthUser user = (AuthUser) authentication.getPrincipal();
        return new TokenResponse(jwtService.generateToken(user), "Bearer", jwtService.getExpirationSeconds());
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthUser user) {
        return userService.get(user.id());
    }
}
