package io.github.jxch.platform.frp.client.core.process;

public record CommandResult(
        boolean success,
        int exitCode,
        String stdout,
        String stderr
) {
}
