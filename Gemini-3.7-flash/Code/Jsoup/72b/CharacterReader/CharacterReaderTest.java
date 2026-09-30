package org.jsoup.parser;

import org.junit.Test;

import java.io.StringReader;
import java.util.Arrays;

import static org.junit.Assert.*;

public class CharacterReaderTest {

    // Tests null input to constructor throwing IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullReader_throwsIllegalArgumentException() {
        new CharacterReader((java.io.Reader) null, 100);
    }

    // Tests normal reading, current, consume, and EOF handling
    @Test
    public void testCurrentAndConsume_normalAndEmptyInput_returnsExpectedCharsAndEOF() {
        CharacterReader reader = new CharacterReader("abc");
        assertFalse(reader.isEmpty());
        assertEquals('a', reader.current());
        assertEquals('a', reader.consume());
        assertEquals('b', reader.current());
        assertEquals('b', reader.consume());
        assertEquals('c', reader.consume());
        assertTrue(reader.isEmpty());
        assertEquals(CharacterReader.EOF, reader.current());
        assertEquals(CharacterReader.EOF, reader.consume());
    }

    // Tests cursor positioning, advance, and unconsume
    @Test
    public void testPosAndAdvanceAndUnconsume_normalInput_tracksPositionCorrectly() {
        CharacterReader reader = new CharacterReader("abcdef");
        assertEquals(0, reader.pos());
        reader.advance();
        assertEquals(1, reader.pos());
        assertEquals('b', reader.current());
        reader.unconsume();
        assertEquals(0, reader.pos());
        assertEquals('a', reader.current());
    }

    // Tests mark and rewindToMark functionality
    @Test
    public void testMarkAndRewindToMark_normalInput_rewindsPosition() {
        CharacterReader reader = new CharacterReader("abcdef");
        reader.consume(); // at 'b'
        reader.mark();
        reader.consume(); // at 'c'
        reader.consume(); // at 'd'
        assertEquals(3, reader.pos());
        reader.rewindToMark();
        assertEquals(1, reader.pos());
        assertEquals('b', reader.current());
    }

    // Tests nextIndexOf for single character
    @Test
    public void testNextIndexOfChar_foundAndNotFound_returnsExpectedOffset() {
        CharacterReader reader = new CharacterReader("hello world");
        assertEquals(4, reader.nextIndexOf('o'));
        assertEquals(-1, reader.nextIndexOf('z'));
        reader.consume(); // 'h'
        assertEquals(3, reader.nextIndexOf('o'));
    }

    // Tests nextIndexOf for CharSequence
    @Test
    public void testNextIndexOfSequence_foundAndNotFound_returnsExpectedOffset() {
        CharacterReader reader = new CharacterReader("hello world");
        assertEquals(6, reader.nextIndexOf("world"));
        assertEquals(-1, reader.nextIndexOf("word"));
        reader.advance();
        assertEquals(5, reader.nextIndexOf("world"));
    }

    // Tests consumeTo with char delimiter
    @Test
    public void testConsumeToChar_presentAndAbsent_consumesCorrectly() {
        CharacterReader reader = new CharacterReader("one,two,three");
        assertEquals("one", reader.consumeTo(','));
        assertEquals(',', reader.consume());
        assertEquals("two", reader.consumeTo(','));
        assertEquals(',', reader.consume());
        assertEquals("three", reader.consumeTo(','));
        assertTrue(reader.isEmpty());
    }

    // Tests consumeTo with String delimiter
    @Test
    public void testConsumeToString_presentAndAbsent_consumesCorrectly() {
        CharacterReader reader = new CharacterReader("one-->two-->three");
        assertEquals("one", reader.consumeTo("-->"));
        assertTrue(reader.matchConsume("-->"));
        assertEquals("two", reader.consumeTo("-->"));
        assertTrue(reader.matchConsume("-->"));
        assertEquals("three", reader.consumeTo("-->"));
    }

    // Tests consumeToAny with vararg delimiters
    @Test
    public void testConsumeToAny_variousDelimiters_consumesUntilDelimiter() {
        CharacterReader reader = new CharacterReader("foo & bar < baz");
        assertEquals("foo ", reader.consumeToAny('&', '<'));
        assertEquals('&', reader.consume());
        assertEquals(" bar ", reader.consumeToAny('&', '<'));
        assertEquals('<', reader.consume());
        assertEquals(" baz", reader.consumeToAny('&', '<'));
    }

    // Tests consumeToAnySorted with sorted char array
    @Test
    public void testConsumeToAnySorted_sortedDelimiters_consumesUntilDelimiter() {
        char[] sortedDelims = new char[]{'&', '<', '>'};
        Arrays.sort(sortedDelims);
        CharacterReader reader = new CharacterReader("foo&bar<baz>qux");
        assertEquals("foo", reader.consumeToAnySorted(sortedDelims));
        reader.advance();
        assertEquals("bar", reader.consumeToAnySorted(sortedDelims));
        reader.advance();
        assertEquals("baz", reader.consumeToAnySorted(sortedDelims));
        reader.advance();
        assertEquals("qux", reader.consumeToAnySorted(sortedDelims));
    }

    // Tests consumeData method
    @Test
    public void testConsumeData_specialDelimiters_consumesUntilSpecialChar() {
        CharacterReader reader = new CharacterReader("text&data<more\0end");
        assertEquals("text", reader.consumeData());
        reader.advance(); // skip '&'
        assertEquals("data", reader.consumeData());
        reader.advance(); // skip '<'
        assertEquals("more", reader.consumeData());
        reader.advance(); // skip '\0'
        assertEquals("end", reader.consumeData());
    }

    // Tests consumeTagName method
    @Test
    public void testConsumeTagName_tagDelimiters_consumesUntilTagDelimiter() {
        CharacterReader reader = new CharacterReader("div class='test' / >");
        assertEquals("div", reader.consumeTagName());
        reader.advance();
        assertEquals("class='test'", reader.consumeToAny(' ', '/'));
    }

    // Tests consumeLetterSequence method
    @Test
    public void testConsumeLetterSequence_lettersAndNonLetters_consumesLettersOnly() {
        CharacterReader reader = new CharacterReader("Hello123World");
        assertEquals("Hello", reader.consumeLetterSequence());
        assertEquals("123", reader.consumeDigitSequence());
        assertEquals("World", reader.consumeLetterSequence());
        assertEquals("", reader.consumeLetterSequence());
    }

    // Tests consumeLetterThenDigitSequence method
    @Test
    public void testConsumeLetterThenDigitSequence_lettersDigitsAndOther_consumesLettersThenDigits() {
        CharacterReader reader = new CharacterReader("abc123-def456");
        assertEquals("abc123", reader.consumeLetterThenDigitSequence());
        assertEquals('-', reader.consume());
        assertEquals("def456", reader.consumeLetterThenDigitSequence());
        assertEquals("", reader.consumeLetterThenDigitSequence());
    }

    // Tests consumeHexSequence and consumeDigitSequence methods
    @Test
    public void testConsumeHexSequenceAndDigitSequence_hexAndDigits_consumesExpectedSequences() {
        CharacterReader reader = new CharacterReader("1aF8g99");
        assertEquals("1aF8", reader.consumeHexSequence());
        assertEquals('g', reader.consume());
        assertEquals("99", reader.consumeDigitSequence());
    }

    // Tests matches and matchesIgnoreCase
    @Test
    public void testMatchesAndMatchesIgnoreCase_matchingAndMismatching_returnsExpectedBoolean() {
        CharacterReader reader = new CharacterReader("TestCase");
        assertTrue(reader.matches('T'));
        assertFalse(reader.matches('t'));
        assertTrue(reader.matches("Test"));
        assertFalse(reader.matches("test"));
        assertTrue(reader.matchesIgnoreCase("test"));
        assertTrue(reader.matchesIgnoreCase("TESTCASE"));
        assertFalse(reader.matchesIgnoreCase("TESTCASEEXTRA"));
    }

    // Tests matchesAny, matchesAnySorted, matchesLetter, matchesDigit
    @Test
    public void testMatchesClassificationMethods_variousInputs_returnsExpectedBoolean() {
        CharacterReader reader = new CharacterReader("A1!");
        assertTrue(reader.matchesLetter());
        assertFalse(reader.matchesDigit());
        assertTrue(reader.matchesAny('B', 'A'));
        assertFalse(reader.matchesAny('B', 'C'));

        char[] sortedChars = new char[]{'A', 'B', 'C'};
        assertTrue(reader.matchesAnySorted(sortedChars));

        reader.advance(); // at '1'
        assertTrue(reader.matchesDigit());
        assertFalse(reader.matchesLetter());

        reader.advance(); // at '!'
        assertFalse(reader.matchesLetter());
        assertFalse(reader.matchesDigit());
    }

    // Tests matchConsume and matchConsumeIgnoreCase
    @Test
    public void testMatchConsumeAndMatchConsumeIgnoreCase_prefixMatching_consumesAndReturnsBoolean() {
        CharacterReader reader = new CharacterReader("HelloWorld");
        assertFalse(reader.matchConsume("hello"));
        assertTrue(reader.matchConsumeIgnoreCase("hello"));
        assertEquals("World", reader.toString());
        assertTrue(reader.matchConsume("World"));
        assertTrue(reader.isEmpty());
    }

    // Tests containsIgnoreCase method
    @Test
    public void testContainsIgnoreCase_presentAndAbsent_returnsExpectedBoolean() {
        CharacterReader reader = new CharacterReader("The quick brown Fox");
        assertTrue(reader.containsIgnoreCase("fox"));
        assertTrue(reader.containsIgnoreCase("FOX"));
        assertTrue(reader.containsIgnoreCase("QUICK"));
        assertFalse(reader.containsIgnoreCase("lazy"));
    }

    // Tests rangeEquals method and string caching boundary behavior
    @Test
    public void testRangeEquals_variousRangesAndStrings_returnsCorrectComparison() {
        CharacterReader reader = new CharacterReader("abcdef");
        assertTrue(reader.rangeEquals(0, 3, "abc"));
        assertFalse(reader.rangeEquals(0, 3, "abd"));
        assertFalse(reader.rangeEquals(0, 2, "abc"));
        assertTrue(reader.rangeEquals(3, 3, "def"));
        assertEquals("abcdef", reader.toString());
    }
}