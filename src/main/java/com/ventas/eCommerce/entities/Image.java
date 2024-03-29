/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ventas.eCommerce.entities;


import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 *
 * @author chris
 */
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
public class Image {

   @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
   
   private String mime;
   private String name;
   @Lob @Basic(fetch = FetchType.LAZY) //Informo a spring que este dato puede ser grande y pesado. y el tipo de carga lenta o peresosa.
   private byte[] content;

}