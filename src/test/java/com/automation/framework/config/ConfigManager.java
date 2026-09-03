package com.automation.framework.config;

import com.automation.framework.utils.Base64Util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigManager {
    private static final Properties properties = new Properties();
    private static final String CONFIG_PATH = "src/test/resources/config/config.properties";

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try (FileInputStream fileInputStream = new FileInputStream(CONFIG_PATH)) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load config.properties", e);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }

    public static String getBrowser() {
        return get("browser");
    }

    public static String getBaseUrl() {
        return get("baseUrl");
    }

    public static String getUsername() {
        return get("username");
    }

    public static String getPassword() {
        return get("password");
    }

    public static String getDecodedPassword() {
        String encodedPassword = getPassword();
        return Base64Util.decode(encodedPassword);
    }

    public static long getImplicitWait() {
        return Long.parseLong(get("implicitWait"));
    }

    public static long getPageLoadTimeout() {
        return Long.parseLong(get("pageLoadTimeout"));
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(get("headless"));
    }
}
