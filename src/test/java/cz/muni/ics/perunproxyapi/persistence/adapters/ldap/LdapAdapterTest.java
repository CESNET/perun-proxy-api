package cz.muni.ics.perunproxyapi.persistence.adapters.ldap;

import cz.muni.ics.perunproxyapi.persistence.AttributeMappingService;
import cz.muni.ics.perunproxyapi.persistence.adapters.DataAdapter;
import cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.LdapAdapterImpl;
import cz.muni.ics.perunproxyapi.persistence.connectors.PerunConnectorLdap;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.query.LdapQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.LdapAdapterImpl.GIVEN_NAME;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.LdapAdapterImpl.OBJECT_CLASS;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.LdapAdapterImpl.PERUN_USER;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.LdapAdapterImpl.PERUN_USER_ID;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.LdapAdapterImpl.SN;
import static cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcAdapterImpl.USERS_MANAGER;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.ldap.query.LdapQueryBuilder.query;


@SpringBootTest
public class LdapAdapterTest {

    private static final String IDP_ENTITY_ID = "testIdpEntityId";

    private final List<String> uids = new ArrayList<>(Arrays.asList("firstUid", "secondUid", "thirdUid"));

    private DataAdapter ldapAdapter;

    private AttributeMappingService attributeMappingService;
    private PerunConnectorLdap perunConnectorLdap;

    @Before
    public void setup() {
        attributeMappingService = mock(AttributeMappingService.class);
        perunConnectorLdap = mock(PerunConnectorLdap.class);

        ldapAdapter = new LdapAdapterImpl(perunConnectorLdap, attributeMappingService);
    }

}
