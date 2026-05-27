package io.github.jxch.platform.oracle.cloud.object.storage.s3.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.oracle.cloud.oci.s3")
public class OciS3Properties {
    /**
     * Enable OCI S3 starter.
     */
    private boolean enabled = true;

    /**
     * OCI Object Storage namespace.
     */
    private String namespace;

    /**
     * OCI region id, e.g. us-ashburn-1.
     */
    private String region;

    /**
     * Full S3 compatibility endpoint.
     * Example:
     * https://<namespace>.compat.objectstorage.<region>.oraclecloud.com
     */
    private String endpoint;

    /**
     * Customer Secret Key access key.
     */
    private String accessKey;

    /**
     * Customer Secret Key secret key.
     */
    private String secretKey;

    /**
     * Default bucket.
     */
    private String defaultBucket;

    /**
     * Use path-style access.
     */
    private boolean pathStyleAccess = true;

    /**
     * Disable chunked encoding for OCI S3 compatibility.
     */
    private boolean chunkedEncodingEnabled = false;

    /**
     * Disable checksum validation if needed.
     */
    private boolean checksumValidationEnabled = false;

    /**
     * Enable health indicator.
     */
    private boolean healthEnabled = true;
}
