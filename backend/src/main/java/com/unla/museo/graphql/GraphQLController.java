package com.unla.museo.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import java.util.List;

@Controller
public class GraphQLController {
	private final ReporteService reporteService;

	public GraphQLController(ReporteService reporteService) {
		this.reporteService = reporteService;
	}

	@QueryMapping
	public String saludo() {
		return "GraphQL funcionando correctamente";
	}

	@QueryMapping
	public List<Obra> obras(@Argument FiltroObras filtro, @Argument Integer pagina, @Argument Integer tamanio) {
		List<Obra> obras = ObraData.filtrarObras(filtro);
		int p = pagina != null ? Math.max(pagina, 0) : 0;
		int t = tamanio != null ? Math.min(Math.max(tamanio, 1), 100) : 20;
		int inicio = p * t;
		if (inicio >= obras.size())
			return List.of();
		return obras.subList(inicio, Math.min(inicio + t, obras.size()));
	}

	@QueryMapping
	@PreAuthorize("hasAnyRole('CURADOR','ADMINISTRADOR')")
	public ReporteAsistencia reporteAsistencia(@Argument FiltroReporte filtro) {
		return reporteService.generarReporte(filtro);
	}
}
