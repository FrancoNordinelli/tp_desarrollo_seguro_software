package com.unla.museo.eventos;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    boolean existsByEventoIdAndUsuarioId(Long eventoId, Long usuarioId);

    long countByEventoId(Long eventoId);

    void deleteByEventoIdAndUsuarioId(Long eventoId, Long usuarioId);

    @EntityGraph(attributePaths = "usuario")
    List<Inscripcion> findByEventoIdOrderByFechaInscripcionAsc(Long eventoId);

    @Query("""
            select i.evento.id as eventoId, count(i) as cantidad
            from Inscripcion i
            where i.evento.id in :eventoIds
            group by i.evento.id
            """)
    List<ConteoPorEvento> contarPorEventos(@Param("eventoIds") List<Long> eventoIds);

    @Query("""
            select i.evento.id
            from Inscripcion i
            where i.evento.id in :eventoIds and i.usuario.id = :usuarioId
            """)
    List<Long> buscarEventoIdsInscriptoDeUsuario(@Param("eventoIds") List<Long> eventoIds, @Param("usuarioId") Long usuarioId);
}
