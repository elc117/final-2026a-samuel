package com.gymsocial.auth;

import com.password4j.Password;

public final class PasswordHasher {

    public String hash(String password) {
        return Password.hash(password).withArgon2().getResult();
    }

    public boolean matches(String password, String passwordHash) {
        return Password.check(password, passwordHash).withArgon2();
    }
}
