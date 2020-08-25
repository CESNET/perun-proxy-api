package cz.muni.ics.perunproxyapi.persistence.connectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import cz.muni.ics.perunproxyapi.persistence.connectors.properties.RpcConnectorProperties;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.System.currentTimeMillis;

/**
 * Connector for calling Perun RPC
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 * @author Pavol Pluta <pavol.pluta1@gmail.com>
 */
@Component
@Slf4j
public class PerunConnectorRpc {

    private final RpcConnectorProperties properties;
    private final String perunUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public PerunConnectorRpc(RpcConnectorProperties properties) {
        this.properties = properties;
        this.perunUrl = properties.getPerunUrl();
    }

    @PostConstruct
    public void postInit() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(properties.getRequestTimeout()) // The timeout when requesting a connection from the connection manager
                .setConnectTimeout(properties.getConnectTimeout()) // Determines the timeout in milliseconds until a connection is established
                .setSocketTimeout(properties.getSocketTimeout()) // The timeout for waiting for data
                .build();

        PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager();
        poolingConnectionManager.setMaxTotal(properties.getMaxConnections()); // max total connections
        poolingConnectionManager.setDefaultMaxPerRoute(properties.getMaxConnectionsPerRoute());

        ConnectionKeepAliveStrategy connectionKeepAliveStrategy = (response, context) -> {
            HeaderElementIterator it = new BasicHeaderElementIterator
                    (response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                HeaderElement he = it.nextElement();
                String param = he.getName();
                String value = he.getValue();

                if (value != null && param.equalsIgnoreCase("timeout")) {
                    return Long.parseLong(value) * 1000;
                }
            }
            return 20000L;
        };

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(poolingConnectionManager)
                .setKeepAliveStrategy(connectionKeepAliveStrategy)
                .build();

        HttpComponentsClientHttpRequestFactory poolingRequestFactory = new HttpComponentsClientHttpRequestFactory();
        poolingRequestFactory.setHttpClient(httpClient);

        // basic auth
        List<ClientHttpRequestInterceptor> interceptors =
                Collections.singletonList(new BasicAuthenticationInterceptor(properties.getPerunUser(),
                        properties.getPerunPassword()));
        InterceptingClientHttpRequestFactory authenticatingRequestFactory =
                new InterceptingClientHttpRequestFactory(poolingRequestFactory, interceptors);
        restTemplate.setRequestFactory(authenticatingRequestFactory);
    }

    /**
     * Make post call to Perun RPC
     * @param manager String value representing manager to be called. Use constants from this class.
     * @param method Method to be called (i.e. getUserById)
     * @param map Map of parameters to be passed as request body
     * @return Response from Perun
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    public JsonNode post(String manager, String method, Map<String, Object> map)
            throws PerunUnknownException, PerunConnectionException {
        if (!properties.isEnabled()) {
            return JsonNodeFactory.instance.nullNode();
        }

        String actionUrl = this.perunUrl + "/json/" + manager + '/' + method;

        // make the call
        try {
            log.trace("calling {} with {}", actionUrl, map);
            long startTime = currentTimeMillis();
            JsonNode result = restTemplate.postForObject(actionUrl, map, JsonNode.class);
            long endTime = currentTimeMillis();
            long responseTime = endTime - startTime;
            log.trace("POST call proceeded in {} ms.",responseTime);
            return result;
        } catch (HttpClientErrorException ex) {
            return handleHttpClientErrorException(ex, actionUrl);
        } catch (Exception e) {
            throw new PerunConnectionException(e);
        }
    }

    private JsonNode handleHttpClientErrorException(HttpClientErrorException ex, String actionUrl)
            throws PerunUnknownException
    {
        MediaType contentType = null;
        if (ex.getResponseHeaders() != null) {
            contentType = ex.getResponseHeaders().getContentType();
        }

        String body = ex.getResponseBodyAsString();

        if (contentType != null && "json".equalsIgnoreCase(contentType.getSubtype())) {
            try {
                JsonNode json = new ObjectMapper().readValue(body,JsonNode.class);
                if (json.has("errorId") && json.has("name")) {
                    switch (json.get("name").asText()) {
                        case "ExtSourceNotExistsException":
                        case "FacilityNotExistsException":
                        case "GroupNotExistsException":
                        case "MemberNotExistsException":
                        case "ResourceNotExistsException":
                        case "VoNotExistsException":
                        case "UserNotExistsException":
                            return JsonNodeFactory.instance.nullNode();
                    }
                }
            } catch (IOException e) {
                log.error("cannot parse error message from JSON", e);
                throw new PerunUnknownException(ex);
            }
        }

        log.error("HTTP ERROR {} URL {} Content-Type: {}", ex.getRawStatusCode(), actionUrl, contentType, ex);
        throw new PerunUnknownException(ex);
    }

}
