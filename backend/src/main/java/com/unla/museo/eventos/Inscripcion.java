package com.unla.museo.eventos;

import com.unla.museo.seguridad.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "INSCRIPCION",
        uniqueConstraints = @UniqueConstraint(name = "uk_inscripcion_evento_usuario", columnNames = {"EVENTO_ID", "USUARIO_ID"}),
        indexes = @Index(name = "idx_inscripcion_usuario_id", columnList = "USUARIO_ID"))
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "EVENTO_ID", nullable = false)
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "USUARIO_ID", nullable = false)
    private UserEntity usuario;

    @Column(name = "FECHA_INSCRIPCION", nullable = false)
    private LocalDateTime fechaInscripcion;
}
