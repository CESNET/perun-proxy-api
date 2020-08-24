package cz.muni.ics.perunproxyapi.application.service;

import cz.muni.ics.perunproxyapi.persistence.adapters.DataAdapter;
import cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcAdapterImpl;
import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@SpringBootTest
public class ProxyUserMiddlewareTest {

    private static final String IDP_ENTITY_ID = "testIdpEntityId";
    private static final String USERS_LOGIN = "usersLogin";

    private final List<String> uids = new ArrayList<>(Arrays.asList("firstUid", "secondUid", "thirdUid"));

    private final ProxyUserMiddleware middleware;
    private final DataAdapter dataAdapter;

    @Autowired
    public ProxyUserMiddlewareTest(ProxyUserMiddleware proxyUserMiddleware) {
        this.middleware = proxyUserMiddleware;
        dataAdapter = mock(RpcAdapterImpl.class);
    }

    @Test
    public void findByExtLoginsCallsAdaptersMethodGetPerunUser() throws PerunUnknownException, PerunConnectionException {
        middleware.findByExtLogins(dataAdapter, IDP_ENTITY_ID, uids);

        verify(dataAdapter, times(1)).getPerunUser(IDP_ENTITY_ID, uids);
    }

    @Test
    public void findByExtLoginCallsAdaptersMethodGetPerunUser() throws PerunUnknownException, PerunConnectionException {
        middleware.findByExtLogin(dataAdapter, IDP_ENTITY_ID, USERS_LOGIN);

        verify(dataAdapter, times(1)).getPerunUser(IDP_ENTITY_ID, Collections.singletonList(USERS_LOGIN));
    }

    @Test
    public void getAttributesValuesCallsAdaptersMethodGetAttributesValues() throws PerunUnknownException, PerunConnectionException {
        middleware.getAttributesValues(dataAdapter, Entity.USER, 1L, new ArrayList<>());

        verify(dataAdapter, times(1)).getAttributesValues(Entity.USER, 1L, new ArrayList<>());
    }

    @Test
    public void findByPerunUserIdCallsAdaptersMethodFindPerunUserById() throws PerunUnknownException, PerunConnectionException {
        middleware.findByPerunUserId(dataAdapter, 1L);

        verify(dataAdapter, times(1)).findPerunUserById(1L);
    }

}
