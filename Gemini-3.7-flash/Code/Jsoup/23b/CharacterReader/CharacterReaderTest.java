package org.jsoup.parser;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class CharacterReaderTest {

    // Tests null input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCharacterReader_nullInput_throwsException() {
        new CharacterReader(null);
    }

    // Tests carriage return normalization in constructor
    @Test
    public void testCharacterReader_carriageReturns_normalisedToNewlines() {
        CharacterReader reader = new CharacterReader("a\r\nb\rc\nd");
        assertEquals("a\nb\nc\nd", reader.toString());
    }

    // Tests current and consume on empty string
    @Test
    public void testConsume_emptyInput_returnsEOF() {
        CharacterReader reader = new CharacterReader("");
        assertTrue(reader.isEmpty());
        assertEquals(CharacterReader.EOF, reader.current());
        assertEquals(CharacterReader.EOF, reader.consume());
        assertTrue(reader.isEmpty());
    }

    // Tests consume, current, advance, and unconsume
    @Test
    public void testConsumeAndUnconsume_validString_advancesAndRewinds() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals('a', reader.current());
        assertEquals('a', reader.consume());
        assertEquals('b', reader.current());
        reader.advance();
        assertEquals('c', reader.current());
        reader.unconsume();
        assertEquals('b', reader.current());
        assertEquals(1, reader.pos());
    }

    // Tests mark and rewindToMark functionality
    @Test
    public void testMarkAndRewind_validString_rewindsToMarkedPosition() {
        CharacterReader reader = new CharacterReader("abcdef");
        reader.consume(); // pos = 1
        reader.mark();
        reader.consume(); // pos = 2
        reader.consume(); // pos = 3
        assertEquals('d', reader.current());
        reader.rewindToMark();
        assertEquals(1, reader.pos());
        assertEquals('b', reader.current());
    }

    // Tests consumeTo with char parameter when found and not found
    @Test
    public void testConsumeToChar_targetFoundAndNotFound_returnsConsumedString() {
        CharacterReader reader = new CharacterReader("hello world");
        String consumed = reader.consumeTo(' ');
        assertEquals("hello", consumed);
        assertEquals(' ', reader.current());

        CharacterReader reader2 = new CharacterReader("hello world");
        String consumedAll = reader2.consumeTo('z');
        assertEquals("hello world", consumedAll);
        assertTrue(reader2.isEmpty());
    }

    // Tests consumeTo with String parameter when found and not found
    @Test
    public void testConsumeToString_seqFoundAndNotFound_returnsConsumedString() {
        CharacterReader reader = new CharacterReader("one two three");
        String consumed = reader.consumeTo("two");
        assertEquals("one ", consumed);
        assertTrue(reader.matches("two"));

        CharacterReader reader2 = new CharacterReader("one two three");
        String consumedAll = reader2.consumeTo("four");
        assertEquals("one two three", consumedAll);
        assertTrue(reader2.isEmpty());
    }

    // Tests consumeToAny with multiple delimiters
    @Test
    public void testConsumeToAny_multipleDelimiters_consumesToFirstMatch() {
        CharacterReader reader = new CharacterReader("foo & bar < baz");
        String consumed = reader.consumeToAny('&', '<');
        assertEquals("foo ", consumed);
        assertEquals('&', reader.current());

        reader.consume(); // consume '&'
        String consumed2 = reader.consumeToAny('&', '<');
        assertEquals(" bar ", consumed2);
        assertEquals('<', reader.current());
    }

    // Tests consumeLetterSequence
    @Test
    public void testConsumeLetterSequence_mixedInput_consumesLettersOnly() {
        CharacterReader reader = new CharacterReader("ValidLetter123");
        String letters = reader.consumeLetterSequence();
        assertEquals("ValidLetter", letters);
        assertEquals('1', reader.current());

        CharacterReader reader2 = new CharacterReader("123abc");
        assertEquals("", reader2.consumeLetterSequence());
    }

    // Tests consumeHexSequence
    @Test
    public void testConsumeHexSequence_hexCharacters_consumesHexOnly() {
        CharacterReader reader = new CharacterReader("0123456789abcdefABCDEFgh");
        String hex = reader.consumeHexSequence();
        assertEquals("0123456789abcdefABCDEF", hex);
        assertEquals('g', reader.current());
    }

    // Tests consumeDigitSequence
    @Test
    public void testConsumeDigitSequence_digitsAndNonDigits_consumesDigitsOnly() {
        CharacterReader reader = new CharacterReader("12345abc");
        String digits = reader.consumeDigitSequence();
        assertEquals("12345", digits);
        assertEquals('a', reader.current());
    }

    // Tests matches with char and String
    @Test
    public void testMatches_charAndString_returnsCorrectBoolean() {
        CharacterReader reader = new CharacterReader("test string");
        assertTrue(reader.matches('t'));
        assertFalse(reader.matches('e'));
        assertTrue(reader.matches("test"));
        assertFalse(reader.matches("best"));

        CharacterReader emptyReader = new CharacterReader("");
        assertFalse(emptyReader.matches('a'));
        assertFalse(emptyReader.matches("a"));
    }

    // Tests matchesIgnoreCase
    @Test
    public void testMatchesIgnoreCase_differentCases_returnsTrue() {
        CharacterReader reader = new CharacterReader("TeSt StRiNg");
        assertTrue(reader.matchesIgnoreCase("test"));
        assertTrue(reader.matchesIgnoreCase("TEST"));
        assertFalse(reader.matchesIgnoreCase("best"));
    }

    // Tests matchesAny with multiple characters
    @Test
    public void testMatchesAny_matchingAndNonMatchingChars_returnsCorrectBoolean() {
        CharacterReader reader = new CharacterReader("hello");
        assertTrue(reader.matchesAny('a', 'e', 'h'));
        assertFalse(reader.matchesAny('x', 'y', 'z'));

        CharacterReader emptyReader = new CharacterReader("");
        assertFalse(emptyReader.matchesAny('a', 'b'));
    }

    // Tests matchesLetter and matchesDigit
    @Test
    public void testMatchesLetterAndDigit_variousChars_returnsCorrectBoolean() {
        CharacterReader reader = new CharacterReader("a1!");
        assertTrue(reader.matchesLetter());
        assertFalse(reader.matchesDigit());

        reader.advance(); // pos at '1'
        assertFalse(reader.matchesLetter());
        assertTrue(reader.matchesDigit());

        reader.advance(); // pos at '!'
        assertFalse(reader.matchesLetter());
        assertFalse(reader.matchesDigit());

        reader.advance(); // EOF
        assertFalse(reader.matchesLetter());
        assertFalse(reader.matchesDigit());
    }

    // Tests matchConsume and matchConsumeIgnoreCase
    @Test
    public void testMatchConsume_matchingAndNonMatching_advancesPosWhenMatched() {
        CharacterReader reader = new CharacterReader("FooBar");
        assertFalse(reader.matchConsume("bar"));
        assertEquals(0, reader.pos());

        assertTrue(reader.matchConsume("Foo"));
        assertEquals(3, reader.pos());
        assertEquals("Bar", reader.toString());

        assertTrue(reader.matchConsumeIgnoreCase("bar"));
        assertTrue(reader.isEmpty());
    }

    // Tests containsIgnoreCase
    @Test
    public void testContainsIgnoreCase_differentCases_returnsTrueIfPresent() {
        CharacterReader reader = new CharacterReader("abc </title> def");
        assertTrue(reader.containsIgnoreCase("</TITLE>"));
        assertTrue(reader.containsIgnoreCase("</title>"));
        assertFalse(reader.containsIgnoreCase("</style>"));
    }

    // Tests toString returns remaining substring
    @Test
    public void testToString_partialConsumption_returnsRemainingString() {
        CharacterReader reader = new CharacterReader("abcdef");
        reader.consume();
        reader.consume();
        assertEquals("cdef", reader.toString());
    }

    // Additional tests for uncovered methods and boundary conditions

    @Test
    public void testConsumeAsString() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals("a", reader.consumeAsString());
        assertEquals("b", reader.consumeAsString());
        assertEquals(2, reader.pos());
    }

    @Test
    public void testConsumeLetterThenDigitSequence() {
        CharacterReader reader = new CharacterReader("foo123bar");
        assertEquals("foo123", reader.consumeLetterThenDigitSequence());
        assertEquals("bar", reader.toString());

        CharacterReader readerOnlyLetters = new CharacterReader("foobar");
        assertEquals("foobar", readerOnlyLetters.consumeLetterThenDigitSequence());
        assertTrue(readerOnlyLetters.isEmpty());

        CharacterReader readerStartingDigits = new CharacterReader("123bar");
        assertEquals("", readerStartingDigits.consumeLetterThenDigitSequence());
        assertEquals("123bar", readerStartingDigits.toString());
    }

    @Test
    public void testConsumeToAnySorted() {
        char[] sortedDelims = new char[]{'&', '<', '>'};
        Arrays.sort(sortedDelims);

        CharacterReader reader = new CharacterReader("one<two&three>four");
        assertEquals("one", reader.consumeToAnySorted(sortedDelims));
        assertEquals('<', reader.consume());
        assertEquals("two", reader.consumeToAnySorted(sortedDelims));
        assertEquals('&', reader.consume());
        assertEquals("three", reader.consumeToAnySorted(sortedDelims));
        assertEquals('>', reader.consume());
        assertEquals("four", reader.consumeToAnySorted(sortedDelims));
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testMatchesAnySorted() {
        char[] sortedChars = new char[]{'c', 'f', 'x'};
        Arrays.sort(sortedChars);

        CharacterReader reader = new CharacterReader("foo");
        assertTrue(reader.matchesAnySorted(sortedChars));

        CharacterReader readerNoMatch = new CharacterReader("bar");
        assertFalse(readerNoMatch.matchesAnySorted(sortedChars));

        CharacterReader emptyReader = new CharacterReader("");
        assertFalse(emptyReader.matchesAnySorted(sortedChars));
    }

    @Test
    public void testMatchesAndMatchesIgnoreCase_seqLongerThanRemaining() {
        CharacterReader reader = new CharacterReader("short");
        assertFalse(reader.matches("longer sequence"));
        assertFalse(reader.matchesIgnoreCase("LONGER SEQUENCE"));

        reader.consumeTo('t');
        assertFalse(reader.matches("too long"));
        assertFalse(reader.matchesIgnoreCase("TOO LONG"));
    }

    @Test
    public void testConsumeHexSequence_nonHexStart() {
        CharacterReader reader = new CharacterReader("xyz123");
        assertEquals("", reader.consumeHexSequence());
        assertEquals('x', reader.current());
    }

    @Test
    public void testConsumeDigitSequence_nonDigitStart() {
        CharacterReader reader = new CharacterReader("abc123");
        assertEquals("", reader.consumeDigitSequence());
        assertEquals('a', reader.current());
    }

    @Test
    public void testConsumeToAny_noMatchConsumesToEnd() {
        CharacterReader reader = new CharacterReader("hello world");
        String consumed = reader.consumeToAny('x', 'y', 'z');
        assertEquals("hello world", consumed);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testContainsIgnoreCase_notFoundWhenRemainingIsShorter() {
        CharacterReader reader = new CharacterReader("short");
        assertFalse(reader.containsIgnoreCase("longer search text"));
    }
}