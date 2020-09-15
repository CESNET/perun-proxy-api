package cz.muni.ics.perunproxyapi.application.facade;

import cz.muni.ics.perunproxyapi.persistence.exceptions.EntityNotFoundException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import lombok.NonNull;

import java.util.List;

/**
 * Facade for RP related things. Purpose of this class is to execute correct lower-level methods
 * to achieve the desired results.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 * @author Ondrej Ernst <oernst1@gmail.com>
 */
public interface RelyingPartyFacade {

    /**
     * Get entitlements based on the service user is trying to access.
     *
     * @param rpIdentifier Identifier of the RP.
     * @param login Login of the user
     * @return List of AARC formatted entitlements (filled or empty).
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     * @throws EntityNotFoundException Thrown when no user has been found.
     */
    List<String> getEntitlements(@NonNull String rpIdentifier, @NonNull String login)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException;

}
