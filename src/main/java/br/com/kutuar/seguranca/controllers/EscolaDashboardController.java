package br.com.kutuar.seguranca.controllers;

import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.models.Escola;
import br.com.kutuar.seguranca.services.EscolaService;
import br.com.kutuar.seguranca.utils.AuthUserContext;
import io.javalin.http.Context;
import org.thymeleaf.TemplateEngine;

import java.util.List;
import java.util.UUID;

public class EscolaDashboardController {

    private final EscolaService escolaService;
    private final TemplateEngine templateEngine;

    public EscolaDashboardController(
            EscolaService escolaService,
            TemplateEngine templateEngine) {

        this.escolaService = escolaService;
        this.templateEngine = templateEngine;
    }

    public void listar(Context ctx) {

        AuthUser currentUser = AuthUserContext.getAuthUser();

        List<Escola> escolas =
                escolaService.listarTodas(currentUser);

        org.thymeleaf.context.Context thymeleaf =
                new org.thymeleaf.context.Context();

        thymeleaf.setVariable("escolas", escolas);

        ctx.html(
                templateEngine.process(
                        "dashboard/escolas/index",
                        thymeleaf
                )
        );
    }

    public void visualizar(Context ctx) {

        AuthUser currentUser = AuthUserContext.getAuthUser();

        UUID id = UUID.fromString(ctx.pathParam("id"));

        Escola escola =
                escolaService.buscarEscolaPorId(id, currentUser);

        org.thymeleaf.context.Context thymeleaf =
                new org.thymeleaf.context.Context();

        thymeleaf.setVariable("escola", escola);

        ctx.html(
                templateEngine.process(
                        "dashboard/escolas/visualizar",
                        thymeleaf
                )
        );
    }

    public void editar(Context ctx) {

        AuthUser currentUser = AuthUserContext.getAuthUser();

        UUID id = UUID.fromString(ctx.pathParam("id"));

        Escola escola =
                escolaService.buscarEscolaPorId(id, currentUser);

        org.thymeleaf.context.Context thymeleaf =
                new org.thymeleaf.context.Context();

        thymeleaf.setVariable("escola", escola);

        ctx.html(
                templateEngine.process(
                        "dashboard/escolas/editar",
                        thymeleaf
                )
        );
    }

    public void nova(Context ctx) {

        ctx.html(
                templateEngine.process(
                        "dashboard/escolas/nova",
                        new org.thymeleaf.context.Context()
                )
        );
    }

}