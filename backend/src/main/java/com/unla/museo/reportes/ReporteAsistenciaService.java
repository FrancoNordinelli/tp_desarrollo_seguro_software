package com.unla.museo.reportes;

import com.unla.museo.eventos.EstadoEvento;
import com.unla.museo.eventos.TipoEvento;

public interface ReporteAsistenciaService {

    /**
     * @param desde  "AAAA-MM-DD" o null
     * @param hasta  "AAAA-MM-DD" o null
     */
    ReporteAsistenciaDTO generar(String desde, String hasta, TipoEvento tipo, EstadoEvento estado,
                                  AgruparPor agruparPor);
}
