package com.seatomatic.core;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCase(String username);
    long countByRoleAndActiveTrue(UserAccount.Role role);
    List<UserAccount> findAllByOrderByUsernameAsc();
    List<UserAccount> findByRoleAndActiveTrueOrderByUsernameAsc(UserAccount.Role role);
}
