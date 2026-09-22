package br.com.kutuar.seguranca.repositories;

import br.com.kutuar.seguranca.enums.EscolaStatus;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Opt-in: usa tabela temporária e rollback, sem tocar o banco de desenvolvimento. */
@EnabledIfEnvironmentVariable(named = "KUTUAR_DASHBOARD_DB_TEST", matches = "true")
class EscolaPostgresTest {
    private Connection connection;
    private EscolaRepository repository;

    @BeforeEach void preparar() throws Exception {
        var env = Dotenv.configure().ignoreIfMissing().load();
        connection = DriverManager.getConnection(env.get("DB_URL"), env.get("DB_USER"), env.get("DB_PASSWORD"));
        connection.setAutoCommit(false);
        try (var statement = connection.createStatement()) {
            statement.execute("CREATE TEMP TABLE escola (id uuid PRIMARY KEY, status text NOT NULL, atualizado_em timestamp)");
        }
        Connection emprestimo = (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{Connection.class}, (proxy, method, args) -> {
                    if (method.getName().equals("close")) return null;
                    try { return method.invoke(connection, args); }
                    catch (InvocationTargetException e) { throw e.getCause(); }
                });
        repository = new EscolaRepository() {
            @Override protected Connection getConnection() { return emprestimo; }
        };
    }

    @AfterEach void fechar() throws Exception {
        if (connection != null) { try { connection.rollback(); } finally { connection.close(); } }
    }

    @Test void atualizaStatusSomenteNoEstadoEsperado() throws Exception {
        UUID id = UUID.randomUUID();
        try (var statement = connection.prepareStatement("INSERT INTO escola (id, status) VALUES (?, 'INATIVA')")) {
            statement.setObject(1, id);
            statement.executeUpdate();
        }
        LocalDateTime atualizadoEm = LocalDateTime.now().withNano(0);

        assertTrue(repository.updateStatus(id, EscolaStatus.INATIVA, EscolaStatus.ATIVA, atualizadoEm));
        assertFalse(repository.updateStatus(id, EscolaStatus.INATIVA, EscolaStatus.ATIVA, atualizadoEm.plusSeconds(1)));

        try (var statement = connection.prepareStatement("SELECT status, atualizado_em FROM escola WHERE id = ?")) {
            statement.setObject(1, id);
            var resultado = statement.executeQuery();
            assertTrue(resultado.next());
            assertEquals("ATIVA", resultado.getString("status"));
            assertEquals(atualizadoEm, resultado.getObject("atualizado_em", LocalDateTime.class));
        }
    }
}
