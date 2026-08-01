-- V18__campos_censo_escolar_aluno.sql
-- Adiciona os campos exigidos pelo Educacenso ao cadastro de Aluno

-- Identificação
ALTER TABLE aluno ADD COLUMN IF NOT EXISTS codigo_inep VARCHAR(50);
ALTER TABLE aluno ADD COLUMN IF NOT EXISTS nome_pai VARCHAR(255);
ALTER TABLE aluno ADD COLUMN IF NOT EXISTS nome_mae VARCHAR(255);
ALTER TABLE aluno ADD COLUMN IF NOT EXISTS sexo VARCHAR(20);
ALTER TABLE aluno ADD COLUMN IF NOT EXISTS cor_raca VARCHAR(30);
ALTER TABLE aluno ADD COLUMN IF NOT EXISTS nacionalidade VARCHAR(100) DEFAULT 'Brasileira';
ALTER TABLE aluno ADD COLUMN IF NOT EXISTS uf_nascimento VARCHAR(2);
ALTER TABLE aluno ADD COLUMN IF NOT EXISTS municipio_nascimento VARCHAR(100);
ALTER TABLE aluno ADD COLUMN IF NOT EXISTS numero_certidao_nascimento VARCHAR(50);
ALTER TABLE aluno ADD COLUMN IF NOT EXISTS nis VARCHAR(20);

-- Endereço (mesmo padrão de nomenclatura usado em escola, V6)
ALTER TABLE aluno ADD COLUMN IF NOT EXISTS cep VARCHAR(10);
ALTER TABLE aluno ADD COLUMN IF NOT EXISTS endereco VARCHAR(255);
ALTER TABLE aluno ADD COLUMN IF NOT EXISTS numero VARCHAR(10);
ALTER TABLE aluno ADD COLUMN IF NOT EXISTS complemento VARCHAR(100);
ALTER TABLE aluno ADD COLUMN IF NOT EXISTS bairro VARCHAR(100);
ALTER TABLE aluno ADD COLUMN IF NOT EXISTS cidade VARCHAR(100);
ALTER TABLE aluno ADD COLUMN IF NOT EXISTS estado VARCHAR(2);
ALTER TABLE aluno ADD COLUMN IF NOT EXISTS zona VARCHAR(20);
ALTER TABLE aluno ADD COLUMN IF NOT EXISTS localizacao_diferenciada VARCHAR(30);

-- Transporte escolar
ALTER TABLE aluno ADD COLUMN IF NOT EXISTS usa_transporte_escolar BOOLEAN DEFAULT FALSE;
ALTER TABLE aluno ADD COLUMN IF NOT EXISTS responsavel_transporte VARCHAR(20);

-- Acessibilidade / Educação Especial
ALTER TABLE aluno ADD COLUMN IF NOT EXISTS tipo_condicao_especial VARCHAR(255);
ALTER TABLE aluno ADD COLUMN IF NOT EXISTS recursos_acessibilidade TEXT;