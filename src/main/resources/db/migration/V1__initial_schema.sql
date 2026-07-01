-- V1__initial_schema.sql

-- Cria a extensão pgcrypto para permitir a geração de UUIDs
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Tabela para armazenar informações das escolas (tenants)
CREATE TABLE IF NOT EXISTS escola (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela para armazenar informações dos usuários
CREATE TABLE IF NOT EXISTS usuario (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    nome VARCHAR(255) NOT NULL,
    cpf VARCHAR(14) UNIQUE NOT NULL, -- CPF no formato XXX.XXX.XXX-XX
    senha VARCHAR(255) NOT NULL, -- Hash BCrypt da senha
    perfil VARCHAR(50) NOT NULL, -- Ex: SUPER_ADMIN, GESTOR, SECRETARIA, PROFESSOR, FINANCEIRO
    ativo BOOLEAN DEFAULT TRUE,
    tentativas_login INTEGER DEFAULT 0,
    bloqueado_ate TIMESTAMP, -- Campo para armazenar até quando o login está bloqueado
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES escola(id)
);

-- Índices para otimização de consultas
CREATE INDEX IF NOT EXISTS idx_usuario_cpf ON usuario (cpf);
CREATE INDEX IF NOT EXISTS idx_usuario_tenant_id ON usuario (tenant_id);
