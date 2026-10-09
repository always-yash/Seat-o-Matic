INSERT INTO users (username, password_hash, salt, role, failed_attempts, active)
VALUES ('admin', '$2a$12$7b3p8Vm1A1Nf6U5weNtG5eZ5b5lVud2I8aD2hyjI8f0tS0h9mmf6i', 'fixed-salt-demo', 'ADMIN', 0, TRUE)
ON DUPLICATE KEY UPDATE username = VALUES(username);
