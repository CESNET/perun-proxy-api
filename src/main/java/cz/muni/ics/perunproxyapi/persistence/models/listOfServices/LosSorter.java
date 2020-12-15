package cz.muni.ics.perunproxyapi.persistence.models.listOfServices;

import java.util.Comparator;

/**
 * Sorts LosFacilities to an alphabetical order by names.
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>
 */
public class LosSorter implements Comparator<LosFacility> {

    @Override
    public int compare(LosFacility lf1, LosFacility lf2) {
        if (lf1.getFacility() == null || lf1.getFacility().getName() == null) {
            if (lf2.getFacility() == null || lf2.getFacility().getName() == null) {
                return 0;
            }
            return -1;
        } else if (lf2.getFacility() == null || lf2.getFacility().getName() == null) {
            return 1;
        }
        return lf2.getFacility().getName().compareToIgnoreCase(lf1.getFacility().getName());
    }
}
