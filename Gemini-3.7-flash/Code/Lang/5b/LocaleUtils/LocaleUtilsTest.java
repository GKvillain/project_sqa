package org.apache.commons.lang3;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
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

    private static final Locale LOCALE_EN = new Locale("en", "");
    private static final Locale LOCALE_EN_US = new Locale("en", "US");
    private static final Locale LOCALE_EN_US_WIN = new Locale("en", "US", "WIN");
    private static final Locale LOCALE_FR = new Locale("fr", "");

    // Tests public constructor
    @Test
    public void testConstructor_isPublic() throws Exception {
        assertNotNull(new LocaleUtils());
        Constructor<LocaleUtils> constructor = LocaleUtils.class.getConstructor();
        assertTrue(Modifier.isPublic(constructor.getModifiers()));
    }

    // Tests null string input returns null
    @Test
    public void testToLocale_nullInput_returnsNull() {
        assertNull(LocaleUtils.toLocale(null));
    }

    // Tests language only valid conversion
    @Test
    public void testToLocale_languageOnly_returnsLocale() {
        assertEquals(new Locale("us"), LocaleUtils.toLocale("us"));
        assertEquals(new Locale("fr"), LocaleUtils.toLocale("fr"));
        assertEquals(new Locale("de"), LocaleUtils.toLocale("de"));
    }

    // Tests language and country valid conversion
    @Test
    public void testToLocale_languageAndCountry_returnsLocale() {
        assertEquals(LOCALE_EN_US, LocaleUtils.toLocale("en_US"));
        assertEquals(new Locale("fr", "CA"), LocaleUtils.toLocale("fr_CA"));
    }

    // Tests language, country and variant valid conversion
    @Test
    public void testToLocale_languageCountryVariant_returnsLocale() {
        assertEquals(LOCALE_EN_US_WIN, LocaleUtils.toLocale("en_US_WIN"));
    }

    // Tests language and variant with empty country
    @Test
    public void testToLocale_languageAndVariantOnly_returnsLocale() {
        assertEquals(new Locale("en", "", "POSIX"), LocaleUtils.toLocale("en__POSIX"));
    }

    // Tests locales starting with underscore (Defects4J Lang-5 bug)
    @Test
    public void testToLocale_startsUnderscore_returnsLocale() {
        assertEquals(new Locale("", "GB"), LocaleUtils.toLocale("_GB"));
        assertEquals(new Locale("", "GB", "xxx"), LocaleUtils.toLocale("_GB_xxx"));
        assertEquals(new Locale("", "", "POSIX"), LocaleUtils.toLocale("___POSIX"));
    }

    // Tests invalid length less than 2
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_lengthLessThan2_throwsException() {
        LocaleUtils.toLocale("u");
    }

    // Tests invalid uppercase in language code
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_uppercaseLanguage_throwsException() {
        LocaleUtils.toLocale("Us_US");
    }

    // Tests invalid length between 2 and 5
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidLength3_throwsException() {
        LocaleUtils.toLocale("usa");
    }

    // Tests missing separator at index 2
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_missingSeparatorAt2_throwsException() {
        LocaleUtils.toLocale("en#US");
    }

    // Tests lowercase country code
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_lowercaseCountry_throwsException() {
        LocaleUtils.toLocale("en_us");
    }

    // Tests invalid length 6 for country and variant
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidLength6_throwsException() {
        LocaleUtils.toLocale("en_US_");
    }

    // Tests missing separator at index 5
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_missingSeparatorAt5_throwsException() {
        LocaleUtils.toLocale("en_US#WIN");
    }

    // Tests locale lookup list single parameter
    @Test
    public void testLocaleLookupList_singleParam_returnsHierarchicalList() {
        List<Locale> list = LocaleUtils.localeLookupList(LOCALE_EN_US_WIN);
        assertEquals(3, list.size());
        assertEquals(LOCALE_EN_US_WIN, list.get(0));
        assertEquals(LOCALE_EN_US, list.get(1));
        assertEquals(LOCALE_EN, list.get(2));
    }

    // Tests locale lookup list with default locale fallback
    @Test
    public void testLocaleLookupList_withDefaultLocale_containsDefault() {
        List<Locale> list = LocaleUtils.localeLookupList(LOCALE_EN_US, LOCALE_FR);
        assertEquals(3, list.size());
        assertEquals(LOCALE_EN_US, list.get(0));
        assertEquals(LOCALE_EN, list.get(1));
        assertEquals(LOCALE_FR, list.get(2));
    }

    // Tests locale lookup list with null input
    @Test
    public void testLocaleLookupList_nullLocale_returnsEmptyList() {
        List<Locale> list = LocaleUtils.localeLookupList(null, LOCALE_EN);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    // Tests available locales list and set
    @Test
    public void testAvailableLocaleListAndSet_valid_returnsLocales() {
        List<Locale> list = LocaleUtils.availableLocaleList();
        Set<Locale> set = LocaleUtils.availableLocaleSet();
        assertNotNull(list);
        assertNotNull(set);
        assertFalse(list.isEmpty());
        assertEquals(list.size(), set.size());
    }

    // Tests isAvailableLocale method
    @Test
    public void testIsAvailableLocale_existingAndNonExisting_returnsCorrectBoolean() {
        assertTrue(LocaleUtils.isAvailableLocale(Locale.US));
        assertFalse(LocaleUtils.isAvailableLocale(new Locale("xx", "YY")));
    }

    // Tests languagesByCountry method and caching
    @Test
    public void testLanguagesByCountry_validAndNull_returnsExpected() {
        assertTrue(LocaleUtils.languagesByCountry(null).isEmpty());
        List<Locale> usLangs = LocaleUtils.languagesByCountry("US");
        assertNotNull(usLangs);
        assertFalse(usLangs.isEmpty());
        assertTrue(usLangs.contains(new Locale("en", "US")));

        // Cached lookup
        List<Locale> usLangsCached = LocaleUtils.languagesByCountry("US");
        assertEquals(usLangs, usLangsCached);
    }

    // Tests countriesByLanguage method and caching
    @Test
    public void testCountriesByLanguage_validAndNull_returnsExpected() {
        assertTrue(LocaleUtils.countriesByLanguage(null).isEmpty());
        List<Locale> enCountries = LocaleUtils.countriesByLanguage("en");
        assertNotNull(enCountries);
        assertFalse(enCountries.isEmpty());
        assertTrue(enCountries.contains(new Locale("en", "US")));

        // Cached lookup
        List<Locale> enCountriesCached = LocaleUtils.countriesByLanguage("en");
        assertEquals(enCountries, enCountriesCached);
    }
}