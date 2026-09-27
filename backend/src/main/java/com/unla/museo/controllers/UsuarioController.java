package com.unla.museo.controllers;

import com.unla.museo.controllers.util.LinksApi;
import com.unla.museo.dto.to.PersonaTO;
import com.unla.museo.services.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UsuarioController {

    private final UserServiceImpl userService;

    public UsuarioController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @Operation(summary = "Listar los usuarios con rol curador", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = LinksApi.UsuarioEndpoints.CURADORES, produces = {"application/json"})
    public ResponseEntity<List<PersonaTO>> listarCuradores() {
        return ResponseEntity.ok(userService.getCuradores());
    }
}
