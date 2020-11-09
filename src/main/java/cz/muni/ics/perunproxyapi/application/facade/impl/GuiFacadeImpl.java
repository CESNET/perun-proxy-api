package cz.muni.ics.perunproxyapi.application.facade.impl;

import com.fasterxml.jackson.databind.JsonNode;
import cz.muni.ics.perunproxyapi.application.facade.FacadeUtils;
import cz.muni.ics.perunproxyapi.application.facade.GuiFacade;
import cz.muni.ics.perunproxyapi.application.facade.configuration.FacadeConfiguration;
import cz.muni.ics.perunproxyapi.application.service.GuiService;
import cz.muni.ics.perunproxyapi.application.service.ProxyUserService;
import cz.muni.ics.perunproxyapi.persistence.adapters.FullAdapter;
import cz.muni.ics.perunproxyapi.persistence.adapters.impl.AdaptersContainer;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.listOfServices.ServicesDataHolder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class GuiFacadeImpl implements GuiFacade {

    public static final String HEADER_PATH = "header_path";
    public static final String FOOTER_PATH = "footer_path";
    public static final String SHOW_OIDC_SERVICES = "show_oidc_services";
    public static final String GET_LIST_OF_SPS = "get_list_of_sps";
    public static final String PROXY_IDENTIFIER = "proxy_identifier";
    public static final String PERUN_PROXY_IDENTIFIER_ATTR = "perun_proxy_identifier_attr";
    public static final String SERVICE_NAME_ATTR = "service_name_attr";
    public static final String LOGIN_URL_ATTR = "login_url_attr";
    public static final String IS_TEST_SP_ATTR = "is_test_sp_attr";
    public static final String SHOW_ON_SERVICE_LIST_ATTR = "show_on_service_list_attr";
    public static final String SAML2ENTITY_ID_ATTR = "saml2entity_id_attr";
    public static final String OIDC_CLIENT_ID_ATTR = "oidc_client_id_attr";
    public static final String ATTRIBUTES_DEFINITIONS = "attributes_definitions";
    public static final String MULTILINGUAL_ATTRIBUTES = "multilingual_attributes";
    public static final String URL_ATTRIBUTES = "url_attributes";

    public static final String GET_HEADER_AND_FOOTER = "get_header_and_footer";
    public static final String HEADER = "header_path";
    public static final String FOOTER = "footer_path";

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

        boolean showOIDCServices = FacadeUtils.getBooleanOption(SHOW_OIDC_SERVICES, options);
        String headerPath = FacadeUtils.getRequiredStringOption(HEADER_PATH, GET_LIST_OF_SPS, options);
        String footerPath = FacadeUtils.getRequiredStringOption(FOOTER_PATH, GET_LIST_OF_SPS, options);
        String proxyIdentifier = FacadeUtils.getRequiredStringOption(PROXY_IDENTIFIER, GET_LIST_OF_SPS, options);
        String perunProxyIdentifierAttr = FacadeUtils.getRequiredStringOption(PERUN_PROXY_IDENTIFIER_ATTR, GET_LIST_OF_SPS, options);
        String serviceNameAttr = FacadeUtils.getRequiredStringOption(SERVICE_NAME_ATTR, GET_LIST_OF_SPS, options);
        String loginUrlAttr = FacadeUtils.getRequiredStringOption(LOGIN_URL_ATTR, GET_LIST_OF_SPS, options);
        String rpEnvironmentAttr = FacadeUtils.getRequiredStringOption(IS_TEST_SP_ATTR, GET_LIST_OF_SPS, options);
        String showOnServiceListAttr = FacadeUtils.getRequiredStringOption(SHOW_ON_SERVICE_LIST_ATTR, GET_LIST_OF_SPS, options);
        String saml2EntityIdAttr = FacadeUtils.getRequiredStringOption(SAML2ENTITY_ID_ATTR, GET_LIST_OF_SPS, options);
        String oidcClientIdAttr = FacadeUtils.getRequiredStringOption(OIDC_CLIENT_ID_ATTR, GET_LIST_OF_SPS, options);
        List<String> attributesDefinitions = FacadeUtils.getRequiredStringListOption (ATTRIBUTES_DEFINITIONS, GET_LIST_OF_SPS, options);
        List<String> multilingualAttributes = FacadeUtils.getRequiredStringListOption(MULTILINGUAL_ATTRIBUTES, GET_LIST_OF_SPS, options);
        List<String> urlAttributes = FacadeUtils.getRequiredStringListOption(URL_ATTRIBUTES, GET_LIST_OF_SPS, options);

        return guiService.getListOfSps(adapter,
                headerPath,
                footerPath,
                proxyIdentifier,
                showOIDCServices,
                perunProxyIdentifierAttr,
                serviceNameAttr,
                loginUrlAttr,
                rpEnvironmentAttr,
                showOnServiceListAttr,
                saml2EntityIdAttr,
                oidcClientIdAttr,
                attributesDefinitions,
                multilingualAttributes,
                urlAttributes);
    }

    @Override
    public ModelAndView addHeaderAndFooter(ModelAndView mav) {
        JsonNode options = FacadeUtils.getOptions(GET_HEADER_AND_FOOTER, methodConfigurations);

        String header = FacadeUtils.getRequiredStringOption(HEADER, GET_HEADER_AND_FOOTER, options);
        String footer = FacadeUtils.getRequiredStringOption(FOOTER, GET_HEADER_AND_FOOTER, options);

        mav.addObject(HEADER, header);
        mav.addObject(FOOTER, footer);

        return mav;
    }

}
