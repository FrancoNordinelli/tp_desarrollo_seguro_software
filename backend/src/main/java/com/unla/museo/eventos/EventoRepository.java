package com.unla.museo.eventos;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    // La inscripción, y editar/borrar el evento, empiezan siempre leyendo acá:
    // el SELECT ... FOR UPDATE bloquea la fila hasta que la transacción termina,
    // así dos inscripciones concurrentes al último lugar no pueden pasar juntas
    // la validación de cupo.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Evento e where e.id = :id")
    Optional<Evento> buscarPorIdConBloqueo(@Param("id") Long id);

    // join fetch del curador para no disparar una consulta por fila al armar
    // "curadorResponsable" de cada item de la página (evita el N+1).
    // "estado" no se compara como enum acá (Hibernate 7 no logra inferirle el
    // tipo al parámetro dentro del OR): se traduce antes, en el servicio, a
    // dos booleanos que activan cada comparación contra :ahora.
    @Query("""
            select e from Evento e join fetch e.curador
            where (:desde is null or e.fechaHora >= :desde)
              and (:hastaExclusivo is null or e.fechaHora < :hastaExclusivo)
              and (:tipo is null or e.tipo = :tipo)
              and (:curadorId is null or e.curador.id = :curadorId)
              and (:filtrarPasados = false or e.fechaHora <= :ahora)
              and (:filtrarFuturos = false or e.fechaHora > :ahora)
            """)
    Page<Evento> buscar(@Param("desde") LocalDateTime desde,
                         @Param("hastaExclusivo") LocalDateTime hastaExclusivo,
                         @Param("tipo") TipoEvento tipo,
                         @Param("curadorId") Long curadorId,
                         @Param("filtrarPasados") boolean filtrarPasados,
                         @Param("filtrarFuturos") boolean filtrarFuturos,
                         @Param("ahora") LocalDateTime ahora,
                         Pageable pageable);

    @Query("select e from Evento e join fetch e.curador where e.id = :id")
    Optional<Evento> buscarPorIdConCurador(@Param("id") Long id);
}
