package org.apache.commons.lang;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link LocaleUtils}.
 */
public class LocaleUtilsTest {

    // Tests constructor instantiation
    @Test
    public void testConstructor_default_instantiatesSuccessfully() {
        assertNotNull(new LocaleUtils());
    }

    // Tests null input for toLocale
    @Test
    public void testToLocale_nullInput_returnsNull() {
        assertNull(LocaleUtils.toLocale(null));
    }

    // Tests valid 2-letter language code
    @Test
    public void testToLocale_languageOnly_returnsLocale() {
        Locale loc = LocaleUtils.toLocale("us");
        assertEquals("us", loc.getLanguage());
        assertEquals("", loc.getCountry());
        assertEquals("", loc.getVariant());
    }

    // Tests valid language and country code
    @Test
    public void testToLocale_languageAndCountry_returnsLocale() {
        Locale loc = LocaleUtils.toLocale("en_GB");
        assertEquals("en", loc.getLanguage());
        assertEquals("GB", loc.getCountry());
        assertEquals("", loc.getVariant());
    }

    // Tests valid language, country and variant
    @Test
    public void testToLocale_languageCountryVariant_returnsLocale() {
        Locale loc = LocaleUtils.toLocale("en_GB_xxx");
        assertEquals("en", loc.getLanguage());
        assertEquals("GB", loc.getCountry());
        assertEquals("xxx", loc.getVariant());
    }

    // Tests language and variant without country (Defects4J Lang-54 bug case)
    @Test
    public void testToLocale_languageAndVariantNoCountry_returnsLocale() {
        Locale loc = LocaleUtils.toLocale("fr__POSIX");
        assertEquals("fr", loc.getLanguage());
        assertEquals("", loc.getCountry());
        assertEquals("POSIX", loc.getVariant());
    }

    // Tests invalid length exception paths
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidLength1_throwsException() {
        LocaleUtils.toLocale("a");
    }

    // Tests invalid length exception paths
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidLength3_throwsException() {
        LocaleUtils.toLocale("eng");
    }

    // Tests invalid length exception paths
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidLength6_throwsException() {
        LocaleUtils.toLocale("en_GB_");
    }

    // Tests invalid language case exception paths
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidLanguageCase_throwsException() {
        LocaleUtils.toLocale("EN");
    }

    // Tests invalid separator between language and country
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidLanguageSeparator_throwsException() {
        LocaleUtils.toLocale("en-GB");
    }

    // Tests invalid country case exception paths
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidCountryCase_throwsException() {
        LocaleUtils.toLocale("en_gb");
    }

    // Tests invalid separator before variant
    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidVariantSeparator_throwsException() {
        LocaleUtils.toLocale("en_GB#xxx");
    }

    // Tests localeLookupList with null input
    @Test
    public void testLocaleLookupList_nullLocale_returnsEmptyList() {
        List list = LocaleUtils.localeLookupList(null);
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    // Tests localeLookupList hierarchy with language, country and variant
    @Test
    public void testLocaleLookupList_fullHierarchy_returnsAllLevels() {
        Locale locale = new Locale("fr", "CA", "xxx");
        List list = LocaleUtils.localeLookupList(locale);
        assertEquals(3, list.size());
        assertEquals(new Locale("fr", "CA", "xxx"), list.get(0));
        assertEquals(new Locale("fr", "CA"), list.get(1));
        assertEquals(new Locale("fr", ""), list.get(2));
    }

    // Tests localeLookupList with default locale parameter
    @Test
    public void testLocaleLookupList_withDefaultLocale_appendsDefaultWhenNotPresent() {
        Locale locale = new Locale("fr", "CA");
        Locale defaultLocale = new Locale("en", "US");
        List list = LocaleUtils.localeLookupList(locale, defaultLocale);
        assertEquals(3, list.size());
        assertEquals(new Locale("fr", "CA"), list.get(0));
        assertEquals(new Locale("fr", ""), list.get(1));
        assertEquals(new Locale("en", "US"), list.get(2));
    }

    // Tests availableLocaleList and availableLocaleSet
    @Test
    public void testAvailableLocaleListAndSet_normal_returnsAvailableLocales() {
        List list = LocaleUtils.availableLocaleList();
        assertNotNull(list);
        assertTrue(list.size() > 0);

        Set set = LocaleUtils.availableLocaleSet();
        assertNotNull(set);
        assertEquals(list.size(), set.size());
        assertTrue(LocaleUtils.isAvailableLocale(Locale.ENGLISH));
        assertFalse(LocaleUtils.isAvailableLocale(new Locale("xx", "YY")));
    }

    // Tests languagesByCountry for null and valid country
    @Test
    public void testLanguagesByCountry_nullAndValidCountry_returnsExpectedList() {
        List nullList = LocaleUtils.languagesByCountry(null);
        assertNotNull(nullList);
        assertEquals(0, nullList.size());

        List usList = LocaleUtils.languagesByCountry("US");
        assertNotNull(usList);
        assertTrue(usList.contains(new Locale("en", "US")));

        List cachedList = LocaleUtils.languagesByCountry("US");
        assertSame(usList, cachedList);
    }

    // Tests countriesByLanguage for null and valid language
    @Test
    public void testCountriesByLanguage_nullAndValidLanguage_returnsExpectedList() {
        List nullList = LocaleUtils.countriesByLanguage(null);
        assertNotNull(nullList);
        assertEquals(0, nullList.size());

        List enList = LocaleUtils.countriesByLanguage("en");
        assertNotNull(enList);
        assertTrue(enList.contains(new Locale("en", "US")));

        List cachedList = LocaleUtils.countriesByLanguage("en");
        assertSame(enList, cachedList);
    }
}