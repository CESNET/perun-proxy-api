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
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class ProxyUserServiceImpl implements ProxyUserService {

    @Override
    public User findByExtLogins(@NonNull DataAdapter preferredAdapter, @NonNull String idpIdentifier,
                                @NonNull List<String> userIdentifiers)
            throws PerunUnknownException, PerunConnectionException
    {
        if (!StringUtils.hasText(idpIdentifier)) {
            throw new IllegalArgumentException("IdP identifier cannot be empty");
        } else if (userIdentifiers.isEmpty()) {
            throw new IllegalArgumentException("User identifiers cannot be empty");
        }
        return preferredAdapter.getPerunUser(idpIdentifier, userIdentifiers);
    }

    @Override
    public User findByExtLogin(@NonNull DataAdapter preferredAdapter, @NonNull String idpIdentifier,
                               @NonNull String login)
            throws PerunUnknownException, PerunConnectionException
    {
        if (!StringUtils.hasText(idpIdentifier)) {
            throw new IllegalArgumentException("IdP Identifier cannot be empty");
        } else if (!StringUtils.hasText(login)) {
            throw new IllegalArgumentException("Login cannot be empty");
        }
        return findByExtLogins(preferredAdapter, idpIdentifier, Collections.singletonList(login));
    }

    @Override
    public Map<String, PerunAttributeValue> getAttributesValues(@NonNull DataAdapter preferredAdapter,
                                                                @NonNull Entity entity, long id,
                                                                List<String> attributes)
            throws PerunUnknownException, PerunConnectionException
    {
        return preferredAdapter.getAttributesValues(entity, id, attributes);
    }

    @Override
    public User findByPerunUserId(@NonNull DataAdapter preferredAdapter, @NonNull Long userId)
            throws PerunUnknownException, PerunConnectionException {
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
    public User getUserWithAttributesByLogin(@NonNull DataAdapter preferredAdapter, @NonNull String login,
                                             List<String> attrIdentifiers)
            throws PerunUnknownException, PerunConnectionException
    {
        if (!StringUtils.hasText(login)) {
            throw new IllegalArgumentException("User login cannot be empty");
        }
        return preferredAdapter.getUserWithAttributesByLogin(login, attrIdentifiers);
    }

    @Override
    public User getUserByLogin(@NonNull DataAdapter preferredAdapter, @NonNull String login)
            throws PerunUnknownException, PerunConnectionException
    {
        if (!StringUtils.hasText(login)) {
            throw new IllegalArgumentException("User login cannot be empty");
        }
        return this.getUserWithAttributesByLogin(preferredAdapter, login, new ArrayList<>());
    }

    @Override
    public User findByIdentifiers(@NonNull DataAdapter adapter,
                                  @NonNull String idpIdentifier,
                                  @NonNull List<String> identifiers,
                                  @NonNull List<String> attrIdentifiers)
    {
        if (!StringUtils.hasText(idpIdentifier)) {
            throw new IllegalArgumentException("IdP Identifier cannot be empty");
        } else if (identifiers.isEmpty()) {
            throw new IllegalArgumentException("Identifiers cannot be empty");
        }
        return adapter.findByIdentifiers(idpIdentifier, identifiers, attrIdentifiers);
    }

}
