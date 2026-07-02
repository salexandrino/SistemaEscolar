package br.com.synge;

import br.com.synge.seguranca.models.Escola;
import br.com.synge.seguranca.repositories.UsuarioRepository;
import br.com.synge.seguranca.repositories.EscolaRepository;
import br.com.synge.seguranca.services.PasswordService;
import br.com.synge.seguranca.services.JwtService;
import br.com.synge.config.DatabaseConfig;
import br.com.synge.config.FlywayConfig;
import br.com.synge.seguranca.controllers.AuthController;
import br.com.synge.seguranca.services.AuthService;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class SyngeApplication {

    private static final Logger logger =
            LoggerFactory.getLogger(SyngeApplication.class);

    public static void main(String[] args) {

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

        AuthService authService = new AuthService(
                usuarioRepository,
                escolaRepository,
                passwordService,
                jwtService
        );

        AuthController authController = new AuthController(authService);

        Javalin app = Javalin.create(config -> config.staticFiles.add(staticFiles -> {
            staticFiles.hostedPath = "/";
            staticFiles.directory = "/public";
            staticFiles.location = Location.CLASSPATH;
        }));

        // Registra middleware de autenticação (popula AuthUserContext quando há JWT válido)
        app.before(new br.com.synge.seguranca.middlewares.AuthMiddleware(jwtService));

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

        app.get("/cadastro", ctx -> {
            Context context = new Context(ctx.req().getLocale());
            List<Escola> escolas = escolaRepository.findAllAtivas();
            context.setVariable("escolas", escolas);
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
        app.post("/auth/register", authController::register);
        app.post("/auth/logout", authController::logout);
        app.post("/auth/reset-password", authController::resetPassword);
        app.patch("/users/{id}/approve", authController::approveUser);

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

        // PING
        app.get("/ping", ctx ->
                ctx.json(Map.of(
                        "status", "ok",
                        "service", "eq14",
                        "timestamp", Instant.now().toString()
                ))
        );

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
