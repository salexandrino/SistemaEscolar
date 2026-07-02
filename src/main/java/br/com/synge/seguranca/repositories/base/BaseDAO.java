package br.com.synge.seguranca.repositories.base;

import br.com.synge.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class BaseDAO {

    protected Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }
}
