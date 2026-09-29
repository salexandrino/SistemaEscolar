package br.com.kutuar.academico.controllers;

import br.com.kutuar.academico.dtos.AtualizarDisciplinaDTO;
import br.com.kutuar.academico.dtos.CriarDisciplinaDTO;
import br.com.kutuar.academico.models.Disciplina;
import br.com.kutuar.academico.services.DisciplinaService;
import br.com.kutuar.seguranca.services.TenantAccessGuard;
import io.javalin.http.Context;

import java.util.UUID;

public class DisciplinaController {

    private final DisciplinaService service;

    public DisciplinaController(DisciplinaService service) {
        this.service = service;
    }

    public void listar(Context ctx) {
        ctx.json(service.listar(TenantAccessGuard.currentTenant()));
    }

    public void obter(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        ctx.json(service.obter(TenantAccessGuard.currentTenant(), id));
    }

    public void criar(Context ctx) {
        CriarDisciplinaDTO dto = ctx.bodyAsClass(CriarDisciplinaDTO.class);
        Disciplina criada = service.criar(TenantAccessGuard.currentTenant(), dto);
        ctx.status(201).json(criada);
    }

    public void atualizar(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        AtualizarDisciplinaDTO dto = ctx.bodyAsClass(AtualizarDisciplinaDTO.class);
        Disciplina atualizada = service.atualizar(TenantAccessGuard.currentTenant(), id, dto);
        ctx.json(atualizada);
    }

    public void remover(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        service.remover(TenantAccessGuard.currentTenant(), id);
        ctx.status(204);
    }
}
