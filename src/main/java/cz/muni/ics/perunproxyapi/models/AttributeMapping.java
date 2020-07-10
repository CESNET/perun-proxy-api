package cz.muni.ics.perunproxyapi.models;

import com.google.common.base.Strings;
import cz.muni.ics.perunproxyapi.enums.PerunAttrValueType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

/**
 * Attribute mapping model. Provides mapping of attribute with an internal name to names specific for interfaces
 * (i.e. LDAP, RPC, ...)
 *
 * Configuration (replace [attrName] with the actual name of the attribute):
 * <ul>
 *     <li><b>[attrName].mapping.ldap</b> - name of attribute in LDAP</li>
 *     <li><b>[attrName].mapping.rpc</b> - name of attribute in LDAP</li>
 *     <li><b>[attrName].mapping.type</b> - [STRING|LARGE_STRING|INTEGER|BOOLEAN|ARRAY|LARGE_ARRAY|MAP_JSON|MAP_KEY_VALUE]
 *     - type of attribute value, defaults to STRING</li>
 *     <li><b>[attrName].mapping.separator</b> - separator of keys ands values if type equals to MAP_KEY_VALUE, defaults to '='</li>
 * </ul>
 * @see cz.muni.ics.oidc.server.AttributeMappingsService for attrName configurations
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AttributeMapping {

    @Getter
    @EqualsAndHashCode.Include
    private String identifier;
    @Getter
    @EqualsAndHashCode.Include
    private String rpcName;
    @Getter
    @EqualsAndHashCode.Include
    private String ldapName;
    @Getter
    private PerunAttrValueType attrType;
    @Getter
    private String separator;

    public AttributeMapping() {
    }

    public AttributeMapping(String identifier, String rpcName, String ldapName, String type) {
        super();
        this.setIdentifier(identifier);
        this.setRpcName(rpcName);
        this.setLdapName(ldapName);
        this.setAttrType(type);
        this.setSeparator("");
    }

    public AttributeMapping(String identifier, String rpcName, String ldapName, String type, String separator) {
        super();
        this.setIdentifier(identifier);
        this.setRpcName(rpcName);
        this.setLdapName(ldapName);
        this.setAttrType(type);
        this.setSeparator(separator);
    }

    public void setIdentifier(String identifier) {
        if (Strings.isNullOrEmpty(identifier)) {
            throw new IllegalArgumentException("identifier cannot be null nor empty");
        }

        this.identifier = identifier;
    }


    public void setRpcName(String rpcName) {
        if (Strings.isNullOrEmpty(rpcName)) {
            throw new IllegalArgumentException("rpcName cannot be null nor empty");
        }

        this.rpcName = rpcName;
    }


    public void setLdapName(String ldapName) {
        this.ldapName = ldapName;
    }


    public void setAttrType(String typeStr) {
        PerunAttrValueType type = PerunAttrValueType.parse(typeStr);
        this.setAttrType(type);
    }

    public void setAttrType(PerunAttrValueType attrType) {
        if (attrType == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        this.attrType = attrType;
    }

    public void setSeparator(String separator) {
        if (separator == null || separator.trim().isEmpty()) {
            separator = "=";
        }
        this.separator = separator;
    }

}

