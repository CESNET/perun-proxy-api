package cz.muni.ics.perunproxyapi.presentation.gui.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.perunproxyapi.application.facade.GuiFacade;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.listOfServices.ServicesDataHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static cz.muni.ics.perunproxyapi.presentation.rest.config.PathConstants.GUI;
import static cz.muni.ics.perunproxyapi.presentation.rest.config.PathConstants.NO_AUTH_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller containing methods related to GUI
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>
 */
@Controller
@RequestMapping(value = NO_AUTH_PATH + GUI)
@Slf4j
public class ListOfSpsController {

    private final GuiFacade facade;

    public static final String LIST_OF_SPS = "list_of_sps";


    @Autowired
    public ListOfSpsController(GuiFacade facade) {
        this.facade = facade;
    }


    /**
     * Show a template with the list of services
     *
     * @return a tek-pate with the list of services
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     * @throws IOException Thrown when error occurs during reading a file.
     */
    @GetMapping(value = "/services")
    public ModelAndView getListOfSps() throws PerunUnknownException, PerunConnectionException, IOException {
        ServicesDataHolder listOfSps = facade.getListOfSps();

        ObjectMapper mapper = new ObjectMapper();

        ModelAndView mav = new ModelAndView(LIST_OF_SPS);
        mav.addObject("statistics", listOfSps.getStatistics());
        mav.addObject("statisticsJson", mapper.convertValue(listOfSps.getStatistics(), JsonNode.class));
        mav.addObject("services", listOfSps.getServices());
        mav.addObject("attributesToShow", listOfSps.getAttributesToShow());
        mav.addObject("multilingualAttributes", listOfSps.getMultilingualAttributes());
        mav.addObject("urlAttributes", listOfSps.getUrlAttributes());

        facade.addHeaderAndFooter(mav);

        return mav;
    }

    /**
     * Returns the list of services as JSON.
     *
     * @return list of services as JSON
     * @throws IOException Thrown when error occurs during reading a file.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    @ResponseBody
    @GetMapping(value = "/services/json", produces = APPLICATION_JSON_VALUE)
    public Map<String, JsonNode> getListOfSpsJson() throws IOException, PerunUnknownException, PerunConnectionException {
        ServicesDataHolder services = facade.getListOfSps();
        ObjectMapper mapper = new ObjectMapper();

        Map<String, JsonNode> propertiesToShow = new HashMap<>();
        propertiesToShow.put("statistics", mapper.valueToTree(services.getStatistics()));
        propertiesToShow.put("services", mapper.valueToTree(services.getServicesJson()));

        return propertiesToShow;
    }
}
