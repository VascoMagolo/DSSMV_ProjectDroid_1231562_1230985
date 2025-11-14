package rttc.dssmv_projectdroid_1231562_1230985.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class TranslationTest {

    @Test
    public void testtranslationConstructor() {
        Translation translation = new Translation("user123", "Hello world", "Hola mundo", "en", "es");
        assertEquals("user123", translation.getUserId());
        assertEquals("Hello world", translation.getOriginalText());
        assertEquals("Hola mundo", translation.getTranslatedText());
        assertEquals("en", translation.getSourceLanguage());
        assertEquals("es", translation.getTargetLanguage());
        assertNotNull(translation.getTimestamp());

        Boolean favorite = translation.getFavorite();
        if (favorite != null) {
            assertFalse(favorite);
        }
    }

    @Test
    public void testtranslationSetters() {
        Translation translation = new Translation();
        translation.setId("conv123");
        translation.setUserId("user456");
        translation.setOriginalText("Test text");
        translation.setTranslatedText("Texto de prueba");
        translation.setSourceLanguage("en");
        translation.setTargetLanguage("es");
        translation.setFavorite(true);

        Date testDate = new Date();
        translation.setTimestamp(testDate);

        assertEquals("conv123", translation.getId());
        assertEquals("user456", translation.getUserId());
        assertEquals("Test text", translation.getOriginalText());
        assertEquals("Texto de prueba", translation.getTranslatedText());
        assertEquals("en", translation.getSourceLanguage());
        assertEquals("es", translation.getTargetLanguage());
        assertTrue(translation.getFavorite());
        assertEquals(testDate, translation.getTimestamp());
    }

    @Test
    public void testFavoriteToggle() {
        Translation translation = new Translation();
        translation.setFavorite(true);
        assertTrue(translation.getFavorite());
        translation.setFavorite(false);
        assertFalse(translation.getFavorite());
    }
}