package com.gymsocial.shared.storage;

import com.gymsocial.shared.exception.ValidationException;

import java.util.Map;
import java.util.Set;

public final class ImageFileValidator {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "image/jpeg",
        "image/png",
        "image/webp"
    );

    public ValidatedImage validate(ImageUpload image, int maximumBytes) {
        if (image.content() == null || image.content().length == 0) {
            throw invalid("Selecione uma imagem.");
        }

        if (image.content().length > maximumBytes) {
            throw invalid(
                "A imagem deve ter no máximo %s MB."
                    .formatted(megabytes(maximumBytes))
            );
        }

        String detectedContentType = detectContentType(image.content());

        if (
            detectedContentType == null ||
            !ALLOWED_CONTENT_TYPES.contains(image.contentType()) ||
            !detectedContentType.equals(image.contentType())
        ) {
            throw invalid("Use uma imagem JPEG, PNG ou WebP válida.");
        }

        return new ValidatedImage(
            detectedContentType,
            extensionFor(detectedContentType),
            image.content()
        );
    }

    private String detectContentType(byte[] content) {
        if (
            content.length >= 3 &&
            unsigned(content[0]) == 0xFF &&
            unsigned(content[1]) == 0xD8 &&
            unsigned(content[2]) == 0xFF
        ) {
            return "image/jpeg";
        }

        if (
            content.length >= 8 &&
            unsigned(content[0]) == 0x89 &&
            content[1] == 'P' &&
            content[2] == 'N' &&
            content[3] == 'G' &&
            unsigned(content[4]) == 0x0D &&
            unsigned(content[5]) == 0x0A &&
            unsigned(content[6]) == 0x1A &&
            unsigned(content[7]) == 0x0A
        ) {
            return "image/png";
        }

        if (
            content.length >= 12 &&
            content[0] == 'R' &&
            content[1] == 'I' &&
            content[2] == 'F' &&
            content[3] == 'F' &&
            content[8] == 'W' &&
            content[9] == 'E' &&
            content[10] == 'B' &&
            content[11] == 'P'
        ) {
            return "image/webp";
        }

        return null;
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> throw new IllegalArgumentException("Unsupported image type");
        };
    }

    private int unsigned(byte value) {
        return value & 0xFF;
    }

    private String megabytes(int bytes) {
        double megabytes = bytes / 1024.0 / 1024.0;

        return megabytes == Math.floor(megabytes)
            ? String.valueOf((int) megabytes)
            : String.format("%.1f", megabytes);
    }

    private ValidationException invalid(String message) {
        return new ValidationException(Map.of("image", message));
    }
}
