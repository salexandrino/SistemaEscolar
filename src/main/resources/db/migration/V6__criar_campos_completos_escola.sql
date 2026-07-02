-- V6__criar_campos_completos_escola.sql
-- Adiciona todos os campos necessários para o CRUD completo de Escolas

-- Adiciona colunas se não existirem
ALTER TABLE escola ADD COLUMN IF NOT EXISTS email_institucional VARCHAR(150);
ALTER TABLE escola ADD COLUMN IF NOT EXISTS telefone VARCHAR(20);
ALTER TABLE escola ADD COLUMN IF NOT EXISTS endereco VARCHAR(255);
ALTER TABLE escola ADD COLUMN IF NOT EXISTS numero VARCHAR(10);
ALTER TABLE escola ADD COLUMN IF NOT EXISTS complemento VARCHAR(100);
ALTER TABLE escola ADD COLUMN IF NOT EXISTS bairro VARCHAR(100);
ALTER TABLE escola ADD COLUMN IF NOT EXISTS cidade VARCHAR(100);
ALTER TABLE escola ADD COLUMN IF NOT EXISTS estado VARCHAR(2);
ALTER TABLE escola ADD COLUMN IF NOT EXISTS cep VARCHAR(10);
ALTER TABLE escola ADD COLUMN IF NOT EXISTS nome_responsavel VARCHAR(255);
ALTER TABLE escola ADD COLUMN IF NOT EXISTS telefone_responsavel VARCHAR(20);
ALTER TABLE escola ADD COLUMN IF NOT EXISTS email_responsavel VARCHAR(150);
ALTER TABLE escola ADD COLUMN IF NOT EXISTS tenant_id UUID;

-- Cria índice UNIQUE para CNPJ se não existir
CREATE UNIQUE INDEX IF NOT EXISTS idx_escola_cnpj_unique ON escola (cnpj) WHERE cnpj IS NOT NULL;

-- Cria índice para tenant_id para melhorar performance
CREATE INDEX IF NOT EXISTS idx_escola_tenant_id ON escola (tenant_id);
