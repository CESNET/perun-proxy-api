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
import cz.muni.ics.perunproxyapi.persistence.models.AttributeObjectMapping;
import cz.muni.ics.perunproxyapi.persistence.models.Facility;
import cz.muni.ics.perunproxyapi.persistence.models.Group;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttribute;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import cz.muni.ics.perunproxyapi.persistence.models.Vo;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.ldap.client.api.search.FilterBuilder;
import org.apache.directory.ldap.client.template.EntryMapper;
import org.springframework.beans.factory.annotation.Autowired;
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

import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.PerunAdapterLdapConstants.ASSIGNED_GROUP_ID;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.PerunAdapterLdapConstants.CN;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.PerunAdapterLdapConstants.DESCRIPTION;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.PerunAdapterLdapConstants.EDU_PERSON_PRINCIPAL_NAMES;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.PerunAdapterLdapConstants.ENTITY_ID;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.PerunAdapterLdapConstants.GIVEN_NAME;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.PerunAdapterLdapConstants.MEMBER_OF;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.PerunAdapterLdapConstants.O;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.PerunAdapterLdapConstants.OBJECT_CLASS;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.PerunAdapterLdapConstants.PERUN_FACILITY;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.PerunAdapterLdapConstants.PERUN_FACILITY_ID;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.PerunAdapterLdapConstants.PERUN_GROUP;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.PerunAdapterLdapConstants.PERUN_GROUP_ID;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.PerunAdapterLdapConstants.PERUN_PARENT_GROUP_ID;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.PerunAdapterLdapConstants.PERUN_RESOURCE;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.PerunAdapterLdapConstants.PERUN_UNIQUE_GROUP_NAME;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.PerunAdapterLdapConstants.PERUN_USER;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.PerunAdapterLdapConstants.PERUN_USER_ID;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.PerunAdapterLdapConstants.PERUN_VO;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.PerunAdapterLdapConstants.PERUN_VO_ID;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.PerunAdapterLdapConstants.SN;
import static org.apache.directory.ldap.client.api.search.FilterBuilder.and;
import static org.apache.directory.ldap.client.api.search.FilterBuilder.equal;
import static org.apache.directory.ldap.client.api.search.FilterBuilder.or;

@Component("ldapAdapter")
@Slf4j
public class LdapAdapterImpl implements DataAdapter {

    private final PerunConnectorLdap connectorLdap;
    private final AttributeMappingService attributeMappingService;
    private final JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;

    @Autowired
    public LdapAdapterImpl(@NonNull PerunConnectorLdap connectorLdap,
                           @NonNull AttributeMappingService attributeMappingService) {
        this.connectorLdap = connectorLdap;
        this.attributeMappingService = attributeMappingService;
    }

    @Override
    public User getPerunUser(@NonNull String idpEntityId, @NonNull List<String> uids) {
        String dnPrefix = "ou=People";

        FilterBuilder[] filterUids = new FilterBuilder[uids.size()];
        int i = 0;
        for (String uid : uids) {
            filterUids[i++] = equal(EDU_PERSON_PRINCIPAL_NAMES, uid);
        }

        FilterBuilder filter = and(equal(OBJECT_CLASS, PERUN_USER), or(filterUids));
        String[] attributes = new String[] { PERUN_USER_ID, GIVEN_NAME, SN };
        EntryMapper<User> mapper = e -> {
            if (!checkHasAttributes(e, new String[] { PERUN_USER_ID, SN })) {
                return null;
            }

            Long id = Long.parseLong(e.get(PERUN_USER_ID).getString());
            String firstName = (e.get(GIVEN_NAME) != null) ? e.get(GIVEN_NAME).getString() : null;
            String lastName = e.get(SN).getString();
            return new User(id, firstName, lastName);
        };

        return connectorLdap.searchFirst(dnPrefix, filter, SearchScope.ONELEVEL, attributes, mapper);
    }

    @Override
    public List<Group> getUserGroupsInVo(@NonNull Long userId, @NonNull Long voId) {
        Set<Long> groupIds = this.getUserGroupIds(userId, voId);
        return this.getGroupsByIds(groupIds);
    }

    @Override
    public List<Group> getSpGroups(@NonNull String spIdentifier) {
        List<Facility> facilities = getFacilitiesByAttribute(ENTITY_ID, spIdentifier);
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
        String[] attributes = new String[]{ PERUN_GROUP_ID, CN, PERUN_UNIQUE_GROUP_NAME, PERUN_VO_ID, DESCRIPTION };
        FilterBuilder groupFilter = and(
                equal(OBJECT_CLASS, PERUN_GROUP),
                equal(PERUN_UNIQUE_GROUP_NAME, groupName)
        );
        EntryMapper<Group> groupMapper = this.groupMapper(attributes);

        return connectorLdap.searchFirst(null, groupFilter, SearchScope.ONELEVEL, attributes, groupMapper);
    }

    @Override
    public Vo getVoByShortName(@NonNull String shortName) {
        String[] attributes = new String[] { PERUN_VO_ID, O, DESCRIPTION };
        FilterBuilder filter = and(equal(OBJECT_CLASS, PERUN_VO), equal(O, shortName));
        EntryMapper<Vo> mapper = this.voMapper(attributes);

        return connectorLdap.searchFirst(null, filter, SearchScope.ONELEVEL, attributes, mapper);
    }

    @Override
    public Vo getVoById(@NonNull Long id) {
        String[] attributes = new String[] { PERUN_VO_ID, O, DESCRIPTION };
        FilterBuilder filter = and(
                equal(OBJECT_CLASS, PERUN_VO),
                equal(PERUN_VO_ID, String.valueOf(id))
        );

        EntryMapper<Vo> mapper = this.voMapper(attributes);

        return connectorLdap.searchFirst(null, filter, SearchScope.ONELEVEL, attributes, mapper);
    }

    @Override
    public Map<String, PerunAttributeValue> getAttributesValues(@NonNull Entity entity, @NonNull Long entityId,
                                                                @NonNull List<String> attrs) {
        Map<String, PerunAttributeValue> resultMap = new HashMap<>();

        Set<AttributeObjectMapping> mappings = getMappingsForAttrNames(attrs);
        String[] attributes = getAttributesFromMappings(mappings);
        if (attributes.length != 0) {
            EntryMapper<Map<String, PerunAttributeValue>> mapper = attrValueMapper(mappings);
            resultMap = this.connectorLdap.lookup(null, attributes, mapper);
        }

        return resultMap;
    }

    @Override
    public List<Facility> getFacilitiesByAttribute(@NonNull String attributeName, @NonNull String attrValue) {
        String[] attributes = new String[] { PERUN_FACILITY_ID, DESCRIPTION, CN };
        EntryMapper<Facility> mapper = e -> {
            if (!checkHasAttributes(e, attributes)) {
                return null;
            }

            Long id = Long.parseLong(e.get(PERUN_FACILITY_ID).getString());
            String facilityName = e.get(CN).getString();
            String description = e.get(DESCRIPTION).getString();
            return new Facility(id, facilityName, description);
        };

        FilterBuilder filter = and(equal(OBJECT_CLASS, PERUN_FACILITY), equal(attributeName, attrValue));
        return connectorLdap.search(null, filter, SearchScope.ONELEVEL, attributes, mapper)
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
        String[] attributes = new String[]{MEMBER_OF};
        FilterBuilder filter = and(
                equal(OBJECT_CLASS, PERUN_USER),
                equal(PERUN_USER_ID, String.valueOf(userId))
        );

        EntryMapper<Set<Long>> mapper = e -> {
            Set<Long> ids = new HashSet<>();
            if (checkHasAttributes(e, attributes)) {
                Attribute a = e.get(MEMBER_OF);
                a.iterator().forEachRemaining(id -> {
                    String fullVal = id.getString();
                    String[] parts = fullVal.split(",", 3);

                    String groupId = parts[0];
                    groupId = groupId.replace(PERUN_GROUP_ID + '=', "");

                    String voIdStr = parts[1];
                    voIdStr = voIdStr.replace(PERUN_VO_ID + '=', "");

                    if (voId == null || voId.equals(Long.valueOf(voIdStr))) {
                        ids.add(Long.valueOf(groupId));
                    }
                });
            }

            return ids;
        };

        List<Set<Long>> result = connectorLdap.search(null, filter, SearchScope.ONELEVEL, attributes, mapper);
        return flatten(result);
    }

    private List<Group> getGroupsByIds(@NonNull Set<Long> groupIds) {
        String[] groupAttributes = new String[] { PERUN_GROUP_ID, CN, DESCRIPTION, PERUN_UNIQUE_GROUP_NAME,
                PERUN_VO_ID, PERUN_PARENT_GROUP_ID };

        int i = 0;
        FilterBuilder[] partialFilters = new FilterBuilder[groupIds.size()];
        for (Long gid: groupIds) {
            partialFilters[i++] = equal(PERUN_GROUP_ID, String.valueOf(gid));
        }

        FilterBuilder filter = and(equal(OBJECT_CLASS, PERUN_GROUP), or(partialFilters));
        EntryMapper<Group> groupMapper = this.groupMapper(groupAttributes);

        return connectorLdap.search(null, filter, SearchScope.SUBTREE, groupAttributes, groupMapper)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Set<Long> getGroupIdsAssignedToFacility(@NonNull Long facilityId) {
        String[] attributes = new String[] { ASSIGNED_GROUP_ID };
        FilterBuilder filter = and(
                equal(OBJECT_CLASS, PERUN_RESOURCE),
                equal(PERUN_FACILITY_ID, String.valueOf(facilityId))
        );

        EntryMapper<Set<Long>> mapper = e -> {
            if (!checkHasAttributes(e, attributes)) {
                return new HashSet<>();
            }
            Attribute assignedGroupIdAttribute = e.get(ASSIGNED_GROUP_ID);

            Set<Long> groupIds = new HashSet<>();
            if (assignedGroupIdAttribute != null) {
                assignedGroupIdAttribute.iterator().forEachRemaining(v -> groupIds.add(Long.valueOf(v.getString())));
            }

            return groupIds;
        };

        List<Set<Long>> result = connectorLdap.search(null, filter, SearchScope.ONELEVEL, attributes, mapper);
        return flatten(result);
    }

    private boolean checkHasAttributes(@NonNull Entry e, @NonNull String[] attributes) {
        if (e == null) {
            return false;
        } else if (attributes == null) {
            return true;
        }

        for (String attr : attributes) {
            if (e.get(attr) == null) {
                return false;
            }
        }

        return true;
    }

    private Set<AttributeObjectMapping> getMappingsForAttrNames(@NonNull Collection<String> attrsToFetch) {
        return this.attributeMappingService.getMappingsByIdentifiers(attrsToFetch);
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

    private PerunAttributeValue parseValue(@NonNull Attribute attr, @NonNull AttributeObjectMapping mapping) {
        PerunAttrValueType type = mapping.getAttrType();
        boolean isNull = (attr == null || attr.get() == null || attr.get().isNull());
        if (isNull && PerunAttrValueType.BOOLEAN.equals(type)) {
            return new PerunAttributeValue(PerunAttributeValue.BOOLEAN_TYPE, jsonNodeFactory.booleanNode(false));
        } else if (isNull && PerunAttrValueType.ARRAY.equals(type)) {
            return new PerunAttributeValue(PerunAttributeValue.ARRAY_TYPE, jsonNodeFactory.arrayNode());
        } else if (isNull && PerunAttrValueType.LARGE_ARRAY.equals(type)) {
            return new PerunAttributeValue(PerunAttributeValue.LARGE_ARRAY_LIST_TYPE, jsonNodeFactory.arrayNode());
        } else if (isNull && PerunAttrValueType.MAP_JSON.equals(type)) {
            return new PerunAttributeValue(PerunAttributeValue.MAP_TYPE, jsonNodeFactory.objectNode());
        } else if (isNull && PerunAttrValueType.MAP_KEY_VALUE.equals(type)) {
            return new PerunAttributeValue(PerunAttributeValue.MAP_TYPE, jsonNodeFactory.objectNode());
        } else if (isNull) {
            return PerunAttributeValue.NULL;
        }
        //MAP_KEY_VALUE deleted for incompatibility with AttributeMappingService
        switch (type) {
            case STRING:
                return new PerunAttributeValue(PerunAttributeValue.STRING_TYPE,
                        jsonNodeFactory.textNode(attr.get().getString()));
            case LARGE_STRING:
                return new PerunAttributeValue(PerunAttributeValue.LARGE_STRING_TYPE,
                        jsonNodeFactory.textNode(attr.get().getString()));
            case INTEGER:
                return new PerunAttributeValue(PerunAttributeValue.INTEGER_TYPE,
                        jsonNodeFactory.numberNode(Long.parseLong(attr.get().getString())));
            case BOOLEAN:
                return new PerunAttributeValue(PerunAttributeValue.BOOLEAN_TYPE,
                        jsonNodeFactory.booleanNode(Boolean.parseBoolean(attr.get().getString())));
            case ARRAY:
                return new PerunAttributeValue(PerunAttributeValue.ARRAY_TYPE,
                        getArrNode(attr));
            case LARGE_ARRAY:
                return new PerunAttributeValue(PerunAttributeValue.LARGE_ARRAY_LIST_TYPE,
                        getArrNode(attr));
            case MAP_JSON:
                return new PerunAttributeValue(PerunAttributeValue.MAP_TYPE,
                        getMapNodeJson(attr));
            case MAP_KEY_VALUE:
                return new PerunAttributeValue(PerunAttributeValue.MAP_TYPE,
                        getMapNodeSeparator(attr, mapping.getSeparator()));
            default:
                throw new IllegalArgumentException("unrecognized type");
        }

    }

    private ObjectNode getMapNodeSeparator(@NonNull Attribute attr, @NonNull String separator) {
        ObjectNode objectNode = jsonNodeFactory.objectNode();
        for (Value value : attr) {
            if (value.getString() != null) {
                String[] parts = value.getString().split(separator, 2);
                objectNode.put(parts[0], parts[1]);
            }
        }
        return objectNode;
    }

    private ObjectNode getMapNodeJson(@NonNull Attribute attr) {
        String jsonStr = attr.get().getString();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonStr, ObjectNode.class);
        } catch (IOException e) {
            throw new InconvertibleValueException("Could not parse value");
        }
    }

    private ArrayNode getArrNode(@NonNull Attribute attr) {
        ArrayNode arrayNode = jsonNodeFactory.arrayNode(attr.size());
        for (Value value : attr) {
            arrayNode.add(value.getString());
        }
        return arrayNode;
    }

    // mappers

    private EntryMapper<Map<String, PerunAttributeValue>> attrValueMapper(@NonNull Set<AttributeObjectMapping> attrMappings) {
        return entry -> {
            Map<String, PerunAttributeValue> resultMap = new LinkedHashMap<>();
            Map<String, Attribute> attrNamesMap = new HashMap<>();

            for (Attribute attr : entry.getAttributes()) {
                if (attr.isHumanReadable()) {
                    attrNamesMap.put(attr.getId(), attr);
                }
            }

            for (AttributeObjectMapping mapping : attrMappings) {
                if (mapping.getLdapName() == null || mapping.getLdapName().isEmpty()) {
                    continue;
                }
                String ldapAttrName = mapping.getLdapName();
                // the library always converts name of attribute to lowercase, therefore we need to convert it as well
                Attribute attribute = attrNamesMap.getOrDefault(ldapAttrName.toLowerCase(), null);
                PerunAttributeValue value = parseValue(attribute, mapping);
                resultMap.put(mapping.getIdentifier(), value);
            }

            return resultMap;
        };
    }

    private EntryMapper<Vo> voMapper(@NonNull String[] attributes) {
        return e -> {
            if (!checkHasAttributes(e, attributes)) {
                return null;
            }

            Long id = Long.valueOf(e.get(PERUN_VO_ID).getString());
            String voShortName = e.get(O).getString();
            String name = e.get(DESCRIPTION).getString();

            return new Vo(id, name, voShortName);
        };
    }

    private EntryMapper<Group> groupMapper(@NonNull String[] attributes) {
        return e -> {
            if (!checkHasAttributes(e, attributes)) {
                return null;
            }

            Long id = Long.parseLong(e.get(PERUN_GROUP_ID).getString());
            String name = e.get(CN).getString();
            String description = e.get(DESCRIPTION).getString();
            String uniqueName = e.get(PERUN_UNIQUE_GROUP_NAME).getString();
            Long groupVoId = Long.valueOf(e.get(PERUN_VO_ID).getString());
            Long parentGroupId = null;
            if (e.get(PERUN_PARENT_GROUP_ID) != null) {
                parentGroupId = Long.valueOf(e.get(PERUN_PARENT_GROUP_ID).getString());
            }

            return new Group(id, parentGroupId, name, description, uniqueName, groupVoId);
        };
    }

    private <T> Set<T> flatten(@NonNull List<Set<T>> sets) {
        Set<T> flatSet = new HashSet<>();
        sets.forEach(flatSet::addAll);
        return flatSet;
    }

}
