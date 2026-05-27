package io.github.jxch.platform.frp.client.core.config;

import io.github.jxch.platform.frp.client.config.FrpClientProperties;
import io.github.jxch.platform.frp.client.core.model.ProxyMapping;

import java.util.List;

public class TomlFrpcConfigRenderer implements FrpcConfigRenderer {

    @Override
    public String render(FrpClientProperties properties, List<ProxyMapping> mappings) {
        StringBuilder sb = new StringBuilder();

        sb.append("serverAddr = ").append(q(properties.getServerAddr())).append("\n");
        sb.append("serverPort = ").append(properties.getServerPort()).append("\n\n");

        sb.append("[auth]\n");
        sb.append("method = ").append(q(properties.getAuth().getMethod())).append("\n");
        if (properties.getAuth().getToken() != null && !properties.getAuth().getToken().isBlank()) {
            sb.append("token = ").append(q(properties.getAuth().getToken())).append("\n");
        }
        sb.append("\n");

        if (properties.getWebServer().isEnabled()) {
            sb.append("webServer.addr = ").append(q(properties.getWebServer().getAddr())).append("\n");
            sb.append("webServer.port = ").append(properties.getWebServer().getPort()).append("\n");
            if (properties.getWebServer().getUsername() != null && !properties.getWebServer().getUsername().isBlank()) {
                sb.append("webServer.user = ").append(q(properties.getWebServer().getUsername())).append("\n");
            }
            if (properties.getWebServer().getPassword() != null && !properties.getWebServer().getPassword().isBlank()) {
                sb.append("webServer.password = ").append(q(properties.getWebServer().getPassword())).append("\n");
            }
            sb.append("\n");
        }

        for (ProxyMapping mapping : mappings) {
            sb.append("[[proxies]]\n");
            sb.append("name = ").append(q(mapping.name())).append("\n");
            sb.append("type = ").append(q(mapping.type())).append("\n");
            sb.append("localIP = ").append(q(mapping.localIp())).append("\n");
            sb.append("localPort = ").append(mapping.localPort()).append("\n");
            sb.append("remotePort = ").append(mapping.remotePort()).append("\n");
            sb.append("enabled = ").append(mapping.enabled()).append("\n\n");
        }

        return sb.toString();
    }

    private String q(String value) {
        return "\"" + value.replace("\"", "\\\"") + "\"";
    }
}
