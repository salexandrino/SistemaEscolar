-- V4__seed_escola_padrao.sql
INSERT INTO escola (nome, cnpj, status, criado_em, atualizado_em)
VALUES ('Escola Padrão', '00.000.000/0001-00', 'ATIVA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;