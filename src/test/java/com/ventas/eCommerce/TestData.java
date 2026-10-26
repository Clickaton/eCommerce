package com.ventas.eCommerce;

import com.ventas.eCommerce.dto.CheckoutForm;
import com.ventas.eCommerce.entities.Product;
import com.ventas.eCommerce.entities.User;
import com.ventas.eCommerce.enums.Category;
import com.ventas.eCommerce.enums.Rol;
import com.ventas.eCommerce.repositories.ProductRepository;
import com.ventas.eCommerce.repositories.UserRepository;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class TestData {

    public static final String PASSWORD = "password123";

    private static final AtomicInteger SEQ = new AtomicInteger();

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public TestData(UserRepository userRepository, ProductRepository productRepository,
                    PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User user(Rol rol) {
        int n = SEQ.incrementAndGet();
        User user = new User();
        user.setName("Test" + n);
        user.setLastName("User");
        user.setEmail("user" + n + "@test.com");
        user.setPhone(String.valueOf(1100000000L + n));
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setRol(rol);
        return userRepository.save(user);
    }

    public Product product(String price, int stock) {
        Product product = new Product();
        product.setName("Perfume " + SEQ.incrementAndGet());
        product.setDescription("Descripción");
        product.setBrand("Marca");
        product.setPrice(new BigDecimal(price));
        product.setCategory(Category.FRAGRANCES);
        product.setStock(stock);
        return productRepository.save(product);
    }

    public static CheckoutForm checkoutForm() {
        CheckoutForm form = new CheckoutForm();
        form.setRecipientName("Juan Pérez");
        form.setStreet("Av. Siempre Viva 742");
        form.setCity("CABA");
        form.setProvince("Buenos Aires");
        form.setPostalCode("1000");
        form.setPhone("1131188134");
        return form;
    }
}
