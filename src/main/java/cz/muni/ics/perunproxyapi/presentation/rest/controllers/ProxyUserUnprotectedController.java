package cz.muni.ics.perunproxyapi.presentation.rest.controllers;

import cz.muni.ics.perunproxyapi.application.facade.ProxyuserFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import static cz.muni.ics.perunproxyapi.presentation.rest.config.PathConstants.NO_AUTH_PATH;
import static cz.muni.ics.perunproxyapi.presentation.rest.config.PathConstants.PROXY_USER;


/**
 * Controller containing methods related to proxy user. No auth is required.
 * methods path: /CONTEXT_PATH/non/proxy-user/**
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Controller
@RequestMapping(value = NO_AUTH_PATH + PROXY_USER)
@Slf4j
public class ProxyUserUnprotectedController {

}
