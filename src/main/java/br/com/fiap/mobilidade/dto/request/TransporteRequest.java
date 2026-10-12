package br.com.fiap.mobilidade.dto.request;

import jakarta.validation.constraints.*;

public record TransporteRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100)
        String nome,

        @Size(max = 500)
        String descricao,

        @NotNull(message = "Emissão de CO2 por km é obrigatória")
        @PositiveOrZero(message = "Emissão de CO2 não pode ser negativa")
        Double emissaoCo2PorKm,

        @NotNull(message = "Pontos por km é obrigatório")
        @PositiveOrZero(message = "Pontos por km não pode ser negativo")
        Integer pontosPorKm
) {}
