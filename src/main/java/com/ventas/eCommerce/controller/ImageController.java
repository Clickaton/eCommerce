/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ventas.eCommerce.controller;

import com.ventas.eCommerce.Services.ProductService;
import com.ventas.eCommerce.Services.UserService;
import com.ventas.eCommerce.entities.Product;
import com.ventas.eCommerce.entities.User;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author chris
 */
@Controller
@RequestMapping("/image")
public class ImageController {
    
    @Autowired
    ProductService productService;
    
    @Autowired
    UserService userService;
    
    @GetMapping("/products/{id}")
    public ResponseEntity<byte[]> productImage (@PathVariable Integer id){
        Product product = productService.getOne(id);
        
        byte[] image = product.getImage().getContent();
       
       HttpHeaders headers = new HttpHeaders();
       
       headers.setContentType(MediaType.IMAGE_JPEG);
       
       return new ResponseEntity<>(image,headers,HttpStatus.OK);
    }
    
        @GetMapping("/perfil/{id}")
    public ResponseEntity<byte[]> imagenUsuario (@PathVariable Integer id, HttpSession session ){
        User user = userService.getOne(id);
       
       byte[] image = user.getImage().getContent();
       
       HttpHeaders headers = new HttpHeaders();
       
       headers.setContentType(MediaType.IMAGE_JPEG);
       
       return new ResponseEntity<>(image, headers, HttpStatus.OK);
       
    }
    
}
