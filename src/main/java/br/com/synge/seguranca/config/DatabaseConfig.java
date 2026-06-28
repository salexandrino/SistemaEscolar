package br.com.synge.seguranca.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;

public class DatabaseConfig {

    private static HikariDataSource dataSource;

    public static HikariDataSource getDataSource() {

        if (dataSource == null) {

            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            String url = System.getenv("DB_URL");
            String user = System.getenv("DB_USER");
            String password = System.getenv("DB_PASSWORD");

            if (url == null) {
                url = dotenv.get("DB_URL");
                user = dotenv.get("DB_USER");
                password = dotenv.get("DB_PASSWORD");
            }

            HikariConfig cfg = new HikariConfig();
            cfg.setJdbcUrl(url);
            cfg.setUsername(user);
            cfg.setPassword(password);
            cfg.setMaximumPoolSize(5);

            dataSource = new HikariDataSource(cfg);
        }

        return dataSource;
    }
}