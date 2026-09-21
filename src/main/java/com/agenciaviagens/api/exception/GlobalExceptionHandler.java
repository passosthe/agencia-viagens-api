package com.agenciaviagens.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tratamento centralizado de erros da API.
 *
 * Alem dos erros da versao 1 (404 e validacao), agora tratamos tambem o
 * acesso negado por perfil, devolvendo 403 com uma mensagem clara em vez
 * de uma pagina de erro generica.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DestinoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> tratarDestinoNaoEncontrado(
            DestinoNaoEncontradoException ex) {
        return montarResposta(HttpStatus.NOT_FOUND, "Destino nao encontrado", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> tratarErrosDeValidacao(
            MethodArgumentNotValidException ex) {
        Map<String, String> camposInvalidos = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(erro ->
                camposInvalidos.put(erro.getField(), erro.getDefaultMessage())
        );

        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("timestamp", LocalDateTime.now());
        corpo.put("status", HttpStatus.BAD_REQUEST.value());
        corpo.put("erro", "Dados invalidos");
        corpo.put("campos", camposInvalidos);
        return new ResponseEntity<>(corpo, HttpStatus.BAD_REQUEST);
    }

    /**
     * Usuario autenticado, porem sem o perfil necessario para a operacao.
     * Exemplo: um USER tentando cadastrar ou excluir um destino.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> tratarAcessoNegado(AccessDeniedException ex) {
        return montarResposta(
                HttpStatus.FORBIDDEN,
                "Acesso negado",
                "Seu perfil nao possui permissao para executar esta operacao");
    }

    private ResponseEntity<Map<String, Object>> montarResposta(
            HttpStatus status, String erro, String mensagem) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("timestamp", LocalDateTime.now());
        corpo.put("status", status.value());
        corpo.put("erro", erro);
        corpo.put("mensagem", mensagem);
        return new ResponseEntity<>(corpo, status);
    }
}
