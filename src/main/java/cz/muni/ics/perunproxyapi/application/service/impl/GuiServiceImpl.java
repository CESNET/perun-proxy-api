package cz.muni.ics.perunproxyapi.application.service.impl;

import cz.muni.ics.perunproxyapi.application.facade.configuration.classes.LosAttribute;
import cz.muni.ics.perunproxyapi.application.facade.parameters.ServicesParams;
import cz.muni.ics.perunproxyapi.application.service.GuiService;
import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.Facility;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttributeValue;
import cz.muni.ics.perunproxyapi.persistence.models.listOfServices.LosFacility;
import cz.muni.ics.perunproxyapi.persistence.models.listOfServices.LosSorter;
import cz.muni.ics.perunproxyapi.persistence.models.listOfServices.ServicesDataHolder;
import lombok.NonNull;
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

    @Override
    public ServicesDataHolder getListOfSps(@NonNull ServicesParams servicesParams)
            throws PerunUnknownException, PerunConnectionException
    {
        List<Facility> facilities = servicesParams.getAdapter().searchFacilitiesByAttributeValue(
                servicesParams.getPerunProxyIdentifierAttr(), servicesParams.getProxyIdentifier());

        Set<String> attrNames = initAttrNames(servicesParams);

        List<LosFacility> samlServices = new ArrayList<>();
        List<LosFacility> oidcServices = new ArrayList<>();

        ServicesCounter oidcCounter = new ServicesCounter();
        ServicesCounter samlCounter = new ServicesCounter();

        for (Facility facility : facilities) {
            Map<String, PerunAttributeValue> attributes = servicesParams.getAdapter()
                    .getAttributesValues(Entity.FACILITY, facility.getId(), new ArrayList<>(attrNames));
            PerunAttributeValue entityId = attributes.get(servicesParams.getSaml2EntityIdAttr());
            if (entityId != null && StringUtils.hasText(entityId.valueAsString())) {
                processFacility(samlServices, facility, attributes, servicesParams, samlCounter, SAML);
            }

            PerunAttributeValue clientId = attributes.get(servicesParams.getOidcClientIdAttr());
            if (clientId != null && StringUtils.hasText(clientId.valueAsString())) {
                processFacility(oidcServices, facility, attributes, servicesParams, oidcCounter, OIDC);
            }
        }

        List<LosFacility> services = new ArrayList<>();
        services.addAll(samlServices);
        services.addAll(oidcServices);
        services.sort(new LosSorter());

        Map<String, Integer> statistics = createCounterData(samlCounter, oidcCounter);

        return new ServicesDataHolder(statistics, services, servicesParams.getDisplayedAttributes(),
                servicesParams.isShowSaml(), servicesParams.isShowOidc(), servicesParams.isShowTesting(),
                servicesParams.isShowStaging(), servicesParams.isShowProduction());
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

    private void processFacility(List<LosFacility> services, Facility facility, Map<String, PerunAttributeValue> attributes,
                                 ServicesParams servicesParams, ServicesCounter sc, String protocol)
    {
        PerunAttributeValue showOnServiceList = attributes.getOrDefault(servicesParams.getShowOnServiceListAttr(), null);
        if (showOnServiceList == null || !showOnServiceList.valueAsBoolean()) {
            services.add(new LosFacility(facility, protocol, attributes));
        }

        PerunAttributeValue environment = attributes.getOrDefault(servicesParams.getRpEnvironmentAttr(), null);
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

    private Set<String> initAttrNames(ServicesParams servicesParams) {
        Set<String> attrNames = new HashSet<>();
        if (servicesParams.isShowOidc()) {
            attrNames.add(servicesParams.getOidcClientIdAttr());
        }
        if (servicesParams.isShowSaml()) {
            attrNames.add(servicesParams.getSaml2EntityIdAttr());
        }

        attrNames.add(servicesParams.getRpEnvironmentAttr());
        attrNames.add(servicesParams.getShowOnServiceListAttr());

        final List<LosAttribute> other = servicesParams.getDisplayedAttributes();
        other.forEach(losAttr -> {
            attrNames.add(losAttr.getSourceAttrName());
            if (StringUtils.hasText(losAttr.getUrlSourceAttr())) {
                attrNames.add(losAttr.getUrlSourceAttr());
            }
        });

        return attrNames;
    }

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

        public int getTesting() {
            return testing;
        }

        public int getStaging() {
            return staging;
        }

        public int getProduction() {
            return production;
        }
    }

}
