/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ventas.eCommerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    
    @GetMapping("/register")
    public String register(){
        
        return "register.html";
    }
    
}
