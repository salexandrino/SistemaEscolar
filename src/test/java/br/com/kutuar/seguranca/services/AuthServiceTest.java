package br.com.kutuar.seguranca.services;

import br.com.kutuar.seguranca.dtos.AlterarSenhaPropriaDTO;
import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.exceptions.AuthenticationException;
import br.com.kutuar.seguranca.exceptions.AuthorizationException;
import br.com.kutuar.seguranca.exceptions.ValidationException;
import br.com.kutuar.seguranca.models.Escola;
import br.com.kutuar.seguranca.models.Usuario;
import br.com.kutuar.seguranca.repositories.EscolaRepository;
import br.com.kutuar.seguranca.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EscolaRepository escolaRepository;

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
        superAdminFake.setEmail("admin@kutuar.com");
        superAdminFake.setSenhaHash("$2a$10$CrivoDeHashFakeAqui");
        superAdminFake.setPerfil(Perfil.SUPER_ADMIN);
        superAdminFake.setCpf("123.456.789-00");
    }

    @Test
    @DisplayName("Deve autenticar o SuperAdmin com sucesso e retornar o Token")
    void deveAutenticarSuperAdminComSucesso() {
        // GIVEN (Configuração do comportamento dos mocks)
        String email = "admin@kutuar.com";
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
        String email = "admin@kutuar.com";
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

    @Test
    @DisplayName("Gestor de escola inativa não consegue logar")
    void gestorDeEscolaInativaNaoConsegueLogar() {
        Usuario gestor = usuarioDaEscola(Perfil.GESTOR);
        when(usuarioRepository.findByCpf(gestor.getCpf())).thenReturn(Optional.of(gestor));
        when(escolaRepository.findById(gestor.getEscolaId())).thenReturn(Optional.of(escola("INATIVA", gestor.getEscolaId())));
        when(passwordService.verificar("Senha@123", gestor.getSenhaHash())).thenReturn(true);

        assertThrows(AuthenticationException.class, () -> authService.autenticar(gestor.getCpf(), "Senha@123"));
        verify(jwtService, never()).gerarToken(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Usuário comum de escola inativa não consegue logar")
    void usuarioComumDeEscolaInativaNaoConsegueLogar() {
        Usuario usuario = usuarioDaEscola(Perfil.PROFESSOR);
        when(usuarioRepository.findByCpf(usuario.getCpf())).thenReturn(Optional.of(usuario));
        when(escolaRepository.findById(usuario.getEscolaId())).thenReturn(Optional.of(escola("INATIVA", usuario.getEscolaId())));
        when(passwordService.verificar("Senha@123", usuario.getSenhaHash())).thenReturn(true);

        assertThrows(AuthenticationException.class, () -> authService.autenticar(usuario.getCpf(), "Senha@123"));
        verify(jwtService, never()).gerarToken(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Usuário volta a logar após reativação da escola")
    void usuarioVoltaALogarAposReativacaoDaEscola() {
        Usuario usuario = usuarioDaEscola(Perfil.PROFESSOR);
        when(usuarioRepository.findByCpf(usuario.getCpf())).thenReturn(Optional.of(usuario));
        when(escolaRepository.findById(usuario.getEscolaId())).thenReturn(Optional.of(escola("ATIVA", usuario.getEscolaId())));
        when(passwordService.verificar("Senha@123", usuario.getSenhaHash())).thenReturn(true);
        when(jwtService.gerarToken(any(), any(), any(), any(), any())).thenReturn("jwt-ativo");

        assertEquals("jwt-ativo", authService.autenticar(usuario.getCpf(), "Senha@123"));
    }

    @Test
    void autenticaSenhaSemTransformacaoAntesDoPasswordService() {
        Usuario usuario = usuarioDaEscola(Perfil.SUPER_ADMIN);
        String senha = "senha de teste";
        when(usuarioRepository.findByCpf(usuario.getCpf())).thenReturn(Optional.of(usuario));
        when(passwordService.verificar(senha, usuario.getSenhaHash())).thenReturn(true);
        when(jwtService.gerarToken(any(), any(), any(), any(), any())).thenReturn("jwt");

        assertEquals("jwt", authService.autenticar(usuario.getCpf(), senha));

        verify(passwordService).verificar(senha, usuario.getSenhaHash());
    }

    @Test
    void naoRemoveEspacosDaSenhaAntesDoPasswordService() {
        Usuario usuario = usuarioDaEscola(Perfil.SUPER_ADMIN);
        String senhaComEspacos = " senha de teste ";
        when(usuarioRepository.findByCpf(usuario.getCpf())).thenReturn(Optional.of(usuario));
        when(passwordService.verificar(senhaComEspacos, usuario.getSenhaHash())).thenReturn(false);

        assertThrows(AuthenticationException.class, () -> authService.autenticar(usuario.getCpf(), senhaComEspacos));

        verify(passwordService).verificar(senhaComEspacos, usuario.getSenhaHash());
        verify(passwordService, never()).verificar("senha de teste", usuario.getSenhaHash());
    }

    @Test
    void redefinicaoPreservaSenhaComEspacosAoGerarHash() {
        String email = "admin@kutuar.com";
        String token = "token-de-recuperacao";
        String senhaComEspacos = " NovaSenha@123 ";
        superAdminFake.setResetPasswordToken(BCrypt.hashpw(token, BCrypt.gensalt()));
        superAdminFake.setResetPasswordExpiresAt(LocalDateTime.now().plusMinutes(10));
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(superAdminFake));
        when(passwordService.hash(senhaComEspacos)).thenReturn("$2a$10$novoHash");

        authService.resetPassword(email, token, senhaComEspacos, senhaComEspacos);

        verify(passwordService).hash(senhaComEspacos);
        verify(usuarioRepository).updatePassword(superAdminFake.getId(), superAdminFake.getTenantId(), "$2a$10$novoHash");
    }

    private Usuario usuarioDaEscola(Perfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setCpf("529.982.247-25");
        usuario.setSenhaHash("hash");
        usuario.setPerfil(perfil);
        usuario.setEscolaId(UUID.randomUUID());
        usuario.setTenantId(UUID.randomUUID());
        usuario.setAtivo(true);
        usuario.setBloqueado(false);
        return usuario;
    }

    private Escola escola(String status, UUID escolaId) {
        Escola escola = new Escola();
        escola.setId(escolaId);
        escola.setStatus(status);
        return escola;
    }

    @Test
    @DisplayName("Deve alterar a senha própria com sucesso")
    void deveAlterarSenhaPropriaComSucesso() {
        // GIVEN
        UUID usuarioId = superAdminFake.getId();
        UUID tenantId = null;
        AlterarSenhaPropriaDTO dto = new AlterarSenhaPropriaDTO("senha_antiga", "NovaSenha@123", "NovaSenha@123");

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(superAdminFake));
        when(passwordService.verificar("senha_antiga", superAdminFake.getSenhaHash())).thenReturn(true);
        when(passwordService.hash("NovaSenha@123")).thenReturn("$2a$10$NovoHashGerado");
        // WHEN
        assertDoesNotThrow(() -> {
            authService.alterarSenhaPropria(usuarioId, tenantId, dto);
        });

        // THEN
        verify(usuarioRepository, times(1)).updatePassword(usuarioId, tenantId, "$2a$10$NovoHashGerado");
    }

    @Test
    @DisplayName("Deve lançar ValidationException se confirmação de senha não bater")
    void deveLancarExcecaoSeConfirmacaoNaoBater() {
        UUID usuarioId = superAdminFake.getId();
        UUID tenantId = UUID.randomUUID();
        AlterarSenhaPropriaDTO dto = new AlterarSenhaPropriaDTO("senha_antiga", "NovaSenha@123", "SenhaDiferente@123");

        assertThrows(ValidationException.class, () -> {
            authService.alterarSenhaPropria(usuarioId, tenantId, dto);
        });

        verify(usuarioRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Deve lançar AuthorizationException se senha atual estiver incorreta")
    void deveLancarExcecaoSeSenhaAtualIncorreta() {
        UUID usuarioId = superAdminFake.getId();
        UUID tenantId = UUID.randomUUID();
        AlterarSenhaPropriaDTO dto = new AlterarSenhaPropriaDTO("senha_errada", "NovaSenha@123", "NovaSenha@123");

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(superAdminFake));
        when(passwordService.verificar("senha_errada", superAdminFake.getSenhaHash())).thenReturn(false);

        assertThrows(AuthorizationException.class, () -> {
            authService.alterarSenhaPropria(usuarioId, tenantId, dto);
        });

        verify(passwordService, never()).hash(any());
        verify(usuarioRepository, never()).updatePassword(any(), any(), any());
    }
}
