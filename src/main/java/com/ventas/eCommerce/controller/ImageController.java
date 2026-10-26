package com.ventas.eCommerce.controller;

import com.ventas.eCommerce.entities.Image;
import com.ventas.eCommerce.services.ProductService;
import com.ventas.eCommerce.services.UserService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/image")
@RequiredArgsConstructor
public class ImageController {

    private final ProductService productService;
    private final UserService userService;

    @GetMapping("/products/{id}")
    public ResponseEntity<byte[]> productImage(@PathVariable Integer id) {
        return toResponse(productService.getImage(id));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<byte[]> userImage(@PathVariable Integer id) {
        return toResponse(userService.getImage(id));
    }

    private static ResponseEntity<byte[]> toResponse(Image image) {
        MediaType type = image.getMime() != null ? MediaType.parseMediaType(image.getMime()) : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok()
                .contentType(type)
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(10)))
                .body(image.getContent());
    }
}
