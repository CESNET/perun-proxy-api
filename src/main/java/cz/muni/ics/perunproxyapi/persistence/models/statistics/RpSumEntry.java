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
public class RpSumEntry {

    private String rpName;
    private String rpIdentifier;
    private int logins;

    public static int compareByLogins(RpSumEntry o1, RpSumEntry o2) {
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
