package se.ifmo.route_information_system.service;

import io.minio.*;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import se.ifmo.route_information_system.config.MinioProperties;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final MinioClient minioClient;
    private final MinioProperties props;
    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    public String upload(MultipartFile file) {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(props.getBucket()).build());
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(props.getBucket()).build());
            }

            String objectName = UUID.randomUUID() + "-" + file.getOriginalFilename();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(props.getBucket())
                            .object(objectName)
                            .contentType(file.getContentType())
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .build());
            return objectName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to MinIO", e);
        }
    }

    public InputStream download(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(props.getBucket())
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file from MinIO", e);
        }
    }

    public void deleteQuietly(String objectName) {
        if (objectName == null || objectName.isBlank()) {
            return;
        }
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(props.getBucket())
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            log.warn("Failed to delete object {} from MinIO", objectName, e);
        }
    }
}
