package cz.muni.ics.perunproxyapi.presentation.rest.controllers;

import cz.muni.ics.perunproxyapi.application.facade.RelyingPartyFacade;
import cz.muni.ics.perunproxyapi.persistence.exceptions.EntityNotFoundException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cz.muni.ics.perunproxyapi.presentation.rest.config.PathConstants.AUTH_PATH;
import static cz.muni.ics.perunproxyapi.presentation.rest.config.PathConstants.RELYING_PARTY;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


/**
 * Controller containing methods related to proxy user. Basic Auth is required.
 * methods path: /CONTEXT_PATH/auth/relying-party/**
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@RestController
@RequestMapping(value = AUTH_PATH + RELYING_PARTY)
@Slf4j
public class RelyingPartyProtectedController {

    public static final String RP_IDENTIFIER = "rp-identifier";
    public static final String LOGIN = "login";

    private final RelyingPartyFacade facade;

    @Autowired
    public RelyingPartyProtectedController(RelyingPartyFacade facade) {
        this.facade = facade;
    }

    /**
     * Get entitlements for user specified by login when he/she is accessing the service specified by the
     * given rp-identifier.
     *
     * EXAMPLE CURL:
     * curl --request GET \
     *   --url 'http://localhost:8081/proxyapi/auth/relying-party/rpID1/proxy-user/ \
     *   example_login_value@einfra.cesnet.cz/entitlements'
     *   --header 'authorization: Basic auth'
     *
     * @param rpIdentifier Identifier of the Relying Party.
     * @param login Login of the user.
     * @return List of entitlements (filled or empty).
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     * @throws EntityNotFoundException Thrown when no user has been found.
     */
    @ResponseBody
    @GetMapping(value = "/{rp-identifier}/proxy-user/{login}/entitlements", produces = APPLICATION_JSON_VALUE)
    public List<String> getEntitlements(@NonNull @PathVariable(RP_IDENTIFIER) String rpIdentifier,
                                        @NonNull @PathVariable(LOGIN) String login)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException
    {
        return facade.getEntitlements(rpIdentifier, login);
    }

}
