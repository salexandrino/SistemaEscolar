package br.com.kutuar.seguranca.repositories;

import br.com.kutuar.seguranca.services.PasswordService;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioRepositoryUpdatePasswordTest {

    private static final String UPDATE_PASSWORD_SQL =
            "UPDATE usuario SET senha_hash = ?, reset_password_token = NULL, reset_password_expires_at = NULL, atualizado_em = ? " +
                    "WHERE id = ? AND tenant_id IS NOT DISTINCT FROM ?";

    @Test
    void superAdminGlobalComTenantNuloAtualizaExatamenteUmRegistro() throws Exception {
        Cenario cenario = cenarioComLinhasAfetadas(1);
        UUID usuarioId = UUID.randomUUID();
        String senhaHash = new PasswordService().hash("NovaSenha@123");

        assertDoesNotThrow(() -> cenario.repository.updatePassword(usuarioId, null, senhaHash));

        verificarParametros(cenario, usuarioId, null, senhaHash);
        assertTrue(new PasswordService().verificar("NovaSenha@123", senhaHash));
    }

    @Test
    void gestorComTenantCorretoAtualiza() throws Exception {
        Cenario cenario = cenarioComLinhasAfetadas(1);
        UUID usuarioId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();

        assertDoesNotThrow(() -> cenario.repository.updatePassword(usuarioId, tenantId, "$2a$10$hashDeTeste"));

        verificarParametros(cenario, usuarioId, tenantId, "$2a$10$hashDeTeste");
    }

    @Test
    void tenantDiferenteNaoAtualiza() throws Exception {
        Cenario cenario = cenarioComLinhasAfetadas(0);

        assertThrows(RuntimeException.class,
                () -> cenario.repository.updatePassword(UUID.randomUUID(), UUID.randomUUID(), "$2a$10$hashDeTeste"));

        verify(cenario.statement).executeUpdate();
    }

    @Test
    void usuarioInexistenteNaoAtualiza() throws Exception {
        Cenario cenario = cenarioComLinhasAfetadas(0);

        assertThrows(RuntimeException.class,
                () -> cenario.repository.updatePassword(UUID.randomUUID(), UUID.randomUUID(), "$2a$10$hashDeTeste"));

        verify(cenario.statement).executeUpdate();
    }

    private Cenario cenarioComLinhasAfetadas(int linhasAfetadas) throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(linhasAfetadas);
        UsuarioRepository repository = new UsuarioRepository() {
            @Override
            protected Connection getConnection() {
                return connection;
            }
        };
        return new Cenario(repository, connection, statement);
    }

    private void verificarParametros(Cenario cenario, UUID usuarioId, UUID tenantId, String senhaHash) throws Exception {
        verify(cenario.connection).prepareStatement(UPDATE_PASSWORD_SQL);
        verify(cenario.statement).setString(1, senhaHash);
        verify(cenario.statement).setObject(3, usuarioId);
        verify(cenario.statement).setObject(4, tenantId);
        verify(cenario.statement).executeUpdate();
    }

    private record Cenario(UsuarioRepository repository, Connection connection, PreparedStatement statement) {
    }
}
