package com.gymsocial.auth;

import com.gymsocial.auth.dto.LoginRequest;
import com.gymsocial.auth.dto.RegisterRequest;
import com.gymsocial.shared.exception.ValidationException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class AuthValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._]+$"
    );

    private AuthValidator() {
    }

    public static void validate(RegisterRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();

        validateRequiredLength(errors, "name", request.name(), 2, 100);
        validateUsername(errors, request.username());
        validateEmail(errors, request.email());
        validatePassword(errors, request.password());
        throwIfInvalid(errors);
    }

    public static void validate(LoginRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();

        validateEmail(errors, request.email());
        if (isBlank(request.password())) {
            errors.put("password", "Informe sua senha.");
        }

        throwIfInvalid(errors);
    }

    private static void validateUsername(
        Map<String, String> errors,
        String username
    ) {
        if (isBlank(username) || username.trim().length() < 3) {
            errors.put("username", "Use pelo menos 3 caracteres.");
        } else if (username.trim().length() > 30) {
            errors.put("username", "O usuário deve ter no máximo 30 caracteres.");
        } else if (!USERNAME_PATTERN.matcher(username.trim()).matches()) {
            errors.put(
                "username",
                "Use somente letras, números, ponto ou sublinhado."
            );
        }
    }

    private static void validateEmail(Map<String, String> errors, String email) {
        if (isBlank(email)) {
            errors.put("email", "Informe seu e-mail.");
        } else if (
            email.trim().length() > 255 ||
            !EMAIL_PATTERN.matcher(email.trim()).matches()
        ) {
            errors.put("email", "Informe um e-mail válido.");
        }
    }

    private static void validatePassword(
        Map<String, String> errors,
        String password
    ) {
        if (password == null || password.length() < 8) {
            errors.put("password", "Use pelo menos 8 caracteres.");
        } else if (!password.matches(".*[a-z].*")) {
            errors.put("password", "Inclua pelo menos uma letra minúscula.");
        } else if (!password.matches(".*[A-Z].*")) {
            errors.put("password", "Inclua pelo menos uma letra maiúscula.");
        } else if (!password.matches(".*[0-9].*")) {
            errors.put("password", "Inclua pelo menos um número.");
        }
    }

    private static void validateRequiredLength(
        Map<String, String> errors,
        String field,
        String value,
        int minimum,
        int maximum
    ) {
        if (isBlank(value) || value.trim().length() < minimum) {
            errors.put(field, "Informe seu nome.");
        } else if (value.trim().length() > maximum) {
            errors.put(field, "O nome deve ter no máximo 100 caracteres.");
        }
    }

    private static void throwIfInvalid(Map<String, String> errors) {
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
