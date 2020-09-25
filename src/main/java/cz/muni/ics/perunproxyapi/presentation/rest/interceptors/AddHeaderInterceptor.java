package cz.muni.ics.perunproxyapi.presentation.rest.interceptors;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import javax.validation.constraints.NotEmpty;
import java.io.IOException;

/**
 * HTTP Request Interceptor which adds a specific header to the request.
 * Name of the header and value are passed in constructor.
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 */
public class AddHeaderInterceptor implements ClientHttpRequestInterceptor {

    private final String header;
    private final String value;

    public AddHeaderInterceptor(@NotEmpty String header, String value) {
        this.header = header;
        this.value = value;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        request.getHeaders().add(header, value);
        return execution.execute(request, body);
    }
}
