package cz.muni.ics.perunproxyapi.application.facade.parameters;

import cz.muni.ics.perunproxyapi.application.facade.configuration.classes.ListOfServicesDisplayedAttribute;
import cz.muni.ics.perunproxyapi.persistence.adapters.FullAdapter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Params holder for methods for LIST OF SERVICES - GUI.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class ListOfServicesGuiParams extends ListOfServicesParams {

    @NonNull private final List<ListOfServicesDisplayedAttribute> displayedAttributes = new ArrayList<>();

    public ListOfServicesGuiParams(@NonNull FullAdapter adapter,
                                   boolean showSaml,
                                   boolean showOidc,
                                   boolean showProduction,
                                   boolean showStaging,
                                   boolean showTesting,
                                   @NonNull String proxyIdentifierAttr,
                                   @NonNull String proxyIdentifierValue,
                                   @NonNull String showOnServiceListAttr,
                                   @NonNull String rpEnvironmentAttr,
                                   @NonNull String rpProtocolAttr,
                                   @NonNull List<ListOfServicesDisplayedAttribute> displayedAttributes)
    {
        super(adapter, showSaml, showOidc, showProduction, showStaging, showTesting, proxyIdentifierAttr,
                proxyIdentifierValue, showOnServiceListAttr, rpEnvironmentAttr, rpProtocolAttr);
        this.displayedAttributes.addAll(displayedAttributes);
    }

    public ListOfServicesGuiParams(@NonNull ListOfServicesParams params,
                                   @NonNull List<ListOfServicesDisplayedAttribute> displayedAttributes)
    {
        super(params.getAdapter(), params.isShowSaml(), params.isShowOidc(), params.isShowProduction(),
                params.isShowStaging(), params.isShowTesting(), params.getProxyIdentifierAttr(),
                params.getProxyIdentifierValue(), params.getShowOnServiceListAttr(), params.getRpEnvironmentAttr(),
                params.getRpProtocolAttr());
        this.displayedAttributes.addAll(displayedAttributes);
    }

}
