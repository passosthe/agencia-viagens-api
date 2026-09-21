package com.agenciaviagens.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Dados que o cliente envia ao avaliar um destino.
 * O autor da avaliacao nao vem no corpo: ele e obtido do usuario autenticado,
 * evitando que alguem registre uma nota em nome de outra pessoa.
 */
@Getter
@Setter
public class AvaliacaoRequestDTO {

    @NotNull(message = "A nota e obrigatoria")
    @DecimalMin(value = "0.0", message = "A nota minima e 0")
    @DecimalMax(value = "5.0", message = "A nota maxima e 5")
    private BigDecimal nota;

    @Size(max = 500, message = "O comentario deve ter no maximo 500 caracteres")
    private String comentario;
}
