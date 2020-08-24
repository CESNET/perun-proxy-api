package cz.muni.ics.perunproxyapi.application.facade.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import cz.muni.ics.perunproxyapi.persistence.adapters.impl.AdaptersContainer;
import cz.muni.ics.perunproxyapi.application.facade.ProxyuserFacade;
import cz.muni.ics.perunproxyapi.application.facade.configuration.FacadeConfiguration;
import cz.muni.ics.perunproxyapi.application.service.ProxyUserMiddleware;
import cz.muni.ics.perunproxyapi.persistence.adapters.DataAdapter;
import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import cz.muni.ics.perunproxyapi.presentation.DTOModels.UserDTO;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cz.muni.ics.perunproxyapi.application.facade.configuration.FacadeConfiguration.ADAPTER_RPC;

@Component
@Slf4j
public class ProxyuserFacadeImpl implements ProxyuserFacade {

    private final Map<String, JsonNode> methodConfigurations;
    private final AdaptersContainer adaptersContainer;
    private final ProxyUserMiddleware userMiddleware;

    private final String defaultIdpIdentifier;

    public static final String FIND_BY_EXT_LOGINS = "find_by_ext_logins";
    public static final String GET_USER_BY_LOGIN = "get_user_by_login";

    public static final String ADAPTER = "adapter";
    public static final String IDP_IDENTIFIER = "idpIdentifier";

    @Autowired
    public ProxyuserFacadeImpl(@NonNull ProxyUserMiddleware userMiddleware,
                               @NonNull AdaptersContainer adaptersContainer,
                               @NonNull FacadeConfiguration facadeConfiguration,
                               @Value("${facade.default_idp}") String defaultIdp) {
        this.userMiddleware = userMiddleware;
        this.adaptersContainer = adaptersContainer;
        this.methodConfigurations = facadeConfiguration.getProxyUserAdapterMethodConfigurations();

        this.defaultIdpIdentifier = defaultIdp;
    }

    @Override
    public User findByExtLogins(String idpIdentifier, List<String> userIdentifiers) throws PerunUnknownException, PerunConnectionException {
        JsonNode options = methodConfigurations.getOrDefault(FIND_BY_EXT_LOGINS, JsonNodeFactory.instance.nullNode());
        DataAdapter adapter = adaptersContainer.getPreferredAdapter(
                options.has(ADAPTER) ? options.get(ADAPTER).asText() : ADAPTER_RPC);

        log.debug("Calling userMiddleware.findByExtLogins on adapter {}", adapter.getClass());

        return userMiddleware.findByExtLogins(adapter, idpIdentifier, userIdentifiers);
    }

    @Override
    public UserDTO getUserByLogin(String login, List<String> fields) throws PerunUnknownException, PerunConnectionException {
        JsonNode options = methodConfigurations.getOrDefault(GET_USER_BY_LOGIN, JsonNodeFactory.instance.nullNode());
        DataAdapter adapter = adaptersContainer.getPreferredAdapter(
                options.has(ADAPTER) ? options.get(ADAPTER).asText() : ADAPTER_RPC);
        String idpIdentifier =
                options.has(IDP_IDENTIFIER) ? options.get(IDP_IDENTIFIER).asText() : defaultIdpIdentifier;

        User user = userMiddleware.findByExtLogin(adapter, idpIdentifier , login);
        UserDTO userDTO = null;

        if (user != null) {
            userDTO = new UserDTO(
                    login,
                    user.getFirstName(),
                    user.getLastName(),
                    String.format("%s %s", user.getFirstName(), user.getLastName()),
                    user.getId(),
                    new HashMap<>()
            );

            if (fields != null && !fields.isEmpty()){
                Map<String, PerunAttributeValue> attributeValues =
                        userMiddleware.getAttributesValues(adapter, Entity.USER , user.getId() , fields);
                userDTO.setPerunAttributes(attributeValues);
            }
        }

        return userDTO;
    }

    @Override
    public User findByPerunUserId(long userId) {
        JsonNode options = methodConfigurations.getOrDefault(FIND_BY_PERUN_USER_ID, JsonNodeFactory.instance.nullNode());
        DataAdapter adapter = adaptersContainer.getPreferredAdapter(
                options.has(ADAPTER) ? options.get(ADAPTER).asText() : RPC);

        log.debug("Calling userMiddleware.findByPerunUserId on adapter {}", adapter.getClass());

        return userMiddleware.findByPerunUserId(adapter, userId);
    }

}
