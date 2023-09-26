/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ventas.eCommerce.controller;

import com.ventas.eCommerce.Services.ProductService;
import com.ventas.eCommerce.entities.Image;
import com.ventas.eCommerce.enums.Category;
import com.ventas.eCommerce.exceptions.MyException;
import java.util.logging.Level;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public String registro_producto(@RequestParam String name, String description, Image image, String brand, Double price, Category category, Boolean creationDeletion, Integer stock){
        try {
            productService.Register(name, description, image, brand, price, Category.JEWELRY, Boolean.TRUE, Integer.SIZE);
        } catch (MyException ex) {
            System.out.println("Error");
        }
        return "index.html";
    }
    
}
