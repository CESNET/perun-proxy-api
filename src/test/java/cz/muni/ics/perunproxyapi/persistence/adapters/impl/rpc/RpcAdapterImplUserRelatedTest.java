package cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.muni.ics.perunproxyapi.TestUtils;
import cz.muni.ics.perunproxyapi.persistence.AttributeMappingService;
import cz.muni.ics.perunproxyapi.persistence.adapters.FullAdapter;
import cz.muni.ics.perunproxyapi.persistence.connectors.PerunConnectorRpc;
import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
import cz.muni.ics.perunproxyapi.persistence.exceptions.InternalErrorException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.AttributeObjectMapping;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttribute;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import cz.muni.ics.perunproxyapi.persistence.models.UserExtSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcAdapterImpl.ATTRIBUTES_MANAGER;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcAdapterImpl.PARAM_ATTR_NAMES;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcAdapterImpl.USERS_MANAGER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@SpringBootTest
public class RpcAdapterImplUserRelatedTest {

    // called methods
    public static final String GET_USER_BY_EXT_SOURCE_NAME_AND_EXT_LOGIN = "getUserByExtSourceNameAndExtLogin";
    public static final String GET_ATTRIBUTES = "getAttributes";
    public static final String GET_ATTRIBUTE = "getAttribute";
    public static final String GET_USER_BY_ID = "getUserById";

    public static final String TEST_IDP_ENTITY_ID = "testIdpEntityId";
    private static final String USER_LOGIN = "login";
    private static final String TEST_LOGIN_IDENTIFIER_VALUE = "login@login.com";

    private final String uid1 = "uid1";
    private final String uid2 = "uid2";
    private final String uid3 = "uid3";
    private final List<String> uids = new LinkedList<>(Arrays.asList(uid1, uid2, uid3));

    private final List<String> userAttrIdentifiers = Arrays.asList("user:Attr1", "user:Attr2", "user:Attr3");
    private final Map<String, PerunAttribute> userAttributes = new HashMap<>();
    private final Map<String, PerunAttributeValue> userAttributesValues = new HashMap<>();
    private final ArrayNode userAttributesJsonArray = JsonNodeFactory.instance.arrayNode();

    private User sampleUser;
    private JsonNode sampleUserJson;
    private UserExtSource sampleUserExtSource;
    private JsonNode sampleUserExtSourceJson;

    @Value("${attributes.identifiers.relying_party}") private String rpIdentifierAttrIdentifier;
    @Value("${attributes.identifiers.additional_identifiers}") private String additionalIdentifiersAttrIdentifier;
    @Value("${attributes.identifiers.login}") private String loginAttrIdentifier;

    private final PerunConnectorRpc connector = mock(PerunConnectorRpc.class);
    private FullAdapter rpcAdapter;

    @Autowired
    private AttributeMappingService attributeMappingService;

    @BeforeEach
    public void setUp() throws PerunUnknownException, PerunConnectionException {
        this.rpcAdapter = new RpcAdapterImpl(connector, this.attributeMappingService,
                rpIdentifierAttrIdentifier, additionalIdentifiersAttrIdentifier, loginAttrIdentifier);
        sampleUser = TestUtils.createSampleUser(USER_LOGIN);
        sampleUserJson = TestUtils.getJsonForUser(sampleUser);
        sampleUserExtSource = TestUtils.createSampleUserExtSource();
        sampleUserExtSourceJson = TestUtils.getJsonForUserExtSource(sampleUserExtSource);
        this.initializeLoginAttribute();
        TestUtils.initializeUserAttributes(this.userAttributes, this.userAttributesValues, this.userAttributesJsonArray);
    }

    private void initializeLoginAttribute() throws PerunUnknownException, PerunConnectionException {
        AttributeObjectMapping loginAttrMapping = attributeMappingService.getMappingByIdentifier(loginAttrIdentifier);
        String rpcName = loginAttrMapping.getRpcName();
        int i = rpcName.lastIndexOf(':');
        String namespace = rpcName.substring(0, i);
        String name = rpcName.substring(i + 1);
        // attributes
        PerunAttribute loginAttribute = new PerunAttribute(1L, name, namespace, "description", "java.lang.String",
                "Login NS Example", true, true, "user", "baseFriendlyName", "", JsonNodeFactory.instance.textNode(USER_LOGIN));
        JsonNode loginAttributeJson = loginAttribute.toJson();

        when(connector.post(eq(ATTRIBUTES_MANAGER), eq(GET_ATTRIBUTE), anyMap())).thenReturn(loginAttributeJson);
    }

    private void prepareGetAttributesMock() throws PerunUnknownException, PerunConnectionException {
        Set<AttributeObjectMapping> mappings = attributeMappingService.getMappingsByIdentifiers(userAttrIdentifiers);
        List<String> rpcNames = mappings.stream().map(AttributeObjectMapping::getRpcName).collect(Collectors.toList());
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(Entity.USER.toString().toLowerCase(), sampleUser.getPerunId());
        params.put(PARAM_ATTR_NAMES, rpcNames);

        when(connector.post(eq(ATTRIBUTES_MANAGER), eq(GET_ATTRIBUTES), anyMap())).thenReturn(this.userAttributesJsonArray);
    }

    // TESTS
    // If there are more than two connector.post() calls inside a single test, we cannot use any() matchers.

    @Test
    public void testGetPerunUserExistingUser() throws PerunUnknownException, PerunConnectionException {
        ObjectNode userJson = JsonNodeFactory.instance.objectNode();
        userJson.put("id", sampleUser.getPerunId());
        userJson.put("firstName", sampleUser.getFirstName());
        userJson.put("lastName", sampleUser.getLastName());

        when(connector.post(eq(USERS_MANAGER), eq(GET_USER_BY_EXT_SOURCE_NAME_AND_EXT_LOGIN), anyMap()))
                .thenReturn(userJson);

        User actual = rpcAdapter.getPerunUser(TEST_IDP_ENTITY_ID, uids, new ArrayList<>());
        assertNotNull(actual, "Expected non-null user to be found.");
        assertEquals(sampleUser, actual, "Found user is not the same as expected.");
    }

    @Test
    public void testGetPerunUserNonExistingUser() throws PerunUnknownException, PerunConnectionException {
        when(connector.post(anyString(), anyString(), anyMap()))
                .thenReturn(JsonNodeFactory.instance.nullNode());

        assertNull(rpcAdapter.getPerunUser(TEST_IDP_ENTITY_ID, uids, new ArrayList<>()));
    }

    @Test
    public void testGetAttributesForUser() throws PerunUnknownException, PerunConnectionException {
        when(connector.post(anyString(), anyString(), anyMap())).thenReturn(this.userAttributesJsonArray);

        Map<String, PerunAttribute> result = rpcAdapter.getAttributes(Entity.USER, sampleUser.getPerunId(), userAttrIdentifiers);

        assertNotNull(result, "Should return map, null returned instead");
        assertEquals(userAttributes.size(), result.size(), "Expected and actual result differ in sizes");
        assertEquals(userAttributes, result, "Expected and actual results differ");
    }

    @Test
    public void testGetAttributeForUser() throws PerunUnknownException, PerunConnectionException {
        when(connector.post(anyString(), anyString(), anyMap())).thenReturn(userAttributesJsonArray.get(0));

        PerunAttribute result = rpcAdapter.getAttribute(Entity.USER, sampleUser.getPerunId(), userAttrIdentifiers.get(0));
        assertEquals(result, userAttributes.get(userAttrIdentifiers.get(0)));
    }

    @Test
    public void testGetAttributesForUserEmptyList() throws PerunUnknownException, PerunConnectionException {
        Map<String, PerunAttribute> result = rpcAdapter.getAttributes(Entity.USER, sampleUser.getPerunId(), new ArrayList<>());

        assertNotNull(result, "Should return map, null returned instead");
        assertTrue(result.isEmpty(), "Should return ");
    }

    @Test
    public void testGetAttributesValuesForUser() throws PerunUnknownException, PerunConnectionException {
        when(connector.post(anyString(), anyString(), anyMap())).thenReturn(this.userAttributesJsonArray);

        Map<String, PerunAttributeValue> result = rpcAdapter.getAttributesValues(Entity.USER, 1L, userAttrIdentifiers);
        assertNotNull(result, "Should return map, null returned instead");
        assertEquals(userAttributesValues.size(), result.size(), "Expected and actual result differ in sizes");
        assertEquals(userAttributesValues, result, "Expected and actual results differ");
    }

    @Test
    public void testGetAttributeValuesForUserEmptyList() throws PerunUnknownException, PerunConnectionException {
        Map<String, PerunAttributeValue> result = rpcAdapter.getAttributesValues(Entity.USER,
                sampleUser.getPerunId(), new ArrayList<>());

        assertNotNull(result, "Should return map, null returned instead");
        assertTrue(result.isEmpty(), "Should return empty map, nonempty map returned instead");
    }

    @Test
    public void testFindPerunUserById() throws PerunUnknownException, PerunConnectionException {
        when(connector.post(eq(USERS_MANAGER), eq(GET_USER_BY_ID), anyMap())).thenReturn(sampleUserJson);
        User actual = rpcAdapter.findPerunUserById(sampleUser.getPerunId(), new ArrayList<>());
        assertNotNull(actual);
        assertEquals(sampleUser, actual, "Expected and found users are different");
    }

    @Test
    public void testFindPerunUserByIdNonExistingUser() throws PerunUnknownException, PerunConnectionException {
        Long uid = 999L;
        when(connector.post(anyString(), anyString(), anyMap()))
                .thenReturn(JsonNodeFactory.instance.nullNode());

        User actual = rpcAdapter.findPerunUserById(uid, new ArrayList<>());
        assertNull(actual, "Expected null to be returned.");
    }

    @Test
    public void testGetUserExtSource() throws PerunUnknownException, PerunConnectionException {
        when(connector.post(anyString(), anyString(), anyMap()))
                .thenReturn(sampleUserExtSourceJson);
        UserExtSource actual = rpcAdapter.getUserExtSource(sampleUserExtSource.getExtSource().getName(), sampleUserExtSource.getLogin());
        assertEquals(actual, sampleUserExtSource, "Expected and found userExtSources are different.");
    }

    @Test
    public void testGetUserWithAttributesByLogin() throws PerunUnknownException, PerunConnectionException {
        JsonNode usersArray = JsonNodeFactory.instance.arrayNode().add(sampleUserJson);

        when(connector.post(anyString(), anyString(), anyMap()))
                .thenReturn(usersArray);

        this.prepareGetAttributesMock();
        sampleUser.setAttributes(this.userAttributesValues);
        User actual = rpcAdapter.getUserWithAttributesByLogin(USER_LOGIN, this.userAttrIdentifiers);
        assertEquals(sampleUser, actual);
        assertEquals(actual.getAttributes(), this.userAttributesValues);
    }

    @Test
    public void testGetUserWithAttributesByLoginNoneUser() throws PerunUnknownException, PerunConnectionException {
        when(connector.post(anyString(), anyString(), anyMap()))
                .thenReturn(JsonNodeFactory.instance.arrayNode());

        User actual = rpcAdapter.getUserWithAttributesByLogin(TEST_LOGIN_IDENTIFIER_VALUE, this.userAttrIdentifiers);
        assertNull(actual, "Expected null to be returned.");
    }

    @Test
    public void testGetUserWithAttributesByLoginMoreUsers() throws PerunUnknownException, PerunConnectionException {
        User user = mock(User.class);
        JsonNode userJson = TestUtils.getJsonForUser(user);
        JsonNode usersArray = JsonNodeFactory.instance.arrayNode()
                .add(sampleUserJson)
                .add(userJson);
        when(connector.post(anyString(), anyString(), anyMap()))
                .thenReturn(usersArray);

        assertThrows(InternalErrorException.class,
                () -> rpcAdapter.getUserWithAttributesByLogin(TEST_LOGIN_IDENTIFIER_VALUE, this.userAttrIdentifiers),
                "Excepted InternalErrorException() to be thrown.");
    }

    @Test
    public void testGetForwardedEntitlements() throws PerunUnknownException, PerunConnectionException {
        // must return attribute of type ArrayList => second mocked attribute
        when(connector.post(anyString(), anyString(), anyMap())).thenReturn(userAttributesJsonArray.get(1));

        List<String> result = rpcAdapter.getForwardedEntitlements(sampleUser.getPerunId(), userAttrIdentifiers.get(1));
        assertEquals(3, result.size());
    }

    @Test
    public void testSetAttributes() throws PerunUnknownException, PerunConnectionException {
        when(connector.post(anyString(), anyString(), anyMap())).thenReturn(JsonNodeFactory.instance.nullNode());

        boolean response = rpcAdapter.setAttributes(Entity.USER, sampleUser.getPerunId(), new ArrayList<>());
        assertTrue(response);
    }

    @Test
    public void testUpdateUserExtSourceLastAccess() throws PerunUnknownException, PerunConnectionException {
        when(connector.post(anyString(), anyString(), anyMap()))
                .thenReturn(JsonNodeFactory.instance.nullNode());

        boolean response = rpcAdapter.updateUserExtSourceLastAccess(sampleUserExtSource);
        assertTrue(response);
    }

}
