package com.ventas.eCommerce.dto;

import com.ventas.eCommerce.entities.Product;
import com.ventas.eCommerce.enums.Category;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * Datos del formulario de alta/edición de productos del panel de administración.
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductForm {

    private Integer id;

    /** Versión con la que se abrió el formulario, para detectar ediciones concurrentes. */
    private Long version;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no puede superar los 255 caracteres")
    private String name;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
    private String description;

    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 255)
    private String brand;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "El precio admite hasta 2 decimales")
    private BigDecimal price;

    @NotNull(message = "Seleccioná una categoría")
    private Category category;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    private boolean active = true;

    private MultipartFile image;

    private boolean hasImage;

    public boolean isNew() {
        return id == null;
    }

    public static ProductForm from(Product product) {
        ProductForm form = new ProductForm();
        form.setId(product.getId());
        form.setVersion(product.getVersion());
        form.setName(product.getName());
        form.setDescription(product.getDescription());
        form.setBrand(product.getBrand());
        form.setPrice(product.getPrice());
        form.setCategory(product.getCategory());
        form.setStock(product.getStock());
        form.setActive(product.isActive());
        form.setHasImage(product.hasImage());
        return form;
    }
}
