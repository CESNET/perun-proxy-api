package cz.muni.ics.perunproxyapi.presentation.gui.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.muni.ics.perunproxyapi.application.facade.GuiFacade;
import cz.muni.ics.perunproxyapi.persistence.exceptions.InvalidRequestParameterException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.management.InvalidAttributeValueException;

import static cz.muni.ics.perunproxyapi.presentation.rest.config.PathConstants.GUI;
import static cz.muni.ics.perunproxyapi.presentation.rest.config.PathConstants.NO_AUTH_PATH;
import static cz.muni.ics.perunproxyapi.presentation.rest.config.PathConstants.RP_ENVIRONMENT;

/**
 * Controller which is able to make a decision if it should show a warning page if
 * user is trying to access a service which is in STAGING or TESTING state.
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>
 */
@Controller
@RequestMapping(value = NO_AUTH_PATH + GUI + RP_ENVIRONMENT)
@Slf4j
public class RpEnvironmentWarningPageController {

    private final GuiFacade facade;

    public static final String FACILITY_ID = "facilityId";
    public static final String SP_URL = "spUrl";
    public static final String TESTING = "TESTING";
    public static final String STAGING = "STAGING";
    public static final String RP_ENVIRONMENT_WARNING_PAGE = "rp_environment_warning_page";
    public static final String REDIRECT = "redirect:";
    public static final String RP_ENVIRONMENT_VALUE = "rpEnvironmentValue";


    @Autowired
    public RpEnvironmentWarningPageController(GuiFacade facade) {
        this.facade = facade;
    }


    /**
     * Evaluates if warning page should be shown and if so, do it.
     *
     * @param facilityId Id of facility user is trying to access
     * @param spUrl URL where user should be redirected after click on the "Continue" button
     * @return A template with warning page if should be shown, redirects otherwise
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     * @throws InvalidRequestParameterException Thrown when passed request parameters do not meet criteria.
     * @throws InvalidAttributeValueException Thrown when attribute value do not meet criteria.
     */
    @ResponseBody
    @GetMapping(value = "/warningPage")
    public ModelAndView rpEnvironmentWarningPage(@RequestParam(value = FACILITY_ID) Long facilityId, @RequestParam(value = SP_URL) String spUrl)
            throws PerunUnknownException, PerunConnectionException, InvalidRequestParameterException, InvalidAttributeValueException {
        if (facilityId == null || facilityId <= 0) {
            throw new InvalidRequestParameterException("Invalid ID for facility");
        }

        String rpEnvironmentValue = facade.getRpEnvironmentValue(facilityId);
        ModelAndView mav;

        if (rpEnvironmentValue.equals(TESTING) || rpEnvironmentValue.equals(STAGING)) {
            mav = new ModelAndView(RP_ENVIRONMENT_WARNING_PAGE);
            mav.addObject(SP_URL, spUrl);
            mav.addObject(RP_ENVIRONMENT_VALUE, rpEnvironmentValue);
        } else {
            mav = new ModelAndView(REDIRECT + spUrl);
        }

        facade.addHeaderAndFooter(mav);

        return mav;
    }

    /**
     * Finds value of rpEnvironment attribute of the given facility. Returns it as JSON.
     *
     * @param facilityId Id of facility user is trying to access
     * @return JSON value of rpEnvironment attribute
     * @throws InvalidRequestParameterException Thrown when passed request parameters do not meet criteria.
     * @throws InvalidAttributeValueException Thrown when attribute value do not meet criteria.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    @ResponseBody
    @GetMapping(value = "/json{facilityId}")
    public JsonNode rpEnvironmentJson(@RequestParam(value = FACILITY_ID) Long facilityId)
            throws InvalidRequestParameterException, InvalidAttributeValueException, PerunUnknownException, PerunConnectionException {
        if (facilityId == null || facilityId <= 0) {
            throw new InvalidRequestParameterException("Invalid ID for facility");
        }

        ObjectMapper mapper = new ObjectMapper();

        return mapper.valueToTree(facade.getRpEnvironmentValue(facilityId));
    }
}
