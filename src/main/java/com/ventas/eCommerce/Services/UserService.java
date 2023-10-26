/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ventas.eCommerce.Services;

import com.ventas.eCommerce.entities.Cart;
import com.ventas.eCommerce.entities.Image;
import com.ventas.eCommerce.entities.User;
import com.ventas.eCommerce.enums.Rol;
import com.ventas.eCommerce.exceptions.MyException;
import com.ventas.eCommerce.repositories.UserRepository;
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
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageService imageService;

    @Transactional
    public void Register(String name, String lastName, MultipartFile file, String password, String password2, String phone, Rol rol) throws MyException {
        User user = new User();

        user.setName(name);
        user.setLastName(lastName);
        Image image = imageService.guardarImagen(file);
        user.setImage(image);
        user.setPassword(password);
        user.setPassword2(password2);
        user.setPhone(phone);
        user.setRol(rol);

        userRepository.save(user);

    }

    public List<User> userList() {

        List<User> users = new ArrayList();

        users = userRepository.findAll();

        return users;
    }

    public void update(Integer id, String name, String email, String lastName, MultipartFile file, String password, String password2, String phone, Rol rol) throws MyException {

        Optional<User> respuesta = userRepository.findById(id);

        if (respuesta.isPresent()) {
            User user = respuesta.get();
            user.setName(name);
            user.setEmail(email);
            user.setLastName(lastName);
            user.setPassword(password);
            user.setPassword2(password2);
            user.setPhone(phone);
            user.setRol(rol);

            Integer IdImagen = null;

            if (user.getImage() != null) {
                IdImagen = user.getImage().getId();
            }

            Image image = imageService.actualizarImagen(file, IdImagen);
            user.setImage(image);
            userRepository.save(user);
        }
    }

    public void validar(String name, String lastName, String email, Image image, String password, String password2, String phone, Rol rol) throws MyException, ValidationException {

        User usuarioExistente = userRepository.findByEmail(email);
        if (usuarioExistente != null) {
            throw new ValidationException("Ya existe un usuario registrado con ese email");
        }

        User usuarioExistente2 = userRepository.findByPhone(phone);
        if (usuarioExistente2 != null) {
            throw new ValidationException("Ya existe un usuario con ese número de teléfono");
        }
        if (name.isEmpty() || name == null) {
            throw new MyException("El Nombre no puede ser nulo o estar vacio");
        }
        if (lastName.isEmpty() || lastName == null) {
            throw new MyException("El Apellido no puede ser nulo o estar vacio");
        }
        if (email.isEmpty() || email == null) {
            throw new MyException("El email no puede ser nulo o estar vacio");
        }
        if (!email.contains(".com") || !email.contains("@")) {
            throw new MyException("El email debe contener @ y .com");
        }
        if (phone.isEmpty() || phone == null || phone.length() < 10) {
            throw new MyException("El Telefono no puede estar vacio y debe tener 10 numeros incluyendo el codigo de area");
        }
        if (password.isEmpty() || password == null || password.length() <= 5) {
            throw new MyException("La Contraseña no puede estar vacia y debe tener mas de 5 digitos");
        }
        if (!password.equals(password2)) {
            throw new MyException("Las Contraseñas ingresadas deben ser iguales");
        }

    }
}
