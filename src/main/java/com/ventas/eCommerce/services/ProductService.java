package com.ventas.eCommerce.services;

import com.ventas.eCommerce.dto.ProductForm;
import com.ventas.eCommerce.dto.ProductResponse;
import com.ventas.eCommerce.entities.Image;
import com.ventas.eCommerce.entities.Product;
import com.ventas.eCommerce.enums.Category;
import com.ventas.eCommerce.exceptions.BusinessException;
import com.ventas.eCommerce.exceptions.ResourceNotFoundException;
import com.ventas.eCommerce.repositories.ProductRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ProductService {

    public static final int LOW_STOCK_THRESHOLD = 5;

    private final ProductRepository productRepository;
    private final ImageService imageService;

    @Transactional(readOnly = true)
    public Page<ProductResponse> catalogue(String q, Category category, Pageable pageable) {
        return productRepository.search(normalize(q), category, true, pageable).map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> adminSearch(String q, Category category, Pageable pageable) {
        return productRepository.search(normalize(q), category, false, pageable).map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> featured() {
        return productRepository.findTop8ByActiveTrueOrderByIdDesc().stream().map(ProductResponse::from).toList();
    }

    /** Detalle visible para clientes: los productos dados de baja no se muestran. */
    @Transactional(readOnly = true)
    public ProductResponse getActive(Integer id) {
        Product product = find(id);
        if (!product.isActive()) {
            throw new ResourceNotFoundException("El producto no está disponible");
        }
        return ProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public ProductForm getForm(Integer id) {
        return ProductForm.from(find(id));
    }

    @Transactional
    public ProductResponse create(ProductForm form) {
        if (!imageService.isPresent(form.getImage())) {
            throw new BusinessException("Seleccioná una imagen para el producto");
        }
        Product product = new Product();
        apply(form, product);
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Integer id, ProductForm form) {
        Product product = find(id);
        if (form.getVersion() != null && !Objects.equals(form.getVersion(), product.getVersion())) {
            throw new BusinessException("El producto fue modificado (por ejemplo, por una venta) mientras lo "
                    + "editabas. Revisá los datos actuales y volvé a guardar.");
        }
        apply(form, product);
        return ProductResponse.from(product);
    }

    @Transactional
    public void setActive(Integer id, boolean active) {
        find(id).setActive(active);
    }

    @Transactional(readOnly = true)
    public Image getImage(Integer productId) {
        Image image = find(productId).getImage();
        if (image == null) {
            throw new ResourceNotFoundException("El producto no tiene imagen");
        }
        image.getContent(); // inicializa el contenido lazy dentro de la transacción
        return image;
    }

    private void apply(ProductForm form, Product product) {
        product.setName(form.getName().trim());
        product.setDescription(form.getDescription().trim());
        product.setBrand(form.getBrand().trim());
        product.setPrice(form.getPrice());
        product.setCategory(form.getCategory());
        product.setStock(form.getStock());
        product.setActive(form.isActive());
        product.setImage(imageService.apply(form.getImage(), product.getImage()));
    }

    private Product find(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
    }

    private static String normalize(String q) {
        return StringUtils.hasText(q) ? q.trim() : null;
    }
}
