package cz.muni.ics.perunproxyapi.ga4gh.model;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A class representing a passport.
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 * @author Dominik Baranek <baranek@ics.muni.cz>
 */
@Getter
@Setter
@ToString(exclude = { "jwt", "signer", "prettyPayload" })
@EqualsAndHashCode
public class PassportVisa {

    private final String jwt;
    private boolean verified = false;
    private String linkedIdentity;
    private String sub;
    private String iss;
    private String type;
    private String value;
    @Getter(AccessLevel.PRIVATE) private String signer;
    @Getter(AccessLevel.PRIVATE) private String prettyPayload;

    public PassportVisa(String jwt) {
        this.jwt = jwt;
    }

}
