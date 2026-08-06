-- V24__reconciliar_campos_censo_aluno.sql
-- Garante que as colunas usadas pelo AlunoRepository existam, independente
-- de qual das duas migrations anteriores (V20/V21) foi de fato aplicada.

ALTER TABLE aluno ADD COLUMN IF NOT EXISTS tipo_condicao VARCHAR(200);
ALTER TABLE aluno ADD COLUMN IF NOT EXISTS certidao_nascimento VARCHAR(100);

-- Se a coluna divergente da V21 existir com dado já digitado por algum teste,
-- migre o conteúdo antes de seguir em frente (não perca dado de teste):
UPDATE aluno SET tipo_condicao = tipo_condicao_especial
WHERE tipo_condicao IS NULL AND tipo_condicao_especial IS NOT NULL;

UPDATE aluno SET certidao_nascimento = numero_certidao_nascimento
WHERE certidao_nascimento IS NULL AND numero_certidao_nascimento IS NOT NULL;