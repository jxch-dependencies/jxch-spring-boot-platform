package io.github.jxch.platform.oracle.cloud.object.storage.s3.ex;

public class OciS3Exception extends RuntimeException {

    public OciS3Exception(String message) {
        super(message);
    }

    public OciS3Exception(String message, Throwable cause) {
        super(message, cause);
    }
}
