package com.gymsocial.shared.storage;

public interface ImageStorage {

    void upload(String objectKey, ValidatedImage image);

    String createReadUrl(String objectKey);

    void delete(String objectKey);
}
