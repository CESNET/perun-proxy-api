package cz.muni.ics.perunproxyapi.presentation.rest.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import cz.muni.ics.perunproxyapi.persistence.exceptions.InvalidRequestParameterException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ControllerUtils {

    public static JsonNode getRequiredJson(JsonNode parent, String fieldName)
            throws InvalidRequestParameterException
    {
        if (parent == null || parent.isNull()) {
            log.warn("Passed JSON is NULL. Actual JSON is\n{}", parent);
            throw new InvalidRequestParameterException("Passed JSON is NULL");
        } else if (!parent.hasNonNull(fieldName)) {
            log.warn("Passed JSON does not contain NON-NULL field {}. Actual JSON is\n{}", fieldName, parent);
            throw new InvalidRequestParameterException("Passed JSON does not contain required field " + fieldName);
        }
        return parent.get(fieldName);
    }

    public static JsonNode getJson(JsonNode parent, String fieldName) throws InvalidRequestParameterException {
        if (parent == null || parent.isNull()) {
            log.warn("Passed JSON is NULL. Actual JSON is\n{}", parent);
            throw new InvalidRequestParameterException("Passed JSON is NULL");
        } else if (!parent.has(fieldName)) {
            log.warn("Passed JSON does not contain field {}. Actual JSON is\n{}", fieldName, parent);
            throw new InvalidRequestParameterException("Passed JSON does not contain required field " + fieldName);
        }
        return parent.get(fieldName);
    }

    public static String extractString(JsonNode container) throws InvalidRequestParameterException {
        if (container == null || container.isNull()) {
            log.warn("Passed JSON is NULL. Actual JSON is\n{}", container);
            throw new InvalidRequestParameterException("Passed JSON is NULL");
        }
        if (!container.isTextual()) {
            log.warn("Passed JSON is not TEXTUAL. Actual JSON is\n{}", container);
            throw new InvalidRequestParameterException("Passed JSON is not TEXTUAL");
        }
        return container.textValue();
    }

    public static String extractRequiredString(JsonNode parent, String fieldName)
            throws InvalidRequestParameterException
    {
        JsonNode container = ControllerUtils.getRequiredJson(parent, fieldName);
        return ControllerUtils.extractString(container);
    }

    public static List<String> extractListOfStrings(JsonNode container)
            throws InvalidRequestParameterException
    {
        if (container == null || container.isNull()) {
            log.warn("Passed JSON is NULL. Actual JSON is\n{}", container);
            throw new InvalidRequestParameterException("Passed JSON is NULL");
        } else if (!container.isArray()) {
            log.warn("Passed JSON is not an ARRAY. Actual JSON is\n{}", container);
            throw new InvalidRequestParameterException("Passed JSON is not an ARRAY");
        }
        List<String> values = new ArrayList<>();
        for (JsonNode subNode: container) {
            values.add(subNode.textValue());
        }
        return values;
    }

    public static List<String> extractRequiredListOfStrings(JsonNode parent, String fieldName)
            throws InvalidRequestParameterException
    {
        JsonNode container = ControllerUtils.getRequiredJson(parent, fieldName);
        return ControllerUtils.extractListOfStrings(container);
    }

    public static void validateRequestBody(JsonNode body) throws InvalidRequestParameterException {
        if (body == null || body.isNull()) {
            throw new InvalidRequestParameterException("Request body cannot be null");
        }
    }

    public static String decodeUrlSafeBase64(String encoded) {
        if (encoded == null) {
            return null;
        } else if (!StringUtils.hasText(encoded)) {
            return "";
        }
        return new String(Base64Utils.decodeFromUrlSafeString(encoded));
    }

    public static List<String> extractFieldsFromBody(JsonNode body, String paramName) {
        if (body == null || body.isNull()) {
            return new ArrayList<>();
        }

        JsonNode paramJson = body.get(paramName);
        if (paramJson == null || paramJson.isNull()) {
            return new ArrayList<>();
        } else if (!paramJson.isArray()) {
            log.warn("ParamJson is not array. Actually, it is\n{}", paramJson);
            throw new IllegalArgumentException("Expected JSON array, something else found instead");
        }

        List<String> values = new ArrayList<>();
        for (JsonNode subNode: paramJson) {
            values.add(subNode.textValue());
        }
        return values;
    }

    public static Long extractLong(JsonNode container) throws InvalidRequestParameterException {
        if (container == null || container.isNull()) {
            log.warn("Passed JSON is NULL. Actual JSON is\n{}", container);
            throw new InvalidRequestParameterException("Passed JSON is NULL");
        }
        if (!container.isNumber()) {
            log.warn("Passed JSON is not NUMBER. Actual JSON is\n{}", container);
            throw new InvalidRequestParameterException("Passed JSON is not NUMBER");
        }
        return container.longValue();
    }

    public static Long extractRequiredLong(JsonNode parent, String fieldName) throws InvalidRequestParameterException {
        JsonNode container = ControllerUtils.getRequiredJson(parent, fieldName);
        return ControllerUtils.extractLong(container);
    }

}
