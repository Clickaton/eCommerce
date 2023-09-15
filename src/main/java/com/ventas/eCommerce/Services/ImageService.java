/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ventas.eCommerce.Services;

import com.ventas.eCommerce.entities.Image;
import com.ventas.eCommerce.repositories.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author chris
 */
//@Service
//public class ImageService {
//    @Autowired
//    private ImageRepository imagenRepositorio;
//    
//    public Image guardarImagen(MultipartFile archivo) /*throws MiException*/{ //cambio nombre del método: guardar -> guardarImagen - gise
//        if (archivo != null){
//            try{
//                Image image = new Image();
//                
//                image.setMime(archivo.getContentType());
//                image.setName(archivo.getName());
//                image.setContenido(archivo.getBytes());
//             
//                return imagenRepositorio.save(image);
//            }catch (Exception e) {
//                System.err.println(e.getMessage());
//                throw new MiException("Error al guardar imagen"); //agrego mensaje de excepción - gise
//            }
//        }
//        return null;
//    }
//    
//    public Imagen actualizarImagen(MultipartFile archivo, String idImagen) throws MiException{ //cambio nombre del método: actualizar -> actualizarImagen - gise
//        
//        if(archivo != null){
//            try {
//                Imagen imagen = new Imagen();
//                
//                if(idImagen != null){
//                    Optional<Imagen> respuesta = imagenRepositorio.findById(idImagen);
//                    if(respuesta.isPresent()){
//                        imagen = respuesta.get();
//                    }
//
//                }
//                
//                imagen.setMime(archivo.getContentType());
//                imagen.setNombre(archivo.getName());
//                imagen.setContenido(archivo.getBytes());
//                
//                return imagenRepositorio.save(imagen);
//            } catch (Exception e) {
//                System.err.println(e.getMessage());
//                throw new MiException("Error al actualizar imagen"); //agrego mensaje de excepción - gise
//            }
//        }      
//        return null;
//    }
//    
//    //Agrego método para eliminar imagen - gise
//    public Imagen eliminarImagen(MultipartFile archivo, String idImagen) throws MiException{
//        
//        if(archivo != null){
//            
//            try {
//                Imagen imagen;
//                
//                if(idImagen != null){
//                    Optional<Imagen> respuesta = imagenRepositorio.findById(idImagen);
//                    
//                    if(respuesta.isPresent()){
//                       imagen = respuesta.get();
//                       imagenRepositorio.delete(imagen);
//                    }
//                }
//                
//            } catch (Exception e) {
//                System.err.println(e.getMessage());
//                throw new MiException("Error al eliminar imagen");
//            }
//        }      
//        return null;
//    }
//
//}
