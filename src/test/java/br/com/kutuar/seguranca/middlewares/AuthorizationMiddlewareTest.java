package br.com.kutuar.seguranca.middlewares;

import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.enums.Permissao;
import br.com.kutuar.seguranca.exceptions.AuthenticationException;
import br.com.kutuar.seguranca.exceptions.AuthorizationException;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.utils.AuthUserContext;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class AuthorizationMiddlewareTest {

    private final Context ctx = mock(Context.class);
    private final AuthorizationMiddleware middleware = new AuthorizationMiddleware(Permissao.USUARIO_CRIAR);

    @AfterEach
    void limpaUsuarioAutenticado() {
        AuthUserContext.clear();
    }

    @Test
    void semUsuarioRetorna401() {
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> middleware.handle(ctx));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void usuarioSemPermissaoRetorna403() {
        AuthUserContext.setAuthUser(usuario(Perfil.PROFESSOR));

        AuthorizationException exception = assertThrows(AuthorizationException.class, () -> middleware.handle(ctx));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void usuarioComPermissaoProssegue() {
        AuthUserContext.setAuthUser(usuario(Perfil.GESTOR));

        assertDoesNotThrow(() -> middleware.handle(ctx));
    }

    @Test
    void superAdminComPermissaoAdministrativaProssegue() {
        AuthUserContext.setAuthUser(usuario(Perfil.SUPER_ADMIN));

        assertDoesNotThrow(() -> middleware.handle(ctx));
    }

    @Test
    void rotaAdministrativaGlobalRecusaPerfilComPermissaoMasSemEscopoGlobal() {
        Handler controller = mock(Handler.class);
        Handler rotaProtegida = new AuthorizationMiddleware(Permissao.ESCOLA_VISUALIZAR, Perfil.SUPER_ADMIN)
                .then(controller);
        AuthUserContext.setAuthUser(usuario(Perfil.GESTOR));

        assertThrows(AuthorizationException.class, () -> rotaProtegida.handle(ctx));

        verifyNoInteractions(controller);
    }

    @Test
    void rotaAdministrativaGlobalChamaControllerParaSuperAdmin() throws Exception {
        Handler controller = mock(Handler.class);
        Handler rotaProtegida = new AuthorizationMiddleware(Permissao.ESCOLA_VISUALIZAR, Perfil.SUPER_ADMIN)
                .then(controller);
        AuthUserContext.setAuthUser(usuario(Perfil.SUPER_ADMIN));

        rotaProtegida.handle(ctx);

        verify(controller).handle(ctx);
    }

    private AuthUser usuario(Perfil perfil) {
        return new AuthUser(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), perfil, "52998224725");
    }
}
