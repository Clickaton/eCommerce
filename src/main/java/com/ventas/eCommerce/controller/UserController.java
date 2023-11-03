/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.ventas.eCommerce.controller;

import com.ventas.eCommerce.Services.UserService;
import com.ventas.eCommerce.enums.Rol;
import com.ventas.eCommerce.exceptions.MyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/register")
    public String register() {

        return "UserForm.html";
    }

    @PostMapping("/registed")
    public String registro_usuario(@RequestParam(required = false)String name, @RequestParam(required = false) String lastName, MultipartFile file, String email, String password, String password2, String phone, String rol, ModelMap model) throws MyException{
        System.out.println("error1");
        try {
            System.out.println("error2");
            Rol rolEnum = Rol.USER;
            System.out.println("Error3");
            userService.Register(name, lastName, file, email, password, password2, phone, rolEnum);
            model.put("exito", "El usuario se ha registrado correctamente");
            System.out.println("errir4");
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

}
