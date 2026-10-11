package com.seatomatic.common.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Manages database connection acquisition and lifecycle pooling via JNDI.
 * Supports standard container lookups (java:/comp/env/jdbc/seatomatic),
 * fallback candidates, test datasource injection, and transaction context awareness.
 */
public final class DBConnection implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBConnection.class);

    public static final String PRIMARY_JNDI_NAME = "java:/comp/env/jdbc/seatomatic";
    private static final String[] JNDI_CANDIDATE_NAMES = {
            "java:/comp/env/jdbc/seatomatic",
            "java:comp/env/jdbc/seatomatic",
            "jdbc/seatomatic"
    };

    private static volatile DataSource testDataSource;
    private static volatile DataSource cachedDataSource;

    private final Connection connection;
    private final boolean managedByTransaction;

    private DBConnection(Connection connection, boolean managedByTransaction) {
        this.connection = connection;
        this.managedByTransaction = managedByTransaction;
    }

    /**
     * Obtains a DBConnection handle. If an active transaction exists on the current thread,
     * it binds to that connection and prevents auto-closing. Otherwise, it pulls a new
     * connection from the JNDI DataSource.
     */
    public static DBConnection open() throws SQLException {
        Connection txConnection = TransactionManager.currentConnection();
        if (txConnection != null && !txConnection.isClosed()) {
            return new DBConnection(txConnection, true);
        }

        DataSource dataSource = getDataSource();
        Connection conn = dataSource.getConnection();
        return new DBConnection(conn, false);
    }

    /**
     * Resolves the DataSource using the test override or JNDI candidate lookups.
     */
    public static DataSource getDataSource() throws SQLException {
        if (testDataSource != null) {
            return testDataSource;
        }
        if (cachedDataSource != null) {
            return cachedDataSource;
        }

        synchronized (DBConnection.class) {
            if (testDataSource != null) {
                return testDataSource;
            }
            if (cachedDataSource != null) {
                return cachedDataSource;
            }

            NamingException lastNamingException = null;
            try {
                InitialContext ctx = new InitialContext();
                for (String jndiName : JNDI_CANDIDATE_NAMES) {
                    try {
                        Object lookup = ctx.lookup(jndiName);
                        if (lookup instanceof DataSource ds) {
                            LOGGER.info("Resolved database DataSource via JNDI: {}", jndiName);
                            cachedDataSource = ds;
                            return ds;
                        }
                    } catch (NamingException e) {
                        lastNamingException = e;
                        LOGGER.debug("JNDI lookup attempt for '{}' not resolved: {}", jndiName, e.getMessage());
                    }
                }
            } catch (NamingException e) {
                lastNamingException = e;
            }

            throw new SQLException(
                    "Unable to resolve JNDI DataSource. Attempted candidates: " + Arrays.toString(JNDI_CANDIDATE_NAMES),
                    lastNamingException
            );
        }
    }

    /**
     * Returns the underlying JDBC connection.
     */
    public Connection connection() {
        return connection;
    }

    /**
     * Alias for {@link #connection()} for standard JDBC API idioms.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Injects a DataSource for unit/integration testing outside of container JNDI.
     */
    public static void setDataSourceForTesting(DataSource dataSource) {
        testDataSource = dataSource;
    }

    /**
     * Clears the test DataSource override and cached datasource.
     */
    public static void resetDataSourceForTesting() {
        testDataSource = null;
        cachedDataSource = null;
    }

    @Override
    public void close() throws SQLException {
        if (!managedByTransaction && connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
