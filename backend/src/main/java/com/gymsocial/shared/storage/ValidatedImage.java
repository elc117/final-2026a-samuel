package com.gymsocial.shared.storage;

public record ValidatedImage(
    String contentType,
    String extension,
    byte[] content
) {
}
