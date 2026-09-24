package br.com.kutuar.seguranca.controllers;

import br.com.kutuar.seguranca.exceptions.BusinessException;
import br.com.kutuar.seguranca.exceptions.NotFoundException;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.models.Escola;
import br.com.kutuar.seguranca.services.EscolaService;
import br.com.kutuar.seguranca.utils.AuthUserContext;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EscolaStatusControllerTest {
    private final EscolaService service = mock(EscolaService.class);
    private final EscolaController controller = new EscolaController(service);
    private final Context ctx = mock(Context.class);

    @BeforeEach void setup() {
        AuthUserContext.setAuthUser(mock(AuthUser.class));
        when(ctx.status(any(HttpStatus.class))).thenReturn(ctx);
    }

    @AfterEach void cleanup() { AuthUserContext.clear(); }

    @Test void retorna400ComCampoMessageParaTransicaoInvalida() {
        UUID id = UUID.randomUUID();
        when(ctx.pathParam("id")).thenReturn(id.toString());
        doThrow(new BusinessException("A escola já está ativa.", HttpStatus.BAD_REQUEST))
                .when(service).ativarEscola(eq(id), any());

        controller.ativarEscola(ctx);

        verify(ctx).status(HttpStatus.BAD_REQUEST);
        verify(ctx).json(Map.of("message", "A escola já está ativa."));
    }

    @Test void superAdminRecebeSucessoAoAtivarEInativar() {
        UUID ativarId = UUID.randomUUID();
        UUID inativarId = UUID.randomUUID();
        Escola ativa = new Escola(); ativa.setId(ativarId); ativa.setStatus("ATIVA");
        Escola inativa = new Escola(); inativa.setId(inativarId); inativa.setStatus("INATIVA");
        when(ctx.pathParam("id")).thenReturn(ativarId.toString(), inativarId.toString());
        when(service.ativarEscola(eq(ativarId), any())).thenReturn(ativa);
        when(service.inativarEscola(eq(inativarId), any())).thenReturn(inativa);

        controller.ativarEscola(ctx);
        controller.inativarEscola(ctx);

        verify(ctx, times(2)).status(HttpStatus.OK);
        verify(service).ativarEscola(eq(ativarId), any());
        verify(service).inativarEscola(eq(inativarId), any());
    }

    @Test void retorna404ComCampoMessageQuandoEscolaNaoExiste() {
        UUID id = UUID.randomUUID();
        when(ctx.pathParam("id")).thenReturn(id.toString());
        doThrow(new NotFoundException("Escola não encontrada.")).when(service).inativarEscola(eq(id), any());

        controller.inativarEscola(ctx);

        verify(ctx).status(HttpStatus.NOT_FOUND);
        verify(ctx).json(Map.of("message", "Escola não encontrada."));
    }
}
