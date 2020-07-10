package cz.muni.ics.perunproxyapi.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Strings;
import cz.muni.ics.perunproxyapi.exceptions.InconvertibleValueException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Model representing value of attribute from Perun
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@ToString
@EqualsAndHashCode
public class PerunAttributeValue {

    public final static String STRING_TYPE = "java.lang.String";
    public final static String INTEGER_TYPE = "java.lang.Integer";
    public final static String BOOLEAN_TYPE = "java.lang.Boolean";
    public final static String ARRAY_TYPE = "java.util.ArrayList";
    public final static String MAP_TYPE = "java.util.LinkedHashMap";
    public final static String LARGE_STRING_TYPE = "java.lang.LargeString";
    public final static String LARGE_ARRAY_LIST_TYPE = "java.util.LargeArrayList";
    public final static String NULL_TYPE = "null";
    public final static PerunAttributeValue NULL = new PerunAttributeValue(NULL_TYPE, JsonNodeFactory.instance.nullNode());

    @Getter
    private String type;
    @Getter
    private Object value;

    public PerunAttributeValue(String type, JsonNode value) {
        super();
        this.setType(type);
        this.setValue(type, value);
    }

    public void setType(String type) {
        if (Strings.isNullOrEmpty(type)) {
            throw new IllegalArgumentException("type can't be null or empty");
        }

        this.type = type;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setValue(String type, JsonNode value) {
        if (isNullValue(value)) {
            if (!BOOLEAN_TYPE.equals(type)) {
                this.value = null;
                return;
            } else {
                value = JsonNodeFactory.instance.booleanNode(false);
            }
        }

        switch (type) {
            case STRING_TYPE:
            case LARGE_STRING_TYPE: {
                this.value = value.asText();
            } break;
            case INTEGER_TYPE: {
                this.value = value.asLong();
            } break;
            case BOOLEAN_TYPE: {
                this.value = value.asBoolean();
            } break;
            case ARRAY_TYPE:
            case LARGE_ARRAY_LIST_TYPE: {
                this.value = parseArrayValue(value);
            } break;
            case MAP_TYPE: {
                this.value = parseMapValue(value);
            } break;
            default:
                this.value = null;
        }
    }

    /**
     * Get value as String.
     * @return String value or null.
     */
    public String valueAsString() {
        if ((STRING_TYPE.equals(type) || LARGE_STRING_TYPE.equals(type))) {
            if (value == null || value instanceof NullNode) {
                return null;
            } else if (value instanceof String) {
                return (String) value;
            } else if (value instanceof TextNode) {
                return ((TextNode) value).textValue();
            }
        } else if (NULL_TYPE.equals(type)) {
            return null;
        }

        try {
            return new ObjectMapper().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    /**
     * Get value as Long
     * @return Long value or null.
     */
    public Long valueAsLong() {
        if (INTEGER_TYPE.equals(type)) {
            if (value == null || value instanceof NullNode) {
                return null;
            } else if (value instanceof Long) {
                return (Long) value;
            } else if (value instanceof NumericNode) {
                return ((NumericNode) value).longValue();
            }
        } else if (NULL_TYPE.equals(type)) {
            return null;
        }

        throw inconvertible(Long.class);
    }

    /**
     * Get value as Boolean.
     * @return True if value is TRUE, false in case of value being false or null.
     */
    public boolean valueAsBoolean() {
        if (BOOLEAN_TYPE.equals(type)) {
            if (value == null || value instanceof NullNode) {
                return false;
            } else if (value instanceof Boolean) {
                return (boolean) value;
            } else if (value instanceof BooleanNode) {
                return ((BooleanNode) value).asBoolean();
            }
        } else if (NULL_TYPE.equals(type)) {
            return false;
        }

        throw inconvertible(Boolean.class);
    }

    /**
     * Get value as List of Strings
     * @return List of strings or null
     */
    @SuppressWarnings("unchecked")
    public List<String> valueAsList() throws InconvertibleValueException {
        if ((ARRAY_TYPE.equals(type) || LARGE_ARRAY_LIST_TYPE.equals(type))) {
            if (value == null || value instanceof NullNode) {
                return new ArrayList<>();
            } else if (value instanceof List) {
                return (List<String>) value;
            } else if (value instanceof ArrayNode) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    return mapper.readValue(
                            ((ArrayNode) value).asText(),
                            mapper.getTypeFactory().constructCollectionType(List.class, String.class)
                    );
                } catch (IOException e) {
                    throw new InconvertibleValueException("Cannot convert value of attribute to List", e);
                }
            }
        } else if (NULL_TYPE.equals(type)) {
            return null;
        }

        // we need to try convert it to string as LDAP does not distinguish between List and String.
        // when LDAP sees one value, it assumes it is String, even if it is supposed to be an array
        try {
            String strVal = valueAsString();
            if (strVal == null) {
                return new ArrayList<>();
            }
            return Arrays.asList(strVal);
        } catch (InconvertibleValueException e) {
            // this is OK, we will throw exception after catch
        }

        throw inconvertible(List.class);
    }

    /**
     * Get value as map of Strings to Strings
     * @return Map or null
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> valueAsMap() throws InconvertibleValueException {
        if (MAP_TYPE.equals(type)) {
            if (value == null || value instanceof NullNode) {
                return new HashMap<>();
            } else if (value instanceof Map) {
                return (Map<String, String>) value;
            } else if (value instanceof ObjectNode) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    return mapper.readValue(
                            ((ObjectNode) value).asText(),
                            mapper.getTypeFactory().constructMapType(Map.class, String.class, String.class)
                    );
                } catch (IOException e) {
                    throw new InconvertibleValueException("Cannot convert value of attribute to Map", e);
                }
            }
        } else if (NULL_TYPE.equals(type)) {
            return null;
        }

        throw inconvertible(Map.class);
    }

    /**
     * Get value as JsonNode
     * @return JsonNode or NullNode
     */
    public JsonNode valueAsJson() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(value);
            return objectMapper.readTree(json);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return NullNode.getInstance();
    }

    private InconvertibleValueException inconvertible(Class clazz) {
        return new InconvertibleValueException("Cannot convert value of attribute to " + clazz.getName() +
                " for object: " + this.toString());
    }

    private List<String> parseArrayValue(JsonNode value) {
        List<String> arrValue = new ArrayList<>();
        for (int i = 0; i < value.size(); i++) {
            JsonNode subValue = value.get(i);
            arrValue.add(subValue.asText());
        }

        return arrValue;
    }

    private Map<String, String> parseMapValue(JsonNode value) {
        Map<String, String> mapValue = new HashMap<>();

        Iterator<String> keysIt = value.fieldNames();
        while (keysIt.hasNext()) {
            String key = keysIt.next();
            String val = value.get(key).asText();
            mapValue.put(key, val);
        }

        return mapValue;
    }

    private static boolean isNullValue(JsonNode value) {
        return value == null ||
                value.isNull() ||
                value instanceof NullNode ||
                "null".equalsIgnoreCase(value.asText());
    }
}
