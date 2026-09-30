package br.com.kutuar.administrativo.controllers;

import br.com.kutuar.administrativo.services.DashboardService;
import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.models.Usuario;
import br.com.kutuar.seguranca.repositories.UsuarioRepository;
import br.com.kutuar.seguranca.services.EscolaService;
import br.com.kutuar.seguranca.services.UsuarioAdminService;
import br.com.kutuar.seguranca.utils.AuthUserContext;
import io.javalin.http.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DashboardControllerTest {

    private final DashboardService dashboardService = mock(DashboardService.class);
    private final EscolaService escolaService = mock(EscolaService.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final UsuarioAdminService usuarioAdminService = mock(UsuarioAdminService.class);
    private final TemplateEngine templateEngine = mock(TemplateEngine.class);
    private final Context ctx = mock(Context.class);
    private final DashboardController controller = new DashboardController(
            dashboardService, escolaService, usuarioRepository, usuarioAdminService, templateEngine);
    private final UUID usuarioId = UUID.randomUUID();
    private final AuthUser admin = new AuthUser(UUID.randomUUID(), null, null, Perfil.SUPER_ADMIN, "00000000000");

    @BeforeEach
    void setUp() {
        AuthUserContext.setAuthUser(admin);
        when(ctx.pathParam("id")).thenReturn(usuarioId.toString());
        when(ctx.formParamMap()).thenReturn(Map.of("nomeCompleto", List.of("Maria da Silva")));
        when(ctx.formParam("nomeCompleto")).thenReturn("Maria da Silva");
        when(ctx.formParam("email")).thenReturn("maria@example.com");
        when(ctx.formParam("telefone")).thenReturn("");
        when(ctx.formParam("perfil")).thenReturn(null);
        when(ctx.formParam("aprovado")).thenReturn(null);
        when(usuarioAdminService.buscarPorId(usuarioId, admin)).thenReturn(usuario());
    }

    @AfterEach
    void tearDown() {
        AuthUserContext.clear();
    }

    @Test
    void rotaLegadaDelegaEdicaoAoServiceEnaoPersistePeloRepository() {
        controller.salvarEditarUsuario(ctx);

        verify(usuarioAdminService).atualizar(eq(usuarioId), any(), eq(admin));
        verify(usuarioRepository, never()).update(any());
        verify(ctx).redirect("/dashboard/usuarios");
    }

    @Test
    void formularioNaoEnviaCamposSemPersistencia() throws IOException {
        String template = Files.readString(Path.of("src/main/resources/templates/dashboard/usuarios/editar.html"));

        assertFalse(template.contains("dataNascimento"));
        assertFalse(template.contains("sexo"));
        assertFalse(template.contains("cep"));
        assertFalse(template.contains("endereco"));
        assertFalse(template.contains("complemento"));
        assertFalse(template.contains("bairro"));
        assertFalse(template.contains("cidade"));
        assertFalse(template.contains("estado"));
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setCpf("52998224725");
        usuario.setAtivo(true);
        return usuario;
    }
}