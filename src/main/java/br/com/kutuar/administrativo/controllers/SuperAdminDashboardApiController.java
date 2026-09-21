package br.com.kutuar.administrativo.controllers;

import br.com.kutuar.administrativo.services.DashboardService;
import io.javalin.http.Context;

public class SuperAdminDashboardApiController {

    private final DashboardService dashboardService;

    public SuperAdminDashboardApiController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    public void dashboard(Context ctx) {
        ctx.json(dashboardService.buscarDashboardSuperAdmin());
    }

    public void alertas(Context ctx) {
        ctx.json(dashboardService.buscarAlertasSistema());
    }

    public void atividades(Context ctx) {
        ctx.json(dashboardService.buscarAtividadesRecentes());
    }

    public void ultimosAcessos(Context ctx) {
        ctx.json(dashboardService.buscarUltimosAcessos());
    }
}
