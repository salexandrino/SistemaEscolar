package br.com.synge.seguranca.middlewares;

import br.com.synge.seguranca.exceptions.AuthorizationException;
import br.com.synge.seguranca.models.AuthUser;
import br.com.synge.seguranca.utils.AuthUserContext;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthorizationMiddleware implements Handler {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationMiddleware.class);
    private final Set<String> requiredPermissions;

    public AuthorizationMiddleware(String... requiredPermissions) {
        this.requiredPermissions = Arrays.stream(requiredPermissions).collect(Collectors.toSet());
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        AuthUser authUser = AuthUserContext.getAuthUser();

        if (authUser == null) {
            logger.warn("Acesso negado: Usuário não autenticado para recurso que requer autorização.");
            throw new AuthorizationException("Acesso negado. Você não está autenticado.");
        }

        // SUPER_ADMIN tem acesso total, ignora outras permissões
        if (authUser.getPerfil().name().equals("SUPER_ADMIN")) {
            ctx.next();
            return;
        }

        // Verifica se o usuário possui todas as permissões necessárias
        boolean hasAllPermissions = requiredPermissions.stream()
                .allMatch(authUser::hasPermission);

        if (!hasAllPermissions) {
            logger.warn("Acesso negado: Usuário {} com perfil {} não possui as permissões necessárias: {}",
                    authUser.getCpf(), authUser.getPerfil(), requiredPermissions);
            throw new AuthorizationException("Acesso negado. Você não tem permissão para acessar este recurso.");
        }

        // Multi-Tenant: A validação do tenantId para dados específicos é responsabilidade do Service/Repository.
        // Este middleware garante que o usuário está autenticado e tem o perfil certo.

        ctx.next();
    }
}