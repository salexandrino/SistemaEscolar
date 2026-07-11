package br.com.synge.academico.controllers;

import br.com.synge.academico.dtos.AnoLetivoResponseDTO;
import br.com.synge.academico.dtos.CriarAnoLetivoDTO;
import br.com.synge.academico.services.AnoLetivoService;
import io.javalin.http.Context;

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
        int anoAtual = ctx.queryParamAsClass("anoAtual", Integer.class).getOrDefault(LocalDate.now().getYear());
        ctx.json(service.historicoAnosAnteriores(anoAtual));
    }

    public void clonar(Context ctx) {
        UUID idOrigem = UUID.fromString(ctx.pathParam("id"));
        UUID idDestino = UUID.fromString(ctx.pathParam("destinoId"));
        service.clonarConfiguracoes(idOrigem, idDestino);
        ctx.status(HttpStatus.NO_CONTENT_204);
    }
}
