package cz.muni.ics.perunproxyapi.ga4gh;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import cz.muni.ics.perunproxyapi.ga4gh.model.config.ElixirGa4ghConfig;
import cz.muni.ics.perunproxyapi.ga4gh.model.Ga4ghProperties;
import cz.muni.ics.perunproxyapi.ga4gh.service.impl.ElixirGa4ghService;
import cz.muni.ics.perunproxyapi.ga4gh.service.Ga4ghService;
import cz.muni.ics.perunproxyapi.ga4gh.service.impl.UnsupportedGa4ghService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * Container for beans specific to GA4GH.
 * Constructs the chosen implementation of GA4GH
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Component
public class Ga4ghBeans {

    @Autowired
    @Bean
    public Ga4ghService ga4ghService(@NonNull Ga4ghProperties properties,
                                     @NonNull JWKSetPublishingEndpoint publishingEndpoint)
            throws IOException
    {
        String configFilePath = properties.getConfigFilePath();
        switch (properties.getType()) {
            case ELIXIR: {
                ElixirGa4ghConfig config = loadConfig(configFilePath);
                return new ElixirGa4ghService(config, publishingEndpoint);
            }
            case CESNET:
            case EDUTEAMS:
            case BBMRI_ERIC:
            default:
                return new UnsupportedGa4ghService();
        }
    }

    private <T> T loadConfig(String configFilePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(new File(configFilePath), new TypeReference<>() {});
    }

}
