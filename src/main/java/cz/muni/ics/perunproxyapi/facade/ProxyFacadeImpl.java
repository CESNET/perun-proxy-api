package cz.muni.ics.perunproxyapi.facade;

import cz.muni.ics.perunproxyapi.adapters.LdapAdapterImpl;
import cz.muni.ics.perunproxyapi.adapters.RpcAdapterImpl;
import cz.muni.ics.perunproxyapi.adapters.interfaces.DataAdapter;
import cz.muni.ics.perunproxyapi.middleware.ProxyUserMiddlewareImpl;
import cz.muni.ics.perunproxyapi.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProxyFacadeImpl implements ProxyFacade {

    @Autowired
    private ProxyUserMiddlewareImpl userMiddleware;
    @Autowired
    private RpcAdapterImpl rpcAdapter;
    @Autowired
    private LdapAdapterImpl ldapAdapter;
    @Autowired
    private Environment env;

    public User findByIdentifiers(String IdPIdentifier,
                                  List<String> identifiers) {

        // second argument is default value
        String preferredAdapter = env.getProperty("proxyuser.find_by_identifiers.adapter","RPC");

        DataAdapter adapter = getPreferredAdapter(preferredAdapter);
        return userMiddleware.findByIdentifiers(adapter, Long.parseLong(IdPIdentifier), identifiers);
    }

    private DataAdapter getPreferredAdapter(String preferredAdapter) {
        if (preferredAdapter.toUpperCase().equals("RPC")) {
            return rpcAdapter;
        } else if (preferredAdapter.toUpperCase().equals("LDAP")) {
            return ldapAdapter;
        }

        return rpcAdapter;
    }
}
