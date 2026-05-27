package io.github.jxch.platform.oracle.cloud.object.storage.s3.util;

import org.springframework.util.StringUtils;

public class OciS3EndpointBuilder {

    public static String resolveEndpoint(String endpoint, String namespace, String region) {
        if (StringUtils.hasText(endpoint)) {
            return endpoint;
        }
        if (!StringUtils.hasText(namespace)) {
            throw new IllegalArgumentException("oci.s3.namespace must not be blank when oci.s3.endpoint is not configured");
        }
        if (!StringUtils.hasText(region)) {
            throw new IllegalArgumentException("oci.s3.region must not be blank when oci.s3.endpoint is not configured");
        }
        return "https://" + namespace + ".compat.objectstorage." + region + ".oraclecloud.com";
    }

}
