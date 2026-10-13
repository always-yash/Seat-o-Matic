package com.seatomatic.core;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountDetailsService implements UserDetailsService {
    private final UserAccountRepository accounts;
    public UserAccountDetailsService(UserAccountRepository accounts) { this.accounts = accounts; }
    @Override @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount account = accounts.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password."));
        return User.withUsername(account.getUsername()).password(account.getPasswordHash())
                .roles(account.getRole().name()).disabled(!account.isActive()).build();
    }
}
