package br.com.kutuar.academico.controllers;

import br.com.kutuar.academico.dtos.AtribuirDocenteDTO;
import br.com.kutuar.academico.services.AlocacaoDocenteService;
import io.javalin.http.Context;

import java.util.Map;
import java.util.UUID;

public class AlocacaoDocenteController {

    private final AlocacaoDocenteService service;

    public AlocacaoDocenteController(AlocacaoDocenteService service) {
        this.service = service;
    }

    public void atribuir(Context ctx) {
        UUID idTurma = UUID.fromString(ctx.pathParam("idTurma"));
        AtribuirDocenteDTO dto = ctx.bodyValidator(AtribuirDocenteDTO.class)
                .check(d -> d.getIdDisciplina() != null, "idDisciplina é obrigatório")
                .check(d -> d.getIdProfessor() != null, "idProfessor é obrigatório")
                .get();
        service.atribuir(idTurma, dto);
        ctx.status(201).json(Map.of("message", "Docente atribuído com sucesso."));
    }

    public void remover(Context ctx) {
        UUID idTurma = UUID.fromString(ctx.pathParam("idTurma"));
        UUID idDisciplina = UUID.fromString(ctx.pathParam("idDisciplina"));
        UUID idProfessor = UUID.fromString(ctx.pathParam("idProfessor"));
        service.remover(idTurma, idDisciplina, idProfessor);
        ctx.json(Map.of("message", "Atribuição removida com sucesso."));
    }
}
