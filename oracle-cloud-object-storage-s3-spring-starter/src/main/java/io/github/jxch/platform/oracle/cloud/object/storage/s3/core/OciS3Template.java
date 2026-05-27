package io.github.jxch.platform.oracle.cloud.object.storage.s3.core;

import io.github.jxch.platform.oracle.cloud.object.storage.s3.config.OciS3Properties;
import io.github.jxch.platform.oracle.cloud.object.storage.s3.ex.OciS3Exception;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.util.List;

public class OciS3Template implements OciS3Operations {

    private final S3Client s3Client;
    private final OciS3Properties properties;

    public OciS3Template(S3Client s3Client, OciS3Properties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    @Override
    public void putObject(String bucket, String key, byte[] content, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(content));
        } catch (S3Exception e) {
            throw new OciS3Exception("Failed to put object to bucket=" + bucket + ", key=" + key, e);
        }
    }

    @Override
    public void putObject(String key, byte[] content, String contentType) {
        putObject(requireDefaultBucket(), key, content, contentType);
    }

    @Override
    public void putObject(String bucket, String key, InputStream inputStream, long contentLength, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));
        } catch (S3Exception e) {
            throw new OciS3Exception("Failed to put object stream to bucket=" + bucket + ", key=" + key, e);
        }
    }

    @Override
    public byte[] getObject(String bucket, String key) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            ResponseBytes<?> responseBytes = s3Client.getObjectAsBytes(request);
            return responseBytes.asByteArray();
        } catch (S3Exception e) {
            throw new OciS3Exception("Failed to get object from bucket=" + bucket + ", key=" + key, e);
        }
    }

    @Override
    public byte[] getObject(String key) {
        return getObject(requireDefaultBucket(), key);
    }

    @Override
    public void deleteObject(String bucket, String key) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(request);
        } catch (S3Exception e) {
            throw new OciS3Exception("Failed to delete object from bucket=" + bucket + ", key=" + key, e);
        }
    }

    @Override
    public void deleteObject(String key) {
        deleteObject(requireDefaultBucket(), key);
    }

    @Override
    public List<S3Object> listObjects(String bucket, String prefix) {
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(prefix)
                    .build();

            return s3Client.listObjectsV2(request).contents();
        } catch (S3Exception e) {
            throw new OciS3Exception("Failed to list objects from bucket=" + bucket + ", prefix=" + prefix, e);
        }
    }

    @Override
    public List<S3Object> listObjects(String prefix) {
        return listObjects(requireDefaultBucket(), prefix);
    }

    private String requireDefaultBucket() {
        String defaultBucket = properties.getDefaultBucket();
        if (defaultBucket == null || defaultBucket.isBlank()) {
            throw new OciS3Exception("oci.s3.default-bucket is not configured");
        }
        return defaultBucket;
    }
}

