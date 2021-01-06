package cz.muni.ics.perunproxyapi.application.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.muni.ics.perunproxyapi.application.facade.configuration.classes.ListOfServicesJsonAttribute;
import cz.muni.ics.perunproxyapi.application.facade.parameters.ListOfServicesGuiParams;
import cz.muni.ics.perunproxyapi.application.facade.parameters.ListOfServicesJsonParams;
import cz.muni.ics.perunproxyapi.application.facade.parameters.ListOfServicesParams;
import cz.muni.ics.perunproxyapi.application.service.GuiService;
import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.Facility;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue;
import cz.muni.ics.perunproxyapi.persistence.models.listOfServices.ListOfServicesDAO;
import cz.muni.ics.perunproxyapi.persistence.models.listOfServices.LosFacility;
import cz.muni.ics.perunproxyapi.persistence.models.listOfServices.LosSorter;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class GuiServiceImpl implements GuiService {

    private static final String SAML = "SAML";
    private static final String OIDC = "OIDC";
    private static final String TESTING = "TESTING";
    private static final String STAGING = "STAGING";
    private static final String PRODUCTION = "PRODUCTION";

    public static final String SERVICES = "services";
    public static final String STATISTICS = "statistics";

    @Override
    public ListOfServicesDAO getListOfSps(@NonNull ListOfServicesGuiParams params)
            throws PerunUnknownException, PerunConnectionException
    {
        Set<String> attrNames = initAttrNames(params);
        ServicesAndCounters sac = getServicesAndCounters(params, attrNames);
        Map<String, Integer> statistics = createCounterData(sac.getSamlCounter(), sac.getOidcCounter());

        return new ListOfServicesDAO(statistics, sac.getServices(), params.getDisplayedAttributes(),
                params.isShowSaml(), params.isShowOidc(), params.isShowTesting(),
                params.isShowStaging(), params.isShowProduction());
    }

    @Override
    public JsonNode getListOfSpsJson(@NonNull ListOfServicesJsonParams params)
            throws PerunUnknownException, PerunConnectionException
    {
        Set<String> attrNames = initAttrNames(params);
        ServicesAndCounters sac = getServicesAndCounters(params, attrNames);

        sac.getServices().sort(new LosSorter());
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        Map<String, Integer> statistics = createCounterData(sac.getSamlCounter(), sac.getOidcCounter());

        json.set(SERVICES, getServicesJsonArray(sac.getServices(), params.getJsonAttributes()));
        json.set(STATISTICS, new ObjectMapper().convertValue(statistics, ObjectNode.class));

        return json;
    }

    private ServicesAndCounters getServicesAndCounters(ListOfServicesParams params, Set<String> attrNames)
            throws PerunUnknownException, PerunConnectionException {
        List<Facility> facilities = params.getAdapter()
                .searchFacilitiesByAttributeValue(params.getProxyIdentifierAttr(), params.getProxyIdentifierValue());
        List<LosFacility> services = new ArrayList<>();
        ServicesCounter oidcCounter = new ServicesCounter();
        ServicesCounter samlCounter = new ServicesCounter();

        for (Facility facility : facilities) {
            Map<String, PerunAttributeValue> attributes = params.getAdapter()
                    .getAttributesValues(Entity.FACILITY, facility.getId(), new ArrayList<>(attrNames));
            PerunAttributeValue protocol = attributes.get(params.getRpProtocolAttr());
            if (protocol != null && StringUtils.hasText(protocol.valueAsString())) {
                if (SAML.equalsIgnoreCase(protocol.valueAsString())) {
                    processFacility(services, facility, attributes, params, samlCounter, SAML);
                } else if (OIDC.equalsIgnoreCase(protocol.valueAsString())) {
                    processFacility(services, facility, attributes, params, oidcCounter, OIDC);
                } else {
                    log.warn("Unrecognized protocol for Facility {}: {}", facility, protocol.valueAsString());
                }
            } else {
                log.warn("No protocol attribute for Facility: {}", facility);
            }
        }

        return new ServicesAndCounters(services, samlCounter, oidcCounter);
    }

    private ArrayNode getServicesJsonArray(List<LosFacility> services, List<ListOfServicesJsonAttribute> attrs) {
        ArrayNode json = JsonNodeFactory.instance.arrayNode();
        for (LosFacility s: services) {
            ObjectNode serviceJson = JsonNodeFactory.instance.objectNode();
            for (ListOfServicesJsonAttribute a: attrs) {
                serviceJson.set(a.getJsonKey(), s.getAttributes().get(a.getSourceAttrName()).getValue());
            }
            json.add(serviceJson);
        }
        return json;
    }

    private Map<String, Integer> createCounterData(ServicesCounter samlCounter, ServicesCounter oidcCounter) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("samlProductionCount", samlCounter.getProduction());
        stats.put("samlTestingCount", samlCounter.getTesting());
        stats.put("samlStagingCount", samlCounter.getStaging());
        stats.put("oidcProductionCount", oidcCounter.getProduction());
        stats.put("oidcTestingCount", oidcCounter.getTesting());
        stats.put("oidcStagingCount", oidcCounter.getStaging());
        stats.put("allProductionCount", samlCounter.getProduction() + oidcCounter.getProduction());
        stats.put("allTestingCount", samlCounter.getTesting() + oidcCounter.getTesting());
        stats.put("allStagingCount", samlCounter.getStaging() + oidcCounter.getStaging());
        return stats;
    }

    private void processFacility(List<LosFacility> services, Facility facility,
                                 Map<String, PerunAttributeValue> attributes,
                                 ListOfServicesParams params, ServicesCounter sc, String protocol)
    {
        PerunAttributeValue showOnServiceList = attributes.getOrDefault(
                params.getShowOnServiceListAttr(), null);
        if (showOnServiceList != null && showOnServiceList.valueAsBoolean()) {
            services.add(new LosFacility(facility, protocol, attributes));
        }

        PerunAttributeValue environment = attributes.getOrDefault(params.getRpEnvironmentAttr(), null);
        if (environment != null && TESTING.equalsIgnoreCase(environment.valueAsString())) {
            sc.incrementTestingServiceCount();
        } else if (environment != null && STAGING.equalsIgnoreCase(environment.valueAsString())) {
            sc.incrementStagingServicesCount();
        } else if (environment != null && PRODUCTION.equalsIgnoreCase(environment.valueAsString())) {
            sc.incrementProductionServicesCount();
        } else {
            log.warn("Not displaying facility {}, no environment set", facility);
        }
    }

    private Set<String> initAttrNames(ListOfServicesGuiParams params) {
        final Set<String> attrNames = initAttrNamesBase(params);
        params.getDisplayedAttributes().forEach(losAttr -> {
            attrNames.add(losAttr.getSourceAttrName());
            if (StringUtils.hasText(losAttr.getUrlSourceAttr())) {
                attrNames.add(losAttr.getUrlSourceAttr());
            }
        });
        return attrNames;
    }

    private Set<String> initAttrNames(ListOfServicesJsonParams params) {
        final Set<String> attrNames = initAttrNamesBase(params);
        params.getJsonAttributes().forEach(losAttr -> attrNames.add(losAttr.getSourceAttrName()));
        return attrNames;
    }

    private Set<String> initAttrNamesBase(ListOfServicesParams params) {
        Set<String> attrNames = new HashSet<>();
        attrNames.add(params.getRpProtocolAttr());
        attrNames.add(params.getRpEnvironmentAttr());
        attrNames.add(params.getShowOnServiceListAttr());
        return attrNames;
    }

    @Getter
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ServicesCounter {
        private int testing = 0;
        private int staging = 0;
        private int production = 0;

        public void incrementTestingServiceCount() {
            testing++;
        }

        public void incrementStagingServicesCount() {
            staging++;
        }

        public void incrementProductionServicesCount() {
            production++;
        }

    }

    @Getter
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ServicesAndCounters {
        private List<LosFacility> services;
        private ServicesCounter samlCounter;
        private ServicesCounter oidcCounter;
    }

}
