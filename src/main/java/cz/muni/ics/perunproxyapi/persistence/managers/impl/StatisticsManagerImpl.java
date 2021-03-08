package cz.muni.ics.perunproxyapi.persistence.managers.impl;

import cz.muni.ics.perunproxyapi.persistence.connectors.properties.StatisticsDbProperties;
import cz.muni.ics.perunproxyapi.persistence.exceptions.DBOperationException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.EntityNotFoundException;
import cz.muni.ics.perunproxyapi.persistence.managers.StatisticsManager;
import cz.muni.ics.perunproxyapi.persistence.mappers.IdpSumEntryMapper;
import cz.muni.ics.perunproxyapi.persistence.mappers.RpSumEntryMapper;
import cz.muni.ics.perunproxyapi.persistence.mappers.StatisticsSumEntryMapper;
import cz.muni.ics.perunproxyapi.persistence.models.statistics.IdpSumEntry;
import cz.muni.ics.perunproxyapi.persistence.models.statistics.RpSumEntry;
import cz.muni.ics.perunproxyapi.persistence.models.statistics.LoginsPerDaySumEntry;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class StatisticsManagerImpl implements StatisticsManager {

    private static final String RP = "RP";
    private static final String IDP = "IDP";

    private static final String PARAM_DATE = "date";
    private static final String PARAM_IDP_ID = "idpId";
    private static final String PARAM_RP_ID = "rpId";
    private static final String PARAM_USER = "user";
    private static final String PARAM_IDENTIFIER = "identifier";
    private static final String PARAM_NAME = "name";

    private final StatisticsSumEntryMapper statisticsSumsMapper = new StatisticsSumEntryMapper();
    private final IdpSumEntryMapper idpSumsMapper = new IdpSumEntryMapper();
    private final RpSumEntryMapper rpSumsMapper = new RpSumEntryMapper();

    private final StatisticsDbProperties dbProperties;
    private final NamedParameterJdbcTemplate template;

    @Autowired
    public StatisticsManagerImpl(StatisticsDbProperties dbProperties,
                                 @Qualifier("statisticsJdbcTemplate") NamedParameterJdbcTemplate template)
    {
        this.dbProperties = dbProperties;
        this.template = template;
    }

    @Override
    public void insertLogin(int rpId, int idpId, String login) throws DBOperationException {
        String queryBase = "INSERT INTO %s(day, idpId, spId, user, logins)" +
                " VALUES(:%s, :%s, :%s, :%s, '1') " +
                " ON DUPLICATE KEY UPDATE logins = logins + 1";
        String query = String.format(queryBase, dbProperties.getStatisticsTable(),
                PARAM_DATE, PARAM_IDP_ID, PARAM_RP_ID, PARAM_USER);

        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_DATE, LocalDate.now());
        params.put(PARAM_IDP_ID, idpId);
        params.put(PARAM_RP_ID, idpId);
        params.put(PARAM_USER, login);

        try {
            template.update(query, new MapSqlParameterSource(params));
        } catch (Exception e) {
            log.error("Caught exception when inserting login");
            log.debug("", e);
            throw new DBOperationException();
        }
    }

    @Override
    public int extractRpId(String rpIdentifier) throws EntityNotFoundException, DBOperationException {
        return extractId(rpIdentifier, dbProperties.getRpMapTable(), "spId", RP);
    }

    @Override
    public int extractIdpId(String idpIdentifier) throws EntityNotFoundException, DBOperationException {
        return extractId(idpIdentifier, dbProperties.getIdpMapTable(), "idpId", IDP);
    }

    @Override
    public void insertRp(String rpIdentifier, String rpName) throws DBOperationException {
        insertIntoMapTable(rpIdentifier, rpName, dbProperties.getRpMapTable(), RP);
    }

    @Override
    public void insertIdp(String idpIdentifier, String idpName) throws DBOperationException {
        insertIntoMapTable(idpIdentifier, idpName, dbProperties.getIdpMapTable(), IDP);
    }

    @Override
    public List<LoginsPerDaySumEntry> getSummaryLogins() throws DBOperationException {
        String whereClause = " WHERE spId = 0 AND idpId = 0 ";

        try {
            return getSummaryStats(whereClause, Collections.emptyMap());
        } catch (Exception e) {
            log.error("Caught exception when fetching statistics sums");
            log.debug("", e);
            throw new DBOperationException();
        }
    }

    @Override
    public List<IdpSumEntry> getSummaryForIdps() throws DBOperationException {
        try {
            return getSummaryForIdpOrRps(dbProperties.getIdpMapTable(), "", Collections.emptyMap(), idpSumsMapper);
        } catch (Exception e) {
            log.error("Caught exception when fetching IdP sums");
            log.debug("", e);
            throw new DBOperationException();
        }
    }

    @Override
    public List<RpSumEntry> getSummaryForRps() throws DBOperationException {
        try {
            return getSummaryForIdpOrRps(dbProperties.getRpMapTable(), "", Collections.emptyMap(), rpSumsMapper);
        } catch (Exception e) {
            log.error("Caught exception when fetching RP sums");
            log.debug("", e);
            throw new DBOperationException();
        }
    }

    @Override
    public List<LoginsPerDaySumEntry> getRpLogins(int rpId) throws DBOperationException {
        String whereClause = " WHERE spId = :" + PARAM_RP_ID + " AND idpId != 0 ";
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_RP_ID, rpId);

        try {
            return getSummaryStats(whereClause, params);
        } catch (Exception e) {
            log.error("Caught exception when fetching statistics sums for RP({})", rpId);
            log.debug("", e);
            throw new DBOperationException();
        }
    }

    @Override
    public List<IdpSumEntry> getIdpStatsForRp(int rpId) throws DBOperationException {
        String whereClause = " WHERE spId = :" + PARAM_RP_ID + ' ';
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_RP_ID, rpId);
        try {
            return getSummaryForIdpOrRps(dbProperties.getRpMapTable(), whereClause, params, idpSumsMapper);
        } catch (Exception e) {
            log.error("Caught exception when fetching RP sums");
            log.debug("", e);
            throw new DBOperationException();
        }
    }

    @Override
    public List<LoginsPerDaySumEntry> getIdpLogins(int idpId) throws DBOperationException {
        String whereClause = " WHERE spId != 0 AND idpId = :" + PARAM_IDP_ID;
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_IDP_ID, idpId);

        try {
            return getSummaryStats(whereClause, params);
        } catch (Exception e) {
            log.error("Caught exception when fetching statistics sums for IdP({})", idpId);
            log.debug("", e);
            throw new DBOperationException();
        }
    }

    @Override
    public List<RpSumEntry> getRpStatsForIdp(int idpId) throws DBOperationException {
        String whereClause = " WHERE idpId = :" + PARAM_IDP_ID + ' ';
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_IDP_ID, idpId);

        try {
            return getSummaryForIdpOrRps(dbProperties.getRpMapTable(), whereClause, params, rpSumsMapper);
        } catch (Exception e) {
            log.error("Caught exception when fetching RP sums");
            log.debug("", e);
            throw new DBOperationException();
        }
    }

    @Override
    public String extractRpName(@NonNull String rpIdentifier) throws DBOperationException, EntityNotFoundException {
        if (!StringUtils.hasText(rpIdentifier)) {
            throw new IllegalArgumentException("No RP identifier provided");
        }
        return extractSpOrIdpName(dbProperties.getRpMapTable(), rpIdentifier, RP);
    }

    @Override
    public String extractIdpName(@NonNull String idpIdentifier) throws DBOperationException, EntityNotFoundException {
        if (!StringUtils.hasText(idpIdentifier)) {
            throw new IllegalArgumentException("No IDP identifier provided");
        }
        return extractSpOrIdpName(dbProperties.getRpMapTable(), idpIdentifier, IDP);
    }

    private String extractSpOrIdpName(String table, String identifier, String type)
            throws DBOperationException, EntityNotFoundException
    {
        String queryBase = "SELECT name FROM %s WHERE identifier = :%s";
        String query = String.format(queryBase, table, PARAM_IDENTIFIER);
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_IDENTIFIER, identifier);
        try {
            return template.queryForObject(query, new MapSqlParameterSource(params), String.class);
        } catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundException("No entity found for given identifier");
        } catch (Exception e) {
            log.error("Caught exception when extracting identifier for {}({})", type, identifier);
            log.debug("", e);
            throw new DBOperationException();
        }
    }

    // private methods

    private List<LoginsPerDaySumEntry> getSummaryStats(String whereClause, Map<String, Object> params) {
        String queryBase = "SELECT id, year, month, day, COALESCE(logins, 0) AS logins, COALESCE(users, 0) AS users " +
                " FROM %s " +
                whereClause +
                " ORDER BY year ASC, month ASC, day ASC";
        String query = String.format(queryBase, dbProperties.getSumsTable());

        if (params != null && !params.isEmpty()) {
            return template.query(query, new MapSqlParameterSource(params), statisticsSumsMapper);
        } else {
            return template.query(query, statisticsSumsMapper);
        }
    }

    private <T> List<T> getSummaryForIdpOrRps(String mapTableName, String whereClause,
                                              Map<String, Object> params, RowMapper<T> mapper)
    {
        String queryBase = "SELECT SUM(COALESCE(logins, 0)) AS logins, identifier, name " +
                " FROM %s NATURAL JOIN %s " +
                whereClause +
                " GROUP BY name, identifier " +
                " ORDER BY logins DESC";
        String query = String.format(queryBase, dbProperties.getSumsTable(), mapTableName);
        if (params != null && !params.isEmpty()) {
            return template.query(query, params, mapper);
        } else {
            return template.query(query, mapper);
        }
    }

    private int extractId(String identifier, String table, String column, String type)
            throws EntityNotFoundException, DBOperationException
    {
        String queryBase = "SELECT %s FROM %s WHERE identifier = :%s";
        String query = String.format(queryBase, column, table, PARAM_IDENTIFIER);
        Map<String, Object> params = Collections.singletonMap(PARAM_IDENTIFIER, identifier);
        try {
            Integer result = template.queryForObject(query, new MapSqlParameterSource(params), Integer.class);
            if (result == null) {
                throw new EntityNotFoundException("No entry found for identifier");
            }
            return result;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Caught exception when fetching {} ID for identifier: {}", type, identifier);
            log.debug("", e);
            throw new DBOperationException();
        }
    }

    private void insertIntoMapTable(String identifier, String rpName, String table, String type)
            throws DBOperationException
    {
        String queryBase = "INSERT INTO %s(identifier, name)" +
                " VALUES (:%s, :%s) ON DUPLICATE KEY UPDATE name = :%s";
        String query = String.format(queryBase, table, PARAM_IDENTIFIER, PARAM_NAME, PARAM_NAME);

        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_IDENTIFIER, identifier);
        params.put(PARAM_NAME, rpName);

        try {
            template.update(query, new MapSqlParameterSource(params));
        } catch (Exception e) {
            log.error("Caught exception when inserting {}", type);
            log.debug("", e);
            throw new DBOperationException();
        }
    }

}
