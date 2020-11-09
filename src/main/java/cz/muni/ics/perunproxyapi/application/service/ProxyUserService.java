package cz.muni.ics.perunproxyapi.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import cz.muni.ics.perunproxyapi.persistence.adapters.DataAdapter;
import cz.muni.ics.perunproxyapi.persistence.adapters.FullAdapter;
import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue;
import cz.muni.ics.perunproxyapi.persistence.models.UpdateAttributeMappingEntry;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import lombok.NonNull;
import org.apache.coyote.Adapter;

import javax.management.InvalidAttributeValueException;
import java.util.List;
import java.util.Map;

/**
 * Service layer for user related things. Purpose of this class is to execute correct methods on the given adapter.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 * @author Pavol Pluta <pavol.pluta1@gmail.com>
 */
public interface ProxyUserService {

    /**
     * Find user by identifiers via given adapter.
     *
     * @param preferredAdapter Adapter to be used.
     * @param idpIdentifier Identifier of source Identity Provider.
     * @param userIdentifiers List of user's identifiers.
     * @return User or null.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    User findByExtLogins(@NonNull DataAdapter preferredAdapter, @NonNull String idpIdentifier,
                         @NonNull List<String> userIdentifiers, List<String> attrIdentifiers)
            throws PerunUnknownException, PerunConnectionException;

    /**
     * Get user by given IdP identifier and attribute.
     *
     * @param preferredAdapter Adapter to be used.
     * @param idpIdentifier Identifier of source Identity Provider.
     * @param login User login attribute.
     * @return User or null.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    User findByExtLogin(@NonNull DataAdapter preferredAdapter, @NonNull String idpIdentifier,
                        @NonNull String login, List<String> attrIdentifiers)
            throws PerunUnknownException, PerunConnectionException;

    /**
     * Get attribute values for a given entity.
     *
     * @param preferredAdapter Adapter to be used.
     * @param entity Entity.
     * @param id Entity id.
     * @param attributes Attributes.
     * @return Map of attribute values for a given entity.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    Map<String, PerunAttributeValue> getAttributesValues(@NonNull DataAdapter preferredAdapter, @NonNull Entity entity,
                                                         long id, List<String> attributes)
            throws PerunUnknownException, PerunConnectionException;

    /**
     * Get user by id.
     *
     * @param preferredAdapter Adapter for connection to be used.
     * @param userId Id of a Perun user.
     * @param attrIdentifiers Identifiers of the attributes to be fetched for user.
     * @return User or null
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    User findByPerunUserIdWithAttributes(@NonNull DataAdapter preferredAdapter, @NonNull Long userId,
                                         List<String> attrIdentifiers)
            throws PerunUnknownException, PerunConnectionException;

    /**
     * Get entitlements for a user.
     *
     * @param userId Id of the user
     * @param prefix Prefix to be prepended.
     * @param authority Authority issuing the entitlements.
     * @param forwardedEntitlementsAttrIdentifier Identifier of the attribute containing forwarded entitlements.
     * @return List of AARC formatted entitlements (filled or empty).
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    List<String> getAllEntitlements(@NonNull DataAdapter adapter, @NonNull Long userId,
                                    @NonNull String prefix, @NonNull String authority,
                                    String forwardedEntitlementsAttrIdentifier)
            throws PerunUnknownException, PerunConnectionException;

    /**
     * Get user with attributes by login.
     * @param preferredAdapter Adapter for connection to be used.
     * @param login Actual login of the user.
     * @param attrIdentifiers Identifiers of the attributes to be fetched for user.
     * @return User with requested attributes or null.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    User getUserWithAttributesByLogin(@NonNull DataAdapter preferredAdapter,
                                      @NonNull String login,
                                      List<String> attrIdentifiers)
            throws PerunUnknownException, PerunConnectionException;

    /**
     * Get user by login.
     * @param preferredAdapter Adapter for connection to be used.
     * @param login Actual login of the user.
     * @return User with requested attributes or null.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    User getUserByLogin(@NonNull DataAdapter preferredAdapter, @NonNull String login)
            throws PerunUnknownException, PerunConnectionException;

    /**
     * Find user by given source IdP entityId and additional source identifiers.
     * !!!! Works only with LDAP adapter !!!!
     * @param adapter Adapter to be used. Only LDAP is supported.
     * @param idpIdentifier Identifier of source Identity Provider.
     * @param identifiers List of strings containing identifiers of the user.
     * @param attrIdentifiers Identifiers of the attributes to be fetched for user.
     * @return User or null.
     */
    User findByIdentifiers(@NonNull DataAdapter adapter,
                           @NonNull String idpIdentifier,
                           @NonNull List<String> identifiers,
                           @NonNull List<String> attrIdentifiers);

    /**
     * Update attributes of the specified user identity.
     * @param login Login of the user
     * @param identityId Identifier of identity
     * @param adapter Adapter to be used.
     * @param requestAttributes Attributes from request that should be updated.
     *                          Key is the external name, value is the new value.
     * @param mapper Map of internal attribute names to external with boolean flag if new values should
     *               be replaced or appended and boolean flag if the internal name is used for searching.
     * @param externalToInternalMapping Map of external names to internal identifiers.
     * @param attrsToSearchBy Attributes by which to look for the correct identity.
     * @return TRUE if updated, FALSE otherwise.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    boolean updateUserIdentityAttributes(@NonNull String login,
                                         @NonNull String identityId,
                                         @NonNull FullAdapter adapter,
                                         @NonNull Map<String, JsonNode> requestAttributes,
                                         @NonNull Map<String, UpdateAttributeMappingEntry> mapper,
                                         @NonNull Map<String, String> externalToInternalMapping,
                                         @NonNull List<String> attrsToSearchBy)
            throws PerunUnknownException, PerunConnectionException;

}
