package cz.muni.ics.perunproxyapi.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import cz.muni.ics.perunproxyapi.persistence.models.AttributeObjectMapping;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service providing methods to use AttributeObjectMapping objects when fetching attributes.
 *
 * Attributes are listed in a separate .yml file in the following way:
 *  - identifier: identifier1
 *    rpcName: rpcName1
 *    ldapName: ldapName1
 *    attrType: type1
 *  - identifier: identifier2
 *    rpcName: rpcName2
 *    ldapName: ldapName2
 *    attrType: type2
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 * @author Dominik Baranek <baranek@ics.muni.cz>
 */
@NoArgsConstructor
@ToString
@Getter
@Component
@Slf4j
public class AttributeMappingService {

    private final Map<String, AttributeObjectMapping> attributeMap = new HashMap<>();

    @Value("${attributes.path}")
    private String path;

    /**
     * Initializes attributes and stores them in attributeMap property.
     */
    @PostConstruct
    public void postInit() {
        if (path != null && !path.isEmpty()) {
            initAttrMappings(path);
        } else {
            log.warn("No path for AttributeMapping file given, no mappings initialized");
        }
    }

    /**
     * Finds AttributeObjectMapping object by attribute identifier.
     *
     * @param identifier String identifier of attribute
     * @return AttributeObjectMapping attribute
     */
    public AttributeObjectMapping getMappingByIdentifier(String identifier) {
        if (!attributeMap.containsKey(identifier)) {
            throw new IllegalArgumentException("Unknown identifier, check your configuration");
        }

        return attributeMap.get(identifier);
    }

    /**
     * Finds AttributeObjectMapping objects by collection of attribute identifiers.
     *
     * @param identifiers Collection of Strings identifiers of attributes
     * @return Set of AttributeObjectMapping objects
     */
    public Set<AttributeObjectMapping> getMappingsByIdentifiers(Collection<String> identifiers) {
        Set<AttributeObjectMapping> mappings = new HashSet<>();
        if (identifiers != null) {
            for (String identifier : identifiers) {
                try {
                    mappings.add(getMappingByIdentifier(identifier));
                } catch (IllegalArgumentException e) {
                    log.warn("Caught {} when getting mappings, check your configuration for identifier {}",
                            e.getClass(), identifier, e);
                }
            }
        }

        return mappings;
    }

    /**
     * Handles initialization of attributes into attributeMap.
     *
     * @param path String path to file with attributes
     */
    private void initAttrMappings(String path) {
        try {
            List<AttributeObjectMapping> attrsMapping = getAttributesFromYamlFile(path);
            if (attrsMapping != null) {
                for (AttributeObjectMapping aom : attrsMapping) {
                    if (aom.getLdapName() != null && aom.getLdapName().trim().isEmpty()) {
                        aom.setLdapName(null);
                    }
                    attributeMap.put(aom.getRpcName(), aom);
                }
                log.trace("Attributes were initialized: {}", attributeMap.toString());
            }
        } catch (IOException ex) {
            log.warn("Reading attributes from config was not successful.");
        }
    }

    /**
     * Reads YAML file and map it into AttributeMappingFromYAML object.
     *
     * @param path String path to YAML file with attributes
     * @return AttributesMappingFromYAML object with mapped attributes
     * @throws IOException thrown when file does not exist, is empty or does not have the right structure
     */
    private List<AttributeObjectMapping> getAttributesFromYamlFile(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        return mapper.readValue(new File(path), new TypeReference<>() {});
    }
}
