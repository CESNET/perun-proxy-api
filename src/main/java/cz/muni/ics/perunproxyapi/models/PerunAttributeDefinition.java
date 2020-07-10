package cz.muni.ics.perunproxyapi.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Perun Attribute Definition model
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@ToString
@EqualsAndHashCode(callSuper = true)
public class PerunAttributeDefinition extends Model {

    @Getter
    private String friendlyName;
    @Getter
    private String namespace;
    @Getter
    private String description;
    @Getter
    private String type;
    @Getter
    private String displayName;
    @Getter
    private boolean writable;
    @Getter
    private boolean unique;
    @Getter
    private String entity;
    @Getter
    private final String beanName = "Attribute";
    @Getter
    private String baseFriendlyName;
    @Getter
    private String friendlyNameParameter;

    public PerunAttributeDefinition() { }

    public PerunAttributeDefinition(Long id, String friendlyName, String namespace, String description, String type,
                                    String displayName, boolean writable, boolean unique, String entity,
                                    String baseFriendlyName, String friendlyNameParameter) {
        super(id);
        this.setFriendlyName(friendlyName);
        this.setNamespace(namespace);
        this.setDescription(description);
        this.setType(type);
        this.setDisplayName(displayName);
        this.setWritable(writable);
        this.setUnique(unique);
        this.setEntity(entity);
        this.setBaseFriendlyName(baseFriendlyName);
        this.setFriendlyNameParameter(friendlyNameParameter);
    }

    public void setFriendlyName(String friendlyName) {
        if (Strings.isNullOrEmpty(friendlyName)) {
            throw new IllegalArgumentException("friendlyName can't be null or empty");
        }

        this.friendlyName = friendlyName;
    }

    public void setNamespace(String namespace) {
        if (Strings.isNullOrEmpty(namespace)) {
            throw new IllegalArgumentException("namespace can't be null or empty");
        }

        this.namespace = namespace;
    }

    public void setDescription(String description) {
        if (description == null) {
            throw new IllegalArgumentException("description can't be null");
        }

        this.description = description;
    }

    public void setType(String type) {
        if (Strings.isNullOrEmpty(type)) {
            throw new IllegalArgumentException("type can't be null or empty");
        }

        this.type = type;
    }

    public void setDisplayName(String displayName) {
        if (Strings.isNullOrEmpty(displayName)) {
            throw new IllegalArgumentException("displayName can't be null or empty");
        }

        this.displayName = displayName;
    }

    public void setWritable(boolean writable) {
        this.writable = writable;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public void setEntity(String entity) {
        if (Strings.isNullOrEmpty(entity)) {
            throw new IllegalArgumentException("entity can't be null or empty");
        }

        this.entity = entity;
    }

    public void setBaseFriendlyName(String baseFriendlyName) {
        if (Strings.isNullOrEmpty(baseFriendlyName)) {
            throw new IllegalArgumentException("baseFriendlyName can't be null or empty");
        }

        this.baseFriendlyName = baseFriendlyName;
    }

    public void setFriendlyNameParameter(String friendlyNameParameter) {
        this.friendlyNameParameter = friendlyNameParameter;
    }

    @JsonIgnore
    public String getUrn() {
        return this.namespace + ':' + this.friendlyName;
    }

    protected ObjectNode toJson() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();

        node.put("id", super.getId());
        node.put("friendlyName", friendlyName);
        node.put("namespace", namespace);
        node.put("type", type);
        node.put("displayName", displayName);
        node.put("writable", writable);
        node.put("unique", unique);
        node.put("entity", entity);
        node.put("beanName", beanName);
        node.put("baseFriendlyName", baseFriendlyName);
        node.put("friendlyName", friendlyName);
        node.put("friendlyNameParameter", friendlyNameParameter);

        return node;
    }
}

