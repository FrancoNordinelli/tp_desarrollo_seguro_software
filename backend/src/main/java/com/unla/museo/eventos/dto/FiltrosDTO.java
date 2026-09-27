package com.unla.museo.eventos.dto;

import com.unla.museo.eventos.EstadoEvento;
import com.unla.museo.eventos.TipoEvento;

import java.time.LocalDate;

/** Mismos campos que los filtros del listado de eventos. Todos opcionales. */
public record FiltrosDTO(LocalDate desde, LocalDate hasta, TipoEvento tipo, Long curadorId, EstadoEvento estado) {
}
