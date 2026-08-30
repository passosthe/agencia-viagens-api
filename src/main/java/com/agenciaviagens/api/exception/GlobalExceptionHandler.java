package com.agenciaviagens.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DestinoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> tratarDestinoNaoEncontrado(DestinoNaoEncontradoException ex) {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("timestamp", LocalDateTime.now());
        corpo.put("status", HttpStatus.NOT_FOUND.value());
        corpo.put("erro", "Destino não encontrado");
        corpo.put("mensagem", ex.getMessage());
        return new ResponseEntity<>(corpo, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> tratarErrosDeValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> camposInvalidos = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(erro ->
                camposInvalidos.put(erro.getField(), erro.getDefaultMessage())
        );

        Map<String, Object> corpo = new HashMap<>();
        corpo.put("timestamp", LocalDateTime.now());
        corpo.put("status", HttpStatus.BAD_REQUEST.value());
        corpo.put("erro", "Dados inválidos");
        corpo.put("campos", camposInvalidos);
        return new ResponseEntity<>(corpo, HttpStatus.BAD_REQUEST);
    }
}
