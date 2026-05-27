package io.github.jxch.platform.oracle.cloud.object.storage.s3.health;

import io.github.jxch.platform.oracle.cloud.object.storage.s3.config.OciS3Properties;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

public class OciS3HealthIndicator implements HealthIndicator {

    private final S3Client s3Client;
    private final OciS3Properties properties;

    public OciS3HealthIndicator(S3Client s3Client, OciS3Properties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    @Override
    public Health health() {
        String bucket = properties.getDefaultBucket();

        if (!StringUtils.hasText(bucket)) {
            return Health.unknown()
                    .withDetail("type", "oci-s3")
                    .withDetail("status", "default-bucket not configured")
                    .withDetail("region", properties.getRegion())
                    .withDetail("endpoint", properties.getEndpoint())
                    .build();
        }

        try {
            HeadBucketRequest request = HeadBucketRequest.builder()
                    .bucket(bucket)
                    .build();

            s3Client.headBucket(request);

            return Health.up()
                    .withDetail("type", "oci-s3")
                    .withDetail("bucket", bucket)
                    .withDetail("region", properties.getRegion())
                    .withDetail("endpoint", resolvedEndpoint())
                    .build();
        } catch (AwsServiceException e) {
            return Health.down(e)
                    .withDetail("type", "oci-s3")
                    .withDetail("bucket", bucket)
                    .withDetail("region", properties.getRegion())
                    .withDetail("endpoint", resolvedEndpoint())
                    .withDetail("statusCode", e.statusCode())
                    .withDetail("errorCode", e.awsErrorDetails() != null ? e.awsErrorDetails().errorCode() : null)
                    .withDetail("errorMessage", e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage())
                    .build();
        } catch (SdkClientException e) {
            return Health.down(e)
                    .withDetail("type", "oci-s3")
                    .withDetail("bucket", bucket)
                    .withDetail("region", properties.getRegion())
                    .withDetail("endpoint", resolvedEndpoint())
                    .withDetail("errorMessage", e.getMessage())
                    .build();
        } catch (Exception e) {
            return Health.down(e)
                    .withDetail("type", "oci-s3")
                    .withDetail("bucket", bucket)
                    .withDetail("region", properties.getRegion())
                    .withDetail("endpoint", resolvedEndpoint())
                    .withDetail("errorMessage", e.getMessage())
                    .build();
        }
    }

    private String resolvedEndpoint() {
        if (StringUtils.hasText(properties.getEndpoint())) {
            return properties.getEndpoint();
        }
        if (StringUtils.hasText(properties.getNamespace()) && StringUtils.hasText(properties.getRegion())) {
            return "https://" + properties.getNamespace()
                    + ".compat.objectstorage."
                    + properties.getRegion()
                    + ".oraclecloud.com";
        }
        return null;
    }

}
