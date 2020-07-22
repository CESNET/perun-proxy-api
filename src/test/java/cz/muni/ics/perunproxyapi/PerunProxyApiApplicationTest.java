package cz.muni.ics.perunproxyapi;

import cz.muni.ics.perunproxyapi.persistence.AttributeMappingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
public class PerunProxyApiApplicationTest {

    @Autowired
    AttributeMappingService ams;

    @Test
    public void testMappingConfig() {
        assertNotNull(ams);
        assertEquals(2, ams.getAttributeMap().keySet().size(), "Should load 2 AttributeObjectMapping objects");
    }
}
