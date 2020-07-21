package cz.muni.ics.perunproxyapi.facade;

import cz.muni.ics.perunproxyapi.models.User;

import java.util.List;

public interface ProxyFacade {
    /**
     *
     * @param IdPIdentifier source identity provider's id
     * @param identifiers source identifiers
     * @return User or null if not exist
     */
    User findByIdentifiers(String IdPIdentifier, List<String> identifiers);
}
