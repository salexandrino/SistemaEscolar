package br.com.synge;
import br.com.synge.administrativo.controllers.DashboardController;
import br.com.synge.administrativo.repositories.DashboardRepository;
import br.com.synge.administrativo.services.DashboardService;
import br.com.synge.seguranca.controllers.EscolaDashboardController;
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

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SyngeApplication {

    private static final Logger logger =
            LoggerFactory.getLogger(SyngeApplication.class);

    public static void main(String[] args) throws SQLException {

        logger.info("Iniciando SYNGE...");


        try {
            DatabaseConfig.init();
            System.out.println("Banco inicializado!");
            FlywayConfig.migrate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        int port = Integer.parseInt(
                System.getenv().getOrDefault("PORT", "8080")
        );

        TemplateEngine templateEngine = createTemplateEngine();
        UsuarioRepository usuarioRepository = new UsuarioRepository();
        EscolaRepository escolaRepository = new EscolaRepository();

        PasswordService passwordService = new PasswordService();
        JwtService jwtService = new JwtService();

        DashboardRepository dashboardRepository = new DashboardRepository();

        DashboardService dashboardService =
                new DashboardService(dashboardRepository);

        DashboardController dashboardController =
                new DashboardController(
                        dashboardService,
                        templateEngine
                );

        AuthService authService = new AuthService(
                usuarioRepository,
                escolaRepository,
                passwordService,
                jwtService
        );

        EscolaService escolaService = new EscolaService(escolaRepository);
        UsuarioAdminService usuarioAdminService = new UsuarioAdminService(usuarioRepository);

        AuthController authController = new AuthController(authService);
        EscolaController escolaController = new EscolaController(escolaService);
        UsuarioAdminController usuarioAdminController = new UsuarioAdminController(usuarioAdminService);
        EscolaDashboardController escolaDashboardController =
                new EscolaDashboardController(
                        escolaService,
                        templateEngine
                );
        Javalin app = Javalin.create(config -> config.staticFiles.add(staticFiles -> {
            staticFiles.hostedPath = "/";
            staticFiles.directory = "/public";
            staticFiles.location = Location.CLASSPATH;
        }));

        // Registra middleware de autenticação (popula AuthUserContext quando há JWT válido)
        app.before(new br.com.synge.seguranca.middlewares.AuthMiddleware(jwtService));

// Registra middlewares de autorização para rotas /dashboard/*
// Dashboard principal: SUPER_ADMIN e GESTOR
        // app.before(new AuthMiddleware(jwtService));

// app.before("/dashboard*", new SuperAdminMiddleware());
        // HOME
        app.get("/", ctx -> {
            Context context = new Context(ctx.req().getLocale());
            context.setVariables(homeModel());
            ctx.html(templateEngine.process("home", context));
        });

        // LOGIN (abrir a tela) - lê mensagem de erro da sessão e a repassa ao template como 'error'
        app.get("/login", ctx -> {
            Context context = new Context(ctx.req().getLocale());
            String errorMessage = ctx.sessionAttribute("errorMessage");
            if (errorMessage != null && !errorMessage.isBlank()) {
                context.setVariable("error", errorMessage);
                ctx.sessionAttribute("errorMessage", null); // limpa após leitura
            }
            ctx.html(templateEngine.process("auth/login", context));
        });
        // LOGIN DO SUPER ADMIN
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


        // RECUPERAR SENHA (abrir a tela via Thymeleaf mapeada para /esqueci-senha)
        app.get("/esqueci-senha", ctx -> {
            Context context = new Context(ctx.req().getLocale());
            ctx.html(templateEngine.process("auth/esqueci-senha", context));
        });

        // ROTAS DA AUTENTICAÇÃO (Processamentos POST/PATCH)
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

        // ROTAS DE ADMINISTRAÇÃO DE ESCOLAS (SUPER_ADMIN ONLY)
        app.post("/escolas", escolaController::criarEscola);
        app.patch("/escolas/{id}", escolaController::atualizarEscola);
        app.get("/escolas", escolaController::listarEscolas);
        app.get("/escolas/ativas", escolaController::listarEscolasAtivas);
        app.get("/escolas/inativas", escolaController::listarEscolasInativas);
        app.get("/escolas/{id}", escolaController::obterEscola);
        app.get("/escolas/cnpj/{cnpj}", escolaController::buscarPorCnpj);
        app.patch("/escolas/{id}/ativar", escolaController::ativarEscola);
        app.patch("/escolas/{id}/inativar", escolaController::inativarEscola);

        // Rota protegida para testar autenticação: retorna dados do usuário autenticado
        app.get("/area-logada", ctx -> {
            try {
                br.com.synge.seguranca.models.AuthUser currentUser = br.com.synge.seguranca.utils.AuthUserContext.getAuthUser();
                if (currentUser == null) {
                    throw new br.com.synge.seguranca.exceptions.AuthenticationException("Usuário não autenticado.");
                }
                ctx.json(Map.of(
                        "userId", currentUser.getUserId(),
                        "tenantId", currentUser.getTenantId(),
                        "perfil", currentUser.getPerfil(),
                        "cpf", currentUser.getCpf()
                ));
            } catch (br.com.synge.seguranca.exceptions.AuthenticationException e) {
                ctx.status(e.getStatus());
                ctx.json(Map.of("message", e.getMessage()));
            }
        });

        // ROTAS DO PAINEL ADMINISTRATIVO (protegidas por middleware de autenticação + autorização)
        
        // Dashboard Principal
        app.get("/dashboard", dashboardController::dashboard);

        // Listagem de Usuários
        app.get("/dashboard/usuarios", ctx -> {

            Context context = new Context(ctx.req().getLocale());

            AuthUser authUser = AuthUserContext.getAuthUser();

            Usuario currentUser = usuarioRepository.findById(authUser.getUserId())
                    .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

            context.setVariable("currentUser", currentUser);

            context.setVariable(
                    "usuarios",
                    usuarioAdminService.listarTodos()
            );
            System.out.println("Entrou na rota de usuários.");

            System.out.println(currentUser.getNomeCompleto());

            System.out.println(usuarioAdminService.listarTodos());

            ctx.html(
                    templateEngine.process(
                            "dashboard/usuarios/index",
                            context
                    )
            );

        });
        // Novo Usuário
        app.get("/dashboard/usuarios/novo", ctx -> {

            Context context = new Context(ctx.req().getLocale());

            context.setVariable("currentUser",
                    AuthUserContext.getAuthUser());

            context.setVariable("escolas",
                    escolaService.listarTodas(AuthUserContext.getAuthUser()));

            ctx.html(templateEngine.process(
                    "dashboard/usuarios/novo",
                    context
            ));

        });

        // Editar Usuário - CORRIGIDO
        app.get("/dashboard/usuarios/editar/{id}", ctx -> {

            Context context = new Context(ctx.req().getLocale());

            AuthUser authUser = AuthUserContext.getAuthUser();

            UUID id = UUID.fromString(ctx.pathParam("id"));

            Usuario usuario = usuarioAdminService.buscarPorId(id, authUser);

            Usuario usuarioCompleto = usuarioRepository.findById(authUser.getUserId())
                    .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

            context.setVariable("currentUser", usuarioCompleto);
            context.setVariable("usuario", usuario);

            ctx.html(templateEngine.process("dashboard/usuarios/editar", context));

        });

        // Visualizar Usuário - CORRIGIDO
        app.get("/dashboard/usuarios/visualizar/{id}", ctx -> {

            Context context = new Context(ctx.req().getLocale());

            AuthUser authUser = AuthUserContext.getAuthUser();

            UUID id = UUID.fromString(ctx.pathParam("id"));

            Usuario usuario = usuarioAdminService.buscarPorId(id, authUser);

            Usuario usuarioCompleto = usuarioRepository.findById(authUser.getUserId())
                    .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

            context.setVariable("currentUser", usuarioCompleto);
            context.setVariable("usuario", usuario);

            ctx.html(templateEngine.process("dashboard/usuarios/visualizar", context));

        });

        // Listagem de Escolas
        app.get("/dashboard/escolas", ctx -> {
                ctx.result("FUNCIONOU");

            Context context = new Context(ctx.req().getLocale());

            AuthUser authUser = AuthUserContext.getAuthUser();

            Usuario currentUser = usuarioRepository.findById(authUser.getUserId())
                    .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

            context.setVariable("currentUser", currentUser);

            context.setVariable(
                    "escolas",
                    escolaService.listarTodas(authUser)
            );
            System.out.println("Entrou na rota de escolas.");

            System.out.println(currentUser.getNomeCompleto());

            System.out.println(escolaService.listarTodas(authUser));

            ctx.html(
                    templateEngine.process(
                            "dashboard/escolas/index",
                            context
                    )
            );

        });
        app.get("/teste", ctx -> {
            ctx.result("FUNCIONOU");
        });
        // Nova Escola (SUPER_ADMIN only - protegido por middleware)
        app.get("/dashboard/escolas/nova", ctx -> {

            Context context = new Context(ctx.req().getLocale());

            AuthUser authUser = AuthUserContext.getAuthUser();

            Usuario usuarioCompleto = usuarioRepository.findById(authUser.getUserId())
                    .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

            context.setVariable("currentUser", usuarioCompleto);

            ctx.html(templateEngine.process("dashboard/escolas/nova", context));

        });

        // Editar Escola (SUPER_ADMIN only - protegido por middleware) - CORRIGIDO
        app.get("/dashboard/escolas/editar/{id}", ctx -> {

            Context context = new Context(ctx.req().getLocale());

            AuthUser authUser = AuthUserContext.getAuthUser();

            UUID id = UUID.fromString(ctx.pathParam("id"));

            Escola escola = escolaService.buscarEscolaPorId(id, authUser);

            Usuario usuarioCompleto = usuarioRepository.findById(authUser.getUserId())
                    .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

            context.setVariable("currentUser", usuarioCompleto);
            context.setVariable("escola", escola);

            ctx.html(templateEngine.process("dashboard/escolas/editar", context));

        });

        // Visualizar Escola (SUPER_ADMIN only - protegido por middleware) - CORRIGIDO
        app.get("/dashboard/escolas/visualizar/{id}", ctx -> {

            Context context = new Context(ctx.req().getLocale());

            AuthUser authUser = AuthUserContext.getAuthUser();

            UUID id = UUID.fromString(ctx.pathParam("id"));

            Escola escola = escolaService.buscarEscolaPorId(id, authUser);

            Usuario usuarioCompleto = usuarioRepository.findById(authUser.getUserId())
                    .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

            context.setVariable("currentUser", usuarioCompleto);
            context.setVariable("escola", escola);

            ctx.html(templateEngine.process("dashboard/escolas/visualizar", context));

        });
        // PING
        app.get("/ping", ctx ->
                ctx.json(Map.of(
                        "status", "ok",
                        "service", "eq14",
                        "timestamp", Instant.now().toString()
                ))
        );
        // CAPTURA GLOBAL DE EXCEÇÕES (Evita o Server Error 500)
        app.exception(br.com.synge.seguranca.exceptions.ApiException.class, (e, ctx) -> {
            ctx.status(e.getStatus());
            if (ctx.path().startsWith("/dashboard")) {
                ctx.sessionAttribute("errorMessage", e.getMessage());
                ctx.redirect("/login");
            } else {
                ctx.json(Map.of("message", e.getMessage()));
            }
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
