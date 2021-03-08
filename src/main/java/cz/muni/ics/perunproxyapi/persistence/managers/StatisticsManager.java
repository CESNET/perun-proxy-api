package cz.muni.ics.perunproxyapi.persistence.managers;

import cz.muni.ics.perunproxyapi.persistence.exceptions.DBOperationException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.EntityNotFoundException;
import cz.muni.ics.perunproxyapi.persistence.models.statistics.IdpSumEntry;
import cz.muni.ics.perunproxyapi.persistence.models.statistics.RpSumEntry;
import cz.muni.ics.perunproxyapi.persistence.models.statistics.LoginsPerDaySumEntry;

import java.util.List;

public interface StatisticsManager {

    /**
     * Insert entry for login
     * @param rpId ID of RP in DB
     * @param idpId ID of IdP in DB
     * @param login users login
     * @throws DBOperationException Thrown when operation in DB has failed.
     */
    void insertLogin(int rpId, int idpId, String login) throws DBOperationException;

    /**
     * Fetch RP_ID for RP specified by identifier (client_id or entity_id)
     * @param rpIdentifier Identifier of the RP
     * @return extracted ID from DB
     * @throws EntityNotFoundException Thrown when no entity exists for specified identifier.
     * @throws DBOperationException Thrown when operation in DB has failed.
     */
    int extractRpId(String rpIdentifier) throws EntityNotFoundException, DBOperationException;

    /**
     * Fetch IDP_ID for IdP specified by identifier (entity_id of the IdP)
     * @param idpEntityId Identifier of the IdP
     * @return extracted ID from DB
     * @throws EntityNotFoundException Thrown when no entity exists for specified identifier.
     * @throws DBOperationException Thrown when operation in DB has failed.
     */
    int extractIdpId(String idpEntityId) throws EntityNotFoundException, DBOperationException;

    /**
     * Insert or update mapping of the RP identifier to RP name
     * @param rpIdentifier identifier of the RP
     * @param rpName name of the RP
     * @throws DBOperationException Thrown when operation in DB has failed.
     */
    void insertRp(String rpIdentifier, String rpName) throws DBOperationException;

    /**
     * Insert or update mapping of the IdP identifier to IdP name
     * @param idpIdentifier identifier of the IdP
     * @param idpName name of the IdP
     * @throws DBOperationException Thrown when operation in DB has failed.
     */
    void insertIdp(String idpIdentifier, String idpName) throws DBOperationException;

    /**
     * Get summaries for user logins for each day.
     * @return List of entries ordered by date ASCENDING.
     * @throws DBOperationException Throw when operation in DB has failed.
     */
    List<LoginsPerDaySumEntry> getSummaryLogins() throws DBOperationException;

    /**
     * Get summaries for IDPs in total.
     * @return List of entries per IDP ordered by number of logins DESCENDING
     * @throws DBOperationException Throw when operation in DB has failed.
     */
    List<IdpSumEntry> getSummaryForIdps() throws DBOperationException;

    /**
     * Get summaries for RPs in total.
     * @return List of entries per RP ordered by number of logins DESCENDING
     * @throws DBOperationException Throw when operation in DB has failed.
     */
    List<RpSumEntry> getSummaryForRps() throws DBOperationException;

    /**
     * Get user logins for each day for particular RP.
     * @param rpId Id of the RP in DB.
     * @return List of entries ordered by date ASCENDING.
     * @throws DBOperationException Throw when operation in DB has failed.
     */
    List<LoginsPerDaySumEntry> getRpLogins(int rpId) throws DBOperationException;

    /**
     * Get summaries for IdPs for particular RP.
     * @param rpId Id of the RP in DB.
     * @return List of entries per IdP ordered by number of logins DESCENDING.
     * @throws DBOperationException Throw when operation in DB has failed.
     */
    List<IdpSumEntry> getIdpStatsForRp(int rpId) throws DBOperationException;

    /**
     * Get user logins for each day for particular IdP.
     * @param idpId Id of the IdP in DB.
     * @return List of entries ordered by date ASCENDING.
     * @throws DBOperationException Throw when operation in DB has failed.
     */
    List<LoginsPerDaySumEntry> getIdpLogins(int idpId) throws DBOperationException;

    /**
     * Get summaries for RPs for particular IdP.
     * @param idpId Id of the IdP in DB.
     * @return List of entries per RP ordered by number of logins DESCENDING.
     * @throws DBOperationException Throw when operation in DB has failed.
     */
    List<RpSumEntry> getRpStatsForIdp(int idpId) throws DBOperationException;

    /**
     * Extract displayable name from DB for RP with specific identifier.
     * @param rpIdentifier Identifier of the RP.
     * @return String name of the RP.
     * @throws DBOperationException Throw when operation in DB has failed.
     * @throws EntityNotFoundException Thrown when no RP has been found for given identifier.
     */
    String extractRpName(String rpIdentifier) throws DBOperationException, EntityNotFoundException;

    /**
     * Extract displayable name from DB for IdP with specific identifier.
     * @param idpIdentifier Identifier of the IdP.
     * @return String name of the IdP.
     * @throws DBOperationException Throw when operation in DB has failed.
     * @throws EntityNotFoundException Thrown when no IdP has been found for given identifier.
     */
    String extractIdpName(String idpIdentifier) throws DBOperationException, EntityNotFoundException;

}
