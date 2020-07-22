package cz.muni.ics.perunproxyapi.persistence.adapters.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cz.muni.ics.perunproxyapi.persistence.enums.MemberStatus;
import cz.muni.ics.perunproxyapi.persistence.models.AttributeObjectMapping;
import cz.muni.ics.perunproxyapi.persistence.models.Aup;
import cz.muni.ics.perunproxyapi.persistence.models.ExtSource;
import cz.muni.ics.perunproxyapi.persistence.models.Facility;
import cz.muni.ics.perunproxyapi.persistence.models.Group;
import cz.muni.ics.perunproxyapi.persistence.models.Member;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttribute;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue;
import cz.muni.ics.perunproxyapi.persistence.models.Resource;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import cz.muni.ics.perunproxyapi.persistence.models.UserExtSource;
import cz.muni.ics.perunproxyapi.persistence.models.Vo;


import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This class is mapping JsonNodes to object models.
 *
 * @author Peter Jancus <jancus@ics.muni.cz>
 */
public class RpcMapper {

    /**
     * Maps JsonNode to Facility model
     *
     * @param jsonNode facility in Json format to be mapped
     * @return Facility mapped from JsonNode
     */
    public static Facility mapFacility(JsonNode jsonNode) {
        Long id = jsonNode.get("id").asLong();
        String name = jsonNode.get("name").asText();
        String description = jsonNode.get("description").asText();

        return new Facility(id, name, description);
    }

    public static User mapPerunUser(JsonNode jsonNode) {
        long userId = jsonNode.path("id").asLong();
        String firstName = jsonNode.path("firstName").asText();
        String lastName = jsonNode.path("lastName").asText();

        return new User(userId, firstName, lastName);
    }

    /**
     * Maps JsonNode to Group model
     * @param jsonNode group in Json format to be mapped
     * @return Group mapped from JsonNode
     */
    public static Group mapGroup(JsonNode jsonNode) {
        Long id = jsonNode.get("id").asLong();
        Long parentGroupId = jsonNode.get("parentGroupId").asLong();
        String name = jsonNode.get("name").asText();
        String description = jsonNode.get("description").asText();
        Long voId = jsonNode.get("voId").asLong();

        return new Group(id, parentGroupId, name, description, null, voId);
    }

    /**
     * Maps JsonNode to List of Groups
     * @param jsonNode groups in Json format to be mapped
     * @return List of groups mapped or empty list
     */
    public static List<Group> mapGroups(JsonNode jsonNode) {
        List<Group> result = new ArrayList<>();
        for (int i = 0; i < jsonNode.size(); i++) {
            JsonNode groupNode = jsonNode.get(i);
            Group mappedGroup = mapGroup(groupNode);
            result.add(mappedGroup);
        }

        return result;
    }

    /**
     * Maps JsonNode to List of Facilities
     * @param jsonNode facilities in Json format to be mapped
     * @return List of facilities mapped or empty list
     */
    public static List<Facility> mapFacilities(JsonNode jsonNode) {
        List<Facility> result = new ArrayList<>();
        for (int i = 0; i < jsonNode.size(); i++) {
            JsonNode facilityNode = jsonNode.get(i);
            Facility mappedFacility = mapFacility(facilityNode);
            result.add(mappedFacility);
        }

        return result;
    }

    /**
     * Maps JsonNode to Member model
     * @param jsonNode member in Json format to be mapped
     * @return Member mapped from JsonNode
     */
    public static Member mapMember(JsonNode jsonNode) {
        Long id = jsonNode.get("id").asLong();
        Long userId = jsonNode.get("userId").asLong();
        Long voId = jsonNode.get("voId").asLong();
        MemberStatus status = MemberStatus.fromString(jsonNode.get("status").asText());

        return new Member(id, userId, voId, status);
    }

    /**
     * Maps JsonNode to List of Member model
     * @param jsonNode members in JSON format to be mapped
     * @return Members mapped from JsonNode
     */
    public static List<Member> mapMembers(JsonNode jsonNode) {
        List<Member> members = new ArrayList<>();
        for (int i = 0; i < jsonNode.size(); i++) {
            JsonNode memberNode = jsonNode.get(i);
            Member mappedMember = mapMember(memberNode);
            members.add(mappedMember);
        }

        return members;
    }

    /**
     * Maps JsonNode to Resource model
     * @param jsonNode resource in Json format to be mapped
     * @return Resource mapped from JsonNode
     */
    public static Resource mapResource(JsonNode jsonNode) {
        Long id = jsonNode.get("id").asLong();
        Long voId = jsonNode.get("voId").asLong();
        String name = jsonNode.get("name").asText();
        String description = jsonNode.get("description").asText();

        Vo vo = null;

        if (jsonNode.has("vo")) {
            JsonNode voJson = jsonNode.get("vo");
            vo = mapVo(voJson);
        }

        return new Resource(id, voId, name, description, vo);
    }

    /**
     * Maps JsonNode to Resource model
     * @param jsonNode resource in Json format to be mapped
     * @return Resource mapped from JsonNode
     */
    public static Resource mapAssignedResource(JsonNode jsonNode) {
        Long id = jsonNode.get("id").asLong();
        Long voId = jsonNode.get("voId").asLong();
        String name = jsonNode.get("name").asText();
        Long facilityId = jsonNode.get("facilityId").asLong();

        Vo vo = null;

        if (jsonNode.has("vo")) {
            JsonNode voJson = jsonNode.get("vo");
            vo = mapVo(voJson);
        }

        return new Resource(id, voId, name, facilityId, vo);
    }
    /**
     * Map JsonNode to Perun attribute
     * @param jsonNode attribute in JSON format to be mapped
     * @return PerunAttribute mapped from JsonNode
     */
    public static PerunAttribute mapAttribute(JsonNode jsonNode) {
        PerunAttribute attribute = new PerunAttribute();

        String type = jsonNode.get("type").asText();
        JsonNode value = jsonNode.get("value");
        PerunAttributeValue attrVal = new PerunAttributeValue(type, value);

        attribute.setId(jsonNode.get("id").asLong());
        attribute.setFriendlyName(jsonNode.get("friendlyName").asText());
        attribute.setNamespace(jsonNode.get("namespace").asText());
        attribute.setDescription(jsonNode.get("description").asText());
        attribute.setType(type);
        attribute.setDisplayName(jsonNode.get("displayName").asText());
        attribute.setWritable(jsonNode.get("writable").asBoolean());
        attribute.setUnique(jsonNode.get("unique").asBoolean());
        attribute.setBaseFriendlyName(jsonNode.get("baseFriendlyName").asText());
        attribute.setFriendlyNameParameter(jsonNode.get("friendlyNameParameter").asText());
        attribute.setValue(attrVal);
        JsonNode valueCreatedAt = jsonNode.get("valueCreatedAt");
        attribute.setValueCreatedAt(valueCreatedAt.isNull() ? null : valueCreatedAt.asText());
        JsonNode valueModifiedAt = jsonNode.get("valueModifiedAt");
        attribute.setValueModifiedAt(valueModifiedAt.isNull() ? null : valueModifiedAt.asText());

        return attribute;
    }

    /**
     * Perun attribute to JsonNode
     * @param attribute PerunAttribute
     * @return PerunAttribute as JsonNode
     */
    public static JsonNode mapAttribute(PerunAttribute attribute) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.valueToTree(attribute);
    }

    /**
     * Map JsonNode to Map of Perun attributes
     * @param jsonNode attributes as array in JSON format to be mapped
     * @return Map of PerunAttributes mapped from JsonNode, where key = URN, value = Attribute
     */
    public static Map<String, PerunAttribute> mapAttributes(JsonNode jsonNode, Set<AttributeObjectMapping> attrMappings) {
        Map<String, PerunAttribute> res = new HashMap<>();
        Map<String, PerunAttribute> attributesAsMap = new HashMap<>();

        for (int i = 0; i < jsonNode.size(); i++) {
            JsonNode attribute = jsonNode.get(i);
            PerunAttribute mappedAttribute = mapAttribute(attribute);
            attributesAsMap.put(mappedAttribute.getUrn(), mappedAttribute);
        }

        for (AttributeObjectMapping mapping: attrMappings) {
            String attrKey = mapping.getRpcName();
            if (attributesAsMap.containsKey(attrKey)) {
                PerunAttribute attribute = attributesAsMap.get(attrKey);
                res.put(mapping.getIdentifier(), attribute);
            } else {
                res.put(mapping.getIdentifier(), PerunAttribute.NULL);
            }
        }

        return res;
    }

    /**
     * Map JsonNode to VO
     * @param jsonNode vo in JSON format to e mapped
     * @return Vo mapped from JsonNode
     */
    public static Vo mapVo(JsonNode jsonNode) {
        Vo vo = new Vo();
        vo.setId(jsonNode.get("id").asLong());
        vo.setName(jsonNode.get("name").asText());
        vo.setShortName(jsonNode.get("shortName").asText());

        return vo;
    }

    /**
     * Map JsonNode to list of resources
     * @param jsonNode resource in JSON format
     * @return List of mapped resources
     */
    public static List<Resource> mapResources(JsonNode jsonNode) {
        List<Resource> res = new ArrayList<>();
        for (int i = 0; i < jsonNode.size(); i++) {
            JsonNode resource = jsonNode.get(i);
            Resource mappedResource = mapResource(resource);
            res.add(mappedResource);
        }

        return res;
    }

    /**
     * Map JsonNode to list of resources
     * @param jsonNode resource in JSON format
     * @return List of mapped resources
     */
    public static List<Resource> mapAssignedResources(JsonNode jsonNode) {
        List<Resource> res = new ArrayList<>();
        for (int i = 0; i < jsonNode.size(); i++) {
            JsonNode resource = jsonNode.get(i);
            Resource mappedResource = mapAssignedResource(resource);
            res.add(mappedResource);
        }

        return res;
    }

    /**
     * Map JsonNode to list of UserExtSources
     * @param jsonNode UserExtSources in JSON format
     * @return List of mapped UserExtSources
     */
    public static List<UserExtSource> mapUserExtSources(JsonNode jsonNode) {
        List<UserExtSource> userExtSources = new ArrayList<>();

        for (int i = 0; i < jsonNode.size(); i++) {
            JsonNode userExtSource = jsonNode.get(i);
            UserExtSource mappedUes = mapUserExtSource(userExtSource);
            userExtSources.add(mappedUes);
        }

        return userExtSources;
    }

    /**
     * Map JsonNode to UserExtSource
     * @param jsonNode UserExtSource in JSON format
     * @return Mapped UserExtSource
     */
    public static UserExtSource mapUserExtSource(JsonNode jsonNode) {
        UserExtSource ues = new UserExtSource();
        JsonNode extSourceJson = jsonNode.path("extSource");
        ExtSource extSource = mapExtSource(extSourceJson);

        ues.setId(jsonNode.get("id").asLong());
        ues.setExtSource(extSource);
        ues.setLastAccess(Timestamp.valueOf(jsonNode.get("lastAccess").asText()));
        ues.setPersistent(jsonNode.get("persistent").asBoolean());
        ues.setLogin(jsonNode.get("login").asText());
        ues.setLoa(jsonNode.get("loa").asInt());

        return ues;
    }

    /**
     * Map JsonNode to ExtSource
     * @param jsonNode ExtSource in JSON format
     * @return Mapped ExtSource
     */
    public static ExtSource mapExtSource(JsonNode jsonNode) {
        ExtSource extSource = new ExtSource();

        extSource.setId(jsonNode.get("id").asLong());
        extSource.setName(jsonNode.get("name").asText());
        extSource.setType(jsonNode.get("type").asText());

        return extSource;
    }


    /**
     * Map JsonNode to list of ExtSources
     * @param jsonNode ExtSources in JSON format
     * @return List of mapped ExtSources
     */
    public static List<ExtSource> mapExtSources(JsonNode jsonNode) {
        List<ExtSource> extSources = new ArrayList<>();

        for (int i = 0; i < jsonNode.size(); i++) {
            JsonNode extSource = jsonNode.get(i);
            ExtSource mappedExtSource = mapExtSource(extSource);
            extSources.add(mappedExtSource);
        }

        return extSources;
    }

    /**
     * Map JsonNode to list of Aup
     * @param jsonNode Aup in JSON format
     * @return Mapped Aup
     */
    public static Aup mapAup(JsonNode jsonNode) {
        Aup aup = new Aup();

        aup.setVersion(jsonNode.get("version").asText());
        aup.setDate(jsonNode.get("date").asText());
        aup.setLink(jsonNode.get("link").asText());
        aup.setText(jsonNode.get("text").asText());
        aup.setSignedOn(jsonNode.hasNonNull(Aup.SIGNED_ON) ? jsonNode.get(Aup.SIGNED_ON).asText() : null);

        return aup;
    }
}

