package cz.muni.ics.perunproxyapi.persistence.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A class representing a passport.
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 * @author Dominik Baranek <baranek@ics.muni.cz>
 */
@ToString
public class PassportVisa {
    @Getter
    @ToString.Exclude
    String jwt;

    @Getter
    @Setter
    boolean verified = false;

    @Getter
    @Setter
    String linkedIdentity;

    @Setter
    @ToString.Exclude
    String signer;

    @Setter
    @ToString.Exclude
    String prettyPayload;

    @Getter
    @Setter
    private String sub;

    @Getter
    @Setter
    private String iss;

    @Getter
    @Setter
    private String type;

    @Getter
    @Setter
    private String value;


    public PassportVisa(String jwt) {
        this.jwt = jwt;
    }

    public String getPrettyString() {
        return prettyPayload + ", signed by " + signer;
    }

}
