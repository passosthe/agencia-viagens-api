package com.agenciaviagens.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Destino {

    private Long id;
    private String nome;
    private String localizacao;
    private String descricao;
    private Double precoPacote;
    private Boolean hoteisDisponiveis;
    private List<String> atividadesTuristicas = new ArrayList<>();

    // Controle da avaliação média (nota de 0 a 5)
    private Double mediaAvaliacao = 0.0;
    private Integer quantidadeAvaliacoes = 0;


    public void registrarNovaAvaliacao(double novaNota) {
        double somaAtual = this.mediaAvaliacao * this.quantidadeAvaliacoes;
        this.quantidadeAvaliacoes++;
        this.mediaAvaliacao = (somaAtual + novaNota) / this.quantidadeAvaliacoes;
    }
}
