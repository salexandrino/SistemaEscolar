-- Corrige o hash de senha do Super Admin.
-- As migrations V10 e V11 haviam gravado um hash inválido/placeholder
-- (não correspondia a nenhuma senha real), o que impedia o login.
-- Este script substitui pelo hash correto, gerado via PasswordService (jBCrypt),
-- referente à senha "SuperAdmin@123".

UPDATE usuario
SET senha_hash = '$2a$10$1jxglOT6XQDLZsZqgavVjuqwJtNSwLn3yJrTt.r/ktKBHdfl6xMoe'
WHERE email = 'admin@kutuar.com.br';