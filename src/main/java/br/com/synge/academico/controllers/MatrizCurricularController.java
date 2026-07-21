package br.com.synge.academico.controllers;

import br.com.synge.academico.dtos.SerieDisciplinaDTO;
import br.com.synge.academico.models.SerieDisciplina;
import br.com.synge.academico.services.MatrizCurricularService;
import io.javalin.http.Context;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MatrizCurricularController {

    private final MatrizCurricularService service;

    public MatrizCurricularController(MatrizCurricularService service) {
        this.service = service;
    }

    public void listar(Context ctx) {
        UUID idSerie = UUID.fromString(ctx.pathParam("idSerie"));
        List<SerieDisciplina> lista = service.listar(idSerie);
        ctx.json(lista);
    }

    public void definir(Context ctx) {
        UUID idSerie = UUID.fromString(ctx.pathParam("idSerie"));
        SerieDisciplinaDTO dto = ctx.bodyAsClass(SerieDisciplinaDTO.class);
        SerieDisciplina sd = service.definir(idSerie, dto);
        ctx.status(201).json(sd);
    }

    public void remover(Context ctx) {
        UUID idSerie = UUID.fromString(ctx.pathParam("idSerie"));
        UUID idDisciplina = UUID.fromString(ctx.pathParam("idDisciplina"));
        service.remover(idSerie, idDisciplina);
        ctx.json(Map.of("message", "Disciplina removida da matriz."));
    }
}
