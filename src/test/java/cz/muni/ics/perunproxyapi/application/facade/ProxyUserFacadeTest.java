package cz.muni.ics.perunproxyapi.application.facade;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.perunproxyapi.persistence.adapters.impl.AdaptersContainer;
import cz.muni.ics.perunproxyapi.application.facade.configuration.FacadeConfiguration;
import cz.muni.ics.perunproxyapi.application.facade.impl.ProxyuserFacadeImpl;
import cz.muni.ics.perunproxyapi.application.service.ProxyUserMiddleware;
import cz.muni.ics.perunproxyapi.application.service.impl.ProxyUserMiddlewareImpl;
import cz.muni.ics.perunproxyapi.persistence.adapters.impl.rpc.RpcAdapterImpl;
import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
import cz.muni.ics.perunproxyapi.persistence.enums.PerunAttrValueType;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import cz.muni.ics.perunproxyapi.presentation.DTOModels.UserDTO;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SpringBootTest
public class ProxyUserFacadeTest {

    private static final String IDP_ENTITY_ID = "testIdpEntityId";
    private static final String DEFAULT_IDP = "defaultIdp";
    private static final String USERS_LOGIN = "usersLogin";

    private final List<String> uids = new ArrayList<>(Arrays.asList("firstUid", "secondUid", "thirdUid"));
    private final List<String> fields = new ArrayList<>(Arrays.asList("firstField", "secondField"));

    private final ProxyUserMiddleware userMiddleware;
    private final AdaptersContainer adaptersContainer;

    private final ProxyuserFacade facade;

    @Autowired
    public ProxyUserFacadeTest() {
        userMiddleware = mock(ProxyUserMiddlewareImpl.class);
        adaptersContainer = mock(AdaptersContainer.class);
        FacadeConfiguration facadeConfiguration = mock(FacadeConfiguration.class);

        ObjectMapper mapper = new ObjectMapper();

        Map<String, JsonNode> methodConfigurations = new HashMap<>();
        methodConfigurations.put("idpIdentifier", mapper.convertValue(IDP_ENTITY_ID, JsonNode.class));

        when(facadeConfiguration.getProxyUserAdapterMethodConfigurations()).thenReturn(methodConfigurations);

        facade = new ProxyuserFacadeImpl(userMiddleware, adaptersContainer, facadeConfiguration, DEFAULT_IDP);
    }

    @Test
    public void findByExtLoginsCallsMiddlewareMethodFindByExtLogins() throws PerunUnknownException, PerunConnectionException {
        RpcAdapterImpl rpcAdapter = mock(RpcAdapterImpl.class);
        when(adaptersContainer.getPreferredAdapter("RPC")).thenReturn(rpcAdapter);

        facade.findByExtLogins(IDP_ENTITY_ID, uids);

        verify(userMiddleware, times(1)).findByExtLogins(rpcAdapter, IDP_ENTITY_ID, uids);
    }

    @Test
    public void getUserByLoginCallsMiddlewareMethodFindByIdentifiers() throws PerunUnknownException, PerunConnectionException {
        RpcAdapterImpl rpcAdapter = mock(RpcAdapterImpl.class);
        when(adaptersContainer.getPreferredAdapter("RPC")).thenReturn(rpcAdapter);

        facade.getUserByLogin(USERS_LOGIN, fields);

        verify(userMiddleware, times(1)).findByExtLogin(rpcAdapter, DEFAULT_IDP, USERS_LOGIN);
    }

    @Test
    public void getUserByLoginReturnsUserDTOAndDoesNotSetAttributesWhenUserWasFoundAndFieldsIsNull() throws PerunUnknownException, PerunConnectionException {
        RpcAdapterImpl rpcAdapter = mock(RpcAdapterImpl.class);
        when(adaptersContainer.getPreferredAdapter("RPC")).thenReturn(rpcAdapter);

        when(userMiddleware.findByExtLogin(rpcAdapter, DEFAULT_IDP, USERS_LOGIN)).thenReturn(new User(1L, "Test", "User"));
        UserDTO user = facade.getUserByLogin(USERS_LOGIN, null);

        assert(user != null && user.getPerunAttributes() != null && user.getPerunAttributes().isEmpty());
    }

    @Test
    public void getUserByLoginReturnsUserDTOAndDoesNotSetAttributesWhenUserWasFoundAndFieldsIsEmpty() throws PerunUnknownException, PerunConnectionException {
        RpcAdapterImpl rpcAdapter = mock(RpcAdapterImpl.class);
        when(adaptersContainer.getPreferredAdapter("RPC")).thenReturn(rpcAdapter);

        when(userMiddleware.findByExtLogin(rpcAdapter, DEFAULT_IDP, USERS_LOGIN)).thenReturn(new User(1L, "Test", "User"));
        UserDTO user = facade.getUserByLogin(USERS_LOGIN, new ArrayList<>());

        assert(user != null && user.getPerunAttributes() != null && user.getPerunAttributes().isEmpty());
    }

    @Test
    public void getUserByLoginReturnsUserDTOAndSetAttributesWhenUserWasFoundAndFieldsIsNotEmpty() throws PerunUnknownException, PerunConnectionException {
        RpcAdapterImpl rpcAdapter = mock(RpcAdapterImpl.class);
        when(adaptersContainer.getPreferredAdapter("RPC")).thenReturn(rpcAdapter);
        when(userMiddleware.findByExtLogin(rpcAdapter, DEFAULT_IDP, USERS_LOGIN)).thenReturn(new User(1L, "Test", "User"));

        ObjectMapper mapper = new ObjectMapper();
        Map<String, PerunAttributeValue> map = new HashMap<>();
        map.put("firstField", new PerunAttributeValue(PerunAttrValueType.STRING, mapper.convertValue("firstFieldValue", JsonNode.class)));
        map.put("secondField", new PerunAttributeValue(PerunAttrValueType.STRING, mapper.convertValue("secondFieldValue", JsonNode.class)));

        when(userMiddleware.getAttributesValues(rpcAdapter, Entity.USER, 1L, fields)).thenReturn(map);

        UserDTO user = facade.getUserByLogin(USERS_LOGIN, fields);
        assert(user.getPerunAttributes() != null && user.getPerunAttributes().size() == 2);
    }

    @Test
    public void findByPerunUserIdCallsMiddlewareMethodFindByPerunUserId() throws PerunUnknownException, PerunConnectionException {
        RpcAdapterImpl rpcAdapter = mock(RpcAdapterImpl.class);
        when(adaptersContainer.getPreferredAdapter("RPC")).thenReturn(rpcAdapter);

        facade.findByPerunUserId(1L);

        verify(userMiddleware, times(1)).findByPerunUserId(rpcAdapter, 1L);
    }

}
