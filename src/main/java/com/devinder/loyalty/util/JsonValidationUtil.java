package com.devinder.loyalty.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.devinder.loyalty.exception.BadRequestException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class JsonValidationUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        try {
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void validateJsonFormat(String json) {
        if (json == null || json.trim().isEmpty()) {
            throw new BadRequestException("JSON content is empty or null");
        }
        try {
            OBJECT_MAPPER.readTree(json);
        } catch (Exception e) {
            log.warn("Invalid JSON content: {}", json, e);
            throw new BadRequestException("Invalid JSON format: " + e.getMessage());
        }
    }
}
