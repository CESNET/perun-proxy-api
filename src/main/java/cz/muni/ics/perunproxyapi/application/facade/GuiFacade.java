package cz.muni.ics.perunproxyapi.application.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import cz.muni.ics.perunproxyapi.persistence.exceptions.EntityNotFoundException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.listOfServices.ListOfServicesDAO;
import lombok.NonNull;

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
     * @throws JsonProcessingException Thrown when error occurs during reading a file.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    ListOfServicesDAO getListOfSps() throws JsonProcessingException, PerunUnknownException, PerunConnectionException;

    /**
     * Gets all data needed for the list of services in JSON format.
     *
     * @return An object containing all data needed for the list of services.
     * @throws JsonProcessingException Thrown when error occurs during reading a file.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    JsonNode getListOfSpsInJson() throws JsonProcessingException, PerunUnknownException, PerunConnectionException;

    /**
     * Get environment for RP
     * @param rpIdentifier Rp identifier.
     * @return Environment
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     * @throws EntityNotFoundException Throw when no RP has been found for given identifier.
     */
    String getRpEnvironmentValue(@NonNull String rpIdentifier)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException;

}
