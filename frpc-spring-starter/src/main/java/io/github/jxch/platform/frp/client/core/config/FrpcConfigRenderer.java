package io.github.jxch.platform.frp.client.core.config;

import io.github.jxch.platform.frp.client.config.FrpClientProperties;
import io.github.jxch.platform.frp.client.core.model.ProxyMapping;

import java.util.List;

public interface FrpcConfigRenderer {
    String render(FrpClientProperties properties, List<ProxyMapping> mappings);
}
