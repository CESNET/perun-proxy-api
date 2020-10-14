package cz.muni.ics.perunproxyapi.ga4gh.model;

import cz.muni.ics.perunproxyapi.ga4gh.Ga4ghImplType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

/**
 * Configuration properties for GA4GH functionality. Contains implementation type and path to configuration file.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Slf4j
@ConfigurationProperties(prefix="ga4gh")
@Configuration
public class Ga4ghProperties {

    @NotNull private Ga4ghImplType type = Ga4ghImplType.NONE;
    private String configFilePath;

    @PostConstruct
    public void init() {
        log.info("Initialized GA4GH properties");
        log.debug("{}", this);
    }

}
