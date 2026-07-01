-- V3__fix_usuario_schema.sql
-- Adiciona/renomeia colunas na tabela usuario para alinhar com UsuarioRepository.java

-- Adiciona coluna escola_id se não existir
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS escola_id UUID;

-- Renomeia 'nome' para 'nome_completo'
ALTER TABLE usuario RENAME COLUMN nome TO nome_completo;

-- Adiciona coluna email se não existir
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS email VARCHAR(150) UNIQUE;

-- Renomeia 'cpf' e garante que seja string (já é VARCHAR)
-- Apenas certificamos que existe com o tamanho adequado
ALTER TABLE usuario ALTER COLUMN cpf TYPE VARCHAR(14);

-- Adiciona coluna telefone se não existir
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS telefone VARCHAR(20);

-- Renomeia 'senha' para 'senha_hash'
ALTER TABLE usuario RENAME COLUMN senha TO senha_hash;

-- Renomeia 'ativo' para manter compatibilidade, mas já tem este nome
-- Verifica se coluna 'ativo' existe e funciona como esperado

-- Adiciona coluna 'bloqueado' se não existir (para bloquear login)
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS bloqueado BOOLEAN DEFAULT FALSE;

-- Renomeia 'tentativas_login' - já existe, apenas confirmar
-- Coluna já existe

-- Renomeia 'bloqueado_ate' para 'ultimo_login'
-- Vamos dropá-la e criar a nova, pois a semântica mudou
ALTER TABLE usuario DROP COLUMN IF EXISTS bloqueado_ate;

-- Adiciona coluna 'ultimo_login' para rastrear último acesso
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS ultimo_login TIMESTAMP;

-- Renomeia 'created_at' para 'criado_em'
ALTER TABLE usuario RENAME COLUMN created_at TO criado_em;

-- Renomeia 'updated_at' para 'atualizado_em'
ALTER TABLE usuario RENAME COLUMN updated_at TO atualizado_em;

-- Adiciona coluna 'reset_password_token' para recuperação de senha
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS reset_password_token VARCHAR(255);

-- Adiciona coluna 'reset_password_expires_at' para expiração do token
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS reset_password_expires_at TIMESTAMP;
