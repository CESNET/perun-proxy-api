package cz.muni.ics.perunproxyapi.application.service.impl;

import cz.muni.ics.perunproxyapi.application.service.ProxyUserMiddleware;
import cz.muni.ics.perunproxyapi.persistence.adapters.DataAdapter;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProxyUserMiddlewareImpl implements ProxyUserMiddleware {

    public User findByIdentifiers(DataAdapter preferredAdapter, String idpEntityId, List<String> userIdentifiers) {
        return preferredAdapter.getPerunUser(idpEntityId, userIdentifiers);
    }

}
