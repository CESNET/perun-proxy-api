package cz.muni.ics.perunproxyapi.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import cz.muni.ics.perunproxyapi.persistence.adapters.DataAdapter;
import cz.muni.ics.perunproxyapi.persistence.adapters.FullAdapter;
import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttribute;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import cz.muni.ics.perunproxyapi.persistence.models.UserExtSource;
import lombok.NonNull;

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
                         @NonNull List<String> userIdentifiers)
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
    User findByExtLogin(@NonNull DataAdapter preferredAdapter, @NonNull String idpIdentifier, @NonNull String login)
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
     * @return User or null
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    User findByPerunUserId(@NonNull DataAdapter preferredAdapter, @NonNull Long userId)
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
     *
     * @param adapter
     * @param extSourceName
     * @param extSourceLogin
     * @return
     * @throws PerunUnknownException
     * @throws PerunConnectionException
     */
    UserExtSource getUserExtSource(@NonNull FullAdapter adapter, @NonNull String extSourceName,
                                   @NonNull String extSourceLogin)
            throws PerunUnknownException, PerunConnectionException;

    /**
     *
     * @param adapter
     * @param userId
     * @return
     * @throws PerunUnknownException
     * @throws PerunConnectionException
     */
    List<UserExtSource> getUserExtSources(@NonNull FullAdapter adapter, @NonNull Long userId) throws PerunUnknownException, PerunConnectionException;

    /**
     *
     * @param login
     * @param identityId
     * @param requestAttributesMap
     * @param adapter
     * @param attributeMapper
     * @param attributesToFindUes
     * @return
     * @throws PerunUnknownException
     * @throws PerunConnectionException
     */
    boolean updateUserIdentityAttributes(@NonNull String login, @NonNull String identityId,
                                                @NonNull Map<String, JsonNode> requestAttributesMap,
                                                @NonNull FullAdapter adapter, @NonNull Map<String, String> attributeMapper,
                                                @NonNull List<String> attributesToFindUes)
            throws PerunUnknownException, PerunConnectionException;

}
