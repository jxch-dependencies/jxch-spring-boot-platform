package io.github.jxch.platform.soybean.keycloak.rective.support.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private String code;
    private String msg;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("0000", "success", data);
    }

    public static <T> ApiResponse<T> fail(String code, String msg) {
        return new ApiResponse<>(code, msg, null);
    }
}
