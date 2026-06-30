package br.com.synge.seguranca.middlewares;

import br.com.synge.seguranca.exceptions.AuthenticationException;
import br.com.synge.seguranca.models.AuthUser;
import br.com.synge.seguranca.services.JwtService;
import br.com.synge.seguranca.utils.AuthUserContext;
import br.com.synge.seguranca.utils.CookieUtil;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthMiddleware implements Handler {

    private static final Logger logger = LoggerFactory.getLogger(AuthMiddleware.class);
    private final JwtService jwtService;

    public AuthMiddleware(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        AuthUserContext.clear(); // Limpa o contexto para cada nova requisição

        String token = CookieUtil.getJwtToken(ctx);

        if (token != null) {
            AuthUser authUser = jwtService.extrairAuthUser(token);
            if (authUser != null) {
                AuthUserContext.setAuthUser(authUser);
                ctx.attribute("currentUser", authUser); // Disponibiliza para templates e outros handlers
                logger.debug("Usuário autenticado no contexto: userId={}, tenantId={}, escolaId={}, perfil={}", authUser.getUserId(), authUser.getTenantId(), authUser.getEscolaId(), authUser.getPerfil());
            } else {
                // Token inválido ou expirado
                logger.warn("Token JWT inválido ou expirado. Redirecionando para login.");
                CookieUtil.removeJwtCookie(ctx); // Remove cookie inválido
                throw new AuthenticationException("Sessão expirada ou inválida. Faça login novamente.");
            }
        }

        ctx.next(); // Continua a cadeia de handlers
    }
}