package com.gymsocial.shared.id;

import org.hashids.Hashids;

import java.util.OptionalLong;

public class PublicIdCodec {

    private static final int MINIMUM_CODE_LENGTH = 10;

    private final Hashids hashids;

    public PublicIdCodec(String salt) {
        this.hashids = new Hashids(salt, MINIMUM_CODE_LENGTH);
    }

    public String encode(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID must be positive");
        }

        return hashids.encode(id);
    }

    public OptionalLong decode(String code) {
        if (code == null || code.isBlank()) {
            return OptionalLong.empty();
        }

        long[] decoded = hashids.decode(code);
        if (decoded.length != 1 || !encode(decoded[0]).equals(code)) {
            return OptionalLong.empty();
        }

        return OptionalLong.of(decoded[0]);
    }
}
