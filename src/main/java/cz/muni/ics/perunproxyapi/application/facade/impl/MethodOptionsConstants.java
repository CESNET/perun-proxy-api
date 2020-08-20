package cz.muni.ics.perunproxyapi.application.facade.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constants used in options of methods.
 * Also contains constants representing fallback values.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MethodOptionsConstants {

    // options
    public static final String ADAPTER = "adapter";

    // fallback values
    public static final String RPC = "RPC";

}
