package com.unla.museo.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    // join fetch del usuario: se necesita su nombre y apellido para cada
    // comentario, y sin el fetch cada uno dispararía su propia consulta al
    // pedir el autor (N+1). Una sola consulta para todo el lote de obras.
    @Query("""
            select c from Comentario c
            join fetch c.usuario
            where c.obra.id in :obraIds
            order by c.fecha asc, c.id asc
            """)
    List<Comentario> buscarPorObraIds(@Param("obraIds") List<Long> obraIds);
}
