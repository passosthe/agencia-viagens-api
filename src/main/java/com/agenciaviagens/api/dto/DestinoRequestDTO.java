package com.agenciaviagens.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class DestinoRequestDTO {

    @NotBlank(message = "O nome do destino é obrigatório")
    private String nome;

    @NotBlank(message = "A localização é obrigatória")
    private String localizacao;

    private String descricao;

    @NotNull(message = "O preço do pacote é obrigatório")
    @PositiveOrZero(message = "O preço não pode ser negativo")
    private Double precoPacote;

    private Boolean hoteisDisponiveis;

    private List<String> atividadesTuristicas;
}
