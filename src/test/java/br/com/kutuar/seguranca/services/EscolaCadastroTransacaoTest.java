package br.com.kutuar.seguranca.services;

import br.com.kutuar.seguranca.dtos.CriarEscolaDTO;
import br.com.kutuar.seguranca.dtos.CriarEscolaResponseDTO;
import br.com.kutuar.seguranca.enums.Perfil;
import br.com.kutuar.seguranca.exceptions.ConflictException;
import br.com.kutuar.seguranca.models.AuthUser;
import br.com.kutuar.seguranca.models.Escola;
import br.com.kutuar.seguranca.models.Usuario;
import br.com.kutuar.seguranca.repositories.EscolaRepository;
import br.com.kutuar.seguranca.repositories.UsuarioRepository;
import br.com.kutuar.seguranca.services.observers.EscolaCadastradaObserver;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EscolaCadastroTransacaoTest {
    private final AuthUser superAdmin = new AuthUser(UUID.randomUUID(), null, null, Perfil.SUPER_ADMIN, "00000000000");

    @Test
    void falhaAoSalvarGestorFazRollbackRestauraAutoCommitENaoDisparaObserver() throws Exception {
        Connection connection = connectionComAutoCommitAtivo();
        EscolaRepository escolas = mock(EscolaRepository.class);
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        PasswordService senha = mock(PasswordService.class);
        EscolaCadastradaObserver observer = mock(EscolaCadastradaObserver.class);
        Escola escolaSalva = escolaSalva();
        when(escolas.existsByCnpj(connection, "04252011000110")).thenReturn(false);
        when(usuarios.existsByEmail(eq(connection), any())).thenReturn(false);
        when(escolas.save(eq(connection), any(Escola.class))).thenReturn(escolaSalva);
        when(senha.hash(any())).thenReturn("hash");
        doThrow(new SQLException("falha ao salvar gestor"))
                .when(usuarios).save(eq(connection), any(Usuario.class), eq(escolaSalva.getTenantId()));
        EscolaService service = service(escolas, usuarios, senha, connection);
        service.adicionarObserver(observer);

        assertThrows(RuntimeException.class, () -> service.cadastrarEscola(dtoValido(), superAdmin));

        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).setAutoCommit(false);
        verify(connection).setAutoCommit(true);
        verify(observer, never()).aoCadastrarEscola(any(), any(), any());
    }

    @Test
    void emailDuplicadoFalhaAntesDePersistirEstadoParcial() throws Exception {
        Connection connection = connectionComAutoCommitAtivo();
        EscolaRepository escolas = mock(EscolaRepository.class);
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        PasswordService senha = mock(PasswordService.class);
        EscolaCadastradaObserver observer = mock(EscolaCadastradaObserver.class);
        when(escolas.existsByCnpj(connection, "04252011000110")).thenReturn(false);
        when(usuarios.existsByEmail(eq(connection), any())).thenReturn(true);
        when(senha.hash(any())).thenReturn("hash");
        EscolaService service = service(escolas, usuarios, senha, connection);
        service.adicionarObserver(observer);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> service.cadastrarEscola(dtoValido(), superAdmin));

        assertEquals("Já existe um usuário cadastrado com o e-mail do responsável.", exception.getMessage());
        verify(escolas, never()).save(eq(connection), any(Escola.class));
        verify(usuarios, never()).save(eq(connection), any(Usuario.class), any());
        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).setAutoCommit(true);
        verify(observer, never()).aoCadastrarEscola(any(), any(), any());
    }

    @Test
    void conflitoDeCnpjDurantePersistenciaEhTraduzidoESemCommitParcial() throws Exception {
        Connection connection = connectionComAutoCommitAtivo();
        EscolaRepository escolas = mock(EscolaRepository.class);
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        PasswordService senha = mock(PasswordService.class);
        EscolaCadastradaObserver observer = mock(EscolaCadastradaObserver.class);
        when(escolas.existsByCnpj(connection, "04252011000110")).thenReturn(false);
        when(usuarios.existsByEmail(eq(connection), any())).thenReturn(false);
        when(senha.hash(any())).thenReturn("hash");
        SQLException uniqueViolation = new SQLException("uk_escola_cnpj", "23505");
        doThrow(new RuntimeException("violação de unicidade", uniqueViolation))
                .when(escolas).save(eq(connection), any(Escola.class));
        EscolaService service = service(escolas, usuarios, senha, connection);
        service.adicionarObserver(observer);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> service.cadastrarEscola(dtoValido(), superAdmin));

        assertEquals("Já existe uma escola cadastrada com este CNPJ.", exception.getMessage());
        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).setAutoCommit(true);
        verify(usuarios, never()).save(eq(connection), any(Usuario.class), any());
        verify(observer, never()).aoCadastrarEscola(any(), any(), any());
    }

    @Test
    void sucessoMantemIntegridadeDoGestorENotificaSomenteAposCommit() throws Exception {
        Connection connection = connectionComAutoCommitAtivo();
        EscolaRepository escolas = mock(EscolaRepository.class);
        UsuarioRepository usuarios = mock(UsuarioRepository.class);
        EscolaCadastradaObserver observer = mock(EscolaCadastradaObserver.class);
        AuditoriaService auditoriaService = mock(AuditoriaService.class);
        Escola escolaSalva = escolaSalva();
        when(escolas.existsByCnpj(connection, "04252011000110")).thenReturn(false);
        when(usuarios.existsByEmail(eq(connection), any())).thenReturn(false);
        when(escolas.save(eq(connection), any(Escola.class))).thenReturn(escolaSalva);
        EscolaService service = new EscolaService(escolas, usuarios, new PasswordService(), () -> connection, auditoriaService);
        service.adicionarObserver(observer);

        CriarEscolaResponseDTO resposta = service.cadastrarEscola(dtoValido(), superAdmin);

        var gestor = org.mockito.ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(eq(connection), gestor.capture(), eq(escolaSalva.getTenantId()));
        assertEquals(escolaSalva.getTenantId(), gestor.getValue().getTenantId());
        assertEquals(escolaSalva.getId(), gestor.getValue().getEscolaId());
        assertEquals(Perfil.GESTOR, gestor.getValue().getPerfil());
        assertTrue(gestor.getValue().isAtivo());
        assertFalse(gestor.getValue().isBloqueado());
        assertTrue(BCrypt.checkpw(resposta.getSenhaGeradaGestor(), gestor.getValue().getSenhaHash()));
        verify(connection).commit();
        verify(connection).setAutoCommit(true);
        var ordem = inOrder(connection, observer);
        ordem.verify(connection).commit();
        ordem.verify(observer).aoCadastrarEscola(eq(escolaSalva), any(Usuario.class), eq(resposta.getSenhaGeradaGestor()));
        verify(auditoriaService).registrar(eq(superAdmin), eq(escolaSalva.getTenantId()), eq("ESCOLA_CRIADA"),
                eq("ESCOLA"), eq(escolaSalva.getId()), eq("gestor_inicial_criado=true"));
        verify(auditoriaService).registrar(eq(superAdmin), eq(escolaSalva.getTenantId()), eq("USUARIO_CRIADO"),
                eq("USUARIO"), org.mockito.ArgumentMatchers.isNull(), eq("perfil=GESTOR;origem=CADASTRO_ESCOLA"));
    }

    private EscolaService service(EscolaRepository escolas, UsuarioRepository usuarios,
                                  PasswordService senha, Connection connection) {
        return new EscolaService(escolas, usuarios, senha, () -> connection);
    }

    private Connection connectionComAutoCommitAtivo() throws SQLException {
        Connection connection = mock(Connection.class);
        when(connection.getAutoCommit()).thenReturn(true);
        return connection;
    }

    private Escola escolaSalva() {
        Escola escola = new Escola();
        escola.setId(UUID.randomUUID());
        escola.setTenantId(UUID.randomUUID());
        escola.setNome("Escola de Teste");
        return escola;
    }

    private CriarEscolaDTO dtoValido() {
        CriarEscolaDTO dto = new CriarEscolaDTO();
        dto.setNome("Escola de Teste");
        dto.setCnpj("04.252.011/0001-10");
        dto.setEmailInstitucional("escola@test.com");
        dto.setEndereco("Rua de Teste, 10");
        dto.setBairro("Centro");
        dto.setCidade("Recife");
        dto.setEstado("PE");
        dto.setCep("50000-000");
        dto.setNomeResponsavel("Gestor de Teste");
        dto.setCpfResponsavel("529.982.247-25");
        dto.setTelefoneResponsavel("81999999999");
        dto.setEmailResponsavel("gestor@test.com");
        return dto;
    }
}
