package com.ventas.eCommerce.entities;

import com.ventas.eCommerce.enums.Category;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Image image;

    private String brand;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private Category category;

    /** Borrado lógico: un producto inactivo no aparece en el catálogo ni puede comprarse. */
    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private int stock;

    /** Bloqueo optimista: una edición concurrente sobre una versión vieja falla en lugar de pisar datos. */
    @Version
    private Long version;

    public boolean isAvailable() {
        return active && stock > 0;
    }

    public boolean hasImage() {
        return image != null;
    }
}
