package com.johanwork.warehouse.common.config.configProps;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oci.config")
public record OciStorageProperties(
        AuthType authType,
       String namespace,
       String bucket,
       boolean bucketPublic,
       String region,
       String path,
       String profile
) {
}
