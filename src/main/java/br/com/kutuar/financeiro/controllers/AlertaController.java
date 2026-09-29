package br.com.kutuar.financeiro.controllers;

import br.com.kutuar.financeiro.services.AlertaService;
import br.com.kutuar.seguranca.services.TenantAccessGuard;
import io.javalin.http.Context;
import java.util.UUID;

public class AlertaController {
    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    public void obterAlertas(Context ctx) {
        UUID tenantId = TenantAccessGuard.currentTenant();
        ctx.json(alertaService.gerarAlertasFinanceiros(tenantId));
    }
}
