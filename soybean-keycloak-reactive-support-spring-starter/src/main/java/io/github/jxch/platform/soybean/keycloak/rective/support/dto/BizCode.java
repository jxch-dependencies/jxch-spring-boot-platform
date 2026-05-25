package io.github.jxch.platform.soybean.keycloak.rective.support.dto;

public final class BizCode {
    public static final String SUCCESS        = "0000";
    public static final String TOKEN_EXPIRED  = "8001"; // -> VITE_SERVICE_EXPIRED_TOKEN_CODES
    public static final String FORCE_LOGOUT   = "8002"; // -> VITE_SERVICE_LOGOUT_CODES
    public static final String MODAL_LOGOUT   = "8003"; // -> VITE_SERVICE_MODAL_LOGOUT_CODES
    public static final String AUTH_FAIL      = "9001";
    public static final String SERVER_ERROR   = "9999";
    private BizCode() {}
}
