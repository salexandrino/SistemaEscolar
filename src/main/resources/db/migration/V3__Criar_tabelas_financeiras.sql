-- V3__Criar_tabelas_financeiras.sql

CREATE TABLE IF NOT EXISTS mensalidades (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    escola_id UUID NOT NULL,
    aluno_id UUID NOT NULL,
    ano_letivo_id UUID NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    data_vencimento DATE NOT NULL,
    data_pagamento DATE,
    status VARCHAR(50) NOT NULL, -- Ex: PENDENTE, PAGO, EM_ATRASO, CANCELADO
    desconto DECIMAL(5,2) DEFAULT 0.00,
    FOREIGN KEY (escola_id) REFERENCES escolas(id),
    FOREIGN KEY (aluno_id) REFERENCES alunos(id),
    FOREIGN KEY (ano_letivo_id) REFERENCES ano_letivo(id)
);

CREATE TABLE IF NOT EXISTS taxas_avulsas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    escola_id UUID NOT NULL,
    aluno_id UUID, -- Pode ser nulo se for uma taxa geral
    descricao VARCHAR(255) NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    data_lancamento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_vencimento DATE,
    data_pagamento DATE,
    status VARCHAR(50) NOT NULL, -- Ex: PENDENTE, PAGO, CANCELADO
    numero_parcelas INTEGER DEFAULT 1,
    FOREIGN KEY (escola_id) REFERENCES escolas(id),
    FOREIGN KEY (aluno_id) REFERENCES alunos(id)
);

CREATE TABLE IF NOT EXISTS fluxo_caixa (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    escola_id UUID NOT NULL,
    descricao VARCHAR(255) NOT NULL,
    tipo VARCHAR(50) NOT NULL, -- Ex: ENTRADA, SAIDA
    valor DECIMAL(10,2) NOT NULL,
    data_movimentacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (escola_id) REFERENCES escolas(id)
);
