package com.gymsocial.config;

public record ApplicationConfig(
    int port,
    String databaseUrl,
    String databaseUser,
    String databasePassword,
    String jwtSecret,
    String hashidsSalt,
    String corsAllowedOrigin,
    boolean cookieSecure,
    String cookieSameSite,
    String s3Endpoint,
    String s3PublicEndpoint,
    String s3AccessKey,
    String s3SecretKey,
    String s3Bucket,
    String s3Region
) {

    private static final int DEFAULT_PORT = 7000;
    private static final int MINIMUM_JWT_SECRET_LENGTH = 32;
    private static final int MINIMUM_HASHIDS_SALT_LENGTH = 16;

    public static ApplicationConfig fromEnvironment() {
        String jwtSecret = required("JWT_SECRET");
        String hashidsSalt = required("HASHIDS_SALT");

        if (jwtSecret.length() < MINIMUM_JWT_SECRET_LENGTH) {
            throw new IllegalStateException(
                "JWT_SECRET must contain at least 32 characters"
            );
        }
        if (hashidsSalt.length() < MINIMUM_HASHIDS_SALT_LENGTH) {
            throw new IllegalStateException(
                "HASHIDS_SALT must contain at least 16 characters"
            );
        }

        return new ApplicationConfig(
            Integer.parseInt(resolvePort()),
            required("DATABASE_URL"),
            required("DATABASE_USER"),
            required("DATABASE_PASSWORD"),
            jwtSecret,
            hashidsSalt,
            optional(
                "CORS_ALLOWED_ORIGIN",
                "http://localhost:5173,http://127.0.0.1:5173"
            ),
            Boolean.parseBoolean(optional("COOKIE_SECURE", "false")),
            optional("COOKIE_SAME_SITE", "Lax"),
            required("S3_ENDPOINT"),
            required("S3_PUBLIC_ENDPOINT"),
            required("S3_ACCESS_KEY"),
            required("S3_SECRET_KEY"),
            required("S3_BUCKET"),
            optional("S3_REGION", "us-east-1")
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

    private static String resolvePort() {
        return optional(
            "PORT",
            optional("APP_PORT", String.valueOf(DEFAULT_PORT))
        );
    }
}
