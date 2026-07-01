-- V2__fix_escola_schema.sql
-- Adiciona colunas faltantes na tabela escola e renomeia colunas de timestamp

-- Adiciona coluna cnpj (UNIQUE, mas NULL por enquanto)
ALTER TABLE escola ADD COLUMN IF NOT EXISTS cnpj VARCHAR(18) UNIQUE;

-- Adiciona coluna status com padrão 'ATIVA'
ALTER TABLE escola ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ATIVA';

-- Renomeia created_at para criado_em
ALTER TABLE escola RENAME COLUMN created_at TO criado_em;

-- Renomeia updated_at para atualizado_em
ALTER TABLE escola RENAME COLUMN updated_at TO atualizado_em;
