package cz.muni.ics.perunproxyapi.application.facade.configuration;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Class with constants defining names of methods. These constants are used for fetching preferred adapter.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MethodNameConstants {

    public static final String FIND_BY_EXT_LOGINS = "find_by_ext_logins";
    public static final String GET_USER_BY_LOGIN = "get_user_by_login";

}
