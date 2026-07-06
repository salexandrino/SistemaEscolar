INSERT INTO usuario (
    id,
    tenant_id,
    escola_id,
    nome_completo,
    cpf,
    email,
    telefone,
    senha_hash,
    perfil,
    ativo,
    tentativas_login,
    bloqueado,
    ultimo_login,
    criado_em,
    atualizado_em
)
SELECT
    gen_random_uuid(),
    NULL,
    NULL,
    'Administrador Master',
    '000.000.000-00',
    'synge.gestao@gmail.com',
    '(00)00000-0000',
    '$2a$10$msYsuhuC7CWfiggSVMwdMugr0DL.sgwHC8WrDiD4zLh/RdA3cqj3y',
    'SUPER_ADMIN',
    TRUE,
    0,
    FALSE,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    WHERE NOT EXISTS (
    SELECT 1
    FROM usuario
    WHERE email = 'synge.gestao@gmail.com'
);