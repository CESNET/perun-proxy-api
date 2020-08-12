package cz.muni.ics.perunproxyapi.application.service.impl;

import cz.muni.ics.perunproxyapi.application.service.ProxyUserMiddleware;
import cz.muni.ics.perunproxyapi.persistence.adapters.DataAdapter;
import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class ProxyUserMiddlewareImpl implements ProxyUserMiddleware {

    public User findByIdentifiers(DataAdapter preferredAdapter, String idpEntityId, List<String> userIdentifiers) {
        return preferredAdapter.getPerunUser(idpEntityId, userIdentifiers);
    }

    public User getUserByAttribute(DataAdapter preferredAdapter, String idpIdentifier, String attribute) {
        return preferredAdapter.getPerunUser(idpIdentifier, Collections.singletonList(attribute));
    }

    public Map<String, PerunAttributeValue> getAttributesValues(DataAdapter preferredAdapter, Entity entity, long id, List<String> attributes) {
        return preferredAdapter.getAttributesValues(entity, id, attributes);
    }
}
