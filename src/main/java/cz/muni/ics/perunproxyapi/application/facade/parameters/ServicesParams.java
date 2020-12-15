package cz.muni.ics.perunproxyapi.application.facade.parameters;

import cz.muni.ics.perunproxyapi.application.facade.configuration.classes.LosAttribute;
import cz.muni.ics.perunproxyapi.persistence.adapters.FullAdapter;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Class representing 
 *
 * adapter - FullData adapter to be used
 * headerPath - String path to header file
 * footerPath - String path to footer file
 * proxyIdentifier - String identifier of the proxy
 * showOIDCServices - boolean indicates if OIDC services should be shown
 * perunProxyIdentifierAttr - String attribute identifier of proxy identifier attribute
 * serviceNameAttr - String attribute identifier of service name attribute
 * loginUrlAttr - String attribute identifier of login URL attribute
 * rpEnvironmentAttr - String attribute identifier of rpEnvironment attribute
 * showOnServiceListAttr - String attribute identifier of show on service list attribute
 * saml2EntityIdAttr - String attribute identifier of saml2EntityId attribute
 * oidcClientIdAttr - String attribute identifier of oidcClientId attribute
 * attributesDefinitions - List<String> of attributes which will be shown
 * multilingualAttributes - List<String> of attributes having structure as a map of translations
 * urlAttributes - List<String> of attributes which have a link as their value
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ServicesParams {

    @NonNull private FullAdapter adapter;
    private boolean showSaml;
    private boolean showOidc;
    private boolean showProduction;
    private boolean showStaging;
    private boolean showTesting;
    @NonNull private String proxyIdentifier;
    @NonNull private String perunProxyIdentifierAttr;
    private String showOnServiceListAttr;
    private String rpEnvironmentAttr;
    private String saml2EntityIdAttr;
    private String oidcClientIdAttr;
    @NonNull private List<LosAttribute> displayedAttributes;

}
