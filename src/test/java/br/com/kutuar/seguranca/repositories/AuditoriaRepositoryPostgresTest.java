package br.com.kutuar.seguranca.repositories;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnabledIfEnvironmentVariable(named = "KUTUAR_AUDIT_DB_TEST", matches = "true")
class AuditoriaRepositoryPostgresTest {

    private Connection connection;
    private AuditoriaRepository repository;

    @BeforeEach
    void preparar() throws Exception {
        var env = Dotenv.configure().ignoreIfMissing().load();
        connection = DriverManager.getConnection(env.get("DB_URL"), env.get("DB_USER"), env.get("DB_PASSWORD"));
        connection.setAutoCommit(false);
        executar("CREATE TEMP TABLE usuario (id uuid PRIMARY KEY, nome_completo text, email text)");
        executar("CREATE TEMP TABLE auditoria (id uuid PRIMARY KEY, executor_id uuid, criado_em timestamp without time zone NOT NULL, acao text NOT NULL, entidade text NOT NULL, detalhes text)");
        Connection borrowedConnection = (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{Connection.class}, (proxy, method, args) -> {
                    if (method.getName().equals("close")) return null;
                    try {
                        return method.invoke(connection, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
        repository = new AuditoriaRepository() {
            @Override
            protected Connection getConnection() {
                return borrowedConnection;
            }
        };
    }

    @AfterEach
    void fechar() throws Exception {
        if (connection != null) {
            try {
                connection.rollback();
            } finally {
                connection.close();
            }
        }
    }

    @Test
    void filtraPeriodoOrdenaEstavelmenteEPreservaFallbackDoExecutor() throws Exception {
        UUID executorId = UUID.randomUUID();
        UUID id1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID id2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID id3 = UUID.fromString("00000000-0000-0000-0000-000000000003");
        LocalDateTime noPeriodo = LocalDateTime.of(2026, 9, 30, 23, 59, 59);
        executar("INSERT INTO usuario (id, nome_completo, email) VALUES ('" + executorId + "', 'Admin Kutuar', 'admin@example.test')");
        inserirEvento(id1, executorId, noPeriodo, "LOGIN", "USUARIO");
        inserirEvento(id2, UUID.randomUUID(), noPeriodo, "LOGIN", "USUARIO");
        inserirEvento(id3, null, noPeriodo, "LOGIN", "USUARIO");
        inserirEvento(UUID.randomUUID(), null, LocalDateTime.of(2026, 10, 1, 0, 0), "LOGIN", "USUARIO");
        inserirEvento(UUID.randomUUID(), null, noPeriodo, "EDITAR", "ESCOLA");

        var inicio = LocalDateTime.of(2026, 9, 1, 0, 0);
        var fimExclusivo = LocalDateTime.of(2026, 10, 1, 0, 0);
        assertEquals(3, repository.countFiltered("login", "usuario", inicio, fimExclusivo));
        assertEquals("Sistema", repository.findFiltered("LOGIN", "USUARIO", inicio, fimExclusivo, 1, 0).getFirst().executor());
        assertEquals("Usuário não disponível", repository.findFiltered("LOGIN", "USUARIO", inicio, fimExclusivo, 1, 1).getFirst().executor());
        assertEquals("Admin Kutuar", repository.findFiltered("LOGIN", "USUARIO", inicio, fimExclusivo, 1, 2).getFirst().executor());
        assertEquals(id3, repository.findFiltered("LOGIN", "USUARIO", inicio, fimExclusivo, 1, 0).getFirst().id());
    }

    private void executar(String sql) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private void inserirEvento(UUID id, UUID executorId, LocalDateTime criadoEm, String acao, String entidade) throws SQLException {
        String sql = "INSERT INTO auditoria (id, executor_id, criado_em, acao, entidade, detalhes) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            statement.setObject(2, executorId);
            statement.setTimestamp(3, Timestamp.valueOf(criadoEm));
            statement.setString(4, acao);
            statement.setString(5, entidade);
            statement.setString(6, "detalhe");
            statement.executeUpdate();
        }
    }
}