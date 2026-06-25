-- V2__Criar_tabelas_academicas.sql

CREATE TABLE IF NOT EXISTS turmas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    escola_id UUID NOT NULL,
    ano_letivo_id UUID NOT NULL,
    nome VARCHAR(255) NOT NULL,
    turno VARCHAR(50) NOT NULL, -- Ex: MANHA, TARDE, NOITE
    serie INTEGER NOT NULL,
    FOREIGN KEY (escola_id) REFERENCES escolas(id),
    FOREIGN KEY (ano_letivo_id) REFERENCES ano_letivo(id)
);

CREATE TABLE IF NOT EXISTS alunos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    escola_id UUID NOT NULL,
    turma_id UUID,
    nome VARCHAR(255) NOT NULL,
    cpf VARCHAR(14) UNIQUE NOT NULL,
    data_nascimento DATE NOT NULL,
    cor_pele VARCHAR(50),
    deficiencias TEXT,
    endereco TEXT,
    status VARCHAR(50) NOT NULL, -- Ex: ATIVO, TRANSFERIDO, CANCELADO
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (escola_id) REFERENCES escolas(id),
    FOREIGN KEY (turma_id) REFERENCES turmas(id)
);

CREATE TABLE IF NOT EXISTS disciplinas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    escola_id UUID NOT NULL,
    nome VARCHAR(255) NOT NULL,
    FOREIGN KEY (escola_id) REFERENCES escolas(id)
);

CREATE TABLE IF NOT EXISTS notas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aluno_id UUID NOT NULL,
    disciplina_id UUID NOT NULL,
    ano_letivo_id UUID NOT NULL,
    bimestre_trimestre INTEGER NOT NULL,
    valor DECIMAL(4,2) NOT NULL,
    data_lancamento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (aluno_id) REFERENCES alunos(id),
    FOREIGN KEY (disciplina_id) REFERENCES disciplinas(id),
    FOREIGN KEY (ano_letivo_id) REFERENCES ano_letivo(id)
);
