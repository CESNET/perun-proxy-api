package cz.muni.ics.perunproxyapi.persistence.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import cz.muni.ics.perunproxyapi.persistence.exceptions.InconvertibleValueException;
import lombok.Data;
import lombok.NonNull;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Model representing value of attribute from Perun.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Data
public class PerunAttributeValue {

    public final static String STRING_TYPE = "java.lang.String";
    public final static String INTEGER_TYPE = "java.lang.Integer";
    public final static String BOOLEAN_TYPE = "java.lang.Boolean";
    public final static String ARRAY_TYPE = "java.util.ArrayList";
    public final static String MAP_TYPE = "java.util.LinkedHashMap";
    public final static String LARGE_STRING_TYPE = "java.lang.LargeString";
    public final static String LARGE_ARRAY_LIST_TYPE = "java.util.LargeArrayList";

    private String type;
    private JsonNode value;

    public void setType(@NonNull String type) {
        if (StringUtils.isEmpty(type)) {
            throw new IllegalArgumentException("type can't be null or empty");
        }

        this.type = type;
    }

    public void setValue(String type, JsonNode value) {
        if (isNullValue(value)) {
            if (!BOOLEAN_TYPE.equals(type)) {
                this.value = JsonNodeFactory.instance.nullNode();
                return;
            } else {
                value = JsonNodeFactory.instance.booleanNode(false);
            }
        }

        this.value = value;
    }

    /**
     * Get value as String.
     * @return String value or null.
     */
    public String valueAsString() {
        if ((STRING_TYPE.equals(type) || LARGE_STRING_TYPE.equals(type))) {
            if (value == null || value instanceof NullNode) {
                return null;
            } else if (value instanceof TextNode) {
                return value.textValue();
            }
        }

        return value.asText();
    }

    /**
     * Get value as Long
     * @return Long value or null.
     */
    public Long valueAsLong() {
        if (INTEGER_TYPE.equals(type)) {
            if (isNullValue(value)) {
                return null;
            } else if (value instanceof NumericNode) {
                return value.longValue();
            }
        }

        throw inconvertible(Long.class.getName());
    }

    /**
     * Get value as Boolean.
     * @return TRUE if value is TRUE, FALSE in case of value being FALSE or NULL.
     */
    public boolean valueAsBoolean() {
        if (BOOLEAN_TYPE.equals(type)) {
            if (value == null || value instanceof NullNode) {
                return false;
            } else if (value instanceof BooleanNode) {
                return value.asBoolean();
            }
        }

        throw inconvertible(Boolean.class.getName());
    }

    /**
     * Get value as List of Strings
     * @return List of Strings
     */
    public List<String> valueAsList() {
        List<String> arr = new ArrayList<>();
        if ((ARRAY_TYPE.equals(type) || LARGE_ARRAY_LIST_TYPE.equals(type))) {
            if (isNullValue(value)) {
                return null;
            } else if (value instanceof ArrayNode) {
                ArrayNode arrJson = (ArrayNode) value;
                arrJson.forEach(item -> arr.add(item.asText()));
            }
        } else {
            arr.add(valueAsString());
        }

        return arr;
    }

    /**
     * Get value as map of Strings to Strings
     * @throws InconvertibleValueException when value cannot be converted to the Map<String, String>
     * @return Map of String keys to String values
     */
    public Map<String, String> valueAsMap() throws InconvertibleValueException {
        if (MAP_TYPE.equals(type)) {
            if (isNullValue(value)) {
                return new HashMap<>();
            } else if (value instanceof ObjectNode) {
                Map<String, String> res = new HashMap<>();
                ObjectNode objJson = (ObjectNode) value;
                Iterator<String> it = objJson.fieldNames();
                while (it.hasNext()) {
                    String key = it.next();
                    res.put(key, objJson.get(key).asText());
                }
                return res;
            }
        }

        throw inconvertible(Map.class.getName());
    }

    private InconvertibleValueException inconvertible(String clazzName) {
        return new InconvertibleValueException("Cannot convert value of attribute to " + clazzName +
                " for object: " + this.toString());
    }

    private static boolean isNullValue(JsonNode value) {
        return value == null ||
                value instanceof NullNode ||
                value.isNull() ||
                "null".equalsIgnoreCase(value.asText());
    }
}
