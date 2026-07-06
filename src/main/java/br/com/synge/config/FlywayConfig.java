package br.com.synge.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.SQLException;

public class FlywayConfig {
    private static final Logger logger = LoggerFactory.getLogger(FlywayConfig.class);

    public static void migrate() {
        try (Connection connection = DatabaseConfig.getConnection()) {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

            String dbUrl = System.getenv("DB_URL");
            if (dbUrl == null || dbUrl.isBlank()) dbUrl = dotenv.get("DB_URL");

            String dbUser = System.getenv("DB_USER");
            if (dbUser == null || dbUser.isBlank()) dbUser = dotenv.get("DB_USER");

            String dbPassword = System.getenv("DB_PASSWORD");
            if (dbPassword == null || dbPassword.isBlank()) dbPassword = dotenv.get("DB_PASSWORD");

            Flyway flyway = Flyway.configure()
                    .dataSource(dbUrl, dbUser, dbPassword)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .baselineVersion("0")      // 🔥 Banco novo => aplica TUDO a partir da V1
                    .validateOnMigrate(true)   // 🔥 Reativado conforme instrução do professor
                    .load();

            logger.info("Aplicando as migrações na nuvem...");
            flyway.migrate(); // 🔥 Cria as tabelas oficialmente

            logger.info("Flyway migrations aplicadas com sucesso.");
        } catch (Exception e) {
            logger.error("FALHA CRÍTICA NO FLYWAY: {}", e.getMessage(), e);
            throw new RuntimeException(e); // Garante que se der erro, você veja o motivo real nos logs
        }
    }
}