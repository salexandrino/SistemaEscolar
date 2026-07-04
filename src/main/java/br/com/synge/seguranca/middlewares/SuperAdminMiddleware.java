package br.com.synge.seguranca.middlewares;

import br.com.synge.seguranca.enums.Perfil;
import br.com.synge.seguranca.exceptions.AuthenticationException;
import br.com.synge.seguranca.models.AuthUser;
import br.com.synge.seguranca.utils.AuthUserContext;
import io.javalin.http.Context;
import io.javalin.http.Handler;

public class SuperAdminMiddleware implements Handler {

    @Override
    public void handle(Context ctx) throws Exception {

        AuthUser usuario = AuthUserContext.getAuthUser();

        if (usuario == null) {
            throw new AuthenticationException("Usuário não autenticado.");
        }

        if (usuario.getPerfil() != Perfil.SUPER_ADMIN) {

            ctx.redirect("/super-admin/login");
            return;

        }
    }
}