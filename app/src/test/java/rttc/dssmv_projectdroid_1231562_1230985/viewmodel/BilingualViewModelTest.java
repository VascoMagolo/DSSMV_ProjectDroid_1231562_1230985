package rttc.dssmv_projectdroid_1231562_1230985.viewmodel;

import android.app.Application;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import rttc.dssmv_projectdroid_1231562_1230985.repository.TranslationRepository;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {28})
public class BilingualViewModelTest {

    private BilingualViewModel viewModel;
    private Application application;

    @Before
    public void setUp() {
        application = ApplicationProvider.getApplicationContext();
        viewModel = new BilingualViewModel(application);
    }

    @Test
    public void testBilingualViewModelCreation() {
        assertNotNull(viewModel);
    }

    @Test
    public void testTranslateTextMethodExists() {
        try {
            viewModel.translateText("Hello", "en", "es");
            assertTrue("translateText method should exist", true);
        } catch (Exception e) {
            fail("translateText should not throw exceptions: " + e.getMessage());
        }
    }

    @Test
    public void testLiveDataObjectsExist() {
        assertNotNull("getTextForLangA should exist", viewModel.getTextForLangA());
        assertNotNull("getTextForLangB should exist", viewModel.getTextForLangB());
        assertNotNull("getTtsRequest should exist", viewModel.getTtsRequest());
        assertNotNull("getStatusMessage should exist", viewModel.getStatusMessage());
        assertNotNull("getErrorMessage should exist", viewModel.getErrorMessage());
    }

    @Test
    public void testInitialLiveDataValues() {
        assertNull("Initial text for lang A should be null", viewModel.getTextForLangA().getValue());
        assertNull("Initial text for lang B should be null", viewModel.getTextForLangB().getValue());
        assertNull("Initial TTS request should be null", viewModel.getTtsRequest().getValue());
        assertNull("Initial status message should be null", viewModel.getStatusMessage().getValue());
        assertNull("Initial error message should be null", viewModel.getErrorMessage().getValue());
    }
}