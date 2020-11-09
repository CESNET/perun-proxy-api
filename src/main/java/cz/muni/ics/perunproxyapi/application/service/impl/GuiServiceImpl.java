package cz.muni.ics.perunproxyapi.application.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import cz.muni.ics.perunproxyapi.application.service.GuiService;
import cz.muni.ics.perunproxyapi.persistence.adapters.FullAdapter;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class GuiServiceImpl implements GuiService {

    private static final String SAML = "SAML";
    private static final String OIDC = "OIDC";
    private static final String TESTING = "TESTING";
    private static final String STAGING = "STAGING";



    @Override
    public ServicesDataHolder getListOfSps(@NonNull FullAdapter adapter,
                                           String headerPath,
                                           String footerPath,
                                           @NonNull String proxyIdentifier,
                                           boolean showOIDCServices,
                                           @NonNull String perunProxyIdentifierAttr,
                                           @NonNull String serviceNameAttr,
                                           String loginUrlAttr,
                                           String rpEnvironmentAttr,
                                           String showOnServiceListAttr,
                                           @NonNull String saml2EntityIdAttr,
                                           String oidcClientIdAttr,
                                           @NonNull List<String> attributesDefinitions,
                                           @NonNull List<String> multilingualAttributes,
                                           @NonNull List<String> urlAttributes) throws PerunUnknownException, PerunConnectionException {

        attributesDefinitions.addAll(multilingualAttributes);
        attributesDefinitions.addAll(urlAttributes);
        List<String> mergedAttrsLists = attributesDefinitions.stream().distinct().collect(Collectors.toList());

        List<Facility> facilities = adapter.searchFacilitiesByAttributeValue(perunProxyIdentifierAttr, proxyIdentifier);
        List<String> attrNames = new ArrayList<>();

        attrNames.add(saml2EntityIdAttr);
        attrNames.add(serviceNameAttr);

        if (oidcClientIdAttr != null && !oidcClientIdAttr.isEmpty()) {
            attrNames.add(oidcClientIdAttr);
        }

        if (loginUrlAttr != null && !loginUrlAttr.isEmpty()) {
            attrNames.add(loginUrlAttr);
        }

        if (rpEnvironmentAttr != null && !rpEnvironmentAttr.isEmpty()) {
            attrNames.add(rpEnvironmentAttr);
        }

        if (showOnServiceListAttr != null && !showOnServiceListAttr.isEmpty()) {
            attrNames.add(showOnServiceListAttr);
        }

        attrNames.addAll(mergedAttrsLists);

        List<LosFacility> samlServices = new ArrayList<>();
        List<LosFacility> oidcServices = new ArrayList<>();
        int samlTestingServicesCount = 0;
        int samlStagingServicesCount = 0;
        int oidcTestingServicesCount = 0;
        int oidcStagingServicesCount = 0;

        for (Facility facility : facilities) {
            Map<String, PerunAttribute> attributes = adapter.getAttributesWithUnrequiredValue(Entity.FACILITY, facility.getId(), attrNames);

            if (attributes.get(saml2EntityIdAttr) != null &&
                    !attributes.get(saml2EntityIdAttr).getValue().equals(JsonNodeFactory.instance.nullNode()) &&
                    !attributes.get(saml2EntityIdAttr).getValue().asText().isEmpty()) {

                samlServices.add(new LosFacility(
                        facility,
                        attributes.get(serviceNameAttr).getValue().asText(),
                        attributes.get(loginUrlAttr).getValue().asText(),
                        attributes.get(showOnServiceListAttr).getValue().asBoolean(),
                        SAML,
                        attributes));

                if (attributes.get(rpEnvironmentAttr) != null && attributes.get(rpEnvironmentAttr).getValue().asText().equals(TESTING)) {
                    samlTestingServicesCount++;
                }

                if (attributes.get(rpEnvironmentAttr) != null && attributes.get(rpEnvironmentAttr).getValue().asText().equals(STAGING)) {
                    samlStagingServicesCount++;
                }
            }

            if (attributes.get(oidcClientIdAttr) != null &&
                    !attributes.get(oidcClientIdAttr).getValue().equals(JsonNodeFactory.instance.nullNode()) &&
                    !attributes.get(oidcClientIdAttr).getValue().asText().isEmpty()) {
                oidcServices.add(new LosFacility(
                        facility,
                        attributes.get(serviceNameAttr).getValue().asText(),
                        attributes.get(loginUrlAttr).getValue().asText(),
                        attributes.get(showOnServiceListAttr).getValue().asBoolean(),
                        OIDC,
                        attributes));

                if (attributes.get(rpEnvironmentAttr) != null && attributes.get(rpEnvironmentAttr).getValue().asText().equals(TESTING)) {
                    oidcTestingServicesCount++;
                }

                if (attributes.get(rpEnvironmentAttr) != null && attributes.get(rpEnvironmentAttr).getValue().asText().equals(STAGING)) {
                    oidcStagingServicesCount++;
                }
            }
        }

        Set<String> attributesToShow = new HashSet<>();

        for (String attrName : attrNames) {
            if (!attrName.equals(loginUrlAttr) &&
                    !attrName.equals(serviceNameAttr) &&
                    !attrName.equals(showOnServiceListAttr) &&
                    !attrName.equals(rpEnvironmentAttr) &&
                    !attrName.equals(oidcClientIdAttr) &&
                    !attrName.equals(saml2EntityIdAttr)) {

                attributesToShow.add(attrName);
            }
        }

        List<LosFacility> services = new ArrayList<>();
        services.addAll(samlServices);
        services.addAll(oidcServices);

        services.sort(new NameSorter());

        Map<String, Integer> statistics = new HashMap<>();
        statistics.put("samlProductionServicesCount", samlServices.size() - samlTestingServicesCount - samlStagingServicesCount);
        statistics.put("samlTestingServicesCount", samlTestingServicesCount);
        statistics.put("samlStagingServicesCount", samlStagingServicesCount);
        statistics.put("oidcProductionServicesCount", oidcServices.size() - oidcTestingServicesCount - oidcStagingServicesCount);
        statistics.put("oidcTestingServicesCount", oidcTestingServicesCount);
        statistics.put("oidcStagingServicesCount", oidcStagingServicesCount);
        statistics.put("allProductionServicesCount", samlServices.size() - samlTestingServicesCount - samlStagingServicesCount + oidcServices.size() - oidcTestingServicesCount - oidcStagingServicesCount);
        statistics.put("allTestingServicesCount", samlTestingServicesCount + oidcTestingServicesCount);
        statistics.put("allStagingServicesCount", samlStagingServicesCount + oidcStagingServicesCount);

        ObjectMapper mapper = new ObjectMapper();

        List<Map<String, JsonNode>> servicesJson = new ArrayList<>();
        for (LosFacility service : services) {
            Map<String, JsonNode> parsedService = new HashMap<>();

            parsedService.put("name", mapper.convertValue(service.getName(), JsonNode.class));

            if (samlServices.contains(service)) {
                parsedService.put("authenticationProtocol", mapper.convertValue(SAML, JsonNode.class));
            } else {
                parsedService.put("authenticationProtocol", mapper.convertValue(OIDC, JsonNode.class));
            }

            for (String attribute : attributesToShow) {
                parsedService.put(service.getAttributes().get(attribute).getDisplayName(), service.getAttributes().get(attribute).getValue());
            }

            servicesJson.add(parsedService);
        }

        return new ServicesDataHolder(statistics, servicesJson, services, attributesToShow, multilingualAttributes, urlAttributes, headerPath, footerPath);
    }

}
