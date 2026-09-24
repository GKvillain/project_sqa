package org.apache.commons.lang3;

import org.junit.Test;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link LocaleUtils}.
 */
public class LocaleUtilsTest {

    // Tests constructor execution
    @Test
    public void testConstructor_default_instantiatesSuccessfully() {
        assertNotNull(new LocaleUtils());
    }

    // Tests null input for toLocale
    @Test
    public void testToLocale_nullInput_returnsNull() {
        assertNull(LocaleUtils.toLocale(null));
    }

    // Tests language only input for toLocale
    @Test
    public void testToLocale_languageOnly_returnsLocale() {
        final Locale locale = LocaleUtils.toLocale("en");
        assertNotNull(locale);
        assertEquals("en", locale.getLanguage());
        assertEquals("", locale.getCountry());
        assertEquals("", locale.getVariant());
    }

    // Tests language and country input for toLocale
    @Test
    public void testToLocale_languageAndCountry_returnsLocale() {
        final Locale locale = LocaleUtils.toLocale("en_US");
        assertNotNull(locale);
        assertEquals("en", locale.getLanguage());
        assertEquals("US", locale.getCountry());
        assertEquals("", locale.getVariant());
    }

    // Tests language, country, and variant input for toLocale
    @Test
    public void testToLocale_languageCountryAndVariant_returnsLocale() {
        final Locale locale = LocaleUtils.toLocale("en_US_WIN");
        assertNotNull(locale);
        assertEquals("en", locale.getLanguage());
        assertEquals("US", locale.getCountry());
        assertEquals("WIN", locale.getVariant());
    }

    // Tests language with variant only (empty country) for toLocale
    @Test
    public void testToLocale_languageAndVariantOnly_returnsLocale() {
        final Locale locale = LocaleUtils.toLocale("en__POSIX");
        assertNotNull(locale);
        assertEquals("en", locale.getLanguage());
        assertEquals("", locale.getCountry());
        assertEquals("POSIX", locale.getVariant());
    }

    // Tests toLocale with invalid length less than 2
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_lengthLessThanTwo_throwsException() {
        LocaleUtils.toLocale("a");
    }

    // Tests toLocale with uppercase character in language code
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidLanguageCase_throwsException() {
        LocaleUtils.toLocale("En");
    }

    // Tests toLocale with length between 2 and 5
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidLengthBetweenTwoAndFive_throwsException() {
        LocaleUtils.toLocale("en_U");
    }

    // Tests toLocale with invalid separator at position 2
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidSeparatorAtTwo_throwsException() {
        LocaleUtils.toLocale("en-US");
    }

    // Tests toLocale with lowercase character in country code
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidCountryCase_throwsException() {
        LocaleUtils.toLocale("en_us");
    }

    // Tests toLocale with invalid length between 5 and 7
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidLengthBetweenFiveAndSeven_throwsException() {
        LocaleUtils.toLocale("en_US_");
    }

    // Tests toLocale with invalid separator at position 5
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidSeparatorAtFive_throwsException() {
        LocaleUtils.toLocale("en_US-WIN");
    }

    // Tests localeLookupList with null input
    @Test
    public void testLocaleLookupList_nullLocale_returnsEmptyList() {
        final List<Locale> list = LocaleUtils.localeLookupList(null);
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    // Tests localeLookupList with full language, country, variant and fallback default
    @Test
    public void testLocaleLookupList_fullLocaleWithDefault_returnsHierarchy() {
        final Locale locale = new Locale("fr", "CA", "xxx");
        final Locale defaultLocale = new Locale("en");
        final List<Locale> list = LocaleUtils.localeLookupList(locale, defaultLocale);

        assertEquals(4, list.size());
        assertEquals(locale, list.get(0));
        assertEquals(new Locale("fr", "CA"), list.get(1));
        assertEquals(new Locale("fr", ""), list.get(2));
        assertEquals(defaultLocale, list.get(3));
    }

    // Tests localeLookupList when default locale is already in hierarchy
    @Test
    public void testLocaleLookupList_defaultAlreadyPresent_doesNotDuplicate() {
        final Locale locale = new Locale("fr", "CA");
        final Locale defaultLocale = new Locale("fr");
        final List<Locale> list = LocaleUtils.localeLookupList(locale, defaultLocale);

        assertEquals(2, list.size());
        assertEquals(locale, list.get(0));
        assertEquals(new Locale("fr", ""), list.get(1));
    }

    // Tests availableLocaleList and availableLocaleSet
    @Test
    public void testAvailableLocales_returnsValidCollections() {
        final List<Locale> list = LocaleUtils.availableLocaleList();
        final Set<Locale> set = LocaleUtils.availableLocaleSet();

        assertNotNull(list);
        assertNotNull(set);
        assertFalse(list.isEmpty());
        assertEquals(list.size(), set.size());
        assertTrue(LocaleUtils.isAvailableLocale(Locale.ENGLISH));
        assertFalse(LocaleUtils.isAvailableLocale(new Locale("xx", "YY")));
    }

    // Tests languagesByCountry with null and valid country
    @Test
    public void testLanguagesByCountry_nullAndValid_returnsExpected() {
        assertTrue(LocaleUtils.languagesByCountry(null).isEmpty());

        final List<Locale> usLocales = LocaleUtils.languagesByCountry("US");
        assertNotNull(usLocales);
        assertFalse(usLocales.isEmpty());
        for (Locale loc : usLocales) {
            assertEquals("US", loc.getCountry());
            assertTrue(loc.getVariant().isEmpty());
        }

        // Test caching mechanism
        final List<Locale> cachedUsLocales = LocaleUtils.languagesByCountry("US");
        assertEquals(usLocales, cachedUsLocales);
    }

    // Tests countriesByLanguage with null and valid language
    @Test
    public void testCountriesByLanguage_nullAndValid_returnsExpected() {
        assertTrue(LocaleUtils.countriesByLanguage(null).isEmpty());

        final List<Locale> enLocales = LocaleUtils.countriesByLanguage("en");
        assertNotNull(enLocales);
        assertFalse(enLocales.isEmpty());
        for (Locale loc : enLocales) {
            assertEquals("en", loc.getLanguage());
            assertFalse(loc.getCountry().isEmpty());
            assertTrue(loc.getVariant().isEmpty());
        }

        // Test caching mechanism
        final List<Locale> cachedEnLocales = LocaleUtils.countriesByLanguage("en");
        assertEquals(enLocales, cachedEnLocales);
    }
}