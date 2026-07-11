-- V5__ensure_escola_columns.sql
-- Garante que colunas esperadas pela aplicação existam (idempotente)

DO $$
BEGIN
    -- Rename timestamps only if original columns exist and target names don't
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='escola' AND column_name='created_at')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='escola' AND column_name='criado_em') THEN
ALTER TABLE escola RENAME COLUMN created_at TO criado_em;
END IF;

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='escola' AND column_name='updated_at')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='escola' AND column_name='atualizado_em') THEN
ALTER TABLE escola RENAME COLUMN updated_at TO atualizado_em;
END IF;
END
$$;

-- Adiciona colunas caso não existam (são idempotentes)
ALTER TABLE escola ADD COLUMN IF NOT EXISTS cnpj VARCHAR(18);
ALTER TABLE escola ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ATIVA';

-- 🔐 NOVA COLUNA: Senha necessária para validação no auto-cadastro de novos usuários
ALTER TABLE escola ADD COLUMN IF NOT EXISTS senha_cadastro VARCHAR(100) DEFAULT 'synge123';