package br.com.kutuar.seguranca.middlewares;

import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.exceptions.AuthorizationException;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.utils.AuthUserContext;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Middleware de autorização baseado em perfis/roles específicos.
 * Mais direto que AuthorizationMiddleware para controle de acesso ao painel administrativo.
 */
public class RoleBasedMiddleware implements Handler {

    private static final Logger logger = LoggerFactory.getLogger(RoleBasedMiddleware.class);
    private final Set<Perfil> allowedRoles;

    public RoleBasedMiddleware(Perfil... allowedRoles) {
        this.allowedRoles = Set.of(allowedRoles);
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        AuthUser authUser = AuthUserContext.getAuthUser();

        if (authUser == null) {
            logger.warn("Acesso negado: Usuário não autenticado para {}.", ctx.path());
            throw new AuthorizationException("Acesso negado. Você não está autenticado.");
        }

        if (!allowedRoles.contains(authUser.getPerfil())) {
            logger.warn("Acesso negado: Usuário {} com perfil {} tentou acessar {}. Perfis permitidos: {}",
                    authUser.getCpf(), authUser.getPerfil(), ctx.path(), allowedRoles);
            throw new AuthorizationException("Acesso negado. Você não tem permissão para acessar este recurso.");
        }

        logger.debug("Acesso autorizado para usuário {} (perfil {}) em {}", 
                authUser.getCpf(), authUser.getPerfil(), ctx.path());
    }
}
