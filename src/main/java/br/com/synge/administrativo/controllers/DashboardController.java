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

    // 1. Tela Inicial Principal do Dashboard
    public void dashboard(Context ctx) {
        DashboardDTO dashboard = dashboardService.buscarDashboard();
        org.thymeleaf.context.Context thymeleaf = new org.thymeleaf.context.Context();
        thymeleaf.setVariable("dashboard", dashboard);
        thymeleaf.setVariable("content", "dashboard/index");
        ctx.html(templateEngine.process("layouts/master-admin", thymeleaf));
    }

    // ==========================================
    // ROTAS DE GESTÃO DE ESCOLAS
    // ==========================================

    // 2. Listar Escolas (index.html dentro de dashboard/escolas/)
    public void escolas(Context ctx) {
        org.thymeleaf.context.Context thymeleaf = new org.thymeleaf.context.Context();

        // Aqui deve buscar a lista do banco de dados quando integrar, por enquanto mantém o fluxo visual
        // thymeleaf.setVariable("escolas", escolaService.listarTodas());

        thymeleaf.setVariable("content", "dashboard/escolas/index");
        ctx.html(templateEngine.process("layouts/master-admin", thymeleaf));
    }

    // 3. Cadastrar Nova Escola (nova.html dentro de dashboard/escolas/)
    public void novaEscola(Context ctx) {
        org.thymeleaf.context.Context thymeleaf = new org.thymeleaf.context.Context();
        thymeleaf.setVariable("content", "dashboard/escolas/nova");
        ctx.html(templateEngine.process("layouts/master-admin", thymeleaf));
    }

    // 4. Editar Escola (editar.html dentro de dashboard/escolas/)
    public void editarEscola(Context ctx) {
        org.thymeleaf.context.Context thymeleaf = new org.thymeleaf.context.Context();

        // Simulando que pegaria o ID enviado pela rota
        // String id = ctx.pathParam("id");

        thymeleaf.setVariable("content", "dashboard/escolas/editar");
        ctx.html(templateEngine.process("layouts/master-admin", thymeleaf));
    }

    // 5. Visualizar Escola (visualizar.html dentro de dashboard/escolas/)
    public void visualizarEscola(Context ctx) {
        org.thymeleaf.context.Context thymeleaf = new org.thymeleaf.context.Context();
        thymeleaf.setVariable("content", "dashboard/escolas/visualizar");
        ctx.html(templateEngine.process("layouts/master-admin", thymeleaf));
    }


    // ==========================================
    // ROTAS DE GESTÃO DE USUÁRIOS
    // ==========================================

    // 6. Listar Usuários (index.html dentro de dashboard/usuarios/)
    public void usuarios(Context ctx) {
        org.thymeleaf.context.Context thymeleaf = new org.thymeleaf.context.Context();

        // Captura se veio o filtro ?status=pendente que colocamos no botão "Aprovar" da Sidebar
        String status = ctx.queryParam("status");
        thymeleaf.setVariable("filtroStatus", status);

        thymeleaf.setVariable("content", "dashboard/usuarios/index");
        ctx.html(templateEngine.process("layouts/master-admin", thymeleaf));
    }

    // 7. Cadastrar Novo Usuário (novo.html dentro de dashboard/usuarios/)
    public void novoUsuario(Context ctx) {
        org.thymeleaf.context.Context thymeleaf = new org.thymeleaf.context.Context();
        thymeleaf.setVariable("content", "dashboard/usuarios/novo");
        ctx.html(templateEngine.process("layouts/master-admin", thymeleaf));
    }

    // 8. Editar Usuário (editar.html dentro de dashboard/usuarios/)
    public void editarUsuario(Context ctx) {
        org.thymeleaf.context.Context thymeleaf = new org.thymeleaf.context.Context();
        thymeleaf.setVariable("content", "dashboard/usuarios/editar");
        ctx.html(templateEngine.process("layouts/master-admin", thymeleaf));
    }

    // 9. Visualizar Usuário (visualizar.html dentro de dashboard/usuarios/)
    public void visualizarUsuario(Context ctx) {
        org.thymeleaf.context.Context thymeleaf = new org.thymeleaf.context.Context();
        thymeleaf.setVariable("content", "dashboard/usuarios/visualizar");
        ctx.html(templateEngine.process("layouts/master-admin", thymeleaf));
    }
}