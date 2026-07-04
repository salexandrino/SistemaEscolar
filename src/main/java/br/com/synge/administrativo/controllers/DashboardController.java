package br.com.synge.administrativo.controllers;

import br.com.synge.administrativo.dto.DashboardDTO;
import br.com.synge.administrativo.services.DashboardService;
import io.javalin.http.Context;
import org.thymeleaf.TemplateEngine;

public class DashboardController {

    private final DashboardService dashboardService;
    private final TemplateEngine templateEngine;

    public DashboardController(
            DashboardService dashboardService,
            TemplateEngine templateEngine) {
        this.dashboardService = dashboardService;
        this.templateEngine = templateEngine;
    }

    public void dashboard(Context ctx) {
        DashboardDTO dashboard = dashboardService.buscarDashboard();
        org.thymeleaf.context.Context thymeleaf = new org.thymeleaf.context.Context();
        thymeleaf.setVariable("dashboard", dashboard);

        // CORREÇÃO: Define qual página será injetada no master-admin
        thymeleaf.setVariable("content", "dashboard/index");

        // CORREÇÃO: Renderiza o layout master passando o contexto
        ctx.html(templateEngine.process("layouts/master-admin", thymeleaf));
    }

    public void Blacklist_ou_Escolas(Context ctx) {
        // Se as outras rotas também usarem o master-admin, siga o mesmo padrão:
        org.thymeleaf.context.Context thymeleaf = new org.thymeleaf.context.Context();
        thymeleaf.setVariable("content", "dashboard/index");
        ctx.html(templateEngine.process("layouts/master-admin", thymeleaf));
    }

    public void escolas(Context ctx) {
        org.thymeleaf.context.Context thymeleaf = new org.thymeleaf.context.Context();
        thymeleaf.setVariable("content", "dashboard/index");
        ctx.html(templateEngine.process("layouts/master-admin", thymeleaf));
    }

    public void novaEscola(Context ctx) {
        org.thymeleaf.context.Context thymeleaf = new org.thymeleaf.context.Context();
        thymeleaf.setVariable("content", "dashboard/escolas/nova");
        ctx.html(templateEngine.process("layouts/master-admin", thymeleaf));
    }

    public void usuarios(Context ctx) {
        org.thymeleaf.context.Context thymeleaf = new org.thymeleaf.context.Context();
        thymeleaf.setVariable("content", "dashboard/index-usuario");
        ctx.html(templateEngine.process("layouts/master-admin", thymeleaf));
    }
}