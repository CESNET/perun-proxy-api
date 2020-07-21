package cz.muni.ics.perunproxyapi.adapters;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import cz.muni.ics.perunproxyapi.adapters.interfaces.DataAdapter;
import cz.muni.ics.perunproxyapi.connectors.PerunConnectorLdap;
import cz.muni.ics.perunproxyapi.enums.Entity;
import cz.muni.ics.perunproxyapi.models.*;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.ldap.client.template.EntryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




import org.apache.directory.ldap.client.api.search.FilterBuilder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.apache.directory.ldap.client.api.search.FilterBuilder.and;
import static org.apache.directory.ldap.client.api.search.FilterBuilder.equal;
import static org.apache.directory.ldap.client.api.search.FilterBuilder.or;
import static cz.muni.ics.perunproxyapi.adapters.PerunAdapterLdapConstants.*;

@Component
public class LdapAdapterImpl implements DataAdapter {


    private final static Logger log = LoggerFactory.getLogger(LdapAdapterImpl.class);
    private PerunConnectorLdap connectorLdap;

    private JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;

    @Override
    public User getPerunUser(Long userId, List<String> uids) {

        log.trace("getPerunUser({}, {})", userId, uids);

        String dnPrefix = "ou=People";
        FilterBuilder filter = and(equal(OBJECT_CLASS, PERUN_USER), equal(EDU_PERSON_PRINCIPAL_NAMES, perunPrincipal.getExtLogin()));
        SearchScope scope = SearchScope.ONELEVEL;
        String[] attributes = new String[]{PERUN_USER_ID, GIVEN_NAME, SN};
        EntryMapper<PerunUser> mapper = e -> {
            if (!checkHasAttributes(e, new String[] { PERUN_USER_ID, SN })) {
                return null;
            }

            long id = Long.parseLong(e.get(PERUN_USER_ID).getString());
            String firstName = (e.get(GIVEN_NAME) != null) ? e.get(GIVEN_NAME).getString() : null;
            String lastName = e.get(SN).getString();
            return new PerunUser(id, firstName, lastName);
        };

        PerunUser foundUser = connectorLdap.searchFirst(dnPrefix, filter, scope, attributes, mapper);

        log.trace("getPreauthenticatedUserId({} returns: {})", perunPrincipal, foundUser);
        return foundUser;

    }

    @Override
    public List<Group> getMemberGroups(Long userId, Long voId) {
        return null;
    }

    @Override
    public List<Group> getSpGroups(Long spEntityId) {
        return null;
    }

    @Override
    public Group getGroupByName(Long voId, String name) {
        return null;
    }

    @Override
    public Vo getVoByShortName(String voShortName) {
        log.trace("getVoByShortName({})", voShortName);

        FilterBuilder filter = and(equal(OBJECT_CLASS, PERUN_VO), equal(O, voShortName));
        String[] attributes = new String[] { PERUN_VO_ID, O, DESCRIPTION };

        EntryMapper<Vo> mapper = e -> {
            if (!checkHasAttributes(e, attributes)) {
                return null;
            }

            Long id = Long.valueOf(e.get(PERUN_VO_ID).getString());
            String shortNameVo = e.get(O).getString();
            String name = e.get(DESCRIPTION).getString();

            return new Vo(id, name, shortNameVo);
        };
        Vo vo = connectorLdap.searchFirst(null, filter, SearchScope.ONELEVEL, attributes, mapper);
        log.trace("getVoByShortName({}) returns: {}", voShortName, vo);
        return vo;
    }

    @Override
    public Vo getVoById(Long voId) {
        log.trace("getVoById({})", voId);

        FilterBuilder filter = and(equal(OBJECT_CLASS, PERUN_VO), equal(PERUN_VO_ID, voId.toString()));
        String[] attributes = new String[] { PERUN_VO_ID, O, DESCRIPTION };

        EntryMapper<Vo> mapper = e -> {
            if (!checkHasAttributes(e, attributes)) {
                return null;
            }

            Long id = Long.valueOf(e.get(PERUN_VO_ID).getString());
            String shortNameVo = e.get(O).getString();
            String name = e.get(DESCRIPTION).getString();

            return new Vo(id, name, shortNameVo);
        };
        Vo vo = connectorLdap.searchFirst(null, filter, SearchScope.ONELEVEL, attributes, mapper);
        log.trace("getVoById({}) returns: {}", voId, vo);
        return vo;
    }

    @Override
    public Map<String, PerunAttribute> getAttributesValues(Entity entity, Long entityId, List<String> attributes) {
        return null;
    }

    @Override
    public List<Facility> getFacilitiesByAttribute(String name, String attrValue) {
        return null;
    }

    @Override
    public List<Group> getUsersGroupsOnFacility(Long spEntityId, Long userId) {
        return null;
    }

    @Override
    public List<Facility> searchFacilitiesByAttributeValue(PerunAttribute attribute) {
        return null;
    }

    private boolean checkHasAttributes(Entry e, String[] attributes) {
        if (e == null) {
            return false;
        } else if (attributes == null) {
            return true;
        }

        for (String attr: attributes) {
            if (e.get(attr) == null) {
                return false;
            }
        }

        return true;
    }
}
