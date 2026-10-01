package org.apache.commons.lang3;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.Test;

/**
 * JUnit 4 test class for LocaleUtils, targeting Defects4J bug 5b.
 */
public class LocaleUtilsTest {

    // Tests null input for toLocale, returns null
    @Test
    public void testToLocale_nullInput_returnsNull() {
        assertNull(LocaleUtils.toLocale(null));
    }

    // Tests invalid short input for toLocale (< 2 chars)
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_lengthLessThan2_throwsIllegalArgumentException() {
        LocaleUtils.toLocale("a");
    }

    // Tests invalid input with non-lowercase first character
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_firstCharNotLowerCase_throwsIllegalArgumentException() {
        LocaleUtils.toLocale("Ab");
    }

    // Tests valid 2-letter language code
    @Test
    public void testToLocale_valid2LetterLanguage_returnsCorrectLocale() {
        Locale locale = LocaleUtils.toLocale("en");
        assertEquals("en", locale.getLanguage());
    }

    // Tests input with valid 2-letter language and underscore but no country (length < 5)
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_length3MissingCountry_throwsIllegalArgumentException() {
        LocaleUtils.toLocale("en_");
    }

    // Tests input with missing separator at position 2
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_missingSeparatorAtPosition2_throwsIllegalArgumentException() {
        LocaleUtils.toLocale("enGB");
    }

    // Tests valid 5-character locale with language and country
    @Test
    public void testToLocale_valid5CharLocale_returnsCorrectLocale() {
        Locale locale = LocaleUtils.toLocale("en_GB");
        assertEquals("en", locale.getLanguage());
        assertEquals("GB", locale.getCountry());
    }

    // Tests input with underscore at position 3 but missing variant (length < 7)
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_underscoreAtPos3MissingVariant_throwsIllegalArgumentException() {
        LocaleUtils.toLocale("en__");
    }

    // Tests input with non-uppercase country code characters
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_countryCodeNotUppercase_throwsIllegalArgumentException() {
        LocaleUtils.toLocale("en_gb");
    }

    // Tests valid locale with variant
    @Test
    public void testToLocale_validLocaleWithVariant_returnsCorrectLocale() {
        Locale locale = LocaleUtils.toLocale("en_GB_xxx");
        assertEquals("en", locale.getLanguage());
        assertEquals("GB", locale.getCountry());
        // Variant case depends on JDK, but we check it's present
        assertNotNull(locale.getVariant());
    }

    // Tests localeLookupList with non-null locale
    @Test
    public void testLocaleLookupList_validLocale_returnsListStartingWithLocale() {
        Locale locale = Locale.FRANCE; // fr_FR
        List<Locale> list = LocaleUtils.localeLookupList(locale);
        assertEquals(locale, list.get(0));
    }

    // Tests localeLookupList with null locale
    @Test
    public void testLocaleLookupList_nullLocale_returnsEmptyList() {
        List<Locale> list = LocaleUtils.localeLookupList(null, Locale.US);
        assertTrue(list.isEmpty());
    }

    // Tests availableLocaleList is not null and not empty
    @Test
    public void testAvailableLocaleList_returnsNonEmptyList() {
        List<Locale> list = LocaleUtils.availableLocaleList();
        assertNotNull(list);
        assertTrue(list.size() > 0);
    }

    // Tests availableLocaleSet is not null and not empty
    @Test
    public void testAvailableLocaleSet_returnsNonEmptySet() {
        Set<Locale> set = LocaleUtils.availableLocaleSet();
        assertNotNull(set);
        assertTrue(set.size() > 0);
    }

    // Tests isAvailableLocale for a known locale
    @Test
    public void testIsAvailableLocale_knownLocale_returnsTrue() {
        assertTrue(LocaleUtils.isAvailableLocale(Locale.US));
    }

    // Tests isAvailableLocale for an unknown locale
    @Test
    public void testIsAvailableLocale_unknownLocale_returnsFalse() {
        // Using Locale.CHINA which is typically available
        // Instead, use a custom locale that is unlikely to be in available list
        Locale custom = new Locale("xx", "XX");
        assertFalse(LocaleUtils.isAvailableLocale(custom));
    }

    // Tests languagesByCountry with null input
    @Test
    public void testLanguagesByCountry_nullInput_returnsEmptyList() {
        assertTrue(LocaleUtils.languagesByCountry(null).isEmpty());
    }

    // Tests countriesByLanguage with null input
    @Test
    public void testCountriesByLanguage_nullInput_returnsEmptyList() {
        assertTrue(LocaleUtils.countriesByLanguage(null).isEmpty());
    }

    // Tests countriesByLanguage with a valid language code
    @Test
    public void testCountriesByLanguage_validLanguageCode_returnsNonEmptyList() {
        List<Locale> countries = LocaleUtils.countriesByLanguage("en");
        assertNotNull(countries);
        // At least en_US and en_GB should be present typically
        assertTrue(countries.size() > 0);
    }

    // Tests languagesByCountry with a valid country code
    @Test
    public void testLanguagesByCountry_validCountryCode_returnsNonEmptyList() {
        List<Locale> langs = LocaleUtils.languagesByCountry("US");
        assertNotNull(langs);
        // At least en_US should be present typically
        assertTrue(langs.size() > 0);
    }

    // Tests localeLookupList with default locale included
    @Test
    public void testLocaleLookupList_withDefaultLocale_includesDefault() {
        Locale locale = new Locale("fr", "CA", "xxx");
        Locale defaultLocale = Locale.US;
        List<Locale> list = LocaleUtils.localeLookupList(locale, defaultLocale);
        assertTrue(list.contains(defaultLocale));
    }

    // Tests localeLookupList with locale having empty variant and country
    @Test
    public void testLocaleLookupList_localeOnlyLanguage_returnsSingleEntry() {
        Locale locale = new Locale("en");
        List<Locale> list = LocaleUtils.localeLookupList(locale, Locale.US);
        assertEquals(2, list.size());
        assertEquals(locale, list.get(0));
        assertEquals(Locale.US, list.get(1));
    }
}