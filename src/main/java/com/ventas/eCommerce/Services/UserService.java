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
import com.ventas.eCommerce.repositories.CartRepository;
import com.ventas.eCommerce.repositories.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import javax.xml.bind.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author chris
 */
@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private CartService cartService;

    @Transactional
    public void Register(String name, String lastName, MultipartFile file, String email, String password, String password2, String phone, Rol rol) throws MyException {
        validar(name, lastName, email, password, password2, phone, rol);
        User user = new User();

        Cart cart = new Cart();
        cartRepository.save(cart);

        user.setName(name);
        user.setLastName(lastName);
        Image image = imageService.guardarImagen(file);
        user.setImage(image);
        user.setEmail(email);
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        user.setPhone(phone);
        user.setRol(rol);
        user.setCart(cart);
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
            user.setLastName(lastName);
            user.setEmail(email);
            user.setLastName(lastName);
            user.setPassword(password);
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

    public User getOne(Integer id) {
        return userRepository.getOne(id);
    }

    public void validar(String name, String lastName, String email, String password, String password2, String phone, Rol rol) throws MyException {

        User usuarioExistente = userRepository.findByEmail(email);
        if (usuarioExistente != null) {
            throw new MyException("Ya existe un usuario registrado con ese email");
        }

        User usuarioExistente2 = userRepository.findByPhone(phone);
        if (usuarioExistente2 != null) {
            throw new MyException("Ya existe un usuario con ese número de teléfono");
        }
        if (name.isEmpty() || name == null) {
            throw new MyException("El Nombre no puede ser nulo o estar vacio");
        }
        if (lastName == null) {
            throw new MyException("El Apellido no puede ser nulo o estar vacio");
        } else {
        }
        if (email.isEmpty() || email == null) {
            throw new MyException("El email es no puede ser nulo o estar vacio");
        }
        if (!email.contains(".") || !email.contains("@")) {
            throw new MyException("El email es incorrecto, por favor verificarlo");
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

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email);

        if (user != null) {
            List<GrantedAuthority> permisos = new ArrayList();
            GrantedAuthority p = new SimpleGrantedAuthority("ROLE_" + user.getRol().toString());
            permisos.add(p);

            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession(true);
            session.setAttribute("usuariosession", user);
            return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), permisos);
        } else {
            return null;
        }
    }
}
