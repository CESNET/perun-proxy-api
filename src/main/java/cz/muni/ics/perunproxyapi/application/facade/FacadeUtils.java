package cz.muni.ics.perunproxyapi.application.facade;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import cz.muni.ics.perunproxyapi.persistence.adapters.DataAdapter;
import cz.muni.ics.perunproxyapi.persistence.adapters.impl.AdaptersContainer;

import javax.validation.constraints.NotNull;
import java.util.Map;

import static cz.muni.ics.perunproxyapi.application.facade.configuration.FacadeConfiguration.ADAPTER_RPC;

/**
 * Utility class for facade.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
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
    public static JsonNode getOptions(String methodName, Map<String, JsonNode> methodConfigurations) {
        return methodConfigurations.getOrDefault(methodName, JsonNodeFactory.instance.nullNode());
    }

}
