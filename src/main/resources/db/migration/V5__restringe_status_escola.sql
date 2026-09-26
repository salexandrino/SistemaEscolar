-- O bloqueio administrativo de uma escola é representado por INATIVA.
-- Não existe um terceiro estado BLOQUEADA neste domínio.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM escola
        WHERE status IS NULL OR status NOT IN ('ATIVA', 'INATIVA')
    ) THEN
        RAISE EXCEPTION 'Existem escolas com status inválido. Corrija os dados antes de aplicar a restrição de status.';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_escola_status'
          AND conrelid = 'escola'::regclass
    ) THEN
        ALTER TABLE escola
            ADD CONSTRAINT ck_escola_status CHECK (status IN ('ATIVA', 'INATIVA'));
    END IF;
END $$;
