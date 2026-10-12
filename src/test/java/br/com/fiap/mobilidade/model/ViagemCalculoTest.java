package br.com.fiap.mobilidade.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes do núcleo ESG da aplicação: o cálculo de CO2 evitado e de pontos
 * de recompensa executado no @PrePersist da entidade Viagem.
 *
 * O teste vive no mesmo pacote da entidade para poder invocar o método
 * protegido calcular() sem reflexão.
 */
class ViagemCalculoTest {

    private TipoTransporte transporte(double emissaoCo2PorKm, int pontosPorKm) {
        return TipoTransporte.builder()
                .id(1L)
                .nome("Transporte de teste")
                .emissaoCo2PorKm(emissaoCo2PorKm)
                .pontosPorKm(pontosPorKm)
                .build();
    }

    private Viagem viagem(TipoTransporte tipo, double distanciaKm) {
        return Viagem.builder()
                .usuario(Usuario.builder().id(1L).nome("Andrey").build())
                .tipoTransporte(tipo)
                .origem("Casa")
                .destino("Faculdade")
                .distanciaKm(distanciaKm)
                .build();
    }

    @Test
    @DisplayName("Bicicleta: economiza todo o CO2 do carro de referência")
    void bicicletaEconomizaEmissaoIntegralDoCarro() {
        Viagem v = viagem(transporte(0.0, 10), 10.0);

        v.calcular();

        // (0,21 - 0,00) kg/km * 10 km = 2,1 kg de CO2 evitados
        assertThat(v.getCo2EconomizadoKg()).isEqualTo(2.1, org.assertj.core.api.Assertions.within(0.0001));
        assertThat(v.getPontosGanhos()).isEqualTo(100);
    }

    @Test
    @DisplayName("Ônibus: economiza apenas a diferença de emissão em relação ao carro")
    void onibusEconomizaApenasADiferenca() {
        Viagem v = viagem(transporte(0.09, 3), 20.0);

        v.calcular();

        // (0,21 - 0,09) kg/km * 20 km = 2,4 kg
        assertThat(v.getCo2EconomizadoKg()).isEqualTo(2.4, org.assertj.core.api.Assertions.within(0.0001));
        assertThat(v.getPontosGanhos()).isEqualTo(60);
    }

    @Test
    @DisplayName("Transporte mais poluente que o carro nunca gera economia negativa")
    void transporteMaisPoluenteNaoGeraEconomiaNegativa() {
        Viagem v = viagem(transporte(0.30, 1), 10.0);

        v.calcular();

        assertThat(v.getCo2EconomizadoKg()).isZero();
        assertThat(v.getPontosGanhos()).isEqualTo(10);
    }

    @Test
    @DisplayName("Data da viagem é preenchida automaticamente quando não informada")
    void dataViagemEhPreenchidaAutomaticamente() {
        Viagem v = viagem(transporte(0.0, 5), 4.0);
        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);

        v.calcular();

        assertThat(v.getDataViagem()).isNotNull().isAfter(antes);
    }

    @Test
    @DisplayName("Distância fracionada trunca os pontos para baixo")
    void distanciaFracionadaTruncaPontos() {
        Viagem v = viagem(transporte(0.0, 10), 2.55);

        v.calcular();

        // 2,55 km * 10 pontos/km = 25,5 -> 25 pontos
        assertThat(v.getPontosGanhos()).isEqualTo(25);
    }
}
