/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ventas.eCommerce.Services;

import com.ventas.eCommerce.entities.Image;
import com.ventas.eCommerce.exceptions.MyException;
import com.ventas.eCommerce.repositories.ImageRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author chris
 */
@Service
public class ImageService {

    @Autowired
    private ImageRepository imagenRepositorio;

    public Image guardarImagen(MultipartFile file) throws MyException{ 
       
        if (file != null) {
            try {
                Image image = new Image();

                image.setMime(file.getContentType());
                image.setName(file.getName());
                image.setContent(file.getBytes());

                return imagenRepositorio.save(image);
            } catch (Exception e) {
                System.out.println(e.getMessage());

            }
        }
        return null;
    }

    public Image actualizarImagen(MultipartFile file, Integer id) throws MyException { //cambio nombre del método: actualizar -> actualizarImagen - gise

        if (file != null) {
            try {
                Image image = new Image();

                if (id != null) {
                    Optional<Image> respuesta = imagenRepositorio.findById(id);
                    if (respuesta.isPresent()) {
                        image = respuesta.get();
                    }

                }

                image.setMime(file.getContentType());
                image.setName(file.getName());
                image.setContent(file.getBytes());

                return imagenRepositorio.save(image);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                throw new MyException("Error al actualizar imagen"); //agrego mensaje de excepción - gise
            }
        }
        return null;
    }

    //Agrego método para eliminar imagen - gise
    public Image eliminarImagen(MultipartFile archivo, Integer id) throws MyException {

        if (archivo != null) {

            try {
                Image imagen;

                if (id != null) {
                    Optional<Image> respuesta = imagenRepositorio.findById(id);

                    if (respuesta.isPresent()) {
                        imagen = respuesta.get();
                        imagenRepositorio.delete(imagen);
                    }
                }

            } catch (Exception e) {
                System.err.println(e.getMessage());
                throw new MyException("Error al eliminar imagen");
            }
        }
        return null;
    }

}
