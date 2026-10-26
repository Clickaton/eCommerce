package com.ventas.eCommerce.config;

import com.ventas.eCommerce.entities.Product;
import com.ventas.eCommerce.enums.Category;
import com.ventas.eCommerce.repositories.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Carga un catálogo de ejemplo en una base vacía. Solo se activa con {@code app.seed-sample-data=true}
 * (perfil dev).
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.seed-sample-data", havingValue = "true")
@RequiredArgsConstructor
public class SampleDataSeeder implements ApplicationRunner {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (productRepository.count() > 0) {
            return;
        }
        List<Product> products = List.of(
                product("Bleu Intense", "Antogn", "45999.90", 12, "Fragancia amaderada y cítrica con notas de pomelo, incienso y cedro."),
                product("Rose Noir", "Antogn", "52500.00", 6, "Rosa damascena, pachulí y vainilla. Intensa y elegante para la noche."),
                product("Citrus Fresh", "Antogn", "31200.00", 20, "Bergamota, limón y neroli. Fresca y ligera para el día a día."),
                product("Ámbar Oriental", "Antogn", "61000.00", 3, "Ámbar, especias y maderas cálidas. Edición limitada."),
                product("Vetiver Club", "Antogn", "48750.00", 1, "Vetiver de Haití con un toque de pimienta rosa."),
                product("Jasmin Blanc", "Antogn", "39900.00", 0, "Jazmín sambac y almizcle blanco. Delicada y luminosa."));
        productRepository.saveAll(products);
        log.info("Se cargaron {} productos de ejemplo", products.size());
    }

    private static Product product(String name, String brand, String price, int stock, String description) {
        Product product = new Product();
        product.setName(name);
        product.setBrand(brand);
        product.setPrice(new BigDecimal(price));
        product.setStock(stock);
        product.setDescription(description);
        product.setCategory(Category.FRAGRANCES);
        return product;
    }
}
