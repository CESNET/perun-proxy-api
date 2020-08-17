package cz.muni.ics.perunproxyapi.presentation.rest.controllers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Class with constants used by controllers.
 * Includes constants for parameters and similar.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParameterConstants {

    public static final String PARAM_IDP_IDENTIFIER = "IdPIdentifier";
    public static final String PARAM_IDENTIFIERS = "identifiers";
    public static final String PARAM_LOGIN = "login";
    public static final String PARAM_FIELDS = "fields";

}
