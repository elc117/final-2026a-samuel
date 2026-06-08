package com.gymsocial.shared.storage;

public record ImageUpload(
    String filename,
    String contentType,
    byte[] content
) {
}
