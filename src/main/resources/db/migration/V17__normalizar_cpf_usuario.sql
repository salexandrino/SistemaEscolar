-- Antes desta correção, o CPF era gravado exatamente como cada tela enviava:
-- a tela de login sempre manda formatado ("000.000.000-00"), mas o cadastro
-- de escola (onde o Gestor é criado) não tinha máscara e podia gravar sem
-- pontuação. Resultado: WHERE cpf = ? no login não batia com o que estava
-- salvo, mesmo sendo o mesmo CPF ("CPF não cadastrado").
--
-- Usuario.setCpf() e AuthService.autenticar() agora sempre normalizam para
-- só dígitos. Esta migration alinha os dados que já existem no banco com essa
-- nova regra, senão contas criadas antes da correção continuariam quebradas.
--
-- CUIDADO: como cpf tem UNIQUE, pode existir mais de uma linha cujo CPF
-- normalizado colida (ex.: uma linha já tinha "05699123423" e outra tinha
-- "056.991.234-23" — mesmo CPF, duas linhas). Um UPDATE direto quebraria a
-- constraint. Por isso: para cada grupo de CPF duplicado após normalizar,
-- mantemos UMA linha com o CPF limpo e inativamos as demais (sem apagar
-- nada), para revisão manual depois via a tela de usuários.

-- 1) Linha "vencedora" de cada grupo duplicado (a mais antiga por id) recebe o CPF normalizado.
WITH normalizados AS (
    SELECT id, regexp_replace(cpf, '[^0-9]', '', 'g') AS cpf_normalizado
    FROM usuario
    WHERE cpf IS NOT NULL
),
     ranked AS (
         SELECT id, cpf_normalizado,
                ROW_NUMBER() OVER (PARTITION BY cpf_normalizado ORDER BY id) AS posicao
         FROM normalizados
     )
UPDATE usuario u
SET cpf = r.cpf_normalizado,
    atualizado_em = now()
    FROM ranked r
WHERE u.id = r.id
  AND r.posicao = 1
  AND u.cpf IS DISTINCT FROM r.cpf_normalizado;

-- 2) Linhas "perdedoras" (duplicatas do mesmo CPF): inativa para não permitir
-- login por uma conta duplicada/confusa, mas SEM apagar nem tocar no valor do
-- cpf (evita novo choque de unicidade). Fica sinalizado para revisão manual.
WITH normalizados AS (
    SELECT id, regexp_replace(cpf, '[^0-9]', '', 'g') AS cpf_normalizado
    FROM usuario
    WHERE cpf IS NOT NULL
),
     ranked AS (
         SELECT id, cpf_normalizado,
                ROW_NUMBER() OVER (PARTITION BY cpf_normalizado ORDER BY id) AS posicao
         FROM normalizados
     )
UPDATE usuario u
SET ativo = FALSE,
    atualizado_em = now()
    FROM ranked r
WHERE u.id = r.id
  AND r.posicao > 1;