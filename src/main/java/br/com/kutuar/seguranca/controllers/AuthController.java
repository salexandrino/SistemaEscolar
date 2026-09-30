package br.com.kutuar.seguranca.controllers;

import br.com.kutuar.seguranca.dtos.ApproveUserDTO;
import br.com.kutuar.seguranca.dtos.ForgotPasswordDTO;
import br.com.kutuar.seguranca.dtos.LoginDTO;
import br.com.kutuar.seguranca.dtos.RegisterDTO;
import br.com.kutuar.seguranca.dtos.RegisterResponseDTO;
import br.com.kutuar.seguranca.dtos.ResetPasswordDTO;
import br.com.kutuar.seguranca.dtos.AlterarSenhaPropriaDTO;
import br.com.kutuar.seguranca.exceptions.AuthenticationException;
import br.com.kutuar.seguranca.exceptions.AuthorizationException;
import br.com.kutuar.seguranca.exceptions.BusinessException;
import br.com.kutuar.seguranca.exceptions.ConflictException;
import br.com.kutuar.seguranca.exceptions.InternalServerException;
import br.com.kutuar.seguranca.exceptions.NotFoundException;
import br.com.kutuar.seguranca.exceptions.ValidationException;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.models.Usuario;
import br.com.kutuar.seguranca.services.AuthService;
import br.com.kutuar.seguranca.utils.AuthUserContext;
import br.com.kutuar.seguranca.utils.CookieUtil;
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
            // CPF é um identificador e pode ser normalizado. A senha, por outro
            // lado, é comparada byte a byte pelo BCrypt e deve ser preservada.
            String cpfParam = ctx.formParam("cpf");
            String senhaParam = ctx.formParam("senha");
            loginDTO.setCpf(cpfParam != null ? cpfParam.trim() : null);
            loginDTO.setSenha(senhaParam);

            if (loginDTO.getCpf() == null || loginDTO.getCpf().isBlank()) {
                throw new ValidationException("O CPF não pode estar em branco.");
            }
            if (loginDTO.getSenha() == null || loginDTO.getSenha().isBlank()) {
                throw new ValidationException("A senha não pode estar em branco.");
            }

            String jwt = authService.autenticar(loginDTO.getCpf(), loginDTO.getSenha());
            CookieUtil.addJwtCookie(ctx, jwt);
            ctx.redirect("/hub");

        } catch (ValidationException e) {
            ctx.sessionAttribute("errorMessage", e.getMessage());
            ctx.redirect("/login");
            logger.warn("Login negado por dados de entrada inválidos.");
        } catch (AuthenticationException e) {
            ctx.sessionAttribute("errorMessage", e.getMessage());
            ctx.redirect("/login");
        } catch (Exception e) {
            logger.error("Erro inesperado durante o login convencional. tipo={}", e.getClass().getSimpleName());
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

            ctx.redirect("/dashboard");

        } catch (ValidationException e) {
            logger.warn("Login do Super Admin negado por dados de entrada inválidos.");
            ctx.sessionAttribute("errorMessage", e.getMessage());
            ctx.redirect("/super-admin/login");
        } catch (AuthenticationException e) {
            ctx.sessionAttribute("errorMessage", e.getMessage());
            ctx.redirect("/super-admin/login");
        } catch (Exception e) {
            logger.error("Erro inesperado durante login do Super Admin. tipo={}", e.getClass().getSimpleName());
            ctx.sessionAttribute("errorMessage", "Ocorreu um erro interno inesperado no sistema. Tente novamente mais tarde.");
            ctx.redirect("/super-admin/login");
        }
    }
    public void register(Context ctx) {
        try {
            Usuario usuario = new Usuario();

            // 1. Verifica se a requisição é baseada em formulário tradicional HTML (URL Encoded)
            if (ctx.contentType() != null && ctx.contentType().contains("application/x-www-form-urlencoded")) {
                usuario.setNomeCompleto(ctx.formParam("nomeCompleto") != null ? ctx.formParam("nomeCompleto") : ctx.formParam("nome"));
                usuario.setEmail(ctx.formParam("email"));
                usuario.setCpf(ctx.formParam("cpf"));
                usuario.setTelefone(ctx.formParam("telefone"));
                usuario.setSenhaHash(ctx.formParam("senha"));
                usuario.setConfirmacaoSenha(ctx.formParam("confirmacaoSenha"));

                String perfilParam = ctx.formParam("perfil");
                if (perfilParam != null && !perfilParam.isBlank()) {
                    usuario.setPerfil(br.com.kutuar.seguranca.enums.Perfil.valueOf(perfilParam));
                }

                String escolaIdParam = ctx.formParam("escolaId") != null ? ctx.formParam("escolaId") : ctx.formParam("escola");
                if (escolaIdParam != null && !escolaIdParam.isBlank()) {
                    usuario.setEscolaId(UUID.fromString(escolaIdParam));
                }
            } else {
                // 2. Se for JSON (Envio via JavaScript fetch), usa o DTO mapeado
                br.com.kutuar.seguranca.dtos.UsuarioRegistroDTO dto = ctx.bodyAsClass(br.com.kutuar.seguranca.dtos.UsuarioRegistroDTO.class);

                usuario.setNomeCompleto(dto.getNomeCompleto());
                usuario.setEmail(dto.getEmail());
                usuario.setCpf(dto.getCpf());
                usuario.setTelefone(dto.getTelefone());
                usuario.setSenhaHash(dto.getSenha());
                usuario.setConfirmacaoSenha(dto.getConfirmacaoSenha());

                if (dto.getPerfil() != null && !dto.getPerfil().isBlank()) {
                    usuario.setPerfil(br.com.kutuar.seguranca.enums.Perfil.valueOf(dto.getPerfil()));
                }
                if (dto.getEscolaId() != null && !dto.getEscolaId().isBlank()) {
                    usuario.setEscolaId(UUID.fromString(dto.getEscolaId()));
                }
            }

            try {
                AuthUser currentUser = AuthUserContext.getAuthUser();
                if (currentUser != null) {
                    usuario.setTenantId(currentUser.getTenantId());
                }
            } catch (Exception e) {
                // Auto-cadastro sem sessão ativa
            }

            // Executa a regra de negócio limpando a assinatura antiga
            authService.register(usuario);

            ctx.status(201).json(Map.of("message", "Usuário cadastrado com sucesso."));

        } catch (br.com.kutuar.seguranca.exceptions.ValidationException |
                 br.com.kutuar.seguranca.exceptions.ConflictException e) {
            ctx.status(400).json(Map.of("message", e.getMessage()));
            logger.warn("Aviso de negócio ao registrar usuário: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erro crítico e inesperado durante o registro de usuário. tipo={}", e.getClass().getSimpleName());
            ctx.status(500).json(Map.of("message", "Ocorreu um erro interno inesperado no sistema. Tente novamente mais tarde."));
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
            logger.error("Erro crítico inesperado durante aprovação de usuário. tipo={}", e.getClass().getSimpleName());
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
            logger.warn("Solicitação de recuperação de senha rejeitada por dados inválidos.");
        } catch (Exception e) {
            logger.error("Erro inesperado na solicitação de recuperação de senha. tipo={}", e.getClass().getSimpleName());
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
            logger.warn("Redefinição de senha rejeitada por validação.");
        } catch (Exception e) {
            logger.error("Erro crítico inesperado durante a redefinição de senha. tipo={}", e.getClass().getSimpleName());
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("message", "Ocorreu um erro interno inesperado no sistema. Tente novamente mais tarde."));
        }
    }

    public void changePassword(Context ctx) {
        try {
            AuthUser currentUser = AuthUserContext.getAuthUser();
            if (currentUser == null) {
                throw new AuthenticationException("Usuário não autenticado.");
            }

            AlterarSenhaPropriaDTO dto = ctx.bodyAsClass(AlterarSenhaPropriaDTO.class);

            authService.alterarSenhaPropria(currentUser.getUserId(), currentUser.getTenantId(), dto);

            ctx.status(HttpStatus.OK);
            ctx.json(Map.of("message", "Senha alterada com sucesso."));
        } catch (AuthenticationException | AuthorizationException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Alteração de senha negada por autenticação ou autorização.");
        } catch (ValidationException | NotFoundException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Alteração de senha rejeitada por validação.");
        } catch (Exception e) {
            logger.error("Erro inesperado durante a alteração de senha do usuário logado. tipo={}", e.getClass().getSimpleName());
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("message", "Ocorreu um erro interno inesperado no sistema. Tente novamente mais tarde."));
        }
    }
}
