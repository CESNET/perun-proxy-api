package cz.muni.ics.perunproxyapi.persistence.enums;

public enum PerunAttrValueType {
    STRING,
    LARGE_STRING,
    INTEGER,
    BOOLEAN,
    ARRAY,
    LARGE_ARRAY,
    MAP_JSON,
    MAP_KEY_VALUE;

    public static PerunAttrValueType parse(String str){
        if (str == null) {
            return STRING;
        }

        switch (str.toLowerCase()) {
            case "large_string": return LARGE_STRING;
            case "integer": return INTEGER;
            case "boolean": return BOOLEAN;
            case "array":
            case "list": return ARRAY;
            case "large_array":
            case "large_list": return LARGE_ARRAY;
            case "map_json": return MAP_JSON;
            case "map_key_value": return MAP_KEY_VALUE;
            default: return STRING;
        }
    }
}
