package cz.muni.ics.perunproxyapi.presentation.rest.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constants for PATHs mapping.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PathConstants {

    // auth, non-auth, ...
    public static final String NO_AUTH_PATH = "/non";
    public static final String AUTH_PATH = "/auth";

    // controller paths
    public static final String PROXY_USER = "/proxy-user";
    public static final String RELYING_PARTY = "/relying-party";

}
