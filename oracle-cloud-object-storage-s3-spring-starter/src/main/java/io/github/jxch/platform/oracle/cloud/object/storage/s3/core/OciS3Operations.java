package io.github.jxch.platform.oracle.cloud.object.storage.s3.core;

import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.InputStream;
import java.util.List;

public interface OciS3Operations {
    void putObject(String bucket, String key, byte[] content, String contentType);

    void putObject(String key, byte[] content, String contentType);

    void putObject(String bucket, String key, InputStream inputStream, long contentLength, String contentType);

    byte[] getObject(String bucket, String key);

    byte[] getObject(String key);

    void deleteObject(String bucket, String key);

    void deleteObject(String key);

    List<S3Object> listObjects(String bucket, String prefix);

    List<S3Object> listObjects(String prefix);
}
