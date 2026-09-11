package br.com.kutuar.academico.controllers;

import br.com.kutuar.academico.dtos.AtualizarDisciplinaDTO;
import br.com.kutuar.academico.dtos.CriarDisciplinaDTO;
import br.com.kutuar.academico.models.Disciplina;
import br.com.kutuar.academico.services.DisciplinaService;
import br.com.kutuar.seguranca.exceptions.AuthenticationException;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.utils.AuthUserContext;
import io.javalin.http.Context;
import io.javalin.http.Handler;

import java.util.Map;
import java.util.UUID;

public class DisciplinaController {

    private final DisciplinaService service;

    public DisciplinaController(DisciplinaService service) {
        this.service = service;
    }

    public void listar(Context ctx) {
        UUID tenantId = requireTenant(ctx);
        ctx.json(service.listar(tenantId));
    }

    public void obter(Context ctx) {
        UUID tenantId = requireTenant(ctx);
        UUID id = UUID.fromString(ctx.pathParam("id"));
        ctx.json(service.obter(tenantId, id));
    }

    public void criar(Context ctx) {
        UUID tenantId = requireTenant(ctx);
        CriarDisciplinaDTO dto = ctx.bodyAsClass(CriarDisciplinaDTO.class);
        Disciplina criada = service.criar(tenantId, dto);
        ctx.status(201).json(criada);
    }

    public void atualizar(Context ctx) {
        UUID tenantId = requireTenant(ctx);
        UUID id = UUID.fromString(ctx.pathParam("id"));
        AtualizarDisciplinaDTO dto = ctx.bodyAsClass(AtualizarDisciplinaDTO.class);
        Disciplina atualizada = service.atualizar(tenantId, id, dto);
        ctx.json(atualizada);
    }

    public void remover(Context ctx) {
        UUID tenantId = requireTenant(ctx);
        UUID id = UUID.fromString(ctx.pathParam("id"));
        service.remover(tenantId, id);
        ctx.status(204);
    }

    private UUID requireTenant(Context ctx) {
        AuthUser user = AuthUserContext.getAuthUser();
        if (user == null || user.getTenantId() == null) {
            throw new AuthenticationException("Usuário não autenticado ou sem tenant ativo.");
        }
        return user.getTenantId();
    }
}
