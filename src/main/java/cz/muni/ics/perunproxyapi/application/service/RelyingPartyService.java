package cz.muni.ics.perunproxyapi.application.service;

import cz.muni.ics.perunproxyapi.persistence.adapters.DataAdapter;
import cz.muni.ics.perunproxyapi.persistence.exceptions.EntityNotFoundException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.Facility;
import lombok.NonNull;

import java.util.List;

/**
 * Service layer for RP related things. Purpose of this class is to execute correct methods on the given adapter.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 * @author Ondrej Ernst <oernst1@gmail.com>
 */
public interface RelyingPartyService {

    /**
     * Get entitlements based on the service user is trying to access.
     *
     * @param adapter Adapter to be used.
     * @param facilityId Id of the facility representing the service.
     * @param userId Id of the user
     * @param prefix Prefix to be prepended.
     * @param authority Authority issuing the entitlements.
     * @param forwardedEntitlementsAttrIdentifier Identifier of the attribute containing forwarded entitlements.
     * @param resourceCapabilitiesAttrIdentifier Identifier of the attribute containing resource capabilities.
     * @param facilityCapabilitiesAttrIdentifier Identifier of the attribute containing facility capabilities.
     * @return List of AARC formatted entitlements (filled or empty).
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    List<String> getEntitlements(@NonNull DataAdapter adapter, @NonNull Long facilityId,
                                 @NonNull Long userId, @NonNull String prefix, @NonNull String authority,
                                 String forwardedEntitlementsAttrIdentifier,
                                 String resourceCapabilitiesAttrIdentifier,
                                 String facilityCapabilitiesAttrIdentifier)
            throws PerunUnknownException, PerunConnectionException;

    /**
     * Get facility by identifier.
     *
     * @param adapter Adapter to be used.
     * @param rpIdentifier Identifier of the RP (ClientID or EntityID).
     * @return Facility representing service or NULL.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    Facility getFacilityByIdentifier(@NonNull DataAdapter adapter, @NonNull String rpIdentifier)
            throws PerunUnknownException, PerunConnectionException;

    /**
     * Check if user has access to the service.
     *
     * @param adapter Adapter to be used
     * @param facilityId Facility id.
     * @param userId User id.
     * @param testVoIds Ids of test VOs.
     * @param prodVoIds Ids of production VOs.
     * @param checkGroupMembershipAttrIdentifier Identifier for the checkGroupMembership attribute.
     * @param isTestSpIdentifier Identifier for the isTestSp attribute.
     * @return TRUE if user has access to service, otherwise FALSE.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    boolean hasAccessToService(@NonNull DataAdapter adapter,
                               @NonNull Long facilityId,
                               @NonNull Long userId,
                               @NonNull List<Long> testVoIds,
                               @NonNull List<Long> prodVoIds,
                               @NonNull String checkGroupMembershipAttrIdentifier,
                               @NonNull String isTestSpIdentifier)
            throws PerunUnknownException, PerunConnectionException;

    /**
     * Get environment for RP
     * @param rpIdentifier RP identifier
     * @param adapter Adapter to be used.
     * @param rpEnvAttr Attribute in which the RP env is stored.
     * @return Environment.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     * @throws EntityNotFoundException Throw when no RP has been found for given identifier.
     */
    String getRpEnvironmentValue(@NonNull String rpIdentifier, @NonNull DataAdapter adapter,
                                 @NonNull String rpEnvAttr)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException;

}
