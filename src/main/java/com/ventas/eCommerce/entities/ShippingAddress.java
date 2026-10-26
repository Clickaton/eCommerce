package com.ventas.eCommerce.entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddress {

    private String recipientName;
    private String street;
    private String city;
    private String province;
    private String postalCode;
    private String phone;

    @Override
    public String toString() {
        return street + ", " + city + " (CP " + postalCode + "), " + province;
    }
}
