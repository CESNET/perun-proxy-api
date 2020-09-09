package cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import cz.muni.ics.perunproxyapi.persistence.AttributeMappingService;
import cz.muni.ics.perunproxyapi.persistence.adapters.AdapterUtils;
import cz.muni.ics.perunproxyapi.persistence.adapters.FullAdapter;
import cz.muni.ics.perunproxyapi.persistence.connectors.PerunConnectorRpc;
import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
import cz.muni.ics.perunproxyapi.persistence.enums.MemberStatus;
import cz.muni.ics.perunproxyapi.persistence.exceptions.InternalErrorException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
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
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cz.muni.ics.perunproxyapi.persistence.enums.Entity.FACILITY;
import static cz.muni.ics.perunproxyapi.persistence.enums.Entity.RESOURCE;


@Component(value = "rpcAdapter")
@Slf4j
public class RpcAdapterImpl implements FullAdapter {

    // MANAGERS
    public static final String ATTRIBUTES_MANAGER = "attributesManager";
    public static final String FACILITIES_MANAGER = "facilitiesManager";
    public static final String GROUPS_MANAGER = "groupsManager";
    public static final String MEMBERS_MANAGER = "membersManager";
    public static final String REGISTRAR_MANAGER = "registrarManager";
    public static final String SEARCHER = "searcher";
    public static final String USERS_MANAGER = "usersManager";
    public static final String VOS_MANAGER = "vosManager";
    public static final String RESOURCES_MANAGER = "resourcesManager";

    // PARAMS
    public static final String PARAM_USER = "user";
    public static final String PARAM_ATTR_NAMES = "attrNames";
    public static final String PARAM_EXT_SOURCE_NAME = "extSourceName";
    public static final String PARAM_EXT_SOURCE_LOGIN = "extSourceLogin";
    public static final String PARAM_ATTRIBUTES = "attributes";
    public static final String PARAM_USER_EXT_SOURCE = "userExtSource";
    public static final String PARAM_VO = "vo";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_SHORT_NAME = "shortName";
    public static final String PARAM_ID = "id";
    public static final String PARAM_ATTRIBUTE_NAME = "attributeName";
    public static final String PARAM_ATTRIBUTE_VALUE = "attributeValue";
    public static final String PARAM_FACILITY = "facility";
    public static final String PARAM_ATTRIBUTES_WITH_SEARCHING_VALUES = "attributesWithSearchingValues";
    public static final String PARAM_RESOURCE = "resource";
    public static final String PARAM_GROUP = "group";
    public static final String PARAM_MEMBER = "member";
    public static final String PARAM_EXT_LOGIN = "extLogin";

    // special
    public static final String NAME_MEMBERS = "members";

    private final PerunConnectorRpc connectorRpc;
    private final AttributeMappingService attributeMappingService;

    @Setter
    @Value("${attributes.identifiers.relying_party}")
    private String rpIdentifierAttrIdentifier;

    @Autowired
    public RpcAdapterImpl(@NonNull PerunConnectorRpc perunConnectorRpc,
                          @NonNull AttributeMappingService attributeMappingService) {
        this.connectorRpc = perunConnectorRpc;
        this.attributeMappingService = attributeMappingService;
    }

    @Override
    public Map<String, PerunAttribute> getAttributes(@NonNull Entity entity,
                                                     @NonNull Long entityId,
                                                     @NonNull List<String> attrsToFetch)
            throws PerunUnknownException, PerunConnectionException
    {
        if (attrsToFetch == null || attrsToFetch.isEmpty()) {
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
    public PerunAttribute getAttribute(@NonNull Entity entity,
                                       @NonNull Long entityId,
                                       @NonNull String attrToFetch)
            throws PerunUnknownException, PerunConnectionException
    {
        AttributeObjectMapping mapping = this.getMappingForAttrName(attrToFetch);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(entity.toString().toLowerCase(), entityId);
        params.put(PARAM_ATTRIBUTE_NAME, mapping.getRpcName());

        JsonNode perunResponse = connectorRpc.post(ATTRIBUTES_MANAGER, "getAttribute", params);
        return RpcMapper.mapAttribute(perunResponse);
    }

    @Override
    public UserExtSource getUserExtSource(@NonNull String extSourceName,
                                          @NonNull String extSourceLogin)
            throws PerunUnknownException, PerunConnectionException
    {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_EXT_SOURCE_NAME, extSourceName);
        params.put(PARAM_EXT_SOURCE_LOGIN, extSourceLogin);

        JsonNode perunResponse = connectorRpc.post(USERS_MANAGER, "getUserExtSourceByExtLoginAndExtSourceName", params);
        return RpcMapper.mapUserExtSource(perunResponse);
    }

    @Override
    public MemberStatus getMemberStatusByUserAndVo(@NonNull Long userId, @NonNull Long voId)
            throws PerunUnknownException, PerunConnectionException
    {
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
                                 @NonNull List<PerunAttribute> attributes)
            throws PerunUnknownException, PerunConnectionException
    {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(entity.toString().toLowerCase(), entityId);
        params.put(PARAM_ATTRIBUTES, attributes);

        JsonNode perunResponse = connectorRpc.post(ATTRIBUTES_MANAGER, "setAttributes", params);
        return (perunResponse == null || perunResponse.isNull() || perunResponse instanceof NullNode);
    }

    @Override
    public boolean updateUserExtSourceLastAccess(@NonNull UserExtSource userExtSource)
            throws PerunUnknownException, PerunConnectionException
    {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_USER_EXT_SOURCE, userExtSource);

        JsonNode perunResponse = connectorRpc.post(USERS_MANAGER, "updateUserExtSourceLastAccess", params);
        return (perunResponse instanceof NullNode || perunResponse == null || perunResponse.isNull());
    }

    @Override
    public Member getMemberByUser(@NonNull Long userId, @NonNull Long voId)
            throws PerunUnknownException, PerunConnectionException
    {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_USER, userId);
        params.put(PARAM_VO, voId);

        JsonNode perunResponse = connectorRpc.post(MEMBERS_MANAGER, "getMemberByUser", params);
        return RpcMapper.mapMember(perunResponse);
    }

    @Override
    public User getPerunUser(@NonNull String idpEntityId, @NonNull List<String> uids)
            throws PerunUnknownException, PerunConnectionException
    {
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
    public User findPerunUserById(Long userId)
            throws PerunUnknownException, PerunConnectionException
    {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_ID, userId);

        JsonNode perunResponse = connectorRpc.post(USERS_MANAGER, "getUserById", params);
        return RpcMapper.mapUser(perunResponse);
    }

    @Override
    public List<Group> getUserGroups(@NonNull Long userId)
            throws PerunUnknownException, PerunConnectionException {
        List<Member> userMembers = this.getMembersByUser(userId);
        List<Group> groups = new LinkedList<>();
        if (!userMembers.isEmpty()) {
            for (Member member : userMembers) {
                if (!MemberStatus.VALID.equals(member.getStatus())) {
                    continue;
                } else if (member.getId() == null) {
                    log.warn("No ID found for member {}", member);
                    continue;
                }
                List<Group> memberGroups = this.getGroupsWhereMemberIsActive(member.getId());
                groups.addAll(memberGroups);
                Group memberSourceGroup = this.getGroupByName(member.getVoId(), NAME_MEMBERS);
                groups.add(memberSourceGroup);
            }
        }

        if (!groups.isEmpty()) {
            this.fillGroupUniqueNames(groups);
        }

        return groups;
    }

    private List<Member> getMembersByUser(@NonNull Long userId) throws PerunUnknownException, PerunConnectionException {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_USER, userId);

        JsonNode perunResponse = connectorRpc.post(MEMBERS_MANAGER, "getMembersByUser", params);
        return RpcMapper.mapMembers(perunResponse);
    }

    private List<Group> getGroupsWhereMemberIsActive(@NonNull Long memberId)
            throws PerunUnknownException, PerunConnectionException {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_MEMBER, memberId);

        JsonNode perunResponse = connectorRpc.post(GROUPS_MANAGER, "getGroupsWhereMemberIsActive", params);
        return RpcMapper.mapGroups(perunResponse);
    }


    @Override
    public List<Group> getSpGroups(@NonNull String spIdentifier)
            throws PerunUnknownException, PerunConnectionException
    {
        List<Facility> facilities = getFacilitiesByAttribute(rpIdentifierAttrIdentifier, spIdentifier);
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
    public Group getGroupByName(@NonNull Long voId, @NonNull String name)
            throws PerunUnknownException, PerunConnectionException
    {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_VO, voId);
        params.put(PARAM_NAME, name);

        JsonNode perunResponse = connectorRpc.post(GROUPS_MANAGER, "getGroupByName", params);
        return RpcMapper.mapGroup(perunResponse);
    }

    @Override
    public Vo getVoByShortName(@NonNull String shortName) throws PerunUnknownException, PerunConnectionException {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(PARAM_SHORT_NAME, shortName);

        JsonNode perunResponse = connectorRpc.post(VOS_MANAGER, "getVoByShortName", map);
        return RpcMapper.mapVo(perunResponse);
    }

    @Override
    public Vo getVoById(@NonNull Long id) throws PerunUnknownException, PerunConnectionException {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_ID, id);

        JsonNode perunResponse = connectorRpc.post(VOS_MANAGER, "getVoById", params);
        return RpcMapper.mapVo(perunResponse);
    }

    @Override
    public Map<String, PerunAttributeValue> getAttributesValues(@NonNull Entity entity,
                                                                @NonNull Long entityId,
                                                                @NonNull List<String> attributes)
            throws PerunUnknownException, PerunConnectionException
    {
        Map<String, PerunAttribute> attrs = this.getAttributes(entity, entityId, attributes);
        return this.extractAttrValues(attrs);
    }

    @Override
    public PerunAttributeValue getAttributeValue(@NonNull Entity entity, @NonNull Long entityId,
                                                 @NonNull String attribute)
            throws PerunUnknownException, PerunConnectionException
    {
        PerunAttribute attr = this.getAttribute(entity, entityId, attribute);
        return this.extractAttrValue(attr);
    }

    @Override
    public List<Facility> getFacilitiesByAttribute(@NonNull String attributeName, @NonNull String attrValue)
            throws PerunUnknownException, PerunConnectionException
    {
        AttributeObjectMapping mapping = this.getMappingForAttrName(attributeName);
        if (mapping == null || !StringUtils.hasText(mapping.getRpcName())) {
            log.error("Cannot look for facilities, name of the RPC attribute is unknown for identifier {} (mapping:{})",
                    attributeName, mapping);
            throw new IllegalArgumentException("Cannot fetch unknown attribute");
        }
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_ATTRIBUTE_NAME, mapping.getRpcName());
        params.put(PARAM_ATTRIBUTE_VALUE, attrValue);

        JsonNode perunResponse = connectorRpc.post(FACILITIES_MANAGER, "getFacilitiesByAttribute", params);
        return RpcMapper.mapFacilities(perunResponse);
    }

    @Override
    public Facility getFacilityByRpIdentifier(@NonNull String rpIdentifier)
            throws PerunUnknownException, PerunConnectionException
    {
        AttributeObjectMapping mapping = this.getMappingForAttrName(rpIdentifierAttrIdentifier);
        if (mapping == null || !StringUtils.hasText(mapping.getRpcName())) {
            log.error("Cannot look for facility, name of the RPC attribute is unknown for identifier {} (mapping:{})",
                    rpIdentifier, mapping);
            throw new IllegalArgumentException("Cannot fetch unknown attribute");
        }
        Map<String, Object> params = new LinkedHashMap<>();

        params.put(PARAM_ATTRIBUTE_NAME, mapping.getRpcName());
        params.put(PARAM_ATTRIBUTE_VALUE, rpIdentifier);

        JsonNode perunResponse = connectorRpc.post(FACILITIES_MANAGER, "getFacilitiesByAttribute", params);
        List<Facility> foundFacilities = RpcMapper.mapFacilities(perunResponse);
        if (foundFacilities.size() > 1) {
            log.error("Found more facilities for identifier than expected. Found {}, expected exactly", foundFacilities);
            throw new InternalErrorException("Error when looking for the RP");
        } else if (foundFacilities.size() == 0) {
            return  null;
        }

        return foundFacilities.get(0);
    }

    @Override
    public List<Group> getUsersGroupsOnFacility(@NonNull Long facilityId, @NonNull Long userId)
            throws PerunUnknownException, PerunConnectionException
    {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_USER, userId);
        params.put(PARAM_FACILITY, facilityId);

        JsonNode perunResponse = connectorRpc.post(USERS_MANAGER, "getGroupsWhereUserIsActive", params);
        List<Group> groups = RpcMapper.mapGroups(perunResponse);
        this.fillGroupUniqueNames(groups);
        return groups;
    }

    @Override
    public List<String> getForwardedEntitlements(@NonNull Long userId, String entitlementsIdentifier)
            throws PerunUnknownException, PerunConnectionException
    {
        return AdapterUtils.getForwardedEntitlements(this, userId, entitlementsIdentifier);
    }

    @Override
    public List<String> getCapabilities(@NonNull Long facilityId, @NonNull Long userId,
                                        @NonNull List<Group> userGroupsOnFacility,
                                        String resourceCapabilitiesAttrIdentifier,
                                        String facilityCapabilitiesAttrIdentifier)
            throws PerunConnectionException, PerunUnknownException
    {
        if (facilityId == null || userGroupsOnFacility == null || userGroupsOnFacility.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> resultCapabilities = new HashSet<>();
        Set<String> resourceGroupNames = new HashSet<>();
        Set<String> userGroupNames = userGroupsOnFacility.stream()
                .map(Group::getUniqueGroupName)
                .collect(Collectors.toSet());

        if (null != resourceCapabilitiesAttrIdentifier) {
            List<Resource> assignedResources = this.getAssignedRichResources(facilityId);
            for (Resource resource : assignedResources) {
                PerunAttributeValue attrValue = this.getAttributeValue(RESOURCE, resource.getId(),
                        resourceCapabilitiesAttrIdentifier);

                List<String> resourceCapabilities = attrValue.valueAsList();
                if (resourceCapabilities == null || resourceCapabilities.size() == 0) {
                    continue;
                }
                List<Group> resourceGroups = this.getAssignedGroups(resource.getId());
                for (Group group : resourceGroups) {
                    String groupName = group.getName();
                    if (resource.getVo() == null) {
                        log.warn("Could not get VO for resource ({}) assigned group, thus cannot construct unique name."
                                 + " Skip this group {}. It might be the cause of capabilities {} not being in list.",
                                   resource, group, resourceCapabilities);
                    }
                    groupName = resource.getVo().getShortName() + ':' + groupName;
                    group.setUniqueGroupName(groupName);
                    if (userGroupNames.contains(groupName)) {
                        log.trace("Group [{}] found in users groups, add capabilities [{}]", groupName,
                                resourceCapabilities);
                        resultCapabilities.addAll(resourceCapabilities);
                    } else {
                        log.trace("Group [{}] not found in users groups, continue to the next one", groupName);
                    }
                    resourceGroupNames.add(groupName);
                }
            }
        }

        if (null != facilityCapabilitiesAttrIdentifier && !Collections.disjoint(userGroupNames, resourceGroupNames)) {
            Set<String> facilityCapabilities = this.getFacilityCapabilities(facilityId, facilityCapabilitiesAttrIdentifier);
            resultCapabilities.addAll(facilityCapabilities);
        }

        return new ArrayList<>(resultCapabilities);
    }

    @Override
    public User getUserWithAttributesByLogin(@NonNull String loginAttributeIdentifier,
                                             @NonNull String login,
                                             @NonNull List<String> attrsToReturnIdentifiers)
            throws PerunUnknownException, PerunConnectionException
    {
        User user = this.getUserByLogin(loginAttributeIdentifier, login);
        if (user != null) {
            Map<String, PerunAttributeValue> userAttributes = this.getAttributesValues(Entity.USER,
                    user.getPerunId(), attrsToReturnIdentifiers);
            if (userAttributes != null) {
                user.setAttributes(userAttributes);
            }
        }
        return user;
    }

    @Override
    public User findByIdentifiers(@NonNull String idpIdentifier,
                                  @NonNull List<String> identifiers,
                                  @NonNull List<String> attrIdentifiers)
    {
        log.error("Tried to find a user by additional identifiers through the RPC adapter, which is not supported.");
        throw new UnsupportedOperationException("This operation is not supported.");
    }

    // private methods

    private Set<String> getFacilityCapabilities(Long facilityId, @NonNull String capabilitiesAttrName)
            throws PerunUnknownException, PerunConnectionException
    {
        Set<String> capabilities = new HashSet<>();
        if (facilityId != null) {
            PerunAttributeValue attr = this.getAttributeValue(FACILITY, facilityId, capabilitiesAttrName);
            if (attr != null && attr.valueAsList() != null) {
                capabilities = new HashSet<>(attr.valueAsList());
            }
        }

        return capabilities;
    }

    private Map<String, PerunAttributeValue> extractAttrValues(Map<String, PerunAttribute> attributeMap) {
        if (attributeMap == null || attributeMap.isEmpty()) {
            log.debug("Given attributeMap is {}", (attributeMap == null ? "null" : "empty"));
            return new HashMap<>();
        }

        Map<String, PerunAttributeValue> resultMap = new LinkedHashMap<>();
        attributeMap.forEach((identifier, attr) -> resultMap.put(identifier, attr == null ? null : attr.getValue()));

        return resultMap;
    }

    private PerunAttributeValue extractAttrValue(PerunAttribute attribute) {
        if (attribute == null ) {
            return null;
        }
        return attribute.getValue();
    }

    private List<Group> getAssignedGroups(@NonNull Long resourceId)
            throws PerunUnknownException, PerunConnectionException
    {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_RESOURCE, resourceId);

        JsonNode perunResponse = connectorRpc.post(RESOURCES_MANAGER, "getAssignedGroups", params);
        return RpcMapper.mapGroups(perunResponse);
    }

    private void fillGroupUniqueNames(@NonNull List<Group> groups)
            throws PerunUnknownException, PerunConnectionException
    {
        for (Group group: groups) {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put(PARAM_GROUP, group.getId());
            params.put(PARAM_ATTRIBUTE_NAME, "urn:perun:group:attribute-def:virt:voShortName");

            JsonNode perunResponse = connectorRpc.post(ATTRIBUTES_MANAGER, "getAttribute", params);
            PerunAttribute attribute = RpcMapper.mapAttribute(perunResponse);

            if (attribute != null && attribute.getValue() != null) {
                String uniqueName = attribute.getValue().valueAsString() + ":" + group.getName();
                group.setUniqueGroupName(uniqueName);
            }
        }
    }

    private List<Resource> getAssignedResources(@NonNull Long facilityId)
            throws PerunUnknownException, PerunConnectionException
    {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_FACILITY, facilityId);

        JsonNode perunResponse = connectorRpc.post(FACILITIES_MANAGER, "getAssignedResources", params);
        return RpcMapper.mapResources(perunResponse);
    }

    private List<Resource> getAssignedRichResources(@NonNull Long facilityId)
            throws PerunUnknownException, PerunConnectionException
    {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_FACILITY, facilityId);

        JsonNode perunResponse = connectorRpc.post(FACILITIES_MANAGER, "getAssignedRichResources", params);
        return RpcMapper.mapResources(perunResponse);
    }

    private User getUserByExtSourceNameAndExtLogin(@NonNull String extSourceName, @NonNull String extLogin)
            throws PerunUnknownException, PerunConnectionException
    {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(PARAM_EXT_SOURCE_NAME, extSourceName);
        map.put(PARAM_EXT_LOGIN, extLogin);

        JsonNode perunResponse = connectorRpc.post(USERS_MANAGER, "getUserByExtSourceNameAndExtLogin", map);
        return RpcMapper.mapUser(perunResponse);
    }

    private User getUserByLogin(@NonNull String loginAttrIdentifier, @NonNull String login)
            throws PerunUnknownException, PerunConnectionException
    {
        AttributeObjectMapping mapping = this.getMappingForAttrName(loginAttrIdentifier);
        if (mapping == null || !StringUtils.hasText(mapping.getRpcName())) {
            log.error("Cannot look for users, name of the RPC attribute is unknown for identifier {} (mapping:{})",
                    loginAttrIdentifier, mapping);
            throw new IllegalArgumentException("Cannot fetch unknown attribute");
        }

        Map<String, Object> params = new LinkedHashMap<>();
        params.put(PARAM_ATTRIBUTE_NAME, mapping.getRpcName());
        params.put(PARAM_ATTRIBUTE_VALUE, login);

        JsonNode perunResponse = connectorRpc.post(USERS_MANAGER, "getUsersByAttributeValue", params);
        List<User> users = RpcMapper.mapUsers(perunResponse);
        if (users.size() < 1) {
            log.debug("No users with login {} stored in the attr mapping {} found.", login, mapping);
            return null;
        } else if (users.size() > 1) {
            log.error("More than one user with login {} stored in the attr mapping {} found.", login, mapping);
            throw new InternalErrorException("Error when looking for user with login " + login);
        }

        return users.get(0);
    }

    private Set<AttributeObjectMapping> getMappingsForAttrNames(@NonNull Collection<String> attrsToFetch) {
        return this.attributeMappingService.getMappingsByIdentifiers(attrsToFetch);
    }

    private AttributeObjectMapping getMappingForAttrName(@NonNull String attrToFetch) {
        return this.attributeMappingService.getMappingByIdentifier(attrToFetch);
    }

}
