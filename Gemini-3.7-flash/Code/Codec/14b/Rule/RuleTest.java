package org.apache.commons.codec.language.bm;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RuleTest {

    // Tests Phoneme constructor, getters, and toString
    @Test
    public void testPhoneme_basicProperties_returnsCorrectValues() {
        Languages.LanguageSet langs = Languages.LanguageSet.from(new HashSet<String>(Arrays.asList("french", "spanish")));
        Rule.Phoneme p = new Rule.Phoneme("abc", langs);

        assertEquals("abc", p.getPhonemeText().toString());
        assertEquals(langs, p.getLanguages());
        assertEquals("abc[french, spanish]", p.toString());
        assertTrue(p.getPhonemes().iterator().hasNext());
        assertEquals(p, p.getPhonemes().iterator().next());
    }

    // Tests Phoneme combination constructors and append
    @Test
    public void testPhoneme_combinationAndAppend_returnsCombinedPhoneme() {
        Languages.LanguageSet langs1 = Languages.LanguageSet.from(new HashSet<String>(Arrays.asList("german")));
        Languages.LanguageSet langs2 = Languages.LanguageSet.from(new HashSet<String>(Arrays.asList("english")));

        Rule.Phoneme p1 = new Rule.Phoneme("foo", langs1);
        Rule.Phoneme p2 = new Rule.Phoneme("bar", langs2);

        Rule.Phoneme combined = new Rule.Phoneme(p1, p2);
        assertEquals("foobar", combined.getPhonemeText().toString());
        assertEquals(langs1, combined.getLanguages());

        Rule.Phoneme combinedWithLangs = new Rule.Phoneme(p1, p2, langs2);
        assertEquals("foobar", combinedWithLangs.getPhonemeText().toString());
        assertEquals(langs2, combinedWithLangs.getLanguages());

        p1.append("baz");
        assertEquals("foobaz", p1.getPhonemeText().toString());
    }

    // Tests Phoneme join method
    @Test
    public void testPhoneme_join_returnsJoinedPhonemeWithRestrictedLanguages() {
        Languages.LanguageSet langs1 = Languages.LanguageSet.from(new HashSet<String>(Arrays.asList("french", "spanish")));
        Languages.LanguageSet langs2 = Languages.LanguageSet.from(new HashSet<String>(Arrays.asList("spanish", "german")));

        Rule.Phoneme p1 = new Rule.Phoneme("hello", langs1);
        Rule.Phoneme p2 = new Rule.Phoneme("world", langs2);

        Rule.Phoneme joined = p1.join(p2);
        assertEquals("helloworld", joined.getPhonemeText().toString());
        Set<String> expectedLangs = new HashSet<String>(Arrays.asList("spanish"));
        assertEquals(Languages.LanguageSet.from(expectedLangs), joined.getLanguages());
    }

    // Tests Phoneme Comparator with equal, shorter, longer, and different text
    @Test
    public void testPhonemeComparator_variousComparisons_returnsCorrectOrder() {
        Languages.LanguageSet any = Languages.ANY_LANGUAGE;
        Rule.Phoneme a = new Rule.Phoneme("a", any);
        Rule.Phoneme b = new Rule.Phoneme("b", any);
        Rule.Phoneme aa = new Rule.Phoneme("aa", any);
        Rule.Phoneme a2 = new Rule.Phoneme("a", any);

        assertTrue(Rule.Phoneme.COMPARATOR.compare(a, b) < 0);
        assertTrue(Rule.Phoneme.COMPARATOR.compare(b, a) > 0);
        assertTrue(Rule.Phoneme.COMPARATOR.compare(a, aa) < 0);
        assertTrue(Rule.Phoneme.COMPARATOR.compare(aa, a) > 0);
        assertEquals(0, Rule.Phoneme.COMPARATOR.compare(a, a2));
    }

    // Tests PhonemeList implementation of PhonemeExpr
    @Test
    public void testPhonemeList_getPhonemes_returnsList() {
        Rule.Phoneme p1 = new Rule.Phoneme("one", Languages.ANY_LANGUAGE);
        Rule.Phoneme p2 = new Rule.Phoneme("two", Languages.ANY_LANGUAGE);
        List<Rule.Phoneme> list = Arrays.asList(p1, p2);

        Rule.PhonemeList pList = new Rule.PhonemeList(list);
        assertEquals(list, pList.getPhonemes());
    }

    // Tests ALL_STRINGS_RMATCHER
    @Test
    public void testAllStringsRMatcher_isMatch_returnsTrue() {
        assertTrue(Rule.ALL_STRINGS_RMATCHER.isMatch("test"));
        assertTrue(Rule.ALL_STRINGS_RMATCHER.isMatch(""));
    }

    // Tests Rule pattern matching with exact match contexts
    @Test
    public void testPatternAndContextMatches_exactMatchContexts_returnsTrueAndFalse() {
        Rule.Phoneme ph = new Rule.Phoneme("out", Languages.ANY_LANGUAGE);
        // lContext matches input up to index ending in 'a'
        // rContext matches from index+patternLength starting with 'c'
        Rule rule = new Rule("b", "a", "c", ph);

        assertEquals("b", rule.getPattern());
        assertEquals(ph, rule.getPhoneme());
        assertNotNull(rule.getLContext());
        assertNotNull(rule.getRContext());

        assertTrue(rule.patternAndContextMatches("abc", 1));
        assertFalse(rule.patternAndContextMatches("xbc", 1));
        assertFalse(rule.patternAndContextMatches("abx", 1));
        assertFalse(rule.patternAndContextMatches("axc", 1));
    }

    // Tests Rule pattern matching with boxed character contexts [chars] and [^chars]
    @Test
    public void testPatternAndContextMatches_boxedContexts_matchesCorrectly() {
        Rule.Phoneme ph = new Rule.Phoneme("out", Languages.ANY_LANGUAGE);
        Rule rule = new Rule("b", "[aeiou]", "[^xyz]", ph);

        assertTrue(rule.patternAndContextMatches("ebw", 1));
        assertFalse(rule.patternAndContextMatches("xbw", 1));
        assertFalse(rule.patternAndContextMatches("ebx", 1));
    }

    // Tests patternAndContextMatches with negative index throws Exception
    @Test(expected = IndexOutOfBoundsException.class)
    public void testPatternAndContextMatches_negativeIndex_throwsException() {
        Rule.Phoneme ph = new Rule.Phoneme("out", Languages.ANY_LANGUAGE);
        Rule rule = new Rule("test", "", "", ph);
        rule.patternAndContextMatches("test", -1);
    }

    // Tests patternAndContextMatches when input is too short for pattern
    @Test
    public void testPatternAndContextMatches_indexPlusPatternTooLong_returnsFalse() {
        Rule.Phoneme ph = new Rule.Phoneme("out", Languages.ANY_LANGUAGE);
        Rule rule = new Rule("test", "", "", ph);
        assertFalse(rule.patternAndContextMatches("te", 0));
        assertFalse(rule.patternAndContextMatches("test", 1));
    }

    // Tests getInstance with NameType, RuleType, and single language String
    @Test
    public void testGetInstance_withLanguageString_returnsRules() {
        List<Rule> rules = Rule.getInstance(NameType.GENERIC, RuleType.EXACT, "english");
        assertNotNull(rules);
        assertFalse(rules.isEmpty());
    }

    // Tests getInstance with NameType, RuleType, and LanguageSet
    @Test
    public void testGetInstance_withLanguageSet_returnsRules() {
        Languages.LanguageSet langSet = Languages.LanguageSet.from(new HashSet<String>(Arrays.asList("french")));
        List<Rule> rules = Rule.getInstance(NameType.GENERIC, RuleType.APPROX, langSet);
        assertNotNull(rules);
        assertFalse(rules.isEmpty());
    }

    // Tests getInstanceMap for singleton language set vs ANY language set
    @Test
    public void testGetInstanceMap_singletonAndAny_returnsMap() {
        Languages.LanguageSet singleLang = Languages.LanguageSet.from(new HashSet<String>(Arrays.asList("spanish")));
        Map<String, List<Rule>> mapSingle = Rule.getInstanceMap(NameType.ASHKENAZI, RuleType.RULES, singleLang);
        assertNotNull(mapSingle);
        assertFalse(mapSingle.isEmpty());

        Map<String, List<Rule>> mapAny = Rule.getInstanceMap(NameType.ASHKENAZI, RuleType.RULES, Languages.ANY_LANGUAGE);
        assertNotNull(mapAny);
        assertFalse(mapAny.isEmpty());
    }

    // Tests getInstanceMap with invalid language throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetInstanceMap_invalidLanguage_throwsIllegalArgumentException() {
        Rule.getInstanceMap(NameType.GENERIC, RuleType.EXACT, "nonExistentLanguage");
    }

    // Tests loaded rule toString output
    @Test
    public void testRule_toString_containsDetails() {
        List<Rule> rules = Rule.getInstance(NameType.GENERIC, RuleType.RULES, "common");
        assertNotNull(rules);
        assertFalse(rules.isEmpty());
        Rule r = rules.get(0);
        String str = r.toString();
        assertTrue(str.contains("Rule"));
        assertTrue(str.contains("pat="));
        assertTrue(str.contains("lcon="));
        assertTrue(str.contains("rcon="));
    }

    // Tests pattern matching with start anchor '^' in lContext and end anchor '$' in rContext
    @Test
    public void testPatternAndContextMatches_startAndEndAnchors_matchesCorrectly() {
        Rule.Phoneme ph = new Rule.Phoneme("out", Languages.ANY_LANGUAGE);
        Rule rule = new Rule("b", "^a", "c$", ph);

        assertTrue(rule.patternAndContextMatches("abc", 1));
        assertFalse(rule.patternAndContextMatches("xabc", 2));
        assertFalse(rule.patternAndContextMatches("abcd", 1));
    }

    // Tests pattern matching with boxed contexts combined with start and end anchors
    @Test
    public void testPatternAndContextMatches_boxedContextWithAnchors_matchesCorrectly() {
        Rule.Phoneme ph = new Rule.Phoneme("out", Languages.ANY_LANGUAGE);
        Rule rule = new Rule("b", "^[aeiou]", "[^xyz]$", ph);

        assertTrue(rule.patternAndContextMatches("abw", 1));
        assertFalse(rule.patternAndContextMatches("xabw", 2));
        assertFalse(rule.patternAndContextMatches("abx", 1));
        assertFalse(rule.patternAndContextMatches("abwa", 1));
    }

    // Tests pattern matching with empty context matchers
    @Test
    public void testPatternAndContextMatches_emptyContexts_matchesCorrectly() {
        Rule.Phoneme ph = new Rule.Phoneme("out", Languages.ANY_LANGUAGE);
        Rule rule = new Rule("abc", "", "", ph);

        assertTrue(rule.patternAndContextMatches("abc", 0));
        assertTrue(rule.patternAndContextMatches("xabcy", 1));
        assertFalse(rule.patternAndContextMatches("xabzy", 1));
    }

    // Tests getInstance with multi-language set
    @Test
    public void testGetInstance_multiLanguageSet_returnsRules() {
        Languages.LanguageSet langSet = Languages.LanguageSet.from(new HashSet<String>(Arrays.asList("french", "german")));
        List<Rule> rules = Rule.getInstance(NameType.GENERIC, RuleType.APPROX, langSet);
        assertNotNull(rules);
        assertFalse(rules.isEmpty());
    }

    // Tests getInstanceMap with single language string
    @Test
    public void testGetInstanceMap_withLanguageString_returnsMap() {
        Map<String, List<Rule>> map = Rule.getInstanceMap(NameType.GENERIC, RuleType.EXACT, "english");
        assertNotNull(map);
        assertFalse(map.isEmpty());
    }

    // Tests getInstanceMap with NO_LANGUAGES throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testGetInstanceMap_noLanguages_throwsIllegalArgumentException() {
        Rule.getInstanceMap(NameType.GENERIC, RuleType.EXACT, Languages.NO_LANGUAGES);
    }

    // Tests getInstance with Sephardic name type
    @Test
    public void testGetInstance_sephardicRules() {
        List<Rule> rules = Rule.getInstance(NameType.SEPHARDIC, RuleType.APPROX, "spanish");
        assertNotNull(rules);
        assertFalse(rules.isEmpty());
    }

    // Tests Phoneme with NO_LANGUAGES
    @Test
    public void testPhoneme_withEmptyLanguageSet() {
        Rule.Phoneme p = new Rule.Phoneme("", Languages.NO_LANGUAGES);
        assertEquals("", p.getPhonemeText().toString());
        assertEquals(Languages.NO_LANGUAGES, p.getLanguages());
    }
}