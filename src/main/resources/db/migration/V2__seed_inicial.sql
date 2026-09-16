-- V2: Dados de semente — escola padrão + super admin
--
-- Separado do V1 (schema) por boa prática: DDL e DML em arquivos
-- diferentes facilitam re-rodar/inspecionar cada um isoladamente.

INSERT INTO public.escola (id, nome, cnpj)
VALUES ('e173de18-874d-4737-803d-e44cd887db8e', 'Escola Padrão', '00.000.000/0001-00');

-- Senha do super admin: hash BCrypt já gerado (não é a senha em texto puro).
-- Login: admin@kutuar.com.br — troque a senha assim que logar pela primeira vez.
INSERT INTO public.usuario (id, nome_completo, cpf, senha_hash, perfil, ativo, email)
VALUES (
    '95103b03-3bc7-448e-ae45-ad173059c344',
    'Administrador Master',
    '000.000.000-00',
    '$2a$10$Z5KtQWCADHNGGuxE2ZJI/uUFVtuERtVcfSIqOtz9br5EE2wjzTEFe',
    'SUPER_ADMIN',
    true,
    'admin@kutuar.com.br'
);