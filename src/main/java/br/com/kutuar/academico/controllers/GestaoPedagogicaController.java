package br.com.kutuar.academico.controllers;

import br.com.kutuar.academico.dtos.LancamentoNotaDTO;
import br.com.kutuar.academico.dtos.SimulacaoNotaDTO;
import br.com.kutuar.academico.services.BoletimService;
import br.com.kutuar.academico.services.LancamentoNotasService;
import br.com.kutuar.seguranca.utils.AuthUserContext;
import io.javalin.http.Context;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public class GestaoPedagogicaController {

    private final LancamentoNotasService lancamentoNotasService;
    private final BoletimService boletimService;

    public GestaoPedagogicaController(LancamentoNotasService lancamentoNotasService, BoletimService boletimService) {
        this.lancamentoNotasService = lancamentoNotasService;
        this.boletimService = boletimService;
    }

    public void lancarNota(Context ctx) {
        LancamentoNotaDTO dto = ctx.bodyAsClass(LancamentoNotaDTO.class);
        lancamentoNotasService.lancar(dto);
        ctx.status(201).json(Map.of("message", "Nota lançada com sucesso."));
    }

    public void gerarBoletim(Context ctx) {
        UUID tenantId = AuthUserContext.getAuthUser().getTenantId();
        UUID idTurma = UUID.fromString(ctx.queryParam("idTurma"));
        UUID idAluno = UUID.fromString(ctx.queryParam("idAluno"));
        ctx.json(boletimService.gerarBoletim(tenantId, idTurma, idAluno));
    }

    public void recuperacao(Context ctx) {
        UUID tenantId = AuthUserContext.getAuthUser().getTenantId();
        UUID idTurma = UUID.fromString(ctx.queryParam("idTurma"));
        UUID idDisciplina = UUID.fromString(ctx.queryParam("idDisciplina"));
        ctx.json(lancamentoNotasService.identificarAlunosEmRecuperacao(tenantId, idTurma, idDisciplina));
    }

    public void notaNecessaria(Context ctx) {
        BigDecimal mediaAtual = new BigDecimal(ctx.pathParam("mediaAtual"));
        BigDecimal necessaria = lancamentoNotasService.calcularNotaNecessariaRecuperacao(mediaAtual);
        ctx.json(Map.of("notaNecessaria", necessaria));
    }

    public void simulador(Context ctx) {
        SimulacaoNotaDTO dto = ctx.bodyAsClass(SimulacaoNotaDTO.class);
        ctx.json(lancamentoNotasService.simularNota(dto.mediaAtual(), dto.notaHipotetica()));
    }
}