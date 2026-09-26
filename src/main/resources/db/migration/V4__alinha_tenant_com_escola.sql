-- Uma escola possui um ID próprio e um ID de tenant distinto. Todos os dados
-- isolados por tenant passam a referenciar escola.tenant_id, não escola.id.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM escola
        WHERE tenant_id IS NULL
        GROUP BY tenant_id
    ) THEN
        RAISE EXCEPTION 'Existem escolas sem tenant_id; corrija os dados antes de alinhar os tenants.';
    END IF;

    IF EXISTS (
        SELECT 1 FROM escola
        GROUP BY tenant_id
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Existem tenant_id duplicados em escola; corrija os dados antes de alinhar os tenants.';
    END IF;
END $$;

UPDATE aluno a SET tenant_id = e.tenant_id FROM escola e WHERE a.tenant_id = e.id;
UPDATE ano_letivo a SET tenant_id = e.tenant_id FROM escola e WHERE a.tenant_id = e.id;
UPDATE avaliacao a SET tenant_id = e.tenant_id FROM escola e WHERE a.tenant_id = e.id;
UPDATE desconto d SET tenant_id = e.tenant_id FROM escola e WHERE d.tenant_id = e.id;
UPDATE disciplina d SET tenant_id = e.tenant_id FROM escola e WHERE d.tenant_id = e.id;
UPDATE documento_aluno d SET tenant_id = e.tenant_id FROM escola e WHERE d.tenant_id = e.id;
UPDATE frequencia f SET tenant_id = e.tenant_id FROM escola e WHERE f.tenant_id = e.id;
UPDATE historico_situacao_aluno h SET tenant_id = e.tenant_id FROM escola e WHERE h.tenant_id = e.id;
UPDATE matricula m SET tenant_id = e.tenant_id FROM escola e WHERE m.tenant_id = e.id;
UPDATE mensalidade m SET tenant_id = e.tenant_id FROM escola e WHERE m.tenant_id = e.id;
UPDATE nota n SET tenant_id = e.tenant_id FROM escola e WHERE n.tenant_id = e.id;
UPDATE pagamento p SET tenant_id = e.tenant_id FROM escola e WHERE p.tenant_id = e.id;
UPDATE parcela p SET tenant_id = e.tenant_id FROM escola e WHERE p.tenant_id = e.id;
UPDATE professor p SET tenant_id = e.tenant_id FROM escola e WHERE p.tenant_id = e.id;
UPDATE serie s SET tenant_id = e.tenant_id FROM escola e WHERE s.tenant_id = e.id;
UPDATE serie_disciplina sd SET tenant_id = e.tenant_id FROM escola e WHERE sd.tenant_id = e.id;
UPDATE turma t SET tenant_id = e.tenant_id FROM escola e WHERE t.tenant_id = e.id;
UPDATE turma_disciplina_professor tdp SET tenant_id = e.tenant_id FROM escola e WHERE tdp.tenant_id = e.id;
UPDATE usuario u SET tenant_id = e.tenant_id FROM escola e WHERE u.tenant_id = e.id;

ALTER TABLE escola ADD CONSTRAINT uk_escola_tenant_id UNIQUE (tenant_id);
ALTER TABLE usuario DROP CONSTRAINT IF EXISTS fk_tenant;
ALTER TABLE usuario ADD CONSTRAINT fk_usuario_tenant
    FOREIGN KEY (tenant_id) REFERENCES escola(tenant_id);
