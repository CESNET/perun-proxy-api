package cz.muni.ics.perunproxyapi.persistence.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.StringUtils;

/**
 * Perun Attribute model
 *
 * @author Dominik Frantisek Bucik <bucik@.ics.muni.cz>
 * @author Ondrej Ernst <ondra.ernst@gmail.com>
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class PerunAttribute extends PerunAttributeValueAwareModel {

    @NonNull private Long id;
    @NonNull private String friendlyName;
    @NonNull private String namespace;
    @NonNull private String description;
    @NonNull private String displayName;
    private boolean writable;
    private boolean unique;
    @NonNull private String entity;
    @NonNull private String baseFriendlyName;
    private String friendlyNameParameter;
    private final String beanName = "Attribute";

    public PerunAttribute(Long id, String friendlyName, String namespace, String description, String type,
                          String displayName, boolean writable, boolean unique, String entity, String baseFriendlyName,
                          String friendlyNameParameter, JsonNode value)
    {
        super(type, value);
        this.setId(id);
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
        this.setValue(type, value);
    }

    public void setFriendlyName(@NonNull String friendlyName) {
        if (!StringUtils.hasText(friendlyName)) {
            throw new IllegalArgumentException("friendlyName cannot be empty");
        }

        this.friendlyName = friendlyName;
    }

    public void setNamespace(@NonNull String namespace) {
        if (!StringUtils.hasText(namespace)) {
            throw new IllegalArgumentException("namespace cannot be empty");
        }

        this.namespace = namespace;
    }

    public void setDisplayName(@NonNull String displayName) {
        if (!StringUtils.hasText(displayName)) {
            throw new IllegalArgumentException("displayName cannot be empty");
        }

        this.displayName = displayName;
    }

    public void setEntity(@NonNull String entity) {
        if (!StringUtils.hasText(entity)) {
            throw new IllegalArgumentException("entity cannot be empty");
        }

        this.entity = entity;
    }

    public void setBaseFriendlyName(@NonNull String baseFriendlyName) {
        if (!StringUtils.hasText(baseFriendlyName)) {
            throw new IllegalArgumentException("baseFriendlyName can't be null or empty");
        }

        this.baseFriendlyName = baseFriendlyName;
    }

    @JsonIgnore
    public String getUrn() {
        return this.namespace + ':' + this.friendlyName;
    }

    protected ObjectNode toJson() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();

        node.put("id", id);
        node.put("friendlyName", friendlyName);
        node.put("namespace", namespace);
        node.put("type", super.getType());
        node.put("displayName", displayName);
        node.put("writable", writable);
        node.put("unique", unique);
        node.put("entity", entity);
        node.put("beanName", beanName);
        node.put("baseFriendlyName", baseFriendlyName);
        node.put("friendlyName", friendlyName);
        node.put("friendlyNameParameter", friendlyNameParameter);
        node.set("value", this.valueAsJson());

        return node;
    }

    public PerunAttributeValue toPerunAttributeValue() {
        return new PerunAttributeValue(this.getUrn(), super.getType(), this.valueAsJson());
    }

}
