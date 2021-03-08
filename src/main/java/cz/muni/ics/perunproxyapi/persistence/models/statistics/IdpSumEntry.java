package cz.muni.ics.perunproxyapi.persistence.models.statistics;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class IdpSumEntry {

    private String idpName;
    private String idpIdentifier;
    private int logins;

    /**
     * Compare objects by date in ascending manner.
     * @param o1 First object
     * @param o2 Second object
     * @return -1 if o1 < o2, 0 if O1 == o2, 1 if o1 > o2
     */
    public static int compareByLogins(IdpSumEntry o1, IdpSumEntry o2) {
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 == null) {
            return -1;
        } else if (o2 == null) {
            return 1;
        } else {
            return Integer.compare(o1.logins, o2.logins);
        }
    }

}
