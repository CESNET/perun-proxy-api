package cz.muni.ics.perunproxyapi.application.service;

import cz.muni.ics.perunproxyapi.application.facade.parameters.ServicesParams;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.listOfServices.ServicesDataHolder;
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
     * @param servicesParams Parameters for List of Services.
     * @return Gets all data needed for the list of services.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    ServicesDataHolder getListOfSps(@NonNull ServicesParams servicesParams)
            throws PerunUnknownException, PerunConnectionException;

}
