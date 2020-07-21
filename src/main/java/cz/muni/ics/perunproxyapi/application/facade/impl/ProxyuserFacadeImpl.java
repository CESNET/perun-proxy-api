package cz.muni.ics.perunproxyapi.application.facade.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import cz.muni.ics.perunproxyapi.application.facade.AdaptersContainer;
import cz.muni.ics.perunproxyapi.application.facade.ProxyuserFacade;
import cz.muni.ics.perunproxyapi.application.facade.configuration.FacadeConfiguration;
import cz.muni.ics.perunproxyapi.application.service.ProxyUserMiddleware;
import cz.muni.ics.perunproxyapi.persistence.adapters.DataAdapter;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static cz.muni.ics.perunproxyapi.application.facade.configuration.MethodNameConstants.FIND_BY_IDENTIFIERS;

@Component
public class ProxyuserFacadeImpl implements ProxyuserFacade {

    private final Map<String, JsonNode> methodConfigurations;
    private final AdaptersContainer adaptersContainer;
    private final ProxyUserMiddleware userMiddleware;

    @Autowired
    public ProxyuserFacadeImpl(@NonNull ProxyUserMiddleware userMiddleware,
                               @NonNull AdaptersContainer adaptersContainer,
                               @NonNull FacadeConfiguration facadeConfiguration) {
        this.userMiddleware = userMiddleware;
        this.adaptersContainer = adaptersContainer;
        this.methodConfigurations = facadeConfiguration.getProxyUserAdapterMethodConfigurations();
    }

    public User findByIdentifiers(String idpIdentifier, List<String> userIdentifiers) {
        JsonNode options = methodConfigurations.getOrDefault(FIND_BY_IDENTIFIERS, JsonNodeFactory.instance.nullNode());
        DataAdapter adapter = adaptersContainer.getPreferredAdapter(
                options.has("adapter") ? options.get("adapter").asText() : "RPC");

        return userMiddleware.findByIdentifiers(adapter, idpIdentifier, userIdentifiers);
    }

}
