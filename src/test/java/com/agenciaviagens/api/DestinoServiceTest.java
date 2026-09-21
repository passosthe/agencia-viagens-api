package com.agenciaviagens.api;

import com.agenciaviagens.api.dto.AvaliacaoRequestDTO;
import com.agenciaviagens.api.exception.DestinoNaoEncontradoException;
import com.agenciaviagens.api.model.Avaliacao;
import com.agenciaviagens.api.model.Destino;
import com.agenciaviagens.api.repository.AvaliacaoRepository;
import com.agenciaviagens.api.repository.DestinoRepository;
import com.agenciaviagens.api.service.DestinoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

/**
 * Testes unitarios da regra de negocio mais sensivel do sistema:
 * o recalculo da media de avaliacoes.
 *
 * Como o repository agora e uma interface do Spring Data (sem implementacao
 * escrita por nos), usamos o Mockito para criar um substituto em memoria.
 * Isso permite testar a REGRA DE NEGOCIO isoladamente, sem precisar de um
 * banco PostgreSQL rodando — testes rapidos e independentes de ambiente.
 */
class DestinoServiceTest {

    private DestinoRepository destinoRepository;
    private AvaliacaoRepository avaliacaoRepository;
    private DestinoService destinoService;
    private Destino destinoEmTeste;

    @BeforeEach
    void setUp() {
        destinoRepository = Mockito.mock(DestinoRepository.class);
        avaliacaoRepository = Mockito.mock(AvaliacaoRepository.class);
        destinoService = new DestinoService(destinoRepository, avaliacaoRepository);

        destinoEmTeste = new Destino();
        destinoEmTeste.setId(1L);
        destinoEmTeste.setNome("Chapada Diamantina");
        destinoEmTeste.setLocalizacao("Bahia, Brasil");
        destinoEmTeste.setPrecoPacote(new BigDecimal("1200.00"));

        // Quando o service buscar o id 1, devolve o destino de teste
        Mockito.when(destinoRepository.findById(1L))
                .thenReturn(Optional.of(destinoEmTeste));

        // O save devolve o proprio objeto recebido, simulando o banco
        Mockito.when(destinoRepository.save(any(Destino.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
        Mockito.when(avaliacaoRepository.save(any(Avaliacao.class)))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
    }

    @Test
    @DisplayName("Deve calcular a media corretamente apos duas avaliacoes")
    void deveCalcularMediaAposDuasAvaliacoes() {
        registrarNota("4.0");
        Destino resultado = registrarNota("5.0");

        assertEquals(0, new BigDecimal("4.50").compareTo(resultado.getMediaAvaliacao()));
        assertEquals(2, resultado.getQuantidadeAvaliacoes());
    }

    @Test
    @DisplayName("Deve calcular a media corretamente apos tres avaliacoes")
    void deveCalcularMediaAposTresAvaliacoes() {
        registrarNota("5.0");
        registrarNota("4.0");
        Destino resultado = registrarNota("3.0");

        assertEquals(0, new BigDecimal("4.00").compareTo(resultado.getMediaAvaliacao()));
        assertEquals(3, resultado.getQuantidadeAvaliacoes());
    }

    @Test
    @DisplayName("Destino recem-cadastrado deve comecar com media zero")
    void destinoNovoDeveComecarComMediaZero() {
        assertEquals(0, BigDecimal.ZERO.compareTo(destinoEmTeste.getMediaAvaliacao()));
        assertEquals(0, destinoEmTeste.getQuantidadeAvaliacoes());
    }

    @Test
    @DisplayName("Deve lancar excecao ao avaliar um destino inexistente")
    void deveLancarExcecaoParaDestinoInexistente() {
        Mockito.when(destinoRepository.findById(anyLong())).thenReturn(Optional.empty());

        AvaliacaoRequestDTO dto = new AvaliacaoRequestDTO();
        dto.setNota(new BigDecimal("5.0"));

        assertThrows(DestinoNaoEncontradoException.class,
                () -> destinoService.registrarAvaliacao(99L, dto, "usuario"));
    }

    @Test
    @DisplayName("Deve registrar o login de quem fez a avaliacao")
    void deveRegistrarAutorDaAvaliacao() {
        Destino resultado = registrarNota("4.0");

        assertEquals(1, resultado.getAvaliacoes().size());
        assertEquals("usuario.teste", resultado.getAvaliacoes().get(0).getUsuarioLogin());
    }

    private Destino registrarNota(String nota) {
        AvaliacaoRequestDTO dto = new AvaliacaoRequestDTO();
        dto.setNota(new BigDecimal(nota));
        return destinoService.registrarAvaliacao(1L, dto, "usuario.teste");
    }
}
