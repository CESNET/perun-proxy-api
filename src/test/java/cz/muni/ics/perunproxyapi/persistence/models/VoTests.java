package cz.muni.ics.perunproxyapi.persistence.models;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class VoTests {

    @Test
    public void testCreateVoNullId() {
        assertThrows(NullPointerException.class, () -> new Vo(null, "name", "sn"));
    }

    @Test
    public void testCreateVoNullName() {
        assertThrows(NullPointerException.class, () -> new Vo(1L, null, "sn"));
    }

    @Test
    public void testCreateVoNullShortName() {
        assertThrows(NullPointerException.class, () -> new Vo(1L, "name", null));
    }

    @Test
    public void testCreateVoEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> new Vo(1L, "", "sn"));
        assertThrows(IllegalArgumentException.class, () -> new Vo(1L, " ", "sn"));
    }

    @Test
    public void testCreateVoEmptyShortName() {
        assertThrows(IllegalArgumentException.class, () -> new Vo(1L, "name", ""));
        assertThrows(IllegalArgumentException.class, () -> new Vo(1L, "name", " "));
    }

}
