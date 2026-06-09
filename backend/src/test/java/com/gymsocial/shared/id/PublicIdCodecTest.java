package com.gymsocial.shared.id;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PublicIdCodecTest {

    private final PublicIdCodec codec = new PublicIdCodec(
        "test-public-id-salt"
    );

    @Test
    void encodesAndDecodesUserId() {
        String code = codec.encode(42);

        assertTrue(code.length() >= 10);
        assertNotEquals("42", code);
        assertEquals(42, codec.decode(code).orElseThrow());
    }

    @Test
    void rejectsInvalidOrModifiedCode() {
        String code = codec.encode(42);

        assertTrue(codec.decode("invalid-code").isEmpty());
        assertTrue(codec.decode(code + "x").isEmpty());
        assertTrue(codec.decode("").isEmpty());
    }

    @Test
    void saltChangesGeneratedCode() {
        String first = codec.encode(42);
        String second = new PublicIdCodec("another-test-salt").encode(42);

        assertNotEquals(first, second);
    }
}
