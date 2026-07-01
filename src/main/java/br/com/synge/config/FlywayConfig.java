package br.com.synge.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class FlywayConfig {

    private static final Logger logger = LoggerFactory.getLogger(FlywayConfig.class);

    private FlywayConfig() {
        // Construtor privado
    }

    public static void migrate() {
        try (Connection connection = DatabaseConfig.getConnection()) {
            // O Flyway precisa de um DataSource para operar.
            // Usamos o DataSource configurado no DatabaseConfig.
            Dotenv dotenv = Dotenv.load();

            Flyway flyway = Flyway.configure()
                    .dataSource(
                            dotenv.get("DB_URL"),
                            dotenv.get("DB_USER"),
                            dotenv.get("DB_PASSWORD")
                    )
                    .locations("classpath:db/migration")
                    .load();

            flyway.migrate();
            logger.info("Flyway migrations aplicadas com sucesso.");
        } catch (SQLException e) {
            logger.error("Erro de SQL ao obter conexão para Flyway: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao inicializar o banco de dados com Flyway (SQL Exception).", e);
        } catch (Exception e) {
            logger.error("Erro ao aplicar Flyway migrations: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao inicializar o banco de dados com Flyway.", e);
        }
    }
}
