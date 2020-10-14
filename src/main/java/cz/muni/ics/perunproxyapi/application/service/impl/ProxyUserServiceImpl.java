package cz.muni.ics.perunproxyapi.application.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import cz.muni.ics.perunproxyapi.application.service.ProxyUserService;
import cz.muni.ics.perunproxyapi.application.service.ServiceUtils;
import cz.muni.ics.perunproxyapi.persistence.adapters.DataAdapter;
import cz.muni.ics.perunproxyapi.persistence.adapters.FullAdapter;
import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
import cz.muni.ics.perunproxyapi.persistence.exceptions.InternalErrorException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.Group;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttribute;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValueAwareModel;
import cz.muni.ics.perunproxyapi.persistence.models.UpdateAttributeMappingEntry;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import cz.muni.ics.perunproxyapi.persistence.models.UserExtSource;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ProxyUserServiceImpl implements ProxyUserService {

    public static final String UES_VALUES_SEPARATOR = ";";

    @Override
    public User findByExtLogins(@NonNull DataAdapter preferredAdapter, @NonNull String idpIdentifier,
                                @NonNull List<String> userIdentifiers, List<String> attrIdentifiers)
            throws PerunUnknownException, PerunConnectionException
    {
        if (!StringUtils.hasText(idpIdentifier)) {
            throw new IllegalArgumentException("IdP identifier cannot be empty");
        } else if (userIdentifiers.isEmpty()) {
            throw new IllegalArgumentException("User identifiers cannot be empty");
        }
        return preferredAdapter.getPerunUser(idpIdentifier, userIdentifiers, attrIdentifiers);
    }

    @Override
    public User findByExtLogin(@NonNull DataAdapter preferredAdapter, @NonNull String idpIdentifier,
                               @NonNull String login, List<String> attrIdentifiers)
            throws PerunUnknownException, PerunConnectionException
    {
        if (!StringUtils.hasText(idpIdentifier)) {
            throw new IllegalArgumentException("IdP Identifier cannot be empty");
        } else if (!StringUtils.hasText(login)) {
            throw new IllegalArgumentException("Login cannot be empty");
        }
        return this.findByExtLogins(preferredAdapter, idpIdentifier,
                Collections.singletonList(login), attrIdentifiers);
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
    public User findByPerunUserIdWithAttributes(@NonNull DataAdapter preferredAdapter,
                                                @NonNull Long userId, List<String> attrIdentifiers)
            throws PerunUnknownException, PerunConnectionException
    {
        return preferredAdapter.findPerunUserById(userId, attrIdentifiers);
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

    @Override
    public boolean updateUserIdentityAttributes(@NonNull String login, @NonNull String identityId,
                                                @NonNull FullAdapter adapter,
                                                @NonNull Map<String, JsonNode> requestAttributes,
                                                @NonNull Map<String, UpdateAttributeMappingEntry> internalToExternalWithOptions,
                                                @NonNull Map<String, String> externalToInternalMapping,
                                                @NonNull List<String> attrsToSearchBy)
            throws PerunUnknownException, PerunConnectionException
    {
        if (!StringUtils.hasText(login)) {
            throw new IllegalArgumentException("Login cannot be null nor empty");
        } else if (!StringUtils.hasText(identityId)) {
            throw new IllegalArgumentException("Identity ID cannot be null nor empty");
        }

        Map<String, JsonNode> internalToNewValues = new HashMap<>();
        for (Map.Entry<String, JsonNode> entry : requestAttributes.entrySet()) {
            internalToNewValues.put(externalToInternalMapping.get(entry.getKey()), entry.getValue());
        }

        UserExtSource ues = this.getUserExtSourceUsingIdentityId(adapter, attrsToSearchBy,
                internalToNewValues, login, identityId);
        Map<String, PerunAttribute> userExtSourceAttrs = adapter.getAttributes(Entity.USER_EXT_SOURCE, ues.getId(),
                        new ArrayList<>(internalToExternalWithOptions.keySet()));

        List<PerunAttribute> attributesToUpdateList = this.getAttributesToUpdate(userExtSourceAttrs,
                internalToNewValues, internalToExternalWithOptions);
        boolean attributesUpdated = true;
        if (!attributesToUpdateList.isEmpty()) {
            attributesUpdated = adapter.setAttributes(Entity.USER_EXT_SOURCE, ues.getId(), attributesToUpdateList);
        }
        boolean lastAccessUpdated = adapter.updateUserExtSourceLastAccess(ues);
        return attributesUpdated && lastAccessUpdated;
    }

    private UserExtSource getUserExtSourceUsingIdentityId(@NonNull FullAdapter adapter, @NonNull List<String> uesAttrs,
                                                          @NonNull Map<String, JsonNode> internalIdentifiersToNewValues,
                                                          @NonNull String login, @NonNull String identityId)
            throws PerunUnknownException, PerunConnectionException
    {
        User user = this.getUserByLogin(adapter, login);
        if (user == null) {
            throw new IllegalArgumentException("Could not find user with given login");
        }

        List<UserExtSource> uesList = adapter.getUserExtSources(user.getPerunId());
        List<UserExtSource> withIdentityId = uesList.stream()
                .filter(x -> Objects.equals(identityId, x.getExtSource().getName()))
                .collect(Collectors.toList());

        if (withIdentityId.isEmpty()) {
            throw new InternalErrorException("No extSource with given EntityID has been found for given user");
        }

        Set<UserExtSource> uesByLogin = this.getUesByLogin(uesAttrs, withIdentityId,
                internalIdentifiersToNewValues);
        if (uesByLogin.size() == 1) {
            return new ArrayList<>(uesByLogin).get(0);
        } else if (uesByLogin.size() > 1) {
            throw new InternalErrorException("More User ExtSources with same login found");
        }

        List<Integer> matchedIndexes = this.getMatchingAttrsUesIndexes(adapter, withIdentityId,
                uesAttrs, internalIdentifiersToNewValues);
        if (matchedIndexes.size() != 1) {
            throw new InternalErrorException("No User ExtSource or more than one match the given identifiers.");
        }
        return withIdentityId.get(matchedIndexes.get(0));
    }

    private Set<UserExtSource> getUesByLogin(@NonNull List<String> attributesToFindUes,
                                             @NonNull List<UserExtSource> uesList,
                                             @NonNull Map<String, JsonNode> internalNamesToNewValuesMap)
    {
        Set<UserExtSource> uesByLogin = new HashSet<>();
        for (String searchAttr: attributesToFindUes) {
            String newValAsString = null;
            if (internalNamesToNewValuesMap.containsKey(searchAttr)) {
                JsonNode val = internalNamesToNewValuesMap.get(searchAttr);
                if (val != null && !val.isNull()) {
                    newValAsString = val.asText();
                }
            }

            if (newValAsString != null) {
                final String finalNewValAsString = newValAsString;
                List<UserExtSource> withLogin = uesList.stream()
                        .filter(x -> Objects.equals(finalNewValAsString, x.getLogin()))
                        .collect(Collectors.toList());
                if (!withLogin.isEmpty()) {
                    uesByLogin.addAll(withLogin);
                }
            }
        }
        return uesByLogin;
    }

    private List<Integer> getMatchingAttrsUesIndexes(@NonNull FullAdapter adapter, @NonNull List<UserExtSource> uesList,
                                                     @NonNull List<String> uesAttrs,
                                                     @NonNull Map<String, JsonNode> internalIdentifiersToNewValuesMap)
            throws PerunUnknownException, PerunConnectionException
    {
        List<Integer> matchedIndexes = new ArrayList<>();
        int i = 0;
        for (UserExtSource ues : uesList) {
            Map<String, PerunAttributeValue> uesAttributes = adapter.getAttributesValues(Entity.USER_EXT_SOURCE,
                    ues.getId(), uesAttrs);
            boolean equalValues = this.doUesAttributesMatch(uesAttributes, internalIdentifiersToNewValuesMap);
            if (equalValues) {
                matchedIndexes.add(i);
            }
            i++;
        }
        return matchedIndexes;
    }

    private boolean doUesAttributesMatch(@NonNull Map<String, PerunAttributeValue> uesAttributes,
                                         @NonNull Map<String, JsonNode> internalNamesToNewValuesMap)
    {
        if (Collections.disjoint(uesAttributes.keySet(), internalNamesToNewValuesMap.keySet())) {
            return false;
        }

        for (Map.Entry<String, PerunAttributeValue> entry: uesAttributes.entrySet()) {
            String attrIdentifier = entry.getKey();
            if (!internalNamesToNewValuesMap.containsKey(attrIdentifier)) {
                continue;
            }
            PerunAttributeValue oldValue = entry.getValue();
            JsonNode oldValueJson = null;
            JsonNode newValueJson = null;
            if (oldValue != null) {
                oldValueJson = oldValue.valueAsJson();
            }
            if (internalNamesToNewValuesMap.containsKey(attrIdentifier)) {
                newValueJson = internalNamesToNewValuesMap.get(attrIdentifier);
            }
            if (oldValueJson != null && newValueJson != null) {
                boolean match = this.compareAttributeValues(oldValueJson, newValueJson);
                if (match) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean compareAttributeValues(JsonNode oldValueJson, JsonNode newValueJson) {
        if (PerunAttributeValueAwareModel.isNullValue(oldValueJson)) {
            return false;
        } else if (PerunAttributeValueAwareModel.isNullValue(newValueJson)) {
            return false;
        } else if (oldValueJson.isArray() && oldValueJson.size() == 0) {
            return false;
        } else if (newValueJson.isArray() && newValueJson.size() == 0) {
            return false;
        }

        List<String> oldParts = Arrays.asList(oldValueJson.asText().split(UES_VALUES_SEPARATOR));
        List<String> newParts = Arrays.asList(newValueJson.asText().split(UES_VALUES_SEPARATOR));

        return !Collections.disjoint(oldParts, newParts);
    }

    private List<PerunAttribute> getAttributesToUpdate(@NonNull Map<String, PerunAttribute> oldValuesMap,
                                                       @NonNull Map<String, JsonNode> newValuesMap,
                                                       @NonNull Map<String, UpdateAttributeMappingEntry> internalToAttrMappingEntries)
    {
        List<PerunAttribute> attributesToUpdateList = new ArrayList<>();

        for (Map.Entry<String, PerunAttribute> e : oldValuesMap.entrySet()) {
            String attrIdentifier = e.getKey();
            PerunAttribute currentAttribute = e.getValue();
            JsonNode newValueToSet;
            if (!newValuesMap.containsKey(attrIdentifier)) {
                if (internalToAttrMappingEntries.get(attrIdentifier).isAppendOnly()) {
                    continue;
                } else {
                    newValueToSet = JsonNodeFactory.instance.nullNode();
                }
            } else {
                JsonNode newValue = newValuesMap.get(attrIdentifier);
                if (internalToAttrMappingEntries.get(attrIdentifier).isAppendOnly()) {
                    JsonNode oldValue = currentAttribute.getValue();
                    if (PerunAttributeValueAwareModel.isNullValue(oldValue)) {
                        newValueToSet = this.serializeValueForUes(newValue);
                    } else {
                        Set<String> parts = new HashSet<>();
                        if (StringUtils.hasText(oldValue.textValue())) {
                            parts.addAll(this.getPartsFromUesStringVal(oldValue));
                        }
                        parts.addAll(this.getPartsFromUesStringVal(this.serializeValueForUes(newValue)));
                        newValueToSet = JsonNodeFactory.instance.textNode(String.join(UES_VALUES_SEPARATOR, parts));
                    }
                } else {
                    newValueToSet = this.serializeValueForUes(newValue);
                }
            }
            currentAttribute.setValue(currentAttribute.getType(), newValueToSet);
            attributesToUpdateList.add(currentAttribute);
        }
        return attributesToUpdateList;
    }

    private JsonNode serializeValueForUes(JsonNode newValue) {
        if (newValue == null || newValue.isNull()) {
            return JsonNodeFactory.instance.nullNode();
        } else if (newValue.isArray()) {
            if (newValue.size() == 0) {
                return JsonNodeFactory.instance.nullNode();
            } else {
                StringJoiner val = new StringJoiner(UES_VALUES_SEPARATOR);
                for (JsonNode node: newValue) {
                    val.add(node.textValue());
                }
                return JsonNodeFactory.instance.textNode(val.toString());
            }
        }

        return newValue;
    }

    private Collection<String> getPartsFromUesStringVal(JsonNode value) {
        if (value == null || value.isNull()) {
            return new HashSet<>();
        }
        return Arrays.asList(value.textValue().split(UES_VALUES_SEPARATOR));
    }

}
