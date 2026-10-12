package br.com.fiap.mobilidade.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_recompensa")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Recompensa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_recompensa")
    @SequenceGenerator(name = "seq_recompensa", sequenceName = "seq_recompensa", allocationSize = 1)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(name = "pontos_necessarios", nullable = false)
    private Integer pontosNecessarios;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantidade = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
    }
}
