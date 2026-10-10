package com.seatomatic.core.auth;

import com.seatomatic.common.security.Role;

public class User {
    private Long id;
    private String username;
    private String fullName;
    private String passwordHash;
    private String salt;
    private Role role;
    private boolean active;

    public User() {
    }

    public User(Long id, String username, String fullName, String passwordHash, Role role, boolean active) {
        this(id, username, fullName, passwordHash, null, role, active);
    }

    public User(Long id, String username, String fullName, String passwordHash, String salt,
                Role role, boolean active) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
