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
import org.springframework.web.bind.annotation.PostMapping;
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
    public static final String ATTRIBUTES = "attributes";
    public static final String FACILITY_ID = "facilityId";

    private final ProxyuserFacade facade;

    @Autowired
    public ProxyUserProtectedController(ProxyuserFacade facade) {
        this.facade = facade;
    }

    /**
     * Find user by logins provided by the external sources and get specified attributes.
     * If no attributes are specified, default set is fetched.
     *
     * EXAMPLE CURL:
     * curl --request GET \
     *   --url 'http://127.0.0.1/proxyapi/auth/proxy-user/findByExtLogins?\
     *   IdPIdentifier=aHR0cHM6Ly9sb2dpbi5jZXNuZXQuY3ovaWRwLw%3D%3D\
     *   &identifiers=id1&identifiers=id2\
     *   &fields=urn%3Aperun%3Auser%3Aattribute-def%3Adef%3Aattr1&fields=urn%3Aperun%3Auser%3Aattribute-def%3Adef%3Aattr2' \
     *   --header 'authorization: Basic auth' \
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
                                   @RequestParam(value = IDENTIFIERS) List<String> identifiers,
                                   @RequestParam(value = FIELDS, required = false) List<String> fields)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException,
            InvalidRequestParameterException
    {
        if (!StringUtils.hasText(idpIdentifier)) {
            throw new InvalidRequestParameterException("IdP identifier cannot be empty");
        } else if (identifiers == null || identifiers.isEmpty()) {
            throw new InvalidRequestParameterException("User identifiers cannot be empty");
        }
        String decodedIdpIdentifier = ControllerUtils.decodeUrlSafeBase64(idpIdentifier);
        return facade.findByExtLogins(decodedIdpIdentifier, identifiers, fields);
    }

    /**
     * Find user by logins provided by the external sources and get specified attributes.
     * If no attributes are specified, default set is fetched.
     *
     * EXAMPLE CURL:
     * curl --request POST --url http://localhost:8081/proxyapi/auth/proxy-user/findByExtLogins \
     *   --header 'authorization: Basic auth' \
     *   --header 'content-type: application/json' \
     *   --data '{
     *             "IdPIdentifier": "aksn64a6sdsgsd48s123",
     *             "identifiers": ["id1", "445348@muni.cz", "id2"],
     *             "fields": [
     *               "urn:perun:user:attribute-def:def:attr1",
     *               "urn:perun:user:attribute-def:def:attr2",
     *              ]
     *           }'
     *
     * @param body Request body. JSON containing fields:
     *             - fields: List of strings identifying attributes we want to fetch. OPTIONAL
     * @return Found user or NULL.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     * @throws EntityNotFoundException Thrown when no user has been found.
     * @throws InvalidRequestParameterException Thrown when passed parameters or body does not meet criteria.
     */
    @ResponseBody
    @PostMapping(value = "/findByExtLogins", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public UserDTO findByExtLogins(@RequestBody JsonNode body)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException,
            InvalidRequestParameterException
    {
        ControllerUtils.validateRequestBody(body);
        String idpIdentifier = ControllerUtils.extractRequiredString(body, IDP_IDENTIFIER);
        if (!StringUtils.hasText(idpIdentifier)) {
            throw new InvalidRequestParameterException("IdP identifier cannot be empty");
        }
        String decodedIdpIdentifier = ControllerUtils.decodeUrlSafeBase64(idpIdentifier);

        List<String> identifiers = ControllerUtils.extractRequiredListOfStrings(body, IDENTIFIERS);
        if (identifiers.isEmpty()) {
            throw new InvalidRequestParameterException("User identifiers cannot be empty");
        }
        List<String> fields = ControllerUtils.extractFieldsFromBody(body, FIELDS);

        return facade.findByExtLogins(decodedIdpIdentifier, identifiers, fields);
    }

    /**
     * Find user by given source IdP entityId and additional source identifiers.
     * !!!! Works only with LDAP adapter !!!!
     *
     * EXAMPLE CURL:
     * curl --request GET \
     *   --url http://127.0.0.1/proxyapi/auth/proxy-user/findByIdentifiers?IdPIdentifier=IDP1 \
     *   &identifiers=ID1&identifiers=ID2\
     *   &fields=urn%3Aperun%3Auser%3Aattribute-def%3Adef%3Aattr1&fields=urn%3Aperun%3Auser%3Aattribute-def%3Adef%3Aattr2' \
     *   --header 'authorization: Basic auth' \
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
                                     @RequestParam(value = IDENTIFIERS) List<String> identifiers,
                                     @RequestParam(value = FIELDS, required = false) List<String> fields)
            throws EntityNotFoundException, InvalidRequestParameterException
    {
        if (!StringUtils.hasText(idpIdentifier)) {
            throw new InvalidRequestParameterException("IdP identifier cannot be empty");
        } else if (identifiers == null || identifiers.isEmpty()) {
            throw new InvalidRequestParameterException("User identifiers cannot be empty");
        }
        String decodedIdpIdentifier = ControllerUtils.decodeUrlSafeBase64(idpIdentifier);
        return facade.findByIdentifiers(decodedIdpIdentifier, identifiers, fields);
    }

    /**
     * Find user by given source IdP entityId and additional source identifiers.
     * !!!! Works only with LDAP adapter !!!!
     *
     * EXAMPLE CURL:
     * curl --request POST --url http://localhost:8081/proxyapi/auth/proxy-user/findByIdentifiers \
     *   --header 'authorization: Basic auth' \
     *   --header 'content-type: application/json' \
     *   --data '{
     *             "IdPIdentifier": "aksn64a6sdsgsd48s123",
     *             "identifiers": ["id1", "445348@muni.cz", "id2"],
     *             "fields": [
     *               "urn:perun:user:attribute-def:def:attr1",
     *               "urn:perun:user:attribute-def:def:attr2",
     *              ]
     *           }'
     *
     * @param body Request body. JSON containing fields:
     *             - fields: List of strings identifying attributes we want to fetch. OPTIONAL
     * @return User or null.
     * @throws EntityNotFoundException Thrown when no user has been found.
     * @throws InvalidRequestParameterException Thrown when passed parameters or body does not meet criteria.
     */
    @ResponseBody
    @PostMapping(value = "/findByIdentifiers", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public UserDTO findByIdentifiers(@RequestBody JsonNode body)
            throws EntityNotFoundException, InvalidRequestParameterException
    {
        ControllerUtils.validateRequestBody(body);
        String idpIdentifier = ControllerUtils.extractRequiredString(body, IDP_IDENTIFIER);
        if (!StringUtils.hasText(idpIdentifier)) {
            throw new InvalidRequestParameterException("IdP identifier cannot be empty");
        }
        String decodedIdpIdentifier = ControllerUtils.decodeUrlSafeBase64(idpIdentifier);
        List<String> identifiers = ControllerUtils.extractRequiredListOfStrings(body, IDENTIFIERS);
        if (identifiers.isEmpty()) {
            throw new InvalidRequestParameterException("User identifiers cannot be empty");
        }
        List<String> fields = ControllerUtils.extractFieldsFromBody(body, FIELDS);

        return facade.findByIdentifiers(decodedIdpIdentifier, identifiers, fields);
    }

    /**
     * Get Perun user by login with specified attributes. If no attributes are specified, default set is fetched.
     *
     * EXAMPLE CURL:
     * curl --request GET --url http://127.0.0.1/proxyapi/auth/proxy-user/login@somewhere.org?\
     *   fields=urn%3Aperun%3Auser%3Aattribute-def%3Adef%3Aattr1&fields=urn%3Aperun%3Auser%3Aattribute-def%3Adef%3Aattr2' \
     *   --header 'authorization: Basic auth'
     *
     * @param login Login of the user.
     * @param fields List of attributes to be fetched.
     * @return JSON representation of the User object.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     * @throws EntityNotFoundException Thrown when no user has been found.
     * @throws InvalidRequestParameterException Thrown when passed parameters or body does not meet criteria.
     */
    @ResponseBody
    @GetMapping(value = "/{login}", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserDTO getUserByLogin(@PathVariable(value = LOGIN) String login,
                                  @RequestParam(value = FIELDS, required = false) List<String> fields)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException,
            InvalidRequestParameterException
    {
        if (!StringUtils.hasText(login)) {
            throw new InvalidRequestParameterException("IdP identifier cannot be empty");
        }
        return facade.getUserByLogin(login, fields);
    }

    /**
     * Get Perun user by login with specified attributes. If no attributes are specified, default set is fetched.
     *
     * EXAMPLE CURL:
     * curl --request POST \
     *   --url http://127.0.0.1/proxyapi/auth/proxy-user/login@somewhere.org \
     *   --header 'content-type: application/json' \
     *   --data '{
     *             "fields": [
     *               "urn:perun:user:attribute-def:def:attr1",
     *               "urn:perun:user:attribute-def:def:attr2",
     *               "urn:perun:user:attribute-def:def:attr3"
     *             ]
     *          }'
     *
     * @param login Login of the user.
     * @param body Request body. JSON containing fields:
     *             - fields: List of strings identifying attributes we want to fetch. OPTIONAL
     * @return JSON representation of the User object.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     * @throws EntityNotFoundException Thrown when no user has been found.
     * @throws InvalidRequestParameterException Thrown when passed parameters or body does not meet criteria.
     */
    @ResponseBody
    @PostMapping(value = "/{login}", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserDTO getUserByLogin(@PathVariable(value = LOGIN) String login, @RequestBody JsonNode body)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException,
            InvalidRequestParameterException
    {
        if (!StringUtils.hasText(login)) {
            throw new InvalidRequestParameterException("IdP identifier cannot be empty");
        }
        List<String> fields = ControllerUtils.extractFieldsFromBody(body, FIELDS);
        return facade.getUserByLogin(login, fields);
    }

    /**
     * Find Perun user by id and get specified attributes. If no attributes are specified, default set is fetched.
     *
     * EXAMPLE CURL:
     * curl --request GET --url 'http://127.0.0.1/proxyapi/auth/proxy-user/findByPerunUserId?userId=12345\
     *   &fields=urn%3Aperun%3Auser%3Aattribute-def%3Adef%3Aattr1&fields=urn%3Aperun%3Auser%3Aattribute-def%3Adef%3Aattr2' \
     *   --header 'authorization: Basic auth' \
     *
     * @param userId Id of a Perun user.
     * @param fields List of attributes we want to fetch.
     * @return JSON representation of the User object.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     * @throws EntityNotFoundException Thrown when no user has been found.
     * @throws InvalidRequestParameterException Thrown when passed parameters or body does not meet criteria.
     */
    @ResponseBody
    @GetMapping(value = "/findByPerunUserId", produces = APPLICATION_JSON_VALUE)
    public UserDTO findByPerunUserId(@RequestParam(value = USER_ID) Long userId,
                                     @RequestParam(value = FIELDS, required = false) List<String> fields)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException,
            InvalidRequestParameterException
    {
        if (userId == null) {
            throw new InvalidRequestParameterException("User ID cannot be null");
        }
        return facade.findByPerunUserId(userId, fields);
    }

    /**
     * Find Perun user by id and get specified attributes. If no attributes are specified, default set is fetched.
     *
     * EXAMPLE CURL:
     * curl --request GET --url 'http://127.0.0.1/proxyapi/auth/proxy-user/findByPerunUserId?userId=12345'
     * --header 'authorization: Basic auth'
     *
     * @param body Request body. JSON containing fields:
     *             - userId: Id of user in Perun. REQUIRED
     *             - fields: List of strings identifying attributes we want to fetch. OPTIONAL
     * @return JSON representation of the User object.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     * @throws EntityNotFoundException Thrown when no user has been found.
     * @throws InvalidRequestParameterException Thrown when passed parameters or body does not meet criteria.
     */
    @ResponseBody
    @PostMapping(value = "/findByPerunUserId", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    public UserDTO findByPerunUserId(@RequestBody JsonNode body)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException,
            InvalidRequestParameterException
    {
        Long userId = ControllerUtils.extractRequiredLong(body, USER_ID);
        List<String> fields = ControllerUtils.extractFieldsFromBody(body, FIELDS);
        return facade.findByPerunUserId(userId, fields);
    }

    /**
     * Get all entitlements for user with given login.
     *
     * EXAMPLE CURL:
     * curl --request GET --url 'http://127.0.0.1/proxyapi/auth/proxy-user/login@somewhere.org/entitlements
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
     * Update attributes of the external source.
     *
     * curl --request PUT \
     *   --url http://127.0.0.1/proxyapi/auth/proxy-user/{login}/identity/{IdPIdentifier} \
     *   --header 'authorization: Basic auth' \
     *   --header 'content-type: application/json' \
     *   --data '{
     *             "attributes": {
     *               "attr1": "val1",
     *               "attr2": ["af1@smwh.com", "af2@smwh.com"]
     *             }
     *           }'
     *
     * @param login Login of the User.
     * @param identityId Base64 URL safe encoded Identifier of the Identity Provider.
     * @param body Request body. JSON containing fields:
     *             - attributes: JSON object containing attribute names and values to be updated. REQUIRED
     * @return true if the attributes were updated properly, false otherwise
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    @ResponseBody
    @PutMapping(value = "/{login}/identity/{IdPIdentifier}",
            produces = APPLICATION_JSON_VALUE,
            consumes = APPLICATION_JSON_VALUE)
    public boolean updateUserIdentityAttributes(@PathVariable(value = LOGIN) String login,
                                                @PathVariable(value = IDP_IDENTIFIER) String identityId,
                                                @RequestBody JsonNode body)
            throws PerunUnknownException, PerunConnectionException, InvalidRequestParameterException
    {
        if (body == null || !body.hasNonNull(ATTRIBUTES)) {
            throw new InvalidRequestParameterException("The request body cannot be null and must contain attributes");
        } else if (!StringUtils.hasText(login)) {
            throw new InvalidRequestParameterException("Users login cannot be empty");
        } else if (!StringUtils.hasText(identityId)) {
            throw new InvalidRequestParameterException("identityId cannot be empty");
        }
        String decodedIdentityIdentifier = ControllerUtils.decodeUrlSafeBase64(identityId);
        ObjectNode jsonAttributes = (ObjectNode) body.get(ATTRIBUTES);
        Map<String, JsonNode> attributes = new HashMap<>();
        Iterator<String> it = jsonAttributes.fieldNames();
        while (it.hasNext()) {
            String fieldName = it.next();
            attributes.put(fieldName, jsonAttributes.get(fieldName));
        }
        return facade.updateUserIdentityAttributes(login, decodedIdentityIdentifier, attributes);
    }

    /**
     * Get all GA4GH Visas and Passports for user with given Perun ID.
     *
     * EXAMPLE CURL:
     * curl --request GET --url 'http://127.0.0.1:8081/proxyapi/auth/proxy-user/ga4gh?userId=12345
     * --header 'authorization: Basic auth'
     *
     * @param userId Id of user in Perun
     * @return List of all GA4GH Passports and Visas.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     * @throws EntityNotFoundException Thrown when no user has been found.
     * @throws InvalidRequestParameterException Thrown when passed parameters or body does not meet criteria.
     */
    @ResponseBody
    @GetMapping(value = "/ga4gh", produces = APPLICATION_JSON_VALUE)
    public JsonNode ga4ghById(@RequestParam(value = USER_ID) Long userId)
            throws PerunConnectionException, PerunUnknownException, EntityNotFoundException,
            InvalidRequestParameterException
    {
        if (userId == null || userId <= 0) {
            throw new InvalidRequestParameterException("Invalid ID for user");
        }
        return facade.ga4ghById(userId);
    }

    /**
     * Get all GA4GH Visas and Passports for user with given login.
     *
     * EXAMPLE CURL:
     * curl --request GET --url 'http://127.0.0.1:8081/proxyapi/auth/proxy-user/ga4gh?userId=12345
     * --header 'authorization: Basic auth'
     *
     * @param login Users login
     * @return List of all GA4GH Passports and Visas.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     * @throws EntityNotFoundException Thrown when no user has been found.
     * @throws InvalidRequestParameterException Thrown when passed parameters or body does not meet criteria.
     */
    @ResponseBody
    @GetMapping(value = "/{login}/ga4gh", produces = APPLICATION_JSON_VALUE)
    public JsonNode ga4ghByLogin(@PathVariable(value = LOGIN) String login)
            throws PerunConnectionException, PerunUnknownException, EntityNotFoundException,
            InvalidRequestParameterException
    {
        if (!StringUtils.hasText(login)) {
            throw new InvalidRequestParameterException("Users login cannot be empty");
        }
        return facade.ga4ghByLogin(login);
    }

}
