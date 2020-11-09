package cz.muni.ics.perunproxyapi.application.service;

import cz.muni.ics.perunproxyapi.persistence.adapters.FullAdapter;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunConnectionException;
import cz.muni.ics.perunproxyapi.persistence.exceptions.PerunUnknownException;
import cz.muni.ics.perunproxyapi.persistence.models.listOfServices.ServicesDataHolder;
import lombok.NonNull;

import java.util.List;

/**
 * Service layer for GUI related things.
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>
 */
public interface GuiService {

    /**
     * Gets all data needed for the list of services.
     *
     * @param adapter FullData adapter to be used
     * @param headerPath String path to header file
     * @param footerPath String path to footer file
     * @param proxyIdentifier String identifier of the proxy
     * @param showOIDCServices boolean indicates if OIDC services should be shown
     * @param perunProxyIdentifierAttr String attribute identifier of proxy identifier attribute
     * @param serviceNameAttr String attribute identifier of service name attribute
     * @param loginUrlAttr String attribute identifier of login URL attribute
     * @param rpEnvironmentAttr String attribute identifier of rpEnvironment attribute
     * @param showOnServiceListAttr String attribute identifier of show on service list attribute
     * @param saml2EntityIdAttr String attribute identifier of saml2EntityId attribute
     * @param oidcClientIdAttr String attribute identifier of oidcClientId attribute
     * @param attributesDefinitions List<String> of attributes which will be shown
     * @param multilingualAttributes List<String> of attributes having structure as a map of translations
     * @param urlAttributes List<String> of attributes which have a link as their value
     * @return Gets all data needed for the list of services.
     * @throws PerunUnknownException Thrown as wrapper of unknown exception thrown by Perun interface.
     * @throws PerunConnectionException Thrown when problem with connection to Perun interface occurs.
     */
    ServicesDataHolder getListOfSps(@NonNull FullAdapter adapter,
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
                                    @NonNull List<String> urlAttributes) throws PerunUnknownException, PerunConnectionException;

}
