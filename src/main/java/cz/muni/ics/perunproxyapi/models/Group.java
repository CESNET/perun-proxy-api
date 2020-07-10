package cz.muni.ics.perunproxyapi.models;


import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Group object model.
 *
 * @author Peter Jancus <jancus@ics.muni.cz>
 */
@EqualsAndHashCode(callSuper = true)
@ToString
public class Group extends Model {
    @Getter
    private Long parentGroupId;
    @Getter
    private String name;
    @Getter
    private String description;
    @Getter
    private String uniqueGroupName; // voShortName + ":" + group name
    @Getter
    private Long voId;
    @Getter
    private Map<String, JsonNode> attributes = new LinkedHashMap<>();

    public Group() {
    }

    public Group(Long id, Long parentGroupId, String name, String description, String uniqueGroupName, Long voId) {
        super(id);
        this.setParentGroupId(parentGroupId);
        this.setName(name);
        this.setDescription(description);
        this.setUniqueGroupName(uniqueGroupName);
        this.setVoId(voId);
    }

    public void setParentGroupId(Long parentGroupId) {
        this.parentGroupId = parentGroupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (Strings.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("name cannot be null nor empty");
        }

        this.name = name;
    }

    public void setDescription(String description) {
        if (Strings.isNullOrEmpty(description)) {
            throw new IllegalArgumentException("description cannot be null nor empty");
        }

        this.description = description;
    }

    public void setUniqueGroupName(String uniqueGroupName) {
        this.uniqueGroupName = uniqueGroupName;
    }

    public void setVoId(Long voId) {
        if (voId == null) {
            throw new IllegalArgumentException("voId cannot be null");
        }

        this.voId = voId;
    }

    public void setAttributes(Map<String, JsonNode> attributes) {
        this.attributes = attributes;
    }

    /**
     * Gets attribute by urn name
     *
     * @param attributeName urn name of attribute
     * @return attribute
     */
    public JsonNode getAttributeByUrnName(String attributeName) {
        if (attributes == null || !attributes.containsKey(attributeName)) {
            return null;
        }

        return attributes.get(attributeName);
    }

    /**
     * Gets attribute by friendly name
     *
     * @param attributeName attribute name
     * @param attributeUrnPrefix urn prefix of attribute
     * @return attribute
     */
    public JsonNode getAttributeByFriendlyName(String attributeName, String attributeUrnPrefix) {
        String key = attributeUrnPrefix + ":" + attributeName;

        if (attributes == null || !attributes.containsKey(key)) {
            return null;
        }

        return attributes.get(key);
    }
}

