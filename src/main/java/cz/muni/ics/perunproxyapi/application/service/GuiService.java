package cz.muni.ics.perunproxyapi.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import cz.muni.ics.perunproxyapi.application.facade.parameters.ListOfServicesGuiParams;
import cz.muni.ics.perunproxyapi.application.facade.parameters.ListOfServicesJsonParams;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.listOfServices.ListOfServicesDAO;
import lombok.NonNull;

/**
 * Service layer for GUI related things.
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>
 */
public interface GuiService {

    /**
     * Gets all data needed for the list of services.
     *
     * @param params Parameters for List of Services.
     * @return Gets all data needed for the list of services.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    ListOfServicesDAO getListOfSps(@NonNull ListOfServicesGuiParams params)
            throws PerunUnknownException, PerunConnectionException;

    /**
     * Gets all data needed for the list of services in JSON.
     *
     * @param params Parameters for List of Services.
     * @return Gets all data needed for the list of services.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */

    JsonNode getListOfSpsJson(@NonNull ListOfServicesJsonParams params)
            throws PerunUnknownException, PerunConnectionException;

}
