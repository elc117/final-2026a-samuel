package com.gymsocial.config;

public record ApplicationConfig(
    int port,
    String databaseUrl,
    String databaseUser,
    String databasePassword,
    String jwtSecret,
    String corsAllowedOrigin,
    boolean cookieSecure
) {

    private static final int DEFAULT_PORT = 7000;
    private static final int MINIMUM_JWT_SECRET_LENGTH = 32;

    public static ApplicationConfig fromEnvironment() {
        String jwtSecret = required("JWT_SECRET");

        if (jwtSecret.length() < MINIMUM_JWT_SECRET_LENGTH) {
            throw new IllegalStateException(
                "JWT_SECRET must contain at least 32 characters"
            );
        }

        return new ApplicationConfig(
            Integer.parseInt(optional("APP_PORT", String.valueOf(DEFAULT_PORT))),
            required("DATABASE_URL"),
            required("DATABASE_USER"),
            required("DATABASE_PASSWORD"),
            jwtSecret,
            optional("CORS_ALLOWED_ORIGIN", "http://localhost:5173"),
            Boolean.parseBoolean(optional("COOKIE_SECURE", "false"))
        );
    }

    private static String required(String name) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " environment variable is required");
        }

        return value;
    }

    private static String optional(String name, String defaultValue) {
        return System.getenv().getOrDefault(name, defaultValue);
    }
}
