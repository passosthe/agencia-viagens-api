package com.agenciaviagens.api;

import com.agenciaviagens.api.dto.AvaliacaoRequestDTO;
import com.agenciaviagens.api.dto.DestinoRequestDTO;
import com.agenciaviagens.api.model.Destino;
import com.agenciaviagens.api.repository.DestinoRepository;
import com.agenciaviagens.api.service.DestinoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class DestinoServiceTest {

    private DestinoService destinoService;

    @BeforeEach
    void setUp() {
        this.destinoService = new DestinoService(new DestinoRepository());
    }

    @Test
    void deveCalcularMediaCorretamenteAposDuasAvaliacoes() {
        DestinoRequestDTO novoDestino = new DestinoRequestDTO();
        novoDestino.setNome("Chapada Diamantina");
        novoDestino.setLocalizacao("Bahia, Brasil");
        novoDestino.setPrecoPacote(1200.0);

        Destino destinoCriado = destinoService.cadastrar(novoDestino);

        AvaliacaoRequestDTO primeiraNota = new AvaliacaoRequestDTO();
        primeiraNota.setNota(4.0);
        destinoService.registrarAvaliacao(destinoCriado.getId(), primeiraNota);

        AvaliacaoRequestDTO segundaNota = new AvaliacaoRequestDTO();
        segundaNota.setNota(5.0);
        Destino destinoAvaliado = destinoService.registrarAvaliacao(destinoCriado.getId(), segundaNota);

        assertEquals(4.5, destinoAvaliado.getMediaAvaliacao());
        assertEquals(2, destinoAvaliado.getQuantidadeAvaliacoes());
    }
}
