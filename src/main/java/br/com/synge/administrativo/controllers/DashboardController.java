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

        org.thymeleaf.context.Context thymeleaf =
                new org.thymeleaf.context.Context();

        thymeleaf.setVariable("dashboard", dashboard);

        ctx.html(
                templateEngine.process(
                        "dashboard/index",
                        thymeleaf
                )
        );
    }
    public void escolas(Context ctx) {

        ctx.html(
                templateEngine.process(
                        "dashboard/escolas/index",
                        new org.thymeleaf.context.Context()
                )
        );

    }

    public void novaEscola(Context ctx) {

        ctx.html(
                templateEngine.process(
                        "dashboard/escolas/nova",
                        new org.thymeleaf.context.Context()
                )
        );

    }

    public void usuarios(Context ctx) {

        ctx.html(
                templateEngine.process(
                        "dashboard/usuarios/index",
                        new org.thymeleaf.context.Context()
                )
        );

    }
}