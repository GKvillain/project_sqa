package org.apache.commons.lang;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.Test;

/**
 * Test class for LocaleUtils.
 */
public class LocaleUtilsTest {

    // Tests toLocale with null input
    @Test
    public void testToLocale_nullInput_returnsNull() {
        assertNull(LocaleUtils.toLocale(null));
    }

    // Tests toLocale with valid 2-character language code
    @Test
    public void testToLocale_validLanguageOnly_returnsLocale() {
        Locale locale = LocaleUtils.toLocale("en");
        assertEquals("en", locale.getLanguage());
        assertEquals("", locale.getCountry());
    }

    // Tests toLocale with valid 5-character language and country
    @Test
    public void testToLocale_validLanguageCountry_returnsLocale() {
        Locale locale = LocaleUtils.toLocale("en_GB");
        assertEquals("en", locale.getLanguage());
        assertEquals("GB", locale.getCountry());
    }

    // Tests toLocale with valid language, country, and variant
    @Test
    public void testToLocale_validFullLocale_returnsLocale() {
        Locale locale = LocaleUtils.toLocale("en_GB_xxx");
        assertEquals("en", locale.getLanguage());
        assertEquals("GB", locale.getCountry());
        assertEquals("xxx", locale.getVariant());
    }

    // Tests toLocale with invalid length
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidLength_throwsException() {
        LocaleUtils.toLocale("eng");
    }

    // Tests toLocale with non-lowercase first character
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_uppercaseFirstChar_throwsException() {
        LocaleUtils.toLocale("En");
    }

    // Tests toLocale with missing underscore separator
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_missingUnderscore_throwsException() {
        LocaleUtils.toLocale("enGB");
    }

    // Tests toLocale with non-uppercase country code
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_nonUppercaseCountry_throwsException() {
        LocaleUtils.toLocale("en_gb");
    }

    // Tests toLocale with length greater than 5 but less than 7 (invalid)
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_lengthSix_throwsException() {
        LocaleUtils.toLocale("en_GBx");
    }

    // Tests toLocale with length 7 but missing second underscore
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_missingSecondUnderscore_throwsException() {
        LocaleUtils.toLocale("en_GBxx");
    }

    // Tests localeLookupList with null locale
    @Test
    public void testLocaleLookupList_nullInput_returnsEmptyList() {
        List list = LocaleUtils.localeLookupList(null);
        assertTrue(list.isEmpty());
    }

    // Tests localeLookupList with a full locale
    @Test
    public void testLocaleLookupList_fullLocale_returnsListWithVariants() {
        Locale locale = new Locale("fr", "CA", "xxx");
        List list = LocaleUtils.localeLookupList(locale);
        assertEquals(3, list.size());
        assertEquals(locale, list.get(0));
        assertEquals(new Locale("fr", "CA"), list.get(1));
        assertEquals(new Locale("fr", ""), list.get(2));
    }

    // Tests localeLookupList with locale and a different default locale
    @Test
    public void testLocaleLookupList_withDefaultLocale_appendsDefault() {
        Locale locale = new Locale("fr", "CA", "xxx");
        Locale defaultLocale = new Locale("en");
        List list = LocaleUtils.localeLookupList(locale, defaultLocale);
        assertEquals(4, list.size());
        assertEquals(defaultLocale, list.get(3));
    }

    // Tests localeLookupList with language-only locale (no country)
    @Test
    public void testLocaleLookupList_languageOnly_returnsSingleLocale() {
        Locale locale = new Locale("fr");
        List list = LocaleUtils.localeLookupList(locale);
        assertEquals(1, list.size());
        assertEquals(locale, list.get(0));
    }

    // Tests availableLocaleList returns non-null list
    @Test
    public void testAvailableLocaleList_returnsNonEmptyList() {
        List list = LocaleUtils.availableLocaleList();
        assertNotNull(list);
        assertTrue(list.size() > 0);
    }

    // Tests availableLocaleSet returns a set with correct size
    @Test
    public void testAvailableLocaleSet_returnsNonEmptySet() {
        Set set = LocaleUtils.availableLocaleSet();
        assertNotNull(set);
        assertEquals(LocaleUtils.availableLocaleList().size(), set.size());
    }

    // Tests isAvailableLocale with known locale
    @Test
    public void testIsAvailableLocale_knownLocale_returnsTrue() {
        Locale locale = Locale.ENGLISH;
        assertTrue(LocaleUtils.isAvailableLocale(locale));
    }

    // Tests isAvailableLocale with unknown locale
    @Test
    public void testIsAvailableLocale_unknownLocale_returnsFalse() {
        Locale locale = new Locale("xx", "YY");
        assertFalse(LocaleUtils.isAvailableLocale(locale));
    }

    // Tests languagesByCountry with known country code and locale variant removal
    @Test
    public void testLanguagesByCountry_knownCountry_returnsLanguagesList() {
        List langs = LocaleUtils.languagesByCountry("GB");
        assertNotNull(langs);
        assertTrue(langs.size() > 0);
        for (int i = 0; i < langs.size(); i++) {
            Locale locale = (Locale) langs.get(i);
            assertEquals("GB", locale.getCountry());
            assertEquals("", locale.getVariant());
        }
    }

    // Tests languagesByCountry with null country code
    @Test
    public void testLanguagesByCountry_nullInput_returnsEmptyList() {
        List langs = LocaleUtils.languagesByCountry(null);
        assertNotNull(langs);
        assertTrue(langs.isEmpty());
    }

    // Tests languagesByCountry with unknown country code
    @Test
    public void testLanguagesByCountry_unknownCountry_returnsEmptyList() {
        List langs = LocaleUtils.languagesByCountry("ZZ");
        assertNotNull(langs);
        assertTrue(langs.isEmpty());
    }

    // Tests countriesByLanguage with known language code
    @Test
    public void testCountriesByLanguage_knownLanguage_returnsCountriesList() {
        List countries = LocaleUtils.countriesByLanguage("en");
        assertNotNull(countries);
        for (int i = 0; i < countries.size(); i++) {
            Locale locale = (Locale) countries.get(i);
            assertEquals("en", locale.getLanguage());
            assertTrue(locale.getCountry().length() > 0);
            assertEquals("", locale.getVariant());
        }
    }

    // Tests countriesByLanguage with null language code
    @Test
    public void testCountriesByLanguage_nullInput_returnsEmptyList() {
        List countries = LocaleUtils.countriesByLanguage(null);
        assertNotNull(countries);
        assertTrue(countries.isEmpty());
    }

    // Tests countriesByLanguage with unknown language code
    @Test
    public void testCountriesByLanguage_unknownLanguage_returnsEmptyList() {
        List countries = LocaleUtils.countriesByLanguage("zz");
        assertNotNull(countries);
        assertTrue(countries.isEmpty());
    }
}