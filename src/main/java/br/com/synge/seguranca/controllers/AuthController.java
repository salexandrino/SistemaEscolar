package br.com.synge.seguranca.controllers;

import br.com.synge.seguranca.dtos.LoginDTO;
import br.com.synge.seguranca.exceptions.AuthenticationException;
import br.com.synge.seguranca.exceptions.ValidationException;
import br.com.synge.seguranca.services.AuthService;
import br.com.synge.seguranca.utils.CookieUtil;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.validation.Validation;
import io.javalin.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public void showLoginPage(Context ctx) {
        ctx.render("auth/login.html", Map.of("errorMessage", ctx.sessionAttribute("errorMessage") != null ? ctx.sessionAttribute("errorMessage") : ""));
        ctx.sessionAttribute("errorMessage", null); // Limpa a mensagem após exibir
    }

    public void login(Context ctx) {
        try {
            // Validação manual do DTO (Javalin não tem @Valid como Spring)
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setCpf(ctx.formParam("cpf"));
            loginDTO.setSenha(ctx.formParam("senha"));

            // Validação de campos obrigatórios
            if (loginDTO.getCpf() == null || loginDTO.getCpf().isBlank()) {
                throw new ValidationException("O CPF não pode estar em branco.");
            }
            if (loginDTO.getSenha() == null || loginDTO.getSenha().isBlank()) {
                throw new ValidationException("A senha não pode estar em branco.");
            }

            String jwt = authService.autenticar(loginDTO.getCpf(), loginDTO.getSenha());
            CookieUtil.addJwtCookie(ctx, jwt);
            ctx.redirect("/"); // Redireciona para a página inicial após login
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
}
