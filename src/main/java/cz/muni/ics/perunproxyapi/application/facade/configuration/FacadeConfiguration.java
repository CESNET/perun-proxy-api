package cz.muni.ics.perunproxyapi.application.facade.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Configuration class for the Facade layer. Initializes preferred adapters for the methods.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Configuration
@Getter
@Slf4j
public class FacadeConfiguration {

    public static final String ADAPTER_RPC = "RPC";
    public static final String ADAPTER_LDAP = "LDAP";

    @Value("${facade.config_path.proxyuser}")
    private String proxyUserPath;

    @Value("${facade.config_path.relying_party}")
    private String relyingPartyPath;

    private final Map<String, JsonNode> proxyUserAdapterMethodConfigurations = new HashMap<>();
    private final Map<String, JsonNode> relyingPartyAdapterMethodConfigurations = new HashMap<>();

    @PostConstruct
    public void postInit() {
        if (proxyUserPath != null && !proxyUserPath.isEmpty()) {
            initAdapterPreferences(proxyUserPath, proxyUserAdapterMethodConfigurations);
        } else {
            log.warn("No path for ProxyuserFacade file given, no adapter preferences initialized");
        }

        if (relyingPartyPath != null && !relyingPartyPath.isEmpty()) {
            initAdapterPreferences(relyingPartyPath, relyingPartyAdapterMethodConfigurations);
        } else {
            log.warn("No path for RelyingPartyFacade file given, no adapter preferences initialized");
        }
    }

    private void initAdapterPreferences(String path, Map<String, JsonNode> preferencesMap) {
        try {
            List<ConfigFileEntry> entries = getEntriesFromYaml(path);
            if (entries != null) {
                if (entries.isEmpty()) {
                    log.debug("No preferences initialized for file {}, probably incorrect format of empty file", path);
                } else {
                    for (ConfigFileEntry entry : entries) {
                        preferencesMap.put(entry.getMethodName(), entry.getConfiguration());
                    }
                    log.debug("Adapter preferences initialized: {}", preferencesMap);
                }
            }
        } catch (IOException ex) {
            log.warn("Reading preferences from config was not successful.", ex);
        }
    }

    private List<ConfigFileEntry> getEntriesFromYaml(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        List<ConfigFileEntry> entries = mapper.readValue(new File(path), new TypeReference<>() {});
        return entries.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Data
    @NoArgsConstructor
    private static class ConfigFileEntry {
        private String methodName;
        private JsonNode configuration;
    }

}
