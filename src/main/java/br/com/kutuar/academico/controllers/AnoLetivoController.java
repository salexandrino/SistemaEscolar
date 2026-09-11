package br.com.kutuar.academico.controllers;

import br.com.kutuar.academico.dtos.AnoLetivoResponseDTO;
import br.com.kutuar.academico.dtos.CriarAnoLetivoDTO;
import br.com.kutuar.academico.dtos.EditarAnoLetivoDTO;
import br.com.kutuar.academico.services.AnoLetivoService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AnoLetivoController {

    private final AnoLetivoService service;

    public AnoLetivoController(AnoLetivoService service) {
        this.service = service;
    }

    public void listar(Context ctx) {
        List<AnoLetivoResponseDTO> lista = service.listar();
        ctx.json(lista);
    }

    public void criar(Context ctx) {
        CriarAnoLetivoDTO dto = ctx.bodyValidator(CriarAnoLetivoDTO.class)
                .check(d -> d.getAno() != null, "Ano é obrigatório.")
                .get();
        AnoLetivoResponseDTO resp = service.criar(dto);
        ctx.status(201).json(resp);
    }

    public void arquivar(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        service.arquivar(id);
        ctx.json(Map.of("message", "Ano letivo arquivado com sucesso."));
    }

    public void definirAtivo(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        service.definirAtivo(id);
        ctx.json(Map.of("message", "Ano letivo definido como ativo."));
    }

    public void historico(Context ctx) {
        ctx.json(service.historicoAnosAnteriores());
    }

    public void clonar(Context ctx) {
        UUID idOrigem = UUID.fromString(ctx.pathParam("id"));
        UUID idDestino = UUID.fromString(ctx.pathParam("destinoId"));
        service.clonarConfiguracoes(idOrigem, idDestino);
        ctx.status(HttpStatus.NO_CONTENT);
    }

    public void editar(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        EditarAnoLetivoDTO dto = ctx.bodyAsClass(EditarAnoLetivoDTO.class);
        service.editar(id, dto);
        ctx.status(HttpStatus.NO_CONTENT);
    }

    public void apagar(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        service.apagar(id);
        ctx.status(HttpStatus.NO_CONTENT);
    }
}