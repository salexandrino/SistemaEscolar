CREATE TABLE mensalidade (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    id_aluno UUID NOT NULL REFERENCES aluno(id),
    descricao VARCHAR(255) NOT NULL,
    valor_original DECIMAL(10,2) NOT NULL,
    data_vencimento DATE NOT NULL,
    status VARCHAR(50) NOT NULL, -- ABERTA, PAGA, VENCIDA, PARCELADA
    data_pagamento DATE,
    valor_pago DECIMAL(10,2),
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE parcela (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    id_mensalidade UUID NOT NULL REFERENCES mensalidade(id),
    numero INTEGER NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    data_vencimento DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    data_pagamento DATE,
    valor_pago DECIMAL(10,2),
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE desconto (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    id_aluno UUID NOT NULL REFERENCES aluno(id),
    id_mensalidade UUID REFERENCES mensalidade(id),
    tipo VARCHAR(50) NOT NULL, -- IRMAO, FAMILIAR, BOLSA, PERSONALIZADO
    percentual DECIMAL(5,2),
    valor_fixo DECIMAL(10,2),
    descricao TEXT,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE pagamento (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    id_mensalidade UUID REFERENCES mensalidade(id),
    id_parcela UUID REFERENCES parcela(id),
    valor DECIMAL(10,2) NOT NULL,
    data_pagamento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    forma_pagamento VARCHAR(50) NOT NULL
);