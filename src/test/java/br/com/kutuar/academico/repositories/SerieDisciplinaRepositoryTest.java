package br.com.kutuar.academico.repositories;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SerieDisciplinaRepositoryTest {

    @Test
    void existenciaEhFiltradaPeloTenantAutenticado() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(connection.prepareStatement("SELECT 1 FROM serie_disciplina WHERE tenant_id = ? AND id_serie = ? AND id_disciplina = ? LIMIT 1"))
                .thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        SerieDisciplinaRepository repository = repositoryCom(connection);
        UUID tenantA = UUID.randomUUID();
        UUID idSerie = UUID.randomUUID();
        UUID idDisciplina = UUID.randomUUID();

        assertFalse(repository.existeNaMatriz(tenantA, idSerie, idDisciplina));

        verify(statement).setObject(1, tenantA);
        verify(statement).setObject(2, idSerie);
        verify(statement).setObject(3, idDisciplina);
    }

    @Test
    void cargaHorariaDeOutroTenantNaoEhRetornada() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(connection.prepareStatement("SELECT carga_horaria_anual FROM serie_disciplina WHERE tenant_id = ? AND id_serie = ? AND id_disciplina = ?"))
                .thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        SerieDisciplinaRepository repository = repositoryCom(connection);
        UUID tenantA = UUID.randomUUID();
        UUID idSerie = UUID.randomUUID();
        UUID idDisciplina = UUID.randomUUID();

        assertEquals(Optional.empty(), repository.obterCargaHorariaAnual(tenantA, idSerie, idDisciplina));

        verify(statement).setObject(1, tenantA);
        verify(statement).setObject(2, idSerie);
        verify(statement).setObject(3, idDisciplina);
    }

    @Test
    void mesmoTenantConsultaSuaRelacaoERemocaoPermaneceIsolada() throws Exception {
        Connection connection = mock(Connection.class);
        PreparedStatement selectExistencia = mock(PreparedStatement.class);
        PreparedStatement selectCarga = mock(PreparedStatement.class);
        PreparedStatement delete = mock(PreparedStatement.class);
        ResultSet existencia = mock(ResultSet.class);
        ResultSet carga = mock(ResultSet.class);
        UUID tenantA = UUID.randomUUID();
        UUID idSerie = UUID.randomUUID();
        UUID idDisciplina = UUID.randomUUID();
        when(connection.prepareStatement("SELECT 1 FROM serie_disciplina WHERE tenant_id = ? AND id_serie = ? AND id_disciplina = ? LIMIT 1"))
                .thenReturn(selectExistencia);
        when(connection.prepareStatement("SELECT carga_horaria_anual FROM serie_disciplina WHERE tenant_id = ? AND id_serie = ? AND id_disciplina = ?"))
                .thenReturn(selectCarga);
        when(connection.prepareStatement("DELETE FROM serie_disciplina WHERE tenant_id = ? AND id_serie = ? AND id_disciplina = ?"))
                .thenReturn(delete);
        when(selectExistencia.executeQuery()).thenReturn(existencia);
        when(existencia.next()).thenReturn(true);
        when(selectCarga.executeQuery()).thenReturn(carga);
        when(carga.next()).thenReturn(true);
        when(carga.getInt(1)).thenReturn(160);
        SerieDisciplinaRepository repository = repositoryCom(connection);

        assertTrue(repository.existeNaMatriz(tenantA, idSerie, idDisciplina));
        assertEquals(Optional.of(160), repository.obterCargaHorariaAnual(tenantA, idSerie, idDisciplina));
        repository.remover(tenantA, idSerie, idDisciplina);

        verify(delete).setObject(1, tenantA);
        verify(delete).setObject(2, idSerie);
        verify(delete).setObject(3, idDisciplina);
    }

    private SerieDisciplinaRepository repositoryCom(Connection connection) {
        return new SerieDisciplinaRepository() {
            @Override
            protected Connection getConnection() {
                return connection;
            }
        };
    }
}
