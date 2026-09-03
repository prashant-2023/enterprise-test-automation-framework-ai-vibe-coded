package com.automation.framework.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class Base64Util {
    private Base64Util() {}

    public static String encode(String plainText) {
        return Base64.getEncoder().encodeToString(plainText.getBytes(StandardCharsets.UTF_8));
    }

    public static String decode(String encodedText) {
        return new String(Base64.getDecoder().decode(encodedText), StandardCharsets.UTF_8);
    }
}
