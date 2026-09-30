package br.com.kutuar.seguranca.services;

import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.exceptions.ValidationException;
import br.com.kutuar.seguranca.models.Usuario;
import br.com.kutuar.seguranca.repositories.EscolaRepository;
import br.com.kutuar.seguranca.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecuperacaoSenhaServiceTest {

    private static final String EMAIL = "usuario@kutuar.com";

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private EscolaRepository escolaRepository;
    @Mock private JwtService jwtService;
    @Mock private EmailService emailService;

    private AuthService authService;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        authService = new AuthService(usuarioRepository, escolaRepository, new PasswordService(), jwtService, emailService);
        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setTenantId(UUID.randomUUID());
        usuario.setEmail(EMAIL);
        usuario.setNomeCompleto("Usuario de Teste");
        usuario.setPerfil(Perfil.SUPER_ADMIN);
        usuario.setAtivo(true);
        usuario.setSenhaHash(new PasswordService().hash("SenhaAntiga@123"));
    }

    @Test
    void geraTokenSeguroComExpiracaoEArmazenaSomenteHash() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));

        authService.forgotPassword(EMAIL);

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).enviarCodigoRecuperacaoSenha(eq(EMAIL), eq(usuario.getNomeCompleto()), tokenCaptor.capture());
        String token = tokenCaptor.getValue();
        assertTrue(token.length() >= 32);
        assertNotEquals(token, usuario.getResetPasswordToken());
        assertTrue(new PasswordService().verificar(token, usuario.getResetPasswordToken()));
        assertTrue(usuario.getResetPasswordExpiresAt().isAfter(LocalDateTime.now().plusMinutes(14)));
        verify(usuarioRepository).update(usuario);
    }

    @Test
    void rejeitaTokenInvalidoEExpirado() {
        String token = solicitarToken();

        assertThrows(ValidationException.class,
                () -> authService.resetPassword(EMAIL, token + "x", "NovaSenha@123", "NovaSenha@123"));

        usuario.setResetPasswordToken("token-legado-sem-hash");
        assertThrows(ValidationException.class,
                () -> authService.resetPassword(EMAIL, token, "NovaSenha@123", "NovaSenha@123"));

        usuario.setResetPasswordToken(new PasswordService().hash(token));
        usuario.setResetPasswordExpiresAt(LocalDateTime.now().minusSeconds(1));
        assertThrows(ValidationException.class,
                () -> authService.resetPassword(EMAIL, token, "NovaSenha@123", "NovaSenha@123"));
        verify(usuarioRepository, never()).updatePassword(any(), any(), any());
    }

    @Test
    void redefineSenhaComBcryptInvalidaTokenERevogaSenhaAnterior() {
        String token = solicitarToken();
        doAnswer(invocation -> {
            usuario.setSenhaHash(invocation.getArgument(2));
            usuario.setResetPasswordToken(null);
            usuario.setResetPasswordExpiresAt(null);
            return null;
        }).when(usuarioRepository).updatePassword(eq(usuario.getId()), eq(usuario.getTenantId()), anyString());

        authService.resetPassword(EMAIL, token, "NovaSenha@123", "NovaSenha@123");

        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        verify(usuarioRepository).updatePassword(eq(usuario.getId()), eq(usuario.getTenantId()), hashCaptor.capture());
        assertTrue(hashCaptor.getValue().startsWith("$2"));
        PasswordService passwordService = new PasswordService();
        assertTrue(passwordService.verificar("NovaSenha@123", usuario.getSenhaHash()));
        assertFalse(passwordService.verificar("SenhaAntiga@123", usuario.getSenhaHash()));
        assertThrows(ValidationException.class,
                () -> authService.resetPassword(EMAIL, token, "OutraSenha@123", "OutraSenha@123"));
    }

    @Test
    void novaSolicitacaoInvalidaTokenAnterior() {
        String tokenAnterior = solicitarToken();
        String tokenAtual = solicitarToken();

        assertNotEquals(tokenAnterior, tokenAtual);
        assertThrows(ValidationException.class,
                () -> authService.resetPassword(EMAIL, tokenAnterior, "NovaSenha@123", "NovaSenha@123"));
    }

    @Test
    void solicitacaoParaEmailInexistenteNaoVazaNemPersisteInformacao() {
        when(usuarioRepository.findByEmail("ausente@kutuar.com")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> authService.forgotPassword("ausente@kutuar.com"));

        verify(usuarioRepository, never()).update(any());
        verify(emailService, never()).enviarCodigoRecuperacaoSenha(anyString(), anyString(), anyString());
    }

    private String solicitarToken() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        authService.forgotPassword(EMAIL);
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService, atLeastOnce()).enviarCodigoRecuperacaoSenha(eq(EMAIL), anyString(), tokenCaptor.capture());
        return tokenCaptor.getAllValues().getLast();
    }
}
