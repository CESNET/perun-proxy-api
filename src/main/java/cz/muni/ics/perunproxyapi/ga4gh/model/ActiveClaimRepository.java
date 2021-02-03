package cz.muni.ics.perunproxyapi.ga4gh.model;

import cz.muni.ics.perunproxyapi.presentation.rest.interceptors.AddHeaderInterceptor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @NonNull private final String name;
    @NonNull private final String actionUrl;
    @NonNull private final RestTemplate restTemplate;

    public ActiveClaimRepository(@NonNull String name, @NonNull String actionUrl,
                                 @NonNull List<ClaimRepository.Header> headers)
    {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("ActiveClaimRepository cannot have empty name");
        }
        this.name = name;
        if (!StringUtils.hasText(actionUrl)) {
            throw new IllegalArgumentException("ActiveClaimRepository cannot have empty actionUrl");
        }
        this.actionUrl = actionUrl;
        if (headers.isEmpty()) {
            throw new IllegalArgumentException("ActiveClaimRepository cannot have empty list of request headers");
        }
        this.restTemplate = initRestTemplate(headers);
    }

    public <T> T getForObject(Class<T> clazz, Map<String, String> uriVariables) {
        return restTemplate.getForObject(actionUrl, clazz, uriVariables);
    }

    private RestTemplate initRestTemplate(@NonNull List<ClaimRepository.Header> headers) {
        RestTemplate restTemplate = new RestTemplate();
        List<ClientHttpRequestInterceptor> interceptors = headers.stream()
                .map(h -> new AddHeaderInterceptor(h.getHeader(), h.getValue()))
                .collect(Collectors.toList());
        restTemplate.setRequestFactory(
                new InterceptingClientHttpRequestFactory(restTemplate.getRequestFactory(), interceptors));
        return restTemplate;
    }

}
