package cz.muni.ics.perunproxyapi.application.facade;

import cz.muni.ics.perunproxyapi.persistence.adapters.DataAdapter;
import cz.muni.ics.perunproxyapi.persistence.adapters.FullAdapter;
import lombok.Getter;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static cz.muni.ics.perunproxyapi.application.facade.configuration.FacadeConfiguration.ADAPTER_LDAP;
import static cz.muni.ics.perunproxyapi.application.facade.configuration.FacadeConfiguration.ADAPTER_RPC;

/**
 * Class containing different adapters. Autowire it anywhere you need to use adapters.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Getter
@Component
public class AdaptersContainer {

    private final FullAdapter rpcAdapter;
    private final DataAdapter ldapAdapter;

    @Autowired
    public AdaptersContainer(@NonNull FullAdapter rpcAdapter,
                             @NonNull DataAdapter ldapAdapter) {
        this.rpcAdapter = rpcAdapter;
        this.ldapAdapter = ldapAdapter;
    }

    public DataAdapter getPreferredAdapter(String preferredAdapter) {
        if (preferredAdapter.toUpperCase().equals(ADAPTER_RPC)) {
            return rpcAdapter;
        } else if (preferredAdapter.toUpperCase().equals(ADAPTER_LDAP)) {
            return ldapAdapter;
        }

        return rpcAdapter;
    }

}
