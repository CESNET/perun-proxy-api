package cz.muni.ics.perunproxyapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import cz.muni.ics.perunproxyapi.persistence.models.ExtSource;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttribute;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValueAwareModel;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import cz.muni.ics.perunproxyapi.persistence.models.UserExtSource;
import cz.muni.ics.perunproxyapi.presentation.DTOModels.UserDTO;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class TestUtils {

    public static void initializeUserAttributes(Map<String, PerunAttribute> userAttributes,
                                                Map<String, PerunAttributeValue> userAttributesValues,
                                                ArrayNode userAttributesJsonArray) {
        TextNode attr1Value = JsonNodeFactory.instance.textNode("attr1Value");
        ArrayNode attr2Value = JsonNodeFactory.instance.arrayNode();
        attr2Value.add("str1");
        attr2Value.add("str2");
        attr2Value.add("str3");
        BooleanNode attr3Value = JsonNodeFactory.instance.booleanNode(true);

        PerunAttribute attr1 = new PerunAttribute(11L, "Attr1Rpc", "user", "userAttr1Rpc", "java.lang.String", "userAttr1Rpc", true, true, "user", "userAttr1Rpc", "userAttr1Rpc", attr1Value);
        PerunAttribute attr2 = new PerunAttribute(12L, "Attr2Rpc", "user", "userAttr2Rpc", "java.lang.String", "userAttr2Rpc", true, true, "user", "userAttr2Rpc", "userAttr2Rpc", attr2Value);
        PerunAttribute attr3 = new PerunAttribute(13L, "Attr3Rpc", "user", "userAttr3Rpc", "java.lang.String", "userAttr3Rpc", true, true, "user", "userAttr3Rpc", "userAttr3Rpc", attr3Value);

        userAttributes.put("user:Attr1", attr1);
        userAttributes.put("user:Attr2", attr2);
        userAttributes.put("user:Attr3", attr3);

        userAttributesValues.put("user:Attr1", attr1.toPerunAttributeValue());
        userAttributesValues.put("user:Attr2", attr2.toPerunAttributeValue());
        userAttributesValues.put("user:Attr3", attr3.toPerunAttributeValue());

        userAttributesJsonArray.add(attr1.toJson());
        userAttributesJsonArray.add(attr2.toJson());
        userAttributesJsonArray.add(attr3.toJson());
    }

    public static User createSampleUser(String userLogin) {
        User user = new User(1L, "John", "Doe", new HashMap<>());
        user.setLogin(userLogin);
        return user;
    }


    public static JsonNode getJsonForUser(User user) {
        ObjectNode userJson = JsonNodeFactory.instance.objectNode();
        userJson.put("id", user.getPerunId());
        userJson.put("firstName", user.getFirstName());
        userJson.put("lastName", user.getLastName());
        return userJson;
    }

    public static UserDTO getDTOForUser(User sampleUser) {
        return new UserDTO(sampleUser.getLogin(), convertAttributesToDTORepresentation(sampleUser.getAttributes()));
    }

    private static Map<String, JsonNode> convertAttributesToDTORepresentation(
            Map<String, ? extends PerunAttributeValueAwareModel> map)
    {
        Map<String, JsonNode> result = new HashMap<>();
        map.forEach((key, value) -> result.put(key, value.valueAsJson()));
        return result;
    }

    public static UserExtSource createSampleUserExtSource() {
        return new UserExtSource(1L, createSampleExtSource(), "login",0, true,
                Timestamp.valueOf("2018-02-13 21:30:55"));
    }

    public static JsonNode getJsonForUserExtSource(UserExtSource userExtSource) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode userExtSourceJson = mapper.convertValue(userExtSource, ObjectNode.class);
        userExtSourceJson.put("lastAccess","2018-02-13 21:30:55");
        return userExtSourceJson;
    }

    private static ExtSource createSampleExtSource() {
        return new ExtSource(1L, "example.cz/idp/test","example.ExtSourceIdp");
    }
}
