package com.gymsocial.shared.validation;

import com.gymsocial.shared.exception.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import java.util.LinkedHashMap;
import java.util.Map;

public final class RequestValidator {

    private final Validator validator;

    public RequestValidator() {
        this.validator = Validation
            .byDefaultProvider()
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory()
            .getValidator();
    }

    public <T> void validate(T request) {
        Map<String, String> errors = new LinkedHashMap<>();

        validator.validate(request).stream()
            .sorted((first, second) ->
                fieldName(first).compareTo(fieldName(second))
            )
            .forEach(violation ->
                errors.putIfAbsent(
                    fieldName(violation),
                    violation.getMessage()
                )
            );

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private String fieldName(ConstraintViolation<?> violation) {
        return violation.getPropertyPath().toString();
    }
}
