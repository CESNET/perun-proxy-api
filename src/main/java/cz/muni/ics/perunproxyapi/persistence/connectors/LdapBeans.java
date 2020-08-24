package cz.muni.ics.perunproxyapi.persistence.connectors;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.DefaultTlsDirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.pool2.factory.PoolConfig;
import org.springframework.ldap.pool2.factory.PooledContextSource;
import org.springframework.ldap.pool2.validation.DefaultDirContextValidator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Setter
public class LdapBeans {

    // Values from config
    @Value("${connector.ldap.base_dn}")
    @Getter
    private String baseDN;

    @Value("${connector.ldap.ldap_hosts}")
    private List<String> ldapHosts;

    @Value("${connector.ldap.ldap_user}")
    private String ldapUser;

    @Value("${connector.ldap.ldap_password}")
    private String ldapPassword;

    @Value("${connector.ldap.timeout_secs}")
    private int timeoutSecs;

    @Value("${connector.ldap.use_tls}")
    private boolean useTLS;

    @Value("${connector.ldap.allow_untrusted_ssl}")
    private boolean allowUntrustedSSL;

    @Bean
    public ContextSource targetContextSource() {
        LdapContextSource cs = new LdapContextSource();
        cs.setUrls(ldapHosts.toArray(new String[] {}));
        cs.setBase(baseDN);
        if (ldapUser != null) {
            cs.setUserDn(ldapUser);
        }

        if (ldapPassword != null) {
            cs.setPassword(ldapPassword);
        }

        if (useTLS) {
            cs.setAuthenticationStrategy(new DefaultTlsDirContextAuthenticationStrategy());
        }
        cs.afterPropertiesSet();
        return cs;
    }

    @Bean
    @Autowired
    public ContextSource contextSource(ContextSource targetContextSource) {
        PoolConfig poolConfig = new PoolConfig();
        poolConfig.setMaxTotal(100);

        PooledContextSource pcs = new PooledContextSource(poolConfig);
        pcs.setContextSource(targetContextSource);
        pcs.setDirContextValidator(new DefaultDirContextValidator());
        return pcs;
    }

    @Bean
    @Autowired
    public LdapTemplate ldapTemplate(ContextSource contextSource) {
        LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
        ldapTemplate.setDefaultTimeLimit(timeoutSecs);
        return ldapTemplate;
    }

}
