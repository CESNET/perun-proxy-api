package cz.muni.ics.perunproxyapi.persistence.models.listOfServices;

import cz.muni.ics.perunproxyapi.persistence.models.Facility;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttribute;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * Model representing object of facility with additional attributes.
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>
 */
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class LosFacility {

    private Facility facility;
    private String name;
    private String loginUrl;
    private boolean showOnServiceList;
    private String authProtocol;
    private Map<String, PerunAttribute> attributes;

}
