package br.com.kutuar.seguranca.middlewares;

import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.enums.Permissao;
import br.com.kutuar.seguranca.exceptions.AuthenticationException;
import br.com.kutuar.seguranca.exceptions.AuthorizationException;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.utils.AuthUserContext;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/** Autoriza uma rota a partir da permissao do perfil autenticado. */
public class AuthorizationMiddleware implements Handler {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationMiddleware.class);

    private final Permissao requiredPermission;
    private final Perfil requiredGlobalProfile;

    public AuthorizationMiddleware(Permissao requiredPermission) {
        this(requiredPermission, null);
    }

    /**
     * Use o perfil opcional apenas para recursos estritamente globais.
     * O usuario precisa satisfazer tanto o perfil quanto a permissao.
     */
    public AuthorizationMiddleware(Permissao requiredPermission, Perfil requiredGlobalProfile) {
        this.requiredPermission = Objects.requireNonNull(requiredPermission, "A permissao obrigatoria nao pode ser nula.");
        this.requiredGlobalProfile = requiredGlobalProfile;
    }

    @Override
    public void handle(@NotNull Context ctx) {
        AuthUser authUser = AuthUserContext.getAuthUser();

        if (authUser == null) {
            logger.warn("Acesso negado a {}: usuario nao autenticado.", ctx.path());
            throw new AuthenticationException("Usuario nao autenticado.");
        }

        Perfil perfil = authUser.getPerfil();
        if (perfil == null || (requiredGlobalProfile != null && perfil != requiredGlobalProfile)) {
            deny(ctx, authUser);
        }

        // A autorizacao e definida exclusivamente pelo perfil, nao por strings legadas.
        if (!perfil.hasPermission(requiredPermission)) {
            deny(ctx, authUser);
        }

        // tenantId permanece disponivel em authUser para regras futuras, sem validacao aqui.
    }

    /** Encadeia a autorizacao a um handler de rota, sem duplicar a checagem nos controllers. */
    public Handler then(Handler next) {
        Objects.requireNonNull(next, "O handler protegido nao pode ser nulo.");
        return ctx -> {
            handle(ctx);
            next.handle(ctx);
        };
    }

    private void deny(Context ctx, AuthUser authUser) {
        logger.warn("Acesso negado a {}: usuario {} com perfil {} nao possui a permissao {}.",
                ctx.path(), authUser.getCpf(), authUser.getPerfil(), requiredPermission);
        throw new AuthorizationException("Acesso negado. Voce nao tem permissao para acessar este recurso.");
    }
}
