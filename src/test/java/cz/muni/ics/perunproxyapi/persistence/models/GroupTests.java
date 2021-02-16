package cz.muni.ics.perunproxyapi.persistence.models;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class GroupTests {

    @Test
    public void testCreateGroupNullId() {
        assertThrows(NullPointerException.class, () -> new Group(null, 5L, "name", "desc", "uniqueName", "uuid", 2L));
    }

    @Test
    public void testCreateGroupNullName() {
        assertThrows(NullPointerException.class, () -> new Group(1L, 5L, null, "desc", "uniqueName", "uuid", 2L));
    }

    @Test
    public void testCreateGroupNullDescription() {
        assertThrows(NullPointerException.class, () -> new Group(1L, 5L, "name", null, "uniqueName", "uuid", 2L));
    }

    @Test
    public void testCreateGroupEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> new Group(1L, 5L, "", "desc", "uniqueName", "uuid", 2L));
        assertThrows(IllegalArgumentException.class, () -> new Group(1L, 5L, " ", "desc", "uniqueName", "uuid", 2L));
    }

    @Test
    public void testCreateGroupEmptyDescription() {
        assertDoesNotThrow(() -> new Group(1L, 5L, "name", "", "uniqueName", "uuid", 2L));
        assertDoesNotThrow(() -> new Group(1L, 5L, "name", " ", "uniqueName", "uuid", 2L));
    }

    @Test
    public void testCreateGroupEmptyUniqueName() {
        assertDoesNotThrow(() -> new Group(1L, 5L, "name", "desc", "", "uuid", 2L));
        assertDoesNotThrow(() -> new Group(1L, 5L, "name", "desc", " ", "uuid", 2L));
    }

    @Test
    public void testCreateGroupNullUniqueName() {
        assertDoesNotThrow(() -> new Group(1L, 5L, "name", "desc", null, "uuid", 2L));
    }

    @Test
    public void testCreateGroupNullVoId() {
        assertThrows(NullPointerException.class, () -> new Group(1L, 5L, null, "desc", "uniqueName", "uuid", null));
    }

}
