package cz.muni.ics.perunproxyapi.application.service;

import cz.muni.ics.perunproxyapi.TestUtils;
import cz.muni.ics.perunproxyapi.application.service.impl.ProxyUserServiceImpl;
import cz.muni.ics.perunproxyapi.persistence.adapters.DataAdapter;
import cz.muni.ics.perunproxyapi.persistence.adapters.FullAdapter;
import cz.muni.ics.perunproxyapi.persistence.adapters.impl.ldap.LdapAdapterImpl;
import cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcAdapterImpl;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SpringBootTest
public class ProxyUserServiceTest {

    private static final String IDP_ENTITY_ID = "testIdpEntityId";
    private static final String USER_LOGIN = "login";

    private final List<String> uids =Arrays.asList("firstUid", "secondUid", "thirdUid");

    private final DataAdapter ldapAdapter = mock(LdapAdapterImpl.class);
    private final FullAdapter rpcAdapter = mock(RpcAdapterImpl.class);

    private ProxyUserService service;
    private User sampleUser;

    @BeforeEach
    public void setUp() {
        this.service = new ProxyUserServiceImpl();
        sampleUser = TestUtils.createSampleUser(USER_LOGIN);
    }

    @Test
    public void testFindByExtLoginsViaRpc() throws PerunUnknownException, PerunConnectionException {
        when(ldapAdapter.getPerunUser(anyString(), anyList())).thenReturn(sampleUser);
        when(rpcAdapter.getPerunUser(anyString(), anyList())).thenReturn(sampleUser);

        User actual = service.findByExtLogins(rpcAdapter, IDP_ENTITY_ID, uids);
        assertNotNull(actual);
        assertEquals(sampleUser, actual);
        verify(rpcAdapter, times(1)).getPerunUser(IDP_ENTITY_ID, uids);
        verify(ldapAdapter, times(0)).getPerunUser(IDP_ENTITY_ID, uids);
    }

    @Test
    public void testFindByExtLoginsViaLdap() throws PerunUnknownException, PerunConnectionException {
        when(ldapAdapter.getPerunUser(anyString(), anyList())).thenReturn(sampleUser);
        when(rpcAdapter.getPerunUser(anyString(), anyList())).thenReturn(sampleUser);

        User actual = service.findByExtLogins(ldapAdapter, IDP_ENTITY_ID, uids);
        assertNotNull(actual);
        assertEquals(sampleUser, actual);
        verify(ldapAdapter, times(1)).getPerunUser(IDP_ENTITY_ID, uids);
        verify(rpcAdapter, times(0)).getPerunUser(IDP_ENTITY_ID, uids);
    }

    @Test
    public void testFindByExtLoginsUserNotFoundViaRpc() throws PerunUnknownException, PerunConnectionException {
        when(ldapAdapter.getPerunUser(anyString(), anyList())).thenReturn(null);
        when(rpcAdapter.getPerunUser(anyString(), anyList())).thenReturn(null);

        User actual = service.findByExtLogins(rpcAdapter, IDP_ENTITY_ID, uids);
        assertNull(actual);
        verify(rpcAdapter, times(1)).getPerunUser(IDP_ENTITY_ID, uids);
        verify(ldapAdapter, times(0)).getPerunUser(IDP_ENTITY_ID, uids);
    }

    @Test
    public void testFindByExtLoginsUserNotFoundViaLdap() throws PerunUnknownException, PerunConnectionException {
        when(ldapAdapter.getPerunUser(anyString(), anyList())).thenReturn(null);
        when(rpcAdapter.getPerunUser(anyString(), anyList())).thenReturn(null);

        User actual = service.findByExtLogins(ldapAdapter, IDP_ENTITY_ID, uids);
        assertNull(actual);
        verify(ldapAdapter, times(1)).getPerunUser(IDP_ENTITY_ID, uids);
        verify(rpcAdapter, times(0)).getPerunUser(IDP_ENTITY_ID, uids);
    }

    @Test
    public void testFindByExtLoginsNullAdapter() {
        assertThrows(NullPointerException.class, () -> service.findByExtLogins(null, IDP_ENTITY_ID, uids));
    }

    @Test
    public void testFindByExtLoginsNullIdpIdentifier() {
        assertThrows(NullPointerException.class, () -> service.findByExtLogins(rpcAdapter, null, uids));
    }

    @Test
    public void testFindByExtLoginsNullUids() {
        assertThrows(NullPointerException.class, () -> service.findByExtLogins(rpcAdapter, IDP_ENTITY_ID, null));
    }

    @Test
    public void testFindByExtLoginsEmptyIdpIdentifier() {
        assertThrows(IllegalArgumentException.class, () -> service.findByExtLogins(rpcAdapter, "", uids));
        assertThrows(IllegalArgumentException.class, () -> service.findByExtLogins(rpcAdapter, " ", uids));
    }

    @Test
    public void testFindByExtLoginsEmptyUids() {
        assertThrows(IllegalArgumentException.class, () -> service.findByExtLogins(rpcAdapter, IDP_ENTITY_ID, new ArrayList<>()));
    }

}
