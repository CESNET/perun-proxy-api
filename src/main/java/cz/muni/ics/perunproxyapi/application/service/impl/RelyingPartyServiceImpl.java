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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static cz.muni.ics.perunproxyapi.persistence.enums.Entity.FACILITY;

@Component
public class RelyingPartyServiceImpl implements RelyingPartyService {

    @Override
    public List<String> getEntitlements(@NonNull DataAdapter adapter, @NonNull Long facilityId,
                                        @NonNull Long userId, @NonNull String prefix, @NonNull String authority,
                                        String forwardedEntitlementsAttrIdentifier,
                                        String resourceCapabilitiesAttrIdentifier,
                                        String facilityCapabilitiesAttrIdentifier)
            throws PerunUnknownException, PerunConnectionException
    {
        List<String> entitlements = new ArrayList<>(
                adapter.getForwardedEntitlements(userId, forwardedEntitlementsAttrIdentifier)
        );

        List<Group> groups = adapter.getUsersGroupsOnFacility(facilityId, userId);
        if (groups == null || groups.isEmpty()) {
            return entitlements;
        }

        List<String> groupEntitlements = ServiceUtils.wrapGroupEntitlements(groups, prefix, authority);
        entitlements.addAll(groupEntitlements);

        List<String> capabilities = adapter.getCapabilities(facilityId, userId, groups,
                resourceCapabilitiesAttrIdentifier, facilityCapabilitiesAttrIdentifier);
        if (capabilities != null && !capabilities.isEmpty()) {
            entitlements.addAll(capabilities.stream()
                    .map(cap -> ServiceUtils.wrapCapabilityToAARC(cap, prefix, authority))
                    .collect(Collectors.toSet())
            );
        }

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

}
