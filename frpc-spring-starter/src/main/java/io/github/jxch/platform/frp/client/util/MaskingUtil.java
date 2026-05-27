package io.github.jxch.platform.frp.client.util;

public class MaskingUtil {

    public static String mask(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        if (value.length() <= 4) {
            return "****";
        }
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }

}
