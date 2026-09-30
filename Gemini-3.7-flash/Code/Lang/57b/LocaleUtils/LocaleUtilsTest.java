package org.apache.commons.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.Test;

public class LocaleUtilsTest {

    // Tests public constructor instantiation
    @Test
    public void testConstructor_instantiation_success() {
        assertNotNull(new LocaleUtils());
    }

    // Tests null string returns null
    @Test
    public void testToLocale_nullInput_returnsNull() {
        assertNull(LocaleUtils.toLocale(null));
    }

    // Tests valid 2-letter language code
    @Test
    public void testToLocale_languageOnly_returnsLocale() {
        Locale loc = LocaleUtils.toLocale("en");
        assertEquals(new Locale("en", ""), loc);
    }

    // Tests valid language and country code
    @Test
    public void testToLocale_languageAndCountry_returnsLocale() {
        Locale loc = LocaleUtils.toLocale("en_US");
        assertEquals(new Locale("en", "US"), loc);
    }

    // Tests valid language, country and variant code
    @Test
    public void testToLocale_languageCountryAndVariant_returnsLocale() {
        Locale loc = LocaleUtils.toLocale("en_US_POSIX");
        assertEquals(new Locale("en", "US", "POSIX"), loc);
    }

    // Tests invalid length triggers exception
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidLength_throwsException() {
        LocaleUtils.toLocale("eng");
    }

    // Tests uppercase language characters triggers exception
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_uppercaseLanguage_throwsException() {
        LocaleUtils.toLocale("EN");
    }

    // Tests invalid separator between language and country
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidSeparatorAtCountry_throwsException() {
        LocaleUtils.toLocale("en-US");
    }

    // Tests lowercase country code triggers exception
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_lowercaseCountry_throwsException() {
        LocaleUtils.toLocale("en_us");
    }

    // Tests invalid separator between country and variant
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidSeparatorAtVariant_throwsException() {
        LocaleUtils.toLocale("en_US-POSIX");
    }

    // Tests localeLookupList with null input returns empty list
    @Test
    public void testLocaleLookupList_nullLocale_returnsEmptyList() {
        List list = LocaleUtils.localeLookupList(null);
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    // Tests localeLookupList hierarchy with variant, country and language
    @Test
    public void testLocaleLookupList_fullLocale_returnsHierarchy() {
        Locale locale = new Locale("fr", "CA", "xxx");
        List list = LocaleUtils.localeLookupList(locale);
        assertEquals(3, list.size());
        assertEquals(locale, list.get(0));
        assertEquals(new Locale("fr", "CA"), list.get(1));
        assertEquals(new Locale("fr", ""), list.get(2));
    }

    // Tests localeLookupList with distinct default locale appended
    @Test
    public void testLocaleLookupList_withDefaultLocale_appendsDefault() {
        Locale locale = new Locale("fr", "CA");
        Locale defaultLocale = new Locale("en", "US");
        List list = LocaleUtils.localeLookupList(locale, defaultLocale);
        assertEquals(3, list.size());
        assertEquals(locale, list.get(0));
        assertEquals(new Locale("fr", ""), list.get(1));
        assertEquals(defaultLocale, list.get(2));
    }

    // Tests availableLocaleList returns unmodifiable non-empty list
    @Test
    public void testAvailableLocaleList_notEmpty() {
        List list = LocaleUtils.availableLocaleList();
        assertNotNull(list);
        assertTrue(list.size() > 0);
    }

    // Tests availableLocaleSet returns unmodifiable non-empty set
    @Test
    public void testAvailableLocaleSet_notEmpty() {
        Set set = LocaleUtils.availableLocaleSet();
        assertNotNull(set);
        assertTrue(set.size() > 0);
    }

    // Tests isAvailableLocale when checking known and unknown locales
    @Test
    public void testIsAvailableLocale_availableAndUnavailableLocales() {
        assertTrue(LocaleUtils.isAvailableLocale(Locale.US));
        assertFalse(LocaleUtils.isAvailableLocale(new Locale("xx", "YY")));
    }

    // Tests languagesByCountry with null input returns empty list
    @Test
    public void testLanguagesByCountry_null_returnsEmptyList() {
        List list = LocaleUtils.languagesByCountry(null);
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    // Tests languagesByCountry with valid country code
    @Test
    public void testLanguagesByCountry_validCountry_returnsLanguages() {
        List list = LocaleUtils.languagesByCountry("US");
        assertNotNull(list);
        assertTrue(list.contains(new Locale("en", "US")));
    }

    // Tests countriesByLanguage with null input returns empty list
    @Test
    public void testCountriesByLanguage_null_returnsEmptyList() {
        List list = LocaleUtils.countriesByLanguage(null);
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    // Tests countriesByLanguage with valid language code
    @Test
    public void testCountriesByLanguage_validLanguage_returnsCountries() {
        List list = LocaleUtils.countriesByLanguage("en");
        assertNotNull(list);
        assertTrue(list.contains(new Locale("en", "US")));
        assertTrue(list.contains(new Locale("en", "GB")));
    }
}