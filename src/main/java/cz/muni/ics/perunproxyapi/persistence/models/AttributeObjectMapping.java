package cz.muni.ics.perunproxyapi.persistence.models;

import cz.muni.ics.perunproxyapi.persistence.AttributeMappingService;
import cz.muni.ics.perunproxyapi.persistence.enums.PerunAttrValueType;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AttributeObjectMapping {

    @EqualsAndHashCode.Include private String identifier;
    @EqualsAndHashCode.Include private String rpcName;
    @EqualsAndHashCode.Include private String ldapName;
    private PerunAttrValueType attrType;

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
}
