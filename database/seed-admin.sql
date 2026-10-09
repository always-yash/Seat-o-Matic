-- Generate a fresh value with: java -cp target/classes com.seatomatic.tools.HashPassword <password>
INSERT INTO users (username, password_hash, salt, role, failed_attempts, active)
VALUES ('admin', 'DONT_PUT_A_REAL_PASSWORD_HASH_IN_SOURCE', 'managed-inside-password-hash', 'ADMIN', 0, TRUE)
ON DUPLICATE KEY UPDATE username = VALUES(username);