package br.com.kutuar.academico.controllers;

import br.com.kutuar.academico.dtos.CriarAvaliacaoDTO;
import br.com.kutuar.academico.services.AvaliacaoService;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.utils.AuthUserContext;
import br.com.kutuar.seguranca.exceptions.ValidationException;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.util.UUID;

public class AvaliacaoController {
    private final AvaliacaoService service;

    public AvaliacaoController(AvaliacaoService service) { this.service = service; }

    private UUID getTenant() {
        AuthUser u = AuthUserContext.getAuthUser();
        if (u == null || u.getTenantId() == null) throw new ValidationException("Não autorizado.");
        return u.getTenantId();
    }

    public void criar(Context ctx) {
        CriarAvaliacaoDTO dto = ctx.bodyAsClass(CriarAvaliacaoDTO.class);
        ctx.status(HttpStatus.CREATED).json(service.criar(getTenant(), dto));
    }

    public void listar(Context ctx) {
        String tParam = ctx.queryParam("idTurma");
        String dParam = ctx.queryParam("idDisciplina");
        if (tParam == null || dParam == null) throw new ValidationException("Parâmetros idTurma e idDisciplina obrigatórios.");
        ctx.json(service.listar(getTenant(), UUID.fromString(tParam), UUID.fromString(dParam)));
    }

    public void obterPorId(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        ctx.json(service.obterPorId(getTenant(), id));
    }

    public void atualizar(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        CriarAvaliacaoDTO dto = ctx.bodyAsClass(CriarAvaliacaoDTO.class);
        service.atualizar(getTenant(), id, dto);
        ctx.status(HttpStatus.NO_CONTENT);
    }

    public void remover(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        service.remover(getTenant(), id);
        ctx.status(HttpStatus.NO_CONTENT);
    }
}