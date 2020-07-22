package cz.muni.ics.perunproxyapi.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

import static cz.muni.ics.perunproxyapi.models.PerunAttributeValue.NULL_TYPE;


/**
 * Perun Attribute model
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@ToString
@EqualsAndHashCode(callSuper = true)
public class PerunAttribute extends PerunAttributeDefinition {

    public static final PerunAttribute NULL = new PerunAttribute(NULL_TYPE, PerunAttributeValue.NULL);
    @Getter
    @Setter
    private PerunAttributeValue value;
    @Getter
    @Setter
    private String valueCreatedAt;
    @Getter
    @Setter
    private String valueModifiedAt;

    public PerunAttribute() { }

    private PerunAttribute(String type, PerunAttributeValue value) {
        super(-1L, "NULL", "NULL" ,"NULL", type, "NULL", false, false, "NULL", "NULL" ,"NULL");
        this.value = value;
        this.valueCreatedAt = null;
        this.valueModifiedAt = null;
    }

    @Override
    @JsonIgnore
    public String getUrn() {
        return super.getUrn();
    }

    public ObjectNode toJson() {
        ObjectNode defInJson = super.toJson();
        defInJson.set("value", value.valueAsJson());

        return defInJson;
    }
}

