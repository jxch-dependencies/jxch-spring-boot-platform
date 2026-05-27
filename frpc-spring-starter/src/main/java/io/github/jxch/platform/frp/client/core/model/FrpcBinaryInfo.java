package io.github.jxch.platform.frp.client.core.model;

public record FrpcBinaryInfo(
        String path,
        boolean exists,
        boolean executable,
        String version
) {
}