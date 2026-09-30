package br.com.kutuar.seguranca.middlewares;

import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.exceptions.AuthenticationException;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.models.Escola;
import br.com.kutuar.seguranca.models.Usuario;
import br.com.kutuar.seguranca.repositories.EscolaRepository;
import br.com.kutuar.seguranca.repositories.UsuarioRepository;
import br.com.kutuar.seguranca.services.JwtService;
import br.com.kutuar.seguranca.utils.AuthUserContext;
import io.javalin.http.Context;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AuthMiddlewareTest {
    private final JwtService jwtService = mock(JwtService.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final EscolaRepository escolaRepository = mock(EscolaRepository.class);
    private final AuthMiddleware middleware = new AuthMiddleware(jwtService, usuarioRepository, escolaRepository);
    private final Context ctx = mock(Context.class);

    @AfterEach
    void limparContexto() {
        AuthUserContext.clear();
    }

    @Test
    void jwtEmitidoAntesDaInativacaoDeixaDeAcessarRotaProtegida() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        AuthUser token = new AuthUser(usuarioId, UUID.randomUUID(), escolaId, Perfil.GESTOR, "52998224725");
        Usuario usuario = usuario(usuarioId, escolaId);
        Escola escola = new Escola();
        escola.setId(escolaId);
        escola.setStatus("INATIVA");
        prepararCookie();
        when(jwtService.extrairAuthUser("jwt-antigo")).thenReturn(token);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(escolaRepository.findById(escolaId)).thenReturn(Optional.of(escola));

        middleware.handle(ctx);

        assertNull(AuthUserContext.getAuthUser());
        verify(ctx).cookie(any(io.javalin.http.Cookie.class));
        assertThrows(AuthenticationException.class,
                () -> new RoleBasedMiddleware(Perfil.GESTOR).handle(ctx));
    }

    @Test
    void superAdminMantemAcessoSemConsultaDeEscolaOuUsuario() throws Exception {
        AuthUser superAdmin = new AuthUser(UUID.randomUUID(), null, null, Perfil.SUPER_ADMIN, "52998224725");
        prepararCookie();
        when(jwtService.extrairAuthUser("jwt-antigo")).thenReturn(superAdmin);

        middleware.handle(ctx);

        assertDoesNotThrow(() -> new RoleBasedMiddleware(Perfil.SUPER_ADMIN).handle(ctx));
        verifyNoInteractions(usuarioRepository, escolaRepository);
    }

    private void prepararCookie() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(ctx.cookie("JWT_TOKEN")).thenReturn("jwt-antigo");
        when(ctx.req()).thenReturn(request);
        when(request.isSecure()).thenReturn(false);
    }

    private Usuario usuario(UUID usuarioId, UUID escolaId) {
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setEscolaId(escolaId);
        usuario.setAtivo(true);
        usuario.setBloqueado(false);
        return usuario;
    }
}
