package com.agenciaviagens.api.dto;

import com.agenciaviagens.api.model.Destino;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;


@Getter
@AllArgsConstructor
public class DestinoResponseDTO {

    private Long id;
    private String nome;
    private String localizacao;
    private String descricao;
    private Double precoPacote;
    private Boolean hoteisDisponiveis;
    private List<String> atividadesTuristicas;
    private Double mediaAvaliacao;
    private Integer quantidadeAvaliacoes;

    /**
     * converte a Entity (dado interno) no DTO (dado exposto).
     */
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
