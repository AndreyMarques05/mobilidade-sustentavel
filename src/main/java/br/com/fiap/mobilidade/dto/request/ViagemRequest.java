package br.com.fiap.mobilidade.dto.request;

import jakarta.validation.constraints.*;

public record ViagemRequest(
        @NotNull(message = "ID do tipo de transporte é obrigatório")
        Long idTipoTransporte,

        @NotBlank(message = "Origem é obrigatória")
        @Size(max = 200)
        String origem,

        @NotBlank(message = "Destino é obrigatório")
        @Size(max = 200)
        String destino,

        @NotNull(message = "Distância é obrigatória")
        @Positive(message = "Distância deve ser maior que zero")
        @DecimalMax(value = "10000.0", message = "Distância não pode exceder 10.000 km")
        Double distanciaKm,

        @Size(max = 500)
        String descricao
) {}
