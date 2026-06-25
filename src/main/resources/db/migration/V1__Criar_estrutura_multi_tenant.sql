-- V1__Criar_estrutura_multi_tenant.sql

CREATE TABLE IF NOT EXISTS escolas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(255) NOT NULL,
    cnpj VARCHAR(18) UNIQUE NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    escola_id UUID NOT NULL,
    cpf VARCHAR(14) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    role VARCHAR(50) NOT NULL, -- Ex: ADMIN, PROFESSOR, ALUNO, FINANCEIRO
    ativo BOOLEAN DEFAULT TRUE,
    data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (escola_id) REFERENCES escolas(id)
);

CREATE TABLE IF NOT EXISTS ano_letivo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    escola_id UUID NOT NULL,
    ano INTEGER NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    UNIQUE (escola_id, ano),
    FOREIGN KEY (escola_id) REFERENCES escolas(id)
);

-- Inserção de dados iniciais para teste (opcional, pode ser removido em produção)
INSERT INTO escolas (nome, cnpj) VALUES ('Escola Modelo SYNGE', '00.000.000/0001-00');
INSERT INTO usuarios (escola_id, cpf, senha, nome, email, role)
SELECT id, '111.111.111-11', '$2a$10$2B.g.Z.e.Q.W.x.Y.Z.a.b.c.d.e.f.g.h.i.j.k.l.m.n.o.p.q.r.s.t.u.v.w.x.y.z.A.B.C.D.E.F.G.H.I.J.K.L.M.N.O.P.Q.R.S.T.U.V.W.X.Y.Z.0.1.2.3.4.5.6.7.8.9.!', 'Admin SYNGE', 'admin@synge.com', 'ADMIN' FROM escolas WHERE cnpj = '00.000.000/0001-00';
INSERT INTO ano_letivo (escola_id, ano, data_inicio, data_fim)
SELECT id, 2024, '2024-02-01', '2024-12-15' FROM escolas WHERE cnpj = '00.000.000/0001-00';
