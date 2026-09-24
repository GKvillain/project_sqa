package org.apache.commons.codec.language.bm;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link Lang}.
 */
public class LangTest {

    private Lang langAshkenazi;
    private Lang langGeneric;
    private Lang langSephardic;

    @Before
    public void setUp() {
        this.langAshkenazi = Lang.instance(NameType.ASHKENAZI);
        this.langGeneric = Lang.instance(NameType.GENERIC);
        this.langSephardic = Lang.instance(NameType.SEPHARDIC);
    }

    // Tests static instance retrieval for all NameType values
    @Test
    public void testInstance_validNameTypes_returnsNonNullInstances() {
        assertNotNull(this.langAshkenazi);
        assertNotNull(this.langGeneric);
        assertNotNull(this.langSephardic);
    }

    // Tests guessLanguage returning a single matching language
    @Test
    public void testGuessLanguage_singletonMatch_returnsExactLanguage() {
        String lang = this.langGeneric.guessLanguage("yamamoto");
        assertEquals("japanese", lang);
    }

    // Tests guessLanguage when word matches multiple languages
    @Test
    public void testGuessLanguage_multipleMatches_returnsAny() {
        String lang = this.langGeneric.guessLanguage("martin");
        assertEquals(Languages.ANY, lang);
    }

    // Tests guessLanguage with German distinctive pattern
    @Test
    public void testGuessLanguage_germanPattern_returnsGerman() {
        String lang = this.langGeneric.guessLanguage("schmidt");
        assertEquals("german", lang);
    }

    // Tests guessLanguage with Russian/Cyrillic-transliterated pattern
    @Test
    public void testGuessLanguage_cyrillicPattern_returnsRussian() {
        String lang = this.langGeneric.guessLanguage("ivanov");
        assertEquals("russian", lang);
    }

    // Tests guessLanguages with case-insensitivity
    @Test
    public void testGuessLanguages_mixedCaseInput_returnsCorrectLanguageSet() {
        Languages.LanguageSet lsUpper = this.langGeneric.guessLanguages("YAMAMOTO");
        Languages.LanguageSet lsLower = this.langGeneric.guessLanguages("yamamoto");
        assertTrue(lsUpper.isSingleton());
        assertEquals("japanese", lsUpper.getAny());
        assertEquals(lsUpper.getAny(), lsLower.getAny());
    }

    // Tests guessLanguages returning multiple potential languages
    @Test
    public void testGuessLanguages_multiplePotentialLanguages_containsExpected() {
        Languages.LanguageSet ls = this.langGeneric.guessLanguages("garcia");
        assertFalse(ls.isSingleton());
        assertTrue(ls.isRestricted());
    }

    // Tests guessLanguages with Ashkenazi specific rules
    @Test
    public void testGuessLanguages_ashkenaziRules_returnsLanguages() {
        Languages.LanguageSet ls = this.langAshkenazi.guessLanguages("schneider");
        assertNotNull(ls);
        assertTrue(ls.isRestricted());
    }

    // Tests guessLanguages with Sephardic specific rules
    @Test
    public void testGuessLanguages_sephardicRules_returnsLanguages() {
        Languages.LanguageSet ls = this.langSephardic.guessLanguages("albuquerque");
        assertNotNull(ls);
        assertTrue(ls.isRestricted());
    }

    // Tests guessLanguages with empty text
    @Test
    public void testGuessLanguages_emptyString_returnsLanguageSet() {
        Languages.LanguageSet ls = this.langGeneric.guessLanguages("");
        assertNotNull(ls);
    }

    // Tests loadFromResource with valid existing resource
    @Test
    public void testLoadFromResource_validResource_loadsSuccessfully() {
        Languages languages = Languages.getInstance(NameType.GENERIC);
        Lang loaded = Lang.loadFromResource("org/apache/commons/codec/language/bm/lang.txt", languages);
        assertNotNull(loaded);
        assertEquals("japanese", loaded.guessLanguage("yamamoto"));
    }

    // Tests loadFromResource with non-existent resource name
    @Test(expected = IllegalStateException.class)
    public void testLoadFromResource_invalidResource_throwsIllegalStateException() {
        Languages languages = Languages.getInstance(NameType.GENERIC);
        Lang.loadFromResource("non/existent/path/lang.txt", languages);
    }

    // Tests guessLanguage with English word under generic
    @Test
    public void testGuessLanguage_englishWord_returnsEnglishOrAny() {
        Languages.LanguageSet ls = this.langGeneric.guessLanguages("smith");
        assertNotNull(ls);
        assertTrue(ls.isRestricted());
    }

    // Tests that Lang.instance returns cached singleton instances
    @Test
    public void testInstance_caching_returnsSameInstances() {
        assertSame(this.langAshkenazi, Lang.instance(NameType.ASHKENAZI));
        assertSame(this.langGeneric, Lang.instance(NameType.GENERIC));
        assertSame(this.langSephardic, Lang.instance(NameType.SEPHARDIC));
    }

    // Tests guessLanguage with Greek distinctive pattern
    @Test
    public void testGuessLanguage_greekPattern_returnsGreek() {
        String lang = this.langGeneric.guessLanguage("papadopoulos");
        assertEquals("greek", lang);
    }

    // Tests guessLanguage with French distinctive pattern
    @Test
    public void testGuessLanguage_frenchPattern_returnsFrench() {
        String lang = this.langGeneric.guessLanguage("dupont");
        assertEquals("french", lang);
    }

    // Tests guessLanguages when rule eliminations result in ANY_LANGUAGE fallback
    @Test
    public void testGuessLanguages_noMatchingRule_returnsAnyLanguage() {
        Languages.LanguageSet ls = this.langGeneric.guessLanguages("12345");
        assertNotNull(ls);
        assertFalse(ls.isSingleton());
    }

    // Tests guessLanguages with Sephardic distinctive pattern
    @Test
    public void testGuessLanguage_sephardicDistinctive_returnsLanguage() {
        String lang = this.langSephardic.guessLanguage("cohen");
        assertNotNull(lang);
    }

    // Tests guessLanguage with Ashkenazi distinctive pattern returning singleton
    @Test
    public void testGuessLanguage_ashkenaziGerman_returnsGerman() {
        String lang = this.langAshkenazi.guessLanguage("schneider");
        assertEquals("german", lang);
    }
}