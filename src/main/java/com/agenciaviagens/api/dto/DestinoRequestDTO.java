package com.agenciaviagens.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Dados que o cliente envia para cadastrar ou atualizar um destino.
 *
 * O preco passou de Double para BigDecimal: valores monetarios nunca devem
 * usar ponto flutuante, porque 0.1 + 0.2 em double nao da exatamente 0.3.
 */
@Getter
@Setter
public class DestinoRequestDTO {

    @NotBlank(message = "O nome do destino e obrigatorio")
    @Size(max = 120, message = "O nome deve ter no maximo 120 caracteres")
    private String nome;

    @NotBlank(message = "A localizacao e obrigatoria")
    @Size(max = 120, message = "A localizacao deve ter no maximo 120 caracteres")
    private String localizacao;

    @Size(max = 1000, message = "A descricao deve ter no maximo 1000 caracteres")
    private String descricao;

    @NotNull(message = "O preco do pacote e obrigatorio")
    @PositiveOrZero(message = "O preco nao pode ser negativo")
    private BigDecimal precoPacote;

    private Boolean hoteisDisponiveis;

    private List<String> atividadesTuristicas;
}
