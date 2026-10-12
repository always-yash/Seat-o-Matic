package com.lostfound.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database Initializer and Bootstrapper.
 * Automatically checks and initializes required tables, seed data,
 * and schema upgrades on first startup.
 */
public class DatabaseInitializer {

    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializer.class.getName());
    private static volatile boolean initialized = false;

    private DatabaseInitializer() {
        // Utility class
    }

    public static synchronized void initializeIfNeeded(Connection conn) {
        if (initialized || conn == null) {
            return;
        }

        try {
            boolean tablesExist = checkUsersTableExists(conn);
            if (!tablesExist) {
                LOGGER.info("Users table not detected. Initializing database schema and seed data...");
                executeSchemaScript(conn);
                LOGGER.info("Database schema and seed records initialized successfully.");
            } else {
                LOGGER.info("Database tables verified.");
            }

            // Always run migrations to ensure schema is up-to-date with latest auth & OTP features
            runSchemaMigrations(conn);

            initialized = true;
            LOGGER.info("Database ready with verified schema.");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed during automatic database schema initialization", e);
        }
    }

    private static boolean checkUsersTableExists(Connection conn) {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT 1 FROM users LIMIT 1")) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void runSchemaMigrations(Connection conn) {
        String[] migrations = {
                "ALTER TABLE users ADD COLUMN student_id VARCHAR(50) DEFAULT NULL",
                "ALTER TABLE users ADD COLUMN google_id VARCHAR(100) DEFAULT NULL",
                "ALTER TABLE users ADD COLUMN profile_image VARCHAR(500) DEFAULT NULL",
                "ALTER TABLE users ADD COLUMN email_verified BOOLEAN DEFAULT TRUE",
                "ALTER TABLE users ADD COLUMN auth_provider VARCHAR(20) DEFAULT 'LOCAL'",
                "CREATE TABLE IF NOT EXISTS otps (" +
                        "otp_id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                        "email VARCHAR(150) NOT NULL, " +
                        "otp_code VARCHAR(20) NOT NULL, " +
                        "otp_purpose VARCHAR(50) NOT NULL, " +
                        "reference_id BIGINT DEFAULT NULL, " +
                        "temp_data TEXT DEFAULT NULL, " +
                        "attempts INT DEFAULT 0, " +
                        "expires_at TIMESTAMP NOT NULL, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
        };

        for (String sql : migrations) {
            try (Statement st = conn.createStatement()) {
                st.execute(sql);
            } catch (Exception ignored) {
                // Ignore if column or table already exists
            }
        }
    }

    private static void executeSchemaScript(Connection conn) {
        try (InputStream in = DatabaseInitializer.class.getClassLoader().getResourceAsStream("schema.sql")) {
            if (in == null) {
                LOGGER.warning("schema.sql not found on classpath!");
                return;
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("--") || trimmed.startsWith("//") || trimmed.isEmpty()) {
                        continue;
                    }
                    sb.append(line).append("\n");
                }
            }

            String[] statements = sb.toString().split(";");
            try (Statement stmt = conn.createStatement()) {
                for (String rawSql : statements) {
                    String sql = rawSql.trim();
                    if (sql.isEmpty()) {
                        continue;
                    }
                    try {
                        stmt.execute(sql);
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "SQL execution warning on statement [" + 
                                   (sql.length() > 50 ? sql.substring(0, 50) + "..." : sql) + "]: " + ex.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error executing schema script", e);
        }
    }
}
