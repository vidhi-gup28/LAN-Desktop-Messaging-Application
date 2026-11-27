package com.lanmessenger.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;

/**
 * High-performance JSON utility providing newline-delimited serialization and deserialization.
 */
public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        com.fasterxml.jackson.core.StreamReadConstraints constraints =
                com.fasterxml.jackson.core.StreamReadConstraints.builder()
                        .maxStringLength(100_000_000)
                        .build();
        MAPPER.getFactory().setStreamReadConstraints(constraints);
    }

    private JsonUtils() {
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    /**
     * Serializes an object to a single-line JSON string followed by a newline delimiter.
     */
    public static String toDelimitedJson(Object object) throws JsonProcessingException {
        return MAPPER.writeValueAsString(object) + "\n";
    }

    /**
     * Serializes an object to a standard JSON string without trailing newline.
     */
    public static String toJson(Object object) throws JsonProcessingException {
        return MAPPER.writeValueAsString(object);
    }

    /**
     * Deserializes a JSON string into the target class.
     */
    public static <T> T fromJson(String json, Class<T> clazz) throws IOException {
        if (json == null) {
            return null;
        }
        String cleaned = json.trim();
        if (cleaned.startsWith("\uFEFF")) {
            cleaned = cleaned.substring(1).trim();
        }
        if (cleaned.isEmpty()) {
            return null;
        }
        return MAPPER.readValue(cleaned, clazz);
    }
}
