package br.com.fiap.mobilidade.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_viagem")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Viagem {

    /** Emissão média de um carro a combustão em kg CO2/km (referência CETESB). */
    public static final double EMISSAO_CARRO_REFERENCIA_KG_POR_KM = 0.21;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_viagem")
    @SequenceGenerator(name = "seq_viagem", sequenceName = "seq_viagem", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_transporte", nullable = false)
    private TipoTransporte tipoTransporte;

    @Column(nullable = false, length = 200)
    private String origem;

    @Column(nullable = false, length = 200)
    private String destino;

    @Column(name = "distancia_km", nullable = false)
    private Double distanciaKm;

    @Column(name = "data_viagem", nullable = false)
    private LocalDateTime dataViagem;

    @Column(name = "co2_economizado_kg", nullable = false)
    private Double co2EconomizadoKg;

    @Column(name = "pontos_ganhos", nullable = false)
    private Integer pontosGanhos;

    @Column(length = 500)
    private String descricao;

    @PrePersist
    protected void calcular() {
        if (tipoTransporte != null && distanciaKm != null) {
            double economizado = (EMISSAO_CARRO_REFERENCIA_KG_POR_KM - tipoTransporte.getEmissaoCo2PorKm()) * distanciaKm;
            co2EconomizadoKg = Math.max(0.0, economizado);
            pontosGanhos = (int) (distanciaKm * tipoTransporte.getPontosPorKm());
        }
        if (dataViagem == null) dataViagem = LocalDateTime.now();
    }
}
