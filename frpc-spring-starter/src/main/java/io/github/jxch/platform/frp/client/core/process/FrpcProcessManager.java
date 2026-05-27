package io.github.jxch.platform.frp.client.core.process;

import io.github.jxch.platform.frp.client.core.model.FrpcBinaryInfo;
import io.github.jxch.platform.frp.client.core.model.FrpcProcessStatus;

import java.nio.file.Path;

public interface FrpcProcessManager {
    FrpcBinaryInfo inspectBinary();
    FrpcProcessStatus status();
    CommandResult verify(Path configFile);
    void start(Path configFile);
    void stop();
    CommandResult reload(Path configFile);
}
