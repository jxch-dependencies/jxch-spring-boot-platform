package io.github.jxch.platform.frp.client.core.service;

import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;

@Component
public class LocalPortChecker {

    public boolean reachable(String host, int port, Duration timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), (int) timeout.toMillis());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
