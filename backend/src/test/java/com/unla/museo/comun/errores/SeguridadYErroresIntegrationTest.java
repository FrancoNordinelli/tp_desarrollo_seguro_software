package com.unla.museo.comun.errores;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica el único mecanismo de autorización (@PreAuthorize +
 * @EnableMethodSecurity) y el formato uniforme de error de
 * GlobalExceptionHandler, de punta a punta contra el filtro de Spring
 * Security real (no mocks). Usa los usuarios que crea DataInitializer contra
 * H2 (perfil test).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeguridadYErroresIntegrationTest {

    private static final String CONTRASENIA = "Aa@12345678";
    // Protegida con @PreAuthorize("hasAnyRole('CURADOR','ADMINISTRADOR')").
    private static final String RUTA_DE_CURADOR = "/api/eventos/exportar";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sinTokenDevuelve401ConCuerpoJson() throws Exception {
        mockMvc.perform(get(RUTA_DE_CURADOR))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.codigo").value(401))
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void visitanteEnRutaDeCuradorDevuelve403NoQuinientos() throws Exception {
        String token = login("visitante@test.com");

        mockMvc.perform(get(RUTA_DE_CURADOR).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value(403));
    }

    @Test
    void rutaInexistenteDevuelve404() throws Exception {
        String token = login("admin@test.com");

        mockMvc.perform(get("/api/esto-no-existe").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value(404));
    }

    @Test
    void ningunaRespuestaDeErrorExponeExcepcionesOStackTraces() throws Exception {
        String tokenVisitante = login("visitante@test.com");
        String tokenAdmin = login("admin@test.com");

        MvcResult sinToken = mockMvc.perform(get(RUTA_DE_CURADOR)).andReturn();
        MvcResult sinPermiso = mockMvc.perform(get(RUTA_DE_CURADOR)
                        .header("Authorization", "Bearer " + tokenVisitante))
                .andReturn();
        MvcResult rutaInexistente = mockMvc.perform(get("/api/esto-no-existe")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andReturn();

        for (MvcResult resultado : List.of(sinToken, sinPermiso, rutaInexistente)) {
            String cuerpo = resultado.getResponse().getContentAsString();
            assertFalse(cuerpo.contains("Exception"), "el cuerpo no debe nombrar una clase de excepción: " + cuerpo);
            assertFalse(cuerpo.contains("at com."), "el cuerpo no debe incluir un stack trace: " + cuerpo);
        }
    }

    // Se arma el cuerpo a mano y se lee el token con JsonPath (sin ObjectMapper):
    // el proyecto conviven Jackson 2 (jjwt, springdoc) y Jackson 3 (el que
    // autoconfigura Spring Boot 4 por defecto), así que no hay un único bean
    // ObjectMapper para inyectar en el test.
    private String login(String email) throws Exception {
        String cuerpo = "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, CONTRASENIA);

        MvcResult resultado = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(resultado.getResponse().getContentAsString(), "$.accessToken");
    }
}
