package cz.muni.ics.perunproxyapi.presentation.DTOModels.statistics;

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
public class DailyGraphEntry {

    private String label;
    private int users;
    private int logins;

}
