-- V13__turmas_e_alocacao.sql
-- Ajustes para Turmas (capacidade/status) e carga horária contratual de Professor

-- Segurança: garantir extensão para UUID
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Adiciona coluna 'capacidade' em turma, caso não exista
ALTER TABLE IF EXISTS turma
    ADD COLUMN IF NOT EXISTS capacidade INT NOT NULL DEFAULT 0;

-- Adiciona coluna 'situacao' em turma, caso não exista (ATIVA | ENCERRADA)
ALTER TABLE IF EXISTS turma
    ADD COLUMN IF NOT EXISTS situacao VARCHAR(20) NOT NULL DEFAULT 'ATIVA';

-- Adiciona coluna 'carga_horaria_contratual' em professor, caso não exista
ALTER TABLE IF EXISTS professor
    ADD COLUMN IF NOT EXISTS carga_horaria_contratual INT NOT NULL DEFAULT 20;

-- Índice/unique para evitar duplicidade de alocação na mesma turma e disciplina
CREATE UNIQUE INDEX IF NOT EXISTS uk_tdp_turma_disciplina_prof 
    ON turma_disciplina_professor (id_turma, id_disciplina, id_professor);
