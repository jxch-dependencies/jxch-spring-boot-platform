package io.github.jxch.platform.frp.client.ex;

public class FrpcCommandException extends FrpClientException {
    public FrpcCommandException(String message) {
        super(message);
    }

    public FrpcCommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
