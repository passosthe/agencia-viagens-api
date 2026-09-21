package com.agenciaviagens.api.controller;

import com.agenciaviagens.api.security.UsuarioDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Endpoint auxiliar de autenticacao.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // GET /api/auth/me -> dados do usuario autenticado
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> usuarioAutenticado(
            @AuthenticationPrincipal UsuarioDetails usuario) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("login", usuario.getUsername());
        corpo.put("perfis", usuario.getPerfis());
        corpo.put("autenticado", true);
        return ResponseEntity.ok(corpo);
    }
}
