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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.external.com.google.gdata.util.common.base.PercentEscaper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cz.muni.ics.perunproxyapi.persistence.enums.Entity.FACILITY;

@Component
@Slf4j
public class RelyingPartyServiceImpl implements RelyingPartyService {

    private static final PercentEscaper ESCAPER = new PercentEscaper("-_.!~*'()", false);

    private static final String MEMBERS = "members";
    private static final String GROUP_ATTRIBUTES = "groupAttributes";
    private static final String DISPLAY_NAME = "displayName";
    private static final String GROUP = "group";

    private final DataSource proxyApiStats;

    @Autowired
    public RelyingPartyServiceImpl(@Qualifier("proxyApiStats") DataSource proxyApiStats) {
        this.proxyApiStats = proxyApiStats;
    }

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

    @Override
    public boolean logStatistics(Long userId, String idpEntityId, String idpName, String rpIdentifier, String rpName,
                                 String statisticsTableName, String idpMapTable, String rpMapTable) {


        LocalDate date = LocalDate.now();


        String insertLoginQuery = "INSERT INTO " + statisticsTableName + "(day, idpId, spId, user, logins)" +
                " VALUES(?, ?, ?, ?, '1') ON DUPLICATE KEY UPDATE logins = logins + 1";

        try (Connection c = proxyApiStats.getConnection()) {
            insertIdpMap(c, idpEntityId, idpName, idpMapTable);
            insertRpMap(c, rpIdentifier, rpName, rpMapTable);
            int idpId = extractIdpId(c, idpEntityId, idpMapTable);
            int spId = extractSpId(c, rpIdentifier, rpMapTable);

            try (PreparedStatement preparedStatement = c.prepareStatement(insertLoginQuery)) {
                preparedStatement.setDate(1, Date.valueOf(date));
                preparedStatement.setInt(2, idpId);
                preparedStatement.setInt(3, spId);
                preparedStatement.setString(4, String.valueOf(userId));
                preparedStatement.execute();
                log.trace("login entry stored ({}, {}, {}, {}, {})", idpEntityId, idpName,
                        rpIdentifier, rpName, userId);
            }
        } catch (SQLException ex) {
            log.warn("caught SQLException", ex);
            return false;
        }

        return true;
    }

    private int extractSpId(Connection c, String rpIdentifier, String rpMapTable) throws SQLException {
        String getSpIdQuery = "SELECT * FROM " + rpMapTable + " WHERE identifier= ?";

        try (PreparedStatement preparedStatement = c.prepareStatement(getSpIdQuery)) {
            preparedStatement.setString(1, rpIdentifier);
            ResultSet rs = preparedStatement.executeQuery();
            rs.first();
            log.debug("RP ID obtained.");
            return rs.getInt("spId");
        }
    }

    private int extractIdpId(Connection c, String idpEntityId, String idpMapTable) throws SQLException {
        String getIdPIdQuery = "SELECT * FROM " + idpMapTable + " WHERE identifier = ?";

        try (PreparedStatement preparedStatement = c.prepareStatement(getIdPIdQuery)) {
            preparedStatement.setString(1, idpEntityId);
            ResultSet rs = preparedStatement.executeQuery();
            rs.first();
            log.debug("Idp ID obtained");
            return rs.getInt("idpId");
        }
    }

    private void insertRpMap(Connection c, String rpIdentifier, String rpName, String rpMapTable) throws SQLException {
        String insertSpMapQuery = "INSERT INTO " + rpMapTable + "(identifier, name)" +
                " VALUES (?, ?) ON DUPLICATE KEY UPDATE name = ?";

        try (PreparedStatement preparedStatement = c.prepareStatement(insertSpMapQuery)) {
            preparedStatement.setString(1, rpIdentifier);
            preparedStatement.setString(2, rpName);
            preparedStatement.setString(3, rpName);
            preparedStatement.execute();
            log.trace("RP map entry inserted");
        }
    }

    private void insertIdpMap(Connection c, String idpEntityId, String idpName, String idpMapTable) throws SQLException {
        String insertIdpMapQuery = "INSERT INTO " + idpMapTable + "(identifier, name)" +
                " VALUES (?, ?) ON DUPLICATE KEY UPDATE name = ?";

        try (PreparedStatement preparedStatement = c.prepareStatement(insertIdpMapQuery)) {
            preparedStatement.setString(1, idpEntityId);
            preparedStatement.setString(2, idpName);
            preparedStatement.setString(3, idpName);
            preparedStatement.execute();
            log.trace("IdP map entry inserted");
        }
    }

}
