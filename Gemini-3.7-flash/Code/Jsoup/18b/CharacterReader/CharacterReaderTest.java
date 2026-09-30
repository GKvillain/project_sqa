package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class CharacterReaderTest {

    // Tests null input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullInput_throwsException() {
        new CharacterReader(null);
    }

    // Tests consume characters and reaching EOF
    @Test
    public void testConsume_normalInput_consumesCharactersAndHitsEOF() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals(0, reader.pos());
        assertFalse(reader.isEmpty());
        assertEquals('a', reader.current());
        assertEquals('a', reader.consume());
        assertEquals('b', reader.consume());
        assertEquals('c', reader.consume());
        assertTrue(reader.isEmpty());
        assertEquals(CharacterReader.EOF, reader.current());
        assertEquals(CharacterReader.EOF, reader.consume());
    }

    // Tests unconsume and advance
    @Test
    public void testUnconsumeAndAdvance_validPosition_movesPosition() {
        CharacterReader reader = new CharacterReader("abc");
        reader.consume(); // 'a'
        assertEquals(1, reader.pos());
        reader.unconsume();
        assertEquals(0, reader.pos());
        assertEquals('a', reader.current());
        reader.advance();
        assertEquals(1, reader.pos());
        assertEquals('b', reader.current());
    }

    // Tests mark and rewind
    @Test
    public void testMarkAndRewindToMark_validMark_restoresPosition() {
        CharacterReader reader = new CharacterReader("abcdef");
        reader.consume(); // a
        reader.mark();
        reader.consume(); // b
        reader.consume(); // c
        assertEquals('d', reader.current());
        reader.rewindToMark();
        assertEquals('b', reader.current());
        assertEquals(1, reader.pos());
    }

    // Tests consumeToEnd to verify all characters including the last character are consumed
    @Test
    public void testConsumeToEnd_validString_consumesEntireRemaining() {
        CharacterReader reader = new CharacterReader("Hello world");
        reader.consume(); // 'H'
        String end = reader.consumeToEnd();
        assertEquals("ello world", end);
        assertTrue(reader.isEmpty());
    }

    // Tests consumeTo char when character is present
    @Test
    public void testConsumeTo_charPresent_consumesUpToChar() {
        CharacterReader reader = new CharacterReader("one;two;three");
        String part = reader.consumeTo(';');
        assertEquals("one", part);
        assertEquals(';', reader.current());
    }

    // Tests consumeTo char when character is not present, falls back to consumeToEnd
    @Test
    public void testConsumeTo_charAbsent_consumesToEnd() {
        CharacterReader reader = new CharacterReader("onetwothree");
        String part = reader.consumeTo(';');
        assertEquals("onetwothree", part);
        assertTrue(reader.isEmpty());
    }

    // Tests consumeTo string sequence when sequence is present
    @Test
    public void testConsumeTo_seqPresent_consumesUpToSeq() {
        CharacterReader reader = new CharacterReader("Hello <!-- comment --> world");
        String part = reader.consumeTo("<!--");
        assertEquals("Hello ", part);
        assertEquals('<', reader.current());
    }

    // Tests consumeTo string sequence when sequence is absent, falls back to consumeToEnd
    @Test
    public void testConsumeTo_seqAbsent_consumesToEnd() {
        CharacterReader reader = new CharacterReader("Hello world");
        String part = reader.consumeTo("missing");
        assertEquals("Hello world", part);
        assertTrue(reader.isEmpty());
    }

    // Tests consumeToAny characters
    @Test
    public void testConsumeToAny_matchesOneOfChars_consumesUpToMatch() {
        CharacterReader reader = new CharacterReader("foo&bar");
        String part = reader.consumeToAny('&', ';');
        assertEquals("foo", part);
        assertEquals('&', reader.current());

        CharacterReader reader2 = new CharacterReader("foo");
        String part2 = reader2.consumeToAny('&', ';');
        assertEquals("foo", part2);
        assertTrue(reader2.isEmpty());
    }

    // Tests consumeLetterSequence
    @Test
    public void testConsumeLetterSequence_mixedInput_consumesLettersOnly() {
        CharacterReader reader = new CharacterReader("HelloWorld123");
        String letters = reader.consumeLetterSequence();
        assertEquals("HelloWorld", letters);
        assertEquals('1', reader.current());

        CharacterReader nonLetter = new CharacterReader("123abc");
        assertEquals("", nonLetter.consumeLetterSequence());
    }

    // Tests consumeHexSequence
    @Test
    public void testConsumeHexSequence_mixedInput_consumesHexCharsOnly() {
        CharacterReader reader = new CharacterReader("12AFafg");
        String hex = reader.consumeHexSequence();
        assertEquals("12AFaf", hex);
        assertEquals('g', reader.current());
    }

    // Tests consumeDigitSequence
    @Test
    public void testConsumeDigitSequence_mixedInput_consumesDigitsOnly() {
        CharacterReader reader = new CharacterReader("12345abc");
        String digits = reader.consumeDigitSequence();
        assertEquals("12345", digits);
        assertEquals('a', reader.current());
    }

    // Tests matches char and string
    @Test
    public void testMatches_variousInputs_evaluatesCorrectly() {
        CharacterReader reader = new CharacterReader("Test string");
        assertTrue(reader.matches('T'));
        assertFalse(reader.matches('t'));
        assertTrue(reader.matches("Test"));
        assertFalse(reader.matches("String"));

        CharacterReader emptyReader = new CharacterReader("");
        assertFalse(emptyReader.matches('a'));
    }

    // Tests matchesIgnoreCase
    @Test
    public void testMatchesIgnoreCase_differentCase_returnsTrue() {
        CharacterReader reader = new CharacterReader("Title");
        assertTrue(reader.matchesIgnoreCase("title"));
        assertTrue(reader.matchesIgnoreCase("TITLE"));
        assertFalse(reader.matchesIgnoreCase("body"));
    }

    // Tests matchesAny
    @Test
    public void testMatchesAny_variousChars_evaluatesCorrectly() {
        CharacterReader reader = new CharacterReader("apple");
        assertTrue(reader.matchesAny('b', 'a', 'c'));
        assertFalse(reader.matchesAny('x', 'y', 'z'));

        CharacterReader emptyReader = new CharacterReader("");
        assertFalse(emptyReader.matchesAny('a'));
    }

    // Tests matchesLetter and matchesDigit
    @Test
    public void testMatchesLetterAndDigit_variousInputs_evaluatesCorrectly() {
        CharacterReader letterReader = new CharacterReader("Abc");
        assertTrue(letterReader.matchesLetter());
        assertFalse(letterReader.matchesDigit());

        CharacterReader digitReader = new CharacterReader("123");
        assertFalse(digitReader.matchesLetter());
        assertTrue(digitReader.matchesDigit());

        CharacterReader emptyReader = new CharacterReader("");
        assertFalse(emptyReader.matchesLetter());
        assertFalse(emptyReader.matchesDigit());
    }

    // Tests matchConsume and matchConsumeIgnoreCase
    @Test
    public void testMatchConsume_caseSensitiveAndInsensitive_consumesOnMatch() {
        CharacterReader reader = new CharacterReader("Hello World");
        assertFalse(reader.matchConsume("hello"));
        assertEquals(0, reader.pos());

        assertTrue(reader.matchConsume("Hello"));
        assertEquals(5, reader.pos());

        assertTrue(reader.matchConsumeIgnoreCase(" world"));
        assertEquals(11, reader.pos());
        assertTrue(reader.isEmpty());
    }

    // Tests containsIgnoreCase
    @Test
    public void testContainsIgnoreCase_variousCasing_evaluatesCorrectly() {
        CharacterReader reader = new CharacterReader("<TITLE>Test</TITLE>");
        assertTrue(reader.containsIgnoreCase("</title>"));
        assertTrue(reader.containsIgnoreCase("</TITLE>"));
        assertFalse(reader.containsIgnoreCase("</style>"));
    }

    // Tests toString returns remaining string
    @Test
    public void testToString_validInput_returnsRemainingCharacters() {
        CharacterReader reader = new CharacterReader("abcdef");
        reader.consume(); // 'a'
        reader.consume(); // 'b'
        assertEquals("cdef", reader.toString());
    }
}