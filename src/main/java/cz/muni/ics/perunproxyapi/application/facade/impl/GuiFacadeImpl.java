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
import cz.muni.ics.perunproxyapi.persistence.adapters.FullAdapter;
import cz.muni.ics.perunproxyapi.persistence.adapters.impl.AdaptersContainer;
import cz.muni.ics.perunproxyapi.persistence.exceptions.ConfigurationException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.listOfServices.ListOfServicesDAO;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static cz.muni.ics.perunproxyapi.application.facade.FacadeUtils.getBooleanOption;
import static cz.muni.ics.perunproxyapi.application.facade.FacadeUtils.getRequiredStringOption;

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
    public static final String RP_ENVIRONMENT_ATTR = "rp_environment_attr";
    public static final String SHOW_ON_SERVICE_LIST_ATTR = "show_on_service_list_attr";
    public static final String RP_PROTOCOL_ATTR = "rp_protocol_attr";
    public static final String DISPLAYED_ATTRIBUTES = "displayed_attributes";
    public static final String JSON_ATTRIBUTES = "json_attributes";

    private final Map<String, JsonNode> methodConfigurations;
    private final AdaptersContainer adaptersContainer;
    private final GuiService guiService;

    @Autowired
    public GuiFacadeImpl(@NonNull GuiService guiService,
                               @NonNull AdaptersContainer adaptersContainer,
                               @NonNull FacadeConfiguration facadeConfiguration)
    {
        this.guiService = guiService;
        this.adaptersContainer = adaptersContainer;
        this.methodConfigurations = facadeConfiguration.getGuiAdapterMethodConfigurations();
    }

    @Override
    public ListOfServicesDAO getListOfSps() throws
            PerunUnknownException, PerunConnectionException, JsonProcessingException
    {
        JsonNode options = FacadeUtils.getOptions(GET_LIST_OF_SPS, methodConfigurations);

        if (!options.hasNonNull(JSON_ATTRIBUTES)) {
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
