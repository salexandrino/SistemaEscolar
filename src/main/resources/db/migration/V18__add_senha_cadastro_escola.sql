-- src/main/resources/db/migration/V18__add_senha_cadastro_escola.sql
ALTER TABLE escola ADD COLUMN IF NOT EXISTS senha_cadastro VARCHAR(100);

UPDATE escola SET senha_cadastro = 'escola123' WHERE nome = 'Escola Padrão';