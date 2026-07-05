package br.com.synge.administrativo.controllers;

import br.com.synge.administrativo.dto.DashboardDTO;
import br.com.synge.administrativo.services.DashboardService;
import br.com.synge.seguranca.models.AuthUser;
import br.com.synge.seguranca.models.Escola;
import br.com.synge.seguranca.services.EscolaService; // Importante importar o service
import br.com.synge.seguranca.utils.AuthUserContext;
import br.com.synge.seguranca.exceptions.NotFoundException;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import java.util.Map;
import java.util.UUID;

public class DashboardController {

    // 1. ADICIONE O LOGGER PRÓPRIO DESTA CLASSE AQUI:
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;
    private final TemplateEngine templateEngine;

    // 2. ADICIONE A VARIÁVEL DO SERVICE AQUI:
    private final EscolaService escolaService;

    // 3. ATUALIZE O CONSTRUTOR PARA RECEBER O ESCOLASERVICE:
    public DashboardController(DashboardService dashboardService, EscolaService escolaService, TemplateEngine templateEngine) {
        this.dashboardService = dashboardService;
        this.escolaService = escolaService; // <--- Inicializa o service aqui
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

    public void editarEscola(Context ctx) {
        try {
            AuthUser currentUser = AuthUserContext.getAuthUser();
            if (currentUser == null) {
                ctx.redirect("/login");
                return;
            }

            String idParam = ctx.pathParam("id");
            UUID schoolId = UUID.fromString(idParam);

            // Agora o 'escolaService' vai funcionar perfeitamente!
            Escola escola = escolaService.buscarEscolaPorId(schoolId, currentUser);

            Map<String, Object> model = Map.of(
                    "escola", escola,
                    "currentUser", currentUser
            );

            ctx.render("dashboard/escolas/editar.html", model);

        } catch (IllegalArgumentException e) {
            logger.error("UUID inválido fornecido na rota: {}", ctx.pathParam("id")); // Agora usa o logger local
            ctx.status(400).result("ID da escola em formato inválido.");
        } catch (NotFoundException e) {
            logger.error("Escola não encontrada para o ID fornecido");
            ctx.status(404).result("Escola não encontrada.");
        } catch (Exception e) {
            logger.error("Erro ao carregar a página de edição de escola", e);
            ctx.status(500).result("Erro interno ao carregar a página.");
        }
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