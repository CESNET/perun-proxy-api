package cz.muni.ics.perunproxyapi;

import cz.muni.ics.perunproxyapi.persistence.AttributeMappingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
public class PerunProxyApiApplicationTest {

    private final AttributeMappingService ams;

    @Autowired
    public PerunProxyApiApplicationTest(AttributeMappingService ams) {
        this.ams = ams;
    }

    @Test
    public void testMappingConfig() {
        int loadedAttrs = ams.getAttributeMap().size();
        assertEquals(3, loadedAttrs, "Should load 3 AttributeObjectMapping objects");
    }

}
