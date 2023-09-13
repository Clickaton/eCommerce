/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ventas.eCommerce.Services;

import com.ventas.eCommerce.entities.Image;
import com.ventas.eCommerce.entities.Product;
import com.ventas.eCommerce.enums.Category;
import com.ventas.eCommerce.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author chris
 */
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    public void Register(String name, String description, Image image, String brand, Double price, Category category, Boolean creationDeletion, Integer stock){
        Product product = new Product();
        
        product.setName(name);
        product.setDescription(description);
        product.setImage(image);
        product.setBrand(brand);
        product.setPrice(price);
        product.setCategory(category);
        product.setCreationDeletion(creationDeletion);
        product.setStock(stock);
        
        productRepository.save(product);
    }
    
}
