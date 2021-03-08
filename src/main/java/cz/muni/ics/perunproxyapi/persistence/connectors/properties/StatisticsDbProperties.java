package cz.muni.ics.perunproxyapi.persistence.connectors.properties;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "database.statistics")
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Slf4j
public class StatisticsDbProperties {

    @NonNull private String url;
    @NonNull private String username;
    @NonNull private String password;
    @NonNull private String driverClassName;
    private int maximumPoolSize = 10;
    @NonNull private String idpMapTable;
    @NonNull private String rpMapTable;
    @NonNull private String statisticsTable;
    @NonNull private String sumsTable;

}
