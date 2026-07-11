-- V15__alunos_matriculas_documentos.sql
-- Tabela para gerenciamento de documentos dos alunos

CREATE TABLE IF NOT EXISTS documento_aluno (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    id_aluno UUID NOT NULL,
    tipo VARCHAR(50) NOT NULL, -- RG, CPF, CERTIDAO, LAUDO
    referencia VARCHAR(500) NOT NULL, -- URL ou descrição externa
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_documento_aluno_tenant ON documento_aluno(tenant_id, id_aluno);

ALTER TABLE documento_aluno
    ADD CONSTRAINT fk_doc_aluno FOREIGN KEY (id_aluno) REFERENCES aluno(id);
