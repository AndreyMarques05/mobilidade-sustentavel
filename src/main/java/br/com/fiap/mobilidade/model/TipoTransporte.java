package br.com.fiap.mobilidade.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_tipo_transporte")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TipoTransporte {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_tipo_transporte")
    @SequenceGenerator(name = "seq_tipo_transporte", sequenceName = "seq_tipo_transporte", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Column(length = 500)
    private String descricao;

    /**
     * kg CO2 emitido por km (0 para bicicleta/caminhada; menor que carro a combustão).
     * Usado para calcular CO2 economizado em relação ao carro médio (0,21 kg/km).
     */
    @Column(name = "emissao_co2_por_km", nullable = false)
    private Double emissaoCo2PorKm;

    @Column(name = "pontos_por_km", nullable = false)
    private Integer pontosPorKm;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;
}
