package com.agenciaviagens.api.dto;

import com.agenciaviagens.api.model.Destino;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Formato de saida de um destino.
 *
 * Alem de controlar o que e exposto, este DTO tem um papel extra agora que
 * usamos JPA: ele quebra o ciclo Destino -> Avaliacoes -> Destino, que
 * causaria recursao infinita na serializacao para JSON.
 */
@Getter
@AllArgsConstructor
public class DestinoResponseDTO {

    private Long id;
    private String nome;
    private String localizacao;
    private String descricao;
    private BigDecimal precoPacote;
    private Boolean hoteisDisponiveis;
    private List<String> atividadesTuristicas;
    private BigDecimal mediaAvaliacao;
    private Integer quantidadeAvaliacoes;

    public static DestinoResponseDTO fromEntity(Destino destino) {
        return new DestinoResponseDTO(
                destino.getId(),
                destino.getNome(),
                destino.getLocalizacao(),
                destino.getDescricao(),
                destino.getPrecoPacote(),
                destino.getHoteisDisponiveis(),
                destino.getAtividadesTuristicas(),
                destino.getMediaAvaliacao(),
                destino.getQuantidadeAvaliacoes()
        );
    }
}
