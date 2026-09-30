package br.com.kutuar.seguranca.controllers;

import br.com.kutuar.seguranca.exceptions.AuthenticationException;
import br.com.kutuar.seguranca.services.AuthService;
import io.javalin.http.Context;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerPasswordTest {

    @Test
    void encaminhaSenhaComEspacosExatamenteComoFoiInformada() {
        AuthService authService = mock(AuthService.class);
        Context context = mock(Context.class);
        String senhaComEspacos = " Admin123@ ";
        when(context.formParam("cpf")).thenReturn(" 529.982.247-25 ");
        when(context.formParam("senha")).thenReturn(senhaComEspacos);
        when(authService.autenticar("529.982.247-25", senhaComEspacos))
                .thenThrow(new AuthenticationException("CPF ou senha inválidos."));

        new AuthController(authService).login(context);

        verify(authService).autenticar("529.982.247-25", senhaComEspacos);
    }
}
