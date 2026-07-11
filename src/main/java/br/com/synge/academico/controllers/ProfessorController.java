package br.com.synge.academico.controllers;

import br.com.synge.academico.dtos.AtualizarProfessorDTO;
import br.com.synge.academico.dtos.CriarProfessorDTO;
import br.com.synge.academico.services.ProfessorService;
import io.javalin.http.Context;
import org.eclipse.jetty.http.HttpStatus;

import java.util.UUID;

public class ProfessorController {

    private final ProfessorService service;

    public ProfessorController(ProfessorService service) {
        this.service = service;
    }

    public void criar(Context ctx) {
        CriarProfessorDTO dto = ctx.bodyAsClass(CriarProfessorDTO.class);
        ctx.status(HttpStatus.CREATED_201).json(service.criar(dto));
    }

    public void atualizar(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        AtualizarProfessorDTO dto = ctx.bodyAsClass(AtualizarProfessorDTO.class);
        ctx.json(service.atualizar(id, dto));
    }

    public void inativar(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        service.inativar(id);
        ctx.status(HttpStatus.NO_CONTENT_204);
    }

    public void obterPorId(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        ctx.json(service.obterPorId(id));
    }

    public void listar(Context ctx) {
        ctx.json(service.listar());
    }

    public void consultarGrade(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        ctx.json(service.consultarGrade(id));
    }
}
