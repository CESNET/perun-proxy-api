package cz.muni.ics.perunproxyapi.presentation.rest.controllers;

import cz.muni.ics.perunproxyapi.application.facade.ProxyuserFacade;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import cz.muni.ics.perunproxyapi.presentation.DTOModels.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    private final ProxyuserFacade facade;

    public static final String PARAM_IDP_IDENTIFIER = "IdPIdentifier";
    public static final String PARAM_IDENTIFIERS = "identifiers";
    public static final String PARAM_LOGIN = "login";
    public static final String PARAM_FIELDS = "fields";
    public static final String PARAM_USER_ID = "userId";

    @Autowired
    public ProxyUserProtectedController(ProxyuserFacade facade) {
        this.facade = facade;
    }

    @ResponseBody
    @RequestMapping(value = "/findByExtLogins", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    public User findByExtLogins(@RequestParam(value = PARAM_IDP_IDENTIFIER) String idpIdentifier,
                                @RequestParam(value = PARAM_IDENTIFIERS) List<String> identifiers)
            throws PerunUnknownException, PerunConnectionException
    {
        return facade.findByExtLogins(idpIdentifier, identifiers);
    }

    /**
     * Get Perun user by login. <br>
     * <br>
     * <b>EXAMPLE CURL:</b>
     * <br>
     * curl --request GET \
     *   --url http://localhost:8081/proxyapi/auth/proxy-user/example_login_value@einfra.cesnet.cz \
     *   --header 'authorization: Basic auth'
     *
     * @param login Login attribute to be used. Must be unique.
     * @param fields OPTIONAL attributes for the user we want to obtain
     * @return JSON representation of the User object.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    @GetMapping(value = "/{login}", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserDTO getUserByLogin(@PathVariable(value = PARAM_LOGIN) String login,
                                  @RequestParam(required = false, value = PARAM_FIELDS) List<String> fields)
            throws PerunUnknownException, PerunConnectionException
    {
        return facade.getUserByLogin(login, fields);
    }

    /**
     * Find Perun user by its id.<br>
     * <br>
     * <b>EXAMPLE CURL:</b>
     * <br>
     * curl --request GET --url 'http://127.0.0.1:8081/proxyapi/auth/proxy-user/findByPerunUserId?userId=12345'
     * --header 'authorization: Basic auth'
     *
     * @param userId Id of a Perun user.
     * @return JSON representation of the User object.
     */
    @RequestMapping(value = "/findByPerunUserId", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    public User findByPerunUserId(@RequestParam(value = PARAM_USER_ID) long userId)
            throws PerunUnknownException, PerunConnectionException
    {
        return facade.findByPerunUserId(userId);
    }

    @GetMapping(value = "/{login}/entitlements", produces = APPLICATION_JSON_VALUE)
    public List<String> getUserEntitlements(@PathVariable(value = PARAM_LOGIN) String login)
            throws PerunUnknownException, PerunConnectionException
    {
        return facade.getAllEntitlements(login);
    }

}
