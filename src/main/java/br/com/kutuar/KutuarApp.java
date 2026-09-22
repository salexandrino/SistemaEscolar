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
import br.com.kutuar.seguranca.controllers.EscolaController;
import br.com.kutuar.seguranca.controllers.UsuarioAdminController;
import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.exceptions.AuthenticationException;
import br.com.kutuar.seguranca.exceptions.AuthorizationException;
import br.com.kutuar.seguranca.exceptions.ConflictException;
import br.com.kutuar.seguranca.exceptions.NotFoundException;
import br.com.kutuar.seguranca.exceptions.ValidationException;
import br.com.kutuar.seguranca.middlewares.AuthMiddleware;
import br.com.kutuar.seguranca.middlewares.RoleBasedMiddleware;
import br.com.kutuar.seguranca.middlewares.SuperAdminMiddleware;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.models.Escola;
import br.com.kutuar.seguranca.models.Usuario;
import br.com.kutuar.seguranca.repositories.EscolaRepository;
import br.com.kutuar.seguranca.repositories.UsuarioRepository;
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
        SuperAdminMiddleware superAdminAuth = new SuperAdminMiddleware();
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

        EscolaService escolaService = new EscolaService(escolaRepository, usuarioRepository, passwordService);
        // Padrão Observer: registra quem deve ser notificado quando uma
        // escola nova for cadastrada (mesmo molde do alunoService.adicionarObserver
        // já usado em academico). Pra adicionar uma nova reação, basta
        // implementar EscolaCadastradaObserver e registrar aqui.
        escolaService.adicionarObserver(new br.com.kutuar.seguranca.services.observers.AuditLogEscolaObserver());
        escolaService.adicionarObserver(new br.com.kutuar.seguranca.services.observers.EmailGestorObserver(emailService));

        UsuarioAdminService usuarioAdminService = new UsuarioAdminService(usuarioRepository);
        AuthService authService = new AuthService(usuarioRepository, escolaRepository, passwordService, jwtService, emailService);
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
        UsuarioAdminController usuarioAdminController = new UsuarioAdminController(usuarioAdminService);
        EscolaController escolaController = new EscolaController(escolaService);
        DashboardController dashboardController = new DashboardController(dashboardService, escolaService, usuarioRepository, templateEngine);
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

        app.before(new AuthMiddleware(jwtService));

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

        app.before("/dashboard", superAdminAuth);
        app.before("/dashboard/*", superAdminAuth);

// ... antes do bloco de rotas ...
        // ATENÇÃO: não usamos mais um único "/api/academico/*" bloqueando PROFESSOR de tudo.
        // Antes disso, o professor não conseguia nem lançar a própria nota. Agora cada área
        // acadêmica tem sua própria regra de acesso:

        // Áreas de uso do professor no dia a dia: avaliações, notas/recuperação/simulador,
        // boletim e a própria grade de aulas. Secretaria/Gestor/Super Admin continuam com acesso total a essas também.
        RoleBasedMiddleware academicoDocente = new RoleBasedMiddleware(Perfil.SUPER_ADMIN, Perfil.GESTOR, Perfil.SECRETARIA, Perfil.PROFESSOR);
        app.before("/api/academico/avaliacoes", academicoDocente);
        app.before("/api/academico/avaliacoes/*", academicoDocente);
        app.before("/api/academico/notas", academicoDocente);
        app.before("/api/academico/notas/*", academicoDocente);
        app.before("/api/academico/boletins", academicoDocente);
        app.before("/api/academico/professores/{id}/grade", academicoDocente);

        // Áreas administrativas/estruturais (matrícula, turma, matriz curricular, disciplinas,
        // séries, ano letivo, alocação docente, CRUD de professor): só quem organiza a escola.
        RoleBasedMiddleware academicoAdministrativo = new RoleBasedMiddleware(Perfil.SUPER_ADMIN, Perfil.GESTOR, Perfil.SECRETARIA);
        app.before("/api/academico/disciplinas", academicoAdministrativo);
        app.before("/api/academico/disciplinas/*", academicoAdministrativo);
        app.before("/api/academico/anos-letivos", academicoAdministrativo);
        app.before("/api/academico/anos-letivos/*", academicoAdministrativo);
        app.before("/api/academico/series", academicoAdministrativo);
        app.before("/api/academico/series/*", academicoAdministrativo);
        app.before("/api/academico/turmas", academicoAdministrativo);
        app.before("/api/academico/turmas/*", academicoAdministrativo);
        app.before("/api/academico/professores", academicoAdministrativo);
        app.before("/api/academico/professores/{id}", academicoAdministrativo);
        app.before("/api/academico/alunos", academicoAdministrativo);
        app.before("/api/academico/alunos/*", academicoAdministrativo);

        // SOLUÇÃO DEFINITIVA: Mapeamento linear direto na instância 'app' (Livre de erros de versão do Javalin)
        app.post("/api/academico/avaliacoes", avaliacaoController::criar);
        app.get("/api/academico/avaliacoes", avaliacaoController::listar);
        app.get("/api/academico/avaliacoes/{id}", avaliacaoController::obterPorId); // mude para ::obter se der erro de assinatura no controller
        app.put("/api/academico/avaliacoes/{id}", avaliacaoController::atualizar);
        app.delete("/api/academico/avaliacoes/{id}", avaliacaoController::remover);

        // Novas rotas da Gestão Pedagógica (Recuperação e Simulador) mapeadas de forma direta
        app.post("/api/academico/notas", pedagogicaController::lancarNota);
        app.get("/api/academico/notas/recuperacao", pedagogicaController::recuperacao);
        app.get("/api/academico/notas/recuperacao/{mediaAtual}/nota-necessaria", pedagogicaController::notaNecessaria);
        app.post("/api/academico/notas/simulador", pedagogicaController::simulador);
        app.get("/api/academico/boletins", pedagogicaController::gerarBoletim);

        // Dashboard Home
        app.get("/dashboard", dashboardController::dashboard);       // Gestão de Escolas
        app.get("/dashboard/escolas", escolaController::exibirPaginaListagem);
        app.get("/dashboard/escolas/nova", dashboardController::novaEscola);
        app.get("/dashboard/escolas/editar/{id}", dashboardController::editarEscola);
        app.get("/dashboard/escolas/visualizar/{id}", escolaController::exibirPaginaVisualizar);
        app.post("/dashboard/escolas/{id}/deletar", escolaController::deletarEscola);

        // Gestão de Usuários
        app.get("/dashboard/usuarios", dashboardController::usuarios);
        app.get("/dashboard/usuarios/novo", dashboardController::novoUsuario);
        app.get("/dashboard/usuarios/editar/{id}", dashboardController::editarUsuario);
        app.post("/dashboard/usuarios/editar/{id}", dashboardController::salvarEditarUsuario);
        app.get("/dashboard/usuarios/visualizar/{id}", dashboardController::visualizarUsuario);
        app.post("/dashboard/usuarios/{id}/deletar", usuarioAdminController::deletar);

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
        app.get("/users", usuarioAdminController::listar);
        app.get("/users/{id}", usuarioAdminController::buscarPorId);
        app.put("/users/{id}", usuarioAdminController::atualizar);
        app.delete("/users/{id}", usuarioAdminController::inativar);
        app.patch("/users/{id}/approve", usuarioAdminController::aprovar);
        app.patch("/users/{id}/unlock", usuarioAdminController::desbloquear);
        app.post("/users/{id}/unlock", usuarioAdminController::desbloquear);
        app.patch("/users/{id}/reativar", usuarioAdminController::reativar);
        app.post("/users/{id}/reativar", usuarioAdminController::reativar);
        app.delete("/users/{id}/excluir", usuarioAdminController::excluir);
        app.patch("/users/{id}/profile", usuarioAdminController::alterarPerfil);

        app.post("/users/{id}/inativar", usuarioAdminController::inativar);
        app.post("/users/{id}/approve", usuarioAdminController::aprovar);
        // API de Escolas
        app.post("/escolas", escolaController::criarEscola);
        app.patch("/escolas/{id}", escolaController::atualizarEscola);
        app.post("/escolas/{id}", escolaController::atualizarEscola);
        app.get("/escolas", escolaController::listarEscolas);
        app.get("/escolas/ativas", escolaController::listarEscolasAtivas);
        app.get("/escolas/inativas", escolaController::listarEscolasInativas);
        app.get("/escolas/{id}", escolaController::obterEscola);
        app.get("/escolas/cnpj/{cnpj}", escolaController::buscarPorCnpj);

        app.before("/escolas/{id}/ativar", new RoleBasedMiddleware(Perfil.SUPER_ADMIN));
        app.patch("/escolas/{id}/ativar", escolaController::ativarEscola);
        app.post("/escolas/{id}/ativar", escolaController::ativarEscola);

        app.before("/escolas/{id}/inativar", new RoleBasedMiddleware(Perfil.SUPER_ADMIN));
        app.patch("/escolas/{id}/inativar", escolaController::inativarEscola);
        app.post("/escolas/{id}/inativar", escolaController::inativarEscola);
        app.delete("/escolas/{id}/excluir", escolaController::excluirEscola);

        app.get("/api/escolas", escolaController::listarEscolas);
        app.get("/api/escolas/{id}", escolaController::obterEscola);

        // ACADÊMICO — DISCIPLINAS
        app.get("/api/academico/disciplinas", disciplinaController::listar);
        app.get("/api/academico/disciplinas/{id}", disciplinaController::obter);
        app.post("/api/academico/disciplinas", disciplinaController::criar);
        app.put("/api/academico/disciplinas/{id}", disciplinaController::atualizar);
        app.delete("/api/academico/disciplinas/{id}", disciplinaController::remover);

        // ACADÊMICO — ANO LETIVO / SÉRIES / MATRIZ
        app.get("/api/academico/anos-letivos", anoLetivoController::listar);
        app.post("/api/academico/anos-letivos", anoLetivoController::criar);
        app.put("/api/academico/anos-letivos/{id}", anoLetivoController::editar);
        app.delete("/api/academico/anos-letivos/{id}", anoLetivoController::apagar);
        app.patch("/api/academico/anos-letivos/{id}/arquivar", anoLetivoController::arquivar);
        app.patch("/api/academico/anos-letivos/{id}/definir-ativo", anoLetivoController::definirAtivo);
        app.get("/api/academico/anos-letivos/historico", anoLetivoController::historico);
        app.post("/api/academico/anos-letivos/{id}/clonar-para/{destinoId}", anoLetivoController::clonar);

        // Séries
        app.get("/api/academico/series", serieController::listarPorAno);
        app.get("/api/academico/series/{id}", serieController::obter);
        app.post("/api/academico/series", serieController::criar);
        app.put("/api/academico/series/{id}", serieController::atualizar);
        app.delete("/api/academico/series/{id}", serieController::remover);

        // Matriz Curricular
        app.get("/api/academico/series/{idSerie}/matriz", matrizCurricularController::listar);
        app.post("/api/academico/series/{idSerie}/matriz", matrizCurricularController::definir);
        app.delete("/api/academico/series/{idSerie}/matriz/{idDisciplina}", matrizCurricularController::remover);

        // ACADÊMICO — TURMAS
        app.get("/api/academico/turmas", turmaController::listar);
        app.post("/api/academico/turmas", turmaController::criar);
        app.get("/api/academico/turmas/{id}", turmaController::buscarPorId);
        app.put("/api/academico/turmas/{id}", turmaController::editar);
        app.delete("/api/academico/turmas/{id}", turmaController::apagar);
        app.patch("/api/academico/turmas/{id}/encerrar", turmaController::encerrar);
        app.get("/api/academico/turmas/{id}/capacidade", turmaController::capacidade);

        // Atribuição Docente
        app.post("/api/academico/turmas/{idTurma}/docentes", alocacaoDocenteController::atribuir);
        app.delete("/api/academico/turmas/{idTurma}/docentes/{idDisciplina}/{idProfessor}", alocacaoDocenteController::remover);

        // Professores
        app.get("/api/academico/professores", professorController::listar);
        app.post("/api/academico/professores", professorController::criar);
        app.get("/api/academico/professores/{id}", professorController::obterPorId);
        app.put("/api/academico/professores/{id}", professorController::atualizar);
        app.delete("/api/academico/professores/{id}", professorController::inativar);
        app.get("/api/academico/professores/{id}/grade", professorController::consultarGrade);

        // Alunos
        app.get("/api/academico/alunos", alunoController::listar);
        app.post("/api/academico/alunos", alunoController::criar);
        app.get("/api/academico/alunos/{id}", alunoController::obterPorId);
        app.put("/api/academico/alunos/{id}", alunoController::atualizar);
        app.patch("/api/academico/alunos/{id}/situacao", alunoController::alterarSituacao);
        app.post("/api/academico/alunos/{id}/matriculas", alunoController::matricular);
        app.post("/api/academico/alunos/{id}/transferencias", alunoController::transferir);
        app.get("/api/academico/alunos/{id}/documentos", alunoController::listarDocumentos);
        app.post("/api/academico/alunos/{id}/documentos", alunoController::adicionarDocumento);
        app.get("/api/academico/alunos/{id}/historico-escolar", alunoController::emitirHistoricoEscolar);

        // Gestão Pedagógica (Apenas Notas e Boletins)

        // MÓDULO FINANCEIRO — PROTEÇÃO POR PERFIL
        app.before("/api/financeiro/*", new RoleBasedMiddleware(Perfil.SUPER_ADMIN, Perfil.GESTOR, Perfil.FINANCEIRO));

        // Rotas de Mensalidades e Transações
        app.post("/api/financeiro/mensalidades", mensalidadeController::cadastrar);
        app.post("/api/financeiro/mensalidades/descontos", mensalidadeController::aplicarDesconto);
        app.post("/api/financeiro/mensalidades/pagamentos", mensalidadeController::registrarPagamento);
        app.post("/api/financeiro/mensalidades/parcelar", mensalidadeController::parcelar);
        app.get("/api/financeiro/mensalidades/aluno/{idAluno}", mensalidadeController::listarPorAluno);

        // Rotas de Relatórios, Fluxo de Caixa e Inadimplência
        app.get("/api/financeiro/relatorios/devedores", relatorioFinanceiroController::listarDevedores);
        app.get("/api/financeiro/relatorios/previsao-fluxo", relatorioFinanceiroController::obterPrevisaoEFluxo);
        // SISTEMA DE ALERTAS
        app.before("/api/alertas", new RoleBasedMiddleware(Perfil.SUPER_ADMIN, Perfil.GESTOR, Perfil.FINANCEIRO));
        app.get("/api/alertas", alertaController::obterAlertas);

        app.before("/api/admin/dashboard", new RoleBasedMiddleware(Perfil.SUPER_ADMIN));
        app.before("/api/admin/dashboard/*", new RoleBasedMiddleware(Perfil.SUPER_ADMIN));
        app.before("/api/admin/escolas", new RoleBasedMiddleware(Perfil.SUPER_ADMIN));
        app.get("/api/admin/escolas", escolaController::listarEscolasAdmin);
        app.get("/api/admin/dashboard", superAdminDashboardApiController::dashboard);
        app.get("/api/admin/dashboard/alertas", superAdminDashboardApiController::alertas);
        app.get("/api/admin/dashboard/atividades", superAdminDashboardApiController::atividades);
        app.get("/api/admin/dashboard/ultimos-acessos", superAdminDashboardApiController::ultimosAcessos);


        // TRATAMENTO DE EXCEÇÕES
        app.exception(AuthenticationException.class, (e, ctx) -> ctx.redirect("/super-admin/login"));

        app.exception(NotFoundException.class, (e, ctx) -> {
            ctx.status(404);
            if (ctx.path().startsWith("/api/")) {
                ctx.json(Map.of("message", e.getMessage()));
            } else {
                Context thymeleafContext = new Context();
                thymeleafContext.setVariable("message", e.getMessage());
                ctx.html(templateEngine.process("errors/404", thymeleafContext));
            }
        });

        app.exception(AuthorizationException.class, (e, ctx) -> {
            ctx.status(403);
            if (ctx.path().startsWith("/api/")) {
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
