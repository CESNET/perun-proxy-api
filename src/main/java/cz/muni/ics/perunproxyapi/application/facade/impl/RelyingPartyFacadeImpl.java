package cz.muni.ics.perunproxyapi.application.facade.impl;

import com.fasterxml.jackson.databind.JsonNode;
import cz.muni.ics.perunproxyapi.application.facade.FacadeUtils;
import cz.muni.ics.perunproxyapi.application.facade.RelyingPartyFacade;
import cz.muni.ics.perunproxyapi.application.facade.configuration.FacadeConfiguration;
import cz.muni.ics.perunproxyapi.application.service.ProxyUserService;
import cz.muni.ics.perunproxyapi.application.service.RelyingPartyService;
import cz.muni.ics.perunproxyapi.persistence.adapters.DataAdapter;
import cz.muni.ics.perunproxyapi.persistence.adapters.impl.AdaptersContainer;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.Facility;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class RelyingPartyFacadeImpl implements RelyingPartyFacade {

    public static final String GET_ENTITLEMENTS = "get_entitlements";
    public static final String PREFIX = "prefix";
    public static final String AUTHORITY = "authority";
    public static final String FORWARDED_ENTITLEMENTS = "forwarded_entitlements";
    public static final String RESOURCE_CAPABILITIES = "resource_capabilities";
    public static final String FACILITY_CAPABILITIES = "facility_capabilities";

    private final Map<String, JsonNode> methodConfigurations;
    private final AdaptersContainer adaptersContainer;
    private final RelyingPartyService relyingPartyService;
    private final ProxyUserService proxyUserService;
    private final String defaultIdpIdentifier;

    @Autowired
    public RelyingPartyFacadeImpl(@NonNull AdaptersContainer adaptersContainer,
                                  @NonNull FacadeConfiguration facadeConfiguration,
                                  @NonNull RelyingPartyService relyingPartyService,
                                  @NonNull ProxyUserService proxyUserService,
                                  @Value("${facade.default_idp}") String defaultIdp)
    {
        this.adaptersContainer = adaptersContainer;
        this.methodConfigurations = facadeConfiguration.getRelyingPartyAdapterMethodConfigurations();
        this.relyingPartyService = relyingPartyService;
        this.proxyUserService = proxyUserService;
        this.defaultIdpIdentifier = defaultIdp;
    }

    @Override
    public List<String> getEntitlements(@NonNull String rpIdentifier, @NonNull String login)
            throws PerunUnknownException, PerunConnectionException
    {
        JsonNode options = FacadeUtils.getOptions(GET_ENTITLEMENTS, methodConfigurations);
        DataAdapter adapter = FacadeUtils.getAdapter(adaptersContainer, options);

        String prefix = FacadeUtils.getRequiredStringOption(PREFIX, options);
        String authority =FacadeUtils.getRequiredStringOption(AUTHORITY, options);

        String forwardedEntitlementsAttrIdentifier = FacadeUtils.getStringOption(FORWARDED_ENTITLEMENTS, options);
        String resourceCapabilitiesAttrIdentifier = FacadeUtils.getStringOption(RESOURCE_CAPABILITIES, options);
        String facilityCapabilitiesAttrIdentifier = FacadeUtils.getStringOption(FACILITY_CAPABILITIES, options);

        User user = proxyUserService.findByExtLogin(adapter, defaultIdpIdentifier, login);
        if (user == null || user.getPerunId() == null) {
            log.error("No user found for login {} with Idp {}. Cannot look for entitlements, return error.",
                    login, defaultIdpIdentifier);
            throw new IllegalArgumentException("User for given login could not be found");
        }

        Facility facility = relyingPartyService.getFacilityByIdentifier(adapter, rpIdentifier);
        if (facility == null || facility.getId() == null) {
            log.error("No facility found for rpIdentifier {}. Cannot look for  entitlements, return error.",
                    rpIdentifier);
            throw new IllegalArgumentException("User for given login could not be found");
        }

        List<String> entitlements = relyingPartyService.getEntitlements(
                adapter, facility.getId(), user.getPerunId(), prefix, authority, forwardedEntitlementsAttrIdentifier,
                resourceCapabilitiesAttrIdentifier, facilityCapabilitiesAttrIdentifier);
        if (entitlements != null) {
            Collections.sort(entitlements);
        }
        return entitlements;
    }

}
