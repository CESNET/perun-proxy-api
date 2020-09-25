package cz.muni.ics.perunproxyapi.presentation.rest.controllers;

import java.util.Map;

import cz.muni.ics.perunproxyapi.application.service.ga4gh.DefaultJWTSigningAndValidationService;
import cz.muni.ics.perunproxyapi.application.service.ga4gh.JWKSetView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nimbusds.jose.jwk.JWK;

@Controller
public class JWKSetPublishingEndpoint {

    public static final String URL = "jwk";

    private DefaultJWTSigningAndValidationService jwtService;

    @Autowired
    public JWKSetPublishingEndpoint(DefaultJWTSigningAndValidationService jwtService) {
        this.jwtService = jwtService;
    }

    @RequestMapping(value = "/" + URL, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getJwk(Model m) {

        // map from key id to key
        Map<String, JWK> keys = jwtService.getAllPublicKeys();

        // TODO: check if keys are empty, return a 404 here or just an empty list?

        m.addAttribute("keys", keys);

        return JWKSetView.VIEWNAME;
    }

    /**
     * @return the jwtService
     */
    public DefaultJWTSigningAndValidationService getJwtService() {
        return jwtService;
    }

    /**
     * @param jwtService the jwtService to set
     */
    public void setJwtService(DefaultJWTSigningAndValidationService jwtService) {
        this.jwtService = jwtService;
    }

}
