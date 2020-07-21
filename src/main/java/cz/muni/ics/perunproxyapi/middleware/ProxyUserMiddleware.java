package cz.muni.ics.perunproxyapi.middleware;

import cz.muni.ics.perunproxyapi.adapters.interfaces.DataAdapter;
import cz.muni.ics.perunproxyapi.models.User;

import java.util.List;

public interface ProxyUserMiddleware {

    /**
     *
     * @param preferredAdapter preferred adapter to be used (rpc or ldap). If no adapter is given, rpc is used.
     * @param idpEntityId source identity provider's id
     * @param uids source identifiers
     * @return User or null if not exist
     */
    User findByIdentifiers(DataAdapter preferredAdapter, long idpEntityId, List<String> uids);
}
