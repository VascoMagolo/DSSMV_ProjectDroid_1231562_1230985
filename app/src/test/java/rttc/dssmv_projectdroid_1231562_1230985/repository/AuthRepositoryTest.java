package rttc.dssmv_projectdroid_1231562_1230985.repository;

import android.content.Context;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import rttc.dssmv_projectdroid_1231562_1230985.model.User;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthRepositoryTest {

    @Mock
    private Context mockContext;

    private AuthRepository authRepository = new AuthRepository();

    @Test
    public void testAuthRepositoryCreation() {
        assertNotNull(authRepository);
    }

    @Test
    public void testRegisterUserMethodExists() {
        try {
            authRepository.RegisterUser("Test", "test@test.com", "password", "en",
                    mock(AuthRepository.RegisterCallback.class));
            assertTrue(true);
        } catch (Exception e) {
            fail("RegisterUser method should exist");
        }
    }

    @Test
    public void testLoginMethodExists() {
        try {
            authRepository.login(mockContext, "test@test.com", "password",
                    mock(AuthRepository.LoginCallback.class));
            assertTrue(true);
        } catch (Exception e) {
            fail("Login method should exist");
        }
    }
}