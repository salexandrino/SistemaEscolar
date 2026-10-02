package br.com.kutuar.administrativo.controllers;

import br.com.kutuar.administrativo.dto.DashboardDTO;
import br.com.kutuar.administrativo.services.DashboardService;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.dtos.AtualizarPerfilUsuarioDTO;
import br.com.kutuar.seguranca.dtos.AtualizarUsuarioDTO;
import br.com.kutuar.seguranca.models.Escola;
import br.com.kutuar.seguranca.models.Usuario; // IMPORTANTE: Importar o modelo de Usuário
import br.com.kutuar.seguranca.repositories.UsuarioRepository; // IMPORTANTE: Importar o repositório de usuários
import br.com.kutuar.seguranca.services.EscolaService;
import br.com.kutuar.seguranca.services.UsuarioAdminService;
import br.com.kutuar.seguranca.utils.AuthUserContext;
import br.com.kutuar.seguranca.utils.PermissaoModelUtil;
import br.com.kutuar.seguranca.exceptions.NotFoundException;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;
    private final TemplateEngine templateEngine;
    private final EscolaService escolaService;

    // ALTERAÇÃO 1: Adicionar a variável do repositório/service de Usuários aqui
    private final UsuarioRepository usuarioRepository;
    private final UsuarioAdminService usuarioAdminService;

    // ALTERAÇÃO 1.1: Atualizar o construtor para receber o UsuarioRepository
    public DashboardController(DashboardService dashboardService, EscolaService escolaService, UsuarioRepository usuarioRepository, UsuarioAdminService usuarioAdminService, TemplateEngine templateEngine) {
        this.dashboardService = dashboardService;
        this.escolaService = escolaService;
        this.usuarioRepository = usuarioRepository; // Inicializa aqui
        this.usuarioAdminService = usuarioAdminService;
        this.templateEngine = templateEngine;
    }

    private org.thymeleaf.context.Context novoContexto() {
        AuthUser user = AuthUserContext.getAuthUser();
        org.thymeleaf.context.Context context = new org.thymeleaf.context.Context();
        PermissaoModelUtil.popularPermissoes(context, user);
        return context;
    }

    // 1. Tela Inicial Principal do Dashboard
    public void dashboard(Context ctx) {
        DashboardDTO dashboard = dashboardService.buscarDashboardSuperAdmin();
        org.thymeleaf.context.Context thymeleaf = novoContexto();
        thymeleaf.setVariable("dashboard", dashboard);
        thymeleaf.setVariable("content", "dashboard/index");
        ctx.html(templateEngine.process("layouts/master-admin", thymeleaf));
    }

    // ==========================================
    // ROTAS DE GESTÃO DE ESCOLAS
    // ==========================================

    public void escolas(Context ctx) {
        org.thymeleaf.context.Context thymeleaf = novoContexto();
        thymeleaf.setVariable("content", "dashboard/escolas/index");
        ctx.html(templateEngine.process("layouts/master-admin", thymeleaf));
    }

    public void novaEscola(Context ctx) {
        org.thymeleaf.context.Context thymeleaf = novoContexto();
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
            Escola escola = escolaService.buscarEscolaPorId(schoolId, currentUser);
            Map<String, Object> model = new java.util.HashMap<>();
            PermissaoModelUtil.popularPermissoes(model, currentUser);
            model.put("escola", escola);
            ctx.render("dashboard/escolas/editar.html", model);
        } catch (Exception e) {
            logger.error("Erro ao carregar a página de edição de escola", e);
            ctx.status(500).result("Erro interno.");
        }
    }

    public void visualizarEscola(Context ctx) {
        org.thymeleaf.context.Context thymeleaf = novoContexto();
        thymeleaf.setVariable("content", "dashboard/escolas/visualizar");
        ctx.html(templateEngine.process("layouts/master-admin", thymeleaf));
    }

    public void auditoria(Context ctx) {
        org.thymeleaf.context.Context thymeleaf = novoContexto();
        thymeleaf.setVariable("content", "dashboard/auditoria/index");
        ctx.html(templateEngine.process("layouts/master-admin", thymeleaf));
    }

    // =========================================================================
    // --- GESTÃO DE USUÁRIOS (ALTERADO PARA CONEXÃO REAL COM O BANCO) ---
    // =========================================================================

    // ALTERAÇÃO 2: Buscar a lista real de usuários cadastrados no Banco de Dados
    public void usuarios(Context ctx) {
        org.thymeleaf.context.Context thymeleafContext = novoContexto();

        // Puxa todos os usuários do banco (seja criado na tela ou no cadastro geral)
        List<Usuario> listaUsuarios = usuarioRepository.findAll();

        // Os filtros usam os mesmos criterios das contagens dos alertas.
        String status = ctx.queryParam("status");
        String acesso = ctx.queryParam("acesso");
        String seguranca = ctx.queryParam("seguranca");
        List<Usuario> listaFiltrada;
        switch (status == null ? "ativos" : status) {
            case "pendente" -> listaFiltrada = listaUsuarios.stream().filter(u -> !u.isAprovado()).collect(Collectors.toList());
            case "inativos", "inativa" -> listaFiltrada = listaUsuarios.stream().filter(u -> !u.isAtivo()).collect(Collectors.toList());
            case "bloqueado" -> listaFiltrada = listaUsuarios.stream().filter(Usuario::isBloqueado).collect(Collectors.toList());
            case "todos" -> listaFiltrada = listaUsuarios;
            default -> listaFiltrada = listaUsuarios.stream().filter(Usuario::isAtivo).collect(Collectors.toList());
        }
        if ("sem-acesso-recente".equals(acesso)) {
            LocalDateTime limite = LocalDateTime.now().minusDays(30);
            listaFiltrada = listaFiltrada.stream()
                    .filter(u -> u.isAtivo() && (u.getUltimoLogin() == null || u.getUltimoLogin().isBefore(limite)))
                    .collect(Collectors.toList());
        }
        if ("tentativas-login".equals(seguranca)) {
            listaFiltrada = listaFiltrada.stream().filter(u -> u.getTentativasLogin() >= 3).collect(Collectors.toList());
        }

        thymeleafContext.setVariable("usuarios", listaFiltrada);
        thymeleafContext.setVariable("filtroStatus", status);
        thymeleafContext.setVariable("filtroAcesso", acesso);
        thymeleafContext.setVariable("filtroSeguranca", seguranca);

        thymeleafContext.setVariable("content", "dashboard/usuarios/index");
        ctx.html(templateEngine.process("layouts/master-admin", thymeleafContext));
    }

    // 7. Cadastrar Novo Usuário (Busca as escolas reais para vincular no Select)
    public void novoUsuario(Context ctx) {
        org.thymeleaf.context.Context thymeleafContext = novoContexto();

        // Se sua amiga tiver um escolaRepository ou se o escolaService listar, usamos ele aqui:
        // Exemplo trazendo a lista real para o formulário de cadastro:
        AuthUser currentUser = AuthUserContext.getAuthUser();
        List<Escola> escolasReais = escolaService.listarTodas(currentUser);
        thymeleafContext.setVariable("escolas", escolasReais);

        thymeleafContext.setVariable("content", "dashboard/usuarios/novo");
        ctx.html(templateEngine.process("layouts/master-admin", thymeleafContext));
    }

    // ALTERAÇÃO 3: Buscar o usuário real pelo ID no Banco de Dados para carregar na tela de Edição
    public void editarUsuario(Context ctx) {
        org.thymeleaf.context.Context thymeleafContext = novoContexto();

        try {
            String idParam = ctx.pathParam("id");
            UUID usuarioId = UUID.fromString(idParam);

            // Busca o usuário real salvo com todos os novos campos (telefone, endereço, CEP...)
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

            thymeleafContext.setVariable("usuario", usuario);

            AuthUser currentUser = AuthUserContext.getAuthUser();
            List<Escola> escolasReais = escolaService.listarTodas(currentUser);
            thymeleafContext.setVariable("escolas", escolasReais);

            thymeleafContext.setVariable("content", "dashboard/usuarios/editar");
            ctx.html(templateEngine.process("layouts/master-admin", thymeleafContext));

        } catch (Exception e) {
            logger.error("Erro ao carregar edição de usuário", e);
            ctx.redirect("/dashboard/usuarios");
        }
    }
    // Método para processar o envio do formulário de edição (POST)
    public void salvarEditarUsuario(Context ctx) {
        org.thymeleaf.context.Context thymeleafContext = novoContexto();
        try {
            UUID usuarioId = UUID.fromString(ctx.pathParam("id"));
            AuthUser currentUser = AuthUserContext.getAuthUser();
            Usuario usuarioOriginal = usuarioAdminService.buscarPorId(usuarioId, currentUser);

            String nomeForm = ctx.formParam("nomeCompleto");
            String emailForm = ctx.formParam("email");
            String telefoneForm = ctx.formParam("telefone");
            String perfilForm = ctx.formParam("perfil");
            String aprovadoForm = ctx.formParam("aprovado");

            AtualizarUsuarioDTO dto = new AtualizarUsuarioDTO();
            dto.setNomeCompleto(nomeForm);
            dto.setEmail(emailForm);
            dto.setCpf(usuarioOriginal.getCpf());
            dto.setTelefone(telefoneForm);
            dto.setEscolaId(usuarioOriginal.getEscolaId());
            usuarioAdminService.atualizar(usuarioId, dto, currentUser);

            if (perfilForm != null && !perfilForm.isBlank()) {
                AtualizarPerfilUsuarioDTO perfilDto = new AtualizarPerfilUsuarioDTO();
                perfilDto.setPerfil(br.com.kutuar.seguranca.enums.Perfil.valueOf(perfilForm));
                usuarioAdminService.alterarPerfil(usuarioId, perfilDto, currentUser);
            }

            if (aprovadoForm != null) {
                boolean querAtivo = Boolean.parseBoolean(aprovadoForm);
                if (querAtivo && !usuarioOriginal.isAtivo()) {
                    usuarioAdminService.aprovar(usuarioId, currentUser);
                } else if (!querAtivo && usuarioOriginal.isAtivo()) {
                    usuarioAdminService.inativar(usuarioId, currentUser);
                }
            }

            ctx.redirect("/dashboard/usuarios");

        } catch (Exception e) {
            logger.error("Erro ao salvar alterações do usuário", e);
            // Se der qualquer erro no processo, te mantém na tela de edição para não perder o que digitou
            ctx.redirect("/dashboard/usuarios/editar/" + ctx.pathParam("id"));
        }
    }

    // ALTERAÇÃO 4: Buscar o usuário real para a tela de Visualização completa
    public void visualizarUsuario(Context ctx) {
        org.thymeleaf.context.Context thymeleafContext = novoContexto();

        try {
            String idParam = ctx.pathParam("id");
            UUID usuarioId = UUID.fromString(idParam);

            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

            thymeleafContext.setVariable("usuario", usuario);
            thymeleafContext.setVariable("content", "dashboard/usuarios/visualizar");
            ctx.html(templateEngine.process("layouts/master-admin", thymeleafContext));

        } catch (Exception e) {
            logger.error("Erro ao carregar visualização do usuário", e);
            ctx.redirect("/dashboard/usuarios");
        }
    }
}
