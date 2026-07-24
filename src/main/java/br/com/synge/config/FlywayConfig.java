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
                    .baselineVersion("0")
                    .validateOnMigrate(true)
                    // 🛠️ ADICIONE APENAS ESTA LINHA ABAIXO:
                    .outOfOrder(true)          // 🔥 Permite que a V4 e V5 rodem mesmo se a V6+ já existir
                    .load();

            // 🔧 CORREÇÃO TEMPORÁRIA: o banco de produção (eq14) está com o checksum
            // de V4/V5 desatualizado (arquivos foram editados após já terem sido
            // aplicados). O repair() apenas atualiza o checksum gravado no
            // flyway_schema_history para bater com os arquivos atuais — ele NÃO
            // re-executa nenhum SQL. Depois que subir com sucesso uma vez em
            // produção, esta linha pode ser removida com segurança.
            logger.info("Executando flyway.repair() para corrigir checksums desatualizados (V4/V5)...");
            flyway.repair();

            logger.info("Aplicando as migrações na nuvem...");
            flyway.migrate();

            logger.info("Flyway migrations aplicadas com sucesso.");
        } catch (Exception e) {
            logger.error("FALHA CRÍTICA NO FLYWAY: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}