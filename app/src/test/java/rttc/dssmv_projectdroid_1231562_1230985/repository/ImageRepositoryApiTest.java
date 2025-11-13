package rttc.dssmv_projectdroid_1231562_1230985.repository;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class ImageRepositoryApiTest {

    private ImageRepository imageRepository = new ImageRepository();

    @Test
    public void testExtractTextCallsOCRAPI() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] apiCalled = {false};

        byte[] fakeImage = new byte[]{
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
                0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01,
                0x01, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00
        };

        imageRepository.extractTextFromImage(fakeImage,
                new ImageRepository.OCRCallback() {
                    @Override
                    public void onSuccess(String extractedText) {
                        apiCalled[0] = true;
                        latch.countDown();
                    }

                    @Override
                    public void onError(Exception e) {
                        apiCalled[0] = true;
                        latch.countDown();
                    }
                });

        boolean completed = latch.await(15, TimeUnit.SECONDS);
        assertTrue("OCR API call should complete within 15 seconds", completed);
        assertTrue("OCR API should be called", apiCalled[0]);
    }
}