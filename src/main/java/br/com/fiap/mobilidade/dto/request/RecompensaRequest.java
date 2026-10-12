package br.com.fiap.mobilidade.dto.request;

import jakarta.validation.constraints.*;

public record RecompensaRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150)
        String nome,

        @Size(max = 500)
        String descricao,

        @NotNull(message = "Pontos necessários são obrigatórios")
        @Positive(message = "Pontos devem ser maiores que zero")
        Integer pontosNecessarios,

        @NotNull(message = "Quantidade é obrigatória")
        @PositiveOrZero(message = "Quantidade não pode ser negativa")
        Integer quantidade
) {}
