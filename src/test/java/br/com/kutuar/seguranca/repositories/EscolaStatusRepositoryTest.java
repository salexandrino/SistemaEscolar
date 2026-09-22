package br.com.kutuar.seguranca.repositories;

import br.com.kutuar.seguranca.enums.EscolaStatus;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class EscolaStatusRepositoryTest {
    @Test void atualizaApenasStatusEDataComPreparedStatement() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);
        EscolaRepository repository = new EscolaRepository() {
            @Override protected Connection getConnection() { return connection; }
        };
        UUID id = UUID.randomUUID();
        LocalDateTime atualizadoEm = LocalDateTime.now();

        assertTrue(repository.updateStatus(id, EscolaStatus.INATIVA, EscolaStatus.ATIVA, atualizadoEm));

        verify(connection).prepareStatement("UPDATE escola SET status = ?, atualizado_em = ? WHERE id = ? AND status = ?");
        verify(statement).setString(1, "ATIVA");
        verify(statement).setObject(2, atualizadoEm);
        verify(statement).setObject(3, id);
        verify(statement).setString(4, "INATIVA");
        verify(statement).close();
    }
}
