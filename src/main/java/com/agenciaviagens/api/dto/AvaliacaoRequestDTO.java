package com.agenciaviagens.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO usado quando um cliente envia uma nova avaliação (nota) para um destino.
 */
@Getter
@Setter
public class AvaliacaoRequestDTO {

    @NotNull(message = "A nota é obrigatória")
    @DecimalMin(value = "0.0", message = "A nota mínima é 0")
    @DecimalMax(value = "5.0", message = "A nota máxima é 5")
    private Double nota;
}
