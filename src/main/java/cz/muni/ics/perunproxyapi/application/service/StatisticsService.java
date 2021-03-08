package cz.muni.ics.perunproxyapi.application.service;

import cz.muni.ics.perunproxyapi.application.service.models.StatsRawData;
import cz.muni.ics.perunproxyapi.persistence.exceptions.EntityNotFoundException;
import lombok.NonNull;

/**
 * Service layer for STATISTICS related things. Purpose of this class is to execute correct methods on the given adapter.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public interface StatisticsService {

    /**
     * Log statistics about login into corresponding table
     * @param userId User ID
     * @param rpIdentifier RP identifier
     * @param rpName Name of the Rp
     * @param idpEntityId ID of the IDP entity
     * @param idpName Name of the IDP
     * @return TRUE if data were inserted into the table, otherwise FALSE.
     */
    boolean logStatistics(@NonNull String userId,
                          @NonNull String rpIdentifier,
                          @NonNull String rpName,
                          @NonNull String idpEntityId,
                          @NonNull String idpName);

    /**
     * Get overall statistics. Includes number of logins per day and data for pie charts of distribution of the
     * logins across RPs and IdPs.
     * @return Entries from DB or NULL if some DB operation has failed.
     */
    StatsRawData getOverallStatistics();

    /**
     * Get overall statistics for RPs only. Includes data for pie chart of distribution of the
     * logins across RPs. Fields for other entities are empty lists.
     * @return Entries from DB or NULL if some DB operation has failed.
     */
    StatsRawData getOverallRpStatistics();

    /**
     * Get overall statistics for IdPs only. Includes data for pie chart of distribution of the
     * logins across IdPs. Fields for other entities are empty lists.
     * @return Entries from DB or NULL if some DB operation has failed.
     */
    StatsRawData getOverallIdpStatistics();

    /**
     * Get statistics for specific RP. Includes number of logins per day and data for pie chart of distribution
     * of the logins across IdPs.
     * @param rpIdentifier Identifier of specific Relying Party.
     * @return Entries from DB or NULL if some DB operation has failed.
     * @throws EntityNotFoundException Throw when no Relying Party has been found for given identifier.
     */
    StatsRawData getRpStatistics(@NonNull String rpIdentifier) throws EntityNotFoundException;

    /**
     * Get statistics for specific IdP. Includes number of logins per day and data for pie chart of distribution
     * of the logins across RPs.
     * @param idpIdentifier Identifier of specific Identity Provider.
     * @return Entries from DB or NULL if some DB operation has failed.
     * @throws EntityNotFoundException Throw when no Identity Provider has been found for given identifier.
     */
    StatsRawData getIdpStatistics(@NonNull String idpIdentifier) throws EntityNotFoundException;

    /**
     * Extract displayable name for RP.
     * @param rpIdentifier Identifier of specific Relying Party.
     * @return String name of the RP from stats DB or empty string.
     * @throws EntityNotFoundException Throw when no Relying Party has been found for given identifier.
     */
    String getRpNameForIdentifier(@NonNull String rpIdentifier) throws EntityNotFoundException;

    /**
     * Extract displayable name for RP.
     * @param idpIdentifier Identifier of specific IdentityProvider
     * @return String name of the IdP from stats DB or empty string.
     * @throws EntityNotFoundException Throw when no Identity Provider has been found for given identifier.
     */
    String getIdpNameForIdentifier(@NonNull String idpIdentifier) throws EntityNotFoundException;

}
