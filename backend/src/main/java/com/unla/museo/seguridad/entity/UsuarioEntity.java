package com.unla.museo.seguridad.entity;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
@Table(name = "USERS")
public class UsuarioEntity extends AuditoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "EMAIL", unique = true, nullable = false)
    private String email;

    @Column(name = "NOMBRE")
    private String nombre;

    @Column(name = "APELLIDO")
    private String apellido;

    @Column(name = "TELEFONO")
    private String telefono;

    @Column(name = "ACTIVO")
    private Boolean activo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_ROL")
    private RolEntity rol;

    @Column(name = "PASSWORD")
    private String password;





}
