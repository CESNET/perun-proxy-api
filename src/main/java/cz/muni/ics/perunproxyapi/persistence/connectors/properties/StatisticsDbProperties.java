package cz.muni.ics.perunproxyapi.persistence.connectors.properties;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Component
@ConfigurationProperties(prefix = "database.statistics")
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Slf4j
public class StatisticsDbProperties {

    @NotBlank private String url;
    @NotNull private String username;
    @NotNull private String password;
    @NotBlank private String driverClassName;
    @Min(1) private int maximumPoolSize = 10;
    @NotBlank private String idpMapTable;
    @NotBlank private String rpMapTable;
    @NotBlank private String statisticsTable;
    @NotBlank private String sumsTable;

}
