package cz.muni.ics.perunproxyapi.persistence.adapters;

import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.AttributeObjectMapping;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static cz.muni.ics.perunproxyapi.persistence.enums.Entity.USER;

/**
 * Utility class for adapters.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public class AdapterUtils {

    /**
     * Extract forwarded entitlements.
     * @param dataAdapter Adapter to be used.
     * @param userId Perun ID of user.
     * @param entitlementsIdentifier Identifier of the attribute containing the forwarded entitlements. Pass NULL
     *                               or empty String to ignore the forwarded entitlements.
     * @return List of entitlements (filled or empty).
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    public static List<String> getForwardedEntitlements(@NonNull DataAdapter dataAdapter,
                                                        @NonNull Long userId,
                                                        String entitlementsIdentifier)
            throws PerunUnknownException, PerunConnectionException
    {
        if (!StringUtils.hasText(entitlementsIdentifier)) {
            return new ArrayList<>();
        }

        PerunAttributeValue attributeValue = dataAdapter.getAttributeValue(USER, userId, entitlementsIdentifier);
        if (attributeValue != null && attributeValue.valueAsList() != null) {
            return attributeValue.valueAsList();
        }

        return new ArrayList<>();
    }

    /**
     * Extract LDAP name from the mapping. Name is considered as required.
     * @param mapping Mapping object.
     * @return Extracted name. If the mapping is null or LDAP name is empty, an exception is thrown.
     */
    public static String getRequiredLdapNameFromMapping(AttributeObjectMapping mapping) {
        if (mapping == null || !StringUtils.hasText(mapping.getLdapName())) {
            log.error("Name of the LDAP attribute is unknown - mapping:{}. Fix the configuration", mapping);
            throw new IllegalArgumentException("Name of the attribute in LDAP is required.");
        }

        return mapping.getLdapName();
    }

    /**
     * Extract LDAP name from the mapping. Name is considered as required.
     * @param mapping Mapping object.
     * @return Extracted name. If the mapping is null or LDAP name is empty, an exception is thrown.
     */
    public static String getRequiredRpcNameFromMapping(AttributeObjectMapping mapping) {
        if (mapping == null || !StringUtils.hasText(mapping.getRpcName())) {
            log.error("Name of the RPC attribute is unknown - mapping:{}. Fix the configuration", mapping);
            throw new IllegalArgumentException("Name of the attribute in RPC is required.");
        }

        return mapping.getRpcName();
    }

}
