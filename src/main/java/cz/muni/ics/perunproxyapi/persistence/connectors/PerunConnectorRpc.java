package cz.muni.ics.perunproxyapi.persistence.connectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.RpcConnectionException;
import lombok.NonNull;
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
import org.springframework.beans.factory.annotation.Value;
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

    private String perunUrl;
    private String perunUser;
    private String perunPassword;
    private boolean isEnabled;
    private RestTemplate restTemplate;

    // Values from config
    @Value("${connector.rpc.request.timeout}")
    private int REQUEST_TIMEOUT;

    @Value("${connector.rpc.connect.timeout}")
    private int CONNECT_TIMEOUT;

    @Value("${connector.rpc.socket.timeout}")
    private int SOCKET_TIMEOUT;

    @Value("${connector.rpc.max.connections}")
    private int MAX_CONNECTIONS;

    @Value("${connector.rpc.max.per_route}")
    private int MAX_CONN_PER_ROUTE;

    @Value("${connector.rpc.perun_url}")
    public void setPerunUrl(@NonNull String perunUrl) {
        if (perunUrl.endsWith("/")) {
            perunUrl = perunUrl.substring(0, perunUrl.length() - 1);
        }

        this.perunUrl = perunUrl;
    }

    @Value("${connector.rpc.perun_user}")
    public void setPerunUser(@NonNull String perunUser) {
        this.perunUser = perunUser;
    }

    @Value("${connector.rpc.perun_password}")
    public void setPerunPassword(@NonNull String perunPassword) {
        this.perunPassword = perunPassword;
    }

    @Value("${connector.rpc.is_enabled}")
    public void setEnabled(String enabled) {
        this.isEnabled = Boolean.parseBoolean(enabled);
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    @PostConstruct
    public void postInit() {
        restTemplate = new RestTemplate();
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(REQUEST_TIMEOUT) // The timeout when requesting a connection from the connection manager
                .setConnectTimeout(CONNECT_TIMEOUT) // Determines the timeout in milliseconds until a connection is established
                .setSocketTimeout(SOCKET_TIMEOUT) // The timeout for waiting for data
                .build();

        PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager();
        poolingConnectionManager.setMaxTotal(MAX_CONNECTIONS); // max total connections
        poolingConnectionManager.setDefaultMaxPerRoute(MAX_CONN_PER_ROUTE);

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
                Collections.singletonList(new BasicAuthenticationInterceptor(perunUser, perunPassword));
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
     */
    public JsonNode post(String manager, String method, Map<String, Object> map)
            throws PerunUnknownException, RpcConnectionException {
        if (!this.isEnabled) {
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
            MediaType contentType = ex.getResponseHeaders().getContentType();
            String body = ex.getResponseBodyAsString();

            if ("json".equals(contentType.getSubtype())) {
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
                            default:
                                log.error("HTTP ERROR " + ex.getRawStatusCode() + " URL " + actionUrl + " Content-Type: " + contentType);
                                log.error(json.path("message").asText());
                                throw new PerunUnknownException(ex);
                        }
                    } else if (json.has("errorId") && !json.has("name")) {
                        log.error("HTTP ERROR " + ex.getRawStatusCode() + " URL " + actionUrl + " Content-Type: " + contentType);
                        log.error(json.path("message").asText());
                        throw new PerunUnknownException(ex);
                    }
                } catch (IOException e) {
                    log.error("cannot parse error message from JSON", e);
                }
            } else {
                log.error("HTTP ERROR " + ex.getRawStatusCode() + " URL " + actionUrl + " Content-Type: " + contentType);
                log.error(ex.getMessage());
            }
            throw new PerunUnknownException(ex);
        } catch (Exception e) {
            throw new RpcConnectionException(e);
        }
    }
}
