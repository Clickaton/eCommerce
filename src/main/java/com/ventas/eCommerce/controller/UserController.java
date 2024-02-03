/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.ventas.eCommerce.controller;

import com.ventas.eCommerce.Services.CartService;
import com.ventas.eCommerce.Services.UserService;
import com.ventas.eCommerce.entities.Cart;
import com.ventas.eCommerce.entities.Product;
import com.ventas.eCommerce.entities.User;
import com.ventas.eCommerce.enums.Rol;
import com.ventas.eCommerce.exceptions.MyException;
import com.ventas.eCommerce.repositories.CartRepository;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSession;
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
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private CartRepository cartRepository;
    
     @Autowired
    private CartService cartService;

    @GetMapping("/register")
    public String register() {

        return "UserForm.html";
    }

    @PostMapping("/registed")
    public String registro_usuario(@RequestParam(required = false)String name, @RequestParam(required = false) String lastName, MultipartFile file, String email, String password, String password2, String phone, String rol, ModelMap model) throws MyException{
        try {
            Rol rolEnum = Rol.USER;
            userService.Register(name, lastName, file, email, password, password2, phone, rolEnum);
            model.put("exito", "El usuario se ha registrado correctamente");
        } catch (MyException ex) {
            model.put("error", ex.getMessage());
        }
        return "UserForm.html";
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error, ModelMap model){
        
        if (error != null) {
            model.put("error", "Usuario o contraseña invalidos!");
        }
        
    return "login.html";
    }
    
    @GetMapping("/cart/{id}")
    public String cart(HttpSession session, ModelMap model){
             User logueado=(User) session.getAttribute("usuariosession");
             Cart carrito = logueado.getCart();
             List<Product> products = carrito.getProducts();
            
       // List<Product> listaProductos = userService. ;
        //modelo.addAttribute("listaProductos", listaProductos);
    return "cart.html";
    }

}
