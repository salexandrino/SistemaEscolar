package br.com.kutuar.seguranca.controllers;

import br.com.kutuar.seguranca.services.AuditoriaPersistenteService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

public class AuditoriaController {

    private static final Logger logger = LoggerFactory.getLogger(AuditoriaController.class);
    private final AuditoriaPersistenteService auditoriaService;

    public AuditoriaController(AuditoriaPersistenteService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    public void listar(Context ctx) {
        try {
            LocalDate dataInicio = parseDate(ctx.queryParam("dataInicio"), "dataInicio");
            LocalDate dataFim = parseDate(ctx.queryParam("dataFim"), "dataFim");
            var response = auditoriaService.listar(
                    ctx.queryParam("acao"), ctx.queryParam("entidade"), dataInicio, dataFim,
                    parseInteger(ctx.queryParam("page"), 1, "page"),
                    parseInteger(ctx.queryParam("size"), 20, "size"));
            ctx.status(HttpStatus.OK).json(response);
        } catch (DateTimeParseException | IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro inesperado ao consultar auditoria. tipo={}", e.getClass().getSimpleName());
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of("message", "Erro interno ao consultar auditoria."));
        }
    }

    private LocalDate parseDate(String value, String parameter) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new DateTimeParseException("Parâmetro " + parameter + " deve usar o formato AAAA-MM-DD.", value, e.getErrorIndex(), e);
        }
    }

    private Integer parseInteger(String value, int defaultValue, String parameter) {
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Parâmetro " + parameter + " deve ser um número inteiro.");
        }
    }
}