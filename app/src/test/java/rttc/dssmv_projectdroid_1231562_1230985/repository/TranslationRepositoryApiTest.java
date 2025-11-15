package rttc.dssmv_projectdroid_1231562_1230985.repository;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class TranslationRepositoryApiTest {

    private TranslationRepository translationRepository;

    @Before
    public void setUp() {
        translationRepository = new TranslationRepository();
    }

    @Test
    public void testDetectAndTranslateMakesApiCall() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] callbackCalled = {false};

        translationRepository.detectAndTranslate("Hello", "es",
                new TranslationRepository.TranslationCallback() {
                    @Override
                    public void onSuccess(String translatedText, String detectedLang) {
                        callbackCalled[0] = true;
                        latch.countDown();
                    }

                    @Override
                    public void onError(Exception e) {
                        callbackCalled[0] = true;
                        latch.countDown();
                    }
                });

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        assertTrue("Translation callback should be called within 10 seconds", completed);
        assertTrue("Callback should be invoked", callbackCalled[0]);
    }

    @Test
    public void testDirectTranslateMakesApiCall() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] callbackCalled = {false};

        translationRepository.translate("Hello", "en", "es",
                new TranslationRepository.TranslationCallback() {
                    @Override
                    public void onSuccess(String translatedText, String detectedLang) {
                        callbackCalled[0] = true;
                        latch.countDown();
                    }

                    @Override
                    public void onError(Exception e) {
                        callbackCalled[0] = true;
                        latch.countDown();
                    }
                });

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        assertTrue("Direct translate callback should be called within 10 seconds", completed);
        assertTrue("Callback should be invoked", callbackCalled[0]);
    }
}