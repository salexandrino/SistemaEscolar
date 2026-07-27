package br.com.synge.academico.controllers;

import br.com.synge.academico.dtos.CriarTurmaDTO;
import br.com.synge.academico.dtos.TurmaResponseDTO;
import br.com.synge.academico.services.TurmaService;
import io.javalin.http.Context;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TurmaController {

    private final TurmaService service;

    public TurmaController(TurmaService service) {
        this.service = service;
    }

    public void listar(Context ctx) {
        UUID idAnoLetivo = UUID.fromString(ctx.queryParam("idAnoLetivo"));
        UUID idSerie = UUID.fromString(ctx.queryParam("idSerie"));
        List<TurmaResponseDTO> lista = service.listar(idAnoLetivo, idSerie);
        ctx.json(lista);
    }

    public void criar(Context ctx) {
        CriarTurmaDTO dto = ctx.bodyValidator(CriarTurmaDTO.class)
                .check(d -> d.getIdAnoLetivo() != null, "idAnoLetivo é obrigatório")
                .check(d -> d.getIdSerie() != null, "idSerie é obrigatório")
                .check(d -> d.getNome() != null && !d.getNome().isBlank(), "nome da turma é obrigatório")
                .check(d -> d.getTurno() != null && !d.getTurno().isBlank(), "turno é obrigatório")
                .check(d -> d.getCapacidade() != null && d.getCapacidade() >= 0, "capacidade deve ser >= 0")
                .get();
        TurmaResponseDTO resp = service.criar(dto);
        ctx.status(201).json(resp);
    }

    public void encerrar(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        service.encerrar(id);
        ctx.json(Map.of("message", "Turma encerrada com sucesso."));
    }

    public void capacidade(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        int matriculados = service.consultarCapacidadeMatriculados(id);
        ctx.json(Map.of("idTurma", id, "matriculados", matriculados));
    }
}