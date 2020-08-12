package cz.muni.ics.perunproxyapi.application.facade.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import cz.muni.ics.perunproxyapi.application.facade.AdaptersContainer;
import cz.muni.ics.perunproxyapi.application.facade.ProxyuserFacade;
import cz.muni.ics.perunproxyapi.application.facade.configuration.FacadeConfiguration;
import cz.muni.ics.perunproxyapi.application.service.ProxyUserMiddleware;
import cz.muni.ics.perunproxyapi.persistence.adapters.DataAdapter;
import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import cz.muni.ics.perunproxyapi.presentation.DTOModels.UserDTO;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static cz.muni.ics.perunproxyapi.application.facade.configuration.MethodNameConstants.FIND_BY_IDENTIFIERS;
import static cz.muni.ics.perunproxyapi.application.facade.configuration.MethodNameConstants.GET_USER_BY_LOGIN;

@Component
public class ProxyuserFacadeImpl implements ProxyuserFacade {

    private final Map<String, JsonNode> methodConfigurations;
    private final AdaptersContainer adaptersContainer;
    private final ProxyUserMiddleware userMiddleware;

    private final String defaultIdpIdentifier;

    @Autowired
    public ProxyuserFacadeImpl(@NonNull ProxyUserMiddleware userMiddleware,
                               @NonNull AdaptersContainer adaptersContainer,
                               @NonNull FacadeConfiguration facadeConfiguration,
                               @Value("${facade.config_path.proxyuser.default_idp}") String defaultIdp) {
        this.userMiddleware = userMiddleware;
        this.adaptersContainer = adaptersContainer;
        this.methodConfigurations = facadeConfiguration.getProxyUserAdapterMethodConfigurations();

        this.defaultIdpIdentifier = defaultIdp;
    }

    public User findByExtLogins(String idpIdentifier, List<String> userIdentifiers) {
        JsonNode options = methodConfigurations.getOrDefault(FIND_BY_IDENTIFIERS, JsonNodeFactory.instance.nullNode());
        DataAdapter adapter = adaptersContainer.getPreferredAdapter(
                options.has("adapter") ? options.get("adapter").asText() : "RPC");

        return userMiddleware.findByExtLogins(adapter, idpIdentifier, userIdentifiers);
    }

    public UserDTO getUserByLogin(String login, List<String> fields) {
        JsonNode options = methodConfigurations.getOrDefault(GET_USER_BY_LOGIN, JsonNodeFactory.instance.nullNode());
        DataAdapter adapter = adaptersContainer.getPreferredAdapter(
                options.has("adapter") ? options.get("adapter").asText() : "RPC");
        String idpIdentifier =
                options.has("idpIdentifier") ? options.get("idpIdentifier").asText() : defaultIdpIdentifier;

        User user = userMiddleware.getUserByAttribute(adapter, idpIdentifier , login);
        UserDTO userDTO =
                new UserDTO(login,
                        user.getFirstName(),
                        user.getLastName(),
                        String.format("%s %s",user.getFirstName(), user.getLastName()),
                        user.getId());

        if (! fields.isEmpty()){
            Map<String, PerunAttributeValue> attributeValues =
                    userMiddleware.getAttributesValues(adapter, Entity.USER , user.getId() , fields);
            userDTO.setPerunAttributes(attributeValues);
        }

        return userDTO;
    }
}
