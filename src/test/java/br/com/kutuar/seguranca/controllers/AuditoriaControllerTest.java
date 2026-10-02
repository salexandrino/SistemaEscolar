package br.com.kutuar.seguranca.controllers;

import br.com.kutuar.seguranca.dtos.EventoAuditoriaResumoDTO;
import br.com.kutuar.seguranca.dtos.PageResponse;
import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.exceptions.AuthenticationException;
import br.com.kutuar.seguranca.exceptions.AuthorizationException;
import br.com.kutuar.seguranca.middlewares.RoleBasedMiddleware;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.services.AuditoriaService;
import br.com.kutuar.seguranca.utils.AuthUserContext;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AuditoriaControllerTest {
    private final AuditoriaService service = mock(AuditoriaService.class);
    private final AuditoriaController controller = new AuditoriaController(service);
    private final Context ctx = mock(Context.class);

    @BeforeEach
    void setup() {
        when(ctx.status(any(HttpStatus.class))).thenReturn(ctx);
    }

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
        when(service.listar(null, null, null, null, 1, 20))
                .thenReturn(new PageResponse<>(1, 20, 0, List.of()));
        AuthUserContext.setAuthUser(new AuthUser(UUID.randomUUID(), null, null, Perfil.SUPER_ADMIN, "cpf"));
        RoleBasedMiddleware middleware = new RoleBasedMiddleware(Perfil.SUPER_ADMIN);

        assertDoesNotThrow(() -> middleware.handle(ctx));
        controller.listar(ctx);

        verify(service).listar(null, null, null, null, 1, 20);
        verify(ctx).status(HttpStatus.OK);
    }

    @Test
    void listaComFiltrosEPaginaNoContratoPageResponse() {
        when(ctx.queryParam("acao")).thenReturn("LOGIN");
        when(ctx.queryParam("entidade")).thenReturn("USUARIO");
        when(ctx.queryParam("dataInicio")).thenReturn("2026-09-01");
        when(ctx.queryParam("dataFim")).thenReturn("2026-09-30");
        when(ctx.queryParam("page")).thenReturn("2");
        when(ctx.queryParam("size")).thenReturn("10");
        var item = new EventoAuditoriaResumoDTO(UUID.randomUUID(), LocalDateTime.of(2026, 9, 30, 12, 0),
                "Admin", UUID.randomUUID(), "LOGIN", "USUARIO", "login válido");
        when(service.listar("LOGIN", "USUARIO", LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30), 2, 10))
                .thenReturn(new PageResponse<>(2, 10, 11, List.of(item)));

        controller.listar(ctx);

        verify(service).listar("LOGIN", "USUARIO", LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30), 2, 10);
        verify(ctx).status(HttpStatus.OK);
        ArgumentCaptor<Object> response = ArgumentCaptor.forClass(Object.class);
        verify(ctx).json(response.capture());
        var json = new com.fasterxml.jackson.databind.ObjectMapper()
                .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                .valueToTree(response.getValue());
        assertTrue(json.get("items").isArray());
        assertEquals(2, json.get("page").asInt());
        assertEquals("Admin", json.get("items").get(0).get("executor").asText());
    }

    @Test
    void dataInvalidaRetorna400SemConsultarService() {
        when(ctx.queryParam("dataInicio")).thenReturn("2026-02-30");

        controller.listar(ctx);

        verify(ctx).status(HttpStatus.BAD_REQUEST);
        ArgumentCaptor<Object> response = ArgumentCaptor.forClass(Object.class);
        verify(ctx).json(response.capture());
        assertTrue(((Map<?, ?>) response.getValue()).get("message").toString().contains("dataInicio"));
        verifyNoInteractions(service);
    }

    @Test
    void dataFinalAnteriorRetorna400() {
        when(ctx.queryParam("dataInicio")).thenReturn("2026-09-30");
        when(ctx.queryParam("dataFim")).thenReturn("2026-09-01");
        when(service.listar(null, null, LocalDate.of(2026, 9, 30), LocalDate.of(2026, 9, 1), 1, 20))
                .thenThrow(new IllegalArgumentException("A data final não pode ser anterior à data inicial."));

        controller.listar(ctx);

        verify(ctx).status(HttpStatus.BAD_REQUEST);
    }
}
