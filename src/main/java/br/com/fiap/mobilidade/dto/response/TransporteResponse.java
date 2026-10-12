package br.com.fiap.mobilidade.dto.response;

import br.com.fiap.mobilidade.model.TipoTransporte;

public record TransporteResponse(
        Long id,
        String nome,
        String descricao,
        Double emissaoCo2PorKm,
        Integer pontosPorKm,
        Boolean ativo
) {
    public static TransporteResponse from(TipoTransporte t) {
        return new TransporteResponse(
                t.getId(), t.getNome(), t.getDescricao(),
                t.getEmissaoCo2PorKm(), t.getPontosPorKm(), t.getAtivo()
        );
    }
}
