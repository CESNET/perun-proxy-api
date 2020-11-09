package cz.muni.ics.perunproxyapi.persistence.models.listOfServices;

import java.util.Comparator;

/**
 * Sorts LosFacilities to an alphabetical order by names.
 *
 * @author Dominik Baranek <baranek@ics.muni.cz>
 */
public class NameSorter implements Comparator<LosFacility> {

    @Override
    public int compare(LosFacility lf1, LosFacility lf2) {
        return lf2.getName().compareToIgnoreCase(lf1.getName());
    }
}
