package br.com.kutuar.seguranca.repositories;

import br.com.kutuar.seguranca.enums.EscolaStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.sql.*;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EscolaPaginacaoRepositoryTest {
    @Test
    void paginaSemResultadosRetornaListaVaziaComOrdenacaoDeterministica() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);
        EscolaRepository repository = new EscolaRepository() {
            @Override protected Connection getConnection() { return conn; }
        };

        assertEquals(List.of(), repository.findAllPaginated("inexistente", EscolaStatus.ATIVA, 20, 40));
        verify(conn).prepareStatement("SELECT * FROM escola WHERE 1 = 1 AND (nome ILIKE ? ESCAPE '!' OR cnpj ILIKE ? ESCAPE '!') AND status = ? ORDER BY nome ASC, id ASC LIMIT ? OFFSET ?");
        verify(stmt).setString(1, "%inexistente%");
        verify(stmt).setString(2, "%inexistente%");
        verify(stmt).setString(3, "ATIVA");
        verify(stmt).setInt(4, 20);
        verify(stmt).setInt(5, 40);
    }

    @Test
    void todasAsCombinacoesUsamOsMesmosFiltrosNaPaginaEContagem() throws Exception {
        for (String search : new String[]{null, "   ", "  Kutuar  ", "12.345", "x%' OR 1=1 --"}) {
            for (EscolaStatus status : new EscolaStatus[]{null, EscolaStatus.ATIVA, EscolaStatus.INATIVA}) {
                Connection conn = mock(Connection.class);
                PreparedStatement stmt = mock(PreparedStatement.class);
                ResultSet rs = mock(ResultSet.class);
                when(conn.prepareStatement(anyString())).thenReturn(stmt);
                when(stmt.executeQuery()).thenReturn(rs);
                UUID id = UUID.randomUUID();
                when(rs.next()).thenReturn(true, false, true);
                when(rs.getObject("id", UUID.class)).thenReturn(id);
                when(rs.getString("nome")).thenReturn("Kutuar");
                when(rs.getString("cnpj")).thenReturn("12.345");
                when(rs.getLong(1)).thenReturn(41L);
                EscolaRepository repository = new EscolaRepository() {
                    @Override protected Connection getConnection() { return conn; }
                };
                var escolas = repository.findAllPaginated(search, status, 20, 40);
                assertEquals(1, escolas.size());
                assertEquals(id, escolas.getFirst().getId());
                assertEquals("12.345", escolas.getFirst().getCnpj());
                assertEquals(41, repository.countFiltered(search, status));
                ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
                verify(conn, times(2)).prepareStatement(captor.capture());
                List<String> sql = captor.getAllValues();
                String where = " WHERE 1 = 1";
                int index = 1;
                if (search != null && !search.isBlank()) {
                    where += " AND (nome ILIKE ? ESCAPE '!' OR cnpj ILIKE ? ESCAPE '!')";
                    String pattern = "%" + search.trim().replace("%", "!%") + "%";
                    verify(stmt, times(2)).setString(index++, pattern);
                    verify(stmt, times(2)).setString(index++, pattern);
                    assertFalse(sql.getFirst().contains(search.trim()));
                }
                if (status != null) {
                    where += " AND status = ?";
                    verify(stmt, times(2)).setString(index++, status.name());
                }
                assertEquals("SELECT * FROM escola" + where + " ORDER BY nome ASC, id ASC LIMIT ? OFFSET ?", sql.get(0));
                assertEquals("SELECT COUNT(*) FROM escola" + where, sql.get(1));
                verify(stmt).setInt(index++, 20);
                verify(stmt).setInt(index, 40);
                verify(stmt, times(2)).close();
                verify(rs, times(2)).close();
            }
        }
    }
}
