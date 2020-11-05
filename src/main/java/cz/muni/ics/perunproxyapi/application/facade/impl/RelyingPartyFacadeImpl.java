package cz.muni.ics.perunproxyapi.application.facade.impl;

import com.fasterxml.jackson.databind.JsonNode;
import cz.muni.ics.perunproxyapi.application.facade.FacadeUtils;
import cz.muni.ics.perunproxyapi.application.facade.RelyingPartyFacade;
import cz.muni.ics.perunproxyapi.application.facade.configuration.FacadeConfiguration;
import cz.muni.ics.perunproxyapi.application.service.ProxyUserService;
import cz.muni.ics.perunproxyapi.application.service.RelyingPartyService;
import cz.muni.ics.perunproxyapi.persistence.adapters.DataAdapter;
import cz.muni.ics.perunproxyapi.persistence.adapters.impl.AdaptersContainer;
import cz.muni.ics.perunproxyapi.persistence.exceptions.EntityNotFoundException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.Facility;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
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
    public static final String CHECK_GROUP_MEMBERSHIP = "check_group_membership";
    public static final String IS_TEST_SP = "is_test_sp";
    public static final String HAS_ACCESS_TO_SERVICE = "has_access_to_service";
    public static final String PROD_VO_IDS = "prod_vo_ids";
    public static final String TEST_VO_IDS = "test_vo_ids";

    private final Map<String, JsonNode> methodConfigurations;
    private final AdaptersContainer adaptersContainer;
    private final RelyingPartyService relyingPartyService;
    private final ProxyUserService proxyUserService;
    private final String loginAttrIdentifier;

    @Autowired
    public RelyingPartyFacadeImpl(@NonNull AdaptersContainer adaptersContainer,
                                  @NonNull FacadeConfiguration facadeConfiguration,
                                  @NonNull RelyingPartyService relyingPartyService,
                                  @NonNull ProxyUserService proxyUserService,
                                  @Value("${attributes.identifiers.login}") String loginAttrIdentifier)
    {
        this.adaptersContainer = adaptersContainer;
        this.methodConfigurations = facadeConfiguration.getRelyingPartyAdapterMethodConfigurations();
        this.relyingPartyService = relyingPartyService;
        this.proxyUserService = proxyUserService;
        this.loginAttrIdentifier = loginAttrIdentifier;
    }

    @Override
    public List<String> getEntitlements(@NonNull String rpIdentifier, @NonNull String login)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException
    {
        JsonNode options = FacadeUtils.getOptions(GET_ENTITLEMENTS, methodConfigurations);
        DataAdapter adapter = FacadeUtils.getAdapter(adaptersContainer, options);

        String prefix = FacadeUtils.getRequiredStringOption(PREFIX, GET_ENTITLEMENTS, options);
        String authority = FacadeUtils.getRequiredStringOption(AUTHORITY, GET_ENTITLEMENTS, options);

        String forwardedEntitlementsAttrIdentifier = FacadeUtils.getStringOption(FORWARDED_ENTITLEMENTS, options);
        String resourceCapabilitiesAttrIdentifier = FacadeUtils.getStringOption(RESOURCE_CAPABILITIES, options);
        String facilityCapabilitiesAttrIdentifier = FacadeUtils.getStringOption(FACILITY_CAPABILITIES, options);

        User user = proxyUserService.getUserByLogin(adapter, login);
        if (user == null) {
            throw new EntityNotFoundException("No user has been found for given login");
        }

        Facility facility = relyingPartyService.getFacilityByIdentifier(adapter, rpIdentifier);
        if (facility == null || facility.getId() == null) {
            throw new EntityNotFoundException("No service has been found for given identifier");
        }

        List<String> entitlements = relyingPartyService.getEntitlements(
                adapter, facility.getId(), user.getPerunId(), prefix, authority, forwardedEntitlementsAttrIdentifier,
                resourceCapabilitiesAttrIdentifier, facilityCapabilitiesAttrIdentifier);
        if (entitlements != null) {
            Collections.sort(entitlements);
        }
        return entitlements;
    }

    @Override
    public boolean hasAccessToService(@NonNull String rpIdentifier, @NonNull String login)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException, IOException {
        JsonNode options = FacadeUtils.getOptions(HAS_ACCESS_TO_SERVICE, methodConfigurations);
        DataAdapter adapter = FacadeUtils.getAdapter(adaptersContainer, options);

        String checkGroupMembershipAttrIdentifier = FacadeUtils.getRequiredStringOption(CHECK_GROUP_MEMBERSHIP,
                HAS_ACCESS_TO_SERVICE, options);
        String isTestSpIdentifier = FacadeUtils.getRequiredStringOption(IS_TEST_SP, HAS_ACCESS_TO_SERVICE, options);
        List<Long> testVoIds = FacadeUtils.getRequiredLongListOption(TEST_VO_IDS, HAS_ACCESS_TO_SERVICE, options);
        List<Long> prodVoIds = FacadeUtils.getRequiredLongListOption(PROD_VO_IDS, HAS_ACCESS_TO_SERVICE, options);

        Facility facility = relyingPartyService.getFacilityByIdentifier(adapter, rpIdentifier);
        if (facility == null || facility.getId() == null) {
            throw new EntityNotFoundException("No facility has been found for given identifier");
        }

        User user = proxyUserService.getUserByLogin(adapter, login);
        if (user == null || user.getPerunId() == null) {
            throw new EntityNotFoundException("No user has been found for given login");
        }

        return relyingPartyService.hasAccessToService(adapter, facility.getId(), user.getPerunId(),
                testVoIds, prodVoIds, checkGroupMembershipAttrIdentifier, isTestSpIdentifier);
    }

}
