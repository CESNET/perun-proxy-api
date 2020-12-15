package cz.muni.ics.perunproxyapi.persistence.models.listOfServices;

import cz.muni.ics.perunproxyapi.application.facade.configuration.classes.LosAttribute;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

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
    private List<LosFacility> services;
    private List<LosAttribute> attributesToShow;
    private boolean showSaml;
    private boolean showOidc;
    private boolean showTesting;
    private boolean showStaging;
    private boolean showProduction;

}

