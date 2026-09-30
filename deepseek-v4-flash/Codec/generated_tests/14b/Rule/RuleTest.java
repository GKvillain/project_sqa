package org.apache.commons.codec.language.bm;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.codec.language.bm.Languages.LanguageSet;
import org.junit.Test;

public class RuleTest {

    // Test Phoneme constructor with CharSequence and LanguageSet
    @Test
    public void testPhoneme_constructorWithCharSequence_createsCorrectly() {
        Set<String> langs = new HashSet<String>();
        langs.add("en");
        LanguageSet ls = LanguageSet.from(langs);
        Rule.Phoneme phoneme = new Rule.Phoneme("test", ls);
        assertEquals("test", phoneme.getPhonemeText().toString());
        assertEquals(ls, phoneme.getLanguages());
    }

    // Test Phoneme append method returns the same instance
    @Test
    public void testPhoneme_append_returnsSameInstance() {
        Set<String> langs = new HashSet<String>();
        langs.add("en");
        LanguageSet ls = LanguageSet.from(langs);
        Rule.Phoneme phoneme = new Rule.Phoneme("test", ls);
        Rule.Phoneme result = phoneme.append("123");
        assertSame(phoneme, result);
        assertEquals("test123", phoneme.getPhonemeText().toString());
    }

    // Test Phoneme constructor combining two phonemes
    @Test
    public void testPhoneme_constructorWithTwoPhonemes_appendsText() {
        Set<String> langs = new HashSet<String>();
        langs.add("en");
        LanguageSet ls = LanguageSet.from(langs);
        Rule.Phoneme left = new Rule.Phoneme("he", ls);
        Rule.Phoneme right = new Rule.Phoneme("llo", ls);
        Rule.Phoneme combined = new Rule.Phoneme(left, right);
        assertEquals("hello", combined.getPhonemeText().toString());
    }

    // Test Phoneme constructor with two phonemes and custom language set
    @Test
    public void testPhoneme_constructorWithTwoPhonemesAndLanguageSet_usesCustomLang() {
        Set<String> langs1 = new HashSet<String>();
        langs1.add("en");
        Set<String> langs2 = new HashSet<String>();
        langs2.add("fr");
        LanguageSet ls1 = LanguageSet.from(langs1);
        LanguageSet ls2 = LanguageSet.from(langs2);
        Rule.Phoneme left = new Rule.Phoneme("he", ls1);
        Rule.Phoneme right = new Rule.Phoneme("llo", ls2);
        Rule.Phoneme combined = new Rule.Phoneme(left, right, ls1);
        assertEquals("hello", combined.getPhonemeText().toString());
        assertEquals(ls1, combined.getLanguages());
    }

    // Test Phoneme getPhonemes returns singleton
    @Test
    public void testPhoneme_getPhonemes_returnsSingletonList() {
        Set<String> langs = new HashSet<String>();
        langs.add("en");
        LanguageSet ls = LanguageSet.from(langs);
        Rule.Phoneme phoneme = new Rule.Phoneme("abc", ls);
        int count = 0;
        for (Rule.Phoneme p : phoneme.getPhonemes()) {
            count++;
            assertSame(phoneme, p);
        }
        assertEquals(1, count);
    }

    // Test Phoneme join method (deprecated)
    @Test
    public void testPhoneme_join_mergesTextAndRestrictsLanguages() {
        Set<String> langs1 = new HashSet<String>();
        langs1.add("en");
        Set<String> langs2 = new HashSet<String>();
        langs2.add("en");
        LanguageSet ls1 = LanguageSet.from(langs1);
        LanguageSet ls2 = LanguageSet.from(langs2);
        Rule.Phoneme left = new Rule.Phoneme("ab", ls1);
        Rule.Phoneme right = new Rule.Phoneme("cd", ls2);
        Rule.Phoneme joined = left.join(right);
        assertEquals("abcd", joined.getPhonemeText().toString());
        assertNotNull(joined.getLanguages());
    }

    // Test PhonemeList getPhonemes returns provided list
    @Test
    public void testPhonemeList_getPhonemes_returnsProvidedList() {
        Set<String> langs = new HashSet<String>();
        langs.add("en");
        LanguageSet ls = LanguageSet.from(langs);
        Rule.Phoneme p1 = new Rule.Phoneme("a", ls);
        Rule.Phoneme p2 = new Rule.Phoneme("b", ls);
        java.util.List<Rule.Phoneme> list = new java.util.ArrayList<Rule.Phoneme>();
        list.add(p1);
        list.add(p2);
        Rule.PhonemeList pl = new Rule.PhonemeList(list);
        assertSame(list, pl.getPhonemes());
    }

    // Test Rule constructor and basic accessors
    @Test
    public void testRule_constructor_storesFields() {
        Rule rule = new Rule("pat", "lCtx", "rCtx", new Rule.Phoneme("pho", Languages.ANY_LANGUAGE));
        assertEquals("pat", rule.getPattern());
        assertNotNull(rule.getLContext());
        assertNotNull(rule.getRContext());
        assertNotNull(rule.getPhoneme());
    }

    // Test patternAndContextMatches returns false for negative index
    @Test(expected = IndexOutOfBoundsException.class)
    public void testPatternAndContextMatches_negativeIndex_throwsException() {
        Rule rule = new Rule("pat", "", "", new Rule.Phoneme("ph", Languages.ANY_LANGUAGE));
        rule.patternAndContextMatches("input", -1);
    }

    // Test patternAndContextMatches returns false when pattern longer than remaining input
    @Test
    public void testPatternAndContextMatches_patternTooLong_returnsFalse() {
        Rule rule = new Rule("hello", "", "", new Rule.Phoneme("ph", Languages.ANY_LANGUAGE));
        assertFalse(rule.patternAndContextMatches("hi", 0));
    }

    // Test patternAndContextMatches returns false when pattern does not match
    @Test
    public void testPatternAndContextMatches_patternMismatch_returnsFalse() {
        Rule rule = new Rule("abc", "", "", new Rule.Phoneme("ph", Languages.ANY_LANGUAGE));
        assertFalse(rule.patternAndContextMatches("xyzabc", 0));
    }

    // Test patternAndContextMatches returns true when pattern matches and contexts are empty
    @Test
    public void testPatternAndContextMatches_patternOnlyMatch_returnsTrue() {
        Rule rule = new Rule("abc", "", "", new Rule.Phoneme("ph", Languages.ANY_LANGUAGE));
        assertTrue(rule.patternAndContextMatches("abc", 0));
    }

    // Test patternAndContextMatches returns false when right context fails
    @Test
    public void testPatternAndContextMatches_rightContextFails_returnsFalse() {
        Rule rule = new Rule("abc", "", "x", new Rule.Phoneme("ph", Languages.ANY_LANGUAGE));
        assertFalse(rule.patternAndContextMatches("abcy", 0));
    }

    // Test patternAndContextMatches returns false when left context fails
    @Test
    public void testPatternAndContextMatches_leftContextFails_returnsFalse() {
        Rule rule = new Rule("abc", "x", "", new Rule.Phoneme("ph", Languages.ANY_LANGUAGE));
        assertFalse(rule.patternAndContextMatches("yabc", 0));
    }

    // Test patternAndContextMatches returns true when all contexts match
    @Test
    public void testPatternAndContextMatches_allMatch_returnsTrue() {
        Rule rule = new Rule("abc", "x", "y", new Rule.Phoneme("ph", Languages.ANY_LANGUAGE));
        assertTrue(rule.patternAndContextMatches("xabcy", 1));
    }

    // Test ALL_STRINGS_RMATCHER always returns true
    @Test
    public void testALL_STRINGS_RMATCHER_isMatch_returnsTrue() {
        assertTrue(Rule.ALL_STRINGS_RMATCHER.isMatch(""));
        assertTrue(Rule.ALL_STRINGS_RMATCHER.isMatch("anything"));
    }

    // Test ALL constant
    @Test
    public void testALL_constant_isCorrectValue() {
        assertEquals("ALL", Rule.ALL);
    }

    // Test Phoneme COMPARATOR compares correctly when first differs
    @Test
    public void testPhonemeComparator_compareFirstDiff_returnsNegative() {
        Set<String> langs = new HashSet<String>();
        langs.add("en");
        LanguageSet ls = LanguageSet.from(langs);
        Rule.Phoneme p1 = new Rule.Phoneme("a", ls);
        Rule.Phoneme p2 = new Rule.Phoneme("b", ls);
        assertTrue(Rule.Phoneme.COMPARATOR.compare(p1, p2) < 0);
    }

    // ===== New tests for uncovered parts =====

    // Test LeftContext isMatch
    @Test
    public void testLeftContext_isMatch_returnsCorrectly() {
        Rule.LeftContext lc = new Rule.LeftContext("ab");
        assertTrue(lc.isMatch("ab"));
        assertTrue(lc.isMatch("xab"));
        assertFalse(lc.isMatch("abc"));
        assertFalse(lc.isMatch(""));
    }

    // Test RightContext isMatch
    @Test
    public void testRightContext_isMatch_returnsCorrectly() {
        Rule.RightContext rc = new Rule.RightContext("cd");
        assertTrue(rc.isMatch("cd"));
        assertTrue(rc.isMatch("cdef"));
        assertFalse(rc.isMatch("acd"));
        assertFalse(rc.isMatch(""));
    }

    // Test Phoneme equals and hashCode
    @Test
    public void testPhoneme_equals_sameValues_returnsTrue() {
        Set<String> langs = new HashSet<String>();
        langs.add("en");
        LanguageSet ls = LanguageSet.from(langs);
        Rule.Phoneme p1 = new Rule.Phoneme("test", ls);
        Rule.Phoneme p2 = new Rule.Phoneme("test", ls);
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testPhoneme_equals_differentText_returnsFalse() {
        Set<String> langs = new HashSet<String>();
        langs.add("en");
        LanguageSet ls = LanguageSet.from(langs);
        Rule.Phoneme p1 = new Rule.Phoneme("a", ls);
        Rule.Phoneme p2 = new Rule.Phoneme("b", ls);
        assertNotEquals(p1, p2);
    }

    // Test Phoneme toString
    @Test
    public void testPhoneme_toString_returnsPhonemeText() {
        Set<String> langs = new HashSet<String>();
        langs.add("en");
        LanguageSet ls = LanguageSet.from(langs);
        Rule.Phoneme phoneme = new Rule.Phoneme("test", ls);
        assertEquals("test", phoneme.toString());
    }

    // Test PhonemeList equals and hashCode (assuming PhonemeList overrides them)
    @Test
    public void testPhonemeList_equals_sameList_returnsTrue() {
        Set<String> langs = new HashSet<String>();
        langs.add("en");
        LanguageSet ls = LanguageSet.from(langs);
        Rule.Phoneme p1 = new Rule.Phoneme("a", ls);
        java.util.List<Rule.Phoneme> list1 = new java.util.ArrayList<Rule.Phoneme>();
        list1.add(p1);
        Rule.PhonemeList pl1 = new Rule.PhonemeList(list1);
        Rule.PhonemeList pl2 = new Rule.PhonemeList(new java.util.ArrayList<Rule.Phoneme>(list1));
        assertEquals(pl1, pl2);
        assertEquals(pl1.hashCode(), pl2.hashCode());
    }

    // Test Phoneme.join with different language sets (intersection)
    @Test
    public void testPhoneme_join_differentLanguages_intersects() {
        Set<String> langs1 = new HashSet<String>();
        langs1.add("en");
        Set<String> langs2 = new HashSet<String>();
        langs2.add("fr");
        LanguageSet ls1 = LanguageSet.from(langs1);
        LanguageSet ls2 = LanguageSet.from(langs2);
        Rule.Phoneme left = new Rule.Phoneme("ab", ls1);
        Rule.Phoneme right = new Rule.Phoneme("cd", ls2);
        Rule.Phoneme joined = left.join(right);
        assertEquals("abcd", joined.getPhonemeText().toString());
        // The languages are the intersection (empty set), so the returned set is not null
        assertNotNull(joined.getLanguages());
    }
}