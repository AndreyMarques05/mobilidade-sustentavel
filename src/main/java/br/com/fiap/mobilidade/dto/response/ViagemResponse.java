package br.com.fiap.mobilidade.dto.response;

import br.com.fiap.mobilidade.model.Viagem;

import java.time.LocalDateTime;

public record ViagemResponse(
        Long id,
        Long idUsuario,
        String nomeUsuario,
        Long idTipoTransporte,
        String tipoTransporte,
        String origem,
        String destino,
        Double distanciaKm,
        LocalDateTime dataViagem,
        Double co2EconomizadoKg,
        Integer pontosGanhos,
        String descricao
) {
    public static ViagemResponse from(Viagem v) {
        return new ViagemResponse(
                v.getId(),
                v.getUsuario().getId(),
                v.getUsuario().getNome(),
                v.getTipoTransporte().getId(),
                v.getTipoTransporte().getNome(),
                v.getOrigem(),
                v.getDestino(),
                v.getDistanciaKm(),
                v.getDataViagem(),
                v.getCo2EconomizadoKg(),
                v.getPontosGanhos(),
                v.getDescricao()
        );
    }
}
