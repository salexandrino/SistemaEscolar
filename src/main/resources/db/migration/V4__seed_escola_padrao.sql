-- V4__seed_escola_padrao.sql
-- Insere uma escola padrão para desenvolvimento e testes

INSERT INTO escola (nome, cnpj, status, senha_cadastro, criado_em, atualizado_em)
VALUES ('Escola Padrão', '00.000.000/0001-00', 'ATIVA', 'escola123', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    ON CONFLICT DO NOTHING;