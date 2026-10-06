package com.automation.framework.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public final class JsonDataUtil {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String DEFAULT_DATA_FILE = "/data/user-data.json";

    private JsonDataUtil() {}

    public static List<Map<String, String>> readJsonData() throws IOException {
        try (InputStream inputStream = JsonDataUtil.class.getResourceAsStream(DEFAULT_DATA_FILE)) {
            if (inputStream == null) {
                throw new IOException("JSON test data resource not found: " + DEFAULT_DATA_FILE);
            }
            return OBJECT_MAPPER.readValue(inputStream, new TypeReference<>() {});
        }
    }

    public static List<Map<String, String>> readJsonData(String filePath) throws IOException {
        return OBJECT_MAPPER.readValue(new File(filePath), new TypeReference<>() {});
    }
}