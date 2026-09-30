package org.apache.commons.lang;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * JUnit 4 test class for LocaleUtils, targeting Defects4J bug 57b.
 */
public class LocaleUtilsTest {

    // ----- toLocale tests -----
    @Test
    public void testToLocale_nullInput_returnsNull() {
        assertNull(LocaleUtils.toLocale(null));
    }

    @Test
    public void testToLocale_shortLanguage_returnsLocale() {
        Locale locale = LocaleUtils.toLocale("en");
        assertEquals("en", locale.getLanguage());
        assertEquals("", locale.getCountry());
        assertEquals("", locale.getVariant());
    }

    @Test
    public void testToLocale_languageCountry_returnsLocale() {
        Locale locale = LocaleUtils.toLocale("en_GB");
        assertEquals("en", locale.getLanguage());
        assertEquals("GB", locale.getCountry());
        assertEquals("", locale.getVariant());
    }

    @Test
    public void testToLocale_languageCountryVariant_returnsLocale() {
        Locale locale = LocaleUtils.toLocale("en_GB_XXX");
        assertEquals("en", locale.getLanguage());
        assertEquals("GB", locale.getCountry());
        assertEquals("XXX", locale.getVariant());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidLength1_throwsException() {
        LocaleUtils.toLocale("e");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidLength3_throwsException() {
        LocaleUtils.toLocale("en_");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidLength4_throwsException() {
        LocaleUtils.toLocale("en_G");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidLength6_throwsException() {
        LocaleUtils.toLocale("en_GB_");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_uppercaseLanguage_throwsException() {
        LocaleUtils.toLocale("EN");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_lowercaseCountry_throwsException() {
        LocaleUtils.toLocale("en_gb");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_missingUnderscore_throwsException() {
        LocaleUtils.toLocale("enGB");
    }

    // variant containing underscore (accepted by Locale constructor)
    @Test
    public void testToLocale_longVariantWithUnderscore_returnsLocale() {
        Locale locale = LocaleUtils.toLocale("en_GB_xxx_yyy");
        assertEquals("en", locale.getLanguage());
        assertEquals("GB", locale.getCountry());
        assertEquals("xxx_yyy", locale.getVariant());
    }

    // ----- localeLookupList tests -----
    @Test
    public void testLocaleLookupList_nullLocale_returnsEmptyList() {
        List list = LocaleUtils.localeLookupList(null);
        assertTrue(list.isEmpty());
    }

    @Test
    public void testLocaleLookupList_languageOnly_returnsListWithDefault() {
        Locale locale = new Locale("fr");
        List list = LocaleUtils.localeLookupList(locale);
        assertTrue(list.contains(locale));
        assertTrue(list.contains(Locale.getDefault()));
    }

    @Test
    public void testLocaleLookupList_fullVariantCountry_returnsList() {
        Locale locale = new Locale("fr", "FR", "XXX");
        List list = LocaleUtils.localeLookupList(locale);
        assertEquals(4, list.size());
        assertEquals(locale, list.get(0));
        assertEquals(new Locale("fr", "FR"), list.get(1));
        assertEquals(new Locale("fr", ""), list.get(2));
        assertEquals(Locale.getDefault(), list.get(3));
    }

    @Test
    public void testLocaleLookupList_countryOnly_returnsList() {
        Locale locale = new Locale("fr", "FR");
        List list = LocaleUtils.localeLookupList(locale);
        assertEquals(3, list.size());
        assertEquals(locale, list.get(0));
        assertEquals(new Locale("fr", ""), list.get(1));
        assertEquals(Locale.getDefault(), list.get(2));
    }

    @Test
    public void testLocaleLookupList_localeEqualsDefault_returnsNoDuplicate() {
        Locale locale = new Locale("fr");
        List list = LocaleUtils.localeLookupList(locale, locale);
        assertEquals(1, list.size());
    }

    // ----- availableLocaleList / Set tests -----
    @Test
    public void testAvailableLocaleList_isNotEmpty() {
        assertFalse(LocaleUtils.availableLocaleList().isEmpty());
    }

    @Test
    public void testAvailableLocaleSet_isNotEmpty() {
        assertFalse(LocaleUtils.availableLocaleSet().isEmpty());
    }

    @Test
    public void testAvailableLocaleSet_containsAllFromList() {
        Set set = LocaleUtils.availableLocaleSet();
        List list = LocaleUtils.availableLocaleList();
        assertTrue(set.containsAll(list));
    }

    // ----- isAvailableLocale tests (defect detection) -----
    @Test
    public void testIsAvailableLocale_beforeSetInitialized_shouldNotThrowNPE() {
        // In buggy version, this call throws NullPointerException because cAvailableLocaleSet is null.
        // The test will fail, detecting the defect.
        Locale fake = new Locale("xx", "XX");
        assertFalse(LocaleUtils.isAvailableLocale(fake));
    }

    @Test
    public void testIsAvailableLocale_afterSetInitialized_returnsCorrectResult() {
        LocaleUtils.availableLocaleSet(); // force initialization
        assertTrue(LocaleUtils.isAvailableLocale(Locale.US));
        assertFalse(LocaleUtils.isAvailableLocale(new Locale("xx", "XX")));
    }

    // ----- languagesByCountry tests -----
    @Test
    public void testLanguagesByCountry_nullInput_returnsEmptyList() {
        assertTrue(LocaleUtils.languagesByCountry(null).isEmpty());
    }

    @Test
    public void testLanguagesByCountry_invalidCountry_returnsEmptyList() {
        assertTrue(LocaleUtils.languagesByCountry("ZZ").isEmpty());
    }

    @Test
    public void testLanguagesByCountry_us_returnsNonEmptyList() {
        List langs = LocaleUtils.languagesByCountry("US");
        assertFalse(langs.isEmpty());
        for (Object obj : langs) {
            Locale loc = (Locale) obj;
            assertEquals("US", loc.getCountry());
            assertEquals("", loc.getVariant());
        }
    }

    // ----- countriesByLanguage tests -----
    @Test
    public void testCountriesByLanguage_nullInput_returnsEmptyList() {
        assertTrue(LocaleUtils.countriesByLanguage(null).isEmpty());
    }

    @Test
    public void testCountriesByLanguage_invalidLanguage_returnsEmptyList() {
        assertTrue(LocaleUtils.countriesByLanguage("zz").isEmpty());
    }

    @Test
    public void testCountriesByLanguage_en_returnsNonEmptyList() {
        List countries = LocaleUtils.countriesByLanguage("en");
        assertFalse(countries.isEmpty());
        for (Object obj : countries) {
            Locale loc = (Locale) obj;
            assertEquals("en", loc.getLanguage());
            assertTrue(loc.getCountry().length() > 0);
            assertEquals("", loc.getVariant());
        }
    }
}