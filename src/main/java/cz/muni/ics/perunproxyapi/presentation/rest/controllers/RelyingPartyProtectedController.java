package cz.muni.ics.perunproxyapi.presentation.rest.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import cz.muni.ics.perunproxyapi.application.facade.RelyingPartyFacade;
import cz.muni.ics.perunproxyapi.persistence.exceptions.EntityNotFoundException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.InvalidRequestParameterException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Set;

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
    private static final String IDP_ENTITY_ID = "idpEntityId";
    private static final String RP_NAME = "rpName";
    private static final String IDP_NAME = "idpName";

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
     * @param rpIdentifier Identifier of the Relying Party in base64url safe format.
     * @param login Login of the user.
     * @return List of entitlements (filled or empty).
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     * @throws EntityNotFoundException Thrown when no user has been found.
     * @throws InvalidRequestParameterException Thrown when passed parameters or body does not meet criteria.
     */
    @ResponseBody
    @GetMapping(value = "/{rp-identifier}/proxy-user/{login}/entitlements", produces = APPLICATION_JSON_VALUE)
    public Set<String> getEntitlements(@PathVariable(RP_IDENTIFIER) String rpIdentifier,
                                        @PathVariable(LOGIN) String login)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException,
            InvalidRequestParameterException
    {
        if (!StringUtils.hasText(rpIdentifier)) {
            throw new InvalidRequestParameterException("RP identifier cannot be empty");
        } else if (!StringUtils.hasText(login)) {
            throw new InvalidRequestParameterException("User login cannot be empty");
        }
        String decodedRpIdentifier = ControllerUtils.decodeUrlSafeBase64(rpIdentifier);
        return facade.getEntitlements(decodedRpIdentifier, login);
    }

    /**
     * Get extended entitlements for user specified by login when he/she is accessing the service specified by the
     * given rp-identifier.
     *
     * EXAMPLE CURL:
     * curl --request GET \
     *   --url 'http://localhost:8081/proxyapi/auth/relying-party/rpID1/proxy-user/ \
     *   example_login_value@einfra.cesnet.cz/entitlementsExtended'
     *   --header 'authorization: Basic auth'
     *
     * @param rpIdentifier Identifier of the Relying Party in base64url safe format.
     * @param login Login of the user.
     * @return List of extended entitlements (filled or empty).
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     * @throws EntityNotFoundException Thrown when no user has been found.
     * @throws InvalidRequestParameterException Thrown when passed parameters or body does not meet criteria.
     */
    @ResponseBody
    @GetMapping(value = "/{rp-identifier}/proxy-user/{login}/entitlementsExtended", produces = APPLICATION_JSON_VALUE)
    public Set<String> getEntitlementsExtended(@PathVariable(RP_IDENTIFIER) String rpIdentifier,
                                               @PathVariable(LOGIN) String login)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException,
            InvalidRequestParameterException
    {
        if (!StringUtils.hasText(rpIdentifier)) {
            throw new InvalidRequestParameterException("RP identifier cannot be empty");
        } else if (!StringUtils.hasText(login)) {
            throw new InvalidRequestParameterException("User login cannot be empty");
        }
        String decodedRpIdentifier = ControllerUtils.decodeUrlSafeBase64(rpIdentifier);
        return facade.getEntitlementsExtended(decodedRpIdentifier, login);
    }

    /**
     * Check if user has access to the service.
     *
     * @param rpIdentifier Identifier of the RP in base64url safe format.
     * @param login Login of the user.
     * @return TRUE if user has access to service, otherwise FALSE.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     * @throws EntityNotFoundException Thrown when no user has been found.
     * @throws IOException Invalid I/O value occurred during conversion from JSON to list of long values.
     */
    @ResponseBody
    @GetMapping(value = "/{rp-identifier}/proxy-user/{login}/access", produces = APPLICATION_JSON_VALUE)
    public boolean hasAccessToService(@PathVariable(RP_IDENTIFIER) String rpIdentifier,
                                      @PathVariable(LOGIN) String login)
            throws PerunUnknownException, PerunConnectionException, InvalidRequestParameterException, EntityNotFoundException, IOException {
        if (!StringUtils.hasText(rpIdentifier)) {
            throw new InvalidRequestParameterException("RP identifier cannot be empty");
        } else if (!StringUtils.hasText(login)) {
            throw new InvalidRequestParameterException("User login cannot be empty");
        }
        String decodedRpIdentifier = ControllerUtils.decodeUrlSafeBase64(rpIdentifier);
        return facade.hasAccessToService(decodedRpIdentifier, login);
    }

    /**
     * Finds value of rpEnvironment attribute of the given facility. Returns it as JSON.
     *
     * @param rpIdentifier RP identifier URL SAFE BASE64 encoded
     * @return RP environment - one of: TESTING | STAGING | PRODUCTION
     * @throws InvalidRequestParameterException Thrown when passed request parameters do not meet criteria.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    @ResponseBody
    @GetMapping(value = "/{rp-identifier}/environment")
    public String rpEnvironmentJson(@PathVariable(RP_IDENTIFIER) String rpIdentifier)
            throws InvalidRequestParameterException, PerunUnknownException, PerunConnectionException,
            EntityNotFoundException
    {
        if (!StringUtils.hasText(rpIdentifier)) {
            throw new InvalidRequestParameterException("Invalid RP identifier");
        }
        String decodedRpIdentifier = ControllerUtils.decodeUrlSafeBase64(rpIdentifier);
        return facade.getRpEnvironmentValue(decodedRpIdentifier);
    }

    /**
     * <pre>
     * Log statistics about login into corresponding tables
     *
     * EXAMPLE CURL:
     *  curl --request PUT \
     *   --url http://localhost:8080/proxyapi/auth/relying-party/statistics \
     *   --header 'Authorization: Basic auth' \
     *   --header 'Content-Type: application/json' \
     *   --data '{
     * 	    "login": "test_user_login",
     * 	    "rp-identifier": "test_rpIdentifier",
     * 	    "rpName": "test_rpName",
     * 	    "idpEntityId": "test_idpEntityId",
     * 	    "idpName": "test_IdpName"
     *    }'
     * </pre>
     * @param body json body corresponding of required attributes:
     *             - login
     *             - rp-identifier
     *             - rpName
     *             - idpEntityId
     *             - idpName
     * @return HTTP Status 200 if data was successfully logged into statistics table, otherwise 404.
     * @throws InvalidRequestParameterException Thrown when passed request parameters do not meet criteria.
     * @throws EntityNotFoundException Thrown when no user has been found.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    @ResponseBody
    @PutMapping(value = "/statistics", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> logStatistics(@RequestBody JsonNode body) throws InvalidRequestParameterException, EntityNotFoundException, PerunUnknownException, PerunConnectionException {
        if (body == null) {
            throw new InvalidRequestParameterException("Request body is empty.");
        }

        String login = ControllerUtils.extractRequiredString(body, LOGIN);
        String rpIdentifier = ControllerUtils.extractRequiredString(body, RP_IDENTIFIER);
        String rpName = ControllerUtils.extractRequiredString(body, RP_NAME);
        String idpEntityId = ControllerUtils.extractRequiredString(body, IDP_ENTITY_ID);
        String idpName = ControllerUtils.extractRequiredString(body, IDP_NAME);


        boolean result = facade.logStatistics(login, rpIdentifier, rpName, idpEntityId, idpName);

        if (result) {
            return new ResponseEntity<>("Statistics successfully logged.", HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
