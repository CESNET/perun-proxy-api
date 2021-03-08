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
public class LoginsPerDaySumEntry {

    private Long id;
    private int year;
    private int month;
    private int day;
    private int logins;
    private int users;

    /**
     * Compare objects by date in ascending manner.
     * @param o1 First object
     * @param o2 Second object
     * @return -1 if o1 < o2, 0 if O1 == o2, 1 if o1 > o2
     */
    public static int compareByDate(LoginsPerDaySumEntry o1, LoginsPerDaySumEntry o2) {
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 == null) {
            return -1;
        } else if (o2 == null) {
            return 1;
        } else if (o1.year - o2.year != 0) {
            return Integer.compare(o1.year, o2.year);
        } else if (o1.month - o2.month != 0) {
            return Integer.compare(o1.month, o2.month);
        } else {
            return Integer.compare(o1.day, o2.day);
        }
    }

}
