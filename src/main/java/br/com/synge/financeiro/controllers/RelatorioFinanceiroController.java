package br.com.synge.financeiro.controllers;

import br.com.synge.financeiro.services.InadimplenciaService;
import br.com.synge.financeiro.services.RelatorioFinanceiroService;
import br.com.synge.seguranca.utils.AuthUserContext;
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