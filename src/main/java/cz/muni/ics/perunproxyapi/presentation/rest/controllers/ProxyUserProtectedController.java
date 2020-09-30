package cz.muni.ics.perunproxyapi.presentation.rest.controllers;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.muni.ics.perunproxyapi.application.facade.ProxyuserFacade;
import cz.muni.ics.perunproxyapi.persistence.exceptions.EntityNotFoundException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.InvalidRequestParameterException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.presentation.DTOModels.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static cz.muni.ics.perunproxyapi.presentation.rest.config.PathConstants.AUTH_PATH;
import static cz.muni.ics.perunproxyapi.presentation.rest.config.PathConstants.PROXY_USER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller containing methods related to proxy user. Basic Auth is required.
 * methods path: /CONTEXT_PATH/auth/proxy-user/**

 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@RestController
@RequestMapping(value = AUTH_PATH + PROXY_USER)
@Slf4j
public class ProxyUserProtectedController {

    public static final String IDP_IDENTIFIER = "IdPIdentifier";
    public static final String IDENTIFIERS = "identifiers";
    public static final String LOGIN = "login";
    public static final String FIELDS = "fields";
    public static final String USER_ID = "userId";
    public static final String IDENTITY_ID = "identityId";

    private final ProxyuserFacade facade;


    @Autowired
    public ProxyUserProtectedController(ProxyuserFacade facade) {
        this.facade = facade;
    }

    /**
     * Find user by logins provided by the external sources.
     *
     * EXAMPLE CURL:
     * curl --request GET --url 'http://127.0.0.1:8081/proxyapi/auth/proxy-user/findByExtLogins?IdPIdentifier=
     * identifier&identifiers=id1&identifiers=id2'
     * --header 'authorization: Basic auth'
     *
     * @param idpIdentifier Identifier of the identity provider (external source identifier).
     * @param identifiers List of user identifiers at the given identity provider.
     * @return Found user or NULL.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     * @throws EntityNotFoundException Thrown when no user has been found.
     * @throws InvalidRequestParameterException Thrown when passed parameters or body does not meet criteria.
     */
    @ResponseBody
    @GetMapping(value = "/findByExtLogins", produces = APPLICATION_JSON_VALUE)
    public UserDTO findByExtLogins(@RequestParam(value = IDP_IDENTIFIER) String idpIdentifier,
                                   @RequestParam(value = IDENTIFIERS) List<String> identifiers)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException,
            InvalidRequestParameterException
    {
        if (!StringUtils.hasText(idpIdentifier)) {
            throw new InvalidRequestParameterException("IdP identifier cannot be empty");
        } else if (identifiers == null || identifiers.isEmpty()) {
            throw new InvalidRequestParameterException("User identifiers cannot be empty");
        }

        return facade.findByExtLogins(idpIdentifier, identifiers);
    }

    /**
     * Find user by given source IdP entityId and additional source identifiers.
     * !!!! Works only with LDAP adapter !!!!
     *
     * EXAMPLE CURL:
     * curl --request GET \
     *   --url http://localhost:8081/proxyapi/auth/proxy-user/findByIdentifiers?IdPIdentifier=IDP1 \
     *   &identifiers=ID1&identifiers=ID2 \
     *   --header 'authorization: Basic auth'
     *
     * @param idpIdentifier Identifier of source Identity Provider.
     * @param identifiers List of string containing identifiers of the user.
     * @return User or null.
     * @throws EntityNotFoundException Thrown when no user has been found.
     * @throws InvalidRequestParameterException Thrown when passed parameters or body does not meet criteria.
     */
    @ResponseBody
    @GetMapping(value = "/findByIdentifiers", produces = APPLICATION_JSON_VALUE)
    public UserDTO findByIdentifiers(@RequestParam(value = IDP_IDENTIFIER) String idpIdentifier,
                                     @RequestParam(value = IDENTIFIERS) List<String> identifiers)
            throws EntityNotFoundException, InvalidRequestParameterException
    {
        if (!StringUtils.hasText(idpIdentifier)) {
            throw new InvalidRequestParameterException("IdP identifier cannot be empty");
        } else if (identifiers == null || identifiers.isEmpty()) {
            throw new InvalidRequestParameterException("User identifiers cannot be empty");
        }
        return facade.findByIdentifiers(idpIdentifier, identifiers);
    }

    /**
     * Get Perun user by login.
     *
     * EXAMPLE CURL:
     * curl --request GET \
     *   --url http://localhost:8081/proxyapi/auth/proxy-user/example_login_value@einfra.cesnet.cz \
     *   --header 'authorization: Basic auth'
     *
     * @param login Login attribute to be used. Must be unique.
     * @param fields OPTIONAL attributes for the user we want to obtain
     * @return JSON representation of the User object.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     * @throws EntityNotFoundException Thrown when no user has been found.
     * @throws InvalidRequestParameterException Thrown when passed parameters or body does not meet criteria.
     */
    @ResponseBody
    @GetMapping(value = "/{login}", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserDTO getUserByLogin(@PathVariable(value = LOGIN) String login,
                                  @RequestParam(required = false, value = FIELDS) List<String> fields)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException,
            InvalidRequestParameterException
    {
        if (!StringUtils.hasText(login)) {
            throw new InvalidRequestParameterException("IdP identifier cannot be empty");
        }
        return facade.getUserByLogin(login, fields);
    }

    /**
     * Find Perun user by its id.
     *
     * EXAMPLE CURL:
     * curl --request GET --url 'http://127.0.0.1:8081/proxyapi/auth/proxy-user/findByPerunUserId?userId=12345'
     * --header 'authorization: Basic auth'
     *
     * @param userId Id of a Perun user.
     * @param fields OPTIONAL attributes for the user we want to obtain
     * @return JSON representation of the User object.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     * @throws EntityNotFoundException Thrown when no user has been found.
     * @throws InvalidRequestParameterException Thrown when passed parameters or body does not meet criteria.
     */
    @ResponseBody
    @GetMapping(value = "/findByPerunUserId", produces = APPLICATION_JSON_VALUE)
    public UserDTO findByPerunUserId(@RequestParam(value = USER_ID) Long userId,
                                     @RequestParam(required = false, value = FIELDS) List<String> fields)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException,
            InvalidRequestParameterException
    {
        if (userId == null) {
            throw new InvalidRequestParameterException("User ID cannot be null");
        }
        return facade.findByPerunUserId(userId, fields);
    }

    /**
     * Get all entitlements for user with given login.
     *
     * EXAMPLE CURL:
     * curl --request GET --url 'http://127.0.0.1:8081/proxyapi/auth/proxy-user/login@somewhere.org/entitlements
     * --header 'authorization: Basic auth'
     *
     * @param login Login of the user.
     * @return List of all entitlements (excluding resource and facility capabilities as we cannot construct them)
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     * @throws EntityNotFoundException Thrown when no user has been found.
     * @throws InvalidRequestParameterException Thrown when passed parameters or body does not meet criteria.
     */
    @ResponseBody
    @GetMapping(value = "/{login}/entitlements", produces = APPLICATION_JSON_VALUE)
    public List<String> getUserEntitlements(@PathVariable(value = LOGIN) String login)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException,
            InvalidRequestParameterException
    {
        if (!StringUtils.hasText(login)) {
            throw new InvalidRequestParameterException("Users login cannot be empty");
        }
        return facade.getAllEntitlements(login);
    }

    /**
     * Update UserExtSource attributes
     * @param login of the user
     * @param identityId the id of the identity provider
     * @param body the body containing UserExtSource attributes to be updated
     * @return true if the attributes were updated properly, false otherwise
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    @ResponseBody
    @PutMapping(value = "/{login}/identity",
            produces = APPLICATION_JSON_VALUE,
            consumes = APPLICATION_JSON_VALUE)
    public boolean updateUserIdentityAttributes(@PathVariable(value = LOGIN) String login,
                                                @RequestParam(value = IDENTITY_ID) String identityId,
                                                @RequestBody JsonNode body)
            throws PerunUnknownException, PerunConnectionException, InvalidRequestParameterException
    {
        if (body == null || !body.hasNonNull("attributes")) {
            throw new InvalidRequestParameterException("The request body cannot be null and must contain attributes");
        }
        if (!StringUtils.hasText(login)) {
            throw new InvalidRequestParameterException("Users login cannot be empty");
        }
        if (!StringUtils.hasText(identityId)) {
            throw new InvalidRequestParameterException("identityId cannot be empty");
        }
        ObjectNode jsonAttributes = (ObjectNode) body.get("attributes");
        Map<String, JsonNode> attributes = new HashMap<>();
        Iterator<String> it = jsonAttributes.fieldNames();
        while (it.hasNext()) {
            String fieldName = it.next();
            attributes.put(fieldName, jsonAttributes.get(fieldName));
        }
        return facade.updateUserIdentityAttributes(login, identityId, attributes);
    }

}
