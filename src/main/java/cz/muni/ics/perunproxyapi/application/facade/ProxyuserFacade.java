package cz.muni.ics.perunproxyapi.application.facade;

import cz.muni.ics.perunproxyapi.persistence.exceptions.EntityNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.presentation.DTOModels.UserDTO;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

/**
 * Facade for proxyuser related things. Purpose of this class is to execute correct lower-level methods
 * to achieve the desired results.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 * @author Pavol Pluta <pavol.pluta1@gmail.com>
 */
public interface ProxyuserFacade {

    /**
     * Find user by userIdentifiers via given adapter.
     *
     * @param idpIdentifier Identifier of source Identity Provider.
     * @param userIdentifiers List of string containing identifiers of a user.
     * @return User or null.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     * @throws EntityNotFoundException Thrown when no user has been found.
     */
    UserDTO findByExtLogins(@NonNull String idpIdentifier, @NonNull List<String> userIdentifiers)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException;

    /**
     * Find user by given source IdP entityId and additional source identifiers.
     * !!!! Works only with LDAP adapter !!!!
     * @param idpIdentifier Identifier of source Identity Provider.
     * @param identifiers List of strings containing identifiers of a user.
     * @return User or null.
     * @throws EntityNotFoundException Thrown when no user has been found.
     */
    UserDTO findByIdentifiers(@NonNull String idpIdentifier, @NonNull List<String> identifiers)
            throws EntityNotFoundException;

    /**
     * Get user with fields by his/her login.
     *
     * @param login User's login.
     * @param fields List of user's attributes we want to retrieve.
     * @return User with attributes or null.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     * @throws EntityNotFoundException Thrown when no user has been found.
     */
    UserDTO getUserByLogin(@NonNull String login, List<String> fields)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException;

    /**
     * Find user by id.
     * @param userId Id of a Perun user.
     * @param fields OPTIONAL attributes for the user we want to obtain
     * @return User or null.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     * @throws EntityNotFoundException Thrown when no user has been found.
     */
    UserDTO findByPerunUserId(Long userId, List<String> fields)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException;

    /**
     * Get entitlements for user.
     *
     * @param login Login of the user.
     * @return List of AARC formatted entitlements (filled or empty).
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     * @throws EntityNotFoundException Thrown when no user has been found.
     */
    List<String> getAllEntitlements(@NonNull String login)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException;

    /**
     * Update User identity attributes.
     * @param login Login of the user.
     * @return TRUE if the requestAttributes were updated properly, FALSE otherwise
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    boolean updateUserIdentityAttributes(@NonNull String login,
                                         @NonNull String identityId,
                                         @NonNull Map<String, JsonNode> requestAttributes)
            throws PerunUnknownException, PerunConnectionException;

}
