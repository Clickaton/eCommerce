/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ventas.eCommerce.Services;

import com.ventas.eCommerce.entities.Image;
import com.ventas.eCommerce.entities.Product;
import com.ventas.eCommerce.enums.Category;
import com.ventas.eCommerce.exceptions.MyException;
import com.ventas.eCommerce.repositories.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import javax.xml.bind.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author chris
 */
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public void Register(String name, String description, Image image, String brand, Double price, Category category, Boolean creationDeletion, Integer stock) throws MyException{
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
    
    public void modificar(Integer id, String name, String description, Image image, String brand, Double price, Category category, Boolean creationDeletion, Integer stock){
    
    
        Optional<Product> respuesta = productRepository.findById(id);
        
        if (respuesta.isPresent()) {
            Product product = respuesta.get();
            
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

    public List<Product> productList() {
       
        List<Product> products = new ArrayList();
        
        products = productRepository.findAll();
        
        return products;
    }

        public void validar(String name, String description, Image image, String brand, Double price, Category category, Boolean creationDeletion, Integer stock) throws MyException, ValidationException{
        
        if (name != null || name.isEmpty()) {
            throw new ValidationException("El nombre del producto no puede ser nulo o estar vacío.");
        }
        if (description != null || description.isEmpty() ) {
            throw new ValidationException("La descripción no puede estar vacía o estar nula.");
        }
        if (image == null) {
            throw new MyException("Debe seleccionar una imagen");
        }
        if (brand.isEmpty() || brand == null) {
            throw new MyException("La marca no puede ser nula o estar vacia");
        }
        if ( price == null) {
            throw new MyException("Debe ingresar un precio.");
        }
        if (category == null) {
            throw new MyException("Debe seleccionar una categoría.");
        }
        if (stock == null) {
            throw new MyException("El stock no puede estar vacío, ingrese un número válido.");
        }
    }
    
}
