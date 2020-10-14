package cz.muni.ics.perunproxyapi.application.facade;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import cz.muni.ics.perunproxyapi.persistence.adapters.DataAdapter;
import cz.muni.ics.perunproxyapi.persistence.adapters.impl.AdaptersContainer;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue;
import cz.muni.ics.perunproxyapi.persistence.models.User;
import cz.muni.ics.perunproxyapi.presentation.DTOModels.UserDTO;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

import static cz.muni.ics.perunproxyapi.application.facade.configuration.FacadeConfiguration.ADAPTER_RPC;

/**
 * Utility class for facade.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public class FacadeUtils {

    public static final String ADAPTER = "adapter";

    /**
     * Get preferred adapter from options of the method.
     * @param adaptersContainer Container of adapters.
     * @param options Options of method.
     * @return Extracted correct adapter.
     */
    public static DataAdapter getAdapter(@NotNull AdaptersContainer adaptersContainer, @NotNull JsonNode options) {
        return adaptersContainer.getPreferredAdapter(
                options.has(ADAPTER) ? options.get(ADAPTER).asText() : ADAPTER_RPC);
    }

    /**
     * Get options for method.
     * @param methodName Key under which options are stored.
     * @param methodConfigurations Map containing all the options.
     * @return JSON with options or null node (to prevent NullPointerException).
     */
    public static JsonNode getOptions(@NonNull String methodName, @NonNull Map<String, JsonNode> methodConfigurations) {
        return methodConfigurations.getOrDefault(methodName, JsonNodeFactory.instance.nullNode());
    }

    /**
     * Get String option from method options. If option is not configured, an exception is thrown.
     * @param key Key identifying the option.
     * @param options JSON containing the method options.
     * @return Extracted option as String.
     */
    public static String getRequiredStringOption(@NonNull String key, @NonNull String method,
                                                 @NonNull JsonNode options)
    {
        String option = options.hasNonNull(key) ? options.get(key).asText() : null;
        if (option == null) {
            log.error("Required option {} has not been found by method {}. Check your configuration.", key, method);
            throw new IllegalArgumentException("Required option has not been found");
        }

        return option;
    }

    /**
     * Get String option from method options. If option is not configured, NULL is returned.
     * For fetching required options as Strings see "FacadeUtils.getRequiredStringOption(...)" method.
     * @param key Key identifying the option.
     * @param options JSON containing the method options.
     * @return Extracted option as String, NULL if option is not present.
     */
    public static String getStringOption(String key, JsonNode options) {
        return options.hasNonNull(key) ? options.get(key).asText() : null;
    }

    /**
     * Get String option from method options. If option is not configured, defaultValue is returned.
     * For fetching required options as Strings see "FacadeUtils.getRequiredStringOption(...)" method.
     * @param key Key identifying the option.
     * @param defaultValue Default value that will be returned if option is not configured.
     * @param options JSON containing the method options.
     * @return Extracted option as String, defaultValue if option is not present.
     */
    public static String getStringOption(String key, String defaultValue, JsonNode options) {
        return options.hasNonNull(key) ? options.get(key).asText() : defaultValue;
    }

    /**
     * Map User object to UserDTO to be returned by API call.
     * @param user User object.
     * @return UserDTO or null
     */
    public static UserDTO mapUserToUserDTO(User user) {
        if (user != null){
            return new UserDTO(user.getLogin(), FacadeUtils.mapAttributeValuesToJsonNodes(user.getAttributes()));
        }
        return null;
    }

    /**
     * Transform Map<String, PerunAttributeValue> to Map<String, JsonNode>.
     * @param attrValueMap Map to be transformed with PerunAttributeValue value.
     * @return Transformed map with JsonNode as value.
     */
    public static Map<String, JsonNode> mapAttributeValuesToJsonNodes(Map<String, PerunAttributeValue> attrValueMap) {
        Map<String, JsonNode> attributes = new HashMap<>();
        if (attrValueMap != null) {
            attrValueMap.forEach((key, value) -> attributes.put(key, (value != null) ?
                    value.valueAsJson() : JsonNodeFactory.instance.nullNode())
            );
            return attributes;
        }

        log.warn("No attributes found. Returning empty map.");
        return attributes;
    }

}
