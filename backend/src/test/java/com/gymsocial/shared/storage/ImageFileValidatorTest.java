package com.gymsocial.shared.storage;

import com.gymsocial.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImageFileValidatorTest {

    private final ImageFileValidator validator = new ImageFileValidator();

    @Test
    void acceptsWebpWhenMimeTypeAndSignatureMatch() {
        byte[] webp = {
            'R', 'I', 'F', 'F', 0, 0, 0, 0,
            'W', 'E', 'B', 'P'
        };

        ValidatedImage result = validator.validate(
            new ImageUpload("cover.webp", "image/webp", webp),
            1024
        );

        assertEquals("image/webp", result.contentType());
        assertEquals("webp", result.extension());
    }

    @Test
    void rejectsFileWhenDeclaredMimeTypeDoesNotMatchItsSignature() {
        byte[] jpeg = {
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0
        };

        assertThrows(
            ValidationException.class,
            () -> validator.validate(
                new ImageUpload("fake.png", "image/png", jpeg),
                1024
            )
        );
    }

    @Test
    void rejectsUnsupportedFileSignature() {
        byte[] executable = {'M', 'Z', 0, 0};

        assertThrows(
            ValidationException.class,
            () -> validator.validate(
                new ImageUpload("image.webp", "image/webp", executable),
                1024
            )
        );
    }

    @Test
    void rejectsImageLargerThanConfiguredLimit() {
        byte[] oversizedWebp = new byte[13];
        oversizedWebp[0] = 'R';
        oversizedWebp[1] = 'I';
        oversizedWebp[2] = 'F';
        oversizedWebp[3] = 'F';
        oversizedWebp[8] = 'W';
        oversizedWebp[9] = 'E';
        oversizedWebp[10] = 'B';
        oversizedWebp[11] = 'P';

        assertThrows(
            ValidationException.class,
            () -> validator.validate(
                new ImageUpload("cover.webp", "image/webp", oversizedWebp),
                12
            )
        );
    }
}
