package cz.muni.ics.perunproxyapi.persistence.connectors.properties;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Arrays;

/**
 * Configuration properties for LDAP Connector.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Component
@ConfigurationProperties(prefix = "connector.ldap")
@Getter
@Setter
@EqualsAndHashCode
@Slf4j
public class LdapProperties {

    @NotBlank private String baseDn;
    @NotBlank private String[] ldapHosts;
    @NotNull private String ldapUser = "";
    @NotNull private String ldapPassword = "";
    private int timeout = 30000;
    private boolean useTLS = false;
    private int connectionPoolSize = 20;

    @PostConstruct
    public void afterInit() {
        log.info("Initialized LdapProperties");
        log.debug("{}", this.toString());
    }

    @Override
    public String toString() {
        return "LdapProperties{" +
                "baseDn='" + baseDn + '\'' +
                ", ldapHosts=" + Arrays.toString(ldapHosts) +
                ", ldapUser='" + ldapUser + '\'' +
                ", ldapPassword='***************'" +
                ", timeout=" + timeout +
                ", useTLS=" + useTLS +
                ", connectionPoolSize=" + connectionPoolSize +
                '}';
    }

}
