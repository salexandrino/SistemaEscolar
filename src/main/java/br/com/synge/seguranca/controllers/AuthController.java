package br.com.synge.seguranca.controllers;

import br.com.synge.seguranca.dtos.ApproveUserDTO;
import br.com.synge.seguranca.dtos.ForgotPasswordDTO;
import br.com.synge.seguranca.dtos.LoginDTO;
import br.com.synge.seguranca.dtos.RegisterDTO;
import br.com.synge.seguranca.dtos.RegisterResponseDTO;
import br.com.synge.seguranca.dtos.ResetPasswordDTO;
import br.com.synge.seguranca.dtos.AlterarSenhaPropriaDTO;
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
            // .trim() é essencial aqui: o BCrypt compara byte a byte, então um
            // espaço em branco extra vindo de copy-paste da senha temporária
            // (mostrada na tela pro Super Admin) já é o suficiente pra dar
            // "CPF ou senha inválidos" mesmo com a senha certa.
            String cpfParam = ctx.formParam("cpf");
            String senhaParam = ctx.formParam("senha");
            loginDTO.setCpf(cpfParam != null ? cpfParam.trim() : null);
            loginDTO.setSenha(senhaParam != null ? senhaParam.trim() : null);

            if (loginDTO.getCpf() == null || loginDTO.getCpf().isBlank()) {
                throw new ValidationException("O CPF não pode estar em branco.");
            }
            if (loginDTO.getSenha() == null || loginDTO.getSenha().isBlank()) {
                throw new ValidationException("A senha não pode estar em branco.");
            }

            String jwt = authService.autenticar(loginDTO.getCpf(), loginDTO.getSenha());
            CookieUtil.addJwtCookie(ctx, jwt);
            ctx.redirect("/hub");

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
                    usuario.setPerfil(br.com.synge.seguranca.enums.Perfil.valueOf(perfilParam));
                }

                String escolaIdParam = ctx.formParam("escolaId") != null ? ctx.formParam("escolaId") : ctx.formParam("escola");
                if (escolaIdParam != null && !escolaIdParam.isBlank()) {
                    usuario.setEscolaId(UUID.fromString(escolaIdParam));
                }
            } else {
                // 2. Se for JSON (Envio via JavaScript fetch), usa o DTO mapeado
                br.com.synge.seguranca.dtos.UsuarioRegistroDTO dto = ctx.bodyAsClass(br.com.synge.seguranca.dtos.UsuarioRegistroDTO.class);

                usuario.setNomeCompleto(dto.getNomeCompleto());
                usuario.setEmail(dto.getEmail());
                usuario.setCpf(dto.getCpf());
                usuario.setTelefone(dto.getTelefone());
                usuario.setSenhaHash(dto.getSenha());
                usuario.setConfirmacaoSenha(dto.getConfirmacaoSenha());

                if (dto.getPerfil() != null && !dto.getPerfil().isBlank()) {
                    usuario.setPerfil(br.com.synge.seguranca.enums.Perfil.valueOf(dto.getPerfil()));
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

        } catch (br.com.synge.seguranca.exceptions.ValidationException |
                 br.com.synge.seguranca.exceptions.ConflictException e) {
            ctx.status(400).json(Map.of("message", e.getMessage()));
            logger.warn("Aviso de negócio ao registrar usuário: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erro crítico e inesperado durante o registro de usuário", e);
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
            logger.info("Senha alterada com sucesso para o usuário [ID: {}].", currentUser.getUserId());
        } catch (AuthenticationException | AuthorizationException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Falha de autorização na troca de senha: {}", e.getMessage());
        } catch (ValidationException | NotFoundException e) {
            ctx.status(e.getStatus());
            ctx.json(Map.of("message", e.getMessage()));
            logger.warn("Falha de validação na troca de senha: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado durante a alteração de senha do usuário logado", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
            ctx.json(Map.of("message", "Ocorreu um erro interno inesperado no sistema. Tente novamente mais tarde."));
        }
    }
}