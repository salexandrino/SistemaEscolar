package br.com.synge.seguranca.middlewares;

import br.com.synge.seguranca.models.AuthUser;
import br.com.synge.seguranca.services.JwtService;
import br.com.synge.seguranca.utils.AuthUserContext;
import br.com.synge.seguranca.utils.CookieUtil;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Middleware que tenta autenticar a partir do cookie JWT.
 * Se o token for válido, popula AuthUserContext e ctx.attribute("currentUser").
 * Se o token for inválido/expirado, apenas remove o cookie e permite a requisição continuar.
 * A responsabilidade de bloquear/lançar AuthenticationException fica nas rotas protegidas.
 */
public class AuthMiddleware implements Handler {

    private static final Logger logger = LoggerFactory.getLogger(AuthMiddleware.class);
    private final JwtService jwtService;

    public AuthMiddleware(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void handle(@NotNull Context ctx) {
        // Limpa contexto sempre no início da requisição
        AuthUserContext.clear();

        String token = CookieUtil.getJwtToken(ctx);

        if (token != null) {
            try {
                AuthUser authUser = jwtService.extrairAuthUser(token);
                if (authUser != null) {
                    AuthUserContext.setAuthUser(authUser);
                    ctx.attribute("currentUser", authUser); // Disponibiliza para templates e handlers
                    logger.debug("Usuário autenticado no contexto: userId={}, tenantId={}, perfil={}",
                            authUser.getUserId(), authUser.getTenantId(), authUser.getPerfil());
                } else {
                    // Token inválido ou expirado: apenas limpar cookie e continuar para rotas públicas
                    logger.debug("Token JWT inválido ou expirado. Removendo cookie e permitindo navegação pública.");
                    CookieUtil.removeJwtCookie(ctx);
                }
            } catch (Exception e) {
                // Em caso de erro inesperado no parsing do token, limpar cookie e continuar
                logger.warn("Falha ao processar token JWT: {}. Removendo cookie.", e.getMessage());
                CookieUtil.removeJwtCookie(ctx);
            }
        }

        // Não lança AuthenticationException aqui — rotas protegidas devem verificar AuthUserContext.
    }
}