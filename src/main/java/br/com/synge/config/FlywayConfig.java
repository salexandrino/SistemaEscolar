package br.com.synge.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlywayConfig {

    private static final Logger logger = LoggerFactory.getLogger(FlywayConfig.class);

    private FlywayConfig() {}

    public static void migrate() {
        try {
            // 🔥 REUTILIZAÇÃO EXATA: Pegamos o pool do HikariCP que já está conectado com sucesso
            var hikariDataSource = DatabaseConfig.getDataSource();

            if (hikariDataSource == null) {
                throw new IllegalStateException("O DataSource do HikariCP não foi inicializado antes do Flyway!");
            }

            Flyway flyway = Flyway.configure()
                    .dataSource(hikariDataSource) // Passa o pool pronto do Hikari aqui
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .baselineVersion("0")
                    .validateOnMigrate(false)
                    .load();

            logger.info("Executando Flyway Repair com o DataSource do Hikari...");
            flyway.repair();

            logger.info("Aplicando as migrações (V1 ao V11) usando o pool do Hikari...");
            flyway.migrate();

            logger.info("Flyway migrations aplicadas com sucesso total.");
        } catch (Exception e) {
            logger.error("❌ Erro crítico ao aplicar Flyway com HikariCP: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}