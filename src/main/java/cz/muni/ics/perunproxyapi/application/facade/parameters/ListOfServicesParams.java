package cz.muni.ics.perunproxyapi.application.facade.parameters;

import cz.muni.ics.perunproxyapi.persistence.adapters.FullAdapter;
import cz.muni.ics.perunproxyapi.persistence.exceptions.ConfigurationException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.StringUtils;

/**
 * Params holder for methods for LIST OF SERVICES.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 * @author Dominik Baranek <baranek@ics.muni.cz>
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ListOfServicesParams {

    @NonNull private FullAdapter adapter;
    private boolean showSaml;
    private boolean showOidc;
    private boolean showProduction;
    private boolean showStaging;
    private boolean showTesting;
    private String proxyIdentifierAttr;
    private String proxyIdentifierValue;
    private String showOnServiceListAttr;
    private String rpEnvironmentAttr;
    private String rpProtocolAttr;

    public ListOfServicesParams(@NonNull FullAdapter adapter, boolean showSaml, boolean showOidc,
                                boolean showProduction, boolean showStaging, boolean showTesting,
                                String proxyIdentifierAttr, String proxyIdentifierValue, String showOnServiceListAttr,
                                String rpEnvironmentAttr, String rpProtocolAttr)
    {
        this.setAdapter(adapter);
        this.setShowSaml(showSaml);
        this.setShowOidc(showOidc);
        this.setShowProduction(showProduction);
        this.setShowStaging(showStaging);
        this.setShowTesting(showTesting);
        this.setProxyIdentifierValue(proxyIdentifierValue);
        this.setProxyIdentifierAttr(proxyIdentifierAttr);
        this.setShowOnServiceListAttr(showOnServiceListAttr);
        this.setRpEnvironmentAttr(rpEnvironmentAttr);
        this.setRpProtocolAttr(rpProtocolAttr);
    }

    public void setProxyIdentifierValue(String proxyIdentifierValue) {
        if (!StringUtils.hasText(proxyIdentifierValue)) {
            throw new ConfigurationException("Proxy identifier cannot be null or empty");
        }
        this.proxyIdentifierValue = proxyIdentifierValue;
    }

    public void setProxyIdentifierAttr(String proxyIdentifier) {
        if (!StringUtils.hasText(proxyIdentifier)) {
            throw new ConfigurationException("ProxyIdentifierAttr cannot be null or empty");
        }
        this.proxyIdentifierAttr = proxyIdentifier;
    }

    public void setShowOnServiceListAttr(String showOnServiceListAttr) {
        if (!StringUtils.hasText(showOnServiceListAttr)) {
            throw new ConfigurationException("ShowOnServiceListAttr cannot be null or empty");
        }
        this.showOnServiceListAttr = showOnServiceListAttr;
    }

    public void setRpEnvironmentAttr(String rpEnvironmentAttr) {
        if (!StringUtils.hasText(rpEnvironmentAttr)) {
            throw new ConfigurationException("RpEnvironmentAttr cannot be null or empty");
        }
        this.rpEnvironmentAttr = rpEnvironmentAttr;
    }

    public void setRpProtocolAttr(String rpProtocolAttr) {
        if (!StringUtils.hasText(rpProtocolAttr)) {
            throw new ConfigurationException("RP Protocol ATTR cannot be null or empty");
        }
        this.rpProtocolAttr = rpProtocolAttr;
    }

}
