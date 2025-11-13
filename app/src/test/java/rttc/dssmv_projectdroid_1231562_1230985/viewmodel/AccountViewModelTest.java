package rttc.dssmv_projectdroid_1231562_1230985.viewmodel;

import android.app.Application;
import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import rttc.dssmv_projectdroid_1231562_1230985.model.User;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {28})
public class AccountViewModelTest {

    private AccountViewModel viewModel;
    private Application application;

    @Before
    public void setUp() {
        application = ApplicationProvider.getApplicationContext();
        viewModel = new AccountViewModel(application);
    }

    @Test
    public void testAccountViewModelCreation() {
        assertNotNull(viewModel);
    }

    @Test
    public void testDeleteUserAccountMethodExists() {
        try {
            Context realContext = ApplicationProvider.getApplicationContext();
            viewModel.deleteUserAccount(realContext);
            assertTrue("deleteUserAccount method should exist", true);
        } catch (Exception e) {
            fail("deleteUserAccount should not throw exceptions: " + e.getMessage());
        }
    }

    @Test
    public void testUpdateUserAccountMethodExists() {
        try {
            Context realContext = ApplicationProvider.getApplicationContext();
            User user = new User("Test", "test@test.com", "pass", "123", "en");
            viewModel.updateUserAccount(realContext, user);
            assertTrue("updateUserAccount method should exist", true);
        } catch (Exception e) {
            fail("updateUserAccount should not throw exceptions: " + e.getMessage());
        }
    }

    @Test
    public void testLiveDataObjectsExist() {
        assertNotNull("getDeleteSuccess should exist", viewModel.getDeleteSuccess());
        assertNotNull("getUpdateSuccess should exist", viewModel.getUpdateSuccess());
        assertNotNull("getErrorMessage should exist", viewModel.getErrorMessage());
    }

    @Test
    public void testInitialLiveDataValues() {
        assertNull("Initial delete success should be null", viewModel.getDeleteSuccess().getValue());
        assertNull("Initial update success should be null", viewModel.getUpdateSuccess().getValue());
        assertNull("Initial error message should be null", viewModel.getErrorMessage().getValue());
    }
}