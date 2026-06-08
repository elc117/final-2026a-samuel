package com.gymsocial.shared.storage;

import com.gymsocial.config.ApplicationConfig;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;

import java.io.ByteArrayInputStream;
import java.util.concurrent.TimeUnit;

public final class MinioImageStorage implements ImageStorage {

    private static final int READ_URL_EXPIRY_HOURS = 1;

    private final MinioClient storageClient;
    private final MinioClient publicClient;
    private final String bucket;

    public MinioImageStorage(ApplicationConfig config) {
        storageClient = client(
            config.s3Endpoint(),
            config.s3AccessKey(),
            config.s3SecretKey(),
            config.s3Region()
        );
        publicClient = client(
            config.s3PublicEndpoint(),
            config.s3AccessKey(),
            config.s3SecretKey(),
            config.s3Region()
        );
        bucket = config.s3Bucket();
    }

    @Override
    public void upload(String objectKey, ValidatedImage image) {
        try (var stream = new ByteArrayInputStream(image.content())) {
            storageClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .contentType(image.contentType())
                    .stream(stream, (long) image.content().length, -1)
                    .build()
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Could not upload image", exception);
        }
    }

    @Override
    public String createReadUrl(String objectKey) {
        try {
            return publicClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(objectKey)
                    .expiry(READ_URL_EXPIRY_HOURS, TimeUnit.HOURS)
                    .build()
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                "Could not create image URL",
                exception
            );
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            storageClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build()
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Could not delete image", exception);
        }
    }

    private MinioClient client(
        String endpoint,
        String accessKey,
        String secretKey,
        String region
    ) {
        return MinioClient.builder()
            .endpoint(endpoint)
            .region(region)
            .credentials(accessKey, secretKey)
            .build();
    }
}
