package cz.muni.ics.perunproxyapi.persistence.connectors;

import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the RPC connector. For each call you need to mock what will the underlying restTemplate return.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@SpringBootTest
public class PerunConnectorRpcTest {

    private static final String mockUrl = "http://somewhere.com";
    private static final RestTemplate restTemplateMock = mock(RestTemplate.class);
    private static final PerunConnectorRpc connectorMock = new PerunConnectorRpc(true, restTemplateMock, mockUrl);

    private final String usersManager = "usersManager";
    private final String getUserByIdMethod = "getUserById";

    @Test
    public void testConnectorConnectionException() {
        when(restTemplateMock.postForObject(anyString(), anyMap(), any()))
                .thenThrow(new RestClientException("Test exception"));

        assertThrows(PerunConnectionException.class,
                () -> connectorMock.post(usersManager, getUserByIdMethod, Collections.emptyMap()));
    }

}
