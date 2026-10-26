package com.ventas.eCommerce.enums;

public enum OrderStatus {
    PENDING("Pendiente de pago", "warning"),
    PAID("Pagado", "success"),
    SHIPPED("Enviado", "info"),
    DELIVERED("Entregado", "primary"),
    CANCELLED("Cancelado", "secondary");

    private final String label;
    private final String badge;

    OrderStatus(String label, String badge) {
        this.label = label;
        this.badge = badge;
    }

    public String getLabel() {
        return label;
    }

    /** Color de Bootstrap usado para mostrar el estado. */
    public String getBadge() {
        return badge;
    }

    /** Transiciones de estado permitidas. */
    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case PENDING -> next == PAID || next == CANCELLED;
            case PAID -> next == SHIPPED || next == CANCELLED;
            case SHIPPED -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
