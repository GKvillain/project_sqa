package org.apache.commons.lang;

import org.junit.Test;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LocaleUtilsTest {

    // Tests constructor creation
    @Test
    public void testConstructor_defaultInstance_createsSuccessfully() {
        assertNotNull(new LocaleUtils());
    }

    // Tests null input conversion
    @Test
    public void testToLocale_nullInput_returnsNull() {
        assertNull(LocaleUtils.toLocale(null));
    }

    // Tests 2-character language string
    @Test
    public void testToLocale_languageOnly_returnsLocale() {
        Locale locale = LocaleUtils.toLocale("en");
        assertNotNull(locale);
        assertEquals("en", locale.getLanguage());
        assertEquals("", locale.getCountry());
        assertEquals("", locale.getVariant());
    }

    // Tests 5-character language and country string
    @Test
    public void testToLocale_languageAndCountry_returnsLocale() {
        Locale locale = LocaleUtils.toLocale("en_US");
        assertNotNull(locale);
        assertEquals("en", locale.getLanguage());
        assertEquals("US", locale.getCountry());
        assertEquals("", locale.getVariant());
    }

    // Tests language, country, and variant string
    @Test
    public void testToLocale_languageCountryAndVariant_returnsLocale() {
        Locale locale = LocaleUtils.toLocale("en_US_WIN");
        assertNotNull(locale);
        assertEquals("en", locale.getLanguage());
        assertEquals("US", locale.getCountry());
        assertEquals("WIN", locale.getVariant());
    }

    // Tests invalid string length
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidLength_throwsException() {
        LocaleUtils.toLocale("e");
    }

    // Tests invalid language format
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidLanguageCase_throwsException() {
        LocaleUtils.toLocale("EN_US");
    }

    // Tests invalid separator between language and country
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidCountrySeparator_throwsException() {
        LocaleUtils.toLocale("en-US");
    }

    // Tests invalid country code format
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidCountryCase_throwsException() {
        LocaleUtils.toLocale("en_us");
    }

    // Tests invalid separator before variant
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidVariantSeparator_throwsException() {
        LocaleUtils.toLocale("en_US#WIN");
    }

    // Tests isAvailableLocale method
    @Test
    public void testIsAvailableLocale_checkLocale_returnsBoolean() {
        assertTrue(LocaleUtils.isAvailableLocale(Locale.US));
        assertFalse(LocaleUtils.isAvailableLocale(new Locale("xx", "XX")));
    }

    // Tests availableLocaleList retrieval
    @Test
    public void testAvailableLocaleList_validCall_returnsUnmodifiableList() {
        List list = LocaleUtils.availableLocaleList();
        assertNotNull(list);
        assertFalse(list.isEmpty());
        assertTrue(list.contains(Locale.US));
    }

    // Tests availableLocaleSet retrieval
    @Test
    public void testAvailableLocaleSet_validCall_returnsUnmodifiableSet() {
        Set set = LocaleUtils.availableLocaleSet();
        assertNotNull(set);
        assertFalse(set.isEmpty());
        assertTrue(set.contains(Locale.US));
    }

    // Tests localeLookupList with null input
    @Test
    public void testLocaleLookupList_nullLocale_returnsEmptyList() {
        List list = LocaleUtils.localeLookupList(null);
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    // Tests localeLookupList with full locale
    @Test
    public void testLocaleLookupList_languageCountryAndVariant_returnsHierarchy() {
        Locale locale = new Locale("fr", "CA", "xxx");
        List list = LocaleUtils.localeLookupList(locale);
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals(locale, list.get(0));
        assertEquals(new Locale("fr", "CA"), list.get(1));
        assertEquals(new Locale("fr"), list.get(2));
    }

    // Tests localeLookupList with defaultLocale fallback
    @Test
    public void testLocaleLookupList_withDefaultLocale_includesDefaultLocale() {
        Locale locale = new Locale("fr", "CA");
        Locale defaultLocale = Locale.ENGLISH;
        List list = LocaleUtils.localeLookupList(locale, defaultLocale);
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals(locale, list.get(0));
        assertEquals(new Locale("fr"), list.get(1));
        assertEquals(defaultLocale, list.get(2));
    }

    // Tests languagesByCountry with null
    @Test
    public void testLanguagesByCountry_nullInput_returnsEmptyList() {
        List list = LocaleUtils.languagesByCountry(null);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    // Tests languagesByCountry with valid country
    @Test
    public void testLanguagesByCountry_validCountry_returnsLanguages() {
        List list = LocaleUtils.languagesByCountry("US");
        assertNotNull(list);
        assertFalse(list.isEmpty());
        assertTrue(list.contains(new Locale("en", "US")));
    }

    // Tests countriesByLanguage with null
    @Test
    public void testCountriesByLanguage_nullInput_returnsEmptyList() {
        List list = LocaleUtils.countriesByLanguage(null);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    // Tests countriesByLanguage with valid language
    @Test
    public void testCountriesByLanguage_validLanguage_returnsCountries() {
        List list = LocaleUtils.countriesByLanguage("en");
        assertNotNull(list);
        assertFalse(list.isEmpty());
        assertTrue(list.contains(new Locale("en", "US")));
    }
}