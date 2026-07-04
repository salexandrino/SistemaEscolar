package br.com.synge.config;

import br.com.synge.administrativo.repositories.DashboardRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static HikariDataSource dataSource;

    private DatabaseConfig() throws SQLException {
        // Construtor privado para evitar instanciação
    }

    public static void init() {

        if (dataSource != null) {
            return;
        }

        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        if (url == null || url.isBlank()) {
            url = dotenv.get("DB_URL");
        }

        if (user == null || user.isBlank()) {
            user = dotenv.get("DB_USER");
        }

        if (password == null || password.isBlank()) {
            password = dotenv.get("DB_PASSWORD");
        }

        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);

        config.setDriverClassName("org.postgresql.Driver");

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        try {

            dataSource = new HikariDataSource(config);

            logger.info("HikariCP inicializado com sucesso.");

        } catch (Exception e) {

            logger.error("Erro ao inicializar HikariCP.", e);

            throw e;
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource não inicializado. Chame DatabaseConfig.init() primeiro.");
        }
        return dataSource.getConnection();
    }

    public static void closeDataSource() {
        if (dataSource != null) {
            dataSource.close();
            logger.info("HikariCP DataSource fechado.");
        }
    }

}
