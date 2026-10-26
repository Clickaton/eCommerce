package com.ventas.eCommerce.enums;

public enum Category {
    ELECTRONICDEVICES("Electrónica"),
    SERVICES("Servicios"),
    FOOD("Alimentos"),
    INDUMENTARY("Indumentaria"),
    JEWELRY("Joyería"),
    ENTERTAINMENT("Entretenimiento"),
    RAW_MATERIAL("Materia prima"),
    CLEANLINESS("Limpieza"),
    CONSTRUCTION("Construcción"),
    FRAGRANCES("Fragancias");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
