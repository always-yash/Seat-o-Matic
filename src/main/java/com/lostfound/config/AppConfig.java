package com.lostfound.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enterprise Application Configuration Manager.
 * Loads configuration from:
 * 1. Java System Properties (-Dkey=value)
 * 2. Operating System Environment Variables
 * 3. Local .env file (root / current dir)
 * 4. Classpath db.properties
 */
public class AppConfig {

    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());
    private static final Properties properties = new Properties();

    static {
        // 1. Load from classpath db.properties
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                properties.load(input);
                LOGGER.info("db.properties loaded from classpath.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load db.properties", e);
        }

        // 2. Load from .env file if available
        loadDotEnvFile();
    }

    private static void loadDotEnvFile() {
        String[] potentialPaths = {
                ".env",
                "../.env",
                System.getProperty("user.dir") + "/.env",
                System.getProperty("user.dir") + "/Campus/.env",
                System.getProperty("catalina.base", "") + "/.env"
        };

        for (String path : potentialPaths) {
            if (path == null || path.isBlank()) continue;
            File envFile = new File(path);
            if (envFile.exists() && envFile.isFile()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(envFile, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) {
                            continue;
                        }
                        int eqIdx = line.indexOf('=');
                        if (eqIdx > 0) {
                            String k = line.substring(0, eqIdx).trim();
                            String v = line.substring(eqIdx + 1).trim();
                            // Strip outer quotes if any
                            if ((v.startsWith("\"") && v.endsWith("\"")) || (v.startsWith("'") && v.endsWith("'"))) {
                                v = v.substring(1, v.length() - 1);
                            }
                            if (!k.isEmpty()) {
                                properties.setProperty(k, v);
                                // Also store with dot notation for versatility
                                properties.setProperty(k.toLowerCase().replace('_', '.'), v);
                            }
                        }
                    }
                    LOGGER.info("Successfully loaded environment variables from: " + envFile.getAbsolutePath());
                    return;
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error reading .env at " + path, e);
                }
            }
        }
    }

    public static String getProperty(String key, String defaultValue) {
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp;
        }

        String envKey = key.replace('.', '_').toUpperCase();
        String envVar = System.getenv(envKey);
        if (envVar != null && !envVar.isBlank()) {
            return envVar;
        }

        // Direct lookup or uppercase env-style lookup in properties
        String propVal = properties.getProperty(key);
        if (propVal != null && !propVal.isBlank()) {
            return propVal;
        }

        String envPropVal = properties.getProperty(envKey);
        if (envPropVal != null && !envPropVal.isBlank()) {
            return envPropVal;
        }

        return defaultValue;
    }

    public static String getDbUrl() {
        return getProperty("db.url", getProperty("DB_URL", "jdbc:mysql://localhost:3306/lost_found_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata&characterEncoding=UTF-8"));
    }

    public static String getDbUsername() {
        return getProperty("db.username", getProperty("DB_USERNAME", "root"));
    }

    public static String getDbPassword() {
        return getProperty("db.password", getProperty("DB_PASSWORD", "New@123"));
    }

    public static String getUploadDir() {
        return getProperty("upload.dir", getProperty("UPLOAD_DIR", "uploads/items"));
    }

    public static int getOtpExpiryMinutes() {
        return Integer.parseInt(getProperty("otp.expiry.minutes", getProperty("OTP_EXPIRY_MINUTES", "5")));
    }

    public static int getSessionTimeoutMinutes() {
        return Integer.parseInt(getProperty("session.timeout.minutes", getProperty("SESSION_TIMEOUT_MINUTES", "30")));
    }

    public static String getGoogleClientId() {
        return getProperty("google.client.id", getProperty("GOOGLE_CLIENT_ID", ""));
    }

    public static String getGoogleClientSecret() {
        return getProperty("google.client.secret", getProperty("GOOGLE_CLIENT_SECRET", ""));
    }

    public static String getGoogleCallbackUrl() {
        return getProperty("google.callback.url", getProperty("GOOGLE_CALLBACK_URL", "http://localhost:8080/LostFoundManagementSystem/api/auth/google/callback"));
    }

    public static String getGmailUser() {
        return getProperty("gmail.user", getProperty("GMAIL_USER", ""));
    }

    public static String getGmailAppPassword() {
        return getProperty("gmail.app.password", getProperty("GMAIL_APP_PASSWORD", ""));
    }

    public static boolean isGmailConfigured() {
        String u = getGmailUser();
        String p = getGmailAppPassword();
        return u != null && !u.isBlank() && p != null && !p.isBlank() && !p.contains("your_16_digit");
    }

    public static String getAllowedEmailDomain() {
        return getProperty("allowed.email.domain", getProperty("ALLOWED_EMAIL_DOMAIN", "gmail.com"));
    }

    public static String getJwtSecret() {
        return getProperty("jwt.secret", getProperty("JWT_SECRET", "campus_lost_found_dev_secret_key_2026"));
    }

    public static String getAppUrl() {
        return getProperty("app.url", getProperty("APP_URL", "http://localhost:8080/LostFoundManagementSystem"));
    }
}
