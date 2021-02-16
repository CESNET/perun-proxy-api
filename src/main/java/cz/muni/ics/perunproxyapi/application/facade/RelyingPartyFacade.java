package cz.muni.ics.perunproxyapi.application.facade;

import cz.muni.ics.perunproxyapi.persistence.exceptions.EntityNotFoundException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import lombok.NonNull;

import java.io.IOException;
import java.util.List;
import java.util.Set;

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
    Set<String> getEntitlements(@NonNull String rpIdentifier, @NonNull String login)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException;

    /**
     * Get extended entitlements based on the service user is trying to access.
     *
     * @param rpIdentifier Identifier of the RP.
     * @param login Login of the user
     * @return List of AARC formatted extended entitlements (filled or empty).
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     * @throws EntityNotFoundException Thrown when no user has been found.
     */
    Set<String> getEntitlementsExtended(@NonNull String rpIdentifier, @NonNull String login)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException;

    /**
     * Check if user has access to the service.
     *
     * @param rpIdentifier Identifier of the RP.
     * @param login Login of the user.
     * @return TRUE if user has access to service, otherwise FALSE.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     * @throws EntityNotFoundException Thrown when no user has been found.
     * @throws IOException Invalid I/O value occurred during conversion from JSON to list of long values.
     */
    boolean hasAccessToService(@NonNull String rpIdentifier,@NonNull String login)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException, IOException;

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
