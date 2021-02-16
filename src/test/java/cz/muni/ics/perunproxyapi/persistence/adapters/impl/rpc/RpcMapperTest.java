package cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.muni.ics.perunproxyapi.persistence.exceptions.MissingFieldException;
import cz.muni.ics.perunproxyapi.persistence.models.AttributeObjectMapping;
import cz.muni.ics.perunproxyapi.persistence.models.ExtSource;
import cz.muni.ics.perunproxyapi.persistence.models.Facility;
import cz.muni.ics.perunproxyapi.persistence.models.Group;
import cz.muni.ics.perunproxyapi.persistence.models.Member;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttribute;
import cz.muni.ics.perunproxyapi.persistence.models.Resource;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import cz.muni.ics.perunproxyapi.persistence.models.UserExtSource;
import cz.muni.ics.perunproxyapi.persistence.models.Vo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcMapper.BASE_FRIENDLY_NAME;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcMapper.DESCRIPTION;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcMapper.DISPLAY_NAME;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcMapper.ENTITY;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcMapper.EXT_SOURCE;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcMapper.FACILITY_ID;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcMapper.FIRST_NAME;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcMapper.ID;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcMapper.LAST_ACCESS;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcMapper.LAST_NAME;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcMapper.LOA;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcMapper.LOGIN;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcMapper.NAMESPACE;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcMapper.PARENT_GROUP_ID;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcMapper.PERSISTENT;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcMapper.SHORT_NAME;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcMapper.STATUS;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcMapper.UNIQUE;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcMapper.USER_ID;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcMapper.UUID;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcMapper.VALUE;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcMapper.VO_ID;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcMapper.NAME;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcMapper.FRIENDLY_NAME;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcMapper.FRIENDLY_NAME_PARAMETER;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcMapper.WRITABLE;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcMapper.TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class RpcMapperTest {

    private final ObjectMapper mapper = new ObjectMapper();

    //User
    private ObjectNode sampleUser1Json;
    private User sampleUser1;
    private ObjectNode sampleUser2Json;
    private User sampleUser2;
    private ArrayNode usersArray;

    //Group
    private ObjectNode sampleGroup1Json;
    private Group sampleGroup1;
    private ObjectNode sampleGroup2Json;
    private Group sampleGroup2;
    private ArrayNode groupsArray;

    //Facility
    private ObjectNode sampleFacility1Json;
    private Facility sampleFacility1;
    private ObjectNode sampleFacility2Json;
    private Facility sampleFacility2;
    private ArrayNode facilitiesArray;

    //Member
    private ObjectNode sampleMember1Json;
    private Member sampleMember1;
    private ObjectNode sampleMember2Json;
    private Member sampleMember2;
    private ArrayNode membersArray;

    //Resource
    private ObjectNode sampleResource1Json;
    private Resource sampleResource1;
    private ObjectNode sampleResource2Json;
    private Resource sampleResource2;
    private ArrayNode resourcesArray;

    //ExtSource
    private ObjectNode sampleExtSource1Json;
    private ExtSource sampleExtSource1;
    private ObjectNode sampleExtSource2Json;
    private ExtSource sampleExtSource2;
    private ArrayNode extSourcesArray;

    //Vo
    private ObjectNode sampleVo1Json;
    private Vo sampleVo1;
    private ObjectNode sampleVo2Json;
    private Vo sampleVo2;
    private ArrayNode vosArray;

    //UserExtSource
    private ObjectNode sampleUserExtSource1Json;
    private UserExtSource sampleUserExtSource1;
    private ObjectNode sampleUserExtSource2Json;
    private UserExtSource sampleUserExtSource2;
    private ArrayNode userExtSourcesArray;

    //PerunAttribute
    private ObjectNode sampleAttribute1Json;
    private PerunAttribute sampleAttribute1;
    private ObjectNode sampleAttribute2Json;
    private PerunAttribute sampleAttribute2;
    private ArrayNode attributesArray;

    //AttributeMappings
    private AttributeObjectMapping aom1;
    private AttributeObjectMapping aom2;
    private Set<AttributeObjectMapping> aomsSet;

    @BeforeEach
    public void setUp() throws ParseException {
        setUpUsers();
        setUpGroups();
        setUpFacilities();
        setUpMembers();
        setUpResources();
        setUpExtSource();
        setUpVos();
        setUpUserExtSources();
        setUpAttributes();
        setUpMappings();
    }

    private void setUpUsers() {
        sampleUser1Json = JsonNodeFactory.instance.objectNode();
        Long id1 = 1L;
        String firstName1 = "John";
        String lastName1 = "Doe";

        sampleUser1Json.put(ID, id1);
        sampleUser1Json.put(FIRST_NAME, firstName1);
        sampleUser1Json.put(LAST_NAME, lastName1);

        sampleUser1 = new User(id1, firstName1, lastName1, new LinkedHashMap<>());

        sampleUser2Json = JsonNodeFactory.instance.objectNode();
        Long id2 = 2L;
        String firstName2 = "Joanne";
        String lastName2 = "Doe";

        sampleUser2Json.put(ID, id2);
        sampleUser2Json.put(FIRST_NAME, firstName2);
        sampleUser2Json.put(LAST_NAME, lastName2);

        sampleUser2 = new User(id2, firstName2, lastName2, new LinkedHashMap<>());

        usersArray = JsonNodeFactory.instance.arrayNode();
        usersArray.add(sampleUser1Json);
        usersArray.add(sampleUser2Json);
    }

    private void setUpGroups() {
        sampleGroup1Json = JsonNodeFactory.instance.objectNode();
        Long id1 = 1L;
        String name1 = "name1";
        String description1 = "description1";
        String uuid1 = "uuid1";
        Long voId1 = 2L;
        Long parentGroupId1 = 3L;

        sampleGroup1Json.put(ID, id1);
        sampleGroup1Json.put(NAME, name1);
        sampleGroup1Json.put(DESCRIPTION, description1);
        sampleGroup1Json.put(UUID, uuid1);
        sampleGroup1Json.put(VO_ID, voId1);
        sampleGroup1Json.put(PARENT_GROUP_ID, parentGroupId1);

        sampleGroup1 = new Group(id1, parentGroupId1, name1, description1, null, uuid1, voId1);

        sampleGroup2Json = JsonNodeFactory.instance.objectNode();
        Long id2 = 4L;
        String name2 = "name2";
        String description2 = "description2";
        String uuid2 = "description2";
        Long voId2 = 5L;
        Long parentGroupId2 = 6L;

        sampleGroup2Json.put(ID, id2);
        sampleGroup2Json.put(NAME, name2);
        sampleGroup2Json.put(DESCRIPTION, description2);
        sampleGroup2Json.put(UUID, uuid2);
        sampleGroup2Json.put(VO_ID, voId2);
        sampleGroup2Json.put(PARENT_GROUP_ID, parentGroupId2);

        sampleGroup2 = new Group(id2, parentGroupId2, name2, description2, null, uuid2, voId2);

        groupsArray = JsonNodeFactory.instance.arrayNode();
        groupsArray.add(sampleGroup1Json);
        groupsArray.add(sampleGroup2Json);
    }

    private void setUpFacilities() {
        sampleFacility1Json = JsonNodeFactory.instance.objectNode();
        Long id1 = 1L;
        String name1 = "name1";
        String description1 = "description1";

        sampleFacility1Json.put(ID, id1);
        sampleFacility1Json.put(NAME, name1);
        sampleFacility1Json.put(DESCRIPTION, description1);

        sampleFacility1 = new Facility(id1, name1, description1);

        sampleFacility2Json = JsonNodeFactory.instance.objectNode();
        Long id2 = 2L;
        String name2 = "name2";
        String description2 = "description2";

        sampleFacility2Json.put(ID, id2);
        sampleFacility2Json.put(NAME, name2);
        sampleFacility2Json.put(DESCRIPTION, description2);

        sampleFacility2 = new Facility(id2, name2, description2);

        facilitiesArray = JsonNodeFactory.instance.arrayNode();
        facilitiesArray.add(sampleFacility1Json);
        facilitiesArray.add(sampleFacility2Json);
    }

    private void setUpMembers() {
        sampleMember1Json = JsonNodeFactory.instance.objectNode();
        Long id1 = 1L;
        Long userId1 = 1L;
        Long voId1 = 1L;
        String status1 = "VALID";

        sampleMember1Json.put(ID, id1);
        sampleMember1Json.put(USER_ID, userId1);
        sampleMember1Json.put(VO_ID, voId1);
        sampleMember1Json.put(STATUS, status1);

        sampleMember1 = new Member(id1, userId1, voId1, status1);

        sampleMember2Json = JsonNodeFactory.instance.objectNode();
        Long id2 = 2L;
        Long userId2 = 2L;
        Long voId2 = 2L;
        String status2 = "VALID";

        sampleMember2Json.put(ID, id2);
        sampleMember2Json.put(VO_ID, voId2);
        sampleMember2Json.put(USER_ID, userId2);
        sampleMember2Json.put(STATUS, status2);

        sampleMember2 = new Member(id2, userId2, voId2, status2);

        membersArray = JsonNodeFactory.instance.arrayNode();
        membersArray.add(sampleMember1Json);
        membersArray.add(sampleMember2Json);
    }

    private void setUpResources() {
        sampleResource1Json = JsonNodeFactory.instance.objectNode();
        Long id1 = 1L;
        String name1 = "name1";
        String description1 = "description1";
        Long voId1 = 2L;
        Long facilityId1 = 3L;

        sampleResource1Json.put(ID, id1);
        sampleResource1Json.put(NAME, name1);
        sampleResource1Json.put(DESCRIPTION, description1);
        sampleResource1Json.put(VO_ID, voId1);
        sampleResource1Json.put(FACILITY_ID, facilityId1);

        sampleResource1 = new Resource(id1, voId1, facilityId1, name1, description1);

        sampleResource2Json = JsonNodeFactory.instance.objectNode();
        Long id2 = 4L;
        String name2 = "name2";
        String description2 = "description2";
        Long voId2 = 5L;
        Long facilityId2 = 6L;

        sampleResource2Json.put(ID, id2);
        sampleResource2Json.put(NAME, name2);
        sampleResource2Json.put(DESCRIPTION, description2);
        sampleResource2Json.put(VO_ID, voId2);
        sampleResource2Json.put(FACILITY_ID, facilityId2);

        sampleResource2 = new Resource(id2, voId2, facilityId2, name2, description2);

        resourcesArray = JsonNodeFactory.instance.arrayNode();
        resourcesArray.add(sampleResource1Json);
        resourcesArray.add(sampleResource2Json);
    }

    private void setUpVos() {
        sampleVo1Json = JsonNodeFactory.instance.objectNode();
        Long id1 = 1L;
        String name1 = "name1";
        String shortName1 = "shortName1";

        sampleVo1Json.put(ID, id1);
        sampleVo1Json.put(NAME, name1);
        sampleVo1Json.put(SHORT_NAME, shortName1);

        sampleVo1 = new Vo(id1, name1, shortName1);

        sampleVo2Json = JsonNodeFactory.instance.objectNode();
        Long id2 = 4L;
        String name2 = "name2";
        String shortName2 = "shortName2";

        sampleVo2Json.put(ID, id2);
        sampleVo2Json.put(NAME, name2);
        sampleVo2Json.put(SHORT_NAME, shortName2);

        sampleVo2 = new Vo(id2, name2, shortName2);

        vosArray = JsonNodeFactory.instance.arrayNode();
        vosArray.add(sampleVo1Json);
        vosArray.add(sampleVo2Json);
    }

    private void setUpExtSource() {
        sampleExtSource1Json = JsonNodeFactory.instance.objectNode();
        Long id1 = 1L;
        String name1 = "name1";
        String type1 = "type1";

        sampleExtSource1Json.put(ID, id1);
        sampleExtSource1Json.put(NAME, name1);
        sampleExtSource1Json.put(TYPE, type1);

        sampleExtSource1 = new ExtSource(id1, name1, type1);

        sampleExtSource2Json = JsonNodeFactory.instance.objectNode();
        Long id2 = 4L;
        String name2 = "name2";
        String type2 = "type2";


        sampleExtSource2Json.put(ID, id2);
        sampleExtSource2Json.put(NAME, name2);
        sampleExtSource2Json.put(TYPE, type2);

        sampleExtSource2 = new ExtSource(id2, name2, type2);

        extSourcesArray = JsonNodeFactory.instance.arrayNode();
        extSourcesArray.add(sampleExtSource1Json);
        extSourcesArray.add(sampleExtSource2Json);
    }

    private void setUpUserExtSources() throws ParseException {
        sampleUserExtSource1Json = JsonNodeFactory.instance.objectNode();
        Long id1 = 1L;
        String login1 = "login1";
        int loa1 = 12;
        String time1 = "1999-10-10 10:10:11";
        Timestamp lastAccess1 = Timestamp.valueOf("1999-10-10 10:10:11");
        boolean persistent1 = true;

        sampleUserExtSource1Json.put(ID, id1);
        sampleUserExtSource1Json.put(LOGIN, login1);
        sampleUserExtSource1Json.set(EXT_SOURCE, sampleExtSource1Json);
        sampleUserExtSource1Json.put(LOA, loa1);
        sampleUserExtSource1Json.put(LAST_ACCESS, time1);
        sampleUserExtSource1Json.put(PERSISTENT, persistent1);

        sampleUserExtSource1 = new UserExtSource(id1, sampleExtSource1, login1, loa1, true, lastAccess1);

        sampleUserExtSource2Json = JsonNodeFactory.instance.objectNode();
        Long id2 = 2L;
        String login2 = "login1";
        int loa2 = 122;
        String time2 = "1999-10-10 10:10:12";
        Timestamp lastAccess2 = Timestamp.valueOf(time2);
        boolean persistent2 = true;

        sampleUserExtSource2Json.put(ID, id2);
        sampleUserExtSource2Json.put(LOGIN, login2);
        sampleUserExtSource2Json.set(EXT_SOURCE, sampleExtSource2Json);
        sampleUserExtSource2Json.put(LOA, loa2);
        sampleUserExtSource2Json.put(LAST_ACCESS, time2);
        sampleUserExtSource2Json.put(PERSISTENT, persistent2);

        sampleUserExtSource2 = new UserExtSource(id2, sampleExtSource2, login2, loa2, true, lastAccess2);

        userExtSourcesArray = JsonNodeFactory.instance.arrayNode();
        userExtSourcesArray.add(sampleUserExtSource1Json);
        userExtSourcesArray.add(sampleUserExtSource2Json);
    }

    private void setUpAttributes() {
        sampleAttribute1Json = JsonNodeFactory.instance.objectNode();
        Long id1 = 1L;
        String friendlyName1 = "friendlyName1";
        String namespace1 = "namespace1";
        String description1 = "description1";
        String type1 = "type1";
        String displayName1 = "displayName1";
        boolean writable1 = true;
        boolean unique1 = true;
        String entity1 = "user1";
        String baseFriendlyName1 = "baseFriendlyName1";
        String friendlyNameParameter1 = "friendlyNameParameter1";
        ObjectNode value1 = JsonNodeFactory.instance.objectNode();


        sampleAttribute1Json.put(ID, id1);
        sampleAttribute1Json.put(FRIENDLY_NAME, friendlyName1);
        sampleAttribute1Json.put(NAMESPACE, namespace1);
        sampleAttribute1Json.put(DESCRIPTION, description1);
        sampleAttribute1Json.put(TYPE, type1);
        sampleAttribute1Json.put(DISPLAY_NAME, displayName1);
        sampleAttribute1Json.put(WRITABLE, writable1);
        sampleAttribute1Json.put(UNIQUE, unique1);
        sampleAttribute1Json.put(ENTITY, entity1);
        sampleAttribute1Json.put(BASE_FRIENDLY_NAME, baseFriendlyName1);
        sampleAttribute1Json.put(FRIENDLY_NAME_PARAMETER, friendlyNameParameter1);
        sampleAttribute1Json.set(VALUE, value1);

        sampleAttribute1 = new PerunAttribute(id1, friendlyName1, namespace1,
                description1, type1, displayName1, writable1, unique1, entity1,
                baseFriendlyName1, friendlyNameParameter1, value1);

        sampleAttribute2Json = JsonNodeFactory.instance.objectNode();
        Long id2 = 2L;
        String friendlyName2 = "friendlyName2";
        String namespace2 = "namespace2";
        String description2 = "description2";
        String type2 = "type2";
        String displayName2 = "displayName2";
        boolean writable2 = false;
        boolean unique2 = false;
        String entity2 = "user2";
        String baseFriendlyName2 = "baseFriendlyName2";
        String friendlyNameParameter2 = "friendlyNameParameter2";
        ObjectNode value2 = JsonNodeFactory.instance.objectNode();

        sampleAttribute2Json.put(ID, id2);
        sampleAttribute2Json.put(FRIENDLY_NAME, friendlyName2);
        sampleAttribute2Json.put(NAMESPACE, namespace2);
        sampleAttribute2Json.put(DESCRIPTION, description2);
        sampleAttribute2Json.put(TYPE, type2);
        sampleAttribute2Json.put(DISPLAY_NAME, displayName2);
        sampleAttribute2Json.put(WRITABLE, writable2);
        sampleAttribute2Json.put(UNIQUE, unique2);
        sampleAttribute2Json.put(ENTITY, entity2);
        sampleAttribute2Json.put(BASE_FRIENDLY_NAME, baseFriendlyName2);
        sampleAttribute2Json.put(FRIENDLY_NAME_PARAMETER, friendlyNameParameter2);
        sampleAttribute2Json.set(VALUE, value2);

        sampleAttribute2 = new PerunAttribute(id1, friendlyName1, namespace1,
                description1, type1, displayName1, writable1, unique1, entity1,
                baseFriendlyName1, friendlyNameParameter1, value1);


        attributesArray = JsonNodeFactory.instance.arrayNode();
        attributesArray.add(sampleAttribute1Json);
        attributesArray.add(sampleAttribute2Json);
    }

    public void setUpMappings() {
        aom1 = new AttributeObjectMapping();
        aom2 = new AttributeObjectMapping();
        aomsSet = new HashSet<AttributeObjectMapping>(List.of(aom1, aom2));

        aom1.setIdentifier("first");
        aom2.setIdentifier("second");
        aom1.setRpcName(sampleAttribute1.getNamespace() + ":" + sampleAttribute1.getFriendlyName());
        aom2.setRpcName(sampleAttribute2.getNamespace() + ":" + sampleAttribute2.getFriendlyName());
    }

    @Test
    public void mapUserStandard() {
        User actual = RpcMapper.mapUser(sampleUser1Json);
        assertNotNull(actual);
        assertEquals(sampleUser1.getPerunId(), actual.getPerunId());
        assertEquals(sampleUser1.getFirstName(), actual.getFirstName());
        assertEquals(sampleUser1.getLastName(), actual.getLastName());
        assertEquals(sampleUser1, actual);
    }

    @Test
    public void mapNullUser() {
        assertThrows(NullPointerException.class, () -> RpcMapper.mapUser(null));
    }

    @Test
    public void mapNullNodeUser() {
        assertNull(RpcMapper.mapUser(JsonNodeFactory.instance.nullNode()));
    }

    @Test
    public void mapUserMissingFields() {
        JsonNode node = mapper.createObjectNode();
        assertThrows(MissingFieldException.class, () -> RpcMapper.mapUser(node));
    }

    @Test
    public void mapUsersStandard() {
        List<User> actual = RpcMapper.mapUsers(usersArray);
        assertNotNull(actual);
        assertFalse(actual.isEmpty());
        assertTrue(actual.containsAll(Arrays.asList(sampleUser1, sampleUser2)));
    }

    @Test
    public void mapNullUsers() {
        assertThrows(NullPointerException.class, () -> RpcMapper.mapUsers(null));
    }

    @Test
    public void mapNullNodeUsers() {
        List<User> actual = RpcMapper.mapUsers(JsonNodeFactory.instance.nullNode());
        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    @Test
    public void mapGroupStandard() {
        Group actual = RpcMapper.mapGroup(sampleGroup1Json);
        assertNotNull(actual);
        assertEquals(sampleGroup1.getId(), actual.getId());
        assertEquals(sampleGroup1.getName(), actual.getName());
        assertEquals(sampleGroup1.getVoId(), actual.getVoId());
        assertEquals(sampleGroup1.getParentGroupId(), actual.getParentGroupId());
        assertEquals(sampleGroup1.getDescription(), actual.getDescription());
        assertEquals(sampleGroup1, actual);
    }

    @Test
    public void mapNullGroup() {
        assertThrows(NullPointerException.class, () -> RpcMapper.mapGroup(null));
    }

    @Test
    public void mapNullNodeGroup() {
        assertNull(RpcMapper.mapGroup(JsonNodeFactory.instance.nullNode()));
    }

    @Test
    public void mapGroupMissingFields() {
        JsonNode node = mapper.createObjectNode();
        assertThrows(MissingFieldException.class, () -> RpcMapper.mapGroup(node));
    }

    @Test
    public void mapGroupsStandard() {
        List<Group> actual = RpcMapper.mapGroups(groupsArray);
        assertNotNull(actual);
        assertFalse(actual.isEmpty());
        assertTrue(actual.containsAll(Arrays.asList(sampleGroup1, sampleGroup2)));
    }

    @Test
    public void mapNullGroups() {
        assertThrows(NullPointerException.class, () -> RpcMapper.mapGroups(null));
    }

    @Test
    public void mapNullNodeGroups() {
        List<Group> actual = RpcMapper.mapGroups(JsonNodeFactory.instance.nullNode());
        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    @Test
    public void mapFacilityStandard() {
        Facility actual = RpcMapper.mapFacility(sampleFacility1Json);
        assertNotNull(actual);
        assertEquals(sampleFacility1.getId(), actual.getId());
        assertEquals(sampleFacility1.getName(), actual.getName());
        assertEquals(sampleFacility1.getDescription(), actual.getDescription());
        assertEquals(sampleFacility1, actual);
    }

    @Test
    public void mapFacilitiesStandard() {
        List<Facility> actual = RpcMapper.mapFacilities(facilitiesArray);
        assertNotNull(actual);
        assertFalse(actual.isEmpty());
        assertTrue(actual.containsAll(Arrays.asList(sampleFacility1, sampleFacility2)));
    }

    @Test
    public void mapNullFacilities() {
        assertThrows(NullPointerException.class, () -> RpcMapper.mapFacilities(null));
    }

    @Test
    public void mapNullNodeFacility() {
        assertNull(RpcMapper.mapFacility(JsonNodeFactory.instance.nullNode()));
    }

    @Test
    public void mapNullNodeFacilities() {
        List<Facility> actual = RpcMapper.mapFacilities(JsonNodeFactory.instance.nullNode());
        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    @Test
    public void mapFacilityMissingFields() {
        JsonNode node = mapper.createObjectNode();
        assertThrows(MissingFieldException.class, () -> RpcMapper.mapFacility(node));
    }

    @Test
    public void mapMemberStandard() {
        Member actual = RpcMapper.mapMember(sampleMember1Json);
        assertNotNull(actual);
        assertEquals(sampleMember1.getId(), actual.getId());
        assertEquals(sampleMember1.getVoId(), actual.getVoId());
        assertEquals(sampleMember1.getUserId(), actual.getUserId());
        assertEquals(sampleMember1.getStatus(), actual.getStatus());
        assertEquals(sampleMember1, actual);
    }

    @Test
    public void mapNullNodeMember() {
        assertNull(RpcMapper.mapMember(JsonNodeFactory.instance.nullNode()));
    }

    @Test
    public void mapNullNodeMembers() {
        List<Member> actual = RpcMapper.mapMembers(JsonNodeFactory.instance.nullNode());
        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    @Test
    public void mapMemberMissingFields() {
        JsonNode node = mapper.createObjectNode();
        assertThrows(MissingFieldException.class, () -> RpcMapper.mapMember(node));
    }

    @Test
    public void mapMembersStandard() {
        List<Member> actual = RpcMapper.mapMembers(membersArray);
        assertNotNull(actual);
        assertFalse(actual.isEmpty());
        assertTrue(actual.containsAll(Arrays.asList(sampleMember1, sampleMember2)));
    }

    @Test
    public void mapNullMember() {
        assertNull(RpcMapper.mapMember(JsonNodeFactory.instance.nullNode()));
    }

    @Test
    public void mapNullMembers() {
        assertThrows(NullPointerException.class, () -> RpcMapper.mapMembers(null));
    }

    @Test
    public void mapResourceStandard() {
        Resource actual = RpcMapper.mapResource(sampleResource1Json);
        assertNotNull(actual);
        assertEquals(sampleResource1.getId(), actual.getId());
        assertEquals(sampleResource1.getName(), actual.getName());
        assertEquals(sampleResource1.getVoId(), actual.getVoId());
        assertEquals(sampleResource1.getFacilityId(), actual.getFacilityId());
        assertEquals(sampleResource1.getDescription(), actual.getDescription());
        assertEquals(sampleResource1, actual);
    }

    @Test
    public void mapNullNodeResource() {
        assertNull(RpcMapper.mapResource(JsonNodeFactory.instance.nullNode()));
    }

    @Test
    public void mapResourceMissingFields() {
        JsonNode node = mapper.createObjectNode();
        assertThrows(MissingFieldException.class, () -> RpcMapper.mapResource(node));
    }

    @Test
    public void mapResourcesStandard() {
        List<Resource> actual = RpcMapper.mapResources(resourcesArray);
        assertNotNull(actual);
        assertFalse(actual.isEmpty());
        assertTrue(actual.containsAll(Arrays.asList(sampleResource1, sampleResource2)));
    }

    @Test
    public void mapNullResource() {
        assertNull(RpcMapper.mapResource(JsonNodeFactory.instance.nullNode()));
    }

    @Test
    public void mapNullResources() {
        assertThrows(NullPointerException.class, () -> RpcMapper.mapResources(null));
    }

    @Test
    public void mapExtSourceStandard() {
        ExtSource actual = RpcMapper.mapExtSource(sampleExtSource1Json);
        assertNotNull(actual);
        assertEquals(sampleExtSource1.getId(), actual.getId());
        assertEquals(sampleExtSource1.getName(), actual.getName());
        assertEquals(sampleExtSource1.getType(), actual.getType());
    }

    @Test
    public void mapNullNodeExtSource() {
        assertNull(RpcMapper.mapExtSource(JsonNodeFactory.instance.nullNode()));
    }

    @Test
    public void mapNullNodeExtSources() {
        List<ExtSource> actual = RpcMapper.mapExtSources(JsonNodeFactory.instance.nullNode());
        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    @Test
    public void mapExtSourcesStandard() {
        List<ExtSource> actual = RpcMapper.mapExtSources(extSourcesArray);
        assertNotNull(actual);
        assertFalse(actual.isEmpty());
        assertTrue(actual.containsAll(Arrays.asList(sampleExtSource1, sampleExtSource2)));
    }

    @Test
    public void mapExtSourceMissingFields() {
        JsonNode node = mapper.createObjectNode();
        assertThrows(MissingFieldException.class, () -> RpcMapper.mapExtSource(node));
    }

    @Test
    public void mapNullExtSource() {
        assertNull(RpcMapper.mapExtSource(JsonNodeFactory.instance.nullNode()));
    }

    @Test
    public void mapNullExtSources() {
        assertThrows(NullPointerException.class, () -> RpcMapper.mapExtSources(null));
    }

    @Test
    public void mapVoStandard() {
        Vo actual = RpcMapper.mapVo(sampleVo1Json);
        assertNotNull(actual);
        assertEquals(sampleVo1.getId(), actual.getId());
        assertEquals(sampleVo1.getName(), actual.getName());
        assertEquals(sampleVo1.getShortName(), actual.getShortName());
        assertEquals(sampleVo1, actual);
    }

    @Test
    public void mapNullNodeVo() {
        assertNull(RpcMapper.mapVo(JsonNodeFactory.instance.nullNode()));
    }

    @Test
    public void mapNullNodeVos() {
        List<Vo> actual = RpcMapper.mapVos(JsonNodeFactory.instance.nullNode());
        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    @Test
    public void mapVoMissingFields() {
        JsonNode node = mapper.createObjectNode();
        assertThrows(MissingFieldException.class, () -> RpcMapper.mapVo(node));
    }

    @Test
    public void mapVosStandard() {
        List<Vo> actual = RpcMapper.mapVos(vosArray);
        assertNotNull(actual);
        assertFalse(actual.isEmpty());
        assertTrue(actual.containsAll(Arrays.asList(sampleVo1, sampleVo2)));
    }

    @Test
    public void mapNullVo() {
        assertEquals(RpcMapper.mapVo(JsonNodeFactory.instance.nullNode()), null);
    }

    @Test
    public void mapNullVos() {
        assertThrows(NullPointerException.class, () -> RpcMapper.mapVos(null));
    }

    @Test
    public void mapUserExtSourceStandard() {
        UserExtSource actual = RpcMapper.mapUserExtSource(sampleUserExtSource1Json);
        assertNotNull(actual);
        assertEquals(sampleUserExtSource1.getId(), actual.getId());
        assertEquals(sampleUserExtSource1.getLogin(), actual.getLogin());
        assertEquals(sampleUserExtSource1.getLoa(), actual.getLoa());
        assertEquals(sampleUserExtSource1.getLastAccess(), actual.getLastAccess());
        assertEquals(sampleUserExtSource1.getExtSource(), actual.getExtSource());
        assertEquals(sampleUserExtSource1, actual);
    }

    @Test
    public void mapNullNodeUserExtSource() {
        assertNull(RpcMapper.mapUserExtSource(JsonNodeFactory.instance.nullNode()));
    }

    @Test
    public void mapUserExtSourceMissingFields() {
        JsonNode node = mapper.createObjectNode();
        assertThrows(MissingFieldException.class, () -> RpcMapper.mapUserExtSource(node));
    }

    @Test
    public void mapNullNodeUserExtSources() {
        List<UserExtSource> actual = RpcMapper.mapUserExtSources(JsonNodeFactory.instance.nullNode());
        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    @Test
    public void mapUserExtSourcesStandard() {
        List<UserExtSource> actual = RpcMapper.mapUserExtSources(userExtSourcesArray);
        assertNotNull(actual);
        assertFalse(actual.isEmpty());
        assertTrue(actual.containsAll(Arrays.asList(sampleUserExtSource1, sampleUserExtSource2)));
    }

    @Test
    public void mapNullUserExtSource() {
        assertNull(RpcMapper.mapUserExtSource(JsonNodeFactory.instance.nullNode()));
    }

    @Test
    public void mapNullUserExtSources() {
        assertThrows(NullPointerException.class, () -> RpcMapper.mapUserExtSources(null));
    }

    @Test
    public void mapAttributeStandard() {
        PerunAttribute actual = RpcMapper.mapAttribute(sampleAttribute1Json);
        assertNotNull(actual);
        assertEquals(sampleAttribute1.getId(), actual.getId());
        assertEquals(sampleAttribute1.getFriendlyName(), actual.getFriendlyName());
        assertEquals(sampleAttribute1.getNamespace(), actual.getNamespace());
        assertEquals(sampleAttribute1.getDescription(), actual.getDescription());
        assertEquals(sampleAttribute1.getType(), actual.getType());
        assertEquals(sampleAttribute1.getDisplayName(), actual.getDisplayName());
        assertEquals(sampleAttribute1.getEntity(), actual.getEntity());
        assertEquals(sampleAttribute1.getBaseFriendlyName(), actual.getBaseFriendlyName());
        assertEquals(sampleAttribute1.getFriendlyNameParameter(), actual.getFriendlyNameParameter());
        assertEquals(sampleAttribute1.getValue(), actual.getValue());
        assertEquals(sampleAttribute1, actual);
    }

    @Test
    public void mapNullNodeAttribute() {
        assertNull(RpcMapper.mapAttribute(JsonNodeFactory.instance.nullNode()));
    }

    @Test
    public void mapNullNodeAttributes() {
        Map<String, PerunAttribute> actual = RpcMapper.mapAttributes(JsonNodeFactory.instance.nullNode(), aomsSet);
        assertNotNull(actual);
        assertTrue(actual.isEmpty());
    }

    @Test
    public void mapAttributeMissingFields() {
        JsonNode node = mapper.createObjectNode();
        assertThrows(MissingFieldException.class, () -> RpcMapper.mapAttribute(node));
    }

    @Test
    public void mapAttributesStandard() {
        Map<String, PerunAttribute> actual = RpcMapper.mapAttributes(attributesArray, aomsSet);
        assertNotNull(actual);
        assertFalse(actual.isEmpty());
        assertTrue(actual.values().containsAll(Arrays.asList(sampleAttribute1, sampleAttribute2)));
    }

    @Test
    public void mapNullAttribute() {
        assertNull(RpcMapper.mapAttribute(JsonNodeFactory.instance.nullNode()));
    }

    @Test
    public void mapNullAttributes() {
        assertThrows(NullPointerException.class, () -> RpcMapper.mapAttributes(null, aomsSet));
    }

    @Test
    public void mapNullMappings() {
        assertThrows(NullPointerException.class, () -> RpcMapper.mapAttributes(attributesArray, null));
    }

}
