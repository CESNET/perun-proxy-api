package cz.muni.ics.perunproxyapi.application.service.impl;

import cz.muni.ics.perunproxyapi.application.service.ProxyUserService;
import cz.muni.ics.perunproxyapi.application.service.ServiceUtils;
import cz.muni.ics.perunproxyapi.persistence.adapters.DataAdapter;
import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.Group;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class ProxyUserServiceImpl implements ProxyUserService {

    @Override
    public User findByExtLogins(DataAdapter preferredAdapter, String idpEntityId, List<String> userIdentifiers) throws PerunUnknownException, PerunConnectionException {
        return preferredAdapter.getPerunUser(idpEntityId, userIdentifiers);
    }

    @Override
    public User findByExtLogin(DataAdapter preferredAdapter, String idpIdentifier, String login) throws PerunUnknownException, PerunConnectionException {
        return findByExtLogins(preferredAdapter, idpIdentifier, Collections.singletonList(login));
    }

    @Override
    public Map<String, PerunAttributeValue> getAttributesValues(DataAdapter preferredAdapter, Entity entity, long id, List<String> attributes) throws PerunUnknownException, PerunConnectionException {
        return preferredAdapter.getAttributesValues(entity, id, attributes);
    }

    @Override
    public User findByPerunUserId(DataAdapter preferredAdapter, Long userId) throws PerunUnknownException, PerunConnectionException {
        return preferredAdapter.findPerunUserById(userId);
    }

    @Override
    public List<String> getAllEntitlements(@NonNull DataAdapter adapter, @NonNull Long userId,
                                           @NonNull String prefix, @NonNull String authority,
                                           String forwardedEntitlementsAttrIdentifier)
            throws PerunUnknownException, PerunConnectionException
    {

        List<String> forwardedEntitlements = adapter.getForwardedEntitlements(userId,
                forwardedEntitlementsAttrIdentifier);
        List<String> entitlements = new ArrayList<>(forwardedEntitlements);

        List<Group> groups = adapter.getUserGroups(userId);
        if (groups != null && !groups.isEmpty()) {
            List<String> eduPersonEntitlement = ServiceUtils.wrapGroupEntitlements(groups, prefix, authority);
            entitlements.addAll(eduPersonEntitlement);
        }

        return entitlements;
    }

    @Override
    public User getUserWithAttributesByLogin(@NonNull DataAdapter preferredAdapter,
                                             @NonNull String loginAttrIdentifier,
                                             @NonNull String login,
                                             List<String> attrIdentifiers)
            throws PerunUnknownException, PerunConnectionException
    {
        return preferredAdapter.getUserWithAttributesByLogin(loginAttrIdentifier, login, attrIdentifiers);
    }

    @Override
    public User getUserByLogin(@NonNull DataAdapter preferredAdapter,
                               @NonNull String loginAttrIdentifier,
                               @NonNull String login)
            throws PerunUnknownException, PerunConnectionException
    {
        return this.getUserWithAttributesByLogin(preferredAdapter, loginAttrIdentifier, login, new ArrayList<>());
    }

    @Override
    public User findByIdentifiers(@NonNull DataAdapter adapter,
                                  @NonNull String idpIdentifier,
                                  @NonNull List<String> identifiers,
                                  @NonNull List<String> attrIdentifiers)
    {
        return adapter.findByIdentifiers(idpIdentifier, identifiers, attrIdentifiers);
    }

}
