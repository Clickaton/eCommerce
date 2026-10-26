package com.ventas.eCommerce.services;

import com.ventas.eCommerce.entities.Image;
import com.ventas.eCommerce.exceptions.BusinessException;
import java.io.IOException;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageService {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    /**
     * Límite por imagen. Debe quedar por debajo de max_allowed_packet de MySQL (4 MB por defecto en 5.7),
     * porque la imagen viaja entera en un solo INSERT.
     */
    public static final long MAX_SIZE = 2L * 1024 * 1024;

    public boolean isPresent(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    /**
     * Copia el archivo subido sobre {@code target} (o sobre una imagen nueva si es null).
     * Devuelve {@code target} sin cambios si no se subió ningún archivo.
     */
    public Image apply(MultipartFile file, Image target) {
        if (!isPresent(file)) {
            return target;
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BusinessException("La imagen debe ser JPG, PNG, WEBP o GIF");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("La imagen no puede superar los 2 MB");
        }

        Image image = target != null ? target : new Image();
        try {
            image.setContent(file.getBytes());
        } catch (IOException e) {
            throw new BusinessException("No se pudo leer la imagen subida", e);
        }
        image.setMime(file.getContentType());
        image.setName(file.getOriginalFilename());
        return image;
    }
}
