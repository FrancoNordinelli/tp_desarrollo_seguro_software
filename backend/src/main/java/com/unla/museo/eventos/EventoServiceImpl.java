package com.unla.museo.eventos;

import com.unla.museo.comun.errores.ConflictoException;
import com.unla.museo.comun.errores.RecursoInexistenteException;
import com.unla.museo.comun.errores.SolicitudInvalidaException;
import com.unla.museo.entities.UserEntity;
import com.unla.museo.eventos.dto.EventoDTO;
import com.unla.museo.eventos.dto.EventoRequest;
import com.unla.museo.eventos.dto.FilaReporteEventoDTO;
import com.unla.museo.eventos.dto.PaginaEventosDTO;
import com.unla.museo.eventos.dto.PersonaDTO;
import com.unla.museo.services.UsuarioActualService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class EventoServiceImpl implements EventoService {

    private static final int TAMANIO_PAGINA_DEFECTO = 10;
    private static final int TAMANIO_PAGINA_MAXIMO = 100;

    private final EventoRepository eventoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final UsuarioActualService usuarioActualService;
    private final Clock clock;

    public EventoServiceImpl(EventoRepository eventoRepository,
                              InscripcionRepository inscripcionRepository,
                              UsuarioActualService usuarioActualService,
                              Clock clock) {
        this.eventoRepository = eventoRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.usuarioActualService = usuarioActualService;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaEventosDTO listar(LocalDate desde, LocalDate hasta, TipoEvento tipo, Long curadorId,
                                    EstadoEvento estado, Integer pagina, Integer tamanio, Authentication authentication) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new SolicitudInvalidaException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
        int paginaEfectiva = pagina != null ? pagina : 0;
        int tamanioEfectivo = tamanio != null ? tamanio : TAMANIO_PAGINA_DEFECTO;
        if (paginaEfectiva < 0) {
            throw new SolicitudInvalidaException("La página no puede ser negativa");
        }
        if (tamanioEfectivo < 1 || tamanioEfectivo > TAMANIO_PAGINA_MAXIMO) {
            throw new SolicitudInvalidaException("El tamaño de página debe estar entre 1 y " + TAMANIO_PAGINA_MAXIMO);
        }
        EstadoEvento estadoEfectivo = estado != null ? estado : EstadoEvento.TODOS;
        LocalDateTime desdeDateTime = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime hastaExclusivo = hasta != null ? hasta.plusDays(1).atStartOfDay() : null;
        LocalDateTime ahora = LocalDateTime.now(clock);

        boolean filtrarPasados = estadoEfectivo == EstadoEvento.PASADOS;
        boolean filtrarFuturos = estadoEfectivo == EstadoEvento.FUTUROS;
        Pageable pageable = PageRequest.of(paginaEfectiva, tamanioEfectivo,
                Sort.by(Sort.Order.asc("fechaHora"), Sort.Order.asc("id")));
        Page<Evento> paginaDeEventos = eventoRepository.buscar(
                desdeDateTime, hastaExclusivo, tipo, curadorId, filtrarPasados, filtrarFuturos, ahora, pageable);

        List<Evento> eventos = paginaDeEventos.getContent();
        List<Long> ids = eventos.stream().map(Evento::getId).toList();
        Map<Long, Long> conteos = contarInscriptosPorEvento(ids);
        Set<Long> inscriptoDelUsuario = idsInscriptoDelUsuarioActual(ids, authentication);

        List<EventoDTO> items = eventos.stream()
                .map(evento -> new EventoDTO(
                        evento.getId(), evento.getTitulo(), evento.getDescripcion(), evento.getTipo(),
                        evento.getFechaHora(), evento.getDuracionMinutos(), evento.getCupoMaximo(),
                        PersonaDTO.desde(evento.getCurador()),
                        conteos.getOrDefault(evento.getId(), 0L),
                        inscriptoDelUsuario.contains(evento.getId()),
                        null))
                .toList();

        return new PaginaEventosDTO(items, paginaEfectiva, tamanioEfectivo, paginaDeEventos.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FilaReporteEventoDTO> obtenerFilasParaReporte(LocalDate desde, LocalDate hasta, TipoEvento tipo,
                                                               EstadoEvento estado) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new SolicitudInvalidaException("La fecha 'desde' no puede ser posterior a 'hasta'");
        }
        EstadoEvento estadoEfectivo = estado != null ? estado : EstadoEvento.TODOS;
        LocalDateTime desdeDateTime = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime hastaExclusivo = hasta != null ? hasta.plusDays(1).atStartOfDay() : null;
        LocalDateTime ahora = LocalDateTime.now(clock);
        boolean filtrarPasados = estadoEfectivo == EstadoEvento.PASADOS;
        boolean filtrarFuturos = estadoEfectivo == EstadoEvento.FUTUROS;

        return eventoRepository.buscarParaReporte(desdeDateTime, hastaExclusivo, tipo, filtrarPasados, filtrarFuturos, ahora)
                .stream()
                .map(fila -> new FilaReporteEventoDTO(
                        fila.getId(), fila.getTitulo(), fila.getTipo(), fila.getFechaHora(),
                        new PersonaDTO(fila.getCuradorId(), fila.getCuradorNombre() + " " + fila.getCuradorApellido()),
                        fila.getCupoMaximo(), fila.getCantidadInscriptos()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventoDTO obtenerDetalle(Long id, Authentication authentication) {
        Evento evento = eventoRepository.buscarPorIdConCurador(id)
                .orElseThrow(() -> new RecursoInexistenteException("El evento no existe"));
        return mapearConDetalle(evento, authentication);
    }

    @Override
    public EventoDTO crear(EventoRequest request) {
        UserEntity curador = usuarioActualService.obtenerCuradorValido(request.getCuradorId());
        Evento evento = new Evento();
        aplicar(evento, request, curador);
        Evento guardado = eventoRepository.save(evento);
        return new EventoDTO(guardado.getId(), guardado.getTitulo(), guardado.getDescripcion(), guardado.getTipo(),
                guardado.getFechaHora(), guardado.getDuracionMinutos(), guardado.getCupoMaximo(),
                PersonaDTO.desde(curador), 0L, false, List.of());
    }

    @Override
    public EventoDTO editar(Long id, EventoRequest request) {
        Evento evento = eventoRepository.buscarPorIdConBloqueo(id)
                .orElseThrow(() -> new RecursoInexistenteException("El evento no existe"));
        UserEntity curador = usuarioActualService.obtenerCuradorValido(request.getCuradorId());
        long inscriptos = inscripcionRepository.countByEventoId(id);
        if (request.getCupoMaximo() < inscriptos) {
            throw new ConflictoException("El cupo no puede ser menor a la cantidad de inscriptos actuales");
        }
        aplicar(evento, request, curador);
        Evento guardado = eventoRepository.save(evento);
        List<PersonaDTO> inscriptosDTO = inscripcionRepository.findByEventoIdOrderByFechaInscripcionAsc(id).stream()
                .map(inscripcion -> PersonaDTO.desde(inscripcion.getUsuario()))
                .toList();
        return new EventoDTO(guardado.getId(), guardado.getTitulo(), guardado.getDescripcion(), guardado.getTipo(),
                guardado.getFechaHora(), guardado.getDuracionMinutos(), guardado.getCupoMaximo(),
                PersonaDTO.desde(curador), inscriptos, false, inscriptosDTO);
    }

    @Override
    public void borrar(Long id) {
        Evento evento = eventoRepository.buscarPorIdConBloqueo(id)
                .orElseThrow(() -> new RecursoInexistenteException("El evento no existe"));
        eventoRepository.delete(evento);
    }

    @Override
    public void inscribirse(Long eventoId, Authentication authentication) {
        // Primera operación: leer con bloqueo pesimista antes de cualquier validación.
        Evento evento = eventoRepository.buscarPorIdConBloqueo(eventoId)
                .orElseThrow(() -> new RecursoInexistenteException("El evento no existe"));
        UserEntity usuario = usuarioActualService.obtenerPorEmail(authentication.getName());
        LocalDateTime ahora = LocalDateTime.now(clock);

        if (!evento.getFechaHora().isAfter(ahora)) {
            throw new ConflictoException("El evento ya comenzó");
        }
        if (inscripcionRepository.existsByEventoIdAndUsuarioId(eventoId, usuario.getId())) {
            throw new ConflictoException("Ya está inscripto en este evento");
        }
        long inscriptos = inscripcionRepository.countByEventoId(eventoId);
        if (inscriptos >= evento.getCupoMaximo()) {
            throw new ConflictoException("El evento no tiene cupo disponible");
        }

        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setEvento(evento);
        inscripcion.setUsuario(usuario);
        inscripcion.setFechaInscripcion(ahora);
        inscripcionRepository.save(inscripcion);
    }

    @Override
    public void desinscribirse(Long eventoId, Authentication authentication) {
        if (!eventoRepository.existsById(eventoId)) {
            throw new RecursoInexistenteException("El evento no existe");
        }
        UserEntity usuario = usuarioActualService.obtenerPorEmail(authentication.getName());
        inscripcionRepository.deleteByEventoIdAndUsuarioId(eventoId, usuario.getId());
    }

    private void aplicar(Evento evento, EventoRequest request, UserEntity curador) {
        evento.setTitulo(request.getTitulo());
        evento.setDescripcion(request.getDescripcion());
        evento.setTipo(request.getTipo());
        evento.setFechaHora(request.getFechaHora());
        evento.setDuracionMinutos(request.getDuracionMinutos());
        evento.setCupoMaximo(request.getCupoMaximo());
        evento.setCurador(curador);
    }

    private EventoDTO mapearConDetalle(Evento evento, Authentication authentication) {
        UserEntity usuarioActual = usuarioActualService.obtenerPorEmail(authentication.getName());
        boolean esGestor = usuarioActualService.esGestor(authentication);
        long cantidadInscriptos = inscripcionRepository.countByEventoId(evento.getId());
        boolean inscripto = inscripcionRepository.existsByEventoIdAndUsuarioId(evento.getId(), usuarioActual.getId());

        List<PersonaDTO> inscriptos = null;
        if (esGestor) {
            inscriptos = inscripcionRepository.findByEventoIdOrderByFechaInscripcionAsc(evento.getId()).stream()
                    .map(inscripcion -> PersonaDTO.desde(inscripcion.getUsuario()))
                    .toList();
        }

        return new EventoDTO(evento.getId(), evento.getTitulo(), evento.getDescripcion(), evento.getTipo(),
                evento.getFechaHora(), evento.getDuracionMinutos(), evento.getCupoMaximo(),
                PersonaDTO.desde(evento.getCurador()), cantidadInscriptos, inscripto, inscriptos);
    }

    private Map<Long, Long> contarInscriptosPorEvento(List<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, Long> conteos = new HashMap<>();
        for (ConteoPorEvento conteo : inscripcionRepository.contarPorEventos(ids)) {
            conteos.put(conteo.getEventoId(), conteo.getCantidad());
        }
        return conteos;
    }

    private Set<Long> idsInscriptoDelUsuarioActual(List<Long> ids, Authentication authentication) {
        if (ids.isEmpty()) {
            return Set.of();
        }
        UserEntity usuarioActual = usuarioActualService.obtenerPorEmail(authentication.getName());
        return new HashSet<>(inscripcionRepository.buscarEventoIdsInscriptoDeUsuario(ids, usuarioActual.getId()));
    }
}
