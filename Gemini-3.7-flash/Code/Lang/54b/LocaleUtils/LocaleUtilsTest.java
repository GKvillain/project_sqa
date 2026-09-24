package org.apache.commons.lang;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link LocaleUtils}.
 */
public class LocaleUtilsTest {

    // Tests constructor creation for JavaBean compatibility
    @Test
    public void testConstructor_default_createsInstance() {
        assertNotNull(new LocaleUtils());
    }

    // Tests toLocale with null input
    @Test
    public void testToLocale_nullInput_returnsNull() {
        assertNull(LocaleUtils.toLocale(null));
    }

    // Tests toLocale with valid 2-letter language
    @Test
    public void testToLocale_languageOnly_returnsLocale() {
        assertEquals(new Locale("en", ""), LocaleUtils.toLocale("en"));
        assertEquals(new Locale("fr", ""), LocaleUtils.toLocale("fr"));
    }

    // Tests toLocale with valid 2-letter language and 2-letter country
    @Test
    public void testToLocale_languageAndCountry_returnsLocale() {
        assertEquals(new Locale("en", "GB"), LocaleUtils.toLocale("en_GB"));
        assertEquals(new Locale("fr", "CA"), LocaleUtils.toLocale("fr_CA"));
    }

    // Tests toLocale with valid language, country and variant
    @Test
    public void testToLocale_languageCountryAndVariant_returnsLocale() {
        assertEquals(new Locale("en", "GB", "xxx"), LocaleUtils.toLocale("en_GB_xxx"));
        assertEquals(new Locale("fr", "CA", "POSIX"), LocaleUtils.toLocale("fr_CA_POSIX"));
    }

    // Tests toLocale with language and variant without country (Defects4J Lang-54)
    @Test
    public void testToLocale_languageAndVariantWithoutCountry_returnsLocale() {
        assertEquals(new Locale("fr", "", "POSIX"), LocaleUtils.toLocale("fr__POSIX"));
        assertEquals(new Locale("de", "", "POSIX"), LocaleUtils.toLocale("de__POSIX"));
    }

    // Tests toLocale with invalid lengths
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidLength1Char_throwsException() {
        LocaleUtils.toLocale("u");
    }

    // Tests toLocale with invalid length of 3 characters
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidLength3Chars_throwsException() {
        LocaleUtils.toLocale("eng");
    }

    // Tests toLocale with invalid length of 4 characters
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidLength4Chars_throwsException() {
        LocaleUtils.toLocale("en_U");
    }

    // Tests toLocale with invalid length of 6 characters
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidLength6Chars_throwsException() {
        LocaleUtils.toLocale("en_GB_");
    }

    // Tests toLocale with invalid uppercase language code
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_uppercaseLanguage_throwsException() {
        LocaleUtils.toLocale("EN");
    }

    // Tests toLocale with invalid lowercase country code
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_lowercaseCountry_throwsException() {
        LocaleUtils.toLocale("en_gb");
    }

    // Tests toLocale with invalid separator
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidSeparator_throwsException() {
        LocaleUtils.toLocale("en-GB");
    }

    // Tests localeLookupList with null input
    @Test
    public void testLocaleLookupList_nullLocale_returnsEmptyList() {
        List list = LocaleUtils.localeLookupList(null);
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    // Tests localeLookupList with language, country and variant
    @Test
    public void testLocaleLookupList_fullLocale_returnsHierarchicalList() {
        Locale locale = new Locale("fr", "CA", "xxx");
        List expected = Arrays.asList(
            new Locale("fr", "CA", "xxx"),
            new Locale("fr", "CA"),
            new Locale("fr", "")
        );
        List actual = LocaleUtils.localeLookupList(locale);
        assertEquals(expected, actual);
    }

    // Tests localeLookupList with explicit default locale
    @Test
    public void testLocaleLookupList_withDefaultLocale_appendsDefault() {
        Locale locale = new Locale("fr", "CA");
        Locale defaultLocale = new Locale("en", "US");
        List expected = Arrays.asList(
            new Locale("fr", "CA"),
            new Locale("fr", ""),
            new Locale("en", "US")
        );
        List actual = LocaleUtils.localeLookupList(locale, defaultLocale);
        assertEquals(expected, actual);
    }

    // Tests availableLocaleList and availableLocaleSet
    @Test
    public void testAvailableLocaleListAndSet_validLocales_returnsCollections() {
        List list = LocaleUtils.availableLocaleList();
        Set set = LocaleUtils.availableLocaleSet();
        assertNotNull(list);
        assertNotNull(set);
        assertTrue(list.size() > 0);
        assertEquals(list.size(), set.size());
        assertTrue(LocaleUtils.isAvailableLocale(Locale.ENGLISH));
        assertFalse(LocaleUtils.isAvailableLocale(new Locale("nonexistent", "XX")));
    }

    // Tests languagesByCountry with valid and null country code
    @Test
    public void testLanguagesByCountry_validAndNullCountry_returnsLanguages() {
        List usLanguages = LocaleUtils.languagesByCountry("US");
        assertNotNull(usLanguages);
        assertTrue(usLanguages.contains(new Locale("en", "US")));

        List nullLanguages = LocaleUtils.languagesByCountry(null);
        assertNotNull(nullLanguages);
        assertEquals(0, nullLanguages.size());
    }

    // Tests countriesByLanguage with valid and null language code
    @Test
    public void testCountriesByLanguage_validAndNullLanguage_returnsCountries() {
        List enCountries = LocaleUtils.countriesByLanguage("en");
        assertNotNull(enCountries);
        assertTrue(enCountries.contains(new Locale("en", "US")));
        assertTrue(enCountries.contains(new Locale("en", "GB")));

        List nullCountries = LocaleUtils.countriesByLanguage(null);
        assertNotNull(nullCountries);
        assertEquals(0, nullCountries.size());
    }
}