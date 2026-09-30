package br.com.kutuar.seguranca.controllers;

import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.exceptions.AuthorizationException;
import br.com.kutuar.seguranca.exceptions.ConflictException;
import br.com.kutuar.seguranca.exceptions.NotFoundException;
import br.com.kutuar.seguranca.exceptions.ValidationException;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.models.Usuario;
import br.com.kutuar.seguranca.services.UsuarioAdminService;
import br.com.kutuar.seguranca.utils.AuthUserContext;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioAdminControllerTest {

    private final UsuarioAdminService service = mock(UsuarioAdminService.class);
    private final UsuarioAdminController controller = new UsuarioAdminController(service);
    private final Context ctx = mock(Context.class);
    private final UUID usuarioId = UUID.randomUUID();
    private final AuthUser admin = new AuthUser(UUID.randomUUID(), null, null, Perfil.SUPER_ADMIN, "00000000000");

    @BeforeEach
    void setUp() {
        AuthUserContext.setAuthUser(admin);
        when(ctx.status(any(HttpStatus.class))).thenReturn(ctx);
        prepararFormulario();
    }

    @AfterEach
    void tearDown() {
        AuthUserContext.clear();
    }

    @Test
    void conflitoRetorna409() {
        doThrow(new ConflictException("E-mail ja cadastrado."))
                .when(service).atualizar(eq(usuarioId), any(), eq(admin));
        when(service.buscarPorId(usuarioId, admin)).thenReturn(usuario());

        controller.atualizar(ctx);

        verify(ctx).status(HttpStatus.CONFLICT);
        verify(ctx).json(Map.of("message", "E-mail ja cadastrado."));
    }

    @Test
    void dadosInvalidosRetornam400() {
        when(service.buscarPorId(usuarioId, admin)).thenReturn(usuario());
        doThrow(new ValidationException("Nome invalido."))
                .when(service).atualizar(eq(usuarioId), any(), eq(admin));

        controller.atualizar(ctx);

        verify(ctx).status(HttpStatus.BAD_REQUEST);
        verify(ctx).json(Map.of("message", "Nome invalido."));
    }

    @Test
    void usuarioInexistenteRetorna404() {
        doThrow(new NotFoundException("Usuario nao encontrado."))
                .when(service).buscarPorId(usuarioId, admin);

        controller.atualizar(ctx);

        verify(ctx).status(HttpStatus.NOT_FOUND);
        verify(ctx).json(Map.of("message", "Usuario nao encontrado."));
    }

    @Test
    void faltaDePermissaoRetorna403() {
        doThrow(new AuthorizationException("Acesso negado."))
                .when(service).buscarPorId(usuarioId, admin);

        controller.atualizar(ctx);

        verify(ctx).status(HttpStatus.FORBIDDEN);
        verify(ctx).json(Map.of("message", "Acesso negado."));
    }

    @Test
    void falhaTecnicaRetorna500SemDetalhesInternos() {
        when(service.buscarPorId(usuarioId, admin)).thenReturn(usuario());
        doThrow(new RuntimeException("SQL password leaked"))
                .when(service).atualizar(eq(usuarioId), any(), eq(admin));

        controller.atualizar(ctx);

        verify(ctx).status(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(ctx).json(Map.of("message", "Erro interno ao atualizar usuario."));
    }

    private void prepararFormulario() {
        when(ctx.pathParam("id")).thenReturn(usuarioId.toString());
        when(ctx.formParamMap()).thenReturn(Map.of("nomeCompleto", List.of("Maria da Silva")));
        when(ctx.formParam("nomeCompleto")).thenReturn("Maria da Silva");
        when(ctx.formParam("email")).thenReturn("maria@example.com");
        when(ctx.formParam("cpf")).thenReturn("52998224725");
        when(ctx.formParam("telefone")).thenReturn("");
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setCpf("52998224725");
        usuario.setAtivo(true);
        return usuario;
    }
}