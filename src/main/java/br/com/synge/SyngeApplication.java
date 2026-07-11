package br.com.synge;

import br.com.synge.administrativo.controllers.DashboardController;
import br.com.synge.administrativo.repositories.DashboardRepository;
import br.com.synge.administrativo.services.DashboardService;
import br.com.synge.academico.controllers.DisciplinaController;
import br.com.synge.academico.controllers.AnoLetivoController;
import br.com.synge.academico.controllers.SerieController;
import br.com.synge.academico.controllers.MatrizCurricularController;
import br.com.synge.academico.controllers.TurmaController;
import br.com.synge.academico.controllers.AlocacaoDocenteController;
import br.com.synge.academico.controllers.ProfessorController;
import br.com.synge.academico.controllers.AlunoController;
import br.com.synge.academico.controllers.GestaoPedagogicaController;
import br.com.synge.academico.repositories.DisciplinaRepository;
import br.com.synge.academico.repositories.AnoLetivoRepository;
import br.com.synge.academico.repositories.SerieRepository;
import br.com.synge.academico.repositories.SerieDisciplinaRepository;
import br.com.synge.academico.repositories.TurmaRepository;
import br.com.synge.academico.repositories.AnoLetivoCloneRepository;
import br.com.synge.academico.repositories.TurmaDisciplinaProfessorRepository;
import br.com.synge.academico.repositories.ProfessorRepository;
import br.com.synge.academico.repositories.AlunoRepository;
import br.com.synge.academico.repositories.MatriculaRepository;
import br.com.synge.academico.repositories.DocumentoAlunoRepository;
import br.com.synge.academico.repositories.HistoricoSituacaoAlunoRepository;
import br.com.synge.academico.repositories.AvaliacaoRepository;
import br.com.synge.academico.repositories.NotaRepository;
import br.com.synge.academico.repositories.FrequenciaRepository;
import br.com.synge.academico.services.DisciplinaService;
import br.com.synge.academico.services.AnoLetivoService;
import br.com.synge.academico.services.SerieService;
import br.com.synge.academico.services.MatrizCurricularService;
import br.com.synge.academico.services.TurmaService;
import br.com.synge.academico.services.AlocacaoDocenteService;
import br.com.synge.academico.services.ProfessorService;
import br.com.synge.academico.services.AlunoService;
import br.com.synge.academico.services.LancamentoNotasService;
import br.com.synge.academico.services.FrequenciaService;
import br.com.synge.academico.services.BoletimService;
import br.com.synge.academico.services.media.CalculoMediaAritmetica;
import br.com.synge.seguranca.controllers.EscolaDashboardController;
import br.com.synge.seguranca.exceptions.AuthenticationException;
import br.com.synge.seguranca.exceptions.NotFoundException;
import br.com.synge.seguranca.exceptions.AuthorizationException;
import br.com.synge.seguranca.exceptions.ValidationException;
import br.com.synge.seguranca.exceptions.ConflictException;
import br.com.synge.seguranca.middlewares.AuthMiddleware;
import br.com.synge.seguranca.middlewares.RoleBasedMiddleware;
import br.com.synge.seguranca.middlewares.SuperAdminMiddleware;
import br.com.synge.seguranca.models.AuthUser;
import br.com.synge.seguranca.models.Escola;
import br.com.synge.seguranca.models.Usuario;
import br.com.synge.seguranca.repositories.UsuarioRepository;
import br.com.synge.seguranca.repositories.EscolaRepository;
import br.com.synge.seguranca.services.PasswordService;
import br.com.synge.seguranca.services.JwtService;
import br.com.synge.seguranca.enums.Perfil;
import br.com.synge.config.DatabaseConfig;
import br.com.synge.config.FlywayConfig;
import br.com.synge.seguranca.controllers.AuthController;
import br.com.synge.seguranca.controllers.EscolaController;
import br.com.synge.seguranca.controllers.UsuarioAdminController;
import br.com.synge.seguranca.services.AuthService;
import br.com.synge.seguranca.services.EscolaService;
import br.com.synge.seguranca.services.UsuarioAdminService;
import br.com.synge.seguranca.strategies.ValidadorCpf;
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

public class SyngeApplication {

    private static final Logger logger = LoggerFactory.getLogger(SyngeApplication.class);

    public static void main(String[] args) throws SQLException {
        logger.info("Iniciando SYNGE...");

        try {
            DatabaseConfig.init();
            System.out.println("Banco inicializado!");
            // Deixa o FlywayConfig (ajustado com baseline 0) cuidar de tudo de forma limpa!
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
        FrequenciaRepository frequenciaRepository = new FrequenciaRepository();

        // 2. Inicialização dos Services
        DashboardService dashboardService = new DashboardService(dashboardRepository);
        EscolaService escolaService = new EscolaService(escolaRepository);
        UsuarioAdminService usuarioAdminService = new UsuarioAdminService(usuarioRepository);
        AuthService authService = new AuthService(usuarioRepository, escolaRepository, passwordService, jwtService);
        // Acadêmico
        DisciplinaService disciplinaService = new DisciplinaService(disciplinaRepository);
        AnoLetivoService anoLetivoService = new AnoLetivoService(anoLetivoRepository, cloneRepository);
        SerieService serieService = new SerieService(serieRepository, anoLetivoRepository);
        MatrizCurricularService matrizCurricularService = new MatrizCurricularService(serieDisciplinaRepository, serieRepository, disciplinaRepository);
        TurmaService turmaService = new TurmaService(turmaRepository, anoLetivoRepository, serieRepository);
        AlocacaoDocenteService alocacaoDocenteService = new AlocacaoDocenteService(tdpRepository, serieDisciplinaRepository, turmaRepository, professorRepository);
        
        ValidadorCpf validadorCpf = new ValidadorCpf();
        ProfessorService professorService = new ProfessorService(professorRepository, tdpRepository, validadorCpf);
        AlunoService alunoService = new AlunoService(alunoRepository, historicoRepository, matriculaRepository, documentoRepository, validadorCpf, turmaService);
        LancamentoNotasService notasService = new LancamentoNotasService(notaRepository, avaliacaoRepository, matriculaRepository);
        FrequenciaService frequenciaService = new FrequenciaService(frequenciaRepository, matriculaRepository);
        BoletimService boletimService = new BoletimService(avaliacaoRepository, notaRepository, frequenciaRepository, new CalculoMediaAritmetica());

        // 3. Inicialização dos Controllers
        AuthController authController = new AuthController(authService);
        UsuarioAdminController usuarioAdminController = new UsuarioAdminController(usuarioAdminService);
        EscolaController escolaController = new EscolaController(escolaService);
        DashboardController dashboardController = new DashboardController(dashboardService, escolaService, usuarioRepository, templateEngine);
        // Acadêmico
        DisciplinaController disciplinaController = new DisciplinaController(disciplinaService);
        AnoLetivoController anoLetivoController = new AnoLetivoController(anoLetivoService);
        SerieController serieController = new SerieController(serieService);
        MatrizCurricularController matrizCurricularController = new MatrizCurricularController(matrizCurricularService);
        TurmaController turmaController = new TurmaController(turmaService);
        AlocacaoDocenteController alocacaoDocenteController = new AlocacaoDocenteController(alocacaoDocenteService);
        ProfessorController professorController = new ProfessorController(professorService);
        AlunoController alunoController = new AlunoController(alunoService);
        GestaoPedagogicaController pedagogicaController = new GestaoPedagogicaController(notasService, frequenciaService, boletimService);

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

        // Middleware de Autenticação Global (Apenas detecta o token, não bloqueia rotas)
        app.before(new AuthMiddleware(jwtService));

        // =================================================================
        // ROTAS PÚBLICAS (NÃO REQUEREM LOGIN)
        // =================================================================

        // Rota Ping (Exigência do Professor - Mantida 100% pública)
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
            // Antes mandava uma lista vazia fixa — agora busca as escolas ativas de
            // verdade, pra aparecerem no dropdown do formulário de auto-cadastro.
            context.setVariable("escolas", escolaRepository.findAllAtivas());
            ctx.html(templateEngine.process("auth/cadastro", context));
        });

        app.get("/esqueci-senha", ctx -> {
            Context context = new Context(ctx.req().getLocale());
            ctx.html(templateEngine.process("auth/esqueci-senha", context));
        });

        // Endpoints de Ações de Autenticação
        app.post("/auth/forgot-password", authController::forgotPassword);
        app.post("/auth/login", authController::login);
        app.post("/auth/super-admin/login", authController::superAdminLogin);
        app.post("/auth/register", authController::register);
        app.post("/auth/logout", authController::logout);
        app.post("/auth/reset-password", authController::resetPassword);


        // =================================================================
        // FILTROS DE SEGURANÇA (BLOQUEIO DE ÁREA RESTRITA)
        // =================================================================
        app.before("/dashboard", superAdminAuth);
        app.before("/dashboard/*", superAdminAuth);

        // Proteção por papéis para APIs acadêmicas
        app.before("/api/academico/*", new RoleBasedMiddleware(Perfil.SUPER_ADMIN, Perfil.GESTOR, Perfil.SECRETARIA));

        // =================================================================
        // ROTAS PROTEGIDAS (REQUEREM LOGIN DE SUPER ADMIN)
        // =================================================================

        // Dashboard Home
        app.get("/dashboard", dashboardController::dashboard);

        // Gestão de Escolas (Páginas HTML)
        app.get("/dashboard/escolas", escolaController::exibirPaginaListagem);
        app.get("/dashboard/escolas/nova", dashboardController::novaEscola);
        app.get("/dashboard/escolas/editar/{id}", dashboardController::editarEscola);
        app.get("/dashboard/escolas/visualizar/{id}", escolaController::exibirPaginaVisualizar);
// --- GESTÃO DE USUÁRIOS ---
        app.get("/dashboard/usuarios", dashboardController::usuarios);
        app.get("/dashboard/usuarios/novo", dashboardController::novoUsuario);
        app.get("/dashboard/usuarios/editar/{id}", dashboardController::editarUsuario);
        app.post("/dashboard/usuarios/editar/{id}", dashboardController::salvarEditarUsuario);
        app.get("/dashboard/usuarios/visualizar/{id}", dashboardController::visualizarUsuario);
        // =================================================================
        // APIs E ROTAS DE CONTEXTO
        // =================================================================
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

        // API de Usuários (JSON / Formulários HTML)
        app.get("/users", usuarioAdminController::listar);
        app.get("/users/{id}", usuarioAdminController::buscarPorId);
        app.put("/users/{id}", usuarioAdminController::atualizar);
        app.delete("/users/{id}", usuarioAdminController::inativar);
        app.patch("/users/{id}/approve", usuarioAdminController::aprovar);
        app.patch("/users/{id}/profile", usuarioAdminController::alterarPerfil);

// 🔥 ADICIONE ESTAS ROTAS ABAIXO PARA SUPORTAR OS BOTÕES DO DASHBOARD:
        app.post("/users/{id}/inativar", usuarioAdminController::inativar);
        app.post("/users/{id}/approve", usuarioAdminController::aprovar);

        // =================================================================
// APIs E ROTAS DE CONTEXTO (No SyngeApplication.java)
// =================================================================

// ... (outras rotas) ...

// API de Escolas (Suporta requisições JSON da API E formulários HTML do Dashboard)
        app.post("/escolas", escolaController::criarEscola);
        app.patch("/escolas/{id}", escolaController::atualizarEscola);
        app.post("/escolas/{id}", escolaController::atualizarEscola);
        app.get("/escolas", escolaController::listarEscolas);
        app.get("/escolas/ativas", escolaController::listarEscolasAtivas);
        app.get("/escolas/inativas", escolaController::listarEscolasInativas);
        app.get("/escolas/{id}", escolaController::obterEscola);
        app.get("/escolas/cnpj/{cnpj}", escolaController::buscarPorCnpj);

// 🟢 Rotas de Ativação (Compatível com API e com cliques de botões HTML)
        app.patch("/escolas/{id}/ativar", escolaController::ativarEscola);
        app.post("/escolas/{id}/ativar", escolaController::ativarEscola);     // 🔥 Aceita o clique de ativar do HTML

// 🔴 Rotas de Inativação (Compatível com API e com cliques de botões HTML)
        app.patch("/escolas/{id}/inativar", escolaController::inativarEscola);
        app.post("/escolas/{id}/inativar", escolaController::inativarEscola);   // 🔥 Aceita o clique de inativar do HTML

        app.get("/api/escolas", escolaController::listarEscolas);
        app.get("/api/escolas/{id}", escolaController::obterEscola);

        // =================================================================
        // ACADÊMICO — DISCIPLINAS (APIs)
        // =================================================================
        app.get("/api/academico/disciplinas", disciplinaController::listar);
        app.get("/api/academico/disciplinas/{id}", disciplinaController::obter);
        app.post("/api/academico/disciplinas", disciplinaController::criar);
        app.put("/api/academico/disciplinas/{id}", disciplinaController::atualizar);
        app.delete("/api/academico/disciplinas/{id}", disciplinaController::remover);

        // =================================================================
        // ACADÊMICO — ANO LETIVO / SÉRIES / MATRIZ (APIs)
        // =================================================================
        // Anos Letivos
        app.get("/api/academico/anos-letivos", anoLetivoController::listar);
        app.post("/api/academico/anos-letivos", anoLetivoController::criar);
        app.patch("/api/academico/anos-letivos/{id}/arquivar", anoLetivoController::arquivar);
        app.patch("/api/academico/anos-letivos/{id}/definir-ativo", anoLetivoController::definirAtivo);
        app.get("/api/academico/anos-letivos/historico", anoLetivoController::historico);
        app.post("/api/academico/anos-letivos/{id}/clonar-para/{destinoId}", anoLetivoController::clonar);

        // Séries
        app.get("/api/academico/series", serieController::listarPorAno); // ?idAnoLetivo=UUID
        app.get("/api/academico/series/{id}", serieController::obter);
        app.post("/api/academico/series", serieController::criar);
        app.put("/api/academico/series/{id}", serieController::atualizar);
        app.delete("/api/academico/series/{id}", serieController::remover);

        // Matriz Curricular (por Série)
        app.get("/api/academico/series/{idSerie}/matriz", matrizCurricularController::listar);
        app.post("/api/academico/series/{idSerie}/matriz", matrizCurricularController::definir);
        app.delete("/api/academico/series/{idSerie}/matriz/{idDisciplina}", matrizCurricularController::remover);

        // =================================================================
        // ACADÊMICO — TURMAS (APIs)
        // =================================================================
        app.get("/api/academico/turmas", turmaController::listar); // ?idAnoLetivo=UUID&idSerie=UUID
        app.post("/api/academico/turmas", turmaController::criar);
        app.patch("/api/academico/turmas/{id}/encerrar", turmaController::encerrar);
        app.get("/api/academico/turmas/{id}/capacidade", turmaController::capacidade);

        // Atribuição Docente na Turma
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

        // Gestão Pedagógica (Notas, Frequências, Boletins)
        app.post("/api/academico/notas", pedagogicaController::lancarNota);
        app.post("/api/academico/frequencias", pedagogicaController::registrarFrequencia);
        app.get("/api/academico/boletins", pedagogicaController::gerarBoletim);
        // =================================================================
        // TRATAMENTO DE EXCEÇÕES
        // =================================================================
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