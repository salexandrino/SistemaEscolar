package br.com.synge;

import br.com.synge.administrativo.controllers.DashboardController;
import br.com.synge.administrativo.repositories.DashboardRepository;
import br.com.synge.administrativo.services.DashboardService;
import br.com.synge.seguranca.controllers.EscolaDashboardController;
import br.com.synge.seguranca.exceptions.AuthenticationException;
import br.com.synge.seguranca.exceptions.NotFoundException;
import br.com.synge.seguranca.middlewares.AuthMiddleware;
import br.com.synge.seguranca.middlewares.SuperAdminMiddleware;
import br.com.synge.seguranca.models.AuthUser;
import br.com.synge.seguranca.models.Escola;
import br.com.synge.seguranca.models.Usuario;
import br.com.synge.seguranca.repositories.UsuarioRepository;
import br.com.synge.seguranca.repositories.EscolaRepository;
import br.com.synge.seguranca.services.PasswordService;
import br.com.synge.seguranca.services.JwtService;
import br.com.synge.config.DatabaseConfig;
import br.com.synge.config.FlywayConfig;
import br.com.synge.seguranca.controllers.AuthController;
import br.com.synge.seguranca.controllers.EscolaController;
import br.com.synge.seguranca.controllers.UsuarioAdminController;
import br.com.synge.seguranca.services.AuthService;
import br.com.synge.seguranca.services.EscolaService;
import br.com.synge.seguranca.services.UsuarioAdminService;
import br.com.synge.seguranca.utils.AuthUserContext;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SyngeApplication {

    private static final Logger logger = LoggerFactory.getLogger(SyngeApplication.class);

    public static void main(String[] args) throws SQLException {
        logger.info("Iniciando SYNGE...");

        try {
            DatabaseConfig.init();
            System.out.println("Banco inicializado!");
            FlywayConfig.migrate();

            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE usuario SET senha_hash = ? WHERE email = ?")) {

                PasswordService ps = new PasswordService();
                String hashGeradoPeloProjeto = ps.hash("SuperAdmin@123");

                stmt.setString(1, hashGeradoPeloProjeto);
                stmt.setString(2, "synge.gestao@gmail.com");
                int linhasAfetadas = stmt.executeUpdate();
            } catch (Exception e) {
                System.out.println("Erro: " + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

        // 1. Primeiro criamos os Repositories e Services básicos
        TemplateEngine templateEngine = createTemplateEngine();
        UsuarioRepository usuarioRepository = new UsuarioRepository();
        EscolaRepository escolaRepository = new EscolaRepository();

        PasswordService passwordService = new PasswordService();
        JwtService jwtService = new JwtService();
        SuperAdminMiddleware superAdminAuth = new SuperAdminMiddleware();
        DashboardRepository dashboardRepository = new DashboardRepository();

        // 2. Criamos os Services
        DashboardService dashboardService = new DashboardService(dashboardRepository);
        EscolaService schoolService = new EscolaService(escolaRepository);
        UsuarioAdminService usuarioAdminService = new UsuarioAdminService(usuarioRepository);
        AuthService authService = new AuthService(usuarioRepository, escolaRepository, passwordService, jwtService);

        // 3. Agora criamos os Controllers passando as dependências prontas
        AuthController authController = new AuthController(authService);
        UsuarioAdminController usuarioAdminController = new UsuarioAdminController(usuarioAdminService);
        EscolaController escolaController = new EscolaController(schoolService);

        DashboardController dashboardController = new DashboardController(dashboardService, schoolService, usuarioRepository, templateEngine);

        // ✔️ Configuração do Javalin 6
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "/public";
                staticFiles.location = Location.CLASSPATH;
            });

            config.fileRenderer((filePath, model, ctx) -> {
                Context thymeleafContext = new Context(ctx.req().getLocale());

                @SuppressWarnings("unchecked")
                Map<String, Object> cleanModel = (Map<String, Object>) (Map<String, ?>) model;
                thymeleafContext.setVariables(cleanModel);

                String templateName = filePath.replace(".html", "");
                return templateEngine.process(templateName, thymeleafContext);
            });
        });

        app.before(new AuthMiddleware(jwtService));

        // HOME
        app.get("/", ctx -> {
            Context context = new Context(ctx.req().getLocale());
            context.setVariables(homeModel());
            ctx.html(templateEngine.process("home", context));
        });

        // LOGIN
        app.get("/login", ctx -> {
            Context context = new Context(ctx.req().getLocale());
            String errorMessage = ctx.sessionAttribute("errorMessage");
            if (errorMessage != null && !errorMessage.isBlank()) {
                context.setVariable("error", errorMessage);
                ctx.sessionAttribute("errorMessage", null);
            }
            ctx.html(templateEngine.process("auth/login", context));
        });

        app.get("/super-admin/login", ctx -> {
            Context context = new Context(ctx.req().getLocale());
            String errorMessage = ctx.sessionAttribute("errorMessage");
            if (errorMessage != null && !errorMessage.isBlank()) {
                context.setVariable("error", errorMessage);
                ctx.sessionAttribute("errorMessage", null);
            }
            ctx.html(templateEngine.process("auth/login-super-admin", context));
        });

        app.get("/cadastro", ctx -> {
            Context context = new Context(ctx.req().getLocale());
            context.setVariable("escolas", List.of());
            ctx.html(templateEngine.process("auth/cadastro", context));
        });

        app.get("/esqueci-senha", ctx -> {
            Context context = new Context(ctx.req().getLocale());
            ctx.html(templateEngine.process("auth/esqueci-senha", context));
        });

        // POST/PATCH AUTENTICAÇÃO E API
        app.post("/auth/forgot-password", authController::forgotPassword);
        app.post("/auth/login", authController::login);
        app.post("/auth/super-admin/login", authController::superAdminLogin);
        app.post("/auth/register", authController::register);
        app.post("/auth/logout", authController::logout);
        app.post("/auth/reset-password", authController::resetPassword);

        app.get("/users", usuarioAdminController::listar);
        app.get("/users/{id}", usuarioAdminController::buscarPorId);
        app.put("/users/{id}", usuarioAdminController::atualizar);
        app.delete("/users/{id}", usuarioAdminController::inativar);
        app.patch("/users/{id}/approve", usuarioAdminController::aprovar);
        app.patch("/users/{id}/profile", usuarioAdminController::alterarPerfil);

        app.post("/escolas", escolaController::criarEscola);
        app.patch("/escolas/{id}", escolaController::atualizarEscola);
        app.get("/escolas", escolaController::listarEscolas);
        app.get("/escolas/ativas", escolaController::listarEscolasAtivas);
        app.get("/escolas/inativas", escolaController::listarEscolasInativas);
        app.get("/dashboard/escolas/editar/{id}", dashboardController::editarEscola);
        app.get("/escolas/{id}", escolaController::obterEscola);
        app.get("/escolas/cnpj/{cnpj}", escolaController::buscarPorCnpj);
        app.patch("/escolas/{id}/ativar", escolaController::ativarEscola);
        app.patch("/escolas/{id}/inativar", escolaController::inativarEscola);

        app.get("/area-logada", ctx -> {
            try {
                AuthUser currentUser = AuthUserContext.getAuthUser();
                if (currentUser == null) {
                    throw new AuthenticationException("Usuário não autenticado.");
                }
                ctx.json(Map.of(
                        "userId", currentUser.getUserId(),
                        "tenantId", currentUser.getTenantId(),
                        "perfil", currentUser.getPerfil(),
                        "cpf", currentUser.getCpf()
                ));
            } catch (AuthenticationException e) {
                ctx.status(e.getStatus());
                ctx.json(Map.of("message", e.getMessage()));
            }
        });

        // ==========================================
        // 1. FILTROS DE SEGURANÇA (OBRIGATÓRIO FICAR NO TOPO)
        // ==========================================
        app.before("/ping", superAdminAuth);
        app.before("/dashboard", superAdminAuth);
        app.before("/dashboard/*", superAdminAuth);

        // ==========================================
        // 2. DEFINIÇÃO DAS ROTAS
        // ==========================================
        app.get("/ping", ctx -> {
            Map<String, String> response = Map.of(
                    "status", "ok",
                    "service", "eq14",
                    "timestamp", Instant.now().toString()
            );
            ctx.json(response);
        });

        // Rotas principais do Painel
        app.get("/dashboard", dashboardController::dashboard);

        // --- GESTÃO DE ESCOLAS ---
        app.get("/dashboard/escolas", escolaController::exibirPaginaListagem);
        app.get("/dashboard/escolas/visualizar/{id}", escolaController::exibirPaginaVisualizar);
        app.get("/dashboard/escolas/nova", dashboardController::novaEscola);
        app.get("/dashboard/escolas/editar", dashboardController::editarEscola);
        app.get("/dashboard/escolas/visualizar", dashboardController::visualizarEscola);

// 🌟 AS ROTAS DE SALVAMENTO DA ESCOLA (Aceitando os caminhos que seu HTML e API usam)
        app.post("/dashboard/escolas/editar/{id}", escolaController::atualizarEscola);
        app.post("/escolas/{id}", escolaController::atualizarEscola);   // 👈 ADICIONE ESTA (É a rota que o form HTML chama!)
        app.patch("/api/escolas/{id}", escolaController::atualizarEscola);

// Rotas de API de Escolas
        app.get("/api/escolas", escolaController::listarEscolas);
        app.get("/api/escolas/{id}", escolaController::obterEscola);

// Rotas de ativação/inativação
        app.post("/escolas/{id}/ativar", escolaController::ativarEscola);
        app.post("/escolas/{id}/inativar", escolaController::inativarEscola);


// --- GESTÃO DE USUÁRIOS ---
        app.get("/dashboard/usuarios", dashboardController::usuarios);
        app.get("/dashboard/usuarios/novo", dashboardController::novoUsuario);
        app.get("/dashboard/usuarios/editar/{id}", dashboardController::editarUsuario);
        app.get("/dashboard/usuarios/visualizar/{id}", dashboardController::visualizarUsuario);
// 🌟 AS ROTAS DE SALVAMENTO DO USUÁRIO
        app.post("/dashboard/usuarios/editar/{id}", usuarioAdminController::atualizar);
        app.post("/usuarios/{id}", usuarioAdminController::atualizar);  // 👈 ADICIONE ESTA (Caso o HTML de usuário use o caminho curto também)



        // ==========================================
        // TRATAMENTO DE EXCEÇÕES E ERROS DA API
        // ==========================================
        app.exception(AuthenticationException.class, (e, ctx) -> {
            ctx.redirect("/super-admin/login");
        });

        app.exception(NotFoundException.class, (e, ctx) -> {
            ctx.status(404);
            Context thymeleafContext = new Context();
            thymeleafContext.setVariable("message", e.getMessage());
            ctx.html(templateEngine.process("errors/404", thymeleafContext));
        });

        app.exception(Exception.class, (e, ctx) -> {
            ctx.status(500);
            Context thymeleafContext = new Context();
            thymeleafContext.setVariable("message", "Ocorreu um erro interno inesperado.");
            ctx.html(templateEngine.process("errors/500", thymeleafContext));
        });

        app.start(port);
        logger.info("Servidor iniciado na porta {}", port);
    }

    private static TemplateEngine createTemplateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(resolver);
        return templateEngine;
    }

    private static Map<String, Object> homeModel() {
        return Map.of(
                "showAnnouncement", true,
                "resources", List.of(
                        Map.of("icon", "bi-mortarboard", "title", "Gestão Acadêmica", "items", List.of("Alunos", "Professores", "Notas", "Frequência", "Boletins")),
                        Map.of("icon", "bi-cash-coin", "title", "Gestão Financeira", "items", List.of("Mensalidades", "Inadimplência", "Fluxo de caixa", "Parcelamentos")),
                        Map.of("icon", "bi-chat-dots", "title", "Comunicação", "items", List.of("Avisos", "Notificações", "Comunicados")),
                        Map.of("icon", "bi-bar-chart", "title", "Relatórios Inteligentes", "items", List.of("Indicadores", "Gráficos", "Exportação em PDF")),
                        Map.of("icon", "bi-shield-lock", "title", "Segurança", "items", List.of("Criptografia", "Logs", "Backup", "Permissões"))
                ),
                "modules", List.of(
                        Map.of("icon", "bi-journal-check", "title", "Acadêmico", "slug", "academico", "description", "Controle matrículas, turmas, notas, frequência e boletins em uma rotina integrada."),
                        Map.of("icon", "bi-wallet2", "title", "Financeiro", "slug", "financeiro", "description", "Acompanhe mensalidades, recebíveis, inadimplência e fluxo de caixa com clareza."),
                        Map.of("icon", "bi-building-gear", "title", "Administrativo", "slug", "administrativo", "description", "Organize cadastros, usuários, permissões e processos internos da instituição."),
                        Map.of("icon", "bi-diagram-3", "title", "Multi Tenant", "slug", "multi-tenant", "description", "Gerencie múltiplas escolas com isolamento de dados e configurações por unidade."),
                        Map.of("icon", "bi-graph-up-arrow", "title", "Relatórios", "slug", "relatorios", "description", "Transforme dados operacionais em indicadores para decisões mais rápidas."),
                        Map.of("icon", "bi-lock", "title", "Segurança", "slug", "seguranca", "description", "Proteja informações sensíveis com logs, backup e controle de acesso.")
                ),
                "benefits", List.of("Centralização de Dados", "Automação de Processos", "Economia de Tempo", "Maior Controle Financeiro", "Tomada de Decisões Inteligente"),
                "plans", List.of(
                        Map.of("name", "Essencial", "description", "Ideal para escolas que desejam centralizar cadastros, turmas, notas e comunicação.", "featured", false),
                        Map.of("name", "Gestão Completa", "description", "Inclui módulos acadêmico, financeiro, relatórios, permissões e suporte especializado.", "featured", true)
                ),
                "testimonials", List.of(
                        Map.of("photo", "/images/avatar-ana.svg", "name", "Ana Ribeiro", "role", "Diretora Escolar", "comment", "O SYNGE reduziu retrabalho e deu visibilidade diária para a nossa equipe pedagógica e financeira."),
                        Map.of("photo", "/images/avatar-marcos.svg", "name", "Marcos Lima", "role", "Coordenador Administrativo", "comment", "A centralização dos dados tornou a gestão mais rápida, organizada e confiável para todos os setores."),
                        Map.of("photo", "/images/avatar-julia.svg", "name", "Júlia Costa", "role", "Gestora Financeira", "comment", "Hoje acompanhamos recebíveis, inadimplência e indicadores em poucos minutos, sem planilhas paralelas.")
                )
        );
    }
}