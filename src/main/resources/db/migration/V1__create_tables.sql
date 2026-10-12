-- ============================================================
-- V1 - Criação das sequences e tabelas do schema
-- ============================================================

-- Sequences
CREATE SEQUENCE seq_usuario      START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_tipo_transporte START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_viagem       START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_recompensa   START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

-- Tabela de usuários (empresas e pessoas físicas)
CREATE TABLE tb_usuario (
    id                NUMBER(19)    NOT NULL,
    nome              VARCHAR2(150) NOT NULL,
    email             VARCHAR2(200) NOT NULL,
    senha             VARCHAR2(255) NOT NULL,
    tipo              VARCHAR2(20)  NOT NULL,   -- PESSOA_FISICA | EMPRESA
    role              VARCHAR2(10)  NOT NULL,   -- USER | ADMIN
    pontos_recompensa NUMBER(10)    DEFAULT 0   NOT NULL,
    data_cadastro     TIMESTAMP     NOT NULL,
    ativo             NUMBER(1)     DEFAULT 1   NOT NULL,
    CONSTRAINT pk_usuario        PRIMARY KEY (id),
    CONSTRAINT uq_usuario_email  UNIQUE (email),
    CONSTRAINT ck_usuario_tipo   CHECK (tipo IN ('PESSOA_FISICA', 'EMPRESA')),
    CONSTRAINT ck_usuario_role   CHECK (role IN ('USER', 'ADMIN')),
    CONSTRAINT ck_usuario_ativo  CHECK (ativo IN (0, 1))
);

-- Tabela de tipos de transporte sustentável
CREATE TABLE tb_tipo_transporte (
    id                  NUMBER(19)    NOT NULL,
    nome                VARCHAR2(100) NOT NULL,
    descricao           VARCHAR2(500),
    emissao_co2_por_km  FLOAT         NOT NULL,
    pontos_por_km       NUMBER(5)     NOT NULL,
    ativo               NUMBER(1)     DEFAULT 1 NOT NULL,
    CONSTRAINT pk_tipo_transporte      PRIMARY KEY (id),
    CONSTRAINT uq_tipo_transporte_nome UNIQUE (nome),
    CONSTRAINT ck_tipo_transporte_ativo CHECK (ativo IN (0, 1))
);

-- Tabela de viagens realizadas
CREATE TABLE tb_viagem (
    id                  NUMBER(19)    NOT NULL,
    id_usuario          NUMBER(19)    NOT NULL,
    id_tipo_transporte  NUMBER(19)    NOT NULL,
    origem              VARCHAR2(200) NOT NULL,
    destino             VARCHAR2(200) NOT NULL,
    distancia_km        FLOAT         NOT NULL,
    data_viagem         TIMESTAMP     NOT NULL,
    co2_economizado_kg  FLOAT         NOT NULL,
    pontos_ganhos       NUMBER(10)    NOT NULL,
    descricao           VARCHAR2(500),
    CONSTRAINT pk_viagem             PRIMARY KEY (id),
    CONSTRAINT fk_viagem_usuario     FOREIGN KEY (id_usuario)         REFERENCES tb_usuario (id),
    CONSTRAINT fk_viagem_transporte  FOREIGN KEY (id_tipo_transporte) REFERENCES tb_tipo_transporte (id)
);

-- Tabela de recompensas
CREATE TABLE tb_recompensa (
    id                  NUMBER(19)    NOT NULL,
    nome                VARCHAR2(150) NOT NULL,
    descricao           VARCHAR2(500),
    pontos_necessarios  NUMBER(10)    NOT NULL,
    quantidade          NUMBER(10)    DEFAULT 0 NOT NULL,
    ativo               NUMBER(1)     DEFAULT 1 NOT NULL,
    data_criacao        TIMESTAMP     NOT NULL,
    CONSTRAINT pk_recompensa       PRIMARY KEY (id),
    CONSTRAINT ck_recompensa_ativo CHECK (ativo IN (0, 1))
);

-- Índices para otimizar consultas frequentes
CREATE INDEX idx_viagem_usuario    ON tb_viagem (id_usuario);
CREATE INDEX idx_viagem_data       ON tb_viagem (data_viagem DESC);
CREATE INDEX idx_usuario_pontos    ON tb_usuario (pontos_recompensa DESC);
