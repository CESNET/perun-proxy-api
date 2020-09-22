package cz.muni.ics.perunproxyapi.persistence.connectors;

import cz.muni.ics.perunproxyapi.persistence.exceptions.LookupException;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ldap.UncategorizedLdapException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the LDAP connector.
 * There are actually two connectors configured.
 *   - connectorMock - this should be used when you need to specify the return value or thrown exception
 *   - connector - this should be used when fetching real data, data is defined in the resources/ldap-test-data.ldif file
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@SpringBootTest
public class PerunConnectorLdapTest {

    private static final String ATTR1 = "attr1";
    private static final String ATTR2 = "attr2";

    private static final LdapTemplate ldapTemplateMock = mock(LdapTemplate.class);
    private static final PerunConnectorLdap connectorMock = new PerunConnectorLdap(ldapTemplateMock);

    @Autowired
    private LdapTemplate ldapTemplate;

    @Autowired
    private PerunConnectorLdap connector;

    @Test
    public void testLookupExceptionThrown() {
        String dn = "testObjectId=1";
        String[] attributes = new String[] {ATTR1, ATTR2};

        ContextMapper<TestObject> mapper = ctx -> {
            DirContextAdapter context = (DirContextAdapter) ctx;
            String attr1 = context.getStringAttribute(ATTR1);
            String[] attr2 = context.getStringAttributes(ATTR2);
            return new TestObject(attr1, Arrays.asList(attr2));
        };
        when(ldapTemplateMock.lookup(anyString(), any(String[].class), any(ContextMapper.class)))
                .thenThrow(new UncategorizedLdapException("Test exception"));

        assertThrows(LookupException.class, () -> connectorMock.lookup(dn, attributes, mapper));
    }

    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TestObject {
        private String attr1;
        private List<String> attr2;
    }

}
