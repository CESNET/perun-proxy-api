package cz.muni.ics.perunproxyapi.application.service.impl;

import cz.muni.ics.perunproxyapi.application.service.ProxyUserMiddleware;
import cz.muni.ics.perunproxyapi.persistence.adapters.DataAdapter;
import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class ProxyUserMiddlewareImpl implements ProxyUserMiddleware {

    @Override
    public User findByExtLogins(DataAdapter preferredAdapter, String idpEntityId, List<String> userIdentifiers) throws PerunUnknownException, PerunConnectionException {
        return preferredAdapter.getPerunUser(idpEntityId, userIdentifiers);
    }

    @Override
    public User findByExtLogin(DataAdapter preferredAdapter, String idpIdentifier, String login) throws PerunUnknownException, PerunConnectionException {
        return findByExtLogins(preferredAdapter, idpIdentifier, Collections.singletonList(login));
    }

    @Override
    public Map<String, PerunAttributeValue> getAttributesValues(DataAdapter preferredAdapter, Entity entity, long id, List<String> attributes) throws PerunUnknownException, PerunConnectionException {
        return preferredAdapter.getAttributesValues(entity, id, attributes);
    }

    @Override
    public User findByPerunUserId(DataAdapter preferredAdapter, Long userId) throws PerunUnknownException, PerunConnectionException {
        return preferredAdapter.findPerunUserById(userId);
    }

}
