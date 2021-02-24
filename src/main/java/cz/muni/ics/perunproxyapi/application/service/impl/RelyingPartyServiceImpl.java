package cz.muni.ics.perunproxyapi.application.service.impl;

import cz.muni.ics.perunproxyapi.application.service.RelyingPartyService;
import cz.muni.ics.perunproxyapi.application.service.ServiceUtils;
import cz.muni.ics.perunproxyapi.persistence.adapters.DataAdapter;
import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
import cz.muni.ics.perunproxyapi.persistence.exceptions.EntityNotFoundException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.Facility;
import cz.muni.ics.perunproxyapi.persistence.models.Group;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.external.com.google.gdata.util.common.base.PercentEscaper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cz.muni.ics.perunproxyapi.persistence.enums.Entity.FACILITY;

@Component
public class RelyingPartyServiceImpl implements RelyingPartyService {

    private static final PercentEscaper ESCAPER = new PercentEscaper("-_.!~*'()", false);

    private static final String MEMBERS = "members";
    private static final String GROUP_ATTRIBUTES = "groupAttributes";
    private static final String DISPLAY_NAME = "displayName";
    private static final String GROUP = "group";

    @Override
    public Set<String> getEntitlements(@NonNull DataAdapter adapter, @NonNull Long facilityId,
                                        @NonNull Long userId, @NonNull String prefix, @NonNull String authority,
                                        String forwardedEntitlementsAttrIdentifier,
                                        String resourceCapabilitiesAttrIdentifier,
                                        String facilityCapabilitiesAttrIdentifier)
            throws PerunUnknownException, PerunConnectionException
    {
        Set<String> entitlements = new HashSet<>(
                adapter.getForwardedEntitlements(userId, forwardedEntitlementsAttrIdentifier)
        );

        List<Group> groups = adapter.getUsersGroupsOnFacility(facilityId, userId);
        if (groups == null || groups.isEmpty()) {
            return entitlements;
        }

        List<String> groupEntitlements = ServiceUtils.wrapGroupEntitlements(groups, prefix, authority);
        entitlements.addAll(groupEntitlements);

        fillCapabilities(entitlements, adapter, facilityId, userId, groups, resourceCapabilitiesAttrIdentifier,
                facilityCapabilitiesAttrIdentifier, prefix, authority);

        return entitlements;
    }

    @Override
    public Set<String> getEntitlementsExtended(@NonNull DataAdapter adapter, @NonNull Long facilityId,
                                               @NonNull Long userId, @NonNull String prefix, @NonNull String authority,
                                               String forwardedEntitlementsAttrIdentifier,
                                               String resourceCapabilitiesAttrIdentifier,
                                               String facilityCapabilitiesAttrIdentifier)
            throws PerunUnknownException, PerunConnectionException
    {
        Set<String> entitlements = new HashSet<>(
                adapter.getForwardedEntitlements(userId, forwardedEntitlementsAttrIdentifier)
        );

        List<Group> groups = adapter.getUsersGroupsOnFacility(facilityId, userId);
        if (groups == null || groups.isEmpty()) {
            return entitlements;
        }

        fillUuidEntitlements(entitlements, groups, prefix, authority);

        fillCapabilities(entitlements, adapter, facilityId, userId, groups, resourceCapabilitiesAttrIdentifier,
                facilityCapabilitiesAttrIdentifier, prefix, authority);

        return entitlements;
    }

    @Override
    public Facility getFacilityByIdentifier(@NonNull DataAdapter adapter, @NonNull String rpIdentifier)
            throws PerunUnknownException, PerunConnectionException {
        return adapter.getFacilityByRpIdentifier(rpIdentifier);
    }

    @Override
    public boolean hasAccessToService(@NonNull DataAdapter adapter,
                                      @NonNull Long facilityId,
                                      @NonNull Long userId,
                                      @NonNull List<Long> testVoIds,
                                      @NonNull List<Long> prodVoIds,
                                      @NonNull String checkGroupMembershipAttrIdentifier,
                                      @NonNull String isTestSpIdentifier)
            throws PerunUnknownException, PerunConnectionException
    {

        PerunAttributeValue isTestSpAttrValue = adapter.getAttributeValue(FACILITY, facilityId, isTestSpIdentifier);
        boolean isTestSp = isTestSpAttrValue != null && isTestSpAttrValue.valueAsBoolean();
        List<Long> voIds = isTestSp ? testVoIds : prodVoIds;
        if (!adapter.isValidMemberOfAnyProvidedVo(userId, voIds)) {
            return false;
        }

        PerunAttributeValue attributeValue = adapter.getAttributeValue(FACILITY, facilityId,
                checkGroupMembershipAttrIdentifier);
        boolean checkGroupMembership = attributeValue != null && attributeValue.valueAsBoolean();
        if (!checkGroupMembership) {
            return true;
        }

        List<Group> groups = adapter.getFacilityGroupsWhereUserIsValidMember(userId, facilityId);
        return !groups.isEmpty();
    }

    @Override
    public String getRpEnvironmentValue(@NonNull String rpIdentifier, @NonNull DataAdapter adapter,
                                        @NonNull String rpEnvAttr)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException
    {
        Facility f = adapter.getFacilityByRpIdentifier(rpIdentifier);
        if (f == null || f.getId() == null) {
            throw new EntityNotFoundException("No RP found for given identifier");
        }

        PerunAttributeValue env = adapter.getAttributeValue(Entity.FACILITY, f.getId(), rpEnvAttr);
        if (env == null) {
            return "";
        }
        return env.valueAsString();
    }

    private void fillUuidEntitlements(Set<String> entitlements, List<Group> userGroups, String prefix, String authority) {
        for (Group group : userGroups) {
            String entitlement = wrapGroupEntitlementToAARC(group.getUuid(), prefix, authority);
            entitlements.add(entitlement);

            String displayName = group.getUniqueGroupName();
            if (StringUtils.hasText(displayName) && MEMBERS.equals(group.getName())) {
                displayName = displayName.replace(':' + MEMBERS, "");
            }

            String entitlementWithAttributes = wrapGroupEntitlementToAARCWithAttributes(group.getUuid(), displayName, prefix, authority);
            entitlements.add(entitlementWithAttributes);
        }
    }

    private void fillCapabilities(Set<String> entitlements, DataAdapter adapter, Long facilityId, Long userId,
                                  List<Group> groups, String resourceCapabilitiesAttrIdentifier,
                                  String facilityCapabilitiesAttrIdentifier, String prefix, String authority)
            throws PerunConnectionException, PerunUnknownException {
        List<String> capabilities = adapter.getCapabilities(facilityId, userId, groups,
                resourceCapabilitiesAttrIdentifier, facilityCapabilitiesAttrIdentifier);

        if (capabilities != null && !capabilities.isEmpty()) {
            entitlements.addAll(capabilities.stream()
                    .map(cap -> ServiceUtils.wrapCapabilityToAARC(cap, prefix, authority))
                    .collect(Collectors.toSet())
            );
        }
    }

    private String wrapGroupEntitlementToAARC(String uuid, String prefix, String authority) {
        return addPrefixAndSuffix(GROUP + ':' + uuid, prefix, authority);
    }

    private String wrapGroupEntitlementToAARCWithAttributes(String uuid, String displayName, String prefix, String authority) {
        return addPrefixAndSuffix(GROUP_ATTRIBUTES + ':' + uuid + '?' + DISPLAY_NAME + '=' +
                ESCAPER.escape(displayName), prefix, authority);
    }

    private String addPrefixAndSuffix(String s, String prefix, String authority) {
        return prefix + s + '#' + authority;
    }

}
