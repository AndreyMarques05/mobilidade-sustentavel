-- ============================================================
-- V2 - Dados iniciais (tipos de transporte e usuário admin)
-- ============================================================

-- Tipos de transporte sustentável
-- emissao_co2_por_km: kg CO2 emitido por km percorrido
-- Referência carro a combustão: 0.21 kg/km (CETESB 2023)
INSERT INTO tb_tipo_transporte (id, nome, descricao, emissao_co2_por_km, pontos_por_km)
VALUES (seq_tipo_transporte.NEXTVAL, 'Bicicleta',
        'Transporte 100% limpo, zero emissão. Ideal para curtas distâncias.', 0.0000, 10);

INSERT INTO tb_tipo_transporte (id, nome, descricao, emissao_co2_por_km, pontos_por_km)
VALUES (seq_tipo_transporte.NEXTVAL, 'Caminhada',
        'Deslocamento a pé, sem emissão de carbono.', 0.0000, 8);

INSERT INTO tb_tipo_transporte (id, nome, descricao, emissao_co2_por_km, pontos_por_km)
VALUES (seq_tipo_transporte.NEXTVAL, 'Patinete Elétrico',
        'Micromobilidade elétrica com emissão mínima.', 0.0160, 7);

INSERT INTO tb_tipo_transporte (id, nome, descricao, emissao_co2_por_km, pontos_por_km)
VALUES (seq_tipo_transporte.NEXTVAL, 'Ônibus Elétrico',
        'Transporte público de alta capacidade com zero emissão direta.', 0.0000, 5);

INSERT INTO tb_tipo_transporte (id, nome, descricao, emissao_co2_por_km, pontos_por_km)
VALUES (seq_tipo_transporte.NEXTVAL, 'Metrô / Trem',
        'Modal ferroviário de alta capacidade com baixa emissão.', 0.0050, 5);

INSERT INTO tb_tipo_transporte (id, nome, descricao, emissao_co2_por_km, pontos_por_km)
VALUES (seq_tipo_transporte.NEXTVAL, 'Ônibus Convencional',
        'Transporte coletivo a combustível com emissão compartilhada.', 0.0650, 3);

INSERT INTO tb_tipo_transporte (id, nome, descricao, emissao_co2_por_km, pontos_por_km)
VALUES (seq_tipo_transporte.NEXTVAL, 'Carro Elétrico',
        'Veículo individual elétrico. Melhor que combustão, mas menos que coletivo.', 0.0480, 4);

INSERT INTO tb_tipo_transporte (id, nome, descricao, emissao_co2_por_km, pontos_por_km)
VALUES (seq_tipo_transporte.NEXTVAL, 'Carro Híbrido',
        'Veículo com motorização híbrida (elétrico + combustão).', 0.1200, 2);

-- Usuário ADMIN padrão
-- Senha: admin123 (BCrypt — gere o hash com BCryptPasswordEncoder antes de usar em produção)
INSERT INTO tb_usuario (id, nome, email, senha, tipo, role, pontos_recompensa, data_cadastro, ativo)
VALUES (seq_usuario.NEXTVAL, 'Administrador', 'admin@mobilidade.com',
        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', -- senha: password
        'PESSOA_FISICA', 'ADMIN', 0, CURRENT_TIMESTAMP, 1);

-- Recompensas iniciais
INSERT INTO tb_recompensa (id, nome, descricao, pontos_necessarios, quantidade, data_criacao)
VALUES (seq_recompensa.NEXTVAL, 'Desconto 10% em Bike Share',
        'Cupom de 10% de desconto em aluguel de bicicletas compartilhadas.', 100, 50, CURRENT_TIMESTAMP);

INSERT INTO tb_recompensa (id, nome, descricao, pontos_necessarios, quantidade, data_criacao)
VALUES (seq_recompensa.NEXTVAL, 'Crédito R$ 5,00 no Transporte Público',
        'Crédito de R$ 5,00 no cartão de transporte público.', 200, 100, CURRENT_TIMESTAMP);

INSERT INTO tb_recompensa (id, nome, descricao, pontos_necessarios, quantidade, data_criacao)
VALUES (seq_recompensa.NEXTVAL, 'Planta uma Árvore em seu Nome',
        'Patrocínio de plantio de uma muda de árvore nativa.', 500, 30, CURRENT_TIMESTAMP);

INSERT INTO tb_recompensa (id, nome, descricao, pontos_necessarios, quantidade, data_criacao)
VALUES (seq_recompensa.NEXTVAL, 'Kit Ciclista Sustentável',
        'Kit com garrafa reutilizável, cadeado e luzes para bicicleta.', 1000, 20, CURRENT_TIMESTAMP);

COMMIT;
