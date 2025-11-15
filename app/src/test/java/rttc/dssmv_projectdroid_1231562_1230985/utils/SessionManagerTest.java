package rttc.dssmv_projectdroid_1231562_1230985.utils;

import android.content.Context;
import android.content.SharedPreferences;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import rttc.dssmv_projectdroid_1231562_1230985.model.User;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SessionManagerTest {

    @Mock
    private Context mockContext;

    @Mock
    private SharedPreferences mockSharedPreferences;

    @Mock
    private SharedPreferences.Editor mockEditor;

    private SessionManager sessionManager;
    private User testUser;

    @Before
    public void setUp() {
        when(mockContext.getSharedPreferences(eq("auth"), anyInt()))
                .thenReturn(mockSharedPreferences);
        when(mockSharedPreferences.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor);
        when(mockEditor.clear()).thenReturn(mockEditor);

        sessionManager = new SessionManager(mockContext);
        testUser = new User("John Doe", "john@example.com", "password", "user123", "en");
    }

    @Test
    public void testSaveUser() {
        sessionManager.saveUser(testUser);
        verify(mockEditor).putString(eq("user_json"), anyString());
        verify(mockEditor).apply();
    }

    @Test
    public void testGetUserWithValidJson() {
        String userJson = "{\"id\":\"user123\",\"name\":\"John Doe\",\"email\":\"john@example.com\",\"preferred_language\":\"en\"}";
        when(mockSharedPreferences.getString("user_json", null)).thenReturn(userJson);

        User result = sessionManager.getUser();

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
    }

    @Test
    public void testGetUserNoUser() {
        when(mockSharedPreferences.getString("user_json", null)).thenReturn(null);
        User result = sessionManager.getUser();
        assertNull(result);
    }

    @Test
    public void testIsLoggedInTrue() {
        when(mockSharedPreferences.getString("user_json", null)).thenReturn("{\"id\":\"user123\"}");
        boolean result = sessionManager.isLoggedIn();
        assertTrue(result);
    }

    @Test
    public void testIsLoggedInFalse() {
        when(mockSharedPreferences.getString("user_json", null)).thenReturn(null);
        boolean result = sessionManager.isLoggedIn();
        assertFalse(result);
    }

    @Test
    public void testClearSession() {
        sessionManager.clearSession();
        verify(mockEditor).clear();
        verify(mockEditor).apply();
    }
}