/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ventas.eCommerce.controller;

import com.ventas.eCommerce.Services.ProductService;
import com.ventas.eCommerce.entities.Image;
import com.ventas.eCommerce.entities.Product;
import com.ventas.eCommerce.enums.Category;
import com.ventas.eCommerce.exceptions.MyException;
import java.util.List;
import java.util.logging.Level;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author chris
 */
@Controller
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/register")
    public String registrar_producto() {

        return "ProductForm.html";
    }

    @PostMapping("/registed")
    public String registro_producto(@RequestParam(required = false)  String name, String description, Image image, String brand, MultipartFile file, Double price, String category, Boolean creationDeletion, Integer stock, ModelMap model) {
        try {
            Category categoryEnum = Category.valueOf(category);
            productService.Register(name, description, file, brand, price, categoryEnum, creationDeletion, stock);
            model.put("exito", "EL producto se ha registrado correctamente.");
            return "ProductForm.html";
        } catch (MyException ex) {
            model.put("error", ex.getMessage());
            
        }
        return "ProductForm.html";
    }
    
    @GetMapping("/catalogue")
    public String registro_catalogo(ModelMap modelo) {
        List<Product> listaProductos = productService.productList();
        modelo.addAttribute("listaProductos", listaProductos);
        return "Catalogue.html";
    }
    

    
}
