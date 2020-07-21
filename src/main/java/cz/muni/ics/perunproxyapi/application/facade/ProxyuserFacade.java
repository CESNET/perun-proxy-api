package cz.muni.ics.perunproxyapi.application.facade;


import cz.muni.ics.perunproxyapi.persistence.models.User;

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
     * @param userIdentifiers List of User users identifiers.
     * @return User or null.
     */
    User findByIdentifiers(String idpIdentifier, List<String> userIdentifiers);

}
