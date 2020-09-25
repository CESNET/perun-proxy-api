package cz.muni.ics.perunproxyapi.persistence.models;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * Model representing affiliation of user.
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 * @author Dominik Baranek <baranek@ics.muni.cz>
 */
@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Affiliation {

    @NonNull
    private final String source;

    private final String value;
    private final long asserted;

}
