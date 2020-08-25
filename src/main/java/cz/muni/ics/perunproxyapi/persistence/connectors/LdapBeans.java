package cz.muni.ics.perunproxyapi.persistence.connectors;

import cz.muni.ics.perunproxyapi.persistence.connectors.properties.LdapProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.DefaultTlsDirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.pool2.factory.PoolConfig;
import org.springframework.ldap.pool2.factory.PooledContextSource;
import org.springframework.ldap.pool2.validation.DefaultDirContextValidator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class LdapBeans {

    private final LdapProperties ldapProperties;

    @Autowired
    public LdapBeans(LdapProperties ldapProperties) {
        this.ldapProperties = ldapProperties;
    }

    @Bean(name = "targetContextSource")
    @Autowired
    public ContextSource targetContextSource(LdapProperties ldapProperties) {
        LdapContextSource cs = new LdapContextSource();
        cs.setUrls(ldapProperties.getLdapHosts());
        cs.setBase(ldapProperties.getBaseDn());
        if (StringUtils.hasText(ldapProperties.getLdapUser())) {
            cs.setUserDn(ldapProperties.getLdapUser());
        } else {
            cs.setAnonymousReadOnly(true);
        }

        if (StringUtils.hasText(ldapProperties.getLdapPassword())) {
            cs.setPassword(ldapProperties.getLdapPassword());
        }

        if (ldapProperties.isUseTLS()) {
            cs.setAuthenticationStrategy(new DefaultTlsDirContextAuthenticationStrategy());
        }
        cs.afterPropertiesSet();
        return cs;
    }

    @Bean(name = "contextSource")
    @Autowired
    public ContextSource contextSource(@Qualifier("targetContextSource") ContextSource targetContextSource) {
        PoolConfig poolConfig = new PoolConfig();
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setMaxTotal(ldapProperties.getConnectionPoolSize());

        PooledContextSource pcs = new PooledContextSource(poolConfig);
        pcs.setContextSource(targetContextSource);
        pcs.setDirContextValidator(new DefaultDirContextValidator());
        return pcs;
    }

    @Bean
    @Autowired
    public LdapTemplate ldapTemplate(@Qualifier("contextSource") ContextSource contextSource) {
        LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
        ldapTemplate.setDefaultTimeLimit(ldapProperties.getTimeout());
        return ldapTemplate;
    }

}
