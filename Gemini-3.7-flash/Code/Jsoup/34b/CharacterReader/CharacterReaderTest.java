package org.jsoup.parser;

import org.junit.Test;

import static org.junit.Assert.*;

public class CharacterReaderTest {

    // Tests null input to constructor throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullInput_throwsException() {
        new CharacterReader(null);
    }

    // Tests basic cursor operations and boundary state
    @Test
    public void testCursorOperations_normalString_tracksPositionAndState() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals(0, reader.pos());
        assertFalse(reader.isEmpty());
        assertEquals('a', reader.current());

        assertEquals('a', reader.consume());
        assertEquals(1, reader.pos());
        assertEquals('b', reader.current());

        reader.advance();
        assertEquals(2, reader.pos());
        assertEquals('c', reader.current());

        reader.unconsume();
        assertEquals(1, reader.pos());
        assertEquals('b', reader.current());

        reader.mark();
        reader.advance();
        reader.advance();
        assertTrue(reader.isEmpty());
        assertEquals(CharacterReader.EOF, reader.current());
        assertEquals(CharacterReader.EOF, reader.consume());

        reader.rewindToMark();
        assertEquals(1, reader.pos());
        assertEquals('b', reader.current());
    }

    // Tests consumeAsString method
    @Test
    public void testConsumeAsString_validInput_returnsSingleCharacterString() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals("a", reader.consumeAsString());
        assertEquals("b", reader.consumeAsString());
        assertEquals(2, reader.pos());
    }

    // Tests nextIndexOf char target
    @Test
    public void testNextIndexOfChar_targetPresentAndAbsent_returnsCorrectOffset() {
        CharacterReader reader = new CharacterReader("abcdef");
        assertEquals(2, reader.nextIndexOf('c'));
        assertEquals(-1, reader.nextIndexOf('z'));

        reader.consume();
        reader.consume();
        assertEquals(0, reader.nextIndexOf('c'));
    }

    // Tests nextIndexOf CharSequence defect with boundary matching where target extends past input
    @Test
    public void testNextIndexOfCharSequence_partialMatchAtEnd_returnsMinusOne() {
        CharacterReader reader = new CharacterReader("test [CDATA[]");
        assertEquals(-1, reader.nextIndexOf("]]>"));
    }

    // Tests nextIndexOf CharSequence for present and absent target
    @Test
    public void testNextIndexOfCharSequence_targetPresentAndAbsent_returnsCorrectOffset() {
        CharacterReader reader = new CharacterReader("hello world");
        assertEquals(6, reader.nextIndexOf("world"));
        assertEquals(-1, reader.nextIndexOf("word"));
    }

    // Tests consumeTo char
    @Test
    public void testConsumeToChar_presentAndAbsentChar_consumesCorrectly() {
        CharacterReader reader = new CharacterReader("one;two;three");
        assertEquals("one", reader.consumeTo(';'));
        assertEquals(';', reader.consume());
        assertEquals("two", reader.consumeTo(';'));
        assertEquals(';', reader.consume());
        assertEquals("three", reader.consumeTo(';'));
        assertTrue(reader.isEmpty());
    }

    // Tests consumeTo string sequence
    @Test
    public void testConsumeToString_presentAndAbsentSequence_consumesCorrectly() {
        CharacterReader reader = new CharacterReader("<!-- comment -->after");
        assertEquals("<!-- comment ", reader.consumeTo("-->"));
        assertTrue(reader.matchConsume("-->"));
        assertEquals("after", reader.consumeTo("-->"));
    }

    // Tests consumeToAny
    @Test
    public void testConsumeToAny_variousDelimiters_consumesUpToDelimiter() {
        CharacterReader reader = new CharacterReader("foo & bar < baz");
        assertEquals("foo ", reader.consumeToAny('&', '<'));
        assertEquals('&', reader.consume());
        assertEquals(" bar ", reader.consumeToAny('&', '<'));
        assertEquals('<', reader.consume());
        assertEquals(" baz", reader.consumeToAny('&', '<'));
        assertEquals("", reader.consumeToAny('&', '<'));
    }

    // Tests consumeToEnd
    @Test
    public void testConsumeToEnd_partialConsumed_returnsRemaining() {
        CharacterReader reader = new CharacterReader("abcdef");
        reader.advance();
        reader.advance();
        assertEquals("cdef", reader.consumeToEnd());
        assertTrue(reader.isEmpty());
    }

    // Tests consumeLetterSequence
    @Test
    public void testConsumeLetterSequence_mixedInput_consumesOnlyLetters() {
        CharacterReader reader = new CharacterReader("abcDEF123ghi");
        assertEquals("abcDEF", reader.consumeLetterSequence());
        assertEquals("123", reader.consumeDigitSequence());
        assertEquals("ghi", reader.consumeLetterSequence());
    }

    // Tests consumeLetterThenDigitSequence
    @Test
    public void testConsumeLetterThenDigitSequence_letterAndDigits_consumesExpectedPrefix() {
        CharacterReader reader = new CharacterReader("var123 = true; 456");
        assertEquals("var123", reader.consumeLetterThenDigitSequence());
        assertEquals(" = true; ", reader.consumeToAny('0', '1', '2', '3', '4', '5', '6', '7', '8', '9'));
        assertEquals("456", reader.consumeLetterThenDigitSequence());
    }

    // Tests consumeHexSequence
    @Test
    public void testConsumeHexSequence_hexCharacters_consumesValidHexOnly() {
        CharacterReader reader = new CharacterReader("0123456789ABCDEFabcdefGHI");
        assertEquals("0123456789ABCDEFabcdef", reader.consumeHexSequence());
        assertEquals("GHI", reader.consumeToEnd());
    }

    // Tests matches char and string
    @Test
    public void testMatches_charAndString_evaluatesCorrectly() {
        CharacterReader reader = new CharacterReader("Hello World");
        assertTrue(reader.matches('H'));
        assertFalse(reader.matches('h'));
        assertTrue(reader.matches("Hello"));
        assertFalse(reader.matches("hello"));
        assertFalse(reader.matches("Hello World Longer Than Input"));

        reader.consumeToEnd();
        assertFalse(reader.matches('a'));
        assertFalse(reader.matches("a"));
    }

    // Tests matchesIgnoreCase
    @Test
    public void testMatchesIgnoreCase_varyingCase_returnsTrue() {
        CharacterReader reader = new CharacterReader("Hello World");
        assertTrue(reader.matchesIgnoreCase("hello"));
        assertTrue(reader.matchesIgnoreCase("HELLO"));
        assertFalse(reader.matchesIgnoreCase("world"));
        assertFalse(reader.matchesIgnoreCase("hello world longer"));
    }

    // Tests matchesAny, matchesLetter, matchesDigit
    @Test
    public void testMatchesTypeChecks_variousCharacters_evaluatesCorrectly() {
        CharacterReader reader = new CharacterReader("A1!");
        assertTrue(reader.matchesAny('X', 'A', 'Z'));
        assertFalse(reader.matchesAny('X', 'Y', 'Z'));
        assertTrue(reader.matchesLetter());
        assertFalse(reader.matchesDigit());

        reader.advance();
        assertFalse(reader.matchesLetter());
        assertTrue(reader.matchesDigit());

        reader.advance();
        assertFalse(reader.matchesLetter());
        assertFalse(reader.matchesDigit());

        reader.advance();
        assertFalse(reader.matchesAny('!'));
        assertFalse(reader.matchesLetter());
        assertFalse(reader.matchesDigit());
    }

    // Tests matchConsume and matchConsumeIgnoreCase
    @Test
    public void testMatchConsume_matchingAndNonMatching_advancesOnlyOnMatch() {
        CharacterReader reader = new CharacterReader("one TWO three");
        assertFalse(reader.matchConsume("ONE"));
        assertEquals(0, reader.pos());

        assertTrue(reader.matchConsumeIgnoreCase("ONE"));
        assertEquals(3, reader.pos());

        assertEquals(' ', reader.consume());
        assertTrue(reader.matchConsume("TWO"));
        assertEquals(7, reader.pos());
    }

    // Tests containsIgnoreCase
    @Test
    public void testContainsIgnoreCase_presentAndAbsentTargets_identifiesCorrectly() {
        CharacterReader reader = new CharacterReader("<html><TITLE>Test</title></html>");
        assertTrue(reader.containsIgnoreCase("title"));
        assertTrue(reader.containsIgnoreCase("TITLE"));
        assertFalse(reader.containsIgnoreCase("body"));
    }

    // Tests toString representation
    @Test
    public void testToString_partiallyConsumed_returnsRemainingString() {
        CharacterReader reader = new CharacterReader("abcdef");
        assertEquals("abcdef", reader.toString());
        reader.consume();
        reader.consume();
        assertEquals("cdef", reader.toString());
    }
}