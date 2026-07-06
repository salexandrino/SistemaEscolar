package br.com.synge.seguranca.controllers;

import br.com.synge.seguranca.dtos.ApproveUserDTO;
import br.com.synge.seguranca.dtos.ForgotPasswordDTO;
import br.com.synge.seguranca.dtos.LoginDTO;
import br.com.synge.seguranca.dtos.RegisterDTO;
import br.com.synge.seguranca.dtos.RegisterResponseDTO;
import br.com.synge.seguranca.dtos.ResetPasswordDTO;
import br.com.synge.seguranca.exceptions.AuthenticationException;
import br.com.synge.seguranca.exceptions.AuthorizationException;
import br.com.synge.seguranca.exceptions.BusinessException;
import br.com.synge.seguranca.exceptions.ConflictException;
import br.com.synge.seguranca.exceptions.InternalServerException;
import br.com.synge.seguranca.exceptions.NotFoundException;
import br.com.synge.seguranca.exceptions.ValidationException;
import br.com.synge.seguranca.models.AuthUser;
import br.com.synge.seguranca.models.Usuario;
import br.com.synge.seguranca.services.AuthService;
import br.com.synge.seguranca.utils.AuthUserContext;
import br.com.synge.seguranca.utils.CookieUtil;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public void showLoginPage(Context ctx) {
        ctx.render("auth/login.html", Map.of("errorMessage", ctx.sessionAttribute("errorMessage") != null ? ctx.sessionAttribute("errorMessage") : ""));
        ctx.sessionAttribute("errorMessage", null);
    }

    public void login(Context ctx) {
        try {
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setCpf(ctx.formParam("cpf"));
            loginDTO.setSenha(ctx.formParam("senha"));

            if (loginDTO.getCpf() == null || loginDTO.getCpf().isBlank()) {
                throw new ValidationException("O CPF não pode estar em branco.");
            }
            if (loginDTO.getSenha() == null || loginDTO.getSenha().isBlank()) {
                throw new ValidationException("A senha não pode estar em branco.");
            }

            String jwt = authService.autenticar(loginDTO.getCpf(), loginDTO.getSenha());
            CookieUtil.addJwtCookie(ctx, jwt);
            ctx.redirect("/");

            logger.info("Usuário {} logado com sucesso.", loginDTO.getCpf().replaceAll("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", "***.***.***-**"));

        } catch (ValidationException | AuthenticationException e) {
            ctx.sessionAttribute("errorMessage", e.getMessage());
            ctx.redirect("/login");
            logger.warn("Falha na tentativa de login: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado durante o login convencional", e);
            ctx.sessionAttribute("errorMessage", "Ocorreu um erro interno inesperado no sistema. Tente novamente mais tarde.");
            ctx.redirect("/login");
        }
    }

    public void logout(Context ctx) {
        CookieUtil.removeJwtCookie(ctx);
        ctx.redirect("/login");
        logger.info("Usuário efetuou logout do sistema.");
    }

    public void superAdminLogin(Context ctx) {
        String email = ctx.formParam("email");
        try {
            String senha = ctx.formParam("password");

            // Executa a autenticação no service
            String jwt = authService.autenticarSuperAdmin(email, senha);
            CookieUtil.addJwtCookie(ctx, jwt);

            // CORREÇÃO: Adicionado log de auditoria seguro para o Super Admin
            logger.info("SuperAdmin [{}] logado com sucesso no painel administrativo.", email);
            ctx.redirect("/dashboard");

        } catch (Exception e) {
            // CORREÇÃO CRÍTICA: Removido o e.printStackTrace() para evitar vazamento de senhas e adicionado log seguro
            logger.warn("Tentativa falha de login como SuperAdmin com o email: {}. Motivo: {}", email, e.getMessage());

            ctx.sessionAttribute("errorMessage", e.getMessage());
            ctx.redirect("/super-admin/login");
        }
    }

    public void register(Context ctx) {
        try {
            Usuario usuario;

            // Se a requisição vier do formulário HTML convencional, preenche o modelo manualmente
            if (ctx.formParamMap() != null && !ctx.formParamMap().isEmpty()) {
                usuario = new Usuario();
                usuario.setNomeCompleto(ctx.formParam("nomeCompleto"));
                usuario.setEmail(ctx.formParam("email"));
                usuario.setCpf(ctx.formParam("cpf"));
                usuario.setTelefone(ctx.formParam("telefone"));

                // 🔐 CAPTURA DA SENHA E DA CONFIRMAÇÃO DE SENHA:
                String senha = ctx.formParam("senha");
                usuario.setSenhaHash(senha);

                String confirmacao = ctx.formParam("confirmacaoSenha");
                usuario.setConfirmacaoSenha(confirmacao); // 🌟 FALTAVA EXATAMENTE ESTA LINHA!

                // Mapeia o Perfil selecionado (PROFESSOR, GESTOR, SECRETARIA)
                String perfilParam = ctx.formParam("perfil");
                if (perfilParam != null && !perfilParam.isBlank()) {
                    try {
                        usuario.setPerfil(br.com.synge.seguranca.enums.Perfil.valueOf(perfilParam));
                    } catch (IllegalArgumentException e) {
                        logger.warn("Perfil inválido recebido do formulário: {}", perfilParam);
                    }
                }

                // Mapeia a Escola selecionada
                String escolaIdParam = ctx.formParam("escolaId");
                if (escolaIdParam != null && !escolaIdParam.isBlank()) {
                    usuario.setEscolaId(UUID.fromString(escolaIdParam));
                }

                // Se houver lógica de Tenant herdada do administrador logado
                AuthUser currentUser = AuthUserContext.getAuthUser();
                if (currentUser != null) {
                    usuario.setTenantId(currentUser.getTenantId());
                }

            } else {
                // Mantém o comportamento original caso a requisição venha via JSON (Postman/API REST)
                usuario = ctx.bodyAsClass(Usuario.class);
            }

            // Executa a sua regra de negócio existente do AuthService
            authService.register(usuario);

            // REDIRECIONAMENTO SEGURO: Se o usuário foi cadastrado pela tela, volta para a listagem
            if (ctx.formParamMap() != null && !ctx.formParamMap().isEmpty()) {
                ctx.redirect("/dashboard/usuarios");
                return;
            }

            ctx.status(HttpStatus.CREATED);
            ctx.json(Map.of("message", "Usuário cadastrado com sucesso."));

        } catch (br.com.synge.seguranca.exceptions.ValidationException |
                 br.com.synge.seguranca.exceptions.ConflictException e) {
            responderErro(ctx, HttpStatus.BAD_REQUEST, e.getMessage());
            logger.warn("Aviso de negócio ao registrar usuário: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erro crítico e inesperado durante o registro de usuário", e);
            responderErro(ctx, HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro interno inesperado no sistema. Tente novamente mais tarde.");
        }
    }

    private void responderErro(Context ctx, HttpStatus status, String message) {
        ctx.status(status);
        ctx.json(Map.of("message", message));
    }
    public void approveUser(Context ctx) {
        try {
            UUID userId = UUID.fromString(ctx.pathParam("id"));
            AuthUser currentUser = AuthUserContext.getAuthUser();

            if (currentUser == null) {
                throw new AuthenticationException("Usuário não autenticado.");
            }

            authService.approveUser(userId, currentUser.getTenantId(), currentUser);

            ctx.status(HttpStatus.OK);
            ctx.json(Map.of("message", "Usuário aprovado com sucesso."));
            logger.info("Usuário [ID: {}] foi aprovado com sucesso no sistema.", userId);
        } catch (AuthenticationException | AuthorizationException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Bloqueio de autorização na aprovação de usuário: {}", e.getMessage());
        } catch (NotFoundException | BusinessException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Regra de negócio violada na aprovação de usuário: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(Map.of("message", "ID de usuário inválido."));
            logger.warn("Formato de UUID inválido enviado na aprovação: {}", ctx.pathParam("id"));
        } catch (Exception e) {
            logger.error("Erro crítico inesperado durante aprovação de usuário", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("message", "Ocorreu um erro interno inesperado no sistema. Tente novamente mais tarde."));
        }
    }

    public void forgotPassword(Context ctx) {
        try {
            ForgotPasswordDTO forgotPasswordDTO = ctx.bodyAsClass(ForgotPasswordDTO.class);

            if (forgotPasswordDTO.getEmail() == null || forgotPasswordDTO.getEmail().isBlank()) {
                throw new ValidationException("O e-mail não pode estar em branco.");
            }

            authService.forgotPassword(forgotPasswordDTO.getEmail());

            ctx.status(HttpStatus.OK);
            ctx.json(Map.of("message", "Se existir uma conta vinculada a este e-mail, um código de recuperação foi enviado."));
            logger.info("Solicitação de recuperação de senha registrada para o e-mail informado.");
        } catch (ValidationException e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Falha ao solicitar recuperação de senha: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado na solicitação de recuperação de senha", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("message", "Ocorreu um erro interno inesperado no sistema. Tente novamente mais tarde."));
        }
    }

    public void showForgotPasswordPage(Context ctx) {
        ctx.render("auth/esqueci-senha.html");
    }

    public void resetPassword(Context ctx) {
        try {
            ResetPasswordDTO resetPasswordDTO = ctx.bodyAsClass(ResetPasswordDTO.class);

            authService.resetPassword(
                    resetPasswordDTO.getEmail(),
                    resetPasswordDTO.getCodigo(),
                    resetPasswordDTO.getNovaSenha(),
                    resetPasswordDTO.getConfirmacaoSenha()
            );

            ctx.status(HttpStatus.OK);
            ctx.json(Map.of("message", "Senha redefinida com sucesso."));
            logger.info("Senha redefinida com sucesso para o usuário correspondente.");
        } catch (ValidationException | NotFoundException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Falha ao executar redefinição de senha: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erro crítico inesperado durante a redefinição de senha", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("message", "Ocorreu um erro interno inesperado no sistema. Tente novamente mais tarde."));
        }
    }
}