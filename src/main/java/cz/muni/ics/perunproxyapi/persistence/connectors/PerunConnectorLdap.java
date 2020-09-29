package cz.muni.ics.perunproxyapi.persistence.connectors;

import cz.muni.ics.perunproxyapi.persistence.exceptions.LookupException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.lang.System.currentTimeMillis;


/**
 * Connector for calling Perun LDAP.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 * @author Pavol Pluta <pavol.pluta1@gmail.com>
 */
@Component
@Slf4j
public class PerunConnectorLdap {

    private final LdapTemplate ldapTemplate;

    @Autowired
    public PerunConnectorLdap(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    /**
     * Search for the first entry that satisfies criteria.
     * @param query Query object
     * @param mapper Mapper for the result
     * @param <T> Class that the result should be mapped to.
     * @return Found entry mapped to target class
     */
    public <T> T searchForObject(@NonNull LdapQuery query, @NonNull ContextMapper<T> mapper) {
        log.trace("searchForObject(\nbase: {},\nscope: {},\nfilters: {},\n attributes: {}\n)",
                query.base(), query.searchScope(), query.filter(), query.attributes());
        long startTime = currentTimeMillis();
        T result = null;
        try {
            result = ldapTemplate.searchForObject(query, mapper);
        } catch (IncorrectResultSizeDataAccessException e) {
            //this is ok, we want the result to be null instead of throwing the exception
        }
        long endTime = currentTimeMillis();
        long responseTime = endTime - startTime;
        log.trace("searchForObject query proceeded in {} ms.", responseTime);
        log.trace("searchForObject(\nbase: {},\nscope: {},\nfilters: {},\n attributes: {}\n)\n returns: {}",
                query.base(), query.searchScope(), query.filter(), query.attributes(), result);

        return result;
    }

    /**
     * Search for the entries satisfy criteria.
     * @param query Query object.
     * @param mapper Mapper for the result
     * @param <T> Class that the result should be mapped to.
     * @return List of found entries mapped to target class
     */
    public <T> List<T> search(@NonNull LdapQuery query, @NonNull ContextMapper<T> mapper) {
        log.trace("searchForObject(\nbase: {},\nscope: {},\nfilters: {},\n attributes: {},\n)",
                query.base(), query.searchScope(), query.filter(), query.attributes());
        long startTime = currentTimeMillis();
        List<T> result = ldapTemplate.search(query, mapper);
        long endTime = currentTimeMillis();
        long responseTime = endTime - startTime;
        log.trace("search query proceeded in {} ms.", responseTime);
        log.trace("searchForObject(\nbase: {},\nscope: {},\nfilters: {},\n attributes: {}\n)\n returns: {}",
                query.base(), query.searchScope(), query.filter(), query.attributes(), result);
        return result;
    }

    /**
     * Perform lookup for the entry that satisfies criteria.
     * @param <T> Class that the result should be mapped to.
     * @param dn Prefix to be added to the base DN. (i.e. ou=People).
     * @param attributes Attributes to be fetch for entry.
     * @param mapper Mapper for the result.
     * @return Found entry mapped to target class
     * @throws LookupException When entry cannot be found
     */
    public <T> T lookup(@NonNull String dn, @NonNull String[] attributes, @NonNull ContextMapper<T> mapper)
            throws LookupException
    {
        log.trace("lookup(\ndn: {},\n attributes: {}\n)", dn, attributes);
        long startTime = currentTimeMillis();
        T result;
        try {
            result = ldapTemplate.lookup(dn, attributes, mapper);
            long endTime = currentTimeMillis();
            long responseTime = endTime - startTime;
            log.trace("lookup query proceeded in {} ms.", responseTime);
            log.trace("lookup(\ndn: {},\n attributes: {}\n)\n returns: {}", dn, attributes, result);
            return result;
        } catch (NamingException e) {
            throw new LookupException();
        }
    }

}
