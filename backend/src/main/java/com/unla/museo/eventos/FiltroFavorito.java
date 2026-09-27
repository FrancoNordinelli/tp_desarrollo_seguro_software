package com.unla.museo.eventos;

import com.unla.museo.entities.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "FILTRO_FAVORITO", indexes = @Index(name = "idx_filtro_favorito_usuario_id", columnList = "USUARIO_ID"))
public class FiltroFavorito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USUARIO_ID", nullable = false)
    private UserEntity usuario;

    @Column(name = "NOMBRE", nullable = false, length = 150)
    private String nombre;

    @Column(name = "DESCRIPCION", length = 500)
    private String descripcion;

    @Column(name = "DESDE")
    private LocalDate desde;

    @Column(name = "HASTA")
    private LocalDate hasta;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO", length = 20)
    private TipoEvento tipo;

    @Column(name = "CURADOR_ID")
    private Long curadorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", length = 20)
    private EstadoEvento estado;
}
