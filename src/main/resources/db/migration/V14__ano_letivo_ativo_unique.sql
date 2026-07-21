-- V14__ano_letivo_ativo_unique.sql
-- Garante unicidade de Ano Letivo ativo por tenant

-- Nota: alguns parsers de SQL do Flyway reclamam de "IF NOT EXISTS" para índices.
-- Para garantir compatibilidade, removemos o IF NOT EXISTS.
-- Alternativa sem cláusula WHERE (compatibilidade de parser): índice único por expressão
-- Garante que apenas um registro por tenant terá (ativo=TRUE)
CREATE UNIQUE INDEX uq_ano_letivo_ativo_tenant
    ON ano_letivo ((CASE WHEN ativo THEN tenant_id ELSE NULL END));
