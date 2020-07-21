package cz.muni.ics.perunproxyapi.application.service;

import cz.muni.ics.perunproxyapi.persistence.adapters.DataAdapter;
import cz.muni.ics.perunproxyapi.persistence.models.User;

import java.util.List;

/**
 * Middleware for user related things. Purpose of this class to execute correct methods on the given adapter.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 * @author Pavol Pluta <pavol.pluta1@gmail.com>
 */
public interface ProxyUserMiddleware {

    /**
     * Find user by identifiers via given adapter.
     *
     * @param preferredAdapter Adapter to be used.
     * @param idpEntityId Identifier of source Identity Provider.
     * @param userIdentifiers List of users identifiers.
     * @return User or null.
     */
    User findByIdentifiers(DataAdapter preferredAdapter, String idpEntityId, List<String> userIdentifiers);

}
