-- V20__aluno_censo_escolar.sql
-- Campos do Censo Escolar / Educacenso para o cadastro de Aluno.

ALTER TABLE aluno
    ADD COLUMN IF NOT EXISTS codigo_inep VARCHAR(20),
    ADD COLUMN IF NOT EXISTS nome_pai VARCHAR(160),
    ADD COLUMN IF NOT EXISTS nome_mae VARCHAR(160),
    ADD COLUMN IF NOT EXISTS sexo VARCHAR(20), -- MASCULINO, FEMININO
    ADD COLUMN IF NOT EXISTS cor_raca VARCHAR(20), -- BRANCA, PRETA, PARDA, AMARELA, INDIGENA, NAO_DECLARADA
    ADD COLUMN IF NOT EXISTS nacionalidade VARCHAR(60) DEFAULT 'Brasileira',
    ADD COLUMN IF NOT EXISTS uf_nascimento VARCHAR(2),
    ADD COLUMN IF NOT EXISTS municipio_nascimento VARCHAR(160),
    ADD COLUMN IF NOT EXISTS certidao_nascimento VARCHAR(100),
    ADD COLUMN IF NOT EXISTS nis VARCHAR(20),
    -- Endereço (mesmo bloco padrão usado na Escola)
    ADD COLUMN IF NOT EXISTS cep VARCHAR(9),
    ADD COLUMN IF NOT EXISTS endereco VARCHAR(200),
    ADD COLUMN IF NOT EXISTS numero VARCHAR(20),
    ADD COLUMN IF NOT EXISTS complemento VARCHAR(100),
    ADD COLUMN IF NOT EXISTS bairro VARCHAR(100),
    ADD COLUMN IF NOT EXISTS cidade VARCHAR(160),
    ADD COLUMN IF NOT EXISTS estado VARCHAR(2),
    ADD COLUMN IF NOT EXISTS zona VARCHAR(20), -- URBANA, RURAL
    ADD COLUMN IF NOT EXISTS localizacao_diferenciada VARCHAR(30) DEFAULT 'NAO_DIFERENCIADA',
    -- Transporte escolar
    ADD COLUMN IF NOT EXISTS usa_transporte_escolar BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS responsavel_transporte VARCHAR(20), -- ESTADUAL, MUNICIPAL, NENHUM
-- Acessibilidade / Educação Especial
-- Texto livre por enquanto (não enum fechado) -- lista oficial do Educacenso é
-- extensa; comece com o essencial e expanda depois se precisar.
    ADD COLUMN IF NOT EXISTS tipo_condicao VARCHAR(200),
    ADD COLUMN IF NOT EXISTS recursos_acessibilidade TEXT;