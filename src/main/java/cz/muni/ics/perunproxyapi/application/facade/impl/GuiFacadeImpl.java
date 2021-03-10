package cz.muni.ics.perunproxyapi.application.facade.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.perunproxyapi.application.facade.FacadeUtils;
import cz.muni.ics.perunproxyapi.application.facade.GuiFacade;
import cz.muni.ics.perunproxyapi.application.facade.configuration.FacadeConfiguration;
import cz.muni.ics.perunproxyapi.application.facade.configuration.classes.ListOfServicesDisplayedAttribute;
import cz.muni.ics.perunproxyapi.application.facade.configuration.classes.ListOfServicesJsonAttribute;
import cz.muni.ics.perunproxyapi.application.facade.parameters.ListOfServicesGuiParams;
import cz.muni.ics.perunproxyapi.application.facade.parameters.ListOfServicesJsonParams;
import cz.muni.ics.perunproxyapi.application.facade.parameters.ListOfServicesParams;
import cz.muni.ics.perunproxyapi.application.service.GuiService;
import cz.muni.ics.perunproxyapi.application.service.RelyingPartyService;
import cz.muni.ics.perunproxyapi.application.service.StatisticsService;
import cz.muni.ics.perunproxyapi.application.service.models.StatsRawData;
import cz.muni.ics.perunproxyapi.persistence.adapters.DataAdapter;
import cz.muni.ics.perunproxyapi.persistence.adapters.FullAdapter;
import cz.muni.ics.perunproxyapi.persistence.adapters.impl.AdaptersContainer;
import cz.muni.ics.perunproxyapi.persistence.exceptions.ConfigurationException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.EntityNotFoundException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.InternalErrorException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.listOfServices.ListOfServicesDAO;
import cz.muni.ics.perunproxyapi.persistence.models.statistics.IdpSumEntry;
import cz.muni.ics.perunproxyapi.persistence.models.statistics.LoginsPerDaySumEntry;
import cz.muni.ics.perunproxyapi.persistence.models.statistics.RpSumEntry;
import cz.muni.ics.perunproxyapi.presentation.DTOModels.statistics.DailyGraphEntry;
import cz.muni.ics.perunproxyapi.presentation.DTOModels.statistics.PieChartEntry;
import cz.muni.ics.perunproxyapi.presentation.DTOModels.statistics.StatisticsDTO;
import cz.muni.ics.perunproxyapi.presentation.enums.StatisticsDisplayMode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cz.muni.ics.perunproxyapi.application.facade.FacadeUtils.getBooleanOption;
import static cz.muni.ics.perunproxyapi.application.facade.FacadeUtils.getRequiredStringOption;
import static cz.muni.ics.perunproxyapi.presentation.enums.StatisticsDisplayMode.ALL;
import static cz.muni.ics.perunproxyapi.presentation.enums.StatisticsDisplayMode.IDP;
import static cz.muni.ics.perunproxyapi.presentation.enums.StatisticsDisplayMode.RP;

@Component
@Slf4j
public class GuiFacadeImpl implements GuiFacade {

    public static final String SAML_ENABLED = "saml_enabled";
    public static final String OIDC_ENABLED = "oidc_enabled";
    public static final String PRODUCTION_ENABLED = "production_enabled";
    public static final String STAGING_ENABLED = "staging_enabled";
    public static final String TESTING_ENABLED = "testing_enabled";
    public static final String GET_LIST_OF_SPS = "get_list_of_sps";
    public static final String GET_LIST_OF_SPS_JSON = "get_list_of_sps_json";
    public static final String PROXY_IDENTIFIER_VALUE = "proxy_identifier_value";
    public static final String PERUN_PROXY_IDENTIFIER_ATTR = "proxy_identifier_attr";
    public static final String SHOW_ON_SERVICE_LIST_ATTR = "show_on_service_list_attr";
    public static final String RP_PROTOCOL_ATTR = "rp_protocol_attr";
    public static final String DISPLAYED_ATTRIBUTES = "displayed_attributes";
    public static final String JSON_ATTRIBUTES = "json_attributes";
    public static final String RP_ENVIRONMENT_ATTR = "rp_environment_attr";
    public static final String RP_ENVIRONMENT = "rp_environment";

    private final Map<String, JsonNode> methodConfigurations;
    private final AdaptersContainer adaptersContainer;
    private final GuiService guiService;
    private final RelyingPartyService relyingPartyService;
    private final StatisticsService statisticsService;

    private final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

    @Autowired
    public GuiFacadeImpl(@NonNull GuiService guiService,
                         @NonNull RelyingPartyService relyingPartyService,
                         @NonNull AdaptersContainer adaptersContainer,
                         @NonNull FacadeConfiguration facadeConfiguration,
                         @NonNull StatisticsService statisticsService)
    {
        this.guiService = guiService;
        this.relyingPartyService = relyingPartyService;
        this.adaptersContainer = adaptersContainer;
        this.methodConfigurations = facadeConfiguration.getGuiAdapterMethodConfigurations();
        this.statisticsService = statisticsService;
    }

    @Override
    public ListOfServicesDAO getListOfSps() throws
            PerunUnknownException, PerunConnectionException, JsonProcessingException
    {
        JsonNode options = FacadeUtils.getOptions(GET_LIST_OF_SPS, methodConfigurations);

        if (!options.hasNonNull(DISPLAYED_ATTRIBUTES)) {
            throw new ConfigurationException("Required option " + DISPLAYED_ATTRIBUTES + " not found by method "
                    + GET_LIST_OF_SPS);
        }
        List<ListOfServicesDisplayedAttribute> displayedAttributes = new ObjectMapper()
                .readValue(options.get(DISPLAYED_ATTRIBUTES).toString(), new TypeReference<>() {});
        ListOfServicesGuiParams params = new ListOfServicesGuiParams(
                getListOfServicesParams(options), displayedAttributes);

        return guiService.getListOfSps(params);
    }

    @Override
    public JsonNode getListOfSpsInJson()
            throws JsonProcessingException, PerunUnknownException, PerunConnectionException
    {
        JsonNode options = FacadeUtils.getOptions(GET_LIST_OF_SPS_JSON, methodConfigurations);

        if (!options.hasNonNull(JSON_ATTRIBUTES)) {
            throw new ConfigurationException("Required option " + JSON_ATTRIBUTES + " not found by method "
                    + GET_LIST_OF_SPS_JSON);
        }
        List<ListOfServicesJsonAttribute> jsonAttributes = new ObjectMapper()
                .readValue(options.get(JSON_ATTRIBUTES).toString(), new TypeReference<>() {});
        ListOfServicesJsonParams params = new ListOfServicesJsonParams(
                getListOfServicesParams(options), jsonAttributes);

        return guiService.getListOfSpsJson(params);
    }

    @Override
    public String getRpEnvironmentValue(@NonNull String rpIdentifier)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException
    {
        JsonNode options = FacadeUtils.getOptions(RP_ENVIRONMENT, methodConfigurations);
        DataAdapter adapter = FacadeUtils.getAdapter(adaptersContainer, options);
        String attrName = FacadeUtils.getRequiredStringOption(RP_ENVIRONMENT_ATTR, RP_ENVIRONMENT, options);

        return relyingPartyService.getRpEnvironmentValue(rpIdentifier, adapter, attrName);
    }

    @Override
    public StatisticsDTO getAllStatistics() {
        StatsRawData stats = null;
        try {
            stats = getStatistics(ALL, null, null);
        } catch (EntityNotFoundException e) {
            // cannot happen
        }
        if (stats == null) {
            throw new InternalErrorException("Could not fetch data");
        }
        List<PieChartEntry> idpData = transformIdpRawData(stats);
        int totalIdpLoginsCount = stats.countTotalPerIdps();
        List<PieChartEntry> rpData = transformRpRawData(stats);
        int totalRpLoginsCount = stats.countTotalPerRps();
        List<DailyGraphEntry> logins = transformLoginData(stats);

        return new StatisticsDTO("", logins, totalIdpLoginsCount, idpData, totalRpLoginsCount, rpData);
    }

    @Override
    public StatisticsDTO getStatisticsForRps() {
        StatsRawData stats = null;
        try {
            stats = getStatistics(RP, null, null);
        } catch (EntityNotFoundException e) {
            // cannot happen
        }
        if (stats == null) {
            throw new InternalErrorException("Could not fetch data");
        }
        List<PieChartEntry> rpData = transformRpRawData(stats);
        int totalRpLoginsCount = stats.countTotalPerRps();
        return new StatisticsDTO("", Collections.emptyList(), 0, Collections.emptyList(), totalRpLoginsCount, rpData);
    }

    @Override
    public StatisticsDTO getStatisticsForIdPs() {
        StatsRawData stats = null;
        try {
            stats = getStatistics(IDP, null, null);
        } catch (EntityNotFoundException e) {
            // cannot happen
        }
        if (stats == null) {
            throw new InternalErrorException("Could not fetch data");
        }
        List<PieChartEntry> idpData = transformIdpRawData(stats);
        int totalIdpLoginsCount = stats.countTotalPerIdps();
        return new StatisticsDTO("", Collections.emptyList(), totalIdpLoginsCount, idpData, 0, Collections.emptyList());
    }

    @Override
    public StatisticsDTO getStatisticsForRp(@NonNull String rpIdentifier) throws EntityNotFoundException {
        StatsRawData stats = getStatistics(RP, null, rpIdentifier);
        List<DailyGraphEntry> logins = transformLoginData(stats);
        List<PieChartEntry> idpData = transformIdpRawData(stats);
        int totalIdpLoginsCount = stats.countTotalPerIdps();
        String rpName = statisticsService.getRpNameForIdentifier(rpIdentifier);

        return new StatisticsDTO(rpName, logins, totalIdpLoginsCount, idpData, 0, Collections.emptyList());
    }

    @Override
    public StatisticsDTO getStatisticsForIdp(@NonNull String idpIdentifier) throws EntityNotFoundException {
        StatsRawData stats = getStatistics(IDP, idpIdentifier, null);
        List<DailyGraphEntry> logins = transformLoginData(stats);
        List<PieChartEntry> rpData = transformRpRawData(stats);
        int totalRpLoginsCount = stats.countTotalPerRps();
        String idpName = statisticsService.getIdpNameForIdentifier(idpIdentifier);

        return new StatisticsDTO(idpName, logins, 0, Collections.emptyList(), totalRpLoginsCount, rpData);
    }

    // private methods

    private List<DailyGraphEntry> transformLoginData(StatsRawData stats) {
        if (stats.getStatsPerDay() == null) {
            return new ArrayList<>();
        }
        List<LoginsPerDaySumEntry> data = stats.getStatsPerDay();
        return data.stream()
                .sorted(LoginsPerDaySumEntry::compareByDate)
                .map(e -> new DailyGraphEntry(getDateLabel(e), e.getUsers(), e.getLogins()))
                .collect(Collectors.toList());
    }

    private long getDateLabel(LoginsPerDaySumEntry entry) {
        String date = String.format("%d-%d-%d", entry.getDay(), entry.getMonth(), entry.getYear());
        try {
            return dateFormat.parse(date).getTime();
        } catch (ParseException e) {
            return 0;
        }
    }

    private List<PieChartEntry> transformIdpRawData(StatsRawData stats) {
        if (stats.getStatsPerIdp() == null) {
            return new ArrayList<>();
        }
        List<IdpSumEntry> data = stats.getStatsPerIdp();
        List<PieChartEntry> transformed =
                data.stream()
                        .sorted(IdpSumEntry::compareByLogins)
                        .map(e -> new PieChartEntry(e.getIdpName(), e.getIdpIdentifier(), e.getLogins()))
                        .collect(Collectors.toList());
        Collections.reverse(transformed);
        return transformed;
    }

    private List<PieChartEntry> transformRpRawData(StatsRawData stats) {
        if (stats.getStatsPerRp() == null) {
            return new ArrayList<>();
        }
        List<RpSumEntry> data = stats.getStatsPerRp();
        List<PieChartEntry> transformed = data.stream()
                .sorted(RpSumEntry::compareByLogins)
                .map(e -> new PieChartEntry(e.getRpName(), e.getRpIdentifier(), e.getLogins()))
                .collect(Collectors.toList());
        Collections.reverse(transformed);
        return transformed;
    }

    private StatsRawData getStatistics(@NonNull StatisticsDisplayMode mode,
                                       String idpIdentifier,
                                       String rpIdentifier) throws EntityNotFoundException {
        StatsRawData stats = null;
        switch (mode) {
            case ALL: {
                stats = statisticsService.getOverallStatistics();
            } break;
            case RP: {
                if (StringUtils.hasText(rpIdentifier)) {
                    stats = statisticsService.getRpStatistics(rpIdentifier);
                } else {
                    stats = statisticsService.getOverallRpStatistics();
                }
            } break;
            case IDP: {
                if (StringUtils.hasText(rpIdentifier)) {
                    stats = statisticsService.getIdpStatistics(idpIdentifier);
                } else {
                    stats = statisticsService.getOverallIdpStatistics();
                }
            } break;
        }
        if (stats == null) {
            throw new InternalErrorException("Could not extract data");
        }
        return stats;
    }

    private ListOfServicesParams getListOfServicesParams(JsonNode options) {
        FullAdapter adapter = FacadeUtils.getFullAdapter(adaptersContainer);
        boolean samlEnabled = getBooleanOption(SAML_ENABLED, options);
        boolean oidcEnabled = getBooleanOption(OIDC_ENABLED, options);
        boolean productionEnabled = getBooleanOption(PRODUCTION_ENABLED, options);
        boolean stagingEnabled = getBooleanOption(STAGING_ENABLED, options);
        boolean testingEnabled = getBooleanOption(TESTING_ENABLED, options);
        String proxyIdentifierValue = getRequiredStringOption(PROXY_IDENTIFIER_VALUE, GET_LIST_OF_SPS_JSON, options);
        String proxyIdentifierAttr = getRequiredStringOption(PERUN_PROXY_IDENTIFIER_ATTR, GET_LIST_OF_SPS_JSON, options);
        String showOnServiceListAttr = getRequiredStringOption(SHOW_ON_SERVICE_LIST_ATTR, GET_LIST_OF_SPS_JSON, options);
        String rpEnvironmentAttr = getRequiredStringOption(RP_ENVIRONMENT_ATTR, GET_LIST_OF_SPS_JSON, options);
        String rpProtocolAttr = getRequiredStringOption(RP_PROTOCOL_ATTR, GET_LIST_OF_SPS, options);

        return new ListOfServicesParams(adapter, samlEnabled, oidcEnabled, productionEnabled, stagingEnabled,
                testingEnabled, proxyIdentifierAttr, proxyIdentifierValue, showOnServiceListAttr, rpEnvironmentAttr,
                rpProtocolAttr);
    }

}
