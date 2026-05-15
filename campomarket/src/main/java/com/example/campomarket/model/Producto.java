package com.example.campomarket.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private Double precio;
    private Integer stock;
    private String categoria;   // Granos, Ganado, Verduras, Frutas, Aves
    private String unidad;      // quintal, cabeza, caja, unidad, libra
    private String descripcion;
    private String emoji;
    private String ubicacion;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
