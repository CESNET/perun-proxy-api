package cz.muni.ics.perunproxyapi.application.service.models;

import cz.muni.ics.perunproxyapi.persistence.models.statistics.IdpSumEntry;
import cz.muni.ics.perunproxyapi.persistence.models.statistics.RpSumEntry;
import cz.muni.ics.perunproxyapi.persistence.models.statistics.LoginsPerDaySumEntry;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class StatsRawData {

    @NonNull private List<LoginsPerDaySumEntry> statsPerDay;
    @NonNull private List<IdpSumEntry> statsPerIdp;
    @NonNull private List<RpSumEntry> statsPerRp;

    public int countTotalPerIdps() {
        return statsPerIdp.stream().mapToInt(IdpSumEntry::getLogins).sum();
    }

    public int countTotalPerRps() {
        return statsPerRp.stream().mapToInt(RpSumEntry::getLogins).sum();
    }

}
