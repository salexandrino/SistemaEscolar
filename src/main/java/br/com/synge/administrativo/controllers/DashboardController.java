package br.com.synge.administrativo.controllers;

import br.com.synge.administrativo.dto.DashboardDTO;
import br.com.synge.administrativo.services.DashboardService;
import br.com.synge.seguranca.models.AuthUser;
import br.com.synge.seguranca.models.Escola;
import br.com.synge.seguranca.models.Usuario; // IMPORTANTE: Importar o modelo de Usuário
import br.com.synge.seguranca.repositories.UsuarioRepository; // IMPORTANTE: Importar o repositório de usuários
import br.com.synge.seguranca.services.EscolaService;
import br.com.synge.seguranca.utils.AuthUserContext;
import br.com.synge.seguranca.exceptions.NotFoundException;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import java.util.Map;
import java.util.UUID;
import java.util.List;

public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;
    private final TemplateEngine templateEngine;
    private final EscolaService escolaService;

    // ALTERAÇÃO 1: Adicionar a variável do repositório/service de Usuários aqui
    private final UsuarioRepository usuarioRepository;

    // ALTERAÇÃO 1.1: Atualizar o construtor para receber o UsuarioRepository
    public DashboardController(DashboardService dashboardService, EscolaService escolaService, UsuarioRepository usuarioRepository, TemplateEngine templateEngine) {
        this.dashboardService = dashboardService;
        this.escolaService = escolaService;
        this.usuarioRepository = usuarioRepository; // Inicializa aqui
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

    public void escolas(Context ctx) {
        org.thymeleaf.context.Context thymeleaf = new org.thymeleaf.context.Context();
        thymeleaf.setVariable("content", "dashboard/escolas/index");
        ctx.html(templateEngine.process("layouts/master-admin", thymeleaf));
    }

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
            Escola escola = escolaService.buscarEscolaPorId(schoolId, currentUser);
            Map<String, Object> model = Map.of("escola", escola, "currentUser", currentUser);
            ctx.render("dashboard/escolas/editar.html", model);
        } catch (Exception e) {
            logger.error("Erro ao carregar a página de edição de escola", e);
            ctx.status(500).result("Erro interno.");
        }
    }

    public void visualizarEscola(Context ctx) {
        org.thymeleaf.context.Context thymeleaf = new org.thymeleaf.context.Context();
        thymeleaf.setVariable("content", "dashboard/escolas/visualizar");
        ctx.html(templateEngine.process("layouts/master-admin", thymeleaf));
    }

    // =========================================================================
    // --- GESTÃO DE USUÁRIOS (ALTERADO PARA CONEXÃO REAL COM O BANCO) ---
    // =========================================================================

    // ALTERAÇÃO 2: Buscar a lista real de usuários cadastrados no Banco de Dados
    public void usuarios(Context ctx) {
        org.thymeleaf.context.Context thymeleafContext = new org.thymeleaf.context.Context();

        // Puxa todos os usuários do banco (seja criado na tela ou no cadastro geral)
        List<Usuario> listaUsuarios = usuarioRepository.findAll();
        thymeleafContext.setVariable("usuarios", listaUsuarios);

        String status = ctx.queryParam("status");
        thymeleafContext.setVariable("filtroStatus", status);

        thymeleafContext.setVariable("content", "dashboard/usuarios/index");
        ctx.html(templateEngine.process("layouts/master-admin", thymeleafContext));
    }

    // 7. Cadastrar Novo Usuário (Busca as escolas reais para vincular no Select)
    public void novoUsuario(Context ctx) {
        org.thymeleaf.context.Context thymeleafContext = new org.thymeleaf.context.Context();

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
        org.thymeleaf.context.Context thymeleafContext = new org.thymeleaf.context.Context();

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
        org.thymeleaf.context.Context thymeleafContext = new org.thymeleaf.context.Context();
        try {
            // 1. Captura o ID da URL e converte para UUID
            java.util.UUID usuarioId = java.util.UUID.fromString(ctx.pathParam("id"));

            // 2. Busca o usuário existente do banco de dados reais
            br.com.synge.seguranca.models.Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new br.com.synge.seguranca.exceptions.NotFoundException("Usuário não encontrado."));

            // 3. Captura os dados enviados pelo formulário HTML (ctx.formParam)
            String nomeForm = ctx.formParam("nomeCompleto");
            String emailForm = ctx.formParam("email");
            String telefoneForm = ctx.formParam("telefone");
            String perfilForm = ctx.formParam("perfil");
            String aprovadoForm = ctx.formParam("aprovado");

            // 4. Aplica as alterações no objeto Java se os campos não vierem nulos
            if (nomeForm != null) usuario.setNomeCompleto(nomeForm);
            if (emailForm != null) usuario.setEmail(emailForm);
            if (telefoneForm != null) usuario.setTelefone(telefoneForm);

            // Tratamento do Enum do Perfil baseado no pacote correto
            if (perfilForm != null && !perfilForm.isBlank()) {
                usuario.setPerfil(br.com.synge.seguranca.enums.Perfil.valueOf(perfilForm));
            }

            // Tratamento do Boolean de aprovação (ativo/inativo)
            if (aprovadoForm != null) {
                usuario.setAprovado("true".equals(aprovadoForm));
            }

            // O PASSO CRUCIAL: Salva as alterações de fato no banco de dados e comita
            usuarioRepository.update(usuario);

            // Atualiza a sessão e força o motor de renderização a carregar os dados novos
            thymeleafContext.setVariable("usuario", usuario);

            // Redireciona de volta para a lista com os dados atualizados
            ctx.redirect("/dashboard/usuarios");

        } catch (Exception e) {
            logger.error("Erro ao salvar alterações do usuário", e);
            // Se der qualquer erro no processo, te mantém na tela de edição para não perder o que digitou
            ctx.redirect("/dashboard/usuarios/editar/" + ctx.pathParam("id"));
        }
    }

    // ALTERAÇÃO 4: Buscar o usuário real para a tela de Visualização completa
    public void visualizarUsuario(Context ctx) {
        org.thymeleaf.context.Context thymeleafContext = new org.thymeleaf.context.Context();

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