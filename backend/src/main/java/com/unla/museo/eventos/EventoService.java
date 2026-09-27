package com.unla.museo.eventos;

import com.unla.museo.eventos.dto.EventoDTO;
import com.unla.museo.eventos.dto.EventoRequest;
import com.unla.museo.eventos.dto.PaginaEventosDTO;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;

public interface EventoService {

    PaginaEventosDTO listar(LocalDate desde, LocalDate hasta, TipoEvento tipo, Long curadorId,
                             EstadoEvento estado, Integer pagina, Integer tamanio, Authentication authentication);

    EventoDTO obtenerDetalle(Long id, Authentication authentication);

    EventoDTO crear(EventoRequest request);

    EventoDTO editar(Long id, EventoRequest request);

    void borrar(Long id);

    void inscribirse(Long eventoId, Authentication authentication);

    void desinscribirse(Long eventoId, Authentication authentication);
}
