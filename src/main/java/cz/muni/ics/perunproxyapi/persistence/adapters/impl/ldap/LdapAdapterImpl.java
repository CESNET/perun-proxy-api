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
import cz.muni.ics.perunproxyapi.persistence.enums.AttributeType;
import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
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
import static cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue.INTEGER_TYPE;
import static cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue.LARGE_STRING_TYPE;
import static cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue.MAP_TYPE;
import static cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue.STRING_TYPE;
import static cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValueAwareModel.ARRAY_TYPE;
import static cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValueAwareModel.BOOLEAN_TYPE;
import static cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValueAwareModel.LARGE_ARRAY_LIST_TYPE;
import static org.springframework.ldap.query.LdapQueryBuilder.query;
import static org.springframework.ldap.query.SearchScope.ONELEVEL;
import static org.springframework.ldap.query.SearchScope.SUBTREE;


@Component("ldapAdapter")
@Slf4j
public class LdapAdapterImpl implements DataAdapter {

    public static final String OU_PEOPLE = "ou=People";

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
    private final String[] PERUN_GROUP_REQUIRED_ATTRIBUTES = new String[]{ PERUN_GROUP_ID, CN, PERUN_UNIQUE_GROUP_NAME,
            PERUN_VO_ID, DESCRIPTION };
    private final String[] PERUN_VO_REQUIRED_ATTRIBUTES = new String[] { PERUN_VO_ID, O, DESCRIPTION };
    private final String[] PERUN_FACILITY_REQUIRED_ATTRIBUTES;
    private final String[] PERUN_USER_REQUIRED_ATTRIBUTES;

    // BEAN_ATTRS
    private final String[] PERUN_GROUP_BEAN_ATTRIBUTES = new String[]{ PERUN_GROUP_ID, CN, PERUN_UNIQUE_GROUP_NAME,
            PERUN_VO_ID, DESCRIPTION, PERUN_PARENT_GROUP_ID};
    private final String[] PERUN_VO_BEAN_ATTRIBUTES = new String[] { PERUN_VO_ID, O, DESCRIPTION };
    private final String[] PERUN_FACILITY_BEAN_ATTRIBUTES;
    private final String[] PERUN_USER_BEAN_ATTRIBUTES;

    @NonNull private final JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
    @NonNull private final AttributeMappingService attributeMappingService;
    @NonNull private final PerunConnectorLdap connectorLdap;
    @NonNull private final String baseDn;

    @NonNull private final String rpIdentifierAttr;
    @NonNull private final String additionalIdentifiersAttr;
    @NonNull private final String loginAttr;

    @Autowired
    public LdapAdapterImpl(PerunConnectorLdap connectorLdap,
                           AttributeMappingService attributeMappingService,
                           LdapProperties ldapProperties,
                           @Value("${attributes.identifiers.relying_party}") String rpIdentifierAttrIdentifier,
                           @Value("${attributes.identifiers.additional_identifiers}") String additionalIdentifiersAttrIdentifier,
                           @Value("${attributes.identifiers.login}") String loginAttrIdentifier)
    {
        this.connectorLdap = connectorLdap;
        this.attributeMappingService = attributeMappingService;
        this.baseDn = ldapProperties.getBaseDn();

        this.rpIdentifierAttr = AdapterUtils.getRequiredLdapNameFromMapping(
                this.attributeMappingService.getMappingByIdentifier(rpIdentifierAttrIdentifier));
        this.additionalIdentifiersAttr = AdapterUtils.getRequiredLdapNameFromMapping(
                this.attributeMappingService.getMappingByIdentifier(additionalIdentifiersAttrIdentifier));
        this.loginAttr = AdapterUtils.getRequiredLdapNameFromMapping(
                this.attributeMappingService.getMappingByIdentifier(loginAttrIdentifier));

        this.PERUN_USER_REQUIRED_ATTRIBUTES = new String[] { PERUN_USER_ID, SN, loginAttr };
        this.PERUN_USER_BEAN_ATTRIBUTES = new String[] { PERUN_USER_ID, GIVEN_NAME, SN, loginAttr };
        this.PERUN_FACILITY_BEAN_ATTRIBUTES = new String[] { PERUN_FACILITY_ID, CN, DESCRIPTION, rpIdentifierAttr };
        this.PERUN_FACILITY_REQUIRED_ATTRIBUTES = new String[] { PERUN_FACILITY_ID, CN, rpIdentifierAttr };
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
    public User findPerunUserById(@NonNull Long userId, List<String> attrIdentifiers) {
        final Set<AttributeObjectMapping> finalMappings = this.getAttributeMappings(attrIdentifiers);
        ContextMapper<User> mapper = this.userWithAttributesMapper(finalMappings);

        Set<String> attributes = new HashSet<>();
        attributes.addAll(Arrays.asList(PERUN_USER_REQUIRED_ATTRIBUTES));
        attributes.addAll(Arrays.asList(this.getAttributesFromMappings(finalMappings)));

        String dn = this.constructUserDn(userId, null);
        try {
            return connectorLdap.lookup(dn, attributes.toArray(new String[] {}), mapper);
        } catch (LookupException e) {
            log.debug("Caught LookupException, returning NULL", e);
            return null;
        }
    }

    @Override
    public List<Group> getUserGroups(@NonNull Long userId) {
        String uniqueMember = this.constructUserDn(userId, baseDn);
        Filter filter = new AndFilter()
                .and(new EqualsFilter(OBJECT_CLASS, PERUN_GROUP))
                .and(new EqualsFilter(UNIQUE_MEMBER, uniqueMember));
        return getGroups(filter);
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
        List<Group> groups = this.getGroupsByIds(groupIds);
        if (groups != null) {
            return groups.stream().filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public Group getGroupByName(@NonNull Long voId, @NonNull String groupName) {
        Filter filter = new AndFilter()
                .and(new EqualsFilter(OBJECT_CLASS, PERUN_GROUP))
                .and(new EqualsFilter(PERUN_UNIQUE_GROUP_NAME, groupName));

        LdapQuery query = query()
                .attributes(PERUN_GROUP_BEAN_ATTRIBUTES)
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
                .attributes(PERUN_FACILITY_BEAN_ATTRIBUTES)
                .filter(filter);

        ContextMapper<Facility> mapper = this.facilityMapper();

        List<Facility> facilities = connectorLdap.search(query, mapper);
        if (facilities != null) {
            return facilities.stream().filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public Facility getFacilityByRpIdentifier(@NonNull String rpIdentifier)
    {
        Filter filter = new AndFilter()
                .and(new EqualsFilter(OBJECT_CLASS, PERUN_FACILITY))
                .and(new EqualsFilter(rpIdentifierAttr, rpIdentifier));
        LdapQuery query = query()
                .attributes(PERUN_FACILITY_BEAN_ATTRIBUTES)
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

        String uniqueMember = this.constructUserDn(userId, baseDn);
        AndFilter filter = new AndFilter()
                .and(new EqualsFilter(OBJECT_CLASS, PERUN_GROUP))
                .and(new EqualsFilter(UNIQUE_MEMBER, uniqueMember))
                .and(resourceIdsFilter);

        return getGroups(filter);
    }

    @Override
    public User getUserWithAttributesByLogin(@NonNull String login,
                                             @NonNull List<String> attrIdentifiers)
    {
        final Set<AttributeObjectMapping> finalMappings = this.getAttributeMappings(attrIdentifiers);
        Filter filter = new AndFilter()
                .and(new EqualsFilter(OBJECT_CLASS, PERUN_USER))
                .and(new EqualsFilter(loginAttr, login));
        LdapQuery query  = query()
                .base(OU_PEOPLE)
                .searchScope(ONELEVEL)
                .attributes(this.constructUserAttributes(finalMappings))
                .filter(filter);
        ContextMapper<User> mapper = this.userWithAttributesMapper(finalMappings);

        User user = connectorLdap.searchForObject(query, mapper);
        if (user != null) {
            user.setLogin(login);
        }

        return user;
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

    @Override
    public User findByIdentifiers(@NonNull String idpIdentifier,
                                  @NonNull List<String> identifiers,
                                  @NonNull List<String> attrIdentifiers)
    {
        Set<AttributeObjectMapping> mappings = this.getAttributeMappings(attrIdentifiers);
        OrFilter identifiersFilter = new OrFilter();
        for (String identifier : identifiers) {
            if (StringUtils.hasText(identifier)) {
                identifiersFilter.or(new EqualsFilter(additionalIdentifiersAttr, identifier));
            }
        }
        AndFilter filter = new AndFilter()
                .and(identifiersFilter)
                .and(new EqualsFilter(OBJECT_CLASS,PERUN_USER));

        LdapQuery ldapQuery = query()
                .base(OU_PEOPLE)
                .attributes(this.constructUserAttributes(mappings))
                .searchScope(ONELEVEL)
                .filter(filter);

        ContextMapper<User> mapper = this.userWithAttributesMapper(mappings);
        return connectorLdap.searchForObject(ldapQuery, mapper);
    }

    // private methods

    private List<Group> getGroupsByUniqueGroupNames(Set<String> groupNames) {
        List<Group> groups = this.getGroups(groupNames, PERUN_UNIQUE_GROUP_NAME);
        if (groups != null) {
            return groups.stream().filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    private List<Group> getGroups(Collection<?> objects, @NonNull String objectAttribute) {
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

            return this.getGroups(filter);
        }
    }

    private Set<String> getCapabilities(@NonNull Long facilityId,
                                        String resourceCapabilitiesAttrName,
                                        String facilityCapabilitiesAttrIdentifier,
                                        @NonNull OrFilter partialFilter)
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
            Set<String> facilityCapabilities = this.getFacilityCapabilities(facilityId,
                    facilityCapabilitiesAttrIdentifier);
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
            case USER: prefix = this.constructUserDn(entityId, null); break;
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

        return this.getGroups(filter);
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
        if (result != null) {
            return this.flatten(result);
        } else {
            return new HashSet<>();
        }
    }

    private List<Group> getGroups(@NonNull Filter filter) {
        LdapQuery query = query()
                .attributes(PERUN_GROUP_ID, CN, PERUN_UNIQUE_GROUP_NAME, PERUN_VO_ID, DESCRIPTION, PERUN_PARENT_GROUP_ID)
                .searchScope(SUBTREE)
                .filter(filter);
        ContextMapper<Group> mapper = this.groupMapper();
        List<Group> groups = connectorLdap.search(query, mapper);
        if (groups != null) {
            return groups.stream().filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
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

    private PerunAttributeValue parseValue(@NonNull DirContextAdapter context, @NonNull String name,
                                           @NonNull AttributeObjectMapping mapping)
    {
        AttributeType type = mapping.getAttrType();
        boolean isPresent = context.attributeExists(name);

        if (!isPresent && AttributeType.BOOLEAN.equals(type)) {
            return new PerunAttributeValue(mapping.getIdentifier(), BOOLEAN_TYPE, jsonNodeFactory.booleanNode(false));
        } else if (!isPresent && AttributeType.ARRAY.equals(type)) {
            return new PerunAttributeValue(mapping.getIdentifier(), ARRAY_TYPE, jsonNodeFactory.arrayNode());
        } else if (!isPresent && AttributeType.LARGE_ARRAY.equals(type)) {
            return new PerunAttributeValue(mapping.getIdentifier(), LARGE_ARRAY_LIST_TYPE, jsonNodeFactory.arrayNode());
        } else if (!isPresent && AttributeType.MAP_JSON.equals(type)) {
            return new PerunAttributeValue(mapping.getIdentifier(), MAP_TYPE, jsonNodeFactory.objectNode());
        } else if (!isPresent && AttributeType.MAP_KEY_VALUE.equals(type)) {
            return new PerunAttributeValue(mapping.getIdentifier(), MAP_TYPE, jsonNodeFactory.objectNode());
        } else if (!isPresent) {
            return new PerunAttributeValue(mapping.getIdentifier(), type, jsonNodeFactory.nullNode());
        }

        switch (type) {
            case STRING:
                return new PerunAttributeValue(mapping.getIdentifier(), STRING_TYPE,
                        jsonNodeFactory.textNode(context.getStringAttribute(name)));
            case LARGE_STRING:
                return new PerunAttributeValue(mapping.getIdentifier(), LARGE_STRING_TYPE,
                        jsonNodeFactory.textNode(context.getStringAttribute(name)));
            case INTEGER:
                return new PerunAttributeValue(mapping.getIdentifier(), INTEGER_TYPE,
                        jsonNodeFactory.numberNode(Long.parseLong(context.getStringAttribute(name))));
            case BOOLEAN:
                return new PerunAttributeValue(mapping.getIdentifier(), BOOLEAN_TYPE,
                        jsonNodeFactory.booleanNode(Boolean.parseBoolean(context.getStringAttribute(name))));
            case ARRAY:
                return new PerunAttributeValue(mapping.getIdentifier(), ARRAY_TYPE,
                        this.getArrNode(context.getStringAttributes(name)));
            case LARGE_ARRAY:
                return new PerunAttributeValue(mapping.getIdentifier(), LARGE_ARRAY_LIST_TYPE,
                        this.getArrNode(context.getStringAttributes(name)));
            case MAP_JSON:
                return new PerunAttributeValue(mapping.getIdentifier(), MAP_TYPE,
                        this.getMapNodeJson(context.getStringAttribute(name)));
            case MAP_KEY_VALUE:
                return new PerunAttributeValue(mapping.getIdentifier(), MAP_TYPE,
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
                .attributes(PERUN_VO_BEAN_ATTRIBUTES)
                .searchScope(ONELEVEL)
                .filter(filter);

        ContextMapper<Vo> mapper = this.voMapper();

        return connectorLdap.searchForObject(query, mapper);
    }

    private <T> Set<T> flatten(@NonNull List<Set<T>> sets) {
        Set<T> flatSet = new HashSet<>();
        sets.forEach(flatSet::addAll);
        return flatSet;
    }

    private User getUser(Filter filter) {
        LdapQuery query = query().base(OU_PEOPLE)
                .attributes(PERUN_USER_BEAN_ATTRIBUTES)
                .filter(filter);
        ContextMapper<User> mapper = this.userMapper();
        return connectorLdap.searchForObject(query, mapper);
    }

    private String[] constructUserAttributes(Set<AttributeObjectMapping> attrMappings) {
        String[] attrs;
        int i = 0;
        int additionalAttrsCnt = 4;
        if (attrMappings == null) {
            attrs = new String[additionalAttrsCnt];
        } else {
            attrs = new String[attrMappings.size() + additionalAttrsCnt];
            for (AttributeObjectMapping mapping: attrMappings) {
                attrs[i++] = mapping.getLdapName();
            }
        }

        attrs[i++] = PERUN_USER_ID;
        attrs[i++] = GIVEN_NAME;
        attrs[i++] = SN;
        attrs[i] = loginAttr;

        return attrs;
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

    // mappers

    private ContextMapper<Map<String, PerunAttributeValue>> attrsValuesMapper(
            @NonNull Set<AttributeObjectMapping> attrMappings)
    {
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

    private ContextMapper<Vo> voMapper() {
        return ctx -> {
            DirContextAdapter context = (DirContextAdapter) ctx;
            if (!checkHasAttributes(context, PERUN_VO_REQUIRED_ATTRIBUTES)) {
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
            String uniqueName = context.getStringAttribute(PERUN_UNIQUE_GROUP_NAME);
            Long groupVoId = Long.valueOf(context.getStringAttribute(PERUN_VO_ID));
            Long parentGroupId = null;
            String description = "";
            if (context.attributeExists(DESCRIPTION)) {
                description = context.getStringAttribute(DESCRIPTION);
            }
            if (context.attributeExists(PERUN_PARENT_GROUP_ID)) {
                String parentGroupIdStr = context.getStringAttribute(PERUN_PARENT_GROUP_ID);
                if (StringUtils.hasText(parentGroupIdStr)) {
                    parentGroupId = Long.valueOf(parentGroupIdStr);
                }
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
            String rpIdentifier = context.getStringAttribute(rpIdentifierAttr);
            return new Facility(id, facilityName, description, rpIdentifier);
        };
    }

    private ContextMapper<User> userMapper() {
        return this.userWithAttributesMapper(new HashSet<>());
    }

    private ContextMapper<User> userWithAttributesMapper(final Set<AttributeObjectMapping> attrMappings) {
        return ctx -> {
            DirContextAdapter context = (DirContextAdapter) ctx;
            if (!checkHasAttributes(context, PERUN_USER_REQUIRED_ATTRIBUTES)) {
                return null;
            }

            Map<String, PerunAttributeValue> attributes = new HashMap<>();
            if (attrMappings != null) {
                for (AttributeObjectMapping mapping : attrMappings) {
                    PerunAttributeValue value = this.parseValue(context, mapping.getLdapName(), mapping);
                    attributes.put(mapping.getIdentifier(), value);
                }
            }

            Long userId = Long.parseLong(context.getStringAttribute(PERUN_USER_ID));
            String firstName = context.attributeExists(GIVEN_NAME) ? context.getStringAttribute(GIVEN_NAME) : "";
            String lastName = context.getStringAttribute(SN);
            String login = context.getStringAttribute(loginAttr);

            return new User(userId, firstName, lastName, login, attributes);
        };
    }

    private Set<AttributeObjectMapping> getAttributeMappings(List<String> attrIdentifiers) {
        Set<AttributeObjectMapping> attrMappings = this.getMappingsForAttrNames(attrIdentifiers);
        if (attrMappings == null) {
            attrMappings = new HashSet<>();
        }

        return attrMappings.stream()
                .filter(mapping -> {
                    if (!StringUtils.hasText(mapping.getLdapName())) {
                        log.warn("No LDAP name found in mapping {}", mapping);
                        return false;
                    }
                    return true;
                }).collect(Collectors.toSet());
    }

    private String constructUserDn(@NonNull Long userId, String baseDn) {
        String dn = PERUN_USER_ID + '=' + userId + ',' + OU_PEOPLE;
        if (baseDn != null) {
            dn += (',' + baseDn);
        }
        return dn;
    }

}
