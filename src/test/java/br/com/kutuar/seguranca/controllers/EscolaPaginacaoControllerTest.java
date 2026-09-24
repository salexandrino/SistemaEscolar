package br.com.kutuar.seguranca.controllers;

import br.com.kutuar.seguranca.dtos.PageResponse;
import br.com.kutuar.seguranca.dtos.EscolaResumoDTO;
import br.com.kutuar.seguranca.enums.*;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.exceptions.AuthorizationException;
import br.com.kutuar.seguranca.middlewares.RoleBasedMiddleware;
import br.com.kutuar.seguranca.services.EscolaService;
import br.com.kutuar.seguranca.utils.AuthUserContext;
import io.javalin.http.Context;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EscolaPaginacaoControllerTest {
    private final EscolaService service = mock(EscolaService.class);
    private final EscolaController controller = new EscolaController(service);
    private final Context ctx = mock(Context.class);
    private final AuthUser admin = new AuthUser(null, null, null, Perfil.SUPER_ADMIN, null);

    @BeforeEach void setup() {
        AuthUserContext.setAuthUser(admin);
        when(ctx.status(any(io.javalin.http.HttpStatus.class))).thenReturn(ctx);
    }
    @AfterEach void cleanup() { AuthUserContext.clear(); }

    @Test void apiRetornaContratoPadronizado() throws Exception {
        when(ctx.queryParam("search")).thenReturn("Kutuar");
        when(ctx.queryParam("status")).thenReturn("ativa");
        when(ctx.queryParam("page")).thenReturn("2");
        when(ctx.queryParam("size")).thenReturn("10");
        when(service.listarPaginadas("Kutuar", EscolaStatus.ATIVA, 2, 10, admin))
                .thenReturn(new PageResponse<>(2, 10, 21, List.of()));
        controller.listarEscolasAdmin(ctx);
        ArgumentCaptor<Object> response = ArgumentCaptor.forClass(Object.class);
        verify(ctx).json(response.capture());
        var json = new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(response.getValue());
        assertEquals(21L, json.get("totalItems").asLong());
        assertTrue(json.get("items").isArray());
        assertEquals(0, json.get("items").size());
        assertEquals(2, json.get("page").asInt());
        assertEquals(3L, json.get("totalPages").asLong());
        assertEquals(5, json.size());
    }

    @Test void apiVaziaRetornaHttp200EItemsVazio() {
        when(service.listarPaginadas(null, null, null, null, admin))
                .thenReturn(new PageResponse<>(1, 20, 0, List.of()));

        controller.listarEscolasAdmin(ctx);

        verify(ctx).status(io.javalin.http.HttpStatus.OK);
        ArgumentCaptor<Object> response = ArgumentCaptor.forClass(Object.class);
        verify(ctx).json(response.capture());
        PageResponse<?> pagina = (PageResponse<?>) response.getValue();
        assertEquals(0, pagina.totalItems());
        assertEquals(0, pagina.totalPages());
        assertEquals(List.of(), pagina.items());
    }

    @Test void apiRejeitaPageNaoInteiro() {
        when(ctx.queryParam("page")).thenReturn("abc");
        controller.listarEscolasAdmin(ctx);
        assertBadRequest("Parâmetro 'page' inválido. Use um número inteiro.");
        verifyNoInteractions(service);
    }

    @Test void apiRejeitaSizeNaoInteiro() {
        when(ctx.queryParam("size")).thenReturn("1.5");
        controller.listarEscolasAdmin(ctx);
        assertBadRequest("Parâmetro 'size' inválido. Use um número inteiro.");
        verifyNoInteractions(service);
    }

    @Test void apiRejeitaStatusInexistente() {
        when(ctx.queryParam("status")).thenReturn("EXCLUIDA");
        controller.listarEscolasAdmin(ctx);
        assertBadRequest("Status inválido. Use um dos valores permitidos: ATIVA ou INATIVA.");
        verifyNoInteractions(service);
    }

    @Test void apiEncaminhaTodosOsParametrosConvertidos() {
        when(ctx.queryParam("search")).thenReturn("  hora  ");
        when(ctx.queryParam("status")).thenReturn("ATIVA");
        when(ctx.queryParam("page")).thenReturn("3");
        when(ctx.queryParam("size")).thenReturn("25");
        when(service.listarPaginadas("  hora  ", EscolaStatus.ATIVA, 3, 25, admin))
                .thenReturn(new PageResponse<>(3, 25, 0, List.of()));

        controller.listarEscolasAdmin(ctx);

        verify(service).listarPaginadas("  hora  ", EscolaStatus.ATIVA, 3, 25, admin);
        verify(ctx).status(io.javalin.http.HttpStatus.OK);
    }

    @Test void middlewareRecusaAusenteEPerfilNaoSuperAdmin() throws Exception {
        RoleBasedMiddleware middleware = new RoleBasedMiddleware(Perfil.SUPER_ADMIN);
        when(ctx.path()).thenReturn("/api/admin/escolas");
        AuthUserContext.clear();
        AuthorizationException ausente = assertThrows(AuthorizationException.class, () -> middleware.handle(ctx));
        assertEquals(io.javalin.http.HttpStatus.FORBIDDEN, ausente.getStatus());

        AuthUser gestor = new AuthUser(null, null, null, Perfil.GESTOR, "00000000000");
        AuthUserContext.setAuthUser(gestor);
        AuthorizationException proibido = assertThrows(AuthorizationException.class, () -> middleware.handle(ctx));
        assertEquals(io.javalin.http.HttpStatus.FORBIDDEN, proibido.getStatus());

        AuthUserContext.setAuthUser(admin);
        assertDoesNotThrow(() -> middleware.handle(ctx));
    }

    @SuppressWarnings("unchecked")
    private void assertBadRequest(String message) {
        verify(ctx).status(io.javalin.http.HttpStatus.BAD_REQUEST);
        ArgumentCaptor<Object> response = ArgumentCaptor.forClass(Object.class);
        verify(ctx).json(response.capture());
        assertEquals(Map.of("message", message), response.getValue());
    }

    @Test void telaPreservaFiltrosETrataParametrosInvalidos() {
        when(ctx.queryParam("search")).thenReturn("  Kutuar  ");
        when(ctx.queryParam("status")).thenReturn("inexistente");
        when(ctx.queryParam("page")).thenReturn("abc");
        when(ctx.queryParam("size")).thenReturn("-3");
        when(service.listarPaginadas("  Kutuar  ", null, 1, -3, admin))
                .thenReturn(new PageResponse<>(1, 1, 0, List.of()));
        controller.exibirPaginaListagem(ctx);
        ArgumentCaptor<Map<String, Object>> model = ArgumentCaptor.forClass(Map.class);
        verify(ctx).render(eq("dashboard/escolas/lista.html"), model.capture());
        assertEquals("Kutuar", model.getValue().get("search"));
        assertEquals("", model.getValue().get("filtroStatus"));
        assertEquals(1, model.getValue().get("size"));
    }

    @Test void templateRenderizaLinksComFiltrosCodificados() {
        var resolver = new org.thymeleaf.templateresolver.ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        var engine = new org.thymeleaf.TemplateEngine();
        engine.setTemplateResolver(resolver);
        var context = new org.thymeleaf.context.Context();
        context.setVariables(Map.of("escolas", List.of(new EscolaResumoDTO(UUID.randomUUID(), "Escola X", "123", "ATIVA", "Recife")), "search", "A&B", "filtroStatus", "ATIVA",
                "page", 2, "size", 10, "total", 30L, "totalPages", 3L,
                "hasPrevious", true, "hasNext", true));
        context.setVariable("content", "dashboard/escolas/lista");
        String html = engine.process("dashboard/escolas/lista", context);
        assertTrue(html.contains("escolas?search=A%26B&amp;status=ATIVA&amp;page=1&amp;size=10"));
        assertTrue(html.contains("escolas?search=A%26B&amp;status=ATIVA&amp;page=3&amp;size=10"));
        assertTrue(html.contains("Recife"));
        assertTrue(html.contains("Escola X"));
        assertTrue(html.contains("value=\"ATIVA\" selected=\"selected\""));
    }
}
