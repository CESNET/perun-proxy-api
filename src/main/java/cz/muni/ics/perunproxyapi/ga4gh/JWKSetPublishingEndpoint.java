package cz.muni.ics.perunproxyapi.ga4gh;

import com.nimbusds.jose.jwk.JWK;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint to publish JWK set.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@RestController
public class JWKSetPublishingEndpoint {

    public static final String URL = "jwk";

    private JWTSigningAndValidationService jwtService;

    public void setJwtService(JWTSigningAndValidationService jwtService) {
        this.jwtService = jwtService;
    }

    @RequestMapping(value = '/' + URL, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, JWK> getJwk() {
        return jwtService.getAllPublicKeys();
    }

}
