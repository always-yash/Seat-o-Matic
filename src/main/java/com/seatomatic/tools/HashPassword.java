package com.seatomatic.tools;

import com.seatomatic.common.security.PasswordHasher;

public final class HashPassword {

    private HashPassword() {
    }

    public static void main(String[] args) {
        String password = args.length > 0 ? args[0] : "admin123";
        System.out.println(PasswordHasher.hash(password));
    }
}
