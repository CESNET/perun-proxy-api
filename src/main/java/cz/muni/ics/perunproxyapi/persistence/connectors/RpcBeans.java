package cz.muni.ics.perunproxyapi.persistence.connectors;

import cz.muni.ics.perunproxyapi.persistence.connectors.properties.RpcConnectorProperties;
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
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Component
public class RpcBeans {

    private final RpcConnectorProperties rpcConnectorProperties;

    @Autowired
    public RpcBeans(RpcConnectorProperties rpcConnectorProperties) {
        this.rpcConnectorProperties = rpcConnectorProperties;
    }

    @Bean
    public RestTemplate restTemplate() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(rpcConnectorProperties.getRequestTimeout()) // The timeout when requesting a connection from the connection manager
                .setConnectTimeout(rpcConnectorProperties.getConnectTimeout()) // Determines the timeout in milliseconds until a connection is established
                .setSocketTimeout(rpcConnectorProperties.getSocketTimeout()) // The timeout for waiting for data
                .build();

        PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager();
        poolingConnectionManager.setMaxTotal(rpcConnectorProperties.getMaxConnections()); // max total connections
        poolingConnectionManager.setDefaultMaxPerRoute(rpcConnectorProperties.getMaxConnectionsPerRoute());

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
                Collections.singletonList(new BasicAuthenticationInterceptor(rpcConnectorProperties.getPerunUser(),
                        rpcConnectorProperties.getPerunPassword()));
        InterceptingClientHttpRequestFactory authenticatingRequestFactory =
                new InterceptingClientHttpRequestFactory(poolingRequestFactory, interceptors);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(authenticatingRequestFactory);
        return restTemplate;
    }
}
