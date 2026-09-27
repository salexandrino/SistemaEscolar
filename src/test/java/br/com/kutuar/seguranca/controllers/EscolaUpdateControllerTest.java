package br.com.kutuar.seguranca.controllers;

import br.com.kutuar.seguranca.dtos.AtualizarEscolaDTO;
import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.exceptions.ValidationException;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.services.EscolaService;
import br.com.kutuar.seguranca.utils.AuthUserContext;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EscolaUpdateControllerTest {
    private final EscolaService service = mock(EscolaService.class);
    private final EscolaController controller = new EscolaController(service);
    private final Context ctx = mock(Context.class);
    private final AuthUser admin = new AuthUser(UUID.randomUUID(), null, null, Perfil.SUPER_ADMIN, "00000000000");

    @BeforeEach
    void setup() {
        AuthUserContext.setAuthUser(admin);
        when(ctx.status(any(HttpStatus.class))).thenReturn(ctx);
    }

    @AfterEach
    void cleanup() {
        AuthUserContext.clear();
    }

    @Test
    void patchOuPutGenericoComStatusRetorna400() {
        UUID escolaId = UUID.randomUUID();
        AtualizarEscolaDTO dto = new AtualizarEscolaDTO();
        dto.setStatus("INATIVA");
        String mensagem = "O status da escola só pode ser alterado pelos fluxos de ativação ou inativação.";
        when(ctx.pathParam("id")).thenReturn(escolaId.toString());
        when(ctx.formParamMap()).thenReturn(Map.of());
        when(ctx.bodyAsClass(AtualizarEscolaDTO.class)).thenReturn(dto);
        doThrow(new ValidationException(mensagem)).when(service).atualizarEscola(eq(escolaId), eq(dto), eq(admin));

        controller.atualizarEscola(ctx);

        verify(ctx).status(HttpStatus.BAD_REQUEST);
        verify(ctx).json(Map.of("error", mensagem));
    }
}
