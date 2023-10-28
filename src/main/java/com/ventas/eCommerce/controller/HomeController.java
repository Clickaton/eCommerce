/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ventas.eCommerce.controller;

import com.ventas.eCommerce.enums.Rol;
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
@RequestMapping("/")
public class HomeController {
    
    @GetMapping("/")
    public String index() {
    
        return "index.html";
    }
    
    
//    @PostMapping("/registed")
//    public String registed(@RequestParam(required = false)  String name, String lastName, MultipartFile file, String password, String password2, String phone, Rol rol|12, ModelMap model) {
//        try {
//            Category categoryEnum = Category.valueOf(category);
//            productService.Register(name, description, file, brand, price, categoryEnum, creationDeletion, stock);
//            model.put("exito", "EL producto se ha registrado correctamente.");
//            return "ProductForm.html";
//        } catch (MyException ex) {
//            model.put("error", ex.getMessage());
//            
//        }
//        return "ProductForm.html";
//    
//        return "index.html";
//    }
    
}
