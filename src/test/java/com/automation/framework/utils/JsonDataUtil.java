package com.automation.framework.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public final class JsonDataUtil {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonDataUtil() {}

    public static List<Map<String, String>> readJsonData(String filePath) throws IOException {
        return OBJECT_MAPPER.readValue(new File(filePath), new TypeReference<>() {});
    }
}
