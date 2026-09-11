package br.com.kutuar.academico.controllers;

import br.com.kutuar.academico.dtos.AtualizarSerieDTO;
import br.com.kutuar.academico.dtos.CriarSerieDTO;
import br.com.kutuar.academico.dtos.SerieResponseDTO;
import br.com.kutuar.academico.services.SerieService;
import io.javalin.http.Context;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SerieController {

    private final SerieService service;

    public SerieController(SerieService service) {
        this.service = service;
    }

    public void listarPorAno(Context ctx) {
        UUID idAno = UUID.fromString(ctx.queryParam("idAnoLetivo"));
        List<SerieResponseDTO> lista = service.listarPorAno(idAno);
        ctx.json(lista);
    }

    public void obter(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        ctx.json(service.obter(id));
    }

    public void criar(Context ctx) {
        CriarSerieDTO dto = ctx.bodyAsClass(CriarSerieDTO.class);
        SerieResponseDTO resp = service.criar(dto);
        ctx.status(201).json(resp);
    }

    public void atualizar(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        AtualizarSerieDTO dto = ctx.bodyAsClass(AtualizarSerieDTO.class);
        service.atualizar(id, dto);
        ctx.json(Map.of("message", "Série atualizada com sucesso."));
    }

    public void remover(Context ctx) {
        UUID id = UUID.fromString(ctx.pathParam("id"));
        service.remover(id);
        ctx.status(204);
    }
}
