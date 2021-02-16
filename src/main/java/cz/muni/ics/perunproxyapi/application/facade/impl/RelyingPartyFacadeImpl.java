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
import java.util.Set;

@Component
@Slf4j
public class RelyingPartyFacadeImpl implements RelyingPartyFacade {

    public static final String GET_ENTITLEMENTS = "get_entitlements";
    public static final String GET_ENTITLEMENTS_EXTENDED = "get_entitlements_extended";
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
    public static final String RP_ENVIRONMENT_ATTR = "rp_environment_attr";
    public static final String RP_ENVIRONMENT = "rp_environment";
    public static final String CREATE_MEMBER_IN_VO = "create_member_in_vo";
    public static final String VO_ID = "vo_id";
    public static final String REQUIRED_ATTRIBUTES = "required_attributes";
    public static final String LOGIN_ATTRIBUTES = "login_attributes";
    public static final String ATTR_MAPPER = "attr_mapper";
    public static final String CANDIDATE_ATTR_MAPPER = "candidate_attr_mapper";

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
    public Set<String> getEntitlements(@NonNull String rpIdentifier, @NonNull String login)
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

        return relyingPartyService.getEntitlements(
                adapter, facility.getId(), user.getPerunId(), prefix, authority, forwardedEntitlementsAttrIdentifier,
                resourceCapabilitiesAttrIdentifier, facilityCapabilitiesAttrIdentifier);
    }

    @Override
    public Set<String> getEntitlementsExtended(@NonNull String rpIdentifier, @NonNull String login)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException
    {
        JsonNode options = FacadeUtils.getOptions(GET_ENTITLEMENTS_EXTENDED, methodConfigurations);
        DataAdapter adapter = FacadeUtils.getAdapter(adaptersContainer, options);

        String prefix = FacadeUtils.getRequiredStringOption(PREFIX, GET_ENTITLEMENTS_EXTENDED, options);
        String authority = FacadeUtils.getRequiredStringOption(AUTHORITY, GET_ENTITLEMENTS_EXTENDED, options);

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

        return relyingPartyService.getEntitlementsExtended(
                adapter, facility.getId(), user.getPerunId(), prefix, authority, forwardedEntitlementsAttrIdentifier,
                resourceCapabilitiesAttrIdentifier, facilityCapabilitiesAttrIdentifier);
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

    @Override
    public String getRpEnvironmentValue(@NonNull String rpIdentifier)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException
    {
        JsonNode options = FacadeUtils.getOptions(RP_ENVIRONMENT, methodConfigurations);
        DataAdapter adapter = FacadeUtils.getAdapter(adaptersContainer, options);
        String attrName = FacadeUtils.getRequiredStringOption(RP_ENVIRONMENT_ATTR, RP_ENVIRONMENT, options);

        return relyingPartyService.getRpEnvironmentValue(rpIdentifier, adapter, attrName);
    }

}
