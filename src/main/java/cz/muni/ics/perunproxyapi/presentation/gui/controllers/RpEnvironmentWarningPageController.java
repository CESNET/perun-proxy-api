package cz.muni.ics.perunproxyapi.presentation.gui.controllers;

import cz.muni.ics.perunproxyapi.application.facade.GuiFacade;
import cz.muni.ics.perunproxyapi.persistence.exceptions.EntityNotFoundException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.InvalidRequestParameterException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.presentation.gui.GuiProperties;
import cz.muni.ics.perunproxyapi.presentation.gui.GuiUtils;
import cz.muni.ics.perunproxyapi.presentation.rest.controllers.ControllerUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static cz.muni.ics.perunproxyapi.presentation.rest.config.PathConstants.GUI;
import static cz.muni.ics.perunproxyapi.presentation.rest.config.PathConstants.NO_AUTH_PATH;

/**
 * Controller which is able to make a decision if it should show a warning page if
 * user is trying to access a service which is in STAGING or TESTING state.
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>
 */
@Controller
@RequestMapping(value = NO_AUTH_PATH)
@Slf4j
public class RpEnvironmentWarningPageController {

    public static final String RP_IDENTIFIER = "rpIdentifier";
    public static final String RETURN_URL = "returnUrl";
    public static final String TESTING = "TESTING";
    public static final String STAGING = "STAGING";
    public static final String RP_ENVIRONMENT_WARNING_PAGE = "rp_environment_warning_page";
    public static final String REDIRECT = "redirect:";
    public static final String RP_ENVIRONMENT_VALUE = "rpEnvironmentValue";

    @NonNull private final GuiFacade facade;
    @NonNull private final GuiProperties guiProperties;

    @Autowired
    public RpEnvironmentWarningPageController(@NonNull GuiFacade facade,
                                              @NonNull GuiProperties guiProperties)
    {
        this.facade = facade;
        this.guiProperties = guiProperties;
    }

    /**
     * Evaluates if warning page should be shown and if so, do it.
     *
     * @param rpIdentifier RP identifier URL SAFE BASE64 encoded
     * @param returnUrl URL where user should be redirected after click on the "Continue" button
     * @return A template with warning page if should be shown, redirects otherwise
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     * @throws InvalidRequestParameterException Thrown when passed request parameters do not meet criteria.
     */
    @ResponseBody
    @GetMapping(value = GUI + "/warningPage")
    public ModelAndView rpEnvironmentWarningPage(@RequestParam(value = RP_IDENTIFIER) String rpIdentifier,
                                                 @RequestParam(value = RETURN_URL) String returnUrl)
            throws PerunUnknownException, PerunConnectionException, InvalidRequestParameterException,
            EntityNotFoundException
    {
        if (!StringUtils.hasText(rpIdentifier)) {
            throw new InvalidRequestParameterException("Invalid RP identifier");
        } else if (!StringUtils.hasText(returnUrl)) {
            throw new InvalidRequestParameterException("No URL for return provided");
        }

        String decodedRpIdentifier = ControllerUtils.decodeUrlSafeBase64(rpIdentifier);
        String rpEnvironmentValue = facade.getRpEnvironmentValue(decodedRpIdentifier);
        ModelAndView mav;

        if (rpEnvironmentValue.equals(TESTING) || rpEnvironmentValue.equals(STAGING)) {
            mav = new ModelAndView(RP_ENVIRONMENT_WARNING_PAGE);
            mav.addObject(RETURN_URL, URLDecoder.decode(returnUrl, StandardCharsets.UTF_8));
            mav.addObject(RP_ENVIRONMENT_VALUE, rpEnvironmentValue);
            return GuiUtils.addCommonGuiOptions(mav, guiProperties);
        } else {
            return new ModelAndView(REDIRECT + returnUrl);
        }
    }

}
