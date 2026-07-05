package br.com.synge.seguranca.services;

import br.com.synge.seguranca.enums.Perfil;
import br.com.synge.seguranca.exceptions.AuthenticationException;
import br.com.synge.seguranca.models.Usuario;
import br.com.synge.seguranca.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordService passwordService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private Usuario superAdminFake;

    @BeforeEach
    void setUp() {
        // Inicializa um usuário mockado para os testes
        superAdminFake = new Usuario();
        superAdminFake.setId(UUID.randomUUID());
        superAdminFake.setEmail("admin@synge.com");
        superAdminFake.setSenhaHash("$2a$10$CrivoDeHashFakeAqui");
        superAdminFake.setPerfil(Perfil.SUPER_ADMIN);
        superAdminFake.setCpf("123.456.789-00");
    }

    @Test
    @DisplayName("Deve autenticar o SuperAdmin com sucesso e retornar o Token")
    void deveAutenticarSuperAdminComSucesso() {
        // GIVEN (Configuração do comportamento dos mocks)
        String email = "admin@synge.com";
        String senhaPura = "123456";
        String tokenEsperado = "jwt-token-de-teste";

        when(usuarioRepository.findSuperAdminByEmail(email)).thenReturn(Optional.of(superAdminFake));
        when(passwordService.verificar(senhaPura, superAdminFake.getSenhaHash())).thenReturn(true);
        when(jwtService.gerarToken(superAdminFake.getId(), null, null, Perfil.SUPER_ADMIN, superAdminFake.getCpf()))
                .thenReturn(tokenEsperado);

        // WHEN (Execução da ação)
        String tokenGerado = authService.autenticarSuperAdmin(email, senhaPura);

        // THEN (Verificação se o resultado é o esperado)
        assertNotNull(tokenGerado);
        assertEquals(tokenEsperado, tokenGerado);

        // Garante que o banco foi consultado corretamente
        verify(usuarioRepository, times(1)).findSuperAdminByEmail(email);
    }

    @Test
    @DisplayName("Deve lançar exceção quando a senha do SuperAdmin estiver incorreta")
    void deveLancarExcecaoQuandoSenhaIncorreta() {
        // GIVEN
        String email = "admin@synge.com";
        String senhaIncorreta = "senha_errada";

        when(usuarioRepository.findSuperAdminByEmail(email)).thenReturn(Optional.of(superAdminFake));
        // Força o validador a dizer que a senha não bate
        when(passwordService.verificar(senhaIncorreta, superAdminFake.getSenhaHash())).thenReturn(false);

        // WHEN & THEN (Garante que a exceção certa é disparada)
        assertThrows(AuthenticationException.class, () -> {
            authService.autenticarSuperAdmin(email, senhaIncorreta);
        });

        // Garante que o gerador de token NUNCA foi chamado
        verify(jwtService, never()).gerarToken(any(), any(), any(), any(), any());
    }
}