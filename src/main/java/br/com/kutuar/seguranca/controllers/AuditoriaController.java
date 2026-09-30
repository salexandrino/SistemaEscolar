package br.com.kutuar.seguranca.controllers;

import br.com.kutuar.seguranca.services.AuditoriaService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.LinkedHashMap;
import java.util.Map;

public class AuditoriaController {
    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    public void listar(Context ctx) {
        var eventos = auditoriaService.listar();
        ctx.status(HttpStatus.OK);
        ctx.json(Map.of("total", eventos.size(), "auditoria", eventos.stream().map(evento -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", evento.getId());
            item.put("executorId", evento.getExecutorId());
            item.put("executorPerfil", evento.getExecutorPerfil());
            item.put("tenantId", evento.getTenantId());
            item.put("acao", evento.getAcao());
            item.put("entidade", evento.getEntidade());
            item.put("entidadeId", evento.getEntidadeId());
            item.put("detalhes", evento.getDetalhes());
            item.put("criadoEm", evento.getCriadoEm());
            return item;
        }).toList()));
    }
}
