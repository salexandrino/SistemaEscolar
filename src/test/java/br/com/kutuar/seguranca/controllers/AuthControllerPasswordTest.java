package br.com.kutuar.seguranca.controllers;

import br.com.kutuar.seguranca.exceptions.AuthenticationException;
import br.com.kutuar.seguranca.dtos.AlterarSenhaPropriaDTO;
import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.utils.AuthUserContext;
import br.com.kutuar.seguranca.services.AuthService;
import io.javalin.http.Context;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import java.util.UUID;

class AuthControllerPasswordTest {

    @Test
    void encaminhaSenhaComEspacosExatamenteComoFoiInformada() {
        AuthService authService = mock(AuthService.class);
        Context context = mock(Context.class);
        String senhaComEspacos = " senha de teste ";
        when(context.formParam("cpf")).thenReturn(" 529.982.247-25 ");
        when(context.formParam("senha")).thenReturn(senhaComEspacos);
        when(authService.autenticar("529.982.247-25", senhaComEspacos))
                .thenThrow(new AuthenticationException("CPF ou senha inválidos."));

        new AuthController(authService).login(context);

        verify(authService).autenticar("529.982.247-25", senhaComEspacos);
    }

    @Test
    void alterarSenhaSemAutenticacaoFalha() {
        AuthUserContext.clear();
        Context context = mock(Context.class);
        AuthService authService = mock(AuthService.class);
        new AuthController(authService).changePassword(context);
        verify(authService, never()).alterarSenhaPropria(any(), any(), any());
        verify(context).status(io.javalin.http.HttpStatus.UNAUTHORIZED);
        verify(context).json(java.util.Map.of("message", "Usuário não autenticado."));
    }

    @Test
    void usuarioAutenticadoConsegueAlterarPropriaSenhaSemTenant() {
        UUID usuarioId = UUID.randomUUID();
        AuthUserContext.setAuthUser(new AuthUser(usuarioId, null, null, Perfil.SUPER_ADMIN, "cpf"));
        Context context = mock(Context.class);
        when(context.bodyAsClass(AlterarSenhaPropriaDTO.class))
                .thenReturn(new AlterarSenhaPropriaDTO("atual", "NovaSenha@123", "NovaSenha@123"));
        AuthService authService = mock(AuthService.class);

        assertDoesNotThrow(() -> new AuthController(authService).changePassword(context));
        verify(authService).alterarSenhaPropria(eq(usuarioId), isNull(), any(AlterarSenhaPropriaDTO.class));
        verify(context).json(java.util.Map.of("message", "Senha alterada com sucesso."));
        AuthUserContext.clear();
    }
}
