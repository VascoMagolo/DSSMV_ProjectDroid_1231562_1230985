package rttc.dssmv_projectdroid_1231562_1230985.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class UserTest {

    @Test
    public void testUserConstructor() {
        User user = new User("John Doe", "john@example.com", "password123", "user-123", "en");
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("user-123", user.getId());
        assertEquals("en", user.getPreferredLanguage());
    }

    @Test
    public void testUserSetters() {
        User user = new User("Old Name", "old@example.com", "oldpass", "oldid", "pt");
        user.setName("New Name");
        user.setEmail("new@example.com");
        user.setPassword("newpass");
        user.setId("newid");
        user.setPreferredLanguage("es");

        assertEquals("New Name", user.getName());
        assertEquals("new@example.com", user.getEmail());
        assertEquals("newpass", user.getPassword());
        assertEquals("newid", user.getId());
        assertEquals("es", user.getPreferredLanguage());
    }

    @Test
    public void testUserWithNullValues() {
        User user = new User(null, null, null, null, null);
        assertNull(user.getName());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getId());
        assertNull(user.getPreferredLanguage());
    }
}