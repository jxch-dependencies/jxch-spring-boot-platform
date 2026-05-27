package io.github.jxch.platform.oracle.cloud.object.storage.s3.autoconfigure;

import io.github.jxch.platform.oracle.cloud.object.storage.s3.config.OciS3Properties;
import io.github.jxch.platform.oracle.cloud.object.storage.s3.core.OciS3Operations;
import io.github.jxch.platform.oracle.cloud.object.storage.s3.core.OciS3Template;
import io.github.jxch.platform.oracle.cloud.object.storage.s3.health.OciS3HealthIndicator;
import io.github.jxch.platform.oracle.cloud.object.storage.s3.util.OciS3EndpointBuilder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.health.autoconfigure.contributor.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@AutoConfiguration
@ConditionalOnClass(S3Client.class)
@EnableConfigurationProperties(OciS3Properties.class)
@ConditionalOnProperty(prefix = "spring.oracle.cloud.oci.s3", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OciS3AutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public S3Client s3Client(OciS3Properties properties) {
        validate(properties);

        String endpoint = OciS3EndpointBuilder.resolveEndpoint(
                properties.getEndpoint(),
                properties.getNamespace(),
                properties.getRegion()
        );

        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                properties.getAccessKey(),
                properties.getSecretKey()
        );

        S3Configuration s3Configuration = S3Configuration.builder()
                .pathStyleAccessEnabled(properties.isPathStyleAccess())
                .chunkedEncodingEnabled(properties.isChunkedEncodingEnabled())
                .checksumValidationEnabled(properties.isChecksumValidationEnabled())
                .build();

        return S3Client.builder()
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(properties.getRegion()))
                .endpointOverride(URI.create(endpoint))
                .serviceConfiguration(s3Configuration)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public OciS3Operations ociS3Operations(S3Client s3Client, OciS3Properties properties) {
        return new OciS3Template(s3Client, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public OciS3Template ociS3Template(S3Client s3Client, OciS3Properties properties) {
        return new OciS3Template(s3Client, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public S3Presigner s3Presigner(OciS3Properties properties) {
        String endpoint = OciS3EndpointBuilder.resolveEndpoint(
                properties.getEndpoint(),
                properties.getNamespace(),
                properties.getRegion()
        );

        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                properties.getAccessKey(),
                properties.getSecretKey()
        );

        return S3Presigner.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(properties.getRegion()))
                .endpointOverride(URI.create(endpoint))
                .build();
    }

    private void validate(OciS3Properties properties) {
        if (!StringUtils.hasText(properties.getRegion())) {
            throw new IllegalArgumentException("oci.s3.region must not be blank");
        }
        if (!StringUtils.hasText(properties.getAccessKey())) {
            throw new IllegalArgumentException("oci.s3.access-key must not be blank");
        }
        if (!StringUtils.hasText(properties.getSecretKey())) {
            throw new IllegalArgumentException("oci.s3.secret-key must not be blank");
        }
    }

    @Bean(name = "ociS3HealthIndicator")
    @ConditionalOnMissingBean(name = "ociS3HealthIndicator")
    @ConditionalOnClass(HealthIndicator.class)
    @ConditionalOnEnabledHealthIndicator("ociS3")
    @ConditionalOnProperty(prefix = "spring.oracle.cloud.oci.s3", name = "health-enabled", havingValue = "true", matchIfMissing = true)
    public HealthIndicator ociS3HealthIndicator(S3Client s3Client, OciS3Properties properties) {
        return new OciS3HealthIndicator(s3Client, properties);
    }

}
