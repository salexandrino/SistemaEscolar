-- CNPJ é armazenado sempre com 14 dígitos. Não escolhe registros em caso de colisão.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM escola
        WHERE cnpj IS NOT NULL
        GROUP BY regexp_replace(cnpj, '[^0-9]', '', 'g')
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Não foi possível normalizar CNPJs: existem CNPJs duplicados após remover pontuação. Corrija os dados com decisão humana antes de executar esta migration.';
    END IF;
END $$;

UPDATE escola
SET cnpj = regexp_replace(cnpj, '[^0-9]', '', 'g')
WHERE cnpj IS NOT NULL
  AND cnpj IS DISTINCT FROM regexp_replace(cnpj, '[^0-9]', '', 'g');

-- Em instalações antigas pode já existir a constraint equivalente do schema consolidado.
DO $$
DECLARE
    constraint_name text;
BEGIN
    SELECT con.conname INTO constraint_name
    FROM pg_constraint con
    JOIN pg_class rel ON rel.oid = con.conrelid
    JOIN pg_namespace ns ON ns.oid = rel.relnamespace
    WHERE con.contype = 'u'
      AND rel.relname = 'escola'
      AND ns.nspname = current_schema()
      AND con.conkey = ARRAY[(SELECT attnum FROM pg_attribute WHERE attrelid = rel.oid AND attname = 'cnpj' AND NOT attisdropped)]::smallint[];

    IF constraint_name IS NULL THEN
        ALTER TABLE escola ADD CONSTRAINT uk_escola_cnpj UNIQUE (cnpj);
    ELSIF constraint_name <> 'uk_escola_cnpj'
      AND NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_escola_cnpj' AND connamespace = current_schema()::regnamespace) THEN
        EXECUTE format('ALTER TABLE escola RENAME CONSTRAINT %I TO uk_escola_cnpj', constraint_name);
    END IF;
END $$;
