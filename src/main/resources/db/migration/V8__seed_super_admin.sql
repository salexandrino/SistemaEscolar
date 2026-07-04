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
    aprovado,
    tentativas_login,
    bloqueado,
    bloqueado_ate,
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
    '$2a$10$aFKSFPa9iSwzo1uxS/zHWOrjaay/9htx9RLxRhNwUVU4JKJilpagy',
    'SUPER_ADMIN',
    TRUE,
    TRUE,
    0,
    FALSE,
    NULL,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
    WHERE NOT EXISTS (
    SELECT 1
    FROM usuario
    WHERE email = 'synge.gestao@gmail.com'
);