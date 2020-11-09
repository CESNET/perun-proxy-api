package cz.muni.ics.perunproxyapi.application.facade;

import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.listOfServices.ServicesDataHolder;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

/**
 * Facade for GUI related things.
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>
 */
public interface GuiFacade {

    /**
     * Gets all data needed for the list of services.
     *
     * @return An object containing all data needed for the list of services.
     * @throws IOException Thrown when error occurs during reading a file.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    ServicesDataHolder getListOfSps() throws IOException, PerunUnknownException, PerunConnectionException;

    /**
     * Adds path to header and footer to the given ModelAndView object
     *
     * @param mav ModelAndView to be filled
     * @return ModelAndView with added paths to header and footer
     */
    ModelAndView addHeaderAndFooter(ModelAndView mav);

}
