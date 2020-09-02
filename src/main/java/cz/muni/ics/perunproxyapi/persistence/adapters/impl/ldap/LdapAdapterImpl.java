package cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.muni.ics.perunproxyapi.persistence.AttributeMappingService;
import cz.muni.ics.perunproxyapi.persistence.adapters.AdapterUtils;
import cz.muni.ics.perunproxyapi.persistence.adapters.DataAdapter;
import cz.muni.ics.perunproxyapi.persistence.connectors.PerunConnectorLdap;
import cz.muni.ics.perunproxyapi.persistence.connectors.properties.LdapProperties;
import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
import cz.muni.ics.perunproxyapi.persistence.enums.PerunAttrValueType;
import cz.muni.ics.perunproxyapi.persistence.exceptions.InconvertibleValueException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.LookupException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.AttributeObjectMapping;
import cz.muni.ics.perunproxyapi.persistence.models.Facility;
import cz.muni.ics.perunproxyapi.persistence.models.Group;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import cz.muni.ics.perunproxyapi.persistence.models.Vo;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.filter.OrFilter;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static cz.muni.ics.perunproxyapi.persistence.enums.Entity.FACILITY;
import static org.springframework.ldap.query.LdapQueryBuilder.query;
import static org.springframework.ldap.query.SearchScope.ONELEVEL;
import static org.springframework.ldap.query.SearchScope.SUBTREE;


@Component("ldapAdapter")
@Slf4j
public class LdapAdapterImpl implements DataAdapter {

    // COMMON
    public static final String O = "o";
    public static final String CN = "cn";
    public static final String SN = "sn";
    public static final String DESCRIPTION = "description";
    public static final String OBJECT_CLASS = "objectClass";

    // USER
    public static final String PERUN_USER = "perunUser";
    public static final String PERUN_USER_ID = "perunUserId";
    public static final String GIVEN_NAME = "givenName";
    public static final String MEMBER_OF = "memberOf";
    public static final String EDU_PERSON_PRINCIPAL_NAMES = "eduPersonPrincipalNames";

    // GROUP
    public static final String PERUN_GROUP = "perunGroup";
    public static final String PERUN_GROUP_ID = "perunGroupId";
    public static final String PERUN_PARENT_GROUP_ID = "perunParentGroupId";
    public static final String PERUN_UNIQUE_GROUP_NAME = "perunUniqueGroupName";
    public static final String UNIQUE_MEMBER = "uniqueMember";

    // VO
    public static final String PERUN_VO = "perunVO";
    public static final String PERUN_VO_ID = "perunVoId";

    // RESOURCE
    public static final String PERUN_RESOURCE = "perunResource";
    public static final String PERUN_RESOURCE_ID = "perunResourceId";
    public static final String ASSIGNED_TO_RESOURCE_ID = "assignedToResourceId";

    // FACILITY
    public static final String PERUN_FACILITY = "perunFacility";
    public static final String PERUN_FACILITY_ID = "perunFacilityId";
    public static final String PERUN_FACILITY_DN = "perunFacilityDn";
    public static final String ASSIGNED_GROUP_ID = "assignedGroupId";
    public static final String ENTITY_ID = "entityID";

    // REQUIRED_ATTRS
    public static final String[] PERUN_FACILITY_REQUIRED_ATTRIBUTES = new String[] {PERUN_FACILITY_ID, CN};
    public static final String[] PERUN_GROUP_REQUIRED_ATTRIBUTES = new String[]{PERUN_GROUP_ID, CN,
            PERUN_UNIQUE_GROUP_NAME, PERUN_VO_ID, DESCRIPTION};

    private final JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
    private final AttributeMappingService attributeMappingService;
    private final PerunConnectorLdap connectorLdap;
    private final String baseDn;

    @Setter
    @Value("${attributes.identifiers.relying_party}")
    private String rpIdentifierAttrIdentifier;

    @Autowired
    public LdapAdapterImpl(@NonNull PerunConnectorLdap connectorLdap,
                           @NonNull AttributeMappingService attributeMappingService,
                           @NonNull LdapProperties ldapProperties)
    {
        this.connectorLdap = connectorLdap;
        this.attributeMappingService = attributeMappingService;
        this.baseDn = ldapProperties.getBaseDn();
    }

    @Override
    public User getPerunUser(@NonNull String idpEntityId, @NonNull List<String> uids) {
        OrFilter uidsOrFilter = new OrFilter();
        for (String uid: uids) {
            uidsOrFilter.or(new EqualsFilter(EDU_PERSON_PRINCIPAL_NAMES, uid));
        }

        Filter filter = new AndFilter()
                .and(new EqualsFilter(OBJECT_CLASS, PERUN_USER))
                .and(uidsOrFilter);

        return this.getUser(filter);
    }

    @Override
    public User findPerunUserById(Long userId) {

        Filter filter = new AndFilter()
                .and(new EqualsFilter(OBJECT_CLASS, PERUN_USER))
                .and(new EqualsFilter(PERUN_USER_ID, String.valueOf(userId)));

        return this.getUser(filter);
    }

    @Override
    public List<Group> getUserGroups(@NonNull Long userId) {

        Filter filter = new AndFilter()
                .and(new EqualsFilter(OBJECT_CLASS, PERUN_GROUP))
                .and(new EqualsFilter(UNIQUE_MEMBER, PERUN_USER_ID + '=' + userId + ",ou=People," + baseDn));
        LdapQuery query = query()
                .attributes(PERUN_GROUP_ID, CN, PERUN_UNIQUE_GROUP_NAME, PERUN_VO_ID, DESCRIPTION, PERUN_PARENT_GROUP_ID)
                .searchScope(SUBTREE)
                .filter(filter);
        ContextMapper<Group> mapper = this.groupMapper();

        return connectorLdap.search(query, mapper);
    }

    @Override
    public List<Group> getSpGroups(@NonNull String spIdentifier) {
        List<Facility> facilities = this.getFacilitiesByAttribute(ENTITY_ID, spIdentifier);
        if (facilities == null || facilities.size() == 0) {
            return new ArrayList<>();
        }

        Facility facility = facilities.get(0);
        if (facility == null) {
            return new ArrayList<>();
        }

        Set<Long> groupIds = this.getGroupIdsAssignedToFacility(facility.getId());
        return this.getGroupsByIds(groupIds);
    }

    @Override
    public Group getGroupByName(@NonNull Long voId, @NonNull String groupName) {
        Filter filter = new AndFilter()
                .and(new EqualsFilter(OBJECT_CLASS, PERUN_GROUP))
                .and(new EqualsFilter(PERUN_UNIQUE_GROUP_NAME, groupName));

        LdapQuery query = query()
                .attributes(PERUN_GROUP_ID, CN, PERUN_UNIQUE_GROUP_NAME, PERUN_VO_ID, DESCRIPTION, PERUN_PARENT_GROUP_ID)
                .searchScope(SUBTREE)
                .filter(filter);

        ContextMapper<Group> mapper = this.groupMapper();

        return connectorLdap.searchForObject(query, mapper);
    }

    @Override
    public Vo getVoByShortName(@NonNull String shortName) {
        Filter filter = new AndFilter()
                .and(new EqualsFilter(OBJECT_CLASS, PERUN_VO))
                .and(new EqualsFilter(O, shortName));

        return getVo(filter);
    }

    @Override
    public Vo getVoById(@NonNull Long id) {
        Filter filter = new AndFilter()
                .and(new EqualsFilter(OBJECT_CLASS, PERUN_VO))
                .and(new EqualsFilter(PERUN_VO_ID, String.valueOf(id)));

        return getVo(filter);
    }

    @Override
    public Map<String, PerunAttributeValue> getAttributesValues(@NonNull Entity entity, @NonNull Long entityId,
                                                                @NonNull List<String> attrs) {
        Map<String, PerunAttributeValue> resultMap = new HashMap<>();

        Set<AttributeObjectMapping> mappings = this.getMappingsForAttrNames(attrs);
        String[] attributes = this.getAttributesFromMappings(mappings);
        if (attributes.length != 0) {
            ContextMapper<Map<String, PerunAttributeValue>> mapper = this.attrsValuesMapper(mappings);
            String prefix = this.getPrefixForEntity(entity, entityId);

            try {
                resultMap = connectorLdap.lookup(prefix, attributes, mapper);
            } catch (LookupException e) {
                log.warn("Caught exception from lookup", e);
                resultMap = new HashMap<>();
            }
        }

        return resultMap;
    }

    @Override
    public PerunAttributeValue getAttributeValue(@NonNull Entity entity,
                                                 @NonNull Long entityId,
                                                 @NonNull String attribute)
    {
        PerunAttributeValue result = null;

        AttributeObjectMapping mapping = this.getMappingForAttrName(attribute);
        if (mapping != null) {
            ContextMapper<PerunAttributeValue> mapper = this.attrValueMapper(mapping);
            String prefix = this.getPrefixForEntity(entity, entityId);

            try {
                result = connectorLdap.lookup(prefix, new String[] {mapping.getLdapName()}, mapper);
            } catch (LookupException e) {
                log.warn("Caught exception from lookup", e);
                result = null;
            }
        }
        
        return result;
    }

    @Override
    public List<Facility> getFacilitiesByAttribute(@NonNull String attributeName, @NonNull String attrValue) {
        AttributeObjectMapping mapping = this.getMappingForAttrName(attributeName);
        if (mapping == null || !StringUtils.hasText(mapping.getLdapName())) {
            log.error("Cannot look for facilities, name of the LDAP attribute is unknown for identifier {} (mapping:{})",
                    attributeName, mapping);
            throw new IllegalArgumentException("Cannot fetch unknown attribute");
        }
        Filter filter = new AndFilter()
                .and(new EqualsFilter(OBJECT_CLASS, PERUN_FACILITY))
                .and(new EqualsFilter(attributeName, attrValue));
        LdapQuery query = query()
                .searchScope(ONELEVEL)
                .attributes(PERUN_FACILITY_ID, CN, DESCRIPTION)
                .filter(filter);

        ContextMapper<Facility> mapper = this.facilityMapper();

        return connectorLdap.search(query, mapper)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Facility getFacilityByRpIdentifier(@NonNull String rpIdentifier)
    {
        AttributeObjectMapping mapping = this.getMappingForAttrName(rpIdentifierAttrIdentifier);
        if (mapping == null || !StringUtils.hasText(mapping.getLdapName())) {
            log.error("Cannot look for facility, name of the LDAP attribute is unknown for identifier {} (mapping:{})",
                    rpIdentifier, mapping);
            throw new IllegalArgumentException("Cannot fetch unknown attribute");
        }

        Filter filter = new AndFilter()
                .and(new EqualsFilter(OBJECT_CLASS, PERUN_FACILITY))
                .and(new EqualsFilter(mapping.getLdapName(), rpIdentifier));
        LdapQuery query = query()
                .attributes(PERUN_FACILITY_ID, CN, DESCRIPTION)
                .searchScope(ONELEVEL)
                .filter(filter);

        ContextMapper<Facility> mapper = this.facilityMapper();

        return connectorLdap.searchForObject(query, mapper);
    }

    @Override
    public List<Group> getUsersGroupsOnFacility(@NonNull Long facilityId, @NonNull Long userId) {
        List<Long> facilityResourceIds = this.getFacilityResourceIds(facilityId);
        if (facilityResourceIds.isEmpty()) {
            return new ArrayList<>();
        }

        OrFilter resourceIdsFilter = new OrFilter();
        for (Long id: facilityResourceIds) {
            resourceIdsFilter.or(new EqualsFilter(ASSIGNED_TO_RESOURCE_ID, String.valueOf(id)));
        }

        AndFilter filter = new AndFilter()
                .and(new EqualsFilter(OBJECT_CLASS, PERUN_GROUP))
                .and(new EqualsFilter(UNIQUE_MEMBER, PERUN_USER_ID + '=' + userId + ",ou=People," + baseDn))
                .and(resourceIdsFilter);

        LdapQuery query = query()
                .attributes(PERUN_GROUP_ID, CN, PERUN_UNIQUE_GROUP_NAME, PERUN_VO_ID, DESCRIPTION, PERUN_PARENT_GROUP_ID)
                .searchScope(SUBTREE)
                .filter(filter);

        ContextMapper<Group> mapper = this.groupMapper();
        return connectorLdap.search(query, mapper);
    }

    @Override
    public List<User> getUsersByAttributeValue(@NonNull String attributeName, @NonNull String attributeValue) {
        // TODO
        return null;
    }

    private List<Long> getFacilityResourceIds(@NonNull Long facilityId) {
        Filter filter = new AndFilter()
                .and(new EqualsFilter(OBJECT_CLASS, PERUN_RESOURCE))
                .and(new EqualsFilter(PERUN_FACILITY_DN, PERUN_FACILITY_ID + '=' + facilityId + ',' + baseDn));

        LdapQuery query = query()
                .attributes(PERUN_RESOURCE_ID)
                .searchScope(SUBTREE)
                .filter(filter);

        ContextMapper<Long> mapper = ctx -> {
            DirContextAdapter context = (DirContextAdapter) ctx;
            if (!this.checkHasAttributes(context, query.attributes())) {
                return null;
            }

            return Long.parseLong(context.getStringAttribute(PERUN_RESOURCE_ID));
        };

        List<Long> ids = connectorLdap.search(query, mapper);
        if (ids == null) {
            return new ArrayList<>();
        } else {
            return ids.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        }
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
    {
        if (facilityId == null || userGroupsOnFacility == null || userGroupsOnFacility.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> groupNames = userGroupsOnFacility.stream()
                .map(Group::getUniqueGroupName)
                .collect(Collectors.toSet());
        Set<Long> groupIdsFromGNames = this.getGroupsByUniqueGroupNames(groupNames).stream()
                .map(Group::getId).collect(Collectors.toSet());

        OrFilter partialFilter = new OrFilter();
        for (Long gid : groupIdsFromGNames) {
            partialFilter.or(new EqualsFilter(ASSIGNED_GROUP_ID, String.valueOf(gid)));
        }
        return new ArrayList<>(this.getCapabilities(facilityId, resourceCapabilitiesAttrIdentifier,
                facilityCapabilitiesAttrIdentifier, partialFilter));
    }

    // private methods
    private List<Group> getGroupsByUniqueGroupNames(Set<String> groupNames) {
        List<Group> groups = this.getGroups(groupNames, PERUN_UNIQUE_GROUP_NAME);
        groups = groups.stream().filter(Objects::nonNull).collect(Collectors.toList());

        return groups;
    }

    private List<Group> getGroups(Collection<?> objects, String objectAttribute) {
        List<Group> result = new ArrayList<>();
        if (objects == null || objects.size() <= 0) {
            return result;
        } else {
            AndFilter filter = new AndFilter();
            if (objects.size() == 1) {
                Object first = objects.toArray()[0];
                filter.and(new EqualsFilter(OBJECT_CLASS, PERUN_GROUP))
                        .and(new EqualsFilter(objectAttribute, String.valueOf(first)));
            } else {
                OrFilter partial = new OrFilter();
                for (Object obj: objects) {
                    partial.or(new EqualsFilter(objectAttribute, String.valueOf(obj)));
                }
                filter.and(new EqualsFilter(OBJECT_CLASS, PERUN_GROUP)).and(partial);
            }

            LdapQuery query = query()
                    .attributes(PERUN_GROUP_ID, CN, DESCRIPTION, PERUN_UNIQUE_GROUP_NAME, PERUN_VO_ID,
                            PERUN_PARENT_GROUP_ID)
                    .searchScope(SUBTREE)
                    .filter(filter);

            ContextMapper<Group> mapper = ctx -> {
                DirContextAdapter context = (DirContextAdapter) ctx;
                if (!checkHasAttributes(context, Arrays.copyOf(query.attributes(), query.attributes().length - 1))) {
                    return null;
                }

                Long id = Long.parseLong(context.getStringAttribute(PERUN_GROUP_ID));
                String name = context.getStringAttribute(CN);
                String description = context.getStringAttribute(DESCRIPTION);
                String uniqueName = context.getStringAttribute(PERUN_UNIQUE_GROUP_NAME);
                Long voId = Long.parseLong(context.getStringAttribute(PERUN_VO_ID));
                Long parentGroupId = null;
                if (context.attributeExists(PERUN_PARENT_GROUP_ID)) {
                    parentGroupId = Long.parseLong(context.getStringAttribute(PERUN_PARENT_GROUP_ID));
                }

                return new Group(id, parentGroupId, name, description, uniqueName, voId);
            };

            result = connectorLdap.search(query, mapper);
            result = result.stream().filter(Objects::nonNull).collect(Collectors.toList());
        }

        return result;
    }

    private Set<String> getCapabilities(Long facilityId,
                                        String resourceCapabilitiesAttrName,
                                        String facilityCapabilitiesAttrIdentifier,
                                        OrFilter partialFilter)
    {
        boolean includeFacilityCapabilities = false;
        Filter filter = new AndFilter()
                .and(new EqualsFilter(OBJECT_CLASS, PERUN_RESOURCE))
                .and(partialFilter)
                .and(new EqualsFilter(PERUN_FACILITY_DN, PERUN_FACILITY_ID + '=' + facilityId + ',' + baseDn));
        String[] attributes;
        AttributeObjectMapping capabilitiesMapping;

        if (StringUtils.hasText(resourceCapabilitiesAttrName)) {
            capabilitiesMapping = this.getMappingForAttrName(resourceCapabilitiesAttrName);
            attributes = new String[] { capabilitiesMapping.getLdapName(), ASSIGNED_GROUP_ID };
        } else {
            attributes = new String[] { ASSIGNED_GROUP_ID };
            capabilitiesMapping = null;
        }

        LdapQuery query = query()
                .attributes(attributes)
                .searchScope(SUBTREE)
                .filter(filter);
        ContextMapper<Set<String>> mapper = this.capabilitiesMapper(capabilitiesMapping, query);

        List<Set<String>> foundSets = connectorLdap.search(query, mapper);

        Set<String> capabilities = new HashSet<>();
        if (foundSets != null && !foundSets.isEmpty()) {
            // if the mapper returns at least one entry, user is member of some group assigned to the facility
            includeFacilityCapabilities = true;
            foundSets.stream().filter(Objects::nonNull).forEach(capabilities::addAll);
        }

        if (StringUtils.hasText(facilityCapabilitiesAttrIdentifier) && includeFacilityCapabilities) {
            Set<String> facilityCapabilities = this.getFacilityCapabilities(facilityId, facilityCapabilitiesAttrIdentifier);
            capabilities.addAll(facilityCapabilities);
        }

        return capabilities;
    }

    private ContextMapper<Set<String>> capabilitiesMapper(AttributeObjectMapping capabilitiesMapping,
                                                          @NonNull LdapQuery query)
    {
        return ctx -> {
            DirContextAdapter context = (DirContextAdapter) ctx;
            Set<String> mappedCapabilities = new HashSet<>();
            if (checkHasAttributes(context, query.attributes())) {
                if (capabilitiesMapping != null) {
                    String[] capabilitiesAttr = context.getStringAttributes(capabilitiesMapping.getLdapName());
                    mappedCapabilities.addAll(Arrays.asList(capabilitiesAttr));
                }
            }
            return mappedCapabilities;
        };
    }

    private Set<String> getFacilityCapabilities(@NonNull Long facilityId, @NonNull String capabilitiesAttrName) {
        Set<String> result = new HashSet<>();
        PerunAttributeValue attrVal = this.getAttributeValue(FACILITY, facilityId, capabilitiesAttrName);
        if (attrVal != null && attrVal.valueAsList() != null) {
            result = new HashSet<>(attrVal.valueAsList());
        }

        return result;
    }

    private String getPrefixForEntity(@NonNull Entity entity, @NonNull Long entityId) {
        String prefix = null;
        switch (entity) {
            case USER: prefix = PERUN_USER_ID + '=' + entityId + ",ou=People"; break;
            case VO: prefix = PERUN_VO_ID + '=' + entityId; break;
            case GROUP: prefix = PERUN_GROUP_ID + '=' + entityId; break;
            case FACILITY: prefix = PERUN_FACILITY_ID + '=' + entityId; break;
            case RESOURCE: prefix = PERUN_RESOURCE_ID + '=' + entityId; break;
        }
        return prefix;
    }

    private List<Group> getGroupsByIds(@NonNull Set<Long> groupIds) {
        OrFilter groupIdsFilter = new OrFilter();
        for (Long gid: groupIds) {
            groupIdsFilter.or(new EqualsFilter(PERUN_GROUP_ID, String.valueOf(gid)));
        }

        Filter filter = new AndFilter()
                .and(new EqualsFilter(OBJECT_CLASS, PERUN_GROUP))
                .and(groupIdsFilter);

        LdapQuery query = query()
                .attributes(PERUN_GROUP_ID, CN, DESCRIPTION, PERUN_UNIQUE_GROUP_NAME, PERUN_VO_ID, PERUN_PARENT_GROUP_ID)
                .searchScope(SUBTREE)
                .filter(filter);


        ContextMapper<Group> groupMapper = this.groupMapper();

        return connectorLdap.search(query, groupMapper)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Set<Long> getGroupIdsAssignedToFacility(@NonNull Long facilityId) {
        Filter filter = new AndFilter()
                .and(new EqualsFilter(OBJECT_CLASS, PERUN_RESOURCE))
                .and(new EqualsFilter(PERUN_FACILITY_ID, String.valueOf(facilityId)));

        LdapQuery query = query()
                .attributes(ASSIGNED_GROUP_ID)
                .searchScope(SUBTREE)
                .filter(filter);

        ContextMapper<Set<Long>> mapper = ctx -> {
            DirContextAdapter context = (DirContextAdapter) ctx;
            if (!checkHasAttributes(context, query.attributes())) {
                return new HashSet<>();
            }
            Set<Long> groupIds = new HashSet<>();
            String[] assignedGroupIds = context.getStringAttributes(ASSIGNED_GROUP_ID);
            if (assignedGroupIds != null) {
                for (String id: assignedGroupIds) {
                    groupIds.add(Long.valueOf(id));
                }
            }

            return groupIds;
        };

        List<Set<Long>> result = connectorLdap.search(query, mapper);
        return flatten(result);
    }

    private boolean checkHasAttributes(DirContextAdapter ctx, String[] attributes) {
        if (ctx == null) {
            return false;
        } else if (attributes == null) {
            return true;
        }

        for (String attr : attributes) {
            if (!ctx.attributeExists(attr)) {
                return false;
            }
        }

        return true;
    }

    private Set<AttributeObjectMapping> getMappingsForAttrNames(@NonNull Collection<String> attrsToFetch) {
        return this.attributeMappingService.getMappingsByIdentifiers(attrsToFetch);
    }

    private AttributeObjectMapping getMappingForAttrName(@NonNull String attrToFetch) {
        return this.attributeMappingService.getMappingByIdentifier(attrToFetch);
    }

    private String[] getAttributesFromMappings(@NonNull Set<AttributeObjectMapping> mappings) {
        return mappings
                .stream()
                .map(AttributeObjectMapping::getLdapName)
                .distinct()
                .filter(e -> e != null && e.length() != 0)
                .collect(Collectors.toList())
                .toArray(new String[]{});
    }

    private PerunAttributeValue parseValue(DirContextAdapter context , String name, @NonNull AttributeObjectMapping mapping) {
        PerunAttrValueType type = mapping.getAttrType();
        boolean isPresent = context.attributeExists(name);

        if (!isPresent && PerunAttrValueType.BOOLEAN.equals(type)) {
            return new PerunAttributeValue(PerunAttributeValue.BOOLEAN_TYPE, jsonNodeFactory.booleanNode(false));
        } else if (!isPresent && PerunAttrValueType.ARRAY.equals(type)) {
            return new PerunAttributeValue(PerunAttributeValue.ARRAY_TYPE, jsonNodeFactory.arrayNode());
        } else if (!isPresent && PerunAttrValueType.LARGE_ARRAY.equals(type)) {
            return new PerunAttributeValue(PerunAttributeValue.LARGE_ARRAY_LIST_TYPE, jsonNodeFactory.arrayNode());
        } else if (!isPresent && PerunAttrValueType.MAP_JSON.equals(type)) {
            return new PerunAttributeValue(PerunAttributeValue.MAP_TYPE, jsonNodeFactory.objectNode());
        } else if (!isPresent && PerunAttrValueType.MAP_KEY_VALUE.equals(type)) {
            return new PerunAttributeValue(PerunAttributeValue.MAP_TYPE, jsonNodeFactory.objectNode());
        } else if (!isPresent) {
            return new PerunAttributeValue(mapping.getAttrType(), jsonNodeFactory.nullNode());
        }

        switch (type) {
            case STRING:
                return new PerunAttributeValue(PerunAttributeValue.STRING_TYPE,
                        jsonNodeFactory.textNode(context.getStringAttribute(name)));
            case LARGE_STRING:
                return new PerunAttributeValue(PerunAttributeValue.LARGE_STRING_TYPE,
                        jsonNodeFactory.textNode(context.getStringAttribute(name)));
            case INTEGER:
                return new PerunAttributeValue(PerunAttributeValue.INTEGER_TYPE,
                        jsonNodeFactory.numberNode(Long.parseLong(context.getStringAttribute(name))));
            case BOOLEAN:
                return new PerunAttributeValue(PerunAttributeValue.BOOLEAN_TYPE,
                        jsonNodeFactory.booleanNode(Boolean.parseBoolean(context.getStringAttribute(name))));
            case ARRAY:
                return new PerunAttributeValue(PerunAttributeValue.ARRAY_TYPE,
                        this.getArrNode(context.getStringAttributes(name)));
            case LARGE_ARRAY:
                return new PerunAttributeValue(PerunAttributeValue.LARGE_ARRAY_LIST_TYPE,
                        this.getArrNode(context.getStringAttributes(name)));
            case MAP_JSON:
                return new PerunAttributeValue(PerunAttributeValue.MAP_TYPE,
                        this.getMapNodeJson(context.getStringAttribute(name)));
            case MAP_KEY_VALUE:
                return new PerunAttributeValue(PerunAttributeValue.MAP_TYPE,
                        getMapNodeSeparator(context.getStringAttributes(name), mapping.getSeparator()));
            default:
                throw new IllegalArgumentException("unrecognized type");
        }

    }

    private ObjectNode getMapNodeSeparator(@NonNull String[] values, @NonNull String separator) {
        ObjectNode objectNode = jsonNodeFactory.objectNode();
        for (String val: values) {
            if (val != null) {
                String[] parts = val.split(separator, 2);
                objectNode.put(parts[0], parts[1]);
            }
        }
        return objectNode;
    }

    private ObjectNode getMapNodeJson(@NonNull String value) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(value, ObjectNode.class);
        } catch (IOException e) {
            throw new InconvertibleValueException("Could not parse value");
        }
    }

    private ArrayNode getArrNode(@NonNull String[] values) {
        ArrayNode arrayNode = jsonNodeFactory.arrayNode(values.length);
        for (String val: values) {
            arrayNode.add(val);
        }
        return arrayNode;
    }

    private Vo getVo(Filter filter) {
        LdapQuery query = query()
                .attributes(PERUN_VO_ID, O, DESCRIPTION)
                .searchScope(ONELEVEL)
                .filter(filter);

        ContextMapper<Vo> mapper = this.voMapper(query.attributes());

        return connectorLdap.searchForObject(query, mapper);
    }

    // mappers

    private ContextMapper<Map<String, PerunAttributeValue>> attrsValuesMapper(@NonNull Set<AttributeObjectMapping> attrMappings) {
        return ctx -> {
            DirContextAdapter context = (DirContextAdapter) ctx;
            Map<String, PerunAttributeValue> resultMap = new LinkedHashMap<>();

            for (AttributeObjectMapping mapping : attrMappings) {
                if (mapping.getLdapName() == null || mapping.getLdapName().isEmpty()) {
                    continue;
                }
                String ldapAttrName = mapping.getLdapName();
                PerunAttributeValue value = this.parseValue(context, ldapAttrName, mapping);
                resultMap.put(mapping.getIdentifier(), value);
            }

            return resultMap;
        };
    }

    private ContextMapper<PerunAttributeValue> attrValueMapper(@NonNull AttributeObjectMapping mapping) {
        return ctx -> {
            DirContextAdapter context = (DirContextAdapter) ctx;
            if (mapping.getLdapName() == null || mapping.getLdapName().isEmpty()) {
                return null;
            }
            String ldapAttrName = mapping.getLdapName();
            return this.parseValue(context, ldapAttrName, mapping);
        };
    }

    private ContextMapper<Vo> voMapper(@NonNull String[] attributes) {
        return ctx -> {
            DirContextAdapter context = (DirContextAdapter) ctx;
            if (!checkHasAttributes(context, attributes)) {
                return null;
            }

            Long id = Long.valueOf(context.getStringAttribute(PERUN_VO_ID));
            String voShortName = context.getStringAttribute(O);
            String name = context.getStringAttribute(DESCRIPTION);

            return new Vo(id, name, voShortName);
        };
    }

    private ContextMapper<Group> groupMapper() {
        return ctx -> {
            DirContextAdapter context = (DirContextAdapter) ctx;
            if (!checkHasAttributes(context, PERUN_GROUP_REQUIRED_ATTRIBUTES)) {
                return null;
            }

            Long id = Long.parseLong(context.getStringAttribute(PERUN_GROUP_ID));
            String name = context.getStringAttribute(CN);
            String description = context.getStringAttribute(DESCRIPTION);
            String uniqueName = context.getStringAttribute(PERUN_UNIQUE_GROUP_NAME);
            Long groupVoId = Long.valueOf(context.getStringAttribute(PERUN_VO_ID));
            Long parentGroupId = null;
            if (context.getStringAttribute(PERUN_PARENT_GROUP_ID) != null) {
                parentGroupId = Long.valueOf(context.getStringAttribute(PERUN_PARENT_GROUP_ID));
            }

            return new Group(id, parentGroupId, name, description, uniqueName, groupVoId);
        };
    }

    private ContextMapper<Facility> facilityMapper() {
        return ctx -> {
            DirContextAdapter context = (DirContextAdapter) ctx;
            if (!checkHasAttributes(context, PERUN_FACILITY_REQUIRED_ATTRIBUTES)) {
                return null;
            }

            Long id = Long.parseLong(context.getStringAttribute(PERUN_FACILITY_ID));
            String facilityName = context.getStringAttribute(CN);
            String description = "";
            if (context.attributeExists(DESCRIPTION)) {
                description = context.getStringAttribute(DESCRIPTION);
            }
            return new Facility(id, facilityName, description);
        };
    }

    private <T> Set<T> flatten(@NonNull List<Set<T>> sets) {
        Set<T> flatSet = new HashSet<>();
        sets.forEach(flatSet::addAll);
        return flatSet;
    }

    private User getUser(Filter filter) {

        LdapQuery query = query().base("ou=People")
                .attributes(PERUN_USER_ID, GIVEN_NAME, SN)
                .filter(filter);

        ContextMapper<User> mapper = ctx -> {
            DirContextAdapter context = (DirContextAdapter) ctx;

            if (!checkHasAttributes(context, new String[]{PERUN_USER_ID, SN})) {
                log.warn("Not all required attributes were found, returning null");
                return null;
            }

            Long id = Long.parseLong(context.getStringAttribute(PERUN_USER_ID));
            String firstName = context.attributeExists(GIVEN_NAME) ? context.getStringAttribute(GIVEN_NAME) : "";
            String lastName = context.getStringAttribute(SN);
            return new User(id, firstName, lastName);
        };

        return connectorLdap.searchForObject(query, mapper);
    }

}
