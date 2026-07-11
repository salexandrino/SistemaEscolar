package br.com.synge.academico.controllers;

import br.com.synge.academico.dtos.LancamentoFrequenciaDTO;
import br.com.synge.academico.dtos.LancamentoNotaDTO;
import br.com.synge.academico.services.BoletimService;
import br.com.synge.academico.services.FrequenciaService;
import br.com.synge.academico.services.LancamentoNotasService;
import io.javalin.http.Context;
import org.eclipse.jetty.http.HttpStatus;

import java.util.UUID;

public class GestaoPedagogicaController {

    private final LancamentoNotasService notasService;
    private final FrequenciaService frequenciaService;
    private final BoletimService boletimService;

    public GestaoPedagogicaController(LancamentoNotasService notasService, 
                                      FrequenciaService frequenciaService, 
                                      BoletimService boletimService) {
        this.notasService = notasService;
        this.frequenciaService = frequenciaService;
        this.boletimService = boletimService;
    }

    public void lancarNota(Context ctx) {
        LancamentoNotaDTO dto = ctx.bodyAsClass(LancamentoNotaDTO.class);
        notasService.lancar(dto);
        ctx.status(HttpStatus.NO_CONTENT_204);
    }

    public void registrarFrequencia(Context ctx) {
        LancamentoFrequenciaDTO dto = ctx.bodyAsClass(LancamentoFrequenciaDTO.class);
        frequenciaService.registrar(dto);
        ctx.status(HttpStatus.NO_CONTENT_204);
    }

    public void gerarBoletim(Context ctx) {
        UUID idAluno = UUID.fromString(ctx.queryParam("idAluno"));
        UUID idTurma = UUID.fromString(ctx.queryParam("idTurma"));
        UUID idDisciplina = UUID.fromString(ctx.queryParam("idDisciplina"));
        
        ctx.json(boletimService.gerarBoletim(idAluno, idTurma, idDisciplina));
    }
}
