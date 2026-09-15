-- V1: Schema inicial consolidado — Kutuar Educação
--
-- Este arquivo substitui o histórico de 22 migrations incrementais
-- (V1 a V24 da versão SYNGE/Kutuar antiga), que acumulou patches
-- ("fix", "corrigir", "reconciliar") ao longo do tempo. Gerado
-- reaplicando as migrations antigas em um banco limpo e exportando
-- o schema resultante (pg_dump --schema-only), então é fiel ao
-- estado real de produção do sistema anterior — não foi reescrito
-- de memória.
--
-- Dados de semente (escola padrão + super admin) ficam em V2, separados
-- do DDL por boa prática (schema vs dados).
--
-- Pré-requisito: banco local recriado do zero
--   docker compose down -v && docker compose up -d

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;

SET default_tablespace = '';

SET default_table_access_method = heap;

CREATE TABLE public.aluno (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    tenant_id uuid NOT NULL,
    nome character varying(160) NOT NULL,
    cpf character varying(14),
    data_nascimento date,
    email character varying(200),
    telefone character varying(30),
    situacao character varying(20) DEFAULT 'ATIVO'::character varying NOT NULL,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    atualizado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    codigo_inep character varying(20),
    nome_filiacao character varying(255),
    sexo character varying(20),
    cor_raca character varying(50),
    nacionalidade character varying(50),
    uf_nascimento character varying(2),
    municipio_nascimento character varying(100),
    certidao_nascimento character varying(100),
    nis character varying(20),
    cep character varying(20),
    endereco character varying(255),
    numero character varying(20),
    complemento character varying(100),
    bairro character varying(100),
    cidade character varying(100),
    estado character varying(2),
    zona character varying(20),
    localizacao_diferenciada character varying(100),
    usa_transporte_escolar boolean DEFAULT false,
    responsavel_transporte character varying(50),
    tipo_acessibilidade character varying(50),
    recursos_acessibilidade text,
    nome_pai character varying(160),
    nome_mae character varying(160),
    tipo_condicao character varying(200),
    numero_certidao_nascimento character varying(50),
    tipo_condicao_especial character varying(255)
);

CREATE TABLE public.ano_letivo (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    tenant_id uuid NOT NULL,
    ano integer NOT NULL,
    data_inicio date NOT NULL,
    data_fim date NOT NULL,
    situacao character varying(20) DEFAULT 'ATIVO'::character varying NOT NULL,
    ativo boolean DEFAULT true NOT NULL,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    atualizado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE public.avaliacao (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    tenant_id uuid NOT NULL,
    id_turma uuid NOT NULL,
    id_disciplina uuid NOT NULL,
    nome character varying(120) NOT NULL,
    peso numeric(5,2) DEFAULT 1.0 NOT NULL,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    atualizado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE public.desconto (
    id uuid NOT NULL,
    tenant_id uuid NOT NULL,
    id_aluno uuid NOT NULL,
    id_mensalidade uuid,
    tipo character varying(50) NOT NULL,
    percentual numeric(5,2),
    valor_fixo numeric(10,2),
    descricao text,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE public.disciplina (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    tenant_id uuid NOT NULL,
    nome character varying(160) NOT NULL,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    atualizado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE public.documento_aluno (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    tenant_id uuid NOT NULL,
    id_aluno uuid NOT NULL,
    tipo character varying(50) NOT NULL,
    referencia character varying(500) NOT NULL,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    atualizado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE public.escola (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    nome character varying(255) NOT NULL,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    atualizado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    cnpj character varying(18),
    status character varying(20) DEFAULT 'ATIVA'::character varying NOT NULL,
    senha_cadastro character varying(100),
    email_institucional character varying(150),
    telefone character varying(20),
    endereco character varying(255),
    numero character varying(10),
    complemento character varying(100),
    bairro character varying(100),
    cidade character varying(100),
    estado character varying(2),
    cep character varying(10),
    nome_responsavel character varying(255),
    telefone_responsavel character varying(20),
    email_responsavel character varying(150),
    tenant_id uuid,
    codigo_inep character varying(20),
    situacao_funcionamento character varying(50),
    data_inicio_ano_letivo date,
    data_termino_ano_letivo date,
    latitude character varying(50),
    longitude character varying(50),
    zona character varying(20),
    localizacao_diferenciada character varying(100),
    dependencia_administrativa character varying(50),
    regulamentacao_numero character varying(100),
    regulamentacao_data date,
    infra_agua character varying(100),
    infra_energia character varying(100),
    infra_esgoto character varying(100),
    infra_lixo character varying(100),
    qtd_computadores integer DEFAULT 0,
    tem_internet boolean DEFAULT false,
    tipo_banda_larga character varying(100),
    lingua_ministrada character varying(50)
);

CREATE TABLE public.frequencia (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    tenant_id uuid NOT NULL,
    id_turma uuid NOT NULL,
    id_disciplina uuid NOT NULL,
    id_aluno uuid NOT NULL,
    data date NOT NULL,
    situacao character varying(20) NOT NULL,
    observacao character varying(255),
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    atualizado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE public.historico_situacao_aluno (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    tenant_id uuid NOT NULL,
    id_aluno uuid NOT NULL,
    situacao_anterior character varying(20),
    situacao_nova character varying(20) NOT NULL,
    motivo character varying(255),
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE public.matricula (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    tenant_id uuid NOT NULL,
    id_aluno uuid NOT NULL,
    id_turma uuid NOT NULL,
    data_matricula date DEFAULT CURRENT_DATE NOT NULL,
    status character varying(20) DEFAULT 'ATIVA'::character varying NOT NULL,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    atualizado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE public.mensalidade (
    id uuid NOT NULL,
    tenant_id uuid NOT NULL,
    id_aluno uuid NOT NULL,
    descricao character varying(255) NOT NULL,
    valor_original numeric(10,2) NOT NULL,
    data_vencimento date NOT NULL,
    status character varying(50) NOT NULL,
    data_pagamento date,
    valor_pago numeric(10,2),
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    atualizado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE public.nota (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    tenant_id uuid NOT NULL,
    id_avaliacao uuid NOT NULL,
    id_aluno uuid NOT NULL,
    valor numeric(5,2) NOT NULL,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    atualizado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT nota_valor_check CHECK (((valor >= (0)::numeric) AND (valor <= (10)::numeric)))
);

CREATE TABLE public.pagamento (
    id uuid NOT NULL,
    tenant_id uuid NOT NULL,
    id_mensalidade uuid,
    id_parcela uuid,
    valor numeric(10,2) NOT NULL,
    data_pagamento timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    forma_pagamento character varying(50) NOT NULL
);

CREATE TABLE public.parcela (
    id uuid NOT NULL,
    tenant_id uuid NOT NULL,
    id_mensalidade uuid NOT NULL,
    numero integer NOT NULL,
    valor numeric(10,2) NOT NULL,
    data_vencimento date NOT NULL,
    status character varying(50) NOT NULL,
    data_pagamento date,
    valor_pago numeric(10,2),
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE public.professor (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    tenant_id uuid NOT NULL,
    nome character varying(160) NOT NULL,
    cpf character varying(14) NOT NULL,
    email character varying(200),
    telefone character varying(30),
    carga_horaria_contratual integer DEFAULT 20 NOT NULL,
    ativo boolean DEFAULT true NOT NULL,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    atualizado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    sexo character varying(20),
    cor_raca character varying(50),
    nacionalidade character varying(50),
    uf_nascimento character varying(2),
    municipio_nascimento character varying(100),
    cep character varying(20),
    endereco character varying(255),
    numero character varying(20),
    complemento character varying(100),
    bairro character varying(100),
    cidade character varying(100),
    estado character varying(2),
    zona character varying(20),
    escolaridade character varying(50),
    curso_superior_nome character varying(255),
    curso_superior_area character varying(100),
    curso_superior_ano integer,
    curso_superior_instituicao character varying(255),
    pos_graduacao character varying(50),
    funcao_exercida character varying(50),
    tipo_vinculo character varying(50)
);

CREATE TABLE public.serie (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    tenant_id uuid NOT NULL,
    id_ano_letivo uuid NOT NULL,
    nome character varying(160) NOT NULL,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    atualizado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    etapa_ensino character varying(40)
);

CREATE TABLE public.serie_disciplina (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    tenant_id uuid NOT NULL,
    id_serie uuid NOT NULL,
    id_disciplina uuid NOT NULL,
    carga_horaria_anual integer NOT NULL,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    atualizado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE public.turma (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    tenant_id uuid NOT NULL,
    id_ano_letivo uuid NOT NULL,
    id_serie uuid NOT NULL,
    nome character varying(160) NOT NULL,
    turno character varying(30) NOT NULL,
    sala character varying(60),
    capacidade integer DEFAULT 35 NOT NULL,
    encerrada boolean DEFAULT false NOT NULL,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    atualizado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    situacao character varying(20) DEFAULT 'ATIVA'::character varying NOT NULL,
    tipo_mediador character varying(50),
    horario_inicio time without time zone,
    horario_termino time without time zone,
    dias_semana character varying(255),
    carga_horaria integer,
    tipo_atendimento character varying(50),
    modalidade_ensino character varying(50),
    etapa_ensino character varying(100),
    forma_organizacao character varying(50),
    hora_inicio time without time zone,
    hora_termino time without time zone,
    carga_horaria_semanal integer
);

CREATE TABLE public.turma_disciplina_professor (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    tenant_id uuid NOT NULL,
    id_turma uuid NOT NULL,
    id_disciplina uuid NOT NULL,
    id_professor uuid NOT NULL,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    atualizado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE public.usuario (
    id uuid DEFAULT gen_random_uuid() NOT NULL,
    tenant_id uuid,
    nome_completo character varying(255) NOT NULL,
    cpf character varying(14) NOT NULL,
    senha_hash character varying(255) NOT NULL,
    perfil character varying(50) NOT NULL,
    ativo boolean DEFAULT true,
    tentativas_login integer DEFAULT 0,
    criado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    atualizado_em timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    escola_id uuid,
    email character varying(150),
    telefone character varying(20),
    bloqueado boolean DEFAULT false,
    ultimo_login timestamp without time zone,
    reset_password_token character varying(255),
    reset_password_expires_at timestamp without time zone
);

ALTER TABLE ONLY public.aluno
    ADD CONSTRAINT aluno_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.ano_letivo
    ADD CONSTRAINT ano_letivo_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.avaliacao
    ADD CONSTRAINT avaliacao_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.desconto
    ADD CONSTRAINT desconto_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.disciplina
    ADD CONSTRAINT disciplina_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.documento_aluno
    ADD CONSTRAINT documento_aluno_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.escola
    ADD CONSTRAINT escola_cnpj_key UNIQUE (cnpj);

ALTER TABLE ONLY public.escola
    ADD CONSTRAINT escola_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.frequencia
    ADD CONSTRAINT frequencia_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.historico_situacao_aluno
    ADD CONSTRAINT historico_situacao_aluno_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.matricula
    ADD CONSTRAINT matricula_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.mensalidade
    ADD CONSTRAINT mensalidade_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.nota
    ADD CONSTRAINT nota_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.pagamento
    ADD CONSTRAINT pagamento_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.parcela
    ADD CONSTRAINT parcela_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.professor
    ADD CONSTRAINT professor_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.serie_disciplina
    ADD CONSTRAINT serie_disciplina_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.serie
    ADD CONSTRAINT serie_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.turma_disciplina_professor
    ADD CONSTRAINT turma_disciplina_professor_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.turma
    ADD CONSTRAINT turma_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_cpf_key UNIQUE (cpf);

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_email_key UNIQUE (email);

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_pkey PRIMARY KEY (id);

CREATE INDEX idx_aluno_tenant ON public.aluno USING btree (tenant_id);

CREATE INDEX idx_avaliacao_tenant ON public.avaliacao USING btree (tenant_id);

CREATE INDEX idx_disciplina_tenant ON public.disciplina USING btree (tenant_id);

CREATE INDEX idx_documento_aluno_tenant ON public.documento_aluno USING btree (tenant_id, id_aluno);

CREATE UNIQUE INDEX idx_escola_cnpj_unique ON public.escola USING btree (cnpj) WHERE (cnpj IS NOT NULL);

CREATE INDEX idx_escola_tenant_id ON public.escola USING btree (tenant_id);

CREATE INDEX idx_frequencia_tenant ON public.frequencia USING btree (tenant_id);

CREATE INDEX idx_hist_sit_aluno_tenant ON public.historico_situacao_aluno USING btree (tenant_id, id_aluno);

CREATE INDEX idx_matricula_tenant ON public.matricula USING btree (tenant_id);

CREATE INDEX idx_nota_tenant ON public.nota USING btree (tenant_id);

CREATE INDEX idx_professor_tenant ON public.professor USING btree (tenant_id);

CREATE INDEX idx_serie_disciplina_tenant ON public.serie_disciplina USING btree (tenant_id);

CREATE INDEX idx_serie_tenant ON public.serie USING btree (tenant_id);

CREATE INDEX idx_tdp_tenant ON public.turma_disciplina_professor USING btree (tenant_id);

CREATE INDEX idx_turma_tenant ON public.turma USING btree (tenant_id);

CREATE INDEX idx_usuario_cpf ON public.usuario USING btree (cpf);

CREATE INDEX idx_usuario_tenant_id ON public.usuario USING btree (tenant_id);

CREATE UNIQUE INDEX uk_tdp_turma_disciplina_prof ON public.turma_disciplina_professor USING btree (id_turma, id_disciplina, id_professor);

CREATE UNIQUE INDEX uq_aluno_cpf_tenant ON public.aluno USING btree (tenant_id, cpf);

CREATE UNIQUE INDEX uq_ano_letivo_ano_tenant ON public.ano_letivo USING btree (tenant_id, ano);

CREATE UNIQUE INDEX uq_ano_letivo_ativo_tenant ON public.ano_letivo USING btree ((
CASE
    WHEN ativo THEN tenant_id
    ELSE NULL::uuid
END));

CREATE UNIQUE INDEX uq_disciplina_nome_tenant ON public.disciplina USING btree (tenant_id, nome);

CREATE UNIQUE INDEX uq_freq_aluno_dia_disciplina ON public.frequencia USING btree (tenant_id, id_aluno, data, id_disciplina, id_turma);

CREATE UNIQUE INDEX uq_matricula_aluno_turma ON public.matricula USING btree (tenant_id, id_aluno, id_turma);

CREATE UNIQUE INDEX uq_nota_avaliacao_aluno ON public.nota USING btree (tenant_id, id_avaliacao, id_aluno);

CREATE UNIQUE INDEX uq_professor_cpf_tenant ON public.professor USING btree (tenant_id, cpf);

CREATE UNIQUE INDEX uq_serie_disciplina ON public.serie_disciplina USING btree (tenant_id, id_serie, id_disciplina);

CREATE UNIQUE INDEX uq_serie_nome_ano_tenant ON public.serie USING btree (tenant_id, id_ano_letivo, nome);

CREATE UNIQUE INDEX uq_tdp ON public.turma_disciplina_professor USING btree (tenant_id, id_turma, id_disciplina);

CREATE UNIQUE INDEX uq_turma_nome_ano_tenant ON public.turma USING btree (tenant_id, id_ano_letivo, nome);

ALTER TABLE ONLY public.desconto
    ADD CONSTRAINT desconto_id_aluno_fkey FOREIGN KEY (id_aluno) REFERENCES public.aluno(id);

ALTER TABLE ONLY public.desconto
    ADD CONSTRAINT desconto_id_mensalidade_fkey FOREIGN KEY (id_mensalidade) REFERENCES public.mensalidade(id);

ALTER TABLE ONLY public.avaliacao
    ADD CONSTRAINT fk_av_disciplina FOREIGN KEY (id_disciplina) REFERENCES public.disciplina(id);

ALTER TABLE ONLY public.avaliacao
    ADD CONSTRAINT fk_av_turma FOREIGN KEY (id_turma) REFERENCES public.turma(id);

ALTER TABLE ONLY public.documento_aluno
    ADD CONSTRAINT fk_doc_aluno FOREIGN KEY (id_aluno) REFERENCES public.aluno(id);

ALTER TABLE ONLY public.matricula
    ADD CONSTRAINT fk_matricula_aluno FOREIGN KEY (id_aluno) REFERENCES public.aluno(id);

ALTER TABLE ONLY public.matricula
    ADD CONSTRAINT fk_matricula_turma FOREIGN KEY (id_turma) REFERENCES public.turma(id);

ALTER TABLE ONLY public.nota
    ADD CONSTRAINT fk_nota_aluno FOREIGN KEY (id_aluno) REFERENCES public.aluno(id);

ALTER TABLE ONLY public.nota
    ADD CONSTRAINT fk_nota_avaliacao FOREIGN KEY (id_avaliacao) REFERENCES public.avaliacao(id);

ALTER TABLE ONLY public.serie_disciplina
    ADD CONSTRAINT fk_sd_disciplina FOREIGN KEY (id_disciplina) REFERENCES public.disciplina(id);

ALTER TABLE ONLY public.serie_disciplina
    ADD CONSTRAINT fk_sd_serie FOREIGN KEY (id_serie) REFERENCES public.serie(id);

ALTER TABLE ONLY public.serie
    ADD CONSTRAINT fk_serie_ano FOREIGN KEY (id_ano_letivo) REFERENCES public.ano_letivo(id);

ALTER TABLE ONLY public.turma_disciplina_professor
    ADD CONSTRAINT fk_tdp_disciplina FOREIGN KEY (id_disciplina) REFERENCES public.disciplina(id);

ALTER TABLE ONLY public.turma_disciplina_professor
    ADD CONSTRAINT fk_tdp_professor FOREIGN KEY (id_professor) REFERENCES public.professor(id);

ALTER TABLE ONLY public.turma_disciplina_professor
    ADD CONSTRAINT fk_tdp_turma FOREIGN KEY (id_turma) REFERENCES public.turma(id);

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT fk_tenant FOREIGN KEY (tenant_id) REFERENCES public.escola(id);

ALTER TABLE ONLY public.turma
    ADD CONSTRAINT fk_turma_ano FOREIGN KEY (id_ano_letivo) REFERENCES public.ano_letivo(id);

ALTER TABLE ONLY public.turma
    ADD CONSTRAINT fk_turma_serie FOREIGN KEY (id_serie) REFERENCES public.serie(id);

ALTER TABLE ONLY public.mensalidade
    ADD CONSTRAINT mensalidade_id_aluno_fkey FOREIGN KEY (id_aluno) REFERENCES public.aluno(id);

ALTER TABLE ONLY public.pagamento
    ADD CONSTRAINT pagamento_id_mensalidade_fkey FOREIGN KEY (id_mensalidade) REFERENCES public.mensalidade(id);

ALTER TABLE ONLY public.pagamento
    ADD CONSTRAINT pagamento_id_parcela_fkey FOREIGN KEY (id_parcela) REFERENCES public.parcela(id);

ALTER TABLE ONLY public.parcela
    ADD CONSTRAINT parcela_id_mensalidade_fkey FOREIGN KEY (id_mensalidade) REFERENCES public.mensalidade(id);