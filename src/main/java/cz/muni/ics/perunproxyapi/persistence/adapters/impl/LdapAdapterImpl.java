package cz.muni.ics.perunproxyapi.persistence.adapters.impl;

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
import cz.muni.ics.perunproxyapi.persistence.models.Resource;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import cz.muni.ics.perunproxyapi.persistence.models.Vo;
import lombok.Setter;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.ldap.client.api.search.FilterBuilder;
import org.apache.directory.ldap.client.template.EntryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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

import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.PerunAdapterLdapConstants.*;
import static org.apache.directory.ldap.client.api.search.FilterBuilder.*;

public class LdapAdapterImpl implements DataAdapter {


    private final static Logger log = LoggerFactory.getLogger(LdapAdapterImpl.class);

    @Autowired
    @Setter
    private PerunConnectorLdap connectorLdap;
    @Autowired
    @Setter
    private AttributeMappingService attributeMappingService;


    private JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;

    @Override
    public User getPerunUser(Long userId, List<String> uids) {

        log.trace("getPerunUser({}, {})", userId, uids);

        String dnPrefix = "ou=People";
        List<FilterBuilder> filterUids = new ArrayList<>();
        for (String uid : uids) {
            filterUids.add(equal(EDU_PERSON_PRINCIPAL_NAMES, uid));
        }

        FilterBuilder filter = and(equal(OBJECT_CLASS, PERUN_USER), or(filterUids.toArray(new FilterBuilder[0])));
        SearchScope scope = SearchScope.ONELEVEL;
        String[] attributes = new String[]{PERUN_USER_ID, GIVEN_NAME, SN};
        EntryMapper<User> mapper = e -> {
            if (!checkHasAttributes(e, new String[]{PERUN_USER_ID, SN})) {
                return null;
            }

            long id = Long.parseLong(e.get(PERUN_USER_ID).getString());
            String firstName = (e.get(GIVEN_NAME) != null) ? e.get(GIVEN_NAME).getString() : null;
            String lastName = e.get(SN).getString();
            return new User(id, firstName, lastName);
        };

        User foundUser = connectorLdap.searchFirst(dnPrefix, filter, scope, attributes, mapper);

        log.trace("getPerunUser({}, {}) returns: {}", userId, uids, foundUser);
        return foundUser;

    }

    @Override
    public List<Group> getMemberGroups(Long userId, Long voId) {
        FilterBuilder filter = and(equal(OBJECT_CLASS, PERUN_USER), equal(PERUN_USER_ID, String.valueOf(userId)));
        String[] attributes = new String[]{MEMBER_OF};
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

        List<Set<Long>> memberGroupIdsAll = connectorLdap.search(null, filter, SearchScope.SUBTREE, attributes, mapper);


        List<Group> groups = new ArrayList<>();
        Set<Long> groupIds = memberGroupIdsAll.stream().flatMap(Set::stream).collect(Collectors.toSet());
        String[] groupAttributes = new String[]{PERUN_GROUP_ID, CN, DESCRIPTION, PERUN_UNIQUE_GROUP_NAME,
                PERUN_VO_ID, PERUN_PARENT_GROUP_ID};
        EntryMapper<Group> groupMapper = e -> {
            if (!checkHasAttributes(e, new String[]{
                    PERUN_GROUP_ID, CN, DESCRIPTION, PERUN_UNIQUE_GROUP_NAME, PERUN_VO_ID})) {
                return null;
            }

            Long id = Long.valueOf(e.get(PERUN_GROUP_ID).getString());
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


        for (Long groupId : groupIds) {
            FilterBuilder groupFilter = and(equal(OBJECT_CLASS, PERUN_GROUP), equal(PERUN_GROUP_ID, String.valueOf(groupId)));
            Group group = connectorLdap.searchFirst(null, groupFilter, SearchScope.ONELEVEL, groupAttributes, groupMapper);
            groups.add(group);
        }

        return groups;

    }

    @Override
    public List<Group> getSpGroups(String spEntityId) {
        List<Facility> facilities = getFacilitiesByAttribute(ENTITY_ID, spEntityId);
        String[] attributes = new String[]{PERUN_RESOURCE_ID, ASSIGNED_GROUP_ID, PERUN_VO_ID};

        if (facilities == null || facilities.size() == 0) {
            return new ArrayList<>();
        }
        Facility facility = facilities.get(0);


        FilterBuilder filter = and(equal(OBJECT_CLASS, PERUN_RESOURCE),
                equal(PERUN_FACILITY_ID, String.valueOf(facility.getId())));
        EntryMapper<Resource> resourceMapper = e -> {
            Long resourceId = Long.valueOf(e.get(PERUN_RESOURCE_ID).getString());
            List<Long> assignedGroupId = new ArrayList<>(); //get as list?

            Attribute assignedGroupIdAttribute = e.get(ASSIGNED_GROUP_ID);

            if (assignedGroupIdAttribute != null) {
                assignedGroupIdAttribute.iterator().forEachRemaining(v -> assignedGroupId.add(Long.valueOf(v.getString())));
            }

            Long voId = Long.valueOf(e.get(PERUN_VO_ID).getString());
            Resource resource = new Resource();
            resource.setVoId(voId);
            resource.setId(resourceId);
            resource.setAssignedGroupId(assignedGroupId);
            return resource;
        };
        String[] groupAttributes = new String[]{PERUN_GROUP_ID, CN, PERUN_UNIQUE_GROUP_NAME, PERUN_VO_ID, DESCRIPTION};
        List<Resource> resources = connectorLdap.search(null, filter, SearchScope.SUBTREE, attributes, resourceMapper);
        EntryMapper<Group> groupMapper = e -> {
            if (!checkHasAttributes(e, groupAttributes)) return null;

            Long id = Long.valueOf(e.get(PERUN_GROUP_ID).getString());
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
        Set<Group> groups = new HashSet<>();
        for (Resource resource : resources) {
            if (resource.getAssignedGroupId() != null) {
                for (Long groupId : resource.getAssignedGroupId()) {
                    FilterBuilder groupFilter = and(equal(OBJECT_CLASS, PERUN_GROUP),
                            equal(PERUN_GROUP_ID, String.valueOf(groupId)),
                            equal(PERUN_VO_ID, String.valueOf(resource.getVoId())));

                    groups.add(connectorLdap.searchFirst(null, groupFilter, SearchScope.ONELEVEL, groupAttributes, groupMapper));

                }
            }
        }
        return new ArrayList<>(groups);
    }

    @Override
    public Group getGroupByName(Long voId, String groupName) {
        String[] groupAttributes = new String[]{PERUN_GROUP_ID, CN, PERUN_UNIQUE_GROUP_NAME, PERUN_VO_ID, DESCRIPTION};
        FilterBuilder groupFilter = and(equal(OBJECT_CLASS, PERUN_GROUP), equal(PERUN_UNIQUE_GROUP_NAME, groupName));
        EntryMapper<Group> groupMapper = e -> {
            if (!checkHasAttributes(e, groupAttributes)) return null;

            Long id = Long.valueOf(e.get(PERUN_GROUP_ID).getString());
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
        return connectorLdap.searchFirst(null, groupFilter, SearchScope.ONELEVEL, groupAttributes, groupMapper);
    }

    @Override
    public Vo getVoByShortName(String voShortName) {
        log.trace("getVoByShortName({})", voShortName);

        FilterBuilder filter = and(equal(OBJECT_CLASS, PERUN_VO), equal(O, voShortName));
        String[] attributes = new String[]{PERUN_VO_ID, O, DESCRIPTION};

        EntryMapper<Vo> mapper = e -> {
            if (!checkHasAttributes(e, attributes)) {
                return null;
            }

            Long id = Long.valueOf(e.get(PERUN_VO_ID).getString());
            String shortNameVo = e.get(O).getString();
            String name = e.get(DESCRIPTION).getString();

            return new Vo(id, name, shortNameVo);
        };
        Vo vo = connectorLdap.searchFirst(null, filter, SearchScope.ONELEVEL, attributes, mapper);
        log.trace("getVoByShortName({}) returns: {}", voShortName, vo);
        return vo;
    }

    @Override
    public Vo getVoById(Long voId) {
        log.trace("getVoById({})", voId);

        FilterBuilder filter = and(equal(OBJECT_CLASS, PERUN_VO), equal(PERUN_VO_ID, String.valueOf(voId)));
        String[] attributes = new String[]{PERUN_VO_ID, O, DESCRIPTION};

        EntryMapper<Vo> mapper = e -> {
            if (!checkHasAttributes(e, attributes)) {
                return null;
            }

            Long id = Long.valueOf(e.get(PERUN_VO_ID).getString());
            String shortNameVo = e.get(O).getString();
            String name = e.get(DESCRIPTION).getString();

            return new Vo(id, name, shortNameVo);
        };
        Vo vo = connectorLdap.searchFirst(null, filter, SearchScope.ONELEVEL, attributes, mapper);
        log.trace("getVoById({}) returns: {}", voId, vo);
        return vo;
    }

    @Override
    public Map<String, PerunAttributeValue> getAttributesValues(Entity entity, Long entityId, List<String> attrs) {
        Set<AttributeObjectMapping> mappings = getMappingsForAttrNames(attrs);
        String[] attributes = getAttributesFromMappings(mappings);

        Map<String, PerunAttributeValue> res = new HashMap<>();
        if (attributes.length != 0) {
            EntryMapper<Map<String, PerunAttributeValue>> mapper = attrValueMapper(mappings);
            res = this.connectorLdap.lookup(null, attributes, mapper);
        }

        return res;
    }

    @Override
    public List<Facility> getFacilitiesByAttribute(String name, String attrValue) {

        SearchScope scope = SearchScope.ONELEVEL;
        String[] attributes = new String[]{PERUN_FACILITY_ID, DESCRIPTION, CN};
        EntryMapper<Facility> mapper = e -> {

            long id = Long.parseLong(e.get(PERUN_FACILITY_ID).getString());
            String facilityName = e.get(CN).getString();
            String description = e.get(DESCRIPTION).getString();

            return new Facility(id, facilityName, description);
        };

        FilterBuilder filter = and(equal(OBJECT_CLASS, PERUN_FACILITY), equal(name, attrValue));
        return connectorLdap.search(null, filter, scope, attributes, mapper);
    }

    @Override
    public List<Group> getUsersGroupsOnFacility(Long spEntityId, Long userId) {

        List<Facility> facilities = getFacilitiesByAttribute(ENTITY_ID, String.valueOf(spEntityId));
        if (facilities == null || facilities.size() == 0) {
            return new ArrayList<>();
        }
        Facility facility = facilities.get(0);

        String[] attributes = new String[]{PERUN_RESOURCE_ID};
        if (facility == null) {
            return new ArrayList<>();
        }


        FilterBuilder filter = and(equal(OBJECT_CLASS, PERUN_RESOURCE),
                equal(PERUN_FACILITY_ID, String.valueOf(facility.getId())));
        EntryMapper<Resource> resourceMapper = e -> {
            Long resourceId = Long.valueOf(e.get(PERUN_RESOURCE_ID).getString());
            List<Long> assignedGroupId = new ArrayList<>(); //get as list?

            Attribute assignedGroupIdAttribute = e.get(ASSIGNED_GROUP_ID);

            if (assignedGroupIdAttribute != null) {
                assignedGroupIdAttribute.iterator().forEachRemaining(v -> assignedGroupId.add(Long.valueOf(v.getString())));
            }
            Resource resource = new Resource();
            resource.setId(resourceId);
            return resource;
        };
        List<Resource> resources = connectorLdap.search(null, filter, SearchScope.ONELEVEL, attributes, resourceMapper);
        List<FilterBuilder> filterResources = new ArrayList<>();
        for (Resource resource : resources) {
            filterResources.add(equal(EDU_PERSON_PRINCIPAL_NAMES, String.valueOf(resource.getId())));
        }

        FilterBuilder resourcesFilter = and(equal(OBJECT_CLASS, PERUN_USER), or(filterResources.toArray(new FilterBuilder[0])));
        String[] groupAttributes = new String[]{PERUN_GROUP_ID, CN, DESCRIPTION, PERUN_UNIQUE_GROUP_NAME,
                PERUN_VO_ID, PERUN_PARENT_GROUP_ID};
        EntryMapper<Group> groupMapper = e -> {
            if (!checkHasAttributes(e, new String[]{
                    PERUN_GROUP_ID, CN, DESCRIPTION, PERUN_UNIQUE_GROUP_NAME, PERUN_VO_ID})) {
                return null;
            }

            Long id = Long.valueOf(e.get(PERUN_GROUP_ID).getString());
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
        List<Group> result;
        result = connectorLdap.search(null, resourcesFilter, SearchScope.SUBTREE, groupAttributes, groupMapper);
        result = result.stream().filter(Objects::nonNull).collect(Collectors.toList());
        return result;

    }

    @Override
    public List<Facility> searchFacilitiesByAttributeValue(PerunAttribute attribute) {
        return null;//not now
    }

    private boolean checkHasAttributes(Entry e, String[] attributes) {
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

    private Set<AttributeObjectMapping> getMappingsForAttrNames(Collection<String> attrsToFetch) {
        return this.attributeMappingService.getMappingsByIdentifiers(attrsToFetch);
    }

    private String[] getAttributesFromMappings(Set<AttributeObjectMapping> mappings) {
        return mappings
                .stream()
                .map(AttributeObjectMapping::getLdapName)
                .distinct()
                .filter(e -> e != null && e.length() != 0)
                .collect(Collectors.toList())
                .toArray(new String[]{});
    }

    private EntryMapper<Map<String, PerunAttributeValue>> attrValueMapper(Set<AttributeObjectMapping> attrMappings) {
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

    private PerunAttributeValue parseValue(Attribute attr, AttributeObjectMapping mapping) {
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
            default:
                throw new IllegalArgumentException("unrecognized type");
        }

    }

    private ObjectNode getMapNodeSeparator(Attribute attr, String separator) {
        ObjectNode objectNode = jsonNodeFactory.objectNode();
        for (Value value : attr) {
            if (value.getString() != null) {
                String[] parts = value.getString().split(separator, 2);
                objectNode.put(parts[0], parts[1]);
            }
        }
        return objectNode;
    }

    private ObjectNode getMapNodeJson(Attribute attr) {
        String jsonStr = attr.get().getString();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonStr, ObjectNode.class);
        } catch (IOException e) {
            throw new InconvertibleValueException("Could not parse value");
        }
    }

    private ArrayNode getArrNode(Attribute attr) {
        ArrayNode arrayNode = jsonNodeFactory.arrayNode(attr.size());
        for (Value value : attr) {
            arrayNode.add(value.getString());
        }
        return arrayNode;
    }


}
