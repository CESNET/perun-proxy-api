package cz.muni.ics.perunproxyapi.application.facade;


import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import cz.muni.ics.perunproxyapi.presentation.DTOModels.UserDTO;

import java.util.List;

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
     */
    User findByExtLogins(String idpIdentifier, List<String> userIdentifiers) throws PerunUnknownException, PerunConnectionException;

    /**
     *
     * @param login User's login.
     * @param fields List of user's attributes.
     * @return User with attributes values or null.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    UserDTO getUserByLogin(String login, List<String> fields) throws PerunUnknownException, PerunConnectionException;

}
