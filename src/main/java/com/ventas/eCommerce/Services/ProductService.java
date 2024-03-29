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
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author chris
 */
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ImageService imageService;

    @Transactional
    public void Register(String name, String description, MultipartFile file, String brand, Double price, Category category, Boolean creationDeletion, Integer stock) throws MyException {
        validar(name, description, file, brand, price, category, creationDeletion, stock);
        Product product = new Product();

        product.setName(name);
        product.setDescription(description);
        Image image = imageService.guardarImagen(file);
        product.setImage(image);
        product.setBrand(brand);
        product.setPrice(price);
        product.setCategory(category);
        product.setCreationDeletion(creationDeletion);
        product.setStock(stock);

        productRepository.save(product);
    }

    public void modificar(Integer id, String name, String description, MultipartFile file, String brand, Double price, Category category, Boolean creationDeletion, Integer stock) throws MyException {

        Optional<Product> respuesta = productRepository.findById(id);

        if (respuesta.isPresent()) {
            Product product = respuesta.get();
            product.setName(name);
            product.setDescription(description);
            product.setBrand(brand);
            product.setPrice(price);
            product.setCategory(category);
            product.setCreationDeletion(creationDeletion);
            product.setStock(stock);

            Integer IdImagen = null;

            if (product.getImage() != null) {
                IdImagen = product.getImage().getId();
            }

            Image image = imageService.actualizarImagen(file, IdImagen);
            product.setImage(image);
            productRepository.save(product);
        }

    }

    public List<Product> productList() {

        List<Product> products = new ArrayList();

        products = productRepository.findAll();

        return products;
    }

    public Product getOne(Integer id) {
        return productRepository.getOne(id);
    }

    public void validar(String name, String description, MultipartFile file, String brand, Double price, Category category, Boolean creationDeletion, Integer stock) throws MyException {

        if (name == null || name.isEmpty()) {
            throw new MyException("El nombre del producto no puede ser nulo o estar vacío.");
        }
        if (description == null || description.isEmpty()) {
            throw new MyException("La descripción no puede estar vacía o estar nula.");
        }
        if (file == null || file.isEmpty()) {
            throw new MyException("Debe seleccionar una imagen");
        }
        if (brand.isEmpty() || brand == null) {
            throw new MyException("La marca no puede ser nula o estar vacia");
        }
        if (price == null) {
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
