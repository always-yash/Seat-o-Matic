package com.lostfound.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Resilient JDBC Database Connection Manager.
 * Connects to MySQL with automatic table bootstrapping and provides
 * an embedded database fallback to guarantee zero-downtime execution.
 */
public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static volatile boolean useH2Fallback = false;
    private static final String H2_URL = "jdbc:h2:./lost_found_db_data;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;AUTO_SERVER=TRUE";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            LOGGER.info("MySQL JDBC Driver registered.");
        } catch (ClassNotFoundException e) {
            LOGGER.warning("MySQL driver not found in classpath.");
        }
        try {
            Class.forName("org.h2.Driver");
            LOGGER.info("H2 embedded fallback Driver registered.");
        } catch (ClassNotFoundException e) {
            LOGGER.warning("H2 driver not found in classpath.");
        }
    }

    private DatabaseConnection() {
        // Utility class
    }

    /**
     * Obtains a functional database connection.
     * Tries configured MySQL first; if unavailable, seamlessly activates embedded database.
     */
    public static Connection getConnection() throws SQLException {
        if (useH2Fallback) {
            Connection h2Conn = DriverManager.getConnection(H2_URL, "sa", "");
            DatabaseInitializer.initializeIfNeeded(h2Conn);
            return h2Conn;
        }

        String url = AppConfig.getDbUrl();
        String username = AppConfig.getDbUsername();
        String password = AppConfig.getDbPassword();

        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            DatabaseInitializer.initializeIfNeeded(conn);
            return conn;
        } catch (SQLException e) {
            String errorMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";

            // If database name is missing, attempt to create it on MySQL server
            if (errorMsg.contains("unknown database") && url.contains("/lost_found_db")) {
                try {
                    String baseUrl = url.substring(0, url.indexOf("/lost_found_db")) + "/?useSSL=false&allowPublicKeyRetrieval=true";
                    try (Connection serverConn = DriverManager.getConnection(baseUrl, username, password);
                         Statement stmt = serverConn.createStatement()) {
                        stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS lost_found_db CHARACTER SET utf8mb4");
                        LOGGER.info("Auto-created MySQL database 'lost_found_db'.");
                    }
                    Connection conn = DriverManager.getConnection(url, username, password);
                    DatabaseInitializer.initializeIfNeeded(conn);
                    return conn;
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "Could not auto-create MySQL database: " + ex.getMessage());
                }
            }

            // Fallback to embedded persistent H2 database
            LOGGER.log(Level.WARNING, "MySQL connection unsuccessful (" + e.getMessage() + 
                       "). Activating embedded persistent database fallback to maintain 100% portal functionality.");
            useH2Fallback = true;
            Connection h2Conn = DriverManager.getConnection(H2_URL, "sa", "");
            DatabaseInitializer.initializeIfNeeded(h2Conn);
            return h2Conn;
        }
    }

    /**
     * Checks whether the application is running on embedded fallback database.
     */
    public static boolean isEmbeddedFallbackActive() {
        return useH2Fallback;
    }

    /**
     * Safely closes an AutoCloseable resource (Connection, Statement, ResultSet).
     */
    public static void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error closing database resource", e);
            }
        }
    }
}
