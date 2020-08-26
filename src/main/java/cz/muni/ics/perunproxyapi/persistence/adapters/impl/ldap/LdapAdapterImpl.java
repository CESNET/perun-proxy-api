package cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.muni.ics.perunproxyapi.persistence.AttributeMappingService;
import cz.muni.ics.perunproxyapi.persistence.adapters.DataAdapter;
import cz.muni.ics.perunproxyapi.persistence.connectors.PerunConnectorLdap;
import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
import cz.muni.ics.perunproxyapi.persistence.enums.PerunAttrValueType;
import cz.muni.ics.perunproxyapi.persistence.exceptions.InconvertibleValueException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.LookupException;
import cz.muni.ics.perunproxyapi.persistence.models.AttributeObjectMapping;
import cz.muni.ics.perunproxyapi.persistence.models.Facility;
import cz.muni.ics.perunproxyapi.persistence.models.Group;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttribute;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import cz.muni.ics.perunproxyapi.persistence.models.Vo;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.filter.OrFilter;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    // FACILITY
    public static final String PERUN_FACILITY = "perunFacility";
    public static final String PERUN_FACILITY_ID = "perunFacilityId";
    public static final String ASSIGNED_GROUP_ID = "assignedGroupId";
    public static final String ENTITY_ID = "entityID";

    private final JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
    private final AttributeMappingService attributeMappingService;
    private final PerunConnectorLdap connectorLdap;

    @Autowired
    public LdapAdapterImpl(@NonNull PerunConnectorLdap connectorLdap,
                           @NonNull AttributeMappingService attributeMappingService) {
        this.connectorLdap = connectorLdap;
        this.attributeMappingService = attributeMappingService;
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
    public List<Group> getUserGroupsInVo(@NonNull Long userId, @NonNull Long voId) {
        Set<Long> groupIds = this.getUserGroupIds(userId, voId);
        return this.getGroupsByIds(groupIds);
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
                .attributes(PERUN_GROUP_ID, CN, PERUN_UNIQUE_GROUP_NAME, PERUN_VO_ID, DESCRIPTION)
                .searchScope(ONELEVEL)
                .filter(filter);

        ContextMapper<Group> mapper = this.groupMapper(query.attributes());

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
            ContextMapper<Map<String, PerunAttributeValue>> mapper = attrValueMapper(mappings);
            String prefix = null;
            switch (entity) {
                case USER: prefix = PERUN_USER_ID + '=' + entityId + ",ou=People"; break;
                case VO: prefix = PERUN_VO_ID + '=' + entityId; break;
                case GROUP: prefix = PERUN_GROUP_ID + '=' + entityId; break;
                case FACILITY: prefix = PERUN_FACILITY_ID + '=' + entityId; break;
                case RESOURCE: prefix = PERUN_RESOURCE_ID + '=' + entityId; break;
            }

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
    public List<Facility> getFacilitiesByAttribute(@NonNull String attributeName, @NonNull String attrValue) {
        Filter filter = new AndFilter()
                .and(new EqualsFilter(OBJECT_CLASS, PERUN_FACILITY))
                .and(new EqualsFilter(attributeName, attrValue));
        LdapQuery query = query()
                .searchScope(ONELEVEL)
                .attributes(PERUN_FACILITY_ID, DESCRIPTION, CN)
                .filter(filter);

        ContextMapper<Facility> mapper = ctx -> {
            DirContextAdapter context = (DirContextAdapter) ctx;
            if (!checkHasAttributes(context, query.attributes())) {
                return null;
            }

            Long id = Long.parseLong(context.getStringAttribute(PERUN_FACILITY_ID));
            String facilityName = context.getStringAttribute(CN);
            String description = context.getStringAttribute(DESCRIPTION);
            return new Facility(id, facilityName, description);
        };

        return connectorLdap.search(query, mapper)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> getUsersGroupsOnFacility(@NonNull Long facilityId, @NonNull Long userId) {
        Set<Long> groupIdsOnFacility = this.getGroupIdsAssignedToFacility(facilityId);
        Set<Long> groupIdsOfUser = this.getUserGroupIds(userId, null);

        Set<Long> intersection = new HashSet<>(groupIdsOfUser);
        intersection.retainAll(groupIdsOnFacility);

        return this.getGroupsByIds(intersection);
    }

    @Override
    public List<Facility> searchFacilitiesByAttributeValue(@NonNull PerunAttribute attribute) {
        return new ArrayList<>(); //TODO: cannot be implemented
    }

    private Set<Long> getUserGroupIds(@NonNull Long userId, Long voId) {
        Filter filter = new AndFilter()
                .and(new EqualsFilter(OBJECT_CLASS, PERUN_USER))
                .and(new EqualsFilter(PERUN_USER_ID, String.valueOf(userId)));

        LdapQuery query = query()
                .attributes(MEMBER_OF)
                .filter(filter);

        ContextMapper<Set<Long>> mapper = ctx -> {
            DirContextAdapter context = (DirContextAdapter) ctx;
            Set<Long> ids = new HashSet<>();
            if (checkHasAttributes(context, query.attributes())) {
                String[] values = context.getStringAttributes(MEMBER_OF);
                for (String id: values) {
                    String[] parts = id.split(",", 3);

                    String groupId = parts[0];
                    groupId = groupId.replace(PERUN_GROUP_ID + '=', "");

                    String voIdStr = parts[1];
                    voIdStr = voIdStr.replace(PERUN_VO_ID + '=', "");

                    if (voId == null || voId.equals(Long.valueOf(voIdStr))) {
                        ids.add(Long.valueOf(groupId));
                    }
                }
            }

            return ids;
        };

        List<Set<Long>> result = connectorLdap.search(query, mapper);
        return flatten(result);
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


        ContextMapper<Group> groupMapper = this.groupMapper(query.attributes());

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
                .searchScope(ONELEVEL)
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

    private AttributeObjectMapping getMappingForAttrNames(@NonNull String attrToFetch) {
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
        //MAP_KEY_VALUE deleted for incompatibility with AttributeMappingService
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

    private ContextMapper<Map<String, PerunAttributeValue>> attrValueMapper(@NonNull Set<AttributeObjectMapping> attrMappings) {
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

    private ContextMapper<Group> groupMapper(@NonNull String[] attributes) {
        return ctx -> {
            DirContextAdapter context = (DirContextAdapter) ctx;
            if (!checkHasAttributes(context, attributes)) {
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
