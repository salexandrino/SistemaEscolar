package br.com.kutuar.seguranca.controllers;

import br.com.kutuar.seguranca.dtos.PaginaEscolasDTO;
import br.com.kutuar.seguranca.enums.*;
import br.com.kutuar.seguranca.models.AuthUser;
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

    @BeforeEach void setup() { AuthUserContext.setAuthUser(admin); }
    @AfterEach void cleanup() { AuthUserContext.clear(); }

    @Test void apiPreservaChavesEAdicionaMetadados() {
        when(ctx.queryParam("search")).thenReturn("Kutuar");
        when(ctx.queryParam("status")).thenReturn("ativa");
        when(ctx.queryParam("page")).thenReturn("2");
        when(ctx.queryParam("size")).thenReturn("10");
        when(service.listarPaginadas("Kutuar", EscolaStatus.ATIVA, 2, 10, admin))
                .thenReturn(new PaginaEscolasDTO(List.of(), 2, 10, 21, 3, true, true));
        controller.listarEscolas(ctx);
        ArgumentCaptor<Object> response = ArgumentCaptor.forClass(Object.class);
        verify(ctx).json(response.capture());
        Map<?, ?> json = (Map<?, ?>) response.getValue();
        assertEquals(21L, json.get("total"));
        assertEquals(List.of(), json.get("escolas"));
        assertEquals(2, json.get("page"));
        assertEquals(3L, json.get("totalPages"));
        assertEquals(true, json.get("hasNext"));
    }

    @Test void telaPreservaFiltrosETrataParametrosInvalidos() {
        when(ctx.queryParam("search")).thenReturn("  Kutuar  ");
        when(ctx.queryParam("status")).thenReturn("inexistente");
        when(ctx.queryParam("page")).thenReturn("abc");
        when(ctx.queryParam("size")).thenReturn("-3");
        when(service.listarPaginadas("  Kutuar  ", null, 1, 1, admin))
                .thenReturn(new PaginaEscolasDTO(List.of(), 1, 1, 0, 0, false, false));
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
        context.setVariables(Map.of("escolas", List.of(), "search", "A&B", "filtroStatus", "ATIVA",
                "page", 2, "size", 10, "total", 30L, "totalPages", 3L,
                "hasPrevious", true, "hasNext", true));
        context.setVariable("content", "dashboard/escolas/lista");
        String html = engine.process("dashboard/escolas/lista", context);
        assertTrue(html.contains("escolas?search=A%26B&amp;status=ATIVA&amp;page=1&amp;size=10"));
        assertTrue(html.contains("escolas?search=A%26B&amp;status=ATIVA&amp;page=3&amp;size=10"));
        assertTrue(html.contains("value=\"ATIVA\" selected=\"selected\""));
    }
}
