package cz.muni.ics.perunproxyapi.ga4gh.model;

import cz.muni.ics.perunproxyapi.presentation.rest.interceptors.AddHeaderInterceptor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

/**
 * A class representing claim repository.
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 * @author Dominik Baranek <baranek@ics.muni.cz>
 */
@Getter
@ToString
@EqualsAndHashCode
public class ActiveClaimRepository {

    private final String name;
    @NonNull private final String actionUrl;
    @NonNull private final RestTemplate restTemplate;

    public ActiveClaimRepository(String name, @NonNull String actionUrl,
                                 @NonNull String authHeader, @NonNull String authValue)
    {
        this.name = name;
        this.actionUrl = actionUrl;
        this.restTemplate = initRestTemplate(authHeader, authValue);
    }

    public <T> T getForObject(Class<T> clazz, Map<String, String> uriVariables) {
        return restTemplate.getForObject(actionUrl, clazz, uriVariables);
    }

    private RestTemplate initRestTemplate(String authHeader, String authValue) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(
                new InterceptingClientHttpRequestFactory(restTemplate.getRequestFactory(),
                        Collections.singletonList(new AddHeaderInterceptor(authHeader, authValue)))
        );
        return restTemplate;
    }

}
