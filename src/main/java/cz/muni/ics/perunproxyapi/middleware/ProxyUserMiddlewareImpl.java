package cz.muni.ics.perunproxyapi.middleware;

import cz.muni.ics.perunproxyapi.adapters.interfaces.DataAdapter;
import cz.muni.ics.perunproxyapi.models.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProxyUserMiddlewareImpl implements ProxyUserMiddleware {

    public User findByIdentifiers(DataAdapter preferredAdapter, long idpEntityId, List<String> uids) {
        return preferredAdapter.getPerunUser(idpEntityId, uids);
    }

}
