package org.apache.commons.codec.language.bm;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link PhoneticEngine}.
 */
public class PhoneticEngineTest {

    // Tests constructor exception when RuleType.RULES is passed
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ruleTypeRules_throwsIllegalArgumentException() {
        new PhoneticEngine(NameType.GENERIC, RuleType.RULES, true);
    }

    // Tests constructor with maxPhonemes and exception when RuleType.RULES is passed
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_withMaxPhonemes_ruleTypeRules_throwsIllegalArgumentException() {
        new PhoneticEngine(NameType.ASHKENAZI, RuleType.RULES, false, 15);
    }

    // Tests getter methods and default max phonemes initialization
    @Test
    public void testGetters_defaultInitialization_returnsCorrectValues() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true);

        assertEquals(NameType.GENERIC, engine.getNameType());
        assertEquals(RuleType.APPROX, engine.getRuleType());
        assertTrue(engine.isConcat());
        assertEquals(20, engine.getMaxPhonemes());
        assertNotNull(engine.getLang());
    }

    // Tests custom max phonemes and isConcat flag set to false
    @Test
    public void testGetters_customInitialization_returnsCorrectValues() {
        PhoneticEngine engine = new PhoneticEngine(NameType.SEPHARDIC, RuleType.EXACT, false, 10);

        assertEquals(NameType.SEPHARDIC, engine.getNameType());
        assertEquals(RuleType.EXACT, engine.getRuleType());
        assertFalse(engine.isConcat());
        assertEquals(10, engine.getMaxPhonemes());
        assertNotNull(engine.getLang());
    }

    // Tests encoding of empty string
    @Test
    public void testEncode_emptyString_returnsEmptyString() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true);
        String result = engine.encode("");
        assertEquals("", result);
    }

    // Tests encoding with basic input using APPROX rule type
    @Test
    public void testEncode_genericApproxSingleWord_returnsPhonemes() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true);
        String result = engine.encode("smith");
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    // Tests encoding with EXACT rule type
    @Test
    public void testEncode_genericExactSingleWord_returnsPhonemes() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.EXACT, true);
        String result = engine.encode("smith");
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    // Tests generic name type with "d'" prefix branch
    @Test
    public void testEncode_genericWithDApostrophePrefix_branchesCorrectly() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true);
        String result = engine.encode("d'angelo");
        assertTrue(result.startsWith("("));
        assertTrue(result.contains(")-("));
        assertTrue(result.endsWith(")"));
    }

    // Tests generic name type with space-separated prefix (e.g., "van ")
    @Test
    public void testEncode_genericWithSpacePrefix_branchesCorrectly() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true);
        String result = engine.encode("van halen");
        assertTrue(result.startsWith("("));
        assertTrue(result.contains(")-("));
        assertTrue(result.endsWith(")"));
    }

    // Tests Sephardic name type with apostrophe prefix removal
    @Test
    public void testEncode_sephardicWithApostrophe_stripsPrefix() {
        PhoneticEngine engine = new PhoneticEngine(NameType.SEPHARDIC, RuleType.APPROX, true);
        String resultWithPrefix = engine.encode("d'almeida");
        String resultWithoutPrefix = engine.encode("almeida");
        assertEquals(resultWithoutPrefix, resultWithPrefix);
    }

    // Tests Ashkenazi name type prefix removal
    @Test
    public void testEncode_ashkenaziWithPrefix_stripsPrefix() {
        PhoneticEngine engine = new PhoneticEngine(NameType.ASHKENAZI, RuleType.APPROX, true);
        String resultWithPrefix = engine.encode("ben cohen");
        String resultWithoutPrefix = engine.encode("cohen");
        assertEquals(resultWithoutPrefix, resultWithPrefix);
    }

    // Tests multi-word name with concat = false
    @Test
    public void testEncode_multiWordConcatFalse_formatsWithHyphen() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, false);
        String result = engine.encode("john smith");
        assertTrue(result.contains("-"));
    }

    // Tests multi-word name with concat = true
    @Test
    public void testEncode_multiWordConcatTrue_concatenatesWords() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true);
        String result = engine.encode("john smith");
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    // Tests dashed input treated as space
    @Test
    public void testEncode_dashedInput_equalsSpacedInput() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true);
        String dashed = engine.encode("jean-pierre");
        String spaced = engine.encode("jean pierre");
        assertEquals(spaced, dashed);
    }

    // Tests encoding with explicit language set
    @Test
    public void testEncode_explicitLanguageSet_returnsPhonemes() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true);
        Languages.LanguageSet languageSet = Languages.LanguageSet.from(Collections.singleton("english"));
        String result = engine.encode("smith", languageSet);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    // Tests max phonemes limit restricts phoneme count
    @Test
    public void testEncode_maxPhonemesLimit_restrictsOutputPhonemes() {
        PhoneticEngine engineLimit1 = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true, 1);
        String result1 = engineLimit1.encode("alexander");
        assertFalse(result1.contains("|"));

        PhoneticEngine engineLimit5 = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true, 5);
        String result5 = engineLimit5.encode("alexander");
        int count = result5.split("\\|").length;
        assertTrue(count <= 5);
    }

    // Tests non-matching or unhandled character sequences
    @Test
    public void testEncode_digitsAndPunctuation_handledWithoutError() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true);
        String result = engine.encode("12345");
        assertNotNull(result);
    }

    // Tests Sephardic name type with space prefix removal
    @Test
    public void testEncode_sephardicWithSpacePrefix_stripsPrefix() {
        PhoneticEngine engine = new PhoneticEngine(NameType.SEPHARDIC, RuleType.APPROX, true);
        String resultWithPrefix = engine.encode("de silva");
        String resultWithoutPrefix = engine.encode("silva");
        assertEquals(resultWithoutPrefix, resultWithPrefix);
    }

    // Tests Ashkenazi name type with space prefix removal (e.g., "von ")
    @Test
    public void testEncode_ashkenaziWithSpacePrefix_stripsPrefix() {
        PhoneticEngine engine = new PhoneticEngine(NameType.ASHKENAZI, RuleType.APPROX, true);
        String resultWithPrefix = engine.encode("von bismarck");
        String resultWithoutPrefix = engine.encode("bismarck");
        assertEquals(resultWithoutPrefix, resultWithPrefix);
    }

    // Tests generic name type with "d'" prefix and concat = false
    @Test
    public void testEncode_genericWithDApostrophePrefixConcatFalse_branchesCorrectly() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, false);
        String result = engine.encode("d'angelo");
        assertTrue(result.startsWith("("));
        assertTrue(result.contains(")-("));
        assertTrue(result.endsWith(")"));
    }

    // Tests generic name type with space-separated prefix and concat = false
    @Test
    public void testEncode_genericWithSpacePrefixConcatFalse_branchesCorrectly() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, false);
        String result = engine.encode("van halen");
        assertTrue(result.startsWith("("));
        assertTrue(result.contains(")-("));
        assertTrue(result.endsWith(")"));
    }

    // Tests whitespace or dash only input strings
    @Test
    public void testEncode_whitespaceOrDashesOnly_returnsEmptyString() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true);
        assertEquals("", engine.encode("   "));
        assertEquals("", engine.encode("-"));
        assertEquals("", engine.encode("  -  "));
    }

    // Tests Sephardic multi-word encoding with concat = false
    @Test
    public void testEncode_sephardicMultiWordConcatFalse_formatsWithHyphen() {
        PhoneticEngine engine = new PhoneticEngine(NameType.SEPHARDIC, RuleType.APPROX, false);
        String result = engine.encode("garcia lopez");
        assertTrue(result.contains("-"));
    }

    // Tests Ashkenazi multi-word encoding with concat = false
    @Test
    public void testEncode_ashkenaziMultiWordConcatFalse_formatsWithHyphen() {
        PhoneticEngine engine = new PhoneticEngine(NameType.ASHKENAZI, RuleType.APPROX, false);
        String result = engine.encode("cohen rosen");
        assertTrue(result.contains("-"));
    }

    // Tests encoding with ANY_LANGUAGE language set
    @Test
    public void testEncode_anyLanguageSet_returnsPhonemes() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true);
        String result = engine.encode("smith", Languages.ANY_LANGUAGE);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    // Tests encoding with NO_LANGUAGES language set
    @Test
    public void testEncode_noLanguagesSet_returnsPhonemes() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true);
        String result = engine.encode("smith", Languages.NO_LANGUAGES);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}