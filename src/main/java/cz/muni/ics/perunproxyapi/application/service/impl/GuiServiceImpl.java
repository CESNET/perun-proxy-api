package cz.muni.ics.perunproxyapi.application.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import cz.muni.ics.perunproxyapi.application.facade.parameters.ServicesParams;
import cz.muni.ics.perunproxyapi.application.service.GuiService;
import cz.muni.ics.perunproxyapi.persistence.enums.Entity;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.Facility;
import cz.muni.ics.perunproxyapi.persistence.models.PerunAttribute;
import cz.muni.ics.perunproxyapi.persistence.models.listOfServices.LosFacility;
import cz.muni.ics.perunproxyapi.persistence.models.listOfServices.NameSorter;
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
            Map<String, PerunAttribute> attributes = servicesParams.getAdapter()
                    .getAttributesWithUnrequiredValue(Entity.FACILITY, facility.getId(), new ArrayList<>(attrNames));
            PerunAttribute entityId = attributes.get(servicesParams.getSaml2EntityIdAttr());
            if (entityId != null && StringUtils.hasText(entityId.valueAsString())) {
                processFacility(samlServices, facility, attributes, servicesParams, samlCounter, SAML);
            }

            PerunAttribute clientId = attributes.get(servicesParams.getOidcClientIdAttr());
            if (clientId != null && StringUtils.hasText(clientId.valueAsString())) {
                processFacility(oidcServices, facility, attributes, servicesParams, oidcCounter, OIDC);
            }
        }

        Set<String> attributesToShow = new HashSet<>();

        List<LosFacility> services = new ArrayList<>();
        services.addAll(samlServices);
        services.addAll(oidcServices);

        services.sort(new NameSorter());

        Map<String, Integer> statistics = createCounterData(samlCounter, oidcCounter);

        ObjectMapper mapper = new ObjectMapper();

        List<Map<String, JsonNode>> servicesJson = new ArrayList<>();
        for (LosFacility service : services) {
            Map<String, JsonNode> parsedService = new HashMap<>();

            parsedService.put("name", mapper.convertValue(service.getName(), JsonNode.class));

            if (samlServices.contains(service)) {
                parsedService.put("authenticationProtocol", JsonNodeFactory.instance.textNode(SAML));
            } else {
                parsedService.put("authenticationProtocol", JsonNodeFactory.instance.textNode(OIDC));
            }

            for (String attribute : attributesToShow) {
                parsedService.put(service.getAttributes().get(attribute).getDisplayName(), service.getAttributes().get(attribute).getValue());
            }

            servicesJson.add(parsedService);
        }

        return new ServicesDataHolder(statistics, servicesJson, services, attributesToShow,
                servicesParams.getMultilingualAttributes(), servicesParams.getUrlAttributes(),
                servicesParams.getHeaderPath(), servicesParams.getFooterPath());
    }

    private Map<String, Integer> createCounterData(ServicesCounter samlCounter, ServicesCounter oidcCounter) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("samlProductionServicesCount", samlCounter.getProductionServicesCount());
        stats.put("samlTestingServicesCount", samlCounter.getTestingServicesCount());
        stats.put("samlStagingServicesCount", samlCounter.getStagingServicesCount());
        stats.put("oidcProductionServicesCount", oidcCounter.getProductionServicesCount());
        stats.put("oidcTestingServicesCount", oidcCounter.getTestingServicesCount());
        stats.put("oidcStagingServicesCount", oidcCounter.getStagingServicesCount());
        stats.put("allProductionServicesCount", samlCounter.getProductionServicesCount() + oidcCounter.getProductionServicesCount());
        stats.put("allTestingServicesCount", samlCounter.getTestingServicesCount() + oidcCounter.getTestingServicesCount());
        stats.put("allStagingServicesCount", samlCounter.getStagingServicesCount() + oidcCounter.getStagingServicesCount());
        return stats;
    }

    private void processFacility(List<LosFacility> services, Facility facility, Map<String, PerunAttribute> attributes,
                                 ServicesParams servicesParams, ServicesCounter sc, String protocol)
    {
        services.add(new LosFacility(
                facility,
                attributes.get(servicesParams.getServiceNameAttr()).getValue().asText(),
                attributes.get(servicesParams.getLoginUrlAttr()).getValue().asText(),
                attributes.get(servicesParams.getShowOnServiceListAttr()).getValue().asBoolean(),
                protocol,
                attributes));

        if (attributes.get(servicesParams.getRpEnvironmentAttr()) != null
                && attributes.get(servicesParams.getRpEnvironmentAttr()).getValue().asText().equals(TESTING)) {
            sc.incrementTestingServiceCount();
        }

        if (attributes.get(servicesParams.getRpEnvironmentAttr()) != null
                && attributes.get(servicesParams.getRpEnvironmentAttr()).getValue().asText().equals(STAGING)) {
            sc.incrementStagingServicesCount();
        }

        if (attributes.get(servicesParams.getRpEnvironmentAttr()) != null
                && attributes.get(servicesParams.getRpEnvironmentAttr()).getValue().asText().equals(PRODUCTION)) {
            sc.incrementProductionServicesCount();
        }
    }

    private Set<String> initAttrNames(ServicesParams servicesParams) {
        Set<String> attrNames = new HashSet<>();
        attrNames.add(servicesParams.getSaml2EntityIdAttr());
        attrNames.add(servicesParams.getServiceNameAttr());

        if (StringUtils.hasText(servicesParams.getOidcClientIdAttr())) {
            attrNames.add(servicesParams.getOidcClientIdAttr());
        }
        if (StringUtils.hasText(servicesParams.getLoginUrlAttr())) {
            attrNames.add(servicesParams.getLoginUrlAttr());
        }
        if (StringUtils.hasText(servicesParams.getRpEnvironmentAttr())) {
            attrNames.add(servicesParams.getRpEnvironmentAttr());
        }
        if (StringUtils.hasText(servicesParams.getShowOnServiceListAttr())) {
            attrNames.add(servicesParams.getShowOnServiceListAttr());
        }

        attrNames.addAll(servicesParams.getAttributesDefinitions());
        attrNames.addAll(servicesParams.getMultilingualAttributes());
        attrNames.addAll(servicesParams.getUrlAttributes());
        return attrNames;
    }

    private static class ServicesCounter {
        private int testingServicesCount = 0;
        private int stagingServicesCount = 0;
        private int productionServicesCount = 0;

        public void incrementTestingServiceCount() {
            testingServicesCount++;
        }

        public void incrementStagingServicesCount() {
            stagingServicesCount++;
        }

        public void incrementProductionServicesCount() {
            productionServicesCount++;
        }

        public int getTestingServicesCount() {
            return testingServicesCount;
        }

        public int getStagingServicesCount() {
            return stagingServicesCount;
        }

        public int getProductionServicesCount() {
            return productionServicesCount;
        }
    }

}
