package cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc;

import com.fasterxml.jackson.databind.JsonNode;
import cz.muni.ics.perunproxyapi.persistence.enums.MemberStatus;
import cz.muni.ics.perunproxyapi.persistence.models.AttributeObjectMapping;
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
import lombok.NonNull;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * This class is mapping JsonNodes to object models.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public class RpcMapper {

    /**
     * Maps JsonNode to User model.
     * @param json User in JSON format from Perun to be mapped.
     * @return Mapped User object.
     */
    public static User mapUser(@NonNull JsonNode json) {
        if (json.isNull()) {
            return null;
        }

        Long id = json.get("id").asLong();
        String firstName = json.get("firstName").asText();
        String lastName = json.get("lastName").asText();

        return new User(id, firstName, lastName);
    }

    public static List<User> mapUsers(@NonNull JsonNode jsonArray) {
        if (jsonArray.isNull()) {
            return new ArrayList<>();
        }

        return IntStream.range(0, jsonArray.size()).
                mapToObj(jsonArray::get).
                map(RpcMapper::mapUser).
                collect(Collectors.toList());
    }

    /**
     * Maps JsonNode to Group model.
     * @param json Group in JSON format from Perun to be mapped.
     * @return Mapped Group object.
     */
    public static Group mapGroup(@NonNull JsonNode json) {
        if (json.isNull()) {
            return null;
        }

        Long id = json.get("id").asLong();
        Long parentGroupId = json.get("parentGroupId").asLong();
        String name = json.get("name").asText();
        String description = json.get("description").asText();
        Long voId = json.get("voId").asLong();

        return new Group(id, parentGroupId, name, description, null, voId);
    }

    /**
     * Maps JsonNode to List of Groups.
     * @param jsonArray JSON array of groups in JSON format from Perun to be mapped.
     * @return List of groups.
     */
    public static List<Group> mapGroups(@NonNull JsonNode jsonArray) {
        if (jsonArray.isNull()) {
            return new ArrayList<>();
        }

        List<Group> result = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonNode groupNode = jsonArray.get(i);
            Group mappedGroup = RpcMapper.mapGroup(groupNode);
            result.add(mappedGroup);
        }

        return result;
    }

    /**
     * Maps JsonNode to Facility model.
     * @param json Facility in JSON format from Perun to be mapped.
     * @return Mapped Facility object.
     */
    public static Facility mapFacility(@NonNull JsonNode json) {
        if (json.isNull()) {
            return null;
        }

        Long id = json.get("id").asLong();
        String name = json.get("name").asText();
        String description = json.get("description").asText();

        return new Facility(id, name, description);
    }

    /**
     * Maps JsonNode to List of Facilities.
     * @param jsonArray JSON array of facilities in JSON format from Perun to be mapped.
     * @return List of facilities.
     */
    public static List<Facility> mapFacilities(@NonNull JsonNode jsonArray) {
        if (jsonArray.isNull()) {
            return new ArrayList<>();
        }

        List<Facility> result = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonNode facilityNode = jsonArray.get(i);
            Facility mappedFacility = RpcMapper.mapFacility(facilityNode);
            result.add(mappedFacility);
        }

        return result;
    }

    /**
     * Maps JsonNode to Member model.
     * @param json Member in JSON format from Perun to be mapped.
     * @return Mapped Member object.
     */
    public static Member mapMember(@NonNull JsonNode json) {
        if (json.isNull()) {
            return null;
        }

        Long id = json.get("id").asLong();
        Long userId = json.get("userId").asLong();
        Long voId = json.get("voId").asLong();
        MemberStatus status = MemberStatus.fromString(json.get("status").asText());

        return new Member(id, userId, voId, status);
    }

    /**
     * Maps JsonNode to List of Members.
     * @param jsonArray JSON array of members in JSON format from Perun to be mapped.
     * @return List of members.
     */
    public static List<Member> mapMembers(@NonNull JsonNode jsonArray) {
        if (jsonArray.isNull()) {
            return new ArrayList<>();
        }

        List<Member> members = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonNode memberNode = jsonArray.get(i);
            Member mappedMember = RpcMapper.mapMember(memberNode);
            members.add(mappedMember);
        }

        return members;
    }

    /**
     * Maps JsonNode to Resource model.
     * @param json Resource in JSON format from Perun to be mapped.
     * @return Mapped Resource object.
     */
    public static Resource mapResource(@NonNull JsonNode json) {
        if (json.isNull()) {
            return null;
        }

        Long id = json.get("id").asLong();
        Long voId = json.get("voId").asLong();
        Long facilityId = json.get("facilityId").asLong();
        String name = json.get("name").asText();
        String description = json.get("description").asText();

        Vo vo = null;

        if (json.has("vo")) {
            JsonNode voJson = json.get("vo");
            vo = RpcMapper.mapVo(voJson);
        }

        return new Resource(id, voId, facilityId, name, description, vo);
    }

    /**
     * Maps JsonNode to List of Resources.
     * @param jsonArray JSON array of resources in JSON format from Perun to be mapped.
     * @return List of resources.
     */
    public static List<Resource> mapResources(@NonNull JsonNode jsonArray) {
        if (jsonArray.isNull()) {
            return new ArrayList<>();
        }

        List<Resource> resources = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonNode resource = jsonArray.get(i);
            Resource mappedResource = RpcMapper.mapResource(resource);
            resources.add(mappedResource);
        }

        return resources;
    }

    /**
     * Maps JsonNode to ExtSource model.
     * @param json ExtSource in JSON format from Perun to be mapped.
     * @return Mapped ExtSource object.
     */
    public static ExtSource mapExtSource(@NonNull JsonNode json) {
        if (json.isNull()) {
            return null;
        }

        Long id = json.get("id").asLong();
        String name = json.get("name").asText();
        String type = json.get("type").asText();

        return new ExtSource(id, name, type);
    }

    /**
     * Maps JsonNode to List of ExtSources.
     * @param jsonArray JSON array of extSources in JSON format from Perun to be mapped.
     * @return List of extSources.
     */
    public static List<ExtSource> mapExtSources(@NonNull JsonNode jsonArray) {
        if (jsonArray.isNull()) {
            return new ArrayList<>();
        }

        List<ExtSource> extSources = new ArrayList<>();

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonNode extSource = jsonArray.get(i);
            ExtSource mappedExtSource = RpcMapper.mapExtSource(extSource);
            extSources.add(mappedExtSource);
        }

        return extSources;
    }

    /**
     * Maps JsonNode to VO model.
     * @param json VO in JSON format from Perun to be mapped.
     * @return Mapped VO object.
     */
    public static Vo mapVo(@NonNull JsonNode json) {
        if (json.isNull()) {
            return null;
        }

        Long id = json.get("id").asLong();
        String name = json.get("name").asText();
        String shortName = json.get("shortName").asText();

        return new Vo(id, name, shortName);
    }

    /**
     * Maps JsonNode to List of VOs.
     * @param jsonArray JSON array of VOs in JSON format from Perun to be mapped.
     * @return List of VOs.
     */
    public static List<Vo> mapVos(@NonNull JsonNode jsonArray) {
        if (jsonArray.isNull()) {
            return new ArrayList<>();
        }

        List<Vo> vos = new ArrayList<>();

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonNode voJson = jsonArray.get(i);
            Vo mappedVo = RpcMapper.mapVo(voJson);
            vos.add(mappedVo);
        }

        return vos;
    }

    /**
     * Maps JsonNode to UserExtSource model.
     * @param json UserExtSource in JSON format from Perun to be mapped.
     * @return Mapped UserExtSource object.
     */
    public static UserExtSource mapUserExtSource(@NonNull JsonNode json) {
        if (json.isNull()) {
            return null;
        }

        Long id = json.get("id").asLong();
        String login = json.get("login").asText();
        ExtSource extSource = RpcMapper.mapExtSource(json.path("extSource"));
        int loa = json.get("loa").asInt();
        boolean persistent = json.get("persistent").asBoolean();
        Timestamp lastAccess = Timestamp.valueOf(json.get("lastAccess").asText());

        return new UserExtSource(id, extSource, login, loa, persistent, lastAccess);
    }

    /**
     * Maps JsonNode to List of UserExtSources.
     * @param jsonArray JSON array of userExtSources in JSON format from Perun to be mapped.
     * @return List of userExtSources.
     */
    public static List<UserExtSource> mapUserExtSources(@NonNull JsonNode jsonArray) {
        if (jsonArray.isNull()) {
            return new ArrayList<>();
        }

        List<UserExtSource> userExtSources = new ArrayList<>();

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonNode userExtSource = jsonArray.get(i);
            UserExtSource mappedUes = RpcMapper.mapUserExtSource(userExtSource);
            userExtSources.add(mappedUes);
        }

        return userExtSources;
    }

    /**
     * Maps JsonNode to PerunAttribute model.
     * @param json PerunAttribute in JSON format from Perun to be mapped.
     * @return Mapped PerunAttribute object.
     */
    public static PerunAttribute mapAttribute(@NonNull JsonNode json) {
        if (json.isNull()) {
            return null;
        }

        Long id = json.get("id").asLong();
        String friendlyName = json.get("friendlyName").asText();
        String namespace = json.get("namespace").asText();
        String description = json.get("description").asText();
        String type = json.get("type").asText();
        String displayName = json.get("displayName").asText();
        boolean writable = json.get("writable").asBoolean();
        boolean unique = json.get("unique").asBoolean();
        String entity = json.get("entity").asText();
        String baseFriendlyName = json.get("baseFriendlyName").asText();
        String friendlyNameParameter = json.get("friendlyNameParameter").asText();
        JsonNode value = json.get("value");

        PerunAttributeValue attrVal = new PerunAttributeValue(type, value);

        return new PerunAttribute(id, friendlyName, namespace, description, type, displayName,
                writable, unique, entity, baseFriendlyName, friendlyNameParameter, attrVal);
    }

    /**
     * Maps JsonNode to Map<String, PerunAttribute>.
     * Keys are the internal identifiers of the attributes.
     * Values are attributes corresponding to the names.
     * @param jsonArray JSON array of perunAttributes in JSON format from Perun to be mapped.
     * @param attrMappings Set of the AttributeObjectMapping objects that will be used for mapping of the attributes.
     * @return Map<String, PerunAttribute>. If attribute for identifier has not been mapped, key contains NULL as value.
     */
    public static Map<String, PerunAttribute> mapAttributes(@NonNull JsonNode jsonArray,
                                                            @NonNull Set<AttributeObjectMapping> attrMappings) {
        if (jsonArray.isNull()) {
            return new HashMap<>();
        }

        Map<String, PerunAttribute> map = new HashMap<>(); //key is internal identifier
        Map<String, PerunAttribute> mappedAttrsMap = new HashMap<>(); //key is URN of the attribute

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonNode attribute = jsonArray.get(i);
            PerunAttribute mappedAttribute = RpcMapper.mapAttribute(attribute);

            if (mappedAttribute != null) {
                mappedAttrsMap.put(mappedAttribute.getUrn(), mappedAttribute);
            }
        }

        for (AttributeObjectMapping mapping: attrMappings) {
            String attrKey = mapping.getRpcName();
            if (mappedAttrsMap.containsKey(attrKey)) {
                PerunAttribute attribute = mappedAttrsMap.get(attrKey);
                map.put(mapping.getIdentifier(), attribute);
            } else {
                map.put(mapping.getIdentifier(), null);
            }
        }

        return map;
    }

}
