package br.com.synge.academico.controllers;

import br.com.synge.academico.dtos.CriarTurmaDTO;
import br.com.synge.academico.dtos.EditarTurmaDTO;
import br.com.synge.academico.dtos.TurmaResponseDTO;
import br.com.synge.academico.services.TurmaService;
import br.com.synge.seguranca.exceptions.ConflictException;
import br.com.synge.seguranca.exceptions.NotFoundException;
import br.com.synge.seguranca.exceptions.ValidationException;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TurmaController {

    private final TurmaService service;

    public TurmaController(TurmaService service) {
        this.service = service;
    }

    public void listar(Context ctx) {
        UUID idAnoLetivo = null;
        UUID idSerie = null;

        try {
            String pAno = ctx.queryParam("idAnoLetivo");
            String pSerie = ctx.queryParam("idSerie");

            if (pAno != null && !pAno.isBlank()) idAnoLetivo = UUID.fromString(pAno);
            if (pSerie != null && !pSerie.isBlank()) idSerie = UUID.fromString(pSerie);

            List<TurmaResponseDTO> lista = service.listar(idAnoLetivo, idSerie);
            ctx.json(lista);
        } catch (IllegalArgumentException e) {
            // UUID#fromString may throw IllegalArgumentException
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("message", "Parâmetro idAnoLetivo ou idSerie inválido."));
        } catch (Exception e) {
            // Protege contra exceções que podem causar retorno HTML/redirect
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("message", "Erro interno ao listar turmas."));
        }
    }

    public void criar(Context ctx) {
        try {
            CriarTurmaDTO dto = ctx.bodyValidator(CriarTurmaDTO.class)
                    .check(d -> d.getIdAnoLetivo() != null, "idAnoLetivo é obrigatório")
                    .check(d -> d.getIdSerie() != null, "idSerie é obrigatório")
                    .check(d -> d.getNome() != null && !d.getNome().isBlank(), "nome da turma é obrigatório")
                    .check(d -> d.getTurno() != null && !d.getTurno().isBlank(), "turno é obrigatório")
                    .check(d -> d.getCapacidade() != null && d.getCapacidade() >= 0, "capacidade deve ser >= 0")
                    .get();
            TurmaResponseDTO resp = service.criar(dto);
            ctx.status(201).json(resp);
        } catch (ValidationException | ConflictException | NotFoundException e) {
            ctx.status(e.getStatus()).json(Map.of("message", e.getMessage()));
        }
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

    public void buscarPorId(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        TurmaResponseDTO turma = service.buscarPorId(id);
        ctx.json(turma);
    }

    public void editar(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        EditarTurmaDTO dto = ctx.bodyAsClass(EditarTurmaDTO.class);
        service.editar(id, dto);
        ctx.status(HttpStatus.NO_CONTENT);
    }

    public void apagar(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        service.apagar(id);
        ctx.status(HttpStatus.NO_CONTENT);
    }
}
