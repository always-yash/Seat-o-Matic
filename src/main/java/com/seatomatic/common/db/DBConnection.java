package com.seatomatic.common.db;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public final class DBConnection implements AutoCloseable {
    private static final String JNDI_NAME = "java:comp/env/jdbc/seatomatic";
    private final Connection connection;

    private DBConnection(Connection connection) {
        this.connection = connection;
    }

    public static DBConnection open() throws SQLException {
        try {
            Object resource = new InitialContext().lookup(JNDI_NAME);
            if (!(resource instanceof DataSource dataSource)) {
                throw new SQLException("JNDI resource is not a DataSource: " + JNDI_NAME);
            }
            return new DBConnection(dataSource.getConnection());
        } catch (NamingException ex) {
            throw new SQLException("Unable to look up database resource " + JNDI_NAME, ex);
        }
    }

    public Connection connection() {
        return connection;
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }
}
