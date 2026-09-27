package com.unla.museo.eventos;

/**
 * Proyección de "cuántos inscriptos tiene cada evento", para pedir un solo
 * conteo agrupado por lote de ids en vez de una consulta por evento.
 */
public interface ConteoPorEvento {
    Long getEventoId();
    Long getCantidad();
}
