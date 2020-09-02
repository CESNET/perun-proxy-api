package cz.muni.ics.perunproxyapi.application.facade.impl;

import com.fasterxml.jackson.databind.JsonNode;
import cz.muni.ics.perunproxyapi.application.facade.FacadeUtils;
import cz.muni.ics.perunproxyapi.application.facade.ProxyuserFacade;
import cz.muni.ics.perunproxyapi.application.facade.configuration.FacadeConfiguration;
import cz.muni.ics.perunproxyapi.application.service.ProxyUserService;
import cz.muni.ics.perunproxyapi.persistence.adapters.DataAdapter;
import cz.muni.ics.perunproxyapi.persistence.adapters.impl.AdaptersContainer;
import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
import cz.muni.ics.perunproxyapi.persistence.exceptions.InvalidNumberOfValuesException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import cz.muni.ics.perunproxyapi.presentation.DTOModels.UserDTO;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ProxyuserFacadeImpl implements ProxyuserFacade {

    private final Map<String, JsonNode> methodConfigurations;
    private final AdaptersContainer adaptersContainer;
    private final ProxyUserService proxyUserService;
    private final String loginIdentifier;
    private final String defaultIdpIdentifier;

    public static final String FIND_BY_EXT_LOGINS = "find_by_ext_logins";
    public static final String GET_USER_BY_LOGIN = "get_user_by_login";
    public static final String FIND_BY_PERUN_USER_ID = "find_by_perun_user_id";
    public static final String GET_ALL_ENTITLEMENTS = "get_all_entitlements";

    public static final String IDP_IDENTIFIER = "idpIdentifier";
    public static final String PREFIX = "prefix";
    public static final String AUTHORITY = "authority";
    public static final String FORWARDED_ENTITLEMENTS = "forwarded_entitlements";
    public static final String DEFAULT_FIELDS = "default_fields";

    @Autowired
    public ProxyuserFacadeImpl(@NonNull ProxyUserService proxyUserService,
                               @NonNull AdaptersContainer adaptersContainer,
                               @NonNull FacadeConfiguration facadeConfiguration,
                               @Value("${facade.proxy_user_login_identifier}") String loginIdentifier,
                               @Value("${facade.default_idp}") String defaultIdpIdentifier)
    {
        this.proxyUserService = proxyUserService;
        this.adaptersContainer = adaptersContainer;
        this.methodConfigurations = facadeConfiguration.getProxyUserAdapterMethodConfigurations();
        this.loginIdentifier = loginIdentifier;
        this.defaultIdpIdentifier = defaultIdpIdentifier;
    }

    @Override
    public User findByExtLogins(String idpIdentifier, List<String> userIdentifiers) throws PerunUnknownException, PerunConnectionException {
        JsonNode options = FacadeUtils.getOptions(FIND_BY_EXT_LOGINS, methodConfigurations);
        DataAdapter adapter = FacadeUtils.getAdapter(adaptersContainer, options);

        log.debug("Calling proxyUserService.findByExtLogins on adapter {}", adapter.getClass());

        return proxyUserService.findByExtLogins(adapter, idpIdentifier, userIdentifiers);
    }

    @Override
    public UserDTO getUserByLogin(String login, List<String> fields) throws PerunUnknownException, PerunConnectionException {
        JsonNode options = FacadeUtils.getOptions(GET_USER_BY_LOGIN, methodConfigurations);
        DataAdapter adapter = FacadeUtils.getAdapter(adaptersContainer, options);
        log.debug("Calling userMiddleware.getUserByLogin on adapter {}", adapter.getClass());

        List<User> users = proxyUserService.getUsersByAttributeValue(adapter, loginIdentifier , login);

        User user;
        if (users.size() < 1) {
            log.debug("No user with login {} found.", login);
            return null;
        } else if (users.size() > 1) {
            throw new InvalidNumberOfValuesException(String.format("More users with the same login %s found.", login));
        } else {
            user = users.get(0);
        }

        UserDTO userDTO = null;
        if (user != null) {
            userDTO = new UserDTO(login, new HashMap<>());

            List<String> fieldsToUse = (fields != null) && !fields.isEmpty() ?
                    fields : this.getDefaultFields(options);

            Map<String, PerunAttributeValue> attributesMap =
                    proxyUserService.getAttributesValues(adapter, Entity.USER , user.getPerunId() , fieldsToUse);

            // Get only values from the PerunAttributeValue object
            Map<String, JsonNode> attributes = attributesMap.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().getValue()
                    ));

            userDTO.setAttributes(attributes);

        }

        return userDTO;
    }

    @Override
    public User findByPerunUserId(Long userId) throws PerunUnknownException, PerunConnectionException {
        JsonNode options = FacadeUtils.getOptions(FIND_BY_PERUN_USER_ID, methodConfigurations);
        DataAdapter adapter = FacadeUtils.getAdapter(adaptersContainer, options);

        log.debug("Calling proxyUserService.findByPerunUserId on adapter {}", adapter.getClass());

        return proxyUserService.findByPerunUserId(adapter, userId);
    }

    @Override
    public List<String> getAllEntitlements(String login) throws PerunUnknownException, PerunConnectionException {
        JsonNode options = FacadeUtils.getOptions(GET_ALL_ENTITLEMENTS, methodConfigurations);
        DataAdapter adapter = FacadeUtils.getAdapter(adaptersContainer, options);

        String prefix = FacadeUtils.getRequiredStringOption(PREFIX, options);
        String authority = FacadeUtils.getRequiredStringOption(AUTHORITY, options);

        User user = proxyUserService.findByExtLogin(adapter, defaultIdpIdentifier, login);
        if (user == null) {
            log.error("No user found for login {} with Idp {}. Cannot look for entitlements, return error.",
                    login, defaultIdpIdentifier);
            throw new IllegalArgumentException("User for given login could not be found");
        }

        String forwardedEntitlementsAttrIdentifier = FacadeUtils.getStringOption(FORWARDED_ENTITLEMENTS, options);

        List<String> entitlements = proxyUserService.getAllEntitlements(adapter, user.getPerunId(), prefix, authority,
                forwardedEntitlementsAttrIdentifier);
        if (entitlements != null) {
            Collections.sort(entitlements);
        }
        return entitlements;
    }

    private List<String> getDefaultFields(JsonNode options) {
        List<String> fields = new ArrayList<>();
        if (! options.hasNonNull(DEFAULT_FIELDS)) {
            log.warn("Default fields are missing in the configuration file. Returning empty list.");
            return fields;
        }

        for (JsonNode subNode : options.get(DEFAULT_FIELDS)) {
            String attr = subNode.asText();
            if (StringUtils.hasText(attr)) {
                fields.add(attr);
            }
        }
        return fields;
    }

}
