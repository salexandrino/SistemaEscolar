package br.com.kutuar.seguranca.middlewares;

import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.exceptions.AuthenticationException;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.utils.AuthUserContext;
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