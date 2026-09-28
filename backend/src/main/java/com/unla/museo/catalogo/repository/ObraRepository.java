package com.unla.museo.catalogo.repository;

import com.unla.museo.catalogo.entity.ObraEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ObraRepository extends JpaRepository<ObraEntity, Long> {

    // palabraClave busca en título, descripción o nombre del artista (OR);
    // el resto de los criterios son AND. Los parámetros ya llegan en
    // minúsculas y trimeados (o null si el criterio no aplica) desde el
    // servicio, así la condición "is null" desactiva cada filtro sin duplicar
    // la normalización acá. o.artista.nombre genera un join implícito (no un
    // fetch): no dispara una consulta aparte ni hidrata la asociación.
    @Query("""
            select o from ObraEntity o
            where (:palabraClave is null
                   or lower(o.titulo) like concat('%', :palabraClave, '%')
                   or lower(o.descripcion) like concat('%', :palabraClave, '%')
                   or lower(o.artista.nombre) like concat('%', :palabraClave, '%'))
              and (:epoca is null or lower(o.epoca) like concat('%', :epoca, '%'))
              and (:tecnica is null or lower(o.tecnica) like concat('%', :tecnica, '%'))
              and (:ubicacion is null or lower(o.ubicacion) like concat('%', :ubicacion, '%'))
              and (:enExhibicion is null or o.enExhibicion = :enExhibicion)
            """)
    Page<ObraEntity> buscar(@Param("palabraClave") String palabraClave,
                            @Param("epoca") String epoca,
                            @Param("tecnica") String tecnica,
                            @Param("ubicacion") String ubicacion,
                            @Param("enExhibicion") Boolean enExhibicion,
                            Pageable pageable);
}
