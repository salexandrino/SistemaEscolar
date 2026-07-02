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
        // Mantido por compatibilidade; rota /login agora faz o mesmo (SyngeApplication)
        ctx.render("auth/login.html", Map.of("errorMessage", ctx.sessionAttribute("errorMessage") != null ? ctx.sessionAttribute("errorMessage") : ""));
        ctx.sessionAttribute("errorMessage", null); // Limpa a mensagem após exibir
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
            logger.warn("Falha no login: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado durante o login: {}", e.getMessage(), e);
            ctx.sessionAttribute("errorMessage", "Ocorreu um erro interno inesperado no sistema. Tente novamente mais tarde.");
            ctx.redirect("/login");
        }
    }

    public void logout(Context ctx) {
        CookieUtil.removeJwtCookie(ctx);
        ctx.redirect("/login");
        logger.info("Usuário deslogado.");
    }

    public void register(Context ctx) {
        try {
            RegisterDTO registerDTO = ctx.bodyAsClass(RegisterDTO.class);

            Usuario usuario = new Usuario();
            usuario.setNomeCompleto(registerDTO.getNomeCompleto());
            usuario.setEmail(registerDTO.getEmail());
            usuario.setCpf(registerDTO.getCpf());
            usuario.setTelefone(registerDTO.getTelefone());
            usuario.setSenhaHash(registerDTO.getSenha()); // Senha pura para validação e hash no service
            usuario.setConfirmacaoSenha(registerDTO.getConfirmacaoSenha()); // Campo temporário para validação
            usuario.setPerfil(registerDTO.getPerfil());
            usuario.setEscolaId(registerDTO.getEscolaId());
            // tenant_id no banco é NOT NULL; aqui usamos o mesmo valor de escolaId para manter compatibilidade
            usuario.setTenantId(registerDTO.getEscolaId());

            authService.register(usuario);

            ctx.status(HttpStatus.CREATED);
            ctx.json(new RegisterResponseDTO("Usuário cadastrado com sucesso. Aguardando aprovação."));
        } catch (ValidationException | ConflictException | NotFoundException | AuthorizationException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Falha no registro de usuário: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado durante o registro de usuário: {}", e.getMessage(), e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("message", "Ocorreu um erro interno inesperado no sistema. Tente novamente mais tarde."));
        }
    }

    public void approveUser(Context ctx) {
        try {
            UUID userId = UUID.fromString(ctx.pathParam("id"));
            AuthUser currentUser = AuthUserContext.getAuthUser(); // Obtém o usuário autenticado do contexto

            if (currentUser == null) {
                throw new AuthenticationException("Usuário não autenticado.");
            }

            authService.approveUser(userId, currentUser.getTenantId(), currentUser);

            ctx.status(HttpStatus.OK);
            ctx.json(Map.of("message", "Usuário aprovado com sucesso."));
        } catch (AuthenticationException | AuthorizationException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Falha na aprovação de usuário: {}", e.getMessage());
        } catch (NotFoundException | BusinessException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Falha na aprovação de usuário: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(Map.of("message", "ID de usuário inválido."));
            logger.warn("ID de usuário inválido na aprovação: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado durante a aprovação de usuário: {}", e.getMessage(), e);
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

        } catch (ValidationException e) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Falha na validação da recuperação de senha: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado durante a solicitação de recuperação de senha: {}", e.getMessage(), e);
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
        } catch (ValidationException | NotFoundException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Falha na redefinição de senha: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado durante a redefinição de senha: {}", e.getMessage(), e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("message", "Ocorreu um erro interno inesperado no sistema. Tente novamente mais tarde."));
        }
    }
}