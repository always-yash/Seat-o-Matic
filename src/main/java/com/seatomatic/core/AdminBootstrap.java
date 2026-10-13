package com.seatomatic.core;

import com.seatomatic.analysis.AuditService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private final UserAccountRepository accounts;
    private final PasswordEncoder passwords;
    private final String username;
    private final String password;
    private final AuditService audit;
    public AdminBootstrap(UserAccountRepository accounts, PasswordEncoder passwords,
                          @Value("${seatomatic.security.username}") String username,
                          @Value("${seatomatic.security.password}") String password, AuditService audit) {
        this.accounts = accounts; this.passwords = passwords; this.username = username; this.password = password; this.audit = audit;
    }
    @Override @Transactional
    public void run(ApplicationArguments args) {
        if (accounts.count() != 0) return;
        if (password.length() < 12) throw new IllegalStateException("The bootstrap password must be at least 12 characters.");
        if (password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) throw new IllegalStateException("The bootstrap password cannot exceed 72 UTF-8 bytes.");
        UserAccount admin = accounts.save(new UserAccount(username, passwords.encode(password), UserAccount.Role.ADMIN));
        audit.record("BOOTSTRAP_ADMIN_CREATED", "User account", admin.getUsername());
    }
}
