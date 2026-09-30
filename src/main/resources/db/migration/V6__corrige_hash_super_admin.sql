-- Corrige exclusivamente o Super Admin sem tenant criado pela V2 legada.
-- A condição pelo hash antigo evita substituir uma senha alterada posteriormente.
UPDATE public.usuario
SET senha_hash = '$2a$10$uK/1B7RDI5FIU4GNr6pgKujJmvOgnQl2pxezkYmSc4CpLxS0V/XNC',
    atualizado_em = CURRENT_TIMESTAMP
WHERE id = '95103b03-3bc7-448e-ae45-ad173059c344'
  AND email = 'admin@kutuar.com.br'
  AND perfil = 'SUPER_ADMIN'
  AND tenant_id IS NULL
  AND senha_hash = '$2a$10$Z5KtQWCADHNGGuxE2ZJI/uUFVtuERtVcfSIqOtz9br5EE2wjzTEFe';
