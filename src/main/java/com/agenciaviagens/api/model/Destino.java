package com.agenciaviagens.api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade JPA que representa um destino de viagem.
 *
 * Na versao 1 esta classe era apenas um objeto guardado em um Map na memoria.
 * Agora, com as anotacoes JPA, ela e mapeada para uma TABELA real no PostgreSQL:
 * cada atributo vira uma coluna e cada objeto salvo vira uma linha.
 */
@Entity
@Table(name = "destinos")
@Getter
@Setter
@NoArgsConstructor
public class Destino {

    /**
     * @Id marca a chave primaria da tabela.
     * @GeneratedValue com IDENTITY delega ao PostgreSQL a geracao automatica
     * do id (coluna do tipo BIGSERIAL), substituindo o contador manual da v1.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 120)
    private String localizacao;

    @Column(length = 1000)
    private String descricao;

    @Column(name = "preco_pacote", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoPacote;

    @Column(name = "hoteis_disponiveis", nullable = false)
    private Boolean hoteisDisponiveis = false;

    /**
     * @ElementCollection cria uma tabela auxiliar (destino_atividades) para
     * guardar a lista de textos, ja que uma coluna comum nao armazena listas.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "destino_atividades",
            joinColumns = @JoinColumn(name = "destino_id")
    )
    @Column(name = "atividade")
    private List<String> atividadesTuristicas = new ArrayList<>();

    /**
     * Relacionamento 1:N — um destino possui muitas avaliacoes.
     * cascade = ALL: ao salvar/excluir o destino, as avaliacoes acompanham.
     * orphanRemoval: avaliacoes removidas da lista somem do banco.
     */
    @OneToMany(mappedBy = "destino", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Avaliacao> avaliacoes = new ArrayList<>();

    /**
     * Media mantida como coluna (campo derivado) para que a listagem nao
     * precise recalcular a media de todos os destinos a cada consulta.
     */
    @Column(name = "media_avaliacao", nullable = false, precision = 3, scale = 2)
    private BigDecimal mediaAvaliacao = BigDecimal.ZERO;

    @Column(name = "quantidade_avaliacoes", nullable = false)
    private Integer quantidadeAvaliacoes = 0;

    /**
     * Regra de negocio do proprio dominio: adiciona a avaliacao a lista
     * (mantendo os dois lados do relacionamento sincronizados) e recalcula
     * a media a partir das avaliacoes realmente persistidas.
     */
    public void adicionarAvaliacao(Avaliacao avaliacao) {
        avaliacao.setDestino(this);
        this.avaliacoes.add(avaliacao);
        recalcularMedia();
    }

    private void recalcularMedia() {
        this.quantidadeAvaliacoes = this.avaliacoes.size();

        if (this.quantidadeAvaliacoes == 0) {
            this.mediaAvaliacao = BigDecimal.ZERO;
            return;
        }

        BigDecimal soma = this.avaliacoes.stream()
                .map(Avaliacao::getNota)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.mediaAvaliacao = soma.divide(
                BigDecimal.valueOf(this.quantidadeAvaliacoes), 2, RoundingMode.HALF_UP);
    }
}
