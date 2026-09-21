package com.agenciaviagens.api.controller;

import com.agenciaviagens.api.dto.AvaliacaoRequestDTO;
import com.agenciaviagens.api.dto.DestinoRequestDTO;
import com.agenciaviagens.api.dto.DestinoResponseDTO;
import com.agenciaviagens.api.model.Destino;
import com.agenciaviagens.api.service.DestinoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints REST de destinos.
 *
 * A camada continua sem regra de negocio e sem acesso ao banco: apenas recebe
 * a requisicao, delega ao service e devolve a resposta HTTP.
 *
 * As regras de quem pode chamar cada endpoint ficam centralizadas no
 * SecurityConfig, mantendo o controller limpo.
 */
@RestController
@RequestMapping("/api/destinos")
public class DestinoController {

    private final DestinoService destinoService;

    public DestinoController(DestinoService destinoService) {
        this.destinoService = destinoService;
    }

    // POST /api/destinos -> cadastrar destino (somente ADMIN)
    @PostMapping
    public ResponseEntity<DestinoResponseDTO> cadastrar(@Valid @RequestBody DestinoRequestDTO dto) {
        Destino destinoCriado = destinoService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DestinoResponseDTO.fromEntity(destinoCriado));
    }

    // GET /api/destinos -> listar todos (USER ou ADMIN)
    @GetMapping
    public ResponseEntity<List<DestinoResponseDTO>> listarTodos() {
        List<DestinoResponseDTO> destinos = destinoService.listarTodos().stream()
                .map(DestinoResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(destinos);
    }

    // GET /api/destinos/pesquisa?nome=&localizacao= -> pesquisar (USER ou ADMIN)
    @GetMapping("/pesquisa")
    public ResponseEntity<List<DestinoResponseDTO>> pesquisar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String localizacao) {
        List<DestinoResponseDTO> resultado = destinoService.pesquisar(nome, localizacao).stream()
                .map(DestinoResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(resultado);
    }

    // GET /api/destinos/{id} -> detalhar (USER ou ADMIN)
    @GetMapping("/{id}")
    public ResponseEntity<DestinoResponseDTO> buscarPorId(@PathVariable Long id) {
        Destino destino = destinoService.buscarPorId(id);
        return ResponseEntity.ok(DestinoResponseDTO.fromEntity(destino));
    }

    // PUT /api/destinos/{id} -> atualizar (somente ADMIN)
    @PutMapping("/{id}")
    public ResponseEntity<DestinoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody DestinoRequestDTO dto) {
        Destino destinoAtualizado = destinoService.atualizar(id, dto);
        return ResponseEntity.ok(DestinoResponseDTO.fromEntity(destinoAtualizado));
    }

    /**
     * PATCH /api/destinos/{id}/avaliacoes -> avaliar (USER ou ADMIN).
     *
     * @AuthenticationPrincipal injeta automaticamente o usuario autenticado,
     * permitindo registrar QUEM fez a avaliacao sem confiar em um campo
     * enviado pelo cliente (que poderia ser falsificado).
     */
    @PatchMapping("/{id}/avaliacoes")
    public ResponseEntity<DestinoResponseDTO> registrarAvaliacao(
            @PathVariable Long id,
            @Valid @RequestBody AvaliacaoRequestDTO dto,
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        Destino destinoAvaliado = destinoService.registrarAvaliacao(
                id, dto, usuarioAutenticado.getUsername());
        return ResponseEntity.ok(DestinoResponseDTO.fromEntity(destinoAvaliado));
    }

    // DELETE /api/destinos/{id} -> excluir (somente ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        destinoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
