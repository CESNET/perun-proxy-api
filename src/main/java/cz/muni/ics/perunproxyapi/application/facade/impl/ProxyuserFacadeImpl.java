package cz.muni.ics.perunproxyapi.application.facade.impl;

import com.fasterxml.jackson.databind.JsonNode;
import cz.muni.ics.perunproxyapi.application.facade.FacadeUtils;
import cz.muni.ics.perunproxyapi.application.facade.ProxyuserFacade;
import cz.muni.ics.perunproxyapi.application.facade.configuration.FacadeConfiguration;
import cz.muni.ics.perunproxyapi.application.service.ProxyUserService;
import cz.muni.ics.perunproxyapi.persistence.adapters.DataAdapter;
import cz.muni.ics.perunproxyapi.persistence.adapters.impl.AdaptersContainer;
import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ProxyuserFacadeImpl implements ProxyuserFacade {

    public static final String FIND_BY_EXT_LOGINS = "find_by_ext_logins";
    public static final String GET_USER_BY_LOGIN = "get_user_by_login";
    public static final String FIND_BY_PERUN_USER_ID = "find_by_perun_user_id";
    public static final String GET_ALL_ENTITLEMENTS = "get_all_entitlements";

    public static final String IDP_IDENTIFIER = "idpIdentifier";
    public static final String PREFIX = "prefix";
    public static final String AUTHORITY = "authority";
    public static final String FORWARDED_ENTITLEMENTS = "forwarded_entitlements";

    private final Map<String, JsonNode> methodConfigurations;
    private final AdaptersContainer adaptersContainer;
    private final ProxyUserService proxyUserService;
    private final String defaultIdpIdentifier;

    @Autowired
    public ProxyuserFacadeImpl(@NonNull ProxyUserService proxyUserService,
                               @NonNull AdaptersContainer adaptersContainer,
                               @NonNull FacadeConfiguration facadeConfiguration,
                               @Value("${facade.default_idp}") String defaultIdp) {
        this.proxyUserService = proxyUserService;
        this.adaptersContainer = adaptersContainer;
        this.methodConfigurations = facadeConfiguration.getProxyUserAdapterMethodConfigurations();
        this.defaultIdpIdentifier = defaultIdp;
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
        String idpIdentifier = FacadeUtils.getStringOption(IDP_IDENTIFIER, defaultIdpIdentifier, options);

        List<User> users = proxyUserService.getUsersByAttributeValue(adapter, idpIdentifier , login);
        User user = users.size() == 1 ? users.get(0) : null;
        UserDTO userDTO = null;

        if (user != null) {
            userDTO = new UserDTO(login, new HashMap<>());

            if (fields != null && !fields.isEmpty()){
                Map<String, PerunAttributeValue> attributeValues =
                        proxyUserService.getAttributesValues(adapter, Entity.USER , user.getPerunId() , fields);
                userDTO.setPerunAttributes(attributeValues);
            }
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

        List<String> entitlements =  proxyUserService.getAllEntitlements(adapter, user.getPerunId(), prefix, authority,
                forwardedEntitlementsAttrIdentifier);
        if (entitlements != null) {
            Collections.sort(entitlements);
        }
        return entitlements;
    }

}
