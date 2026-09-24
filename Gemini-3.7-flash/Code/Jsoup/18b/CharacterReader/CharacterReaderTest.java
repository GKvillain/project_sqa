package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class CharacterReaderTest {

    // Tests null input to constructor
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullInput_throwsException() {
        new CharacterReader(null);
    }

    // Tests consuming full string to the end (defect 18 regression test)
    @Test
    public void testConsumeToEnd_standardString_returnsCompleteString() {
        CharacterReader r = new CharacterReader("abcdef");
        String data = r.consumeToEnd();
        assertEquals("abcdef", data);
        assertTrue(r.isEmpty());
    }

    // Tests consumeTo(char) when char is present and when absent
    @Test
    public void testConsumeToChar_presentAndAbsent_consumesExpected() {
        CharacterReader r = new CharacterReader("foo&bar");
        assertEquals("foo", r.consumeTo('&'));
        assertEquals('&', r.current());
        r.consume(); // consume '&'
        assertEquals("bar", r.consumeTo(';')); // not found, consumes to end
        assertTrue(r.isEmpty());
    }

    // Tests consumeTo(String) when sequence is present and when absent
    @Test
    public void testConsumeToString_presentAndAbsent_consumesExpected() {
        CharacterReader r = new CharacterReader("hello world target end");
        assertEquals("hello world ", r.consumeTo("target"));
        assertEquals("target end", r.consumeTo("missing")); // not found, consumes to end
        assertTrue(r.isEmpty());
    }

    // Tests consumeToAny(char...) matching any of the given characters
    @Test
    public void testConsumeToAny_multipleChars_consumesToFirstMatch() {
        CharacterReader r = new CharacterReader("one, two; three");
        assertEquals("one", r.consumeToAny(',', ';'));
        assertEquals(',', r.current());
        r.advance(); // skip ','
        r.advance(); // skip ' '
        assertEquals("two", r.consumeToAny(';', ','));
        assertEquals(';', r.current());
    }

    // Tests consumeToAny when none match and when at EOF
    @Test
    public void testConsumeToAny_noMatchAndEmpty_returnsCorrect() {
        CharacterReader r = new CharacterReader("test");
        assertEquals("test", r.consumeToAny('x', 'y'));
        assertEquals("", r.consumeToAny('x', 'y'));
    }

    // Tests consumeLetterSequence with upper, lower, and non-letter chars
    @Test
    public void testConsumeLetterSequence_mixedInput_consumesLettersOnly() {
        CharacterReader r = new CharacterReader("HelloWorld123");
        assertEquals("HelloWorld", r.consumeLetterSequence());
        assertEquals("123", r.consumeToEnd());
    }

    // Tests consumeDigitSequence with digits and non-digits
    @Test
    public void testConsumeDigitSequence_digitsAndNonDigits_consumesDigitsOnly() {
        CharacterReader r = new CharacterReader("12345abc");
        assertEquals("12345", r.consumeDigitSequence());
        assertEquals("abc", r.consumeToEnd());
    }

    // Tests consumeHexSequence with hex digits and non-hex chars
    @Test
    public void testConsumeHexSequence_hexAndNonHex_consumesHexOnly() {
        CharacterReader r = new CharacterReader("0123456789abcdefABCDEFgh");
        assertEquals("0123456789abcdefABCDEF", r.consumeHexSequence());
        assertEquals("gh", r.consumeToEnd());
    }

    // Tests current(), consume(), isEmpty(), and pos() tracking
    @Test
    public void testCurrentAndConsume_normalFlow_advancesAndReturnsEOF() {
        CharacterReader r = new CharacterReader("ab");
        assertEquals(0, r.pos());
        assertFalse(r.isEmpty());
        assertEquals('a', r.current());
        assertEquals('a', r.consume());
        assertEquals(1, r.pos());
        assertEquals('b', r.consume());
        assertTrue(r.isEmpty());
        assertEquals(CharacterReader.EOF, r.current());
        assertEquals(CharacterReader.EOF, r.consume());
    }

    // Tests unconsume() and advance()
    @Test
    public void testUnconsumeAndAdvance_modifiesPosCorrectly() {
        CharacterReader r = new CharacterReader("abc");
        r.advance();
        assertEquals(1, r.pos());
        assertEquals('b', r.current());
        r.unconsume();
        assertEquals(0, r.pos());
        assertEquals('a', r.current());
    }

    // Tests mark() and rewindToMark()
    @Test
    public void testMarkAndRewind_restoresMarkedPosition() {
        CharacterReader r = new CharacterReader("testing");
        r.advance();
        r.advance();
        r.mark();
        assertEquals(2, r.pos());
        r.consumeLetterSequence();
        assertTrue(r.isEmpty());
        r.rewindToMark();
        assertEquals(2, r.pos());
        assertEquals('s', r.current());
    }

    // Tests matches(char) and matches(String)
    @Test
    public void testMatches_charAndString_returnsCorrectBoolean() {
        CharacterReader r = new CharacterReader("test string");
        assertTrue(r.matches('t'));
        assertFalse(r.matches('e'));
        assertTrue(r.matches("test"));
        assertFalse(r.matches("string"));

        r.consumeToEnd();
        assertFalse(r.matches('t'));
        assertFalse(r.matches("test"));
    }

    // Tests matchesIgnoreCase(String)
    @Test
    public void testMatchesIgnoreCase_caseInsensitive_returnsTrue() {
        CharacterReader r = new CharacterReader("TeSt StRiNg");
        assertTrue(r.matchesIgnoreCase("test"));
        assertTrue(r.matchesIgnoreCase("TEST"));
        assertFalse(r.matchesIgnoreCase("string"));
    }

    // Tests matchesAny(char...)
    @Test
    public void testMatchesAny_variousChars_returnsExpected() {
        CharacterReader r = new CharacterReader("apple");
        assertTrue(r.matchesAny('b', 'a', 'c'));
        assertFalse(r.matchesAny('x', 'y', 'z'));

        r.consumeToEnd();
        assertFalse(r.matchesAny('a'));
    }

    // Tests matchesLetter() and matchesDigit()
    @Test
    public void testMatchesLetterAndDigit_validAndInvalid_returnsCorrectly() {
        CharacterReader rLetters = new CharacterReader("aZ1");
        assertTrue(rLetters.matchesLetter());
        assertFalse(rLetters.matchesDigit());

        rLetters.advance(); // 'Z'
        assertTrue(rLetters.matchesLetter());

        rLetters.advance(); // '1'
        assertFalse(rLetters.matchesLetter());
        assertTrue(rLetters.matchesDigit());

        rLetters.advance(); // EOF
        assertFalse(rLetters.matchesLetter());
        assertFalse(rLetters.matchesDigit());
    }

    // Tests matchConsume(String)
    @Test
    public void testMatchConsume_matchingAndNonMatching_advancesWhenMatched() {
        CharacterReader r = new CharacterReader("hello world");
        assertFalse(r.matchConsume("world"));
        assertEquals(0, r.pos());
        assertTrue(r.matchConsume("hello "));
        assertEquals(6, r.pos());
        assertEquals("world", r.toString());
    }

    // Tests matchConsumeIgnoreCase(String)
    @Test
    public void testMatchConsumeIgnoreCase_caseInsensitive_advancesWhenMatched() {
        CharacterReader r = new CharacterReader("HeLLo World");
        assertFalse(r.matchConsumeIgnoreCase("world"));
        assertEquals(0, r.pos());
        assertTrue(r.matchConsumeIgnoreCase("hello "));
        assertEquals(6, r.pos());
        assertEquals("World", r.toString());
    }

    // Tests containsIgnoreCase(String)
    @Test
    public void testContainsIgnoreCase_variousCasing_findsPresence() {
        CharacterReader r = new CharacterReader("<html><head><TITLE>Test</title></head>");
        assertTrue(r.containsIgnoreCase("</TITLE>"));
        assertTrue(r.containsIgnoreCase("</title>"));
        assertFalse(r.containsIgnoreCase("</style>"));
    }

    // Tests toString()
    @Test
    public void testToString_returnsRemainingUnconsumedSubstring() {
        CharacterReader r = new CharacterReader("sample text");
        assertEquals("sample text", r.toString());
        r.consumeLetterSequence();
        assertEquals(" text", r.toString());
    }
}