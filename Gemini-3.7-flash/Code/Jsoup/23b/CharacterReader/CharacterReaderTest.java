package org.jsoup.parser;

import org.junit.Test;

import static org.junit.Assert.*;

public class CharacterReaderTest {

    // Tests null input throwing IllegalArgumentException via Validate.notNull
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullInput_throwsException() {
        new CharacterReader(null);
    }

    // Tests newline normalisation (\r\n and \r to \n)
    @Test
    public void testConstructor_normalisesNewlines_convertsToLf() {
        CharacterReader reader = new CharacterReader("a\r\nb\rc\n");
        assertEquals("a\nb\nc\n", reader.toString());
    }

    // Tests consume and current behavior on normal string and EOF
    @Test
    public void testConsumeAndCurrent_normalInput_advancesAndDetectsEof() {
        CharacterReader reader = new CharacterReader("ab");
        assertEquals('a', reader.current());
        assertEquals('a', reader.consume());
        assertEquals(1, reader.pos());
        assertFalse(reader.isEmpty());
        assertEquals('b', reader.current());
        assertEquals('b', reader.consume());
        assertTrue(reader.isEmpty());
        assertEquals(CharacterReader.EOF, reader.current());
        assertEquals(CharacterReader.EOF, reader.consume());
    }

    // Tests unconsume and advance operations
    @Test
    public void testUnconsumeAndAdvance_movePosition_adjustsCorrectly() {
        CharacterReader reader = new CharacterReader("abc");
        reader.advance();
        assertEquals(1, reader.pos());
        assertEquals('b', reader.current());
        reader.unconsume();
        assertEquals(0, reader.pos());
        assertEquals('a', reader.current());
    }

    // Tests mark and rewindToMark functionality
    @Test
    public void testMarkAndRewindToMark_validSequence_restoresPosition() {
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

    // Tests consumeTo with a character delimiter
    @Test
    public void testConsumeToChar_foundAndNotFound_consumesExpectedString() {
        CharacterReader reader = new CharacterReader("one;two;three");
        String part1 = reader.consumeTo(';');
        assertEquals("one", part1);
        assertEquals(';', reader.current());
        reader.consume(); // consume ';'

        CharacterReader reader2 = new CharacterReader("notFound");
        String all = reader2.consumeTo(';');
        assertEquals("notFound", all);
        assertTrue(reader2.isEmpty());
    }

    // Tests consumeTo with a string delimiter
    @Test
    public void testConsumeToString_foundAndNotFound_consumesExpectedString() {
        CharacterReader reader = new CharacterReader("one-->two-->three");
        String part1 = reader.consumeTo("-->");
        assertEquals("one", part1);
        assertTrue(reader.matches("-->"));

        CharacterReader reader2 = new CharacterReader("noMatch");
        String all = reader2.consumeTo("-->");
        assertEquals("noMatch", all);
        assertTrue(reader2.isEmpty());
    }

    // Tests consumeToAny with variable character arguments
    @Test
    public void testConsumeToAny_multipleDelimiters_consumesUntilFirstMatch() {
        CharacterReader reader = new CharacterReader("foo&bar<baz");
        String part = reader.consumeToAny('&', '<');
        assertEquals("foo", part);
        assertEquals('&', reader.current());

        CharacterReader emptyReader = new CharacterReader("");
        assertEquals("", emptyReader.consumeToAny('a', 'b'));
    }

    // Tests consumeToEnd method
    @Test
    public void testConsumeToEnd_partialAndFull_consumesRemaining() {
        CharacterReader reader = new CharacterReader("hello world");
        reader.consume(); // skip 'h'
        String remainder = reader.consumeToEnd();
        assertEquals("ello world", remainder);
        assertTrue(reader.isEmpty());
        assertEquals("", reader.consumeToEnd());
    }

    // Tests consumeLetterSequence
    @Test
    public void testConsumeLetterSequence_mixedInput_consumesLettersOnly() {
        CharacterReader reader = new CharacterReader("ValidLetterSeq123");
        String letters = reader.consumeLetterSequence();
        assertEquals("ValidLetterSeq", letters);
        assertEquals('1', reader.current());

        CharacterReader nonLetterReader = new CharacterReader("123abc");
        assertEquals("", nonLetterReader.consumeLetterSequence());
    }

    // Tests consumeHexSequence
    @Test
    public void testConsumeHexSequence_hexAndNonHex_consumesHexOnly() {
        CharacterReader reader = new CharacterReader("0123456789ABCDEFabcdefGHI");
        String hex = reader.consumeHexSequence();
        assertEquals("0123456789ABCDEFabcdef", hex);
        assertEquals('G', reader.current());

        CharacterReader emptyReader = new CharacterReader("");
        assertEquals("", emptyReader.consumeHexSequence());
    }

    // Tests consumeDigitSequence
    @Test
    public void testConsumeDigitSequence_digitsAndNonDigits_consumesDigitsOnly() {
        CharacterReader reader = new CharacterReader("12345abc");
        String digits = reader.consumeDigitSequence();
        assertEquals("12345", digits);
        assertEquals('a', reader.current());

        CharacterReader noDigitReader = new CharacterReader("abc");
        assertEquals("", noDigitReader.consumeDigitSequence());
    }

    // Tests matches with char and String
    @Test
    public void testMatches_charAndString_matchesExactPrefix() {
        CharacterReader reader = new CharacterReader("test string");
        assertTrue(reader.matches('t'));
        assertFalse(reader.matches('e'));
        assertTrue(reader.matches("test"));
        assertFalse(reader.matches("testing"));

        CharacterReader emptyReader = new CharacterReader("");
        assertFalse(emptyReader.matches('a'));
        assertFalse(emptyReader.matches("a"));
    }

    // Tests matchesIgnoreCase
    @Test
    public void testMatchesIgnoreCase_variousCases_returnsTrue() {
        CharacterReader reader = new CharacterReader("TeSt StRiNg");
        assertTrue(reader.matchesIgnoreCase("test"));
        assertTrue(reader.matchesIgnoreCase("TEST"));
        assertFalse(reader.matchesIgnoreCase("toast"));
    }

    // Tests matchesAny
    @Test
    public void testMatchesAny_matchingAndNonMatchingChars_returnsCorrectBoolean() {
        CharacterReader reader = new CharacterReader("target");
        assertTrue(reader.matchesAny('x', 'y', 't'));
        assertFalse(reader.matchesAny('x', 'y', 'z'));

        CharacterReader emptyReader = new CharacterReader("");
        assertFalse(emptyReader.matchesAny('a', 'b'));
    }

    // Tests matchesLetter and matchesDigit
    @Test
    public void testMatchesLetterAndMatchesDigit_variousChars_correctClassification() {
        CharacterReader letterReader = new CharacterReader("aZ1");
        assertTrue(letterReader.matchesLetter());
        assertFalse(letterReader.matchesDigit());

        letterReader.advance(); // 'Z'
        assertTrue(letterReader.matchesLetter());
        assertFalse(letterReader.matchesDigit());

        letterReader.advance(); // '1'
        assertFalse(letterReader.matchesLetter());
        assertTrue(letterReader.matchesDigit());

        letterReader.advance(); // EOF
        assertFalse(letterReader.matchesLetter());
        assertFalse(letterReader.matchesDigit());
    }

    // Tests matchConsume
    @Test
    public void testMatchConsume_matchingAndNonMatching_advancesOnlyOnMatch() {
        CharacterReader reader = new CharacterReader("abcdef");
        assertTrue(reader.matchConsume("abc"));
        assertEquals(3, reader.pos());
        assertFalse(reader.matchConsume("xyz"));
        assertEquals(3, reader.pos());
    }

    // Tests matchConsumeIgnoreCase
    @Test
    public void testMatchConsumeIgnoreCase_caseInsensitiveMatch_advancesOnMatch() {
        CharacterReader reader = new CharacterReader("ABCdef");
        assertTrue(reader.matchConsumeIgnoreCase("abc"));
        assertEquals(3, reader.pos());
        assertFalse(reader.matchConsumeIgnoreCase("xyz"));
        assertEquals(3, reader.pos());
    }

    // Tests containsIgnoreCase
    @Test
    public void testContainsIgnoreCase_uppercaseAndLowercase_findsPresence() {
        CharacterReader reader = new CharacterReader("<html><head><TITLE>Test</TITLE></head></html>");
        assertTrue(reader.containsIgnoreCase("title"));
        assertTrue(reader.containsIgnoreCase("TITLE"));
        assertFalse(reader.containsIgnoreCase("body"));

        reader.consumeTo("Test");
        assertFalse(reader.containsIgnoreCase("<head>"));
    }

    // Tests toString returning remaining substring
    @Test
    public void testToString_validReader_returnsRemainingString() {
        CharacterReader reader = new CharacterReader("remainder text");
        reader.consumeTo(' ');
        reader.consume(); // consume space
        assertEquals("text", reader.toString());
    }
}