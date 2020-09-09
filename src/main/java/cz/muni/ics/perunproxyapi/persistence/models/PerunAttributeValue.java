package cz.muni.ics.perunproxyapi.persistence.models;

import com.fasterxml.jackson.databind.JsonNode;
import cz.muni.ics.perunproxyapi.persistence.enums.AttributeType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 * Model representing value of attribute from Perun.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class PerunAttributeValue extends PerunAttributeValueAwareModel {

    @NonNull private String attrName;

    public PerunAttributeValue(String attrName, String type, JsonNode value) {
        super(type, value);
        this.setAttrName(attrName);
    }

    public PerunAttributeValue(String attrName, AttributeType type, JsonNode value)
    {
        super(type, value);
        this.setAttrName(attrName);
    }

}
