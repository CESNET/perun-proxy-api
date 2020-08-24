package cz.muni.ics.perunproxyapi.persistence.models;

import cz.muni.ics.perunproxyapi.persistence.AttributeMappingService;
import cz.muni.ics.perunproxyapi.persistence.enums.PerunAttrValueType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.springframework.util.StringUtils;

/**
 * Attribute mapping model. Provides mapping of attribute with an internal name to names specific for interfaces
 * (i.e. LDAP, RPC, ...)
 *
 * @see AttributeMappingService for attrName configurations
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 * @author Dominik Baranek <baranek@ics.muni.cz>
 */
@Getter
@ToString
@EqualsAndHashCode
public class AttributeObjectMapping {

    public static final String DEFAULT_SEPARATOR = ",";

    private String identifier;
    private String rpcName;
    private String ldapName;
    @EqualsAndHashCode.Exclude private PerunAttrValueType attrType;
    private String separator;

    public AttributeObjectMapping() {
        this.separator = DEFAULT_SEPARATOR;
    }

    public AttributeObjectMapping(String identifier, String rpcName, String ldapName,
                                  PerunAttrValueType attrType, String separator) {
        this.setIdentifier(identifier);
        this.setRpcName(rpcName);
        this.setLdapName(ldapName);
        this.setAttrType(attrType);
        this.setSeparator(separator);
    }

    public void setIdentifier(@NonNull String identifier) {
        if (StringUtils.isEmpty(identifier)) {
            throw new IllegalArgumentException("identifier cannot be null nor empty");
        }

        this.identifier = identifier;
    }

    public void setRpcName(@NonNull String rpcName) {
        if (StringUtils.isEmpty(rpcName)) {
            throw new IllegalArgumentException("rpcName cannot be null nor empty");
        }

        this.rpcName = rpcName;
    }

    public void setLdapName(String ldapName) {
        if (StringUtils.isEmpty(ldapName)) {
            this.ldapName = null;
        } else {
            this.ldapName = ldapName;
        }
    }

    public void setAttrType(String typeStr) {
        PerunAttrValueType type = PerunAttrValueType.parse(typeStr);
        this.setAttrType(type);
    }

    public void setAttrType(@NonNull PerunAttrValueType attrType) {
        this.attrType = attrType;
    }

    public void setSeparator(String separator) {
        if (separator == null || separator.length() == 0) {
            this.separator = DEFAULT_SEPARATOR;
        } else {
            this.separator = separator;
        }
    }

}
