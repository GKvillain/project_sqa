package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class CharacterReaderTest {

    // Tests defect 34b: nextIndexOf CharSequence longer than remaining input doesn't throw ArrayIndexOutOfBoundsException
    @Test
    public void testNextIndexOf_sequenceLongerThanRemainingInput_returnsMinusOne() {
        CharacterReader reader = new CharacterReader("something special");
        assertEquals(10, reader.nextIndexOf("special"));
        assertEquals(-1, reader.nextIndexOf("not present"));
        assertEquals(-1, reader.nextIndexOf("something special and more"));
        assertEquals(-1, reader.nextIndexOf("special extra"));
    }

    // Tests constructor validation on null input
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullInput_throwsException() {
        new CharacterReader(null);
    }

    // Tests basic consume, current, advance, isEmpty, and EOF behavior
    @Test
    public void testConsumeAndCurrent_normalInput_consumesCorrectly() {
        CharacterReader reader = new CharacterReader("abc");
        assertFalse(reader.isEmpty());
        assertEquals('a', reader.current());
        assertEquals('a', reader.consume());
        assertEquals(1, reader.pos());
        assertEquals("b", reader.consumeAsString());
        reader.advance();
        assertTrue(reader.isEmpty());
        assertEquals(CharacterReader.EOF, reader.current());
        assertEquals(CharacterReader.EOF, reader.consume());
    }

    // Tests unconsume, mark, and rewindToMark
    @Test
    public void testMarkAndRewind_validFlow_restoresPosition() {
        CharacterReader reader = new CharacterReader("hello");
        reader.consume(); // pos = 1
        reader.mark();
        reader.consume(); // pos = 2
        reader.consume(); // pos = 3
        assertEquals(3, reader.pos());
        reader.rewindToMark();
        assertEquals(1, reader.pos());
        assertEquals('e', reader.current());
        reader.unconsume();
        assertEquals(0, reader.pos());
        assertEquals('h', reader.current());
    }

    // Tests nextIndexOf char and CharSequence
    @Test
    public void testNextIndexOf_charAndCharSequence_returnsCorrectOffsets() {
        CharacterReader reader = new CharacterReader("one two three");
        assertEquals(3, reader.nextIndexOf(' '));
        assertEquals(-1, reader.nextIndexOf('z'));
        assertEquals(4, reader.nextIndexOf("two"));
        assertEquals(8, reader.nextIndexOf("three"));
        assertEquals(-1, reader.nextIndexOf("four"));
    }

    // Tests consumeTo char and String
    @Test
    public void testConsumeTo_charAndString_consumesUpToMatchOrEnd() {
        CharacterReader reader = new CharacterReader("one,two,three");
        assertEquals("one", reader.consumeTo(','));
        assertEquals(',', reader.consume());
        assertEquals("two", reader.consumeTo(",three"));
        assertEquals(",three", reader.consumeToEnd());
        assertEquals("", reader.consumeToEnd());

        CharacterReader reader2 = new CharacterReader("unmatched");
        assertEquals("unmatched", reader2.consumeTo('x'));
        assertEquals("", reader2.consumeTo("xyz"));
    }

    // Tests consumeToAny with multiple character delimiters
    @Test
    public void testConsumeToAny_multipleDelimiters_consumesCorrectly() {
        CharacterReader reader = new CharacterReader("foo & bar < baz");
        assertEquals("foo ", reader.consumeToAny('&', '<'));
        assertEquals('&', reader.consume());
        assertEquals(" bar ", reader.consumeToAny('&', '<'));
        assertEquals('<', reader.consume());
        assertEquals(" baz", reader.consumeToAny('&', '<'));
    }

    // Tests consumeLetterSequence, consumeDigitSequence, and consumeHexSequence
    @Test
    public void testConsumeSequences_lettersDigitsHex_consumesMatchingSubstrings() {
        CharacterReader readerLetters = new CharacterReader("AbCd123");
        assertEquals("AbCd", readerLetters.consumeLetterSequence());

        CharacterReader readerDigits = new CharacterReader("12345abc");
        assertEquals("12345", readerDigits.consumeDigitSequence());

        CharacterReader readerHex = new CharacterReader("1aF8g9");
        assertEquals("1aF8", readerHex.consumeHexSequence());
    }

    // Tests consumeLetterThenDigitSequence
    @Test
    public void testConsumeLetterThenDigitSequence_mixedInput_consumesLettersThenDigits() {
        CharacterReader reader = new CharacterReader("alpha123-rest");
        assertEquals("alpha123", reader.consumeLetterThenDigitSequence());
        assertEquals("-rest", reader.consumeToEnd());

        CharacterReader readerOnlyLetters = new CharacterReader("alpha");
        assertEquals("alpha", readerOnlyLetters.consumeLetterThenDigitSequence());

        CharacterReader readerOnlyDigits = new CharacterReader("123");
        assertEquals("123", readerOnlyDigits.consumeLetterThenDigitSequence());
    }

    // Tests matches, matchesIgnoreCase, matchesLetter, matchesDigit, and matchesAny
    @Test
    public void testMatches_variousPredicates_returnsExpectedBoolean() {
        CharacterReader reader = new CharacterReader("Title1");
        assertTrue(reader.matches('T'));
        assertFalse(reader.matches('t'));
        assertTrue(reader.matches("Title"));
        assertFalse(reader.matches("title"));
        assertTrue(reader.matchesIgnoreCase("title"));
        assertFalse(reader.matchesIgnoreCase("Title1234")); // longer than input
        assertTrue(reader.matchesLetter());
        assertFalse(reader.matchesDigit());
        assertTrue(reader.matchesAny('a', 'T', 'z'));
        assertFalse(reader.matchesAny('a', 'b', 'c'));

        CharacterReader emptyReader = new CharacterReader("");
        assertFalse(emptyReader.matches('a'));
        assertFalse(emptyReader.matches("a"));
        assertFalse(emptyReader.matchesIgnoreCase("a"));
        assertFalse(emptyReader.matchesLetter());
        assertFalse(emptyReader.matchesDigit());
        assertFalse(emptyReader.matchesAny('a'));
    }

    // Tests matchConsume and matchConsumeIgnoreCase
    @Test
    public void testMatchConsume_caseSensitiveAndInsensitive_advancesOnlyOnMatch() {
        CharacterReader reader = new CharacterReader("<html><BODY>");
        assertTrue(reader.matchConsume("<html>"));
        assertEquals(6, reader.pos());
        assertFalse(reader.matchConsume("body"));
        assertEquals(6, reader.pos());
        assertTrue(reader.matchConsumeIgnoreCase("<body>"));
        assertEquals(12, reader.pos());
        assertTrue(reader.isEmpty());
    }

    // Tests containsIgnoreCase
    @Test
    public void testContainsIgnoreCase_differentCases_returnsTrueIfContained() {
        CharacterReader reader = new CharacterReader("<html><TITLE>Test</TITLE><p>text</p>");
        assertTrue(reader.containsIgnoreCase("title"));
        assertTrue(reader.containsIgnoreCase("TITLE"));
        assertTrue(reader.containsIgnoreCase("html"));
        assertFalse(reader.containsIgnoreCase("style"));
    }

    // Tests toString method
    @Test
    public void testToString_partiallyConsumed_returnsRemainingString() {
        CharacterReader reader = new CharacterReader("abcdef");
        assertEquals("abcdef", reader.toString());
        reader.consume();
        reader.consume();
        assertEquals("cdef", reader.toString());
    }
}