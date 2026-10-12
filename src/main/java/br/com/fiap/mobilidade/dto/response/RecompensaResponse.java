package br.com.fiap.mobilidade.dto.response;

import br.com.fiap.mobilidade.model.Recompensa;

import java.time.LocalDateTime;

public record RecompensaResponse(
        Long id,
        String nome,
        String descricao,
        Integer pontosNecessarios,
        Integer quantidade,
        Boolean ativo,
        LocalDateTime dataCriacao
) {
    public static RecompensaResponse from(Recompensa r) {
        return new RecompensaResponse(
                r.getId(), r.getNome(), r.getDescricao(),
                r.getPontosNecessarios(), r.getQuantidade(), r.getAtivo(), r.getDataCriacao()
        );
    }
}
