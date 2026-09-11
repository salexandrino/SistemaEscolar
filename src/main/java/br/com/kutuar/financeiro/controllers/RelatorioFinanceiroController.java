package br.com.kutuar.financeiro.controllers;

import br.com.kutuar.financeiro.services.InadimplenciaService;
import br.com.kutuar.financeiro.services.RelatorioFinanceiroService;
import br.com.kutuar.seguranca.utils.AuthUserContext;
import io.javalin.http.Context;
import java.util.UUID;

public class RelatorioFinanceiroController {
    private final InadimplenciaService inadimplenciaService;
    private final RelatorioFinanceiroService relatorioFinanceiroService;

    public RelatorioFinanceiroController(InadimplenciaService inadimplenciaService, RelatorioFinanceiroService relatorioFinanceiroService) {
        this.inadimplenciaService = inadimplenciaService;
        this.relatorioFinanceiroService = relatorioFinanceiroService;
    }

    public void listarDevedores(Context ctx) {
        UUID tenantId = AuthUserContext.getAuthUser().getTenantId();
        ctx.json(inadimplenciaService.listarDevedores(tenantId));
    }

    public void obterPrevisaoEFluxo(Context ctx) {
        UUID tenantId = AuthUserContext.getAuthUser().getTenantId();
        ctx.json(relatorioFinanceiroService.gerarPrevisaoEFluxo(tenantId));
    }
}