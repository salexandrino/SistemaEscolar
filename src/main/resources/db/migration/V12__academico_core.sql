-- V12__academico_core.sql
-- Núcleo do módulo acadêmico (multi-tenant)

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Ano Letivo
CREATE TABLE IF NOT EXISTS ano_letivo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    ano INTEGER NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE NOT NULL,
    situacao VARCHAR(20) NOT NULL DEFAULT 'ATIVO', -- ATIVO, ARQUIVADO
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_ano_letivo_ano_tenant ON ano_letivo(tenant_id, ano);

-- Disciplina (global do tenant)
CREATE TABLE IF NOT EXISTS disciplina (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    nome VARCHAR(160) NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_disciplina_nome_tenant ON disciplina(tenant_id, nome);
CREATE INDEX IF NOT EXISTS idx_disciplina_tenant ON disciplina(tenant_id);

-- Série (matriz curricular por ano letivo)
CREATE TABLE IF NOT EXISTS serie (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    id_ano_letivo UUID NOT NULL,
    nome VARCHAR(160) NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_serie_nome_ano_tenant ON serie(tenant_id, id_ano_letivo, nome);
CREATE INDEX IF NOT EXISTS idx_serie_tenant ON serie(tenant_id);

-- Associação Série x Disciplina (define carga horária anual padrão)
CREATE TABLE IF NOT EXISTS serie_disciplina (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    id_serie UUID NOT NULL,
    id_disciplina UUID NOT NULL,
    carga_horaria_anual INTEGER NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_serie_disciplina ON serie_disciplina(tenant_id, id_serie, id_disciplina);
CREATE INDEX IF NOT EXISTS idx_serie_disciplina_tenant ON serie_disciplina(tenant_id);

-- Professor
CREATE TABLE IF NOT EXISTS professor (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    nome VARCHAR(160) NOT NULL,
    cpf VARCHAR(14) NOT NULL,
    email VARCHAR(200),
    telefone VARCHAR(30),
    carga_horaria_contratual INTEGER NOT NULL DEFAULT 20,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_professor_cpf_tenant ON professor(tenant_id, cpf);
CREATE INDEX IF NOT EXISTS idx_professor_tenant ON professor(tenant_id);

-- Aluno
CREATE TABLE IF NOT EXISTS aluno (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    nome VARCHAR(160) NOT NULL,
    cpf VARCHAR(14),
    data_nascimento DATE,
    email VARCHAR(200),
    telefone VARCHAR(30),
    situacao VARCHAR(20) NOT NULL DEFAULT 'ATIVO', -- ATIVO, TRANCADO, TRANSFERIDO, CONCLUIDO, CANCELADO
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_aluno_cpf_tenant ON aluno(tenant_id, cpf);
CREATE INDEX IF NOT EXISTS idx_aluno_tenant ON aluno(tenant_id);

-- Turma
CREATE TABLE IF NOT EXISTS turma (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    id_ano_letivo UUID NOT NULL,
    id_serie UUID NOT NULL,
    nome VARCHAR(160) NOT NULL,
    turno VARCHAR(30) NOT NULL, -- MANHA, TARDE, NOITE, INTEGRAL
    sala VARCHAR(60),
    capacidade INTEGER NOT NULL DEFAULT 35,
    encerrada BOOLEAN NOT NULL DEFAULT FALSE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_turma_nome_ano_tenant ON turma(tenant_id, id_ano_letivo, nome);
CREATE INDEX IF NOT EXISTS idx_turma_tenant ON turma(tenant_id);

-- Matrícula
CREATE TABLE IF NOT EXISTS matricula (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    id_aluno UUID NOT NULL,
    id_turma UUID NOT NULL,
    data_matricula DATE NOT NULL DEFAULT CURRENT_DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVA', -- ATIVA, TRANSFERIDA, CANCELADA, CONCLUIDA
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_matricula_aluno_turma ON matricula(tenant_id, id_aluno, id_turma);
CREATE INDEX IF NOT EXISTS idx_matricula_tenant ON matricula(tenant_id);

-- Turma x Disciplina x Professor (alocação)
CREATE TABLE IF NOT EXISTS turma_disciplina_professor (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    id_turma UUID NOT NULL,
    id_disciplina UUID NOT NULL,
    id_professor UUID NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_tdp ON turma_disciplina_professor(tenant_id, id_turma, id_disciplina);
CREATE INDEX IF NOT EXISTS idx_tdp_tenant ON turma_disciplina_professor(tenant_id);

-- Avaliação (configurável por disciplina/turma)
CREATE TABLE IF NOT EXISTS avaliacao (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    id_turma UUID NOT NULL,
    id_disciplina UUID NOT NULL,
    nome VARCHAR(120) NOT NULL,
    peso NUMERIC(5,2) NOT NULL DEFAULT 1.0,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_avaliacao_tenant ON avaliacao(tenant_id);

-- Nota (lançamento de notas por aluno/avaliação)
CREATE TABLE IF NOT EXISTS nota (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    id_avaliacao UUID NOT NULL,
    id_aluno UUID NOT NULL,
    valor NUMERIC(5,2) NOT NULL CHECK (valor >= 0 AND valor <= 10),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_nota_avaliacao_aluno ON nota(tenant_id, id_avaliacao, id_aluno);
CREATE INDEX IF NOT EXISTS idx_nota_tenant ON nota(tenant_id);

-- Frequência
CREATE TABLE IF NOT EXISTS frequencia (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    id_turma UUID NOT NULL,
    id_disciplina UUID NOT NULL,
    id_aluno UUID NOT NULL,
    data DATE NOT NULL,
    situacao VARCHAR(20) NOT NULL, -- PRESENTE, FALTA, FALTA_JUSTIFICADA, ATRASO
    observacao VARCHAR(255),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_freq_aluno_dia_disciplina ON frequencia(tenant_id, id_aluno, data, id_disciplina, id_turma);
CREATE INDEX IF NOT EXISTS idx_frequencia_tenant ON frequencia(tenant_id);

-- Histórico de Situação do Aluno
CREATE TABLE IF NOT EXISTS historico_situacao_aluno (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    id_aluno UUID NOT NULL,
    situacao_anterior VARCHAR(20),
    situacao_nova VARCHAR(20) NOT NULL,
    motivo VARCHAR(255),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_hist_sit_aluno_tenant ON historico_situacao_aluno(tenant_id, id_aluno);

-- Chaves estrangeiras básicas (sem cascatas agressivas)
ALTER TABLE serie
    ADD CONSTRAINT fk_serie_ano FOREIGN KEY (id_ano_letivo) REFERENCES ano_letivo(id);

ALTER TABLE serie_disciplina
    ADD CONSTRAINT fk_sd_serie FOREIGN KEY (id_serie) REFERENCES serie(id),
    ADD CONSTRAINT fk_sd_disciplina FOREIGN KEY (id_disciplina) REFERENCES disciplina(id);

ALTER TABLE turma
    ADD CONSTRAINT fk_turma_ano FOREIGN KEY (id_ano_letivo) REFERENCES ano_letivo(id),
    ADD CONSTRAINT fk_turma_serie FOREIGN KEY (id_serie) REFERENCES serie(id);

ALTER TABLE matricula
    ADD CONSTRAINT fk_matricula_aluno FOREIGN KEY (id_aluno) REFERENCES aluno(id),
    ADD CONSTRAINT fk_matricula_turma FOREIGN KEY (id_turma) REFERENCES turma(id);

ALTER TABLE turma_disciplina_professor
    ADD CONSTRAINT fk_tdp_turma FOREIGN KEY (id_turma) REFERENCES turma(id),
    ADD CONSTRAINT fk_tdp_disciplina FOREIGN KEY (id_disciplina) REFERENCES disciplina(id),
    ADD CONSTRAINT fk_tdp_professor FOREIGN KEY (id_professor) REFERENCES professor(id);

ALTER TABLE avaliacao
    ADD CONSTRAINT fk_av_turma FOREIGN KEY (id_turma) REFERENCES turma(id),
    ADD CONSTRAINT fk_av_disciplina FOREIGN KEY (id_disciplina) REFERENCES disciplina(id);

ALTER TABLE nota
    ADD CONSTRAINT fk_nota_avaliacao FOREIGN KEY (id_avaliacao) REFERENCES avaliacao(id),
    ADD CONSTRAINT fk_nota_aluno FOREIGN KEY (id_aluno) REFERENCES aluno(id);
