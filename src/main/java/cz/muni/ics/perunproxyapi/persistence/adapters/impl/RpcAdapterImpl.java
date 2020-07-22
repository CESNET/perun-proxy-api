package cz.muni.ics.perunproxyapi.persistence.adapters.impl;

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
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.PerunAdapterLdapConstants.ENTITY_ID;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.PerunAdapterRpcConstants.*;

public class RpcAdapterImpl implements FullAdapter {

    private final static Logger log = LoggerFactory.getLogger(RpcAdapterImpl.class);

    @Autowired
    @Setter
    private PerunConnectorRpc connectorRpc;

    @Autowired
    @Setter
    private AttributeMappingService attributeMappingService;

    @Override
    public Map<String, PerunAttribute> getAttributes(Entity entity, Long entityId, List<String> attrsToFetch) {
        if (!this.connectorRpc.isEnabled()) {
            return new HashMap<>();
        } else if (attrsToFetch == null || attrsToFetch.isEmpty()) {
            return new HashMap<>();
        }
        log.trace("getAttributes({}, {}, {})", entity, entityId, attrsToFetch);
        Set<AttributeObjectMapping> mappings = attributeMappingService.getMappingsByIdentifiers(attrsToFetch);

        List<String> rpcNames = mappings.stream().map(AttributeObjectMapping::getRpcName).collect(Collectors.toList());

        Map<String, Object> map = new LinkedHashMap<>();
        map.put(entity.toString().toLowerCase(), entityId);
        map.put("attrNames", rpcNames);

        JsonNode res = connectorRpc.post(ATTRIBUTES_MANAGER, "getAttributes", map);
        Map<String, PerunAttribute> attributes = RpcMapper.mapAttributes(res, mappings);
        log.trace("getAttributes({}, {}, {}) returns {}", entity, entityId, attrsToFetch, attributes);
        return attributes;
    }

    @Override
    public UserExtSource getUserExtSource(String extSourceName, String extSourceLogin) {
        if (!this.connectorRpc.isEnabled()) {
            return null;
        }

        log.trace("getUserExtSource({}, {})", extSourceName, extSourceLogin);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("extSourceName", extSourceName);
        map.put("extSourceLogin", extSourceLogin);

        JsonNode res = connectorRpc.post(USERS_MANAGER, "getUserExtSourceByExtLoginAndExtSourceName", map);
        UserExtSource ues = RpcMapper.mapUserExtSource(res);
        log.trace("getUserExtSource({}, {}) returns {}", extSourceName, extSourceLogin, ues);
        return ues;

    }

    @Override
    public MemberStatus getMemberStatusByUserAndVo(User user, Vo vo) {
        if (!this.connectorRpc.isEnabled()) {
            return null;
        }
        log.trace("getMemberStatusByUserAndVo({}, {})", user, vo);
        Member member = getMemberByUser(user, vo);
        log.trace("getMemberStatusByUserAndVo({}, {}) returns {}", user, vo, member);
        return member.getStatus();
    }

    @Override
    public boolean setAttributes(Entity entity, Long entityId, List<PerunAttribute> attributes) {
        if (!this.connectorRpc.isEnabled()) {
            return false;
        }
        log.trace("setAttributes({}, {}, {})", entity, entityId, attributes);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(entity.toString().toLowerCase(), entityId);
        map.put("attributes", attributes);
        JsonNode res = connectorRpc.post(ATTRIBUTES_MANAGER, "setAttributes", map);
        boolean successful = (res == null || res.isNull() || res instanceof NullNode);
        log.trace("setAttributes({}, {}, {}) returns {}", entity, entityId, attributes, res);
        return successful;
    }

    @Override
    public boolean updateUserExtSourceLastAccess(UserExtSource userExtSource) {
        if (!this.connectorRpc.isEnabled()) {
            return false;
        }

        log.trace("updateUserExtSourceLastAccess({})", userExtSource);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("userExtSource", userExtSource);

        JsonNode res = connectorRpc.post(USERS_MANAGER, "updateUserExtSourceLastAccess", map);
        boolean successful = (res == null || res.isNull() || res instanceof NullNode);
        log.trace("updateUserExtSourceLastAccess({}) returns {}", userExtSource, res);
        return successful;
    }

    @Override
    public Member getMemberByUser(User user, Vo vo) {
        if (!this.connectorRpc.isEnabled()) {
            return null;
        }

        log.trace("getMemberByUser({}, {})", user, vo);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("user", user.getId());
        map.put("vo", vo.getId());
        JsonNode res = connectorRpc.post(MEMBERS_MANAGER, "getMemberByUser", map);
        Member member = RpcMapper.mapMember(res);
        log.trace("getMemberByUser({}, {}) returns {}", user, vo, member);
        return member;
    }

    @Override
    public User getPerunUser(Long idpEntityId, List<String> uids) {
        if (!this.connectorRpc.isEnabled()) {
            return null;
        }
        log.trace("getPerunUser({}, {})", idpEntityId, uids);
        User user = null;
        for (String uid : uids) {
            try {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("extSourceName", idpEntityId);
                map.put("extLogin", uid);
                JsonNode res = connectorRpc.post(USERS_MANAGER, "getUserByExtSourceNameAndExtLogin", map);
                user = RpcMapper.mapPerunUser(res);
                break;
            } catch (Exception e) {
                //probably needs to be done differently
            }
        }
        log.trace("getPerunUser({}, {}) returns {}", idpEntityId, uids, user);
        return user;

    }

    @Override
    public List<Group> getMemberGroups(User user, Vo vo) {
        if (!this.connectorRpc.isEnabled()) {
            return new ArrayList<>();
        }
        log.trace("getMemberGroups({}, {})", user, vo);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("user", user.getId());
        map.put("vo", vo.getId());
        JsonNode res = connectorRpc.post(MEMBERS_MANAGER, "getMemberByUser", map);
        Member member = RpcMapper.mapMember(res);

        Map<String, Object> groupsMap = new LinkedHashMap<>();
        groupsMap.put("member", member.getId());
        JsonNode groupsRes = connectorRpc.post(GROUPS_MANAGER, "getAllMemberGroups", groupsMap);
        List<Group> memberGroups = RpcMapper.mapGroups(groupsRes);

        for (Group group : memberGroups) {
            Map<String, Object> attributeMap = new LinkedHashMap<>();
            attributeMap.put("group", group.getId());
            attributeMap.put("attributeName", "urn:perun:group:attribute-def:virt:voShortName");
            JsonNode attributeRes = connectorRpc.post(ATTRIBUTES_MANAGER, "getAttribute", attributeMap);
            PerunAttribute attribute = RpcMapper.mapAttribute(attributeRes);
            group.setUniqueGroupName(attribute.getValue() + ":" + group.getName());
        }

        log.trace("getMemberGroups({}, {}) returns {}", user, vo, memberGroups);
        return memberGroups;
    }

    @Override
    public List<Group> getSpGroups(String spEntityId) {
        if (!this.connectorRpc.isEnabled()) {
            return new ArrayList<>();
        }
        log.trace("getSpGroups({})", spEntityId);
        List<Facility> facilities = getFacilitiesByAttribute(ENTITY_ID, spEntityId);
        if (facilities == null || facilities.size() == 0) {
            return new ArrayList<>();
        }
        Facility facility = facilities.get(0);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("facility", facility.getId());
        JsonNode res = connectorRpc.post(FACILITIES_MANAGER, "getAssignedResources", map);
        List<Resource> resources = RpcMapper.mapAssignedResources(res);

        List<Group> spGroups = new ArrayList<>();
        for (Resource resource : resources) {
            Map<String, Object> resourceMap = new LinkedHashMap<>();
            resourceMap.put("resource", resource.getId());
            JsonNode resourceRes = connectorRpc.post(RESOURCES_MANAGER, "getAssignedGroups", resourceMap);
            List<Group> groups = RpcMapper.mapGroups(resourceRes);
            for (Group group : groups) {
                Map<String, Object> groupMap = new LinkedHashMap<>();
                groupMap.put("group", group.getId());
                groupMap.put("attributeName", "urn:perun:group:attribute-def:virt:voShortName");
                JsonNode groupRes = connectorRpc.post(ATTRIBUTES_MANAGER, "getAttribute", groupMap);
                PerunAttribute attribute = RpcMapper.mapAttribute(groupRes);
                String uniqueName = attribute.getValue() + ":" + group.getName();
                group.setUniqueGroupName(uniqueName);
            }
            spGroups.addAll(groups);
        }
        List<Group> result = spGroups.stream().distinct().collect(Collectors.toList());
        log.trace("getSpGroups({}) returns {}", spEntityId, result);
        return result;
    }

    @Override
    public Group getGroupByName(Long voId, String name) {
        if (!this.connectorRpc.isEnabled()) {
            return null;
        }

        log.trace("getGroupByName({}, {})", voId, name);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("vo", voId);
        map.put("name", name);

        JsonNode res = connectorRpc.post(GROUPS_MANAGER, "getGroupByName", map);
        Group group = RpcMapper.mapGroup(res);
        log.trace("getGroupByName({}, {}) returns {}", voId, name, group);
        return group;
    }

    @Override
    public Vo getVoByShortName(String voShortName) {
        if (!this.connectorRpc.isEnabled()) {
            return null;
        }

        log.trace("getVoByShortName({})", voShortName);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("shortName", voShortName);

        JsonNode res = connectorRpc.post(VOS_MANAGER, "getVoByShortName", map);
        Vo vo = RpcMapper.mapVo(res);
        log.trace("getVoByShortName({}) returns {}", voShortName, vo);
        return vo;
    }

    @Override
    public Vo getVoById(Long voId) {

        if (!this.connectorRpc.isEnabled()) {
            return null;
        }

        log.trace("getVoById({})", voId);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", voId);

        JsonNode res = connectorRpc.post(VOS_MANAGER, "getVoById", map);
        Vo vo = RpcMapper.mapVo(res);
        log.trace("getVoById({}) returns {}", voId, vo);
        return vo;
    }

    @Override
    public Map<String, PerunAttributeValue> getAttributesValues(Entity entity, Long entityId, List<String> attributes) {
        if (!this.connectorRpc.isEnabled()) {
            return new HashMap<>();
        }

        log.trace("getAttributesValues({}, {}, {})", entity, entityId, attributes);
        Map<String, PerunAttribute> userAttributes = this.getAttributes(entity, entityId, attributes);
        Map<String, PerunAttributeValue> valueMap = extractValues(userAttributes);

        log.trace("getAttributesValues({}, {}, {}) returns: {}", entity, entityId, attributes, valueMap);
        return valueMap;
    }

    @Override
    public List<Facility> getFacilitiesByAttribute(String name, String attrValue) {
        log.trace("getFacilitiesByAttribute({}, {})", name, attrValue);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("attributeName", name);
        map.put("attributeValue", attrValue);
        JsonNode res = connectorRpc.post(FACILITIES_MANAGER, "getFacilitiesByAttribute", map);
        List<Facility> facilities = RpcMapper.mapFacilities(res);
        log.trace("getFacilitiesByAttribute({}, {}) returns {}", name, attrValue, facilities);
        return facilities;
    }

    @Override
    public List<Group> getUsersGroupsOnFacility(Long facilityId, Long userId) {

        if (!this.connectorRpc.isEnabled()) {
            return new ArrayList<>();
        }
        log.trace("getUsersGroupsOnFacility({}, {})", facilityId, userId);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("user", userId);
        map.put("facility", facilityId);

        JsonNode res = connectorRpc.post(USERS_MANAGER, "getGroupsWhereUserIsActive", map);
        List<Group> groups = RpcMapper.mapGroups(res);
        log.trace("getUsersGroupsOnFacility({}, {}) returns {}", facilityId, userId, groups);
        return groups;

    }

    @Override
    public List<Facility> searchFacilitiesByAttributeValue(PerunAttribute attribute) {

        if (!this.connectorRpc.isEnabled()) {
            return new ArrayList<>();
        }
        log.trace("searchFacilitiesByAttributeValue({})", attribute);
        Map<String, Object> map = new LinkedHashMap<>();
        Map<String, String> attributeValue = new LinkedHashMap<>();
        attributeValue.put(attribute.getType(), attribute.getValue().toString());
        map.put("attributesWithSearchingValues", attributeValue);

        JsonNode res = connectorRpc.post(SEARCHER, "getFacilities", map);
        List<Facility> facilities = RpcMapper.mapFacilities(res);
        log.trace("searchFacilitiesByAttributeValue({}) returns {}", attribute, facilities);
        return facilities;

    }

    private Map<String, PerunAttributeValue> extractValues(Map<String, PerunAttribute> attributeMap) {
        if (!this.connectorRpc.isEnabled()) {
            return new HashMap<>();
        }

        Map<String, PerunAttributeValue> resultMap = new LinkedHashMap<>();
        for (Map.Entry<String, PerunAttribute> attrPair : attributeMap.entrySet()) {
            String attrName = attrPair.getKey();
            PerunAttribute attr = attrPair.getValue();
            resultMap.put(attrName, attr.getValue());
        }

        return resultMap;
    }

}
