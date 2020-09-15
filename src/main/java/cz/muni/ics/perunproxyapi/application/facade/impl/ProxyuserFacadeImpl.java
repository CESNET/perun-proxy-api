package cz.muni.ics.perunproxyapi.application.facade.impl;

import com.fasterxml.jackson.databind.JsonNode;
import cz.muni.ics.perunproxyapi.application.facade.FacadeUtils;
import cz.muni.ics.perunproxyapi.application.facade.ProxyuserFacade;
import cz.muni.ics.perunproxyapi.application.facade.configuration.FacadeConfiguration;
import cz.muni.ics.perunproxyapi.application.service.ProxyUserService;
import cz.muni.ics.perunproxyapi.persistence.adapters.DataAdapter;
import cz.muni.ics.perunproxyapi.persistence.adapters.impl.AdaptersContainer;
import cz.muni.ics.perunproxyapi.persistence.exceptions.EntityNotFoundException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import cz.muni.ics.perunproxyapi.presentation.DTOModels.UserDTO;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ProxyuserFacadeImpl implements ProxyuserFacade {

    public static final String FIND_BY_EXT_LOGINS = "find_by_ext_logins";
    public static final String FIND_BY_IDENTIFIERS = "find_by_identifiers";
    public static final String GET_USER_BY_LOGIN = "get_user_by_login";
    public static final String FIND_BY_PERUN_USER_ID = "find_by_perun_user_id";
    public static final String GET_ALL_ENTITLEMENTS = "get_all_entitlements";

    public static final String PREFIX = "prefix";
    public static final String AUTHORITY = "authority";
    public static final String FORWARDED_ENTITLEMENTS = "forwarded_entitlements";
    public static final String DEFAULT_FIELDS = "default_fields";

    private final Map<String, JsonNode> methodConfigurations;
    private final AdaptersContainer adaptersContainer;
    private final ProxyUserService proxyUserService;

    @Autowired
    public ProxyuserFacadeImpl(@NonNull ProxyUserService proxyUserService,
                               @NonNull AdaptersContainer adaptersContainer,
                               @NonNull FacadeConfiguration facadeConfiguration)
    {
        this.proxyUserService = proxyUserService;
        this.adaptersContainer = adaptersContainer;
        this.methodConfigurations = facadeConfiguration.getProxyUserAdapterMethodConfigurations();
    }

    @Override
    public UserDTO findByExtLogins(String idpIdentifier, List<String> userIdentifiers)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException
    {
        JsonNode options = FacadeUtils.getOptions(FIND_BY_EXT_LOGINS, methodConfigurations);
        DataAdapter adapter = FacadeUtils.getAdapter(adaptersContainer, options);

        User user = proxyUserService.findByExtLogins(adapter, idpIdentifier, userIdentifiers);
        if (user == null) {
            throw new EntityNotFoundException("No user has been found for given identifiers");
        }

        return FacadeUtils.mapUserToUserDTO(user);
    }

    @Override
    public UserDTO findByIdentifiers(@NonNull String idpIdentifier, @NonNull List<String> identifiers)
            throws EntityNotFoundException
    {
        JsonNode options = FacadeUtils.getOptions(FIND_BY_IDENTIFIERS, methodConfigurations);
        //TODO: currently works only with LDAP
        //DataAdapter adapter = FacadeUtils.getAdapter(adaptersContainer, options);
        DataAdapter adapter = adaptersContainer.getLdapAdapter();

        List<String> fieldsToFetch = this.getDefaultFields(options);
        User user = proxyUserService.findByIdentifiers(adapter, idpIdentifier, identifiers, fieldsToFetch);
        if (user == null) {
            throw new EntityNotFoundException("No user has been found for given identifiers");
        }
        return FacadeUtils.mapUserToUserDTO(user);
    }

    @Override
    public UserDTO getUserByLogin(@NonNull String login, List<String> fields)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException
    {
        JsonNode options = FacadeUtils.getOptions(GET_USER_BY_LOGIN, methodConfigurations);
        DataAdapter adapter = FacadeUtils.getAdapter(adaptersContainer, options);
        List<String> fieldsToFetch = (fields != null && !fields.isEmpty()) ? fields : this.getDefaultFields(options);

        User user = proxyUserService.getUserWithAttributesByLogin(adapter, login, fieldsToFetch);
        if (user == null) {
            throw new EntityNotFoundException("No user has been found for given login");
        }

        return FacadeUtils.mapUserToUserDTO(user);
    }

    @Override
    public UserDTO findByPerunUserId(Long userId)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException
    {
        JsonNode options = FacadeUtils.getOptions(FIND_BY_PERUN_USER_ID, methodConfigurations);
        DataAdapter adapter = FacadeUtils.getAdapter(adaptersContainer, options);

        User user = proxyUserService.findByPerunUserId(adapter, userId);
        if (user == null) {
            throw new EntityNotFoundException("No user has been found for given user ID");
        }

        return FacadeUtils.mapUserToUserDTO(user);
    }

    @Override
    public List<String> getAllEntitlements(String login)
            throws PerunUnknownException, PerunConnectionException, EntityNotFoundException
    {
        JsonNode options = FacadeUtils.getOptions(GET_ALL_ENTITLEMENTS, methodConfigurations);
        DataAdapter adapter = FacadeUtils.getAdapter(adaptersContainer, options);

        String prefix = FacadeUtils.getRequiredStringOption(PREFIX, options);
        String authority = FacadeUtils.getRequiredStringOption(AUTHORITY, options);
        String forwardedEntitlementsAttrIdentifier = FacadeUtils.getStringOption(FORWARDED_ENTITLEMENTS, options);

        User user = proxyUserService.getUserByLogin(adapter, login);
        if (user == null) {
            throw new EntityNotFoundException("User for given login could not be found");
        }

        List<String> entitlements = proxyUserService.getAllEntitlements(adapter, user.getPerunId(), prefix, authority,
                forwardedEntitlementsAttrIdentifier);
        if (entitlements != null) {
            Collections.sort(entitlements);
        }
        return entitlements;
    }

    private List<String> getDefaultFields(JsonNode options) {
        List<String> fields = new ArrayList<>();
        if (!options.hasNonNull(DEFAULT_FIELDS)) {
                log.error("Required option {} has not been found by the getEntitlements method. " +
                        "Check your configuration.", DEFAULT_FIELDS);
                throw new IllegalArgumentException("Required option has not been found");
        }

        for (JsonNode subNode : options.get(DEFAULT_FIELDS)) {
            if (!subNode.isNull()){
                String attr = subNode.asText();
                if (StringUtils.hasText(attr)) {
                    fields.add(attr);
                }
            }
        }
        return fields;
    }

}
