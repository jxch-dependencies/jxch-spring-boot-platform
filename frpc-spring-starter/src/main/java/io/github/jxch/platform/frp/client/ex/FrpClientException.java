package io.github.jxch.platform.frp.client.ex;

public class FrpClientException extends RuntimeException {
    public FrpClientException(String message) {
        super(message);
    }

    public FrpClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
