package br.com.kutuar;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import br.com.kutuar.academico.services.observers.HistoricoSituacaoObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import br.com.kutuar.academico.controllers.AlocacaoDocenteController;
import br.com.kutuar.academico.controllers.AlunoController;
import br.com.kutuar.academico.controllers.AnoLetivoController;
import br.com.kutuar.academico.controllers.AvaliacaoController;
import br.com.kutuar.academico.controllers.DisciplinaController;
import br.com.kutuar.academico.controllers.GestaoPedagogicaController;
import br.com.kutuar.academico.controllers.MatrizCurricularController;
import br.com.kutuar.academico.controllers.ProfessorController;
import br.com.kutuar.academico.controllers.SerieController;
import br.com.kutuar.academico.controllers.TurmaController;
import br.com.kutuar.academico.repositories.AlunoRepository;
import br.com.kutuar.academico.repositories.AnoLetivoCloneRepository;
import br.com.kutuar.academico.repositories.AnoLetivoRepository;
import br.com.kutuar.academico.repositories.AvaliacaoRepository;
import br.com.kutuar.academico.repositories.DisciplinaRepository;
import br.com.kutuar.academico.repositories.DocumentoAlunoRepository;
import br.com.kutuar.academico.repositories.HistoricoSituacaoAlunoRepository;
import br.com.kutuar.academico.repositories.MatriculaRepository;
import br.com.kutuar.academico.repositories.NotaRepository;
import br.com.kutuar.academico.repositories.ProfessorRepository;
import br.com.kutuar.academico.repositories.SerieDisciplinaRepository;
import br.com.kutuar.academico.repositories.SerieRepository;
import br.com.kutuar.academico.repositories.TurmaDisciplinaProfessorRepository;
import br.com.kutuar.academico.repositories.TurmaRepository;
import br.com.kutuar.academico.services.AlocacaoDocenteService;
import br.com.kutuar.academico.services.AlunoService;
import br.com.kutuar.academico.services.AnoLetivoService;
import br.com.kutuar.academico.services.AvaliacaoService;
import br.com.kutuar.academico.services.BoletimService;
import br.com.kutuar.academico.services.DisciplinaService;
import br.com.kutuar.academico.services.LancamentoNotasService;
import br.com.kutuar.academico.services.MatrizCurricularService;
import br.com.kutuar.academico.services.ProfessorService;
import br.com.kutuar.academico.services.SerieService;
import br.com.kutuar.academico.services.TurmaService;
import br.com.kutuar.academico.services.media.CalculoMediaAritmetica;
import br.com.kutuar.administrativo.controllers.DashboardController;
import br.com.kutuar.administrativo.controllers.SuperAdminDashboardApiController;
import br.com.kutuar.administrativo.repositories.DashboardRepository;
import br.com.kutuar.administrativo.services.DashboardService;
import br.com.kutuar.config.DatabaseConfig;
import br.com.kutuar.config.FlywayConfig;
import br.com.kutuar.financeiro.controllers.AlertaController;
import br.com.kutuar.financeiro.controllers.MensalidadeController;
import br.com.kutuar.financeiro.controllers.RelatorioFinanceiroController;
import br.com.kutuar.financeiro.repositories.DescontoRepository;
import br.com.kutuar.financeiro.repositories.MensalidadeRepository;
import br.com.kutuar.financeiro.repositories.PagamentoRepository;
import br.com.kutuar.financeiro.repositories.ParcelaRepository;
import br.com.kutuar.financeiro.services.AlertaService;
import br.com.kutuar.financeiro.services.InadimplenciaService;
import br.com.kutuar.financeiro.services.MensalidadeService;
import br.com.kutuar.financeiro.services.ParcelamentoService;
import br.com.kutuar.financeiro.services.RelatorioFinanceiroService;
import br.com.kutuar.seguranca.controllers.AuthController;
import br.com.kutuar.seguranca.controllers.AuditoriaController;
import br.com.kutuar.seguranca.controllers.EscolaController;
import br.com.kutuar.seguranca.controllers.UsuarioAdminController;
import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.enums.Permissao;
import br.com.kutuar.seguranca.exceptions.AuthenticationException;
import br.com.kutuar.seguranca.exceptions.AuthorizationException;
import br.com.kutuar.seguranca.exceptions.ConflictException;
import br.com.kutuar.seguranca.exceptions.NotFoundException;
import br.com.kutuar.seguranca.exceptions.ValidationException;
import br.com.kutuar.seguranca.middlewares.AuthMiddleware;
import br.com.kutuar.seguranca.middlewares.AuthorizationMiddleware;
import br.com.kutuar.seguranca.middlewares.RoleBasedMiddleware;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.models.Escola;
import br.com.kutuar.seguranca.models.Usuario;
import br.com.kutuar.seguranca.repositories.EscolaRepository;
import br.com.kutuar.seguranca.repositories.AuditoriaRepository;
import br.com.kutuar.seguranca.repositories.UsuarioRepository;
import br.com.kutuar.seguranca.services.AuditoriaPersistenteService;
import br.com.kutuar.seguranca.services.AuditoriaService;
import br.com.kutuar.seguranca.services.AuthService;
import br.com.kutuar.seguranca.services.EmailService;
import br.com.kutuar.seguranca.services.EscolaService;
import br.com.kutuar.seguranca.services.JwtService;
import br.com.kutuar.seguranca.services.PasswordService;
import br.com.kutuar.seguranca.services.UsuarioAdminService;
import br.com.kutuar.seguranca.strategies.ValidadorCpf;
import br.com.kutuar.seguranca.utils.AuthUserContext;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

public class KutuarApp {

    private static final Logger logger = LoggerFactory.getLogger(KutuarApp.class);

    public static void main(String[] args) throws SQLException {
        logger.info("Iniciando Kutuar Educação...");

        try {
            DatabaseConfig.init();
            System.out.println("Banco inicializado!");
            FlywayConfig.migrate();
            logger.info("Banco de dados inicializado com sucesso.");
        } catch (Exception e) {
            logger.error("Falha crítica ao inicializar banco/migrations. Encerrando aplicação.", e);
            System.exit(1);
        }

        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

        // 1. Inicialização de Repositories e dependências básicas
        TemplateEngine templateEngine = createTemplateEngine();
        UsuarioRepository usuarioRepository = new UsuarioRepository();
        EscolaRepository escolaRepository = new EscolaRepository();

        PasswordService passwordService = new PasswordService();
        JwtService jwtService = new JwtService();
        DashboardRepository dashboardRepository = new DashboardRepository();
        // Acadêmico
        DisciplinaRepository disciplinaRepository = new DisciplinaRepository();
        AnoLetivoRepository anoLetivoRepository = new AnoLetivoRepository();
        AnoLetivoCloneRepository cloneRepository = new AnoLetivoCloneRepository();
        SerieRepository serieRepository = new SerieRepository();
        SerieDisciplinaRepository serieDisciplinaRepository = new SerieDisciplinaRepository();
        TurmaRepository turmaRepository = new TurmaRepository();
        TurmaDisciplinaProfessorRepository tdpRepository = new TurmaDisciplinaProfessorRepository();
        ProfessorRepository professorRepository = new ProfessorRepository();
        AlunoRepository alunoRepository = new AlunoRepository();
        MatriculaRepository matriculaRepository = new MatriculaRepository();
        DocumentoAlunoRepository documentoRepository = new DocumentoAlunoRepository();
        HistoricoSituacaoAlunoRepository historicoRepository = new HistoricoSituacaoAlunoRepository();
        AvaliacaoRepository avaliacaoRepository = new AvaliacaoRepository();
        NotaRepository notaRepository = new NotaRepository();
        AvaliacaoService avaliacaoService = new AvaliacaoService(avaliacaoRepository);
        AvaliacaoController avaliacaoController = new AvaliacaoController(avaliacaoService);
        // CORRIGIDO: Removido FrequenciaRepository

        // 2. Inicialização dos Services
        DashboardService dashboardService = new DashboardService(dashboardRepository);
        EmailService emailService = new EmailService();

        AuditoriaService auditoriaService = new AuditoriaPersistenteService(new AuditoriaRepository());
        EscolaService escolaService = new EscolaService(escolaRepository, usuarioRepository, passwordService, auditoriaService);
        // Padrão Observer: registra quem deve ser notificado quando uma
        // escola nova for cadastrada (mesmo molde do alunoService.adicionarObserver
        // já usado em academico). Pra adicionar uma nova reação, basta
        // implementar EscolaCadastradaObserver e registrar aqui.
        escolaService.adicionarObserver(new br.com.kutuar.seguranca.services.observers.AuditLogEscolaObserver());
        escolaService.adicionarObserver(new br.com.kutuar.seguranca.services.observers.EmailGestorObserver(emailService));

        UsuarioAdminService usuarioAdminService = new UsuarioAdminService(usuarioRepository, auditoriaService);
        AuthService authService = new AuthService(usuarioRepository, escolaRepository, passwordService, jwtService, emailService, auditoriaService);
        // Acadêmico
        DisciplinaService disciplinaService = new DisciplinaService(disciplinaRepository);
        AnoLetivoService anoLetivoService = new AnoLetivoService(anoLetivoRepository, cloneRepository);
        SerieService serieService = new SerieService(serieRepository, anoLetivoRepository, turmaRepository);
        MatrizCurricularService matrizCurricularService = new MatrizCurricularService(serieDisciplinaRepository, serieRepository, disciplinaRepository);
        TurmaService turmaService = new TurmaService(turmaRepository, anoLetivoRepository, serieRepository);
        AlocacaoDocenteService alocacaoDocenteService = new AlocacaoDocenteService(tdpRepository, serieDisciplinaRepository, turmaRepository, professorRepository);

        ValidadorCpf validadorCpf = new ValidadorCpf();
        ProfessorService professorService = new ProfessorService(professorRepository, tdpRepository, validadorCpf);
        AlunoService alunoService = new AlunoService(alunoRepository, historicoRepository, matriculaRepository, documentoRepository, validadorCpf, turmaService);
        // Padrão Observer: quem grava o histórico de mudança de situação do aluno
        // agora é o HistoricoSituacaoObserver (o CancelarMensalidadesObserver é
        // registrado mais abaixo, assim que o MensalidadeService existir).
        alunoService.adicionarObserver(new HistoricoSituacaoObserver(historicoRepository));
        LancamentoNotasService notasService = new LancamentoNotasService(notaRepository, avaliacaoRepository, matriculaRepository, alunoRepository);
        // CORRIGIDO: Removido FrequenciaService e ajustado construtor do BoletimService
        BoletimService boletimService = new BoletimService(avaliacaoRepository, notaRepository, new CalculoMediaAritmetica());

        // 3. Inicialização dos Controllers
        AuthController authController = new AuthController(authService);
        AuditoriaController auditoriaController = new AuditoriaController(auditoriaService);
        UsuarioAdminController usuarioAdminController = new UsuarioAdminController(usuarioAdminService);
        EscolaController escolaController = new EscolaController(escolaService);
        DashboardController dashboardController = new DashboardController(dashboardService, escolaService, usuarioRepository, usuarioAdminService, templateEngine);
        SuperAdminDashboardApiController superAdminDashboardApiController = new SuperAdminDashboardApiController(dashboardService);
        // Acadêmico
        DisciplinaController disciplinaController = new DisciplinaController(disciplinaService);
        AnoLetivoController anoLetivoController = new AnoLetivoController(anoLetivoService);
        SerieController serieController = new SerieController(serieService);
        MatrizCurricularController matrizCurricularController = new MatrizCurricularController(matrizCurricularService);
        TurmaController turmaController = new TurmaController(turmaService);
        AlocacaoDocenteController alocacaoDocenteController = new AlocacaoDocenteController(alocacaoDocenteService);
        ProfessorController professorController = new ProfessorController(professorService);
        AlunoController alunoController = new AlunoController(alunoService);
        // CORRIGIDO: Construtor aceita apenas notasService e boletimService
        GestaoPedagogicaController pedagogicaController = new GestaoPedagogicaController(notasService, boletimService);
        // 1. Inicialização de Repositories
        MensalidadeRepository mensalidadeRepository = new MensalidadeRepository();
        ParcelaRepository parcelaRepository = new ParcelaRepository();
        DescontoRepository descontoRepository = new DescontoRepository();
        PagamentoRepository pagamentoRepository = new PagamentoRepository();

        // 2. Inicialização dos Services
        MensalidadeService mensalidadeService = new MensalidadeService(mensalidadeRepository, descontoRepository, pagamentoRepository, parcelaRepository);
        alunoService.adicionarObserver(new br.com.kutuar.financeiro.observers.CancelarMensalidadesObserver(mensalidadeService));
        ParcelamentoService parcelamentoService = new ParcelamentoService(parcelaRepository, mensalidadeRepository);
        InadimplenciaService inadimplenciaService = new InadimplenciaService(mensalidadeRepository);
        RelatorioFinanceiroService relatorioFinanceiroService = new RelatorioFinanceiroService(mensalidadeRepository);

        // 3. Inicialização dos Controllers
        MensalidadeController mensalidadeController = new MensalidadeController(mensalidadeService, parcelamentoService);
        RelatorioFinanceiroController relatorioFinanceiroController = new RelatorioFinanceiroController(inadimplenciaService, relatorioFinanceiroService);
// No bloco de inicialização de Services:
        AlertaService alertaService = new AlertaService(mensalidadeRepository, inadimplenciaService);

        // No bloco de inicialização de Controllers:
        AlertaController alertaController = new AlertaController(alertaService);

        // 4. Configuração do Javalin 6
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

        app.before(new AuthMiddleware(jwtService, usuarioRepository, escolaRepository));

        // Rota Ping
        app.get("/ping", ctx -> {
            ctx.status(200).json(Map.of(
                    "status", "ok",
                    "service", "eq14",
                    "timestamp", Instant.now().toString()
            ));
        });

        // Home
        app.get("/", ctx -> {
            Context context = new Context(ctx.req().getLocale());
            context.setVariables(homeModel());
            ctx.html(templateEngine.process("home", context));
        });

        // Telas de Autenticação
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
            context.setVariable("escolas", escolaRepository.findAllAtivas());
            ctx.html(templateEngine.process("auth/cadastro", context));
        });

        app.get("/esqueci-senha", ctx -> {
            Context context = new Context(ctx.req().getLocale());
            ctx.html(templateEngine.process("auth/esqueci-senha", context));
        });
        app.get("/redefinir-senha", ctx -> {
            Context context = new Context(ctx.req().getLocale());
            ctx.html(templateEngine.process("auth/redefinir-senha", context));
        });
        app.get("/hub", ctx -> {
            try {
                AuthUser currentUser = AuthUserContext.getAuthUser();

                if (currentUser == null) {
                    ctx.redirect("/login");
                    return;
                }

                Context context = new Context(ctx.req().getLocale());

                Usuario usuarioReal = usuarioRepository.findById(currentUser.getUserId())
                        .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

                Escola escola = null;
                if (currentUser.getTenantId() != null) {
                    escola = escolaRepository.findById(currentUser.getTenantId()).orElse(null);
                }

                context.setVariable("usuarioLogado", usuarioReal);
                context.setVariable("escolaNome", escola != null ? escola.getNome() : "Sua Instituição");

                // Processa o arquivo do hub diretamente (pois ele mesmo já estende o master-escola)
                ctx.html(templateEngine.process("dashboard/escolas/hub", context));
            } catch (Exception e) {
                // ANTES: esse catch engolia qualquer erro (NPE, erro de template, etc.)
                // e mandava o usuário de volta pro /login SEM NENHUMA mensagem — o login
                // parecia "não funcionar" mesmo quando a autenticação tinha dado certo.
                // Agora loga o erro de verdade, pra dar pra diagnosticar o que quebrou.
                logger.error("Erro ao renderizar /hub para o usuário logado: {}", e.getMessage(), e);
                ctx.sessionAttribute("errorMessage", "Ocorreu um erro ao carregar o painel. Tente novamente ou contate o suporte.");
                ctx.redirect("/login");
            }
        });

        // Middlewares para as rotas da UI baseados em hub.html
        RoleBasedMiddleware uiAcessoPeriodosSeries = new RoleBasedMiddleware(Perfil.SUPER_ADMIN, Perfil.GESTOR, Perfil.SECRETARIA);
        RoleBasedMiddleware uiAcessoTurmasAlunos = new RoleBasedMiddleware(Perfil.SUPER_ADMIN, Perfil.GESTOR, Perfil.SECRETARIA, Perfil.PROFESSOR);
        RoleBasedMiddleware uiAcessoBoletim = new RoleBasedMiddleware(Perfil.SUPER_ADMIN, Perfil.GESTOR, Perfil.PROFESSOR);

        app.before("/portal/anos-letivos", uiAcessoPeriodosSeries);
        app.before("/portal/series", uiAcessoPeriodosSeries);
        app.before("/portal/professores", uiAcessoPeriodosSeries);
        app.get("/portal/professores", ctx -> {
            Context context = new Context(ctx.req().getLocale());
            context.setVariable("content", "dashboard/escola/professores");
            ctx.html(templateEngine.process("dashboard/escola/professores", context));
        });
        app.before("/portal/turmas", uiAcessoTurmasAlunos);
        app.before("/portal/alunos", uiAcessoTurmasAlunos);
        app.before("/portal/notas", uiAcessoBoletim);

        // Rotas UI do Portal da Escola (Dashboard Gestor)
        app.get("/portal/anos-letivos", ctx -> {
            // 1. Obter o usuário logado
            AuthUser currentUser = AuthUserContext.getAuthUser();
            if (currentUser == null) {
                ctx.redirect("/login");
                return;
            }

            // 2. Determinar a permissão de edição no backend
            Perfil perfil = currentUser.getPerfil();
            boolean canEdit = (perfil == Perfil.GESTOR || perfil == Perfil.SUPER_ADMIN);

            // 3. Preparar o contexto para o Thymeleaf
            Context context = new Context(ctx.req().getLocale());
            context.setVariable("currentUser", currentUser);
            context.setVariable("canEdit", canEdit); // <-- Variável booleana injetada aqui!

            // Opcional: o "content" é parte de um layout, mantemos como está
            context.setVariable("content", "dashboard/academico/anos-letivos/index");

            // 4. Renderizar o template
            ctx.html(templateEngine.process("dashboard/academico/anos-letivos/index", context));
        });

        app.get("/portal/disciplinas", ctx -> {
            Context context = new Context(ctx.req().getLocale());
            context.setVariable("content", "dashboard/academico/disciplinas/index");
            ctx.html(templateEngine.process("dashboard/academico/disciplinas/index", context));
        });

        app.get("/portal/series", ctx -> {
            Context context = new Context(ctx.req().getLocale());
            context.setVariable("content", "dashboard/academico/series/index");
            ctx.html(templateEngine.process("dashboard/academico/series/index", context));
        });

        app.get("/portal/series/novo", ctx -> {
            Context context = new Context(ctx.req().getLocale());
            context.setVariable("content", "dashboard/academico/series/novo");
            ctx.html(templateEngine.process("dashboard/academico/series/novo", context));
        });

        app.get("/portal/series/editar", ctx -> {
            Context context = new Context(ctx.req().getLocale());
            context.setVariable("content", "dashboard/academico/series/editar");
            ctx.html(templateEngine.process("dashboard/academico/series/editar", context));
        });

        app.get("/portal/turmas", ctx -> {
            Context context = new Context(ctx.req().getLocale());
            AuthUser currentUser = AuthUserContext.getAuthUser();

            // CORREÇÃO: Adicionando lógica de permissão
            boolean canEdit = false;
            if (currentUser != null) {
                Perfil perfil = currentUser.getPerfil();
                canEdit = (perfil == Perfil.GESTOR || perfil == Perfil.SUPER_ADMIN || perfil == Perfil.SECRETARIA);
            }
            context.setVariable("canEdit", canEdit);

            context.setVariable("currentUser", currentUser);
            context.setVariable("content", "dashboard/academico/turmas/index");
            ctx.html(templateEngine.process("dashboard/academico/turmas/index", context));
        });


        app.get("/portal/turmas/novo", ctx -> {
            Context context = new Context(ctx.req().getLocale());
            AuthUser currentUser = AuthUserContext.getAuthUser();
            context.setVariable("currentUser", currentUser);
            context.setVariable("content", "dashboard/academico/turmas/novo");
            ctx.html(templateEngine.process("dashboard/academico/turmas/novo", context));
        });

        app.get("/portal/turmas/editar", ctx -> {
            Context context = new Context(ctx.req().getLocale());
            AuthUser currentUser = AuthUserContext.getAuthUser();
            context.setVariable("currentUser", currentUser);
            context.setVariable("content", "dashboard/academico/turmas/editar");
            ctx.html(templateEngine.process("dashboard/academico/turmas/editar", context));
        });

        app.get("/portal/turmas/visualizar", ctx -> {
            Context context = new Context(ctx.req().getLocale());
            AuthUser currentUser = AuthUserContext.getAuthUser();
            context.setVariable("currentUser", currentUser);
            context.setVariable("content", "dashboard/academico/turmas/visualizar");
            ctx.html(templateEngine.process("dashboard/academico/turmas/visualizar", context));
        });

        app.get("/portal/alunos", ctx -> {
            Context context = new Context(ctx.req().getLocale());
            context.setVariable("content", "dashboard/escola/alunos");
            ctx.html(templateEngine.process("dashboard/escola/alunos", context));
        });

        app.get("/portal/notas", ctx -> {
            Context context = new Context(ctx.req().getLocale());
            context.setVariable("content", "dashboard/escola/notas");
            ctx.html(templateEngine.process("dashboard/escola/notas", context));
        });

        app.get("/portal/perfil", ctx -> {
            AuthUser currentUser = AuthUserContext.getAuthUser();
            if (currentUser == null) {
                ctx.redirect("/login");
                return;
            }
            Context context = new Context(ctx.req().getLocale());
            context.setVariable("content", "portal/perfil");
            Usuario usuarioReal = usuarioRepository.findById(currentUser.getUserId())
                    .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));
            context.setVariable("usuarioLogado", usuarioReal);
            ctx.html(templateEngine.process("portal/perfil", context));
        });

        // Endpoints de Ações de Autenticação
        app.post("/auth/forgot-password", authController::forgotPassword);
        app.post("/auth/login", authController::login);
        app.post("/auth/super-admin/login", authController::superAdminLogin);
        app.post("/auth/register", authController::register);
        app.post("/auth/logout", authController::logout);
        app.post("/auth/reset-password", authController::resetPassword);
        app.patch("/auth/change-password", authController::changePassword);
        app.patch("/perfil/senha", authController::changePassword);

        // Recursos de administracao global exigem a permissao indicada e SUPER_ADMIN.
        // O perfil adicional preserva a natureza global dessas operacoes ate que exista RBAC por escopo.
        AuthorizationMiddleware dashboardGlobal = new AuthorizationMiddleware(Permissao.AUDITORIA_VISUALIZAR, Perfil.SUPER_ADMIN);
        AuthorizationMiddleware escolaVisualizarGlobal = new AuthorizationMiddleware(Permissao.ESCOLA_VISUALIZAR, Perfil.SUPER_ADMIN);
        AuthorizationMiddleware escolaCriarGlobal = new AuthorizationMiddleware(Permissao.ESCOLA_CRIAR, Perfil.SUPER_ADMIN);
        AuthorizationMiddleware escolaEditarGlobal = new AuthorizationMiddleware(Permissao.ESCOLA_EDITAR, Perfil.SUPER_ADMIN);
        AuthorizationMiddleware escolaBloquearGlobal = new AuthorizationMiddleware(Permissao.ESCOLA_BLOQUEAR, Perfil.SUPER_ADMIN);
        AuthorizationMiddleware usuarioVisualizarGlobal = new AuthorizationMiddleware(Permissao.USUARIO_VISUALIZAR, Perfil.SUPER_ADMIN);
        AuthorizationMiddleware usuarioCriarGlobal = new AuthorizationMiddleware(Permissao.USUARIO_CRIAR, Perfil.SUPER_ADMIN);
        AuthorizationMiddleware usuarioEditarGlobal = new AuthorizationMiddleware(Permissao.USUARIO_EDITAR, Perfil.SUPER_ADMIN);
        AuthorizationMiddleware usuarioBloquearGlobal = new AuthorizationMiddleware(Permissao.USUARIO_BLOQUEAR, Perfil.SUPER_ADMIN);
        AuthorizationMiddleware usuarioAprovarGlobal = new AuthorizationMiddleware(Permissao.USUARIO_APROVAR, Perfil.SUPER_ADMIN);

// ... antes do bloco de rotas ...
        // ATENÇÃO: não usamos mais um único "/api/academico/*" bloqueando PROFESSOR de tudo.
        // Antes disso, o professor não conseguia nem lançar a própria nota. Agora cada área
        // acadêmica tem sua própria regra de acesso:

        AuthorizationMiddleware alunoVisualizar = new AuthorizationMiddleware(Permissao.ALUNO_VISUALIZAR);
        AuthorizationMiddleware alunoCriar = new AuthorizationMiddleware(Permissao.ALUNO_CRIAR);
        AuthorizationMiddleware alunoEditar = new AuthorizationMiddleware(Permissao.ALUNO_EDITAR);
        AuthorizationMiddleware matriculaVisualizar = new AuthorizationMiddleware(Permissao.MATRICULA_VISUALIZAR);
        AuthorizationMiddleware matriculaCriar = new AuthorizationMiddleware(Permissao.MATRICULA_CRIAR);
        AuthorizationMiddleware matriculaEditar = new AuthorizationMiddleware(Permissao.MATRICULA_EDITAR);
        AuthorizationMiddleware turmaVisualizar = new AuthorizationMiddleware(Permissao.TURMA_VISUALIZAR);
        AuthorizationMiddleware turmaGerenciar = new AuthorizationMiddleware(Permissao.TURMA_GERENCIAR);
        AuthorizationMiddleware notaVisualizar = new AuthorizationMiddleware(Permissao.NOTA_VISUALIZAR);
        AuthorizationMiddleware notaLancar = new AuthorizationMiddleware(Permissao.NOTA_LANCAR);
        AuthorizationMiddleware financeiroVisualizar = new AuthorizationMiddleware(Permissao.FINANCEIRO_VISUALIZAR);
        AuthorizationMiddleware financeiroGerenciar = new AuthorizationMiddleware(Permissao.FINANCEIRO_GERENCIAR);

        // Áreas de uso do professor no dia a dia: avaliações, notas/recuperação/simulador,
        // boletim e a própria grade de aulas. Secretaria/Gestor/Super Admin continuam com acesso total a essas também.
        // Áreas administrativas/estruturais (matrícula, turma, matriz curricular, disciplinas,
        // séries, ano letivo, alocação docente, CRUD de professor): só quem organiza a escola.
        // SOLUÇÃO DEFINITIVA: Mapeamento linear direto na instância 'app' (Livre de erros de versão do Javalin)
        app.post("/api/academico/avaliacoes", notaLancar.then(avaliacaoController::criar));
        app.get("/api/academico/avaliacoes", notaVisualizar.then(avaliacaoController::listar));
        app.get("/api/academico/avaliacoes/{id}", notaVisualizar.then(avaliacaoController::obterPorId));
        app.put("/api/academico/avaliacoes/{id}", notaLancar.then(avaliacaoController::atualizar));
        app.delete("/api/academico/avaliacoes/{id}", notaLancar.then(avaliacaoController::remover));

        // Novas rotas da Gestão Pedagógica (Recuperação e Simulador) mapeadas de forma direta
        app.post("/api/academico/notas", notaLancar.then(pedagogicaController::lancarNota));
        app.get("/api/academico/notas/recuperacao", notaVisualizar.then(pedagogicaController::recuperacao));
        app.get("/api/academico/notas/recuperacao/{mediaAtual}/nota-necessaria", notaVisualizar.then(pedagogicaController::notaNecessaria));
        app.post("/api/academico/notas/simulador", notaVisualizar.then(pedagogicaController::simulador));
        app.get("/api/academico/boletins", notaVisualizar.then(pedagogicaController::gerarBoletim));

        // Dashboard Home
        app.get("/dashboard", dashboardGlobal.then(dashboardController::dashboard));
        app.get("/dashboard/escolas", escolaVisualizarGlobal.then(escolaController::exibirPaginaListagem));
        app.get("/dashboard/escolas/nova", escolaCriarGlobal.then(dashboardController::novaEscola));
        app.get("/dashboard/escolas/editar/{id}", escolaEditarGlobal.then(dashboardController::editarEscola));
        app.get("/dashboard/escolas/visualizar/{id}", escolaVisualizarGlobal.then(escolaController::exibirPaginaVisualizar));
        app.post("/dashboard/escolas/{id}/deletar", escolaBloquearGlobal.then(escolaController::deletarEscola));
        app.get("/dashboard/auditoria", dashboardGlobal.then(dashboardController::auditoria));

        // Gestão de Usuários
        app.get("/dashboard/usuarios", usuarioVisualizarGlobal.then(dashboardController::usuarios));
        app.get("/dashboard/usuarios/novo", usuarioCriarGlobal.then(dashboardController::novoUsuario));
        app.get("/dashboard/usuarios/editar/{id}", usuarioEditarGlobal.then(dashboardController::editarUsuario));
        app.post("/dashboard/usuarios/editar/{id}", usuarioEditarGlobal.then(dashboardController::salvarEditarUsuario));
        app.get("/dashboard/usuarios/visualizar/{id}", usuarioVisualizarGlobal.then(dashboardController::visualizarUsuario));
        app.post("/dashboard/usuarios/{id}/deletar", usuarioBloquearGlobal.then(usuarioAdminController::deletar));

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

        // API de Usuários
        app.get("/users", usuarioVisualizarGlobal.then(usuarioAdminController::listar));
        app.get("/users/{id}", usuarioVisualizarGlobal.then(usuarioAdminController::buscarPorId));
        app.put("/users/{id}", usuarioEditarGlobal.then(usuarioAdminController::atualizar));
        app.delete("/users/{id}", usuarioBloquearGlobal.then(usuarioAdminController::inativar));
        app.patch("/users/{id}/approve", usuarioAprovarGlobal.then(usuarioAdminController::aprovar));
        app.patch("/users/{id}/unlock", usuarioBloquearGlobal.then(usuarioAdminController::desbloquear));
        app.post("/users/{id}/unlock", usuarioBloquearGlobal.then(usuarioAdminController::desbloquear));
        app.patch("/users/{id}/reativar", usuarioAprovarGlobal.then(usuarioAdminController::reativar));
        app.post("/users/{id}/reativar", usuarioAprovarGlobal.then(usuarioAdminController::reativar));
        app.delete("/users/{id}/excluir", usuarioBloquearGlobal.then(usuarioAdminController::excluir));
        app.patch("/users/{id}/profile", usuarioEditarGlobal.then(usuarioAdminController::alterarPerfil));
        app.post("/users/{id}/inativar", usuarioBloquearGlobal.then(usuarioAdminController::inativar));
        app.post("/users/{id}/approve", usuarioAprovarGlobal.then(usuarioAdminController::aprovar));
        // API de Escolas
        app.post("/escolas", escolaCriarGlobal.then(escolaController::criarEscola));
        app.patch("/escolas/{id}", escolaEditarGlobal.then(escolaController::atualizarEscola));
        app.post("/escolas/{id}", escolaEditarGlobal.then(escolaController::atualizarEscola));
        app.get("/escolas", escolaVisualizarGlobal.then(escolaController::listarEscolas));
        app.get("/escolas/ativas", escolaVisualizarGlobal.then(escolaController::listarEscolasAtivas));
        app.get("/escolas/inativas", escolaVisualizarGlobal.then(escolaController::listarEscolasInativas));
        app.get("/escolas/{id}", escolaVisualizarGlobal.then(escolaController::obterEscola));
        app.get("/escolas/cnpj/{cnpj}", escolaVisualizarGlobal.then(escolaController::buscarPorCnpj));
        app.patch("/escolas/{id}/ativar", escolaBloquearGlobal.then(escolaController::ativarEscola));
        app.post("/escolas/{id}/ativar", escolaBloquearGlobal.then(escolaController::ativarEscola));
        app.patch("/escolas/{id}/inativar", escolaBloquearGlobal.then(escolaController::inativarEscola));
        app.post("/escolas/{id}/inativar", escolaBloquearGlobal.then(escolaController::inativarEscola));
        app.delete("/escolas/{id}/excluir", escolaBloquearGlobal.then(escolaController::excluirEscola));

        app.get("/api/escolas", escolaVisualizarGlobal.then(escolaController::listarEscolas));
        app.get("/api/escolas/{id}", escolaVisualizarGlobal.then(escolaController::obterEscola));

        // ACADÊMICO — DISCIPLINAS
        app.get("/api/academico/disciplinas", turmaVisualizar.then(disciplinaController::listar));
        app.get("/api/academico/disciplinas/{id}", turmaVisualizar.then(disciplinaController::obter));
        app.post("/api/academico/disciplinas", turmaGerenciar.then(disciplinaController::criar));
        app.put("/api/academico/disciplinas/{id}", turmaGerenciar.then(disciplinaController::atualizar));
        app.delete("/api/academico/disciplinas/{id}", turmaGerenciar.then(disciplinaController::remover));

        // ACADÊMICO — ANO LETIVO / SÉRIES / MATRIZ
        app.get("/api/academico/anos-letivos", turmaVisualizar.then(anoLetivoController::listar));
        app.post("/api/academico/anos-letivos", turmaGerenciar.then(anoLetivoController::criar));
        app.put("/api/academico/anos-letivos/{id}", turmaGerenciar.then(anoLetivoController::editar));
        app.delete("/api/academico/anos-letivos/{id}", turmaGerenciar.then(anoLetivoController::apagar));
        app.patch("/api/academico/anos-letivos/{id}/arquivar", turmaGerenciar.then(anoLetivoController::arquivar));
        app.patch("/api/academico/anos-letivos/{id}/definir-ativo", turmaGerenciar.then(anoLetivoController::definirAtivo));
        app.get("/api/academico/anos-letivos/historico", turmaVisualizar.then(anoLetivoController::historico));
        app.post("/api/academico/anos-letivos/{id}/clonar-para/{destinoId}", turmaGerenciar.then(anoLetivoController::clonar));

        // Séries
        app.get("/api/academico/series", turmaVisualizar.then(serieController::listarPorAno));
        app.get("/api/academico/series/{id}", turmaVisualizar.then(serieController::obter));
        app.post("/api/academico/series", turmaGerenciar.then(serieController::criar));
        app.put("/api/academico/series/{id}", turmaGerenciar.then(serieController::atualizar));
        app.delete("/api/academico/series/{id}", turmaGerenciar.then(serieController::remover));

        // Matriz Curricular
        app.get("/api/academico/series/{idSerie}/matriz", turmaVisualizar.then(matrizCurricularController::listar));
        app.post("/api/academico/series/{idSerie}/matriz", turmaGerenciar.then(matrizCurricularController::definir));
        app.delete("/api/academico/series/{idSerie}/matriz/{idDisciplina}", turmaGerenciar.then(matrizCurricularController::remover));

        // ACADÊMICO — TURMAS
        app.get("/api/academico/turmas", turmaVisualizar.then(turmaController::listar));
        app.post("/api/academico/turmas", turmaGerenciar.then(turmaController::criar));
        app.get("/api/academico/turmas/{id}", turmaVisualizar.then(turmaController::buscarPorId));
        app.put("/api/academico/turmas/{id}", turmaGerenciar.then(turmaController::editar));
        app.delete("/api/academico/turmas/{id}", turmaGerenciar.then(turmaController::apagar));
        app.patch("/api/academico/turmas/{id}/encerrar", turmaGerenciar.then(turmaController::encerrar));
        app.get("/api/academico/turmas/{id}/capacidade", turmaVisualizar.then(turmaController::capacidade));

        // Atribuição Docente
        app.post("/api/academico/turmas/{idTurma}/docentes", turmaGerenciar.then(alocacaoDocenteController::atribuir));
        app.delete("/api/academico/turmas/{idTurma}/docentes/{idDisciplina}/{idProfessor}", turmaGerenciar.then(alocacaoDocenteController::remover));

        // Professores
        app.get("/api/academico/professores", turmaVisualizar.then(professorController::listar));
        app.post("/api/academico/professores", turmaGerenciar.then(professorController::criar));
        app.get("/api/academico/professores/{id}", turmaVisualizar.then(professorController::obterPorId));
        app.put("/api/academico/professores/{id}", turmaGerenciar.then(professorController::atualizar));
        app.delete("/api/academico/professores/{id}", turmaGerenciar.then(professorController::inativar));
        app.get("/api/academico/professores/{id}/grade", turmaVisualizar.then(professorController::consultarGrade));

        // Alunos
        app.get("/api/academico/alunos", alunoVisualizar.then(alunoController::listar));
        app.post("/api/academico/alunos", alunoCriar.then(alunoController::criar));
        app.get("/api/academico/alunos/{id}", alunoVisualizar.then(alunoController::obterPorId));
        app.put("/api/academico/alunos/{id}", alunoEditar.then(alunoController::atualizar));
        app.patch("/api/academico/alunos/{id}/situacao", alunoEditar.then(alunoController::alterarSituacao));
        app.post("/api/academico/alunos/{id}/matriculas", matriculaCriar.then(alunoController::matricular));
        app.post("/api/academico/alunos/{id}/transferencias", matriculaEditar.then(alunoController::transferir));
        app.get("/api/academico/alunos/{id}/documentos", alunoVisualizar.then(alunoController::listarDocumentos));
        app.post("/api/academico/alunos/{id}/documentos", alunoEditar.then(alunoController::adicionarDocumento));
        app.get("/api/academico/alunos/{id}/historico-escolar", matriculaVisualizar.then(alunoController::emitirHistoricoEscolar));

        // Gestão Pedagógica (Apenas Notas e Boletins)

        // MÓDULO FINANCEIRO — PROTEÇÃO POR PERFIL
        // Rotas de Mensalidades e Transações
        app.post("/api/financeiro/mensalidades", financeiroGerenciar.then(mensalidadeController::cadastrar));
        app.post("/api/financeiro/mensalidades/descontos", financeiroGerenciar.then(mensalidadeController::aplicarDesconto));
        app.post("/api/financeiro/mensalidades/pagamentos", financeiroGerenciar.then(mensalidadeController::registrarPagamento));
        app.post("/api/financeiro/mensalidades/parcelar", financeiroGerenciar.then(mensalidadeController::parcelar));
        app.get("/api/financeiro/mensalidades/aluno/{idAluno}", financeiroVisualizar.then(mensalidadeController::listarPorAluno));

        // Rotas de Relatórios, Fluxo de Caixa e Inadimplência
        app.get("/api/financeiro/relatorios/devedores", financeiroVisualizar.then(relatorioFinanceiroController::listarDevedores));
        app.get("/api/financeiro/relatorios/previsao-fluxo", financeiroVisualizar.then(relatorioFinanceiroController::obterPrevisaoEFluxo));
        // SISTEMA DE ALERTAS
        app.get("/api/alertas", financeiroVisualizar.then(alertaController::obterAlertas));

        app.get("/api/admin/escolas", escolaVisualizarGlobal.then(escolaController::listarEscolasAdmin));
        app.get("/api/admin/escolas/{id}/exclusao/impacto", escolaBloquearGlobal.then(escolaController::analisarImpactoExclusao));
        app.get("/api/admin/dashboard", dashboardGlobal.then(superAdminDashboardApiController::dashboard));
        app.get("/api/admin/dashboard/alertas", dashboardGlobal.then(superAdminDashboardApiController::alertas));
        app.get("/api/admin/dashboard/atividades", dashboardGlobal.then(superAdminDashboardApiController::atividades));
        app.get("/api/admin/dashboard/ultimos-acessos", dashboardGlobal.then(superAdminDashboardApiController::ultimosAcessos));
        app.get("/api/admin/auditoria", new RoleBasedMiddleware(Perfil.SUPER_ADMIN).then(auditoriaController::listar));


        // TRATAMENTO DE EXCEÇÕES
        app.exception(AuthenticationException.class, (e, ctx) -> {
            if (isJsonEndpoint(ctx.path())) {
                ctx.status(401).json(Map.of("message", e.getMessage()));
            } else {
                ctx.redirect("/super-admin/login");
            }
        });

        app.exception(NotFoundException.class, (e, ctx) -> {
            ctx.status(404);
            if (isJsonEndpoint(ctx.path())) {
                ctx.json(Map.of("message", e.getMessage()));
            } else {
                Context thymeleafContext = new Context();
                thymeleafContext.setVariable("message", e.getMessage());
                ctx.html(templateEngine.process("errors/404", thymeleafContext));
            }
        });

        app.exception(AuthorizationException.class, (e, ctx) -> {
            ctx.status(403);
            if (isJsonEndpoint(ctx.path())) {
                ctx.json(Map.of("message", e.getMessage()));
            } else {
                Context thymeleafContext = new Context();
                thymeleafContext.setVariable("message", e.getMessage());
                ctx.html(templateEngine.process("errors/403", thymeleafContext));
            }
        });

        app.exception(ValidationException.class, (e, ctx) -> {
            ctx.status(400);
            ctx.json(Map.of("message", e.getMessage()));
        });

        app.exception(ConflictException.class, (e, ctx) -> {
            ctx.status(409);
            ctx.json(Map.of("message", e.getMessage()));
        });

        app.exception(Exception.class, (e, ctx) -> {
            ctx.status(500);
            String message = "Ocorreu um erro interno inesperado.";
            if (isJsonEndpoint(ctx.path())) {
                ctx.json(Map.of("message", message));
            } else {
                Context thymeleafContext = new Context();
                thymeleafContext.setVariable("message", message);
                ctx.html(templateEngine.process("errors/500", thymeleafContext));
            }
        });

        app.start(port);
        logger.info("Servidor iniciado na porta {}", port);
    }

    private static boolean isJsonEndpoint(String path) {
        return path.startsWith("/api/") || path.startsWith("/users") || path.startsWith("/escolas");
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
                        Map.of("icon", "bi-mortarboard", "title", "Gestão Acadêmica", "items", List.of("Alunos", "Professores", "Notas", "Boletins")),
                        Map.of("icon", "bi-cash-coin", "title", "Gestão Financeira", "items", List.of("Mensalidades", "Inadimplência", "Fluxo de caixa", "Parcelamentos")),
                        Map.of("icon", "bi-chat-dots", "title", "Comunicação", "items", List.of("Avisos", "Notificações", "Comunicados")),
                        Map.of("icon", "bi-bar-chart", "title", "Relatórios Inteligentes", "items", List.of("Indicadores", "Gráficos", "Exportação em PDF")),
                        Map.of("icon", "bi-shield-lock", "title", "Segurança", "items", List.of("Criptografia", "Logs", "Backup", "Permissões"))
                ),
                "modules", List.of(
                        Map.of("icon", "bi-journal-check", "title", "Acadêmico", "slug", "academico", "description", "Controle matrículas, turmas, notas e boletins em uma rotina integrada."),
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
                        Map.of("photo", "/images/avatar-ana.svg", "name", "Ana Ribeiro", "role", "Diretora Escolar", "comment", "A Kutuar Educação reduziu retrabalho e deu visibilidade diária para a nossa equipe pedagógica e financeira."),
                        Map.of("photo", "/images/avatar-marcos.svg", "name", "Marcos Lima", "role", "Coordenador Administrativo", "comment", "A centralização dos dados tornou a gestão mais rápida, organizada e confiável para todos os setores."),
                        Map.of("photo", "/images/avatar-julia.svg", "name", "Júlia Costa", "role", "Gestora Financeira", "comment", "Hoje acompanhamos recebíveis, inadimplência e indicadores em poucos minutos, sem planilhas paralelas.")
                )
        );
    }
}
