CREATE TABLE public.auditoria (
    id uuid NOT NULL,
    executor_id uuid,
    executor_perfil character varying(50),
    tenant_id uuid,
    acao character varying(100) NOT NULL,
    entidade character varying(100) NOT NULL,
    entidade_id uuid,
    detalhes text,
    criado_em timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT auditoria_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_auditoria_criado_em ON public.auditoria (criado_em DESC);
CREATE INDEX idx_auditoria_executor_id ON public.auditoria (executor_id);
CREATE INDEX idx_auditoria_entidade ON public.auditoria (entidade, entidade_id);
