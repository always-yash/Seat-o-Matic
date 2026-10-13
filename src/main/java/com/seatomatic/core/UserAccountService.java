package com.seatomatic.core;

import com.seatomatic.analysis.AuditService;
import java.util.List;
import java.util.Locale;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountService {
    private final UserAccountRepository accounts;
    private final PasswordEncoder passwords;
    private final AuditService audit;
    public UserAccountService(UserAccountRepository accounts, PasswordEncoder passwords, AuditService audit) {
        this.accounts = accounts; this.passwords = passwords; this.audit = audit;
    }
    @Transactional(readOnly = true) public List<AccountView> list() {
        return accounts.findAllByOrderByUsernameAsc().stream().map(UserAccountService::view).toList();
    }
    @Transactional public AccountView create(CreateAccount input) {
        String username = input.username().strip().toLowerCase(Locale.ROOT);
        if (input.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) throw new IllegalArgumentException("Passwords cannot exceed 72 UTF-8 bytes.");
        if (accounts.existsByUsernameIgnoreCase(username)) throw new IllegalArgumentException("That username is already in use.");
        UserAccount account = accounts.save(new UserAccount(username, passwords.encode(input.password()), input.role()));
        audit.record("USER_ACCOUNT_CREATED", "User account", username + " · " + input.role());
        return view(account);
    }
    @Transactional public AccountView setActive(Long id, boolean active) {
        UserAccount account = accounts.findById(id).orElseThrow(() -> new IllegalArgumentException("User account not found."));
        if (account.isActive() == active) return view(account);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!active && authentication != null && authentication.getName().equalsIgnoreCase(account.getUsername())) {
            throw new IllegalArgumentException("You cannot deactivate your own account.");
        }
        if (!active && account.getRole() == UserAccount.Role.ADMIN && accounts.countByRoleAndActiveTrue(UserAccount.Role.ADMIN) <= 1) {
            throw new IllegalArgumentException("At least one active Admin account must remain.");
        }
        account.setActive(active);
        audit.record(active ? "USER_ACCOUNT_ACTIVATED" : "USER_ACCOUNT_DEACTIVATED", "User account", account.getUsername());
        return view(account);
    }
    private static AccountView view(UserAccount account) {
        return new AccountView(account.getId(), account.getUsername(), account.getRole(), account.isActive(), account.getCreatedAt());
    }
    public record CreateAccount(String username, String password, UserAccount.Role role) { }
    public record AccountView(Long id, String username, UserAccount.Role role, boolean active, java.time.LocalDateTime createdAt) { }
}
