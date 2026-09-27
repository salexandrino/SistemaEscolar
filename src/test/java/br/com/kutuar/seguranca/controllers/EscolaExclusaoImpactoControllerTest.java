package br.com.kutuar.seguranca.controllers;

import br.com.kutuar.seguranca.dtos.EscolaExclusaoImpactoDTO;
import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.exceptions.NotFoundException;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.services.EscolaService;
import br.com.kutuar.seguranca.utils.AuthUserContext;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EscolaExclusaoImpactoControllerTest {
    private final EscolaService service = mock(EscolaService.class);
    private final EscolaController controller = new EscolaController(service);
    private final Context ctx = mock(Context.class);
    private final AuthUser admin = new AuthUser(null, null, null, Perfil.SUPER_ADMIN, null);

    @BeforeEach void setup() {
        AuthUserContext.setAuthUser(admin);
        when(ctx.status(any(HttpStatus.class))).thenReturn(ctx);
    }

    @AfterEach void cleanup() { AuthUserContext.clear(); }

    @Test void superAdminRecebeImpacto() {
        UUID id = UUID.randomUUID();
        EscolaExclusaoImpactoDTO impacto = new EscolaExclusaoImpactoDTO(id, "Escola", "INATIVA", false,
                List.of("Política pendente"), Map.of("usuario", 1L));
        when(ctx.pathParam("id")).thenReturn(id.toString());
        when(service.analisarImpactoExclusao(id, admin)).thenReturn(impacto);

        controller.analisarImpactoExclusao(ctx);

        verify(ctx).status(HttpStatus.OK);
        verify(ctx).json(impacto);
    }

    @Test void retorna404QuandoEscolaNaoExiste() {
        UUID id = UUID.randomUUID();
        when(ctx.pathParam("id")).thenReturn(id.toString());
        doThrow(new NotFoundException("Escola não encontrada.")).when(service).analisarImpactoExclusao(eq(id), any());

        controller.analisarImpactoExclusao(ctx);

        verify(ctx).status(HttpStatus.NOT_FOUND);
        verify(ctx).json(Map.of("message", "Escola não encontrada."));
    }

    @Test void naoAutenticadoRecebe401SemChamarService() {
        AuthUserContext.clear();
        controller.analisarImpactoExclusao(ctx);

        verify(ctx).status(HttpStatus.UNAUTHORIZED);
        verifyNoInteractions(service);
    }
}
