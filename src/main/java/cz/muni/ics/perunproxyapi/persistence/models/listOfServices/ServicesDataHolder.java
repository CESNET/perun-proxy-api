package cz.muni.ics.perunproxyapi.persistence.models.listOfServices;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Object containing all data needed for the list of services.
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>
 */
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ServicesDataHolder {

    private Map<String, Integer> statistics;
    private List<Map<String, JsonNode>> servicesJson;
    private List<LosFacility> services;
    private Set<String> attributesToShow;
    private List<String> multilingualAttributes;
    private List<String> urlAttributes;
    private String headerPath;
    private String footerPath;
}

