package cz.muni.ics.perunproxyapi.application.facade.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.muni.ics.perunproxyapi.application.facade.FacadeUtils;
import cz.muni.ics.perunproxyapi.application.facade.GuiFacade;
import cz.muni.ics.perunproxyapi.application.facade.configuration.FacadeConfiguration;
import cz.muni.ics.perunproxyapi.application.facade.configuration.classes.LosAttribute;
import cz.muni.ics.perunproxyapi.application.facade.parameters.ServicesParams;
import cz.muni.ics.perunproxyapi.application.service.GuiService;
import cz.muni.ics.perunproxyapi.persistence.adapters.FullAdapter;
import cz.muni.ics.perunproxyapi.persistence.adapters.impl.AdaptersContainer;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.listOfServices.ServicesDataHolder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
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
    public static final String PROXY_IDENTIFIER_VALUE = "proxy_identifier_value";
    public static final String PERUN_PROXY_IDENTIFIER_ATTR = "proxy_identifier_attr";
    public static final String RP_ENVIROMENT_ATTR = "rp_environment_attr";
    public static final String SHOW_ON_SERVICE_LIST_ATTR = "show_on_service_list_attr";
    public static final String SAML2_ENTITY_ID_ATTR = "saml2_entity_id_attr";
    public static final String OIDC_CLIENT_ID_ATTR = "oidc_client_id_attr";
    public static final String DISPLAYED_ATTRIBUTES = "displayed_attributes";

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
    public ServicesDataHolder getListOfSps() throws IOException, PerunUnknownException, PerunConnectionException {
        JsonNode options = FacadeUtils.getOptions(GET_LIST_OF_SPS, methodConfigurations);
        FullAdapter adapter = FacadeUtils.getFullAdapter(adaptersContainer);

        boolean oidcEnabled = getBooleanOption(OIDC_ENABLED, options);
        boolean samlEnabled = getBooleanOption(SAML_ENABLED, options);
        boolean productionEnabled = getBooleanOption(PRODUCTION_ENABLED, options);
        boolean stagingEnabled = getBooleanOption(STAGING_ENABLED, options);
        boolean testingEnabled = getBooleanOption(TESTING_ENABLED, options);

        String proxyIdentifier = getRequiredStringOption(PROXY_IDENTIFIER_VALUE, GET_LIST_OF_SPS, options);
        String perunProxyIdentifierAttr = getRequiredStringOption(PERUN_PROXY_IDENTIFIER_ATTR, GET_LIST_OF_SPS, options);
        String rpEnvironmentAttr = getRequiredStringOption(RP_ENVIROMENT_ATTR, GET_LIST_OF_SPS, options);
        String showOnServiceListAttr = getRequiredStringOption(SHOW_ON_SERVICE_LIST_ATTR, GET_LIST_OF_SPS, options);

        String saml2EntityIdAttr = null;
        if (samlEnabled) {
            saml2EntityIdAttr = getRequiredStringOption(SAML2_ENTITY_ID_ATTR, GET_LIST_OF_SPS, options);
        }

        String oidcClientIdAttr = null;
        if (oidcEnabled) {
            oidcClientIdAttr = getRequiredStringOption(OIDC_CLIENT_ID_ATTR, GET_LIST_OF_SPS, options);
        }

        String str = options.get(DISPLAYED_ATTRIBUTES).toString();
        List<LosAttribute> displayedAttributes = new ObjectMapper().readValue(str, new TypeReference<>() {});

        ServicesParams params = new ServicesParams(adapter, samlEnabled, oidcEnabled, productionEnabled, stagingEnabled,
                testingEnabled, proxyIdentifier, perunProxyIdentifierAttr, showOnServiceListAttr, rpEnvironmentAttr,
                saml2EntityIdAttr, oidcClientIdAttr, displayedAttributes);

        return guiService.getListOfSps(params);
    }

}
