package org.apache.commons.codec.language.bm;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.Test;

public class LangTest {

    @Test
    public void testInstance_allNameTypes_returnsNonNull() {
        for (NameType nameType : NameType.values()) {
            assertNotNull("Lang instance for " + nameType, Lang.instance(nameType));
        }
    }

    @Test
    public void testInstance_sameNameType_sameInstance() {
        assertSame(Lang.instance(NameType.GENERIC), Lang.instance(NameType.GENERIC));
    }

    @Test
    public void testLoadFromResource_existingResource_returnsNonNull() {
        Lang lang = Lang.loadFromResource(
            "org/apache/commons/codec/language/bm/lang.txt",
            Languages.getInstance(NameType.GENERIC)
        );
        assertNotNull(lang);
    }

    @Test(expected = IllegalStateException.class)
    public void testLoadFromResource_missingResource_throwsIllegalStateException() {
        Lang.loadFromResource(
            "org/apache/commons/codec/language/bm/nonexistent.txt",
            Languages.getInstance(NameType.GENERIC)
        );
    }

    @Test
    public void testLoadFromResource_rulesLoadedForInstance_shouldNotBeEmpty() throws Exception {
        Lang lang = Lang.instance(NameType.GENERIC);
        List<?> rules = getPrivateField(lang, "rules");
        assertNotNull(rules);
        assertFalse("Lang rules should not be empty", rules.isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void testGuessLanguage_nullInput_throwsNullPointerException() {
        Lang.instance(NameType.GENERIC).guessLanguage(null);
    }

    @Test(expected = NullPointerException.class)
    public void testGuessLanguages_nullInput_throwsNullPointerException() {
        Lang.instance(NameType.GENERIC).guessLanguages(null);
    }

    @Test
    public void testGuessLanguage_blankInput_returnsAny() {
        Lang lang = Lang.instance(NameType.GENERIC);
        assertEquals(Languages.ANY, lang.guessLanguage(""));
        assertEquals(Languages.ANY, lang.guessLanguage("   "));
    }

    @Test
    public void testGuessLanguages_blankInput_returnsNonNull() {
        Lang lang = Lang.instance(NameType.GENERIC);
        assertNotNull(lang.guessLanguages(""));
        assertNotNull(lang.guessLanguages("   "));
    }

    @Test
    public void testGuessLanguage_mixedCase_returnsSameAsLowerCase() {
        Lang lang = Lang.instance(NameType.GENERIC);
        String lower = lang.guessLanguage("hello");
        String upper = lang.guessLanguage("HELLO");
        assertEquals(lower, upper);
    }

    @Test
    public void testGuessLanguages_mixedCase_returnsSameAsLowerCase() {
        Lang lang = Lang.instance(NameType.GENERIC);
        Languages.LanguageSet lower = lang.guessLanguages("hello");
        Languages.LanguageSet upper = lang.guessLanguages("HELLO");
        assertEquals(lower, upper);
    }

    @Test
    public void testGuessLanguage_nonEmptyInput_returnsNonNull() {
        String result = Lang.instance(NameType.GENERIC).guessLanguage("Anglo");
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test
    public void testGuessLanguages_nonEmptyInput_returnsNonNull() {
        assertNotNull(Lang.instance(NameType.GENERIC).guessLanguages("Anglo"));
    }

    @Test
    public void testGuessLanguage_digitsInput_returnsNonNull() {
        String result = Lang.instance(NameType.GENERIC).guessLanguage("ABC123");
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test
    public void testGuessLanguages_digitsInput_returnsNonNull() {
        assertNotNull(Lang.instance(NameType.GENERIC).guessLanguages("ABC123"));
    }

    @SuppressWarnings("unchecked")
    private static <T> T getPrivateField(Object obj, String fieldName) throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(obj);
    }

    // ========== Test cases ใหม่สำหรับส่วนที่ยังไม่ถูกครอบคลุม ==========

    @Test
    public void testInstance_differentNameType_returnsDifferentInstance() {
        assertNotSame(Lang.instance(NameType.GENERIC), Lang.instance(NameType.ASHKENAZI));
        assertNotSame(Lang.instance(NameType.GENERIC), Lang.instance(NameType.SEPHARDIC));
        assertNotSame(Lang.instance(NameType.ASHKENAZI), Lang.instance(NameType.SEPHARDIC));
    }

    @Test
    public void testGuessLanguage_withAshkenazi_returnsNonNull() {
        String result = Lang.instance(NameType.ASHKENAZI).guessLanguage("hello");
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test
    public void testGuessLanguages_withAshkenazi_returnsNonNull() {
        assertNotNull(Lang.instance(NameType.ASHKENAZI).guessLanguages("hello"));
    }

    @Test
    public void testGuessLanguage_withSephardic_returnsNonNull() {
        String result = Lang.instance(NameType.SEPHARDIC).guessLanguage("hello");
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test
    public void testGuessLanguages_withSephardic_returnsNonNull() {
        assertNotNull(Lang.instance(NameType.SEPHARDIC).guessLanguages("hello"));
    }

    @Test
    public void testLoadFromResource_ashkenaziResource_notEmpty() throws Exception {
        Lang lang = Lang.instance(NameType.ASHKENAZI);
        List<?> rules = getPrivateField(lang, "rules");
        assertNotNull(rules);
        assertFalse("Rules for ASHKENAZI should not be empty", rules.isEmpty());
    }

    @Test
    public void testLoadFromResource_sephardicResource_notEmpty() throws Exception {
        Lang lang = Lang.instance(NameType.SEPHARDIC);
        List<?> rules = getPrivateField(lang, "rules");
        assertNotNull(rules);
        assertFalse("Rules for SEPHARDIC should not be empty", rules.isEmpty());
    }

    @Test
    public void testGuessLanguage_numericOnly_returnsNonNull() {
        String result = Lang.instance(NameType.GENERIC).guessLanguage("12345");
        assertNotNull(result);
        assertTrue(result.length() >0);
    }

    @Test
    public void testGuessLanguages_numericOnly_returnsNonNull() {
        assertNotNull(Lang.instance(NameType.GENERIC).guessLanguages("12345"));
    }

    @Test
    public void testGuessLanguage_symbolsOnly_returnsNonNull() {
        String result = Lang.instance(NameType.GENERIC).guessLanguage("!@#$%");
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test
    public void testGuessLanguages_symbolsOnly_returnsNonNull() {
        assertNotNull(Lang.instance(NameType.GENERIC).guessLanguages("!@#$%"));
    }

    @Test
    public void testGuessLanguage_longInput_returnsNonNull() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("a");
        }
        String result = Lang.instance(NameType.GENERIC).guessLanguage(sb.toString());
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    @Test
    public void testGuessLanguages_longInput_returnsNonNull() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("a");
        }
        assertNotNull(Lang.instance(NameType.GENERIC).guessLanguages(sb.toString()));
    }
}