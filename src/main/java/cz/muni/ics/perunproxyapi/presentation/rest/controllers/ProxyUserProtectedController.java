package cz.muni.ics.perunproxyapi.presentation.rest.controllers;

import cz.muni.ics.perunproxyapi.application.facade.impl.ProxyuserFacadeImpl;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.RpcConnectionException;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import cz.muni.ics.perunproxyapi.presentation.DTOModels.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cz.muni.ics.perunproxyapi.presentation.rest.config.PathConstants.AUTH_PATH;
import static cz.muni.ics.perunproxyapi.presentation.rest.config.PathConstants.PROXY_USER;
import static cz.muni.ics.perunproxyapi.presentation.rest.controllers.ParameterConstants.PARAM_FIELDS;
import static cz.muni.ics.perunproxyapi.presentation.rest.controllers.ParameterConstants.PARAM_IDENTIFIERS;
import static cz.muni.ics.perunproxyapi.presentation.rest.controllers.ParameterConstants.PARAM_IDP_IDENTIFIER;
import static cz.muni.ics.perunproxyapi.presentation.rest.controllers.ParameterConstants.PARAM_LOGIN;

/**
 * Controller containing methods related to proxy user. Basic Auth is required.
 * methods path: /CONTEXT_PATH/auth/proxy-user/**

 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@RestController
@RequestMapping(value = AUTH_PATH + PROXY_USER)
@Slf4j
public class ProxyUserProtectedController {

    private final ProxyuserFacadeImpl facade;

    @Autowired
    public ProxyUserProtectedController(ProxyuserFacadeImpl facade) {
        this.facade = facade;
    }

    @ResponseBody
    @RequestMapping(value = "/findByExtLogins", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public User findByExtLogins(@RequestParam(value = PARAM_IDP_IDENTIFIER) String idpIdentifier,
                                @RequestParam(value = PARAM_IDENTIFIERS) List<String> identifiers) throws PerunUnknownException, RpcConnectionException {
        return facade.findByExtLogins(idpIdentifier, identifiers);
    }

    @RequestMapping(value = "/{login}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public UserDTO getUserByLogin(@PathVariable(value = PARAM_LOGIN) String login,
                                  @RequestParam(value = PARAM_FIELDS) List<String> fields) throws PerunUnknownException, RpcConnectionException {
        return facade.getUserByLogin(login, fields);
    }

}
