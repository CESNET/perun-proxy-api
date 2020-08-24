package cz.muni.ics.perunproxyapi.persistence.adapters.rpc;


import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.muni.ics.perunproxyapi.persistence.AttributeMappingService;
import cz.muni.ics.perunproxyapi.persistence.adapters.FullAdapter;
import cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcAdapterImpl;
import cz.muni.ics.perunproxyapi.persistence.connectors.PerunConnectorRpc;
import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
import cz.muni.ics.perunproxyapi.persistence.enums.PerunAttrValueType;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.AttributeObjectMapping;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttribute;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcAdapterImpl.ATTRIBUTES_MANAGER;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcAdapterImpl.PARAM_ATTR_NAMES;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcAdapterImpl.PARAM_EXT_LOGIN;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcAdapterImpl.PARAM_EXT_SOURCE_NAME;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcAdapterImpl.PARAM_ID;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcAdapterImpl.USERS_MANAGER;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SpringBootTest
public class RpcAdapterTest {
    private static final String TEST_IDP_ENTITY_ID = "testIdpEntityId";

    private final FullAdapter rpcAdapter;

    private final PerunConnectorRpc perunConnectorRpc;
    private final AttributeMappingService attributeMappingService;
    private final ArrayNode attributesAsJson;

    private final List<String> uids = new ArrayList<>(Arrays.asList("firstUid", "secondUid", "thirdUid"));
    private final List<String> attrsToFetch = new ArrayList<>(Arrays.asList("firstAttr", "secondAttr"));

    private final Set<AttributeObjectMapping> attrMappings = new HashSet<>(Arrays.asList(
            new AttributeObjectMapping("identifier1", "rpcName:1", "ldapName1", PerunAttrValueType.STRING, ","),
            new AttributeObjectMapping("identifier2", "rpcName:2", "ldapName2", PerunAttrValueType.STRING, ",")
    ));

    @Autowired
    public RpcAdapterTest() {
        this.perunConnectorRpc = mock(PerunConnectorRpc.class);
        this.attributeMappingService = mock(AttributeMappingService.class);
        this.rpcAdapter = new RpcAdapterImpl(perunConnectorRpc, attributeMappingService);

        ObjectNode node1 = JsonNodeFactory.instance.objectNode();
        node1.put("id", 1L);
        node1.put("friendlyName", "1");
        node1.put("namespace", "rpcName");
        node1.put("description", "c");
        node1.put("type", "string");
        node1.put("displayName", "a");
        node1.put("writable", true);
        node1.put("unique", true);
        node1.put("entity", "a");
        node1.put("baseFriendlyName", "a");
        node1.put("friendlyNameParameter", "a");
        node1.put("value", "val");

        ObjectNode node2 = JsonNodeFactory.instance.objectNode();
        node2.put("id", 2L);
        node2.put("friendlyName", "2");
        node2.put("namespace", "rpcName");
        node2.put("description", "c");
        node2.put("type", "string");
        node2.put("displayName", "a");
        node2.put("writable", true);
        node2.put("unique", true);
        node2.put("entity", "a");
        node2.put("baseFriendlyName", "a");
        node2.put("friendlyNameParameter", "a");
        node2.put("value", "val");

        this.attributesAsJson = JsonNodeFactory.instance.arrayNode();
        attributesAsJson.add(node1);
        attributesAsJson.add(node2);
    }

    @Test
    public void getPerunUserReturnsNullWhenUserIsNotFound() throws PerunUnknownException, PerunConnectionException {
        Map<String, Object> map1 = new LinkedHashMap<>();
        map1.put(PARAM_EXT_SOURCE_NAME, TEST_IDP_ENTITY_ID);
        map1.put(PARAM_EXT_LOGIN, uids.get(0));

        Map<String, Object> map2 = new LinkedHashMap<>();
        map2.put(PARAM_EXT_SOURCE_NAME, TEST_IDP_ENTITY_ID);
        map2.put(PARAM_EXT_LOGIN, uids.get(1));

        Map<String, Object> map3 = new LinkedHashMap<>();
        map3.put(PARAM_EXT_SOURCE_NAME, TEST_IDP_ENTITY_ID);
        map3.put(PARAM_EXT_LOGIN, uids.get(2));

        when(perunConnectorRpc.post(USERS_MANAGER, "getUserByExtSourceNameAndExtLogin", map1)).thenReturn(JsonNodeFactory.instance.nullNode());
        when(perunConnectorRpc.post(USERS_MANAGER, "getUserByExtSourceNameAndExtLogin", map2)).thenReturn(JsonNodeFactory.instance.nullNode());
        when(perunConnectorRpc.post(USERS_MANAGER, "getUserByExtSourceNameAndExtLogin", map3)).thenReturn(JsonNodeFactory.instance.nullNode());

        assertNull(rpcAdapter.getPerunUser(TEST_IDP_ENTITY_ID, uids));
    }

    @Test
    public void getPerunUserReturnsUserWhenFound() throws PerunUnknownException, PerunConnectionException {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(PARAM_EXT_SOURCE_NAME, TEST_IDP_ENTITY_ID);
        map.put(PARAM_EXT_LOGIN, uids.get(0));

        ObjectNode node = JsonNodeFactory.instance.objectNode();

        node.put("id", 1);
        node.put("firstName", "John");
        node.put("lastName", "Doe");

        when(perunConnectorRpc.post(USERS_MANAGER, "getUserByExtSourceNameAndExtLogin", map)).thenReturn(node);

        assert(rpcAdapter.getPerunUser(TEST_IDP_ENTITY_ID, uids).equals(new User(1L, "John", "Doe")));

        verify(perunConnectorRpc, times(1))
                .post(USERS_MANAGER, "getUserByExtSourceNameAndExtLogin", map);
    }

    @Test
    public void getAttributesReturnsEmptyMapWhenAttrsToFetchIsEmpty() throws PerunUnknownException, PerunConnectionException {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(Entity.USER.toString().toLowerCase(), 1L);
        params.put(PARAM_ATTR_NAMES, new ArrayList<>(Arrays.asList("rpcName:2", "rpcName:1")));

        when(attributeMappingService.getMappingsByIdentifiers(attrsToFetch)).thenReturn(attrMappings);
        when(perunConnectorRpc.post(ATTRIBUTES_MANAGER, "getAttributes", params)).thenReturn(this.attributesAsJson);

        Map<String, PerunAttribute> result = rpcAdapter.getAttributes(Entity.USER, 1L, new ArrayList<>());

        assert(result != null && result.isEmpty());
    }

    @Test
    public void getAttributesReturnsCorrectResult() throws PerunUnknownException, PerunConnectionException {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(Entity.USER.toString().toLowerCase(), 1L);
        params.put(PARAM_ATTR_NAMES, new ArrayList<>(Arrays.asList("rpcName:2", "rpcName:1")));

        when(attributeMappingService.getMappingsByIdentifiers(attrsToFetch)).thenReturn(attrMappings);
        when(perunConnectorRpc.post(ATTRIBUTES_MANAGER, "getAttributes", params)).thenReturn(this.attributesAsJson);

        Map<String, PerunAttribute> result = rpcAdapter.getAttributes(Entity.USER, 1L, attrsToFetch);
        assert(result != null && result.size() == 2);
    }

    @Test
    public void getAttributesValuesReturnsCorrectResult() throws PerunUnknownException, PerunConnectionException {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(Entity.USER.toString().toLowerCase(), 1L);
        params.put(PARAM_ATTR_NAMES, new ArrayList<>(Arrays.asList("rpcName:2", "rpcName:1")));

        when(attributeMappingService.getMappingsByIdentifiers(attrsToFetch)).thenReturn(attrMappings);
        when(perunConnectorRpc.post(ATTRIBUTES_MANAGER, "getAttributes", params)).thenReturn(this.attributesAsJson);

        Map<String, PerunAttributeValue> result = rpcAdapter.getAttributesValues(Entity.USER, 1L, attrsToFetch);
        assert(result != null && result.size() == 2);
    }

    @Test
    public void getAttributesValuesReturnsEmptyMapWhenGetAttributesReturnsEmptyMap() throws PerunUnknownException, PerunConnectionException {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(Entity.USER.toString().toLowerCase(), 1L);
        params.put(PARAM_ATTR_NAMES, new ArrayList<>(Arrays.asList("rpcName:2", "rpcName:1")));

        when(attributeMappingService.getMappingsByIdentifiers(attrsToFetch)).thenReturn(attrMappings);
        when(perunConnectorRpc.post(ATTRIBUTES_MANAGER, "getAttributes", params)).thenReturn(JsonNodeFactory.instance.nullNode());

        Map<String, PerunAttributeValue> result = rpcAdapter.getAttributesValues(Entity.USER, 1L, attrsToFetch);
        assert(result != null && result.isEmpty());
    }

    @Test
    public void findPerunUserByIdReturnsNullWhenUserIsNotFound() throws PerunUnknownException, PerunConnectionException {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_ID, 1L);

        when(perunConnectorRpc.post(USERS_MANAGER, "getUserById", params)).thenReturn(JsonNodeFactory.instance.nullNode());

        assert(rpcAdapter.findPerunUserById(1L) == null);
    }

    @Test
    public void findPerunUserByIdReturnsCorrectResult() throws PerunUnknownException, PerunConnectionException {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_ID, 1L);

        ObjectNode user = JsonNodeFactory.instance.objectNode();
        user.put("id", 1L);
        user.put("firstName", "John");
        user.put("lastName", "Doe");

        when(perunConnectorRpc.post(USERS_MANAGER, "getUserById", params)).thenReturn(user);
        User result = rpcAdapter.findPerunUserById(1L);

        assert(result != null && result.getId() == 1L && result.getFirstName().equals("John") && result.getLastName().equals("Doe"));
    }

}
