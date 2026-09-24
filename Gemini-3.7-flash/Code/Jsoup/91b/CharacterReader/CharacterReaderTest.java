package org.jsoup.parser;

import org.jsoup.UncheckedIOException;
import org.junit.Test;

import java.io.StringReader;
import java.util.Arrays;

import static org.junit.Assert.*;

public class CharacterReaderTest {

    // Tests normal reading, consume and current
    @Test
    public void testConsume_normalInput_readsSequentialChars() {
        CharacterReader reader = new CharacterReader("One");
        assertEquals(0, reader.pos());
        assertEquals('O', reader.current());
        assertEquals('O', reader.consume());
        assertEquals('n', reader.current());
        assertEquals('n', reader.consume());
        assertEquals('e', reader.consume());
        assertTrue(reader.isEmpty());
        assertEquals(CharacterReader.EOF, reader.consume());
        assertEquals(CharacterReader.EOF, reader.current());
    }

    // Tests unconsume normal and exception when no buffer left
    @Test
    public void testUnconsume_validAndInvalidPosition_worksAndThrowsException() {
        CharacterReader reader = new CharacterReader("One");
        reader.consume();
        assertEquals('n', reader.current());
        reader.unconsume();
        assertEquals('O', reader.current());
    }

    // Tests unconsume throws exception on start
    @Test(expected = UncheckedIOException.class)
    public void testUnconsume_atBufferStart_throwsException() {
        CharacterReader reader = new CharacterReader("One");
        reader.unconsume();
    }

    // Tests mark and rewind functionality
    @Test
    public void testMarkAndRewind_validMark_rewindsPosition() {
        CharacterReader reader = new CharacterReader("Hello World");
        reader.consume(); // 'H'
        reader.mark();
        reader.consume(); // 'e'
        reader.consume(); // 'l'
        assertEquals(3, reader.pos());
        reader.rewindToMark();
        assertEquals(1, reader.pos());
        assertEquals('e', reader.current());
    }

    // Tests rewind without mark throws exception
    @Test(expected = UncheckedIOException.class)
    public void testRewindToMark_noMarkSet_throwsException() {
        CharacterReader reader = new CharacterReader("Hello");
        reader.rewindToMark();
    }

    // Tests nextIndexOf char and CharSequence
    @Test
    public void testNextIndexOf_charAndCharSequence_returnsCorrectOffset() {
        CharacterReader reader = new CharacterReader("abcdefg");
        assertEquals(2, reader.nextIndexOf('c'));
        assertEquals(-1, reader.nextIndexOf('z'));
        assertEquals(3, reader.nextIndexOf("def"));
        assertEquals(-1, reader.nextIndexOf("dgh"));
    }

    // Tests consumeTo with char and string
    @Test
    public void testConsumeTo_charAndString_consumesCorrectTokens() {
        CharacterReader reader = new CharacterReader("foo=bar&baz=qux");
        assertEquals("foo", reader.consumeTo('='));
        reader.consume(); // skip '='
        assertEquals("bar", reader.consumeTo("&"));
        reader.consume(); // skip '&'
        assertEquals("baz=qux", reader.consumeTo('z')); // delimiter 'z' found at start
        assertEquals("", reader.consumeTo("notfound"));
    }

    // Tests consumeToAny and consumeToAnySorted
    @Test
    public void testConsumeToAny_variousDelimiters_consumesCorrectly() {
        CharacterReader reader = new CharacterReader("foo & bar < baz");
        assertEquals("foo ", reader.consumeToAny('&', '<'));
        reader.consume(); // skip '&'
        char[] sortedDelims = new char[]{'<', '>'};
        Arrays.sort(sortedDelims);
        assertEquals(" bar ", reader.consumeToAnySorted(sortedDelims));
    }

    // Tests consumeData and consumeTagName
    @Test
    public void testConsumeDataAndTagName_htmlTokens_consumesCorrectSubstrings() {
        CharacterReader reader1 = new CharacterReader("hello&world<tag");
        assertEquals("hello", reader1.consumeData());

        CharacterReader reader2 = new CharacterReader("div class='test'>");
        assertEquals("div", reader2.consumeTagName());

        CharacterReader reader3 = new CharacterReader("div<span");
        assertEquals("div", reader3.consumeTagName());
    }

    // Tests letter and digit consuming methods
    @Test
    public void testConsumeLetterAndDigits_mixedInput_consumesSequences() {
        CharacterReader reader = new CharacterReader("abc123DEF456 789 1a2b");
        assertEquals("abc", reader.consumeLetterSequence());
        assertEquals("123DEF", reader.consumeLetterThenDigitSequence()); // starts with digit, empty letter part
        reader.advance();
        assertEquals("456", reader.consumeDigitSequence());
        reader.advance();
        assertEquals("789", reader.consumeHexSequence());
    }

    // Tests matching methods (matches, matchesIgnoreCase, matchesAny, matchesDigit, matchesLetter)
    @Test
    public void testMatches_variousPatterns_returnsExpectedBooleans() {
        CharacterReader reader = new CharacterReader("Hello 123");
        assertTrue(reader.matches('H'));
        assertFalse(reader.matches('e'));
        assertTrue(reader.matches("Hello"));
        assertFalse(reader.matches("hello"));
        assertTrue(reader.matchesIgnoreCase("hello"));
        assertTrue(reader.matchesAny('a', 'H', 'z'));
        assertFalse(reader.matchesAny('x', 'y'));
        assertTrue(reader.matchesLetter());
        assertFalse(reader.matchesDigit());

        char[] sorted = new char[]{'H', 'X'};
        assertTrue(reader.matchesAnySorted(sorted));

        assertTrue(reader.containsIgnoreCase("HELLO"));
        assertTrue(reader.containsIgnoreCase("123"));
        assertFalse(reader.containsIgnoreCase("notFound"));
    }

    // Tests matchConsume and matchConsumeIgnoreCase
    @Test
    public void testMatchConsume_caseSensitiveAndInsensitive_consumesAndAdvances() {
        CharacterReader reader = new CharacterReader("Hello World");
        assertFalse(reader.matchConsume("hello"));
        assertEquals('H', reader.current());
        assertTrue(reader.matchConsume("Hello"));
        assertEquals(' ', reader.current());

        CharacterReader reader2 = new CharacterReader("Hello World");
        assertTrue(reader2.matchConsumeIgnoreCase("hello"));
        assertEquals(' ', reader2.current());
    }

    // Tests rangeEquals and toString
    @Test
    public void testRangeEqualsAndToString_stringComparisons_correctResults() {
        CharacterReader reader = new CharacterReader("Testing");
        assertTrue(reader.rangeEquals(0, 4, "Test"));
        assertFalse(reader.rangeEquals(0, 4, "Fail"));
        assertFalse(reader.rangeEquals(0, 3, "Test"));
        assertEquals("Testing", reader.toString());
    }

    // Tests empty string reader initialization and operations
    @Test
    public void testEmptyReader_emptyString_behavesCorrectly() {
        CharacterReader reader = new CharacterReader("");
        assertTrue(reader.isEmpty());
        assertEquals(CharacterReader.EOF, reader.current());
        assertEquals(CharacterReader.EOF, reader.consume());
        assertEquals("", reader.consumeToEnd());
        assertFalse(reader.matches('a'));
        assertFalse(reader.matches("a"));
        assertFalse(reader.matchesLetter());
        assertFalse(reader.matchesDigit());
    }

    // Tests constructor with Reader and buffer sizing
    @Test
    public void testReaderConstructor_largeBufferInput_buffersCorrectly() {
        String data = "Long string data for testing reader buffer logic";
        CharacterReader reader = new CharacterReader(new StringReader(data), 16);
        assertEquals('L', reader.consume());
        assertEquals("ong", reader.consumeTo(' '));
    }
}