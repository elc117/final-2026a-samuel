package com.gymsocial.auth;

import com.password4j.Argon2Function;
import com.password4j.Password;
import com.password4j.types.Argon2;

public final class PasswordHasher {

    private static final int MEMORY_KIB = 19_456;
    private static final int ITERATIONS = 2;
    private static final int PARALLELISM = 1;
    private static final int OUTPUT_LENGTH = 32;
    private static final int VERSION = Argon2Function.ARGON2_VERSION_13;

    private static final Argon2Function HASHING_FUNCTION =
        Argon2Function.getInstance(
            MEMORY_KIB,
            ITERATIONS,
            PARALLELISM,
            OUTPUT_LENGTH,
            Argon2.ID,
            VERSION
        );

    public String hash(String password) {
        return Password.hash(password).with(HASHING_FUNCTION).getResult();
    }

    public boolean matches(String password, String passwordHash) {
        Argon2Function hashingFunction =
            Argon2Function.getInstanceFromHash(passwordHash);

        return Password.check(password, passwordHash).with(hashingFunction);
    }
}
