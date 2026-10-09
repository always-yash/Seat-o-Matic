CREATE USER IF NOT EXISTS 'seatomatic_app'@'localhost' IDENTIFIED BY 'seatomatic_app';
GRANT SELECT, INSERT, UPDATE, DELETE ON seatomatic.* TO 'seatomatic_app'@'localhost';
GRANT SELECT, INSERT ON seatomatic.audit_logs TO 'seatomatic_app'@'localhost';
FLUSH PRIVILEGES;
