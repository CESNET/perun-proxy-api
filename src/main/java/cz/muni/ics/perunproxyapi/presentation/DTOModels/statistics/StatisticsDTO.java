package cz.muni.ics.perunproxyapi.presentation.DTOModels.statistics;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDTO {

    private String label;
    private List<DailyGraphEntry> loginsData;
    private int loginsIdpTotal;
    private List<PieChartEntry> idpData;
    private int loginsRpTotal;
    private List<PieChartEntry> rpData;

}
