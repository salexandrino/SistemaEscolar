package br.com.kutuar.seguranca.controllers;

import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.exceptions.AuthenticationException;
import br.com.kutuar.seguranca.exceptions.AuthorizationException;
import br.com.kutuar.seguranca.middlewares.RoleBasedMiddleware;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.models.EventoAuditoria;
import br.com.kutuar.seguranca.services.AuditoriaService;
import br.com.kutuar.seguranca.utils.AuthUserContext;
import io.javalin.http.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditoriaControllerTest {
    private final Context ctx = mock(Context.class);

    @AfterEach
    void limparContexto() { AuthUserContext.clear(); }

    @Test
    void auditoriaBloqueiaUsuarioNaoAutenticado() {
        AuthUserContext.clear();
        assertThrows(AuthenticationException.class,
                () -> new RoleBasedMiddleware(Perfil.SUPER_ADMIN).handle(ctx));
    }

    @Test
    void auditoriaBloqueiaPerfilDiferenteDeSuperAdmin() {
        AuthUserContext.setAuthUser(new AuthUser(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Perfil.GESTOR, "cpf"));
        assertThrows(AuthorizationException.class,
                () -> new RoleBasedMiddleware(Perfil.SUPER_ADMIN).handle(ctx));
    }

    @Test
    void superAdminListaAuditoriaMesmoSemTenant() throws Exception {
        AuditoriaService service = mock(AuditoriaService.class);
        when(service.listar()).thenReturn(List.of(new EventoAuditoria()));
        AuthUserContext.setAuthUser(new AuthUser(UUID.randomUUID(), null, null, Perfil.SUPER_ADMIN, "cpf"));
        RoleBasedMiddleware middleware = new RoleBasedMiddleware(Perfil.SUPER_ADMIN);

        assertDoesNotThrow(() -> middleware.handle(ctx));
        new AuditoriaController(service).listar(ctx);

        verify(service).listar();
        verify(ctx).status(io.javalin.http.HttpStatus.OK);
    }
}
