package cz.muni.ics.perunproxyapi.application.service.impl;

import cz.muni.ics.perunproxyapi.application.service.StatisticsService;
import cz.muni.ics.perunproxyapi.application.service.models.StatsRawData;
import cz.muni.ics.perunproxyapi.persistence.exceptions.DBOperationException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.EntityNotFoundException;
import cz.muni.ics.perunproxyapi.persistence.managers.StatisticsManager;
import cz.muni.ics.perunproxyapi.persistence.models.statistics.IdpSumEntry;
import cz.muni.ics.perunproxyapi.persistence.models.statistics.RpSumEntry;
import cz.muni.ics.perunproxyapi.persistence.models.statistics.LoginsPerDaySumEntry;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsManager statisticsManager;

    @Autowired
    public StatisticsServiceImpl(@NonNull StatisticsManager statisticsManager) {
        this.statisticsManager = statisticsManager;
    }

    @Override
    public boolean logStatistics(@NonNull String userId,
                                 @NonNull String rpIdentifier,
                                 @NonNull String rpName,
                                 @NonNull String idpIdentifier,
                                 @NonNull String idpName)
    {
        try {
            statisticsManager.insertIdp(idpIdentifier, idpName);
            statisticsManager.insertRp(rpIdentifier, rpName);
            int idpId = statisticsManager.extractIdpId(idpIdentifier);
            int rpId = statisticsManager.extractRpId(rpIdentifier);
            statisticsManager.insertLogin(rpId, idpId, userId);
        } catch (EntityNotFoundException | DBOperationException e) {
            return false;
        }
        return true;
    }

    @Override
    public StatsRawData getOverallStatistics() {
        try {
            List<LoginsPerDaySumEntry> statsPerDay = statisticsManager.getSummaryLogins();
            List<IdpSumEntry> statsPerIdp = statisticsManager.getSummaryForIdps();
            List<RpSumEntry> statsPerRp = statisticsManager.getSummaryForRps();
            return new StatsRawData(statsPerDay, statsPerIdp, statsPerRp);
        } catch (DBOperationException e) {
            log.warn("Could not complete operation due to database exception");
            log.trace("", e);
        }
        return null;
    }

    @Override
    public StatsRawData getOverallRpStatistics() {
        try {
            List<RpSumEntry> statsPerRp = statisticsManager.getSummaryForRps();
            return new StatsRawData(Collections.emptyList(), Collections.emptyList(), statsPerRp);
        } catch (DBOperationException e) {
            log.warn("Could not complete operation due to database exception");
            log.trace("", e);
        }
        return null;
    }

    @Override
    public StatsRawData getOverallIdpStatistics() {
        try {
            List<IdpSumEntry> statsPerIdp = statisticsManager.getSummaryForIdps();
            return new StatsRawData(Collections.emptyList(), statsPerIdp, Collections.emptyList());
        } catch (DBOperationException e) {
            log.warn("Could not complete operation due to database exception");
            log.trace("", e);
        }
        return null;
    }

    @Override
    public StatsRawData getRpStatistics(String rpIdentifier) throws EntityNotFoundException {
        if (!StringUtils.hasText(rpIdentifier)) {
            throw new IllegalArgumentException("No RP identifier provided");
        }
        try {
            int rpId = statisticsManager.extractRpId(rpIdentifier);
            List<LoginsPerDaySumEntry> statsPerDay = statisticsManager.getRpLogins(rpId);
            List<IdpSumEntry> statsPerIdp = statisticsManager.getIdpStatsForRp(rpId);
            return new StatsRawData(statsPerDay, statsPerIdp, Collections.emptyList());
        } catch (DBOperationException e) {
            log.warn("Could not complete operation due to database exception");
            log.trace("", e);
        }
        return null;
    }

    @Override
    public StatsRawData getIdpStatistics(String idpIdentifier) throws EntityNotFoundException {
        if (!StringUtils.hasText(idpIdentifier)) {
            throw new IllegalArgumentException("No IdP identifier provided");
        }
        try {
            int idpId = statisticsManager.extractIdpId(idpIdentifier);
            List<LoginsPerDaySumEntry> statsPerDay = statisticsManager.getIdpLogins(idpId);
            List<RpSumEntry> statsPerRp = statisticsManager.getRpStatsForIdp(idpId);
            return new StatsRawData(statsPerDay, Collections.emptyList(), statsPerRp);
        } catch (DBOperationException e) {
            log.warn("Could not complete operation due to database exception");
            log.trace("", e);
        }
        return null;
    }

    @Override
    public String getRpNameForIdentifier(@NonNull String rpIdentifier) throws EntityNotFoundException {
        if (!StringUtils.hasText(rpIdentifier)) {
            throw new IllegalArgumentException("No RP identifier provided");
        }
        try {
            return statisticsManager.extractRpName(rpIdentifier);
        } catch (DBOperationException e) {
            log.warn("Could not complete operation due to database exception");
            log.trace("", e);
        }
        return "";
    }

    @Override
    public String getIdpNameForIdentifier(@NonNull String idpIdentifier) throws EntityNotFoundException {
        if (!StringUtils.hasText(idpIdentifier)) {
            throw new IllegalArgumentException("No IdP identifier provided");
        }
        try {
            return statisticsManager.extractIdpName(idpIdentifier);
        } catch (DBOperationException e) {
            log.warn("Could not complete operation due to database exception");
            log.trace("", e);
        }
        return "";
    }

}
