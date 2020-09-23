package cz.muni.ics.perunproxyapi.application.facade;

import cz.muni.ics.perunproxyapi.TestUtils;
import cz.muni.ics.perunproxyapi.application.facade.configuration.FacadeConfiguration;
import cz.muni.ics.perunproxyapi.application.facade.impl.ProxyuserFacadeImpl;
import cz.muni.ics.perunproxyapi.application.service.ProxyUserService;
import cz.muni.ics.perunproxyapi.application.service.impl.ProxyUserServiceImpl;
import cz.muni.ics.perunproxyapi.persistence.adapters.impl.AdaptersContainer;
import cz.muni.ics.perunproxyapi.persistence.exceptions.EntityNotFoundException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import cz.muni.ics.perunproxyapi.presentation.DTOModels.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@SpringBootTest
public class ProxyUserFacadeTest {

    private static final String IDP_ENTITY_ID = "testIdpEntityId";
    private static final String NON_EXISTING_IDP_ENTITY_ID = "nonExistingIdpEntityId";
    private static final String USER_LOGIN = "login";

    private final List<String> uids = Arrays.asList("firstUid", "secondUid", "thirdUid");

    private final ProxyUserService service = mock(ProxyUserServiceImpl.class);
    private final AdaptersContainer adaptersContainer = mock(AdaptersContainer.class);
    private ProxyuserFacade facade;
    private User sampleUser;
    private UserDTO sampleUserDTO;

    @BeforeEach
    public void setUp() {
        FacadeConfiguration facadeConfiguration = mock(FacadeConfiguration.class);
        facade = new ProxyuserFacadeImpl(service, adaptersContainer, facadeConfiguration);
        sampleUser = TestUtils.createSampleUser(USER_LOGIN);
        sampleUserDTO = TestUtils.getDTOForUser(sampleUser);
    }

    @Test
    public void testFindByExtLogin()
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException
    {
        when(service.findByExtLogins(any(), eq(IDP_ENTITY_ID), eq(uids))).thenReturn(sampleUser);

        UserDTO actual = facade.findByExtLogins(IDP_ENTITY_ID, uids);
        assertNotNull(actual);
        assertEquals(sampleUserDTO, actual);
    }

    @Test
    public void testFindByExtLoginNullIdpEntityId() {
        assertThrows(NullPointerException.class, () -> facade.findByExtLogins(null, uids));
    }

    @Test
    public void testFindByExtLoginNullUids() {
        assertThrows(NullPointerException.class, () -> facade.findByExtLogins(IDP_ENTITY_ID, null));
    }

    @Test
    public void testFindByExtLoginEmptyUids() {
        assertThrows(IllegalArgumentException.class, () -> facade.findByExtLogins(IDP_ENTITY_ID, new ArrayList<>()));
    }

    @Test
    public void testFindByExtLoginNonExistingIdpEntityId() throws PerunUnknownException, PerunConnectionException {
        when(service.findByExtLogins(any(), eq(NON_EXISTING_IDP_ENTITY_ID), anyList())).thenReturn(null);
        assertThrows(EntityNotFoundException.class, () -> facade.findByExtLogins(NON_EXISTING_IDP_ENTITY_ID, uids));
    }

    @Test
    public void testFindByExtLoginNonExistingIdpEntityIdUidCombination()
            throws PerunUnknownException, PerunConnectionException
    {
        List<String> uids = Collections.singletonList("nonExistingUID1");
        when(service.findByExtLogins(any(), eq(IDP_ENTITY_ID), eq(uids))).thenReturn(null);
        assertThrows(EntityNotFoundException.class, () -> facade.findByExtLogins(IDP_ENTITY_ID, uids));
    }

}
