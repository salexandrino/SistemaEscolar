package br.com.synge.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class FlywayConfig {

    private static final Logger logger = LoggerFactory.getLogger(FlywayConfig.class);

    private FlywayConfig() {}

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
                    .baselineVersion("0") // 🔥 MUDADO PARA 0 para ele ler a V1, V2, V3...
                    .validateOnMigrate(false)
                    .load();
            flyway.repair();
            flyway.migrate();

            logger.info("Reparando o histórico do Flyway no servidor...");
            flyway.repair();

            logger.info("Criando/atualizando as tabelas na nuvem...");
            flyway.migrate();

            logger.info("Flyway migrations aplicadas com sucesso.");
        } catch (SQLException e) {
            logger.error("Erro de SQL no Flyway: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            logger.error("Erro ao aplicar Flyway: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}