package cz.muni.ics.perunproxyapi.presentation.rest.controllers;

import cz.muni.ics.perunproxyapi.application.facade.GuiFacade;
import cz.muni.ics.perunproxyapi.persistence.exceptions.InvalidRequestParameterException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.management.InvalidAttributeValueException;

import static cz.muni.ics.perunproxyapi.presentation.rest.config.PathConstants.NO_AUTH_PATH;
import static cz.muni.ics.perunproxyapi.presentation.rest.config.PathConstants.RP_ENVIRONMENT;

@RestController
@RequestMapping(value = NO_AUTH_PATH + RP_ENVIRONMENT)
@Slf4j
public class GetRpEnvironmentController {

    private final GuiFacade facade;

    public static final String FACILITY_ID = "facilityId";


    @Autowired
    public GetRpEnvironmentController(GuiFacade facade) {
        this.facade = facade;
    }


    @ResponseBody
    @GetMapping(value = "/getRpEnvironment")
    public String getRpEnvironment(@RequestParam(value = FACILITY_ID) Long facilityId)
            throws PerunUnknownException, PerunConnectionException, InvalidRequestParameterException, InvalidAttributeValueException {
        if (facilityId == null || facilityId <= 0) {
            throw new InvalidRequestParameterException("Invalid ID for facility");
        }

        return facade.getRpEnvironmentValue(facilityId);
    }

}
