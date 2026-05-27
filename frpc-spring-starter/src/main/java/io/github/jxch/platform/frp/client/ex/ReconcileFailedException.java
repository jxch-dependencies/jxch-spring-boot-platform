package io.github.jxch.platform.frp.client.ex;

public class ReconcileFailedException extends FrpClientException {
    public ReconcileFailedException(String message) {
        super(message);
    }

    public ReconcileFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
