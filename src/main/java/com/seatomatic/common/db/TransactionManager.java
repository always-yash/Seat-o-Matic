package com.seatomatic.common.db;

import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager implements AutoCloseable {

    private static final ThreadLocal<Connection> CONNECTION_HOLDER = new ThreadLocal<>();

    public void begin() throws SQLException, NamingException {
        DataSource dataSource = DataSourceProvider.getDataSource();
        Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        CONNECTION_HOLDER.set(connection);
    }

    public static Connection currentConnection() {
        return CONNECTION_HOLDER.get();
    }

    public void commit() throws SQLException {
        Connection connection = currentConnection();
        if (connection != null) {
            connection.commit();
        }
    }

    public void rollback() throws SQLException {
        Connection connection = currentConnection();
        if (connection != null) {
            connection.rollback();
        }
    }

    @Override
    public void close() throws SQLException {
        Connection connection = currentConnection();
        if (connection != null) {
            try {
                connection.setAutoCommit(true);
                connection.close();
            } finally {
                CONNECTION_HOLDER.remove();
            }
        }
    }
}
