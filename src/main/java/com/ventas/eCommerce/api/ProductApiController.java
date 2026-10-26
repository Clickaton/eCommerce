package com.ventas.eCommerce.api;

import com.ventas.eCommerce.dto.ProductResponse;
import com.ventas.eCommerce.enums.Category;
import com.ventas.eCommerce.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductApiController {

    private final ProductService productService;

    @GetMapping
    public Page<ProductResponse> list(@RequestParam(required = false) String q,
                                      @RequestParam(required = false) Category category,
                                      @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return productService.catalogue(q, category, pageable);
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Integer id) {
        return productService.getActive(id);
    }
}
