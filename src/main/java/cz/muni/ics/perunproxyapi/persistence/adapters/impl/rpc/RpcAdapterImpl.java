package cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import cz.muni.ics.perunproxyapi.persistence.AttributeMappingService;
import cz.muni.ics.perunproxyapi.persistence.adapters.FullAdapter;
import cz.muni.ics.perunproxyapi.persistence.connectors.PerunConnectorRpc;
import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
import cz.muni.ics.perunproxyapi.persistence.enums.MemberStatus;
import cz.muni.ics.perunproxyapi.persistence.models.AttributeObjectMapping;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.PerunAdapterLdapConstants.ENTITY_ID;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.PerunAdapterRpcConstants.ATTRIBUTES_MANAGER;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.PerunAdapterRpcConstants.FACILITIES_MANAGER;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.PerunAdapterRpcConstants.GROUPS_MANAGER;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.PerunAdapterRpcConstants.MEMBERS_MANAGER;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.PerunAdapterRpcConstants.PARAM_ATTRIBUTES;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.PerunAdapterRpcConstants.PARAM_ATTRIBUTES_WITH_SEARCHING_VALUES;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.PerunAdapterRpcConstants.PARAM_ATTRIBUTE_NAME;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.PerunAdapterRpcConstants.PARAM_ATTRIBUTE_VALUE;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.PerunAdapterRpcConstants.PARAM_ATTR_NAMES;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.PerunAdapterRpcConstants.PARAM_EXT_LOGIN;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.PerunAdapterRpcConstants.PARAM_EXT_SOURCE_LOGIN;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.PerunAdapterRpcConstants.PARAM_EXT_SOURCE_NAME;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.PerunAdapterRpcConstants.PARAM_FACILITY;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.PerunAdapterRpcConstants.PARAM_GROUP;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.PerunAdapterRpcConstants.PARAM_ID;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.PerunAdapterRpcConstants.PARAM_MEMBER;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.PerunAdapterRpcConstants.PARAM_NAME;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.PerunAdapterRpcConstants.PARAM_RESOURCE;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.PerunAdapterRpcConstants.PARAM_SHORT_NAME;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.PerunAdapterRpcConstants.PARAM_USER;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.PerunAdapterRpcConstants.PARAM_USER_EXT_SOURCE;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.PerunAdapterRpcConstants.PARAM_VO;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.PerunAdapterRpcConstants.RESOURCES_MANAGER;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.PerunAdapterRpcConstants.SEARCHER;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.PerunAdapterRpcConstants.USERS_MANAGER;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.PerunAdapterRpcConstants.VOS_MANAGER;

@Slf4j
public class RpcAdapterImpl implements FullAdapter {

    private final PerunConnectorRpc connectorRpc;
    private final AttributeMappingService attributeMappingService;

    @Autowired
    public RpcAdapterImpl(@NonNull PerunConnectorRpc perunConnectorRpc,
                          @NonNull AttributeMappingService attributeMappingService) {
        this.connectorRpc = perunConnectorRpc;
        this.attributeMappingService = attributeMappingService;
    }

    @Override
    public Map<String, PerunAttribute> getAttributes(@NonNull Entity entity,
                                                     @NonNull Long entityId,
                                                     @NonNull List<String> attrsToFetch) {
        if (!this.connectorRpc.isEnabled()) {
            return new HashMap<>();
        } else if (attrsToFetch == null || attrsToFetch.isEmpty()) {
            log.debug("No attrs to fetch - attrsToFetch: {}", (attrsToFetch == null ? "null" : "empty"));
            return new HashMap<>();
        }

        Set<AttributeObjectMapping> mappings = attributeMappingService.getMappingsByIdentifiers(attrsToFetch);

        List<String> rpcNames = mappings.stream()
                .map(AttributeObjectMapping::getRpcName)
                .collect(Collectors.toList());

        Map<String, Object> params = new LinkedHashMap<>();
        params.put(entity.toString().toLowerCase(), entityId);
        params.put(PARAM_ATTR_NAMES, rpcNames);

        JsonNode perunResponse = connectorRpc.post(ATTRIBUTES_MANAGER, "getAttributes", params);
        return RpcMapper.mapAttributes(perunResponse, mappings);
    }

    @Override
    public UserExtSource getUserExtSource(@NonNull String extSourceName,
                                          @NonNull String extSourceLogin) {
        if (!this.connectorRpc.isEnabled()) {
            return null;
        }

        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_EXT_SOURCE_NAME, extSourceName);
        params.put(PARAM_EXT_SOURCE_LOGIN, extSourceLogin);

        JsonNode perunResponse = connectorRpc.post(USERS_MANAGER, "getUserExtSourceByExtLoginAndExtSourceName", params);
        return RpcMapper.mapUserExtSource(perunResponse);
    }

    @Override
    public MemberStatus getMemberStatusByUserAndVo(@NonNull Long userId, @NonNull Long voId) {
        if (!this.connectorRpc.isEnabled()) {
            return null;
        }

        Member member = getMemberByUser(userId, voId);
        if (member != null) {
            return member.getStatus();
        } else {
            return null;
        }
    }

    @Override
    public boolean setAttributes(@NonNull Entity entity,
                                 @NonNull Long entityId,
                                 @NonNull List<PerunAttribute> attributes) {
        if (!this.connectorRpc.isEnabled()) {
            return false;
        }

        Map<String, Object> params = new LinkedHashMap<>();
        params.put(entity.toString().toLowerCase(), entityId);
        params.put(PARAM_ATTRIBUTES, attributes);

        JsonNode perunResponse = connectorRpc.post(ATTRIBUTES_MANAGER, "setAttributes", params);
        return (perunResponse == null || perunResponse.isNull() || perunResponse instanceof NullNode);
    }

    @Override
    public boolean updateUserExtSourceLastAccess(@NonNull UserExtSource userExtSource) {
        if (!this.connectorRpc.isEnabled()) {
            return false;
        }

        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_USER_EXT_SOURCE, userExtSource);

        JsonNode perunResponse = connectorRpc.post(USERS_MANAGER, "updateUserExtSourceLastAccess", params);
        return (perunResponse instanceof NullNode || perunResponse == null || perunResponse.isNull());
    }

    @Override
    public Member getMemberByUser(@NonNull Long userId, @NonNull Long voId) {
        if (!this.connectorRpc.isEnabled()) {
            return null;
        }

        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_USER, userId);
        params.put(PARAM_VO, voId);

        JsonNode perunResponse = connectorRpc.post(MEMBERS_MANAGER, "getMemberByUser", params);
        return RpcMapper.mapMember(perunResponse);
    }

    @Override
    public User getPerunUser(@NonNull String idpEntityId, @NonNull List<String> uids) {
        if (!this.connectorRpc.isEnabled()) {
            return null;
        }

        User user = null;
        for (String uid : uids) {
            user = this.getUserByExtSourceNameAndExtLogin(idpEntityId, uid);
            if (user != null) {
                break;
            }
        }

        return user;
    }

    @Override
    public List<Group> getUserGroupsInVo(@NonNull Long userId, @NonNull Long voId) {
        if (!this.connectorRpc.isEnabled()) {
            return new ArrayList<>();
        }

        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_USER, userId);
        params.put(PARAM_VO, voId);

        JsonNode perunResponse = connectorRpc.post(MEMBERS_MANAGER, "getMemberByUser", params);
        Member member = RpcMapper.mapMember(perunResponse);

        List<Group> memberGroups = this.getMemberGroups(member.getId());
        this.fillGroupUniqueNames(memberGroups);
        return memberGroups;
    }

    @Override
    public List<Group> getSpGroups(@NonNull String spIdentifier) {
        if (!this.connectorRpc.isEnabled()) {
            return new ArrayList<>();
        }

        List<Facility> facilities = getFacilitiesByAttribute(ENTITY_ID, spIdentifier);
        if (facilities == null || facilities.size() == 0) {
            return new ArrayList<>();
        }

        Facility facility = facilities.get(0);
        List<Resource> resources = this.getAssignedResources(facility.getId());

        Set<Group> spGroups = new HashSet<>();
        for (Resource resource : resources) {
            List<Group> groups = this.getAssignedGroups(resource.getId());
            this.fillGroupUniqueNames(groups);
            spGroups.addAll(groups);
        }

        return new ArrayList<>(spGroups);
    }

    @Override
    public Group getGroupByName(@NonNull Long voId, @NonNull String name) {
        if (!this.connectorRpc.isEnabled()) {
            return null;
        }

        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_VO, voId);
        params.put(PARAM_NAME, name);

        JsonNode perunResponse = connectorRpc.post(GROUPS_MANAGER, "getGroupByName", params);
        return RpcMapper.mapGroup(perunResponse);
    }

    @Override
    public Vo getVoByShortName(@NonNull String shortName) {
        if (!this.connectorRpc.isEnabled()) {
            return null;
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(PARAM_SHORT_NAME, shortName);

        JsonNode perunResponse = connectorRpc.post(VOS_MANAGER, "getVoByShortName", map);
        return RpcMapper.mapVo(perunResponse);
    }

    @Override
    public Vo getVoById(@NonNull Long id) {
        if (!this.connectorRpc.isEnabled()) {
            return null;
        }

        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_ID, id);

        JsonNode perunResponse = connectorRpc.post(VOS_MANAGER, "getVoById", params);
        return RpcMapper.mapVo(perunResponse);
    }

    @Override
    public Map<String, PerunAttributeValue> getAttributesValues(@NonNull Entity entity,
                                                                @NonNull Long entityId,
                                                                @NonNull List<String> attributes) {
        if (!this.connectorRpc.isEnabled()) {
            return new HashMap<>();
        }

        Map<String, PerunAttribute> userAttributes = this.getAttributes(entity, entityId, attributes);
        return extractAttrValues(userAttributes);
    }

    @Override
    public List<Facility> getFacilitiesByAttribute(@NonNull String attributeName, @NonNull String attrValue) {
        if (!this.connectorRpc.isEnabled()) {
            return new ArrayList<>();
        }

        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_ATTRIBUTE_NAME, attributeName);
        params.put(PARAM_ATTRIBUTE_VALUE, attrValue);

        JsonNode perunResponse = connectorRpc.post(FACILITIES_MANAGER, "getFacilitiesByAttribute", params);
        return RpcMapper.mapFacilities(perunResponse);
    }

    @Override
    public List<Group> getUsersGroupsOnFacility(@NonNull Long facilityId, @NonNull Long userId) {
        if (!this.connectorRpc.isEnabled()) {
            return new ArrayList<>();
        }

        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_USER, userId);
        params.put(PARAM_FACILITY, facilityId);

        JsonNode perunResponse = connectorRpc.post(USERS_MANAGER, "getGroupsWhereUserIsActive", params);
        return RpcMapper.mapGroups(perunResponse);
    }

    @Override
    public List<Facility> searchFacilitiesByAttributeValue(@NonNull PerunAttribute attribute) {
        if (!this.connectorRpc.isEnabled()) {
            return new ArrayList<>();
        }

        Map<String, Object> params = new LinkedHashMap<>();
        Map<String, String> attributeValue = new LinkedHashMap<>();

        attributeValue.put(attribute.getType(), attribute.getValue().toString());
        params.put(PARAM_ATTRIBUTES_WITH_SEARCHING_VALUES, attributeValue);

        JsonNode perunResponse = connectorRpc.post(SEARCHER, "getFacilities", params);

        return RpcMapper.mapFacilities(perunResponse);
    }

    // private methods

    private Map<String, PerunAttributeValue> extractAttrValues(@NonNull Map<String, PerunAttribute> attributeMap) {
        if (!this.connectorRpc.isEnabled()) {
            return new HashMap<>();
        } else if (attributeMap == null || attributeMap.isEmpty()) {
            log.debug("Given attributeMap is {}", (attributeMap == null ? "null" : "empty"));
            return new HashMap<>();
        }

        Map<String, PerunAttributeValue> resultMap = new LinkedHashMap<>();
        attributeMap.forEach((identifier, attr) -> resultMap.put(identifier, attr.getValue()));

        return resultMap;
    }

    private List<Group> getAssignedGroups(@NonNull Long resourceId) {
        if (!this.connectorRpc.isEnabled()) {
            return new ArrayList<>();
        }

        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_RESOURCE, resourceId);

        JsonNode perunResponse = connectorRpc.post(RESOURCES_MANAGER, "getAssignedGroups", params);
        return RpcMapper.mapGroups(perunResponse);
    }

    private void fillGroupUniqueNames(@NonNull List<Group> groups) {
        if (!this.connectorRpc.isEnabled()) {
            return;
        }

        for (Group group: groups) {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put(PARAM_GROUP, group.getId());
            params.put(PARAM_ATTRIBUTE_NAME, "urn:perun:group:attribute-def:virt:voShortName");

            JsonNode perunResponse = connectorRpc.post(ATTRIBUTES_MANAGER, "getAttribute", params);
            PerunAttribute attribute = RpcMapper.mapAttribute(perunResponse);
            String uniqueName = attribute.getValue() + ":" + group.getName();
            group.setUniqueGroupName(uniqueName);
        }
    }

    private List<Resource> getAssignedResources(@NonNull Long facilityId) {
        if (!this.connectorRpc.isEnabled()) {
            return new ArrayList<>();
        }

        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_FACILITY, facilityId);

        JsonNode perunResponse = connectorRpc.post(FACILITIES_MANAGER, "getAssignedResources", params);
        return RpcMapper.mapResources(perunResponse);
    }

    private List<Group> getMemberGroups(@NonNull Long memberId) {
        if (!this.connectorRpc.isEnabled()) {
            return new ArrayList<>();
        }

        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_MEMBER, memberId);

        JsonNode perunResponse = connectorRpc.post(GROUPS_MANAGER, "getMemberGroups", params);
        return RpcMapper.mapGroups(perunResponse);
    }

    private User getUserByExtSourceNameAndExtLogin(@NonNull String extSourceName, @NonNull String extLogin) {
        if (!this.connectorRpc.isEnabled()) {
            return null;
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(PARAM_EXT_SOURCE_NAME, extSourceName);
        map.put(PARAM_EXT_LOGIN, extLogin);

        JsonNode perunResponse = connectorRpc.post(USERS_MANAGER, "getUserByExtSourceNameAndExtLogin", map);
        return RpcMapper.mapUser(perunResponse);
    }

}
