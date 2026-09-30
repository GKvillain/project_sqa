package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class CharacterReaderTest {

    @Test(expected = NullPointerException.class)
    public void testConstructor_nullInput_throwsNullPointerException() {
        new CharacterReader(null);
    }

    @Test
    public void testCurrent_emptyInput_returnsEOF() {
        CharacterReader reader = new CharacterReader("");
        assertEquals(CharacterReader.EOF, reader.current());
    }

    @Test
    public void testConsume_emptyInput_returnsEOF() {
        CharacterReader reader = new CharacterReader("");
        assertEquals(CharacterReader.EOF, reader.consume());
    }

    @Test
    public void testConsumeToEnd_normalInput_returnsFullString() {
        CharacterReader reader = new CharacterReader("Hello");
        String result = reader.consumeToEnd();
        assertEquals("Hello", result);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeToEnd_emptyInput_returnsEmptyString() {
        CharacterReader reader = new CharacterReader("");
        String result = reader.consumeToEnd();
        assertEquals("", result);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeTo_charFound_consumesUpToChar() {
        CharacterReader reader = new CharacterReader("abcd");
        String result = reader.consumeTo('c');
        assertEquals("ab", result);
        assertEquals('c', reader.current());
    }

    @Test
    public void testConsumeTo_charNotFound_consumesToEnd() {
        CharacterReader reader = new CharacterReader("Hello");
        String result = reader.consumeTo('x');
        assertEquals("Hello", result);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeTo_seqFound_consumesUpToSeq() {
        CharacterReader reader = new CharacterReader("abcdef-xyz");
        String result = reader.consumeTo("xyz");
        assertEquals("abcdef-", result);
        assertEquals('x', reader.current());
    }

    @Test
    public void testConsumeToAny_matchAtStart_returnsEmpty() {
        CharacterReader reader = new CharacterReader("abcdef");
        String result = reader.consumeToAny('a', 'b');
        assertEquals("", result);
        assertEquals('a', reader.current());
    }

    @Test
    public void testConsumeAsString_singleChar_returnsThatChar() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals("a", reader.consumeAsString());
        assertEquals('b', reader.current());
    }

    @Test
    public void testConsumeAsString_multipleChars_returnsEachChar() {
        CharacterReader reader = new CharacterReader("xy");
        assertEquals("x", reader.consumeAsString());
        assertEquals("y", reader.consumeAsString());
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testMatchesIgnoreCase_caseInsensitive_returnsTrue() {
        CharacterReader reader = new CharacterReader("Hello");
        assertTrue(reader.matchesIgnoreCase("hello"));
    }

    @Test
    public void testMatchesIgnoreCase_caseSensitive_returnsFalse() {
        CharacterReader reader = new CharacterReader("Hello");
        assertFalse(reader.matchesIgnoreCase("world"));
    }

    @Test
    public void testMatchConsume_seqMatches_updatesPosAndReturnsTrue() {
        CharacterReader reader = new CharacterReader("abcxyz");
        assertTrue(reader.matchConsume("abc"));
        assertEquals('x', reader.current());
    }

    @Test
    public void testMatchConsume_seqNotMatch_keepsPosAndReturnsFalse() {
        CharacterReader reader = new CharacterReader("abcxyz");
        assertFalse(reader.matchConsume("abx"));
        assertEquals('a', reader.current());
    }

    @Test
    public void testMatchesLetter_letterChar_returnsTrue() {
        CharacterReader reader = new CharacterReader("a");
        assertTrue(reader.matchesLetter());
    }

    @Test
    public void testMatchesLetter_nonLetter_returnsFalse() {
        CharacterReader reader = new CharacterReader("1");
        assertFalse(reader.matchesLetter());
    }

    @Test
    public void testMatchesDigit_digitChar_returnsTrue() {
        CharacterReader reader = new CharacterReader("5");
        assertTrue(reader.matchesDigit());
    }

    @Test
    public void testContainsIgnoreCase_caseInsensitiveFound_returnsTrue() {
        CharacterReader reader = new CharacterReader("Some Text </TITLE>");
        assertTrue(reader.containsIgnoreCase("</title>"));
    }

    @Test
    public void testRewindToMark_afterConsume_restoresPosition() {
        CharacterReader reader = new CharacterReader("abcdef");
        reader.consume(); // a consumed, pos=1
        reader.consume(); // b consumed, pos=2
        reader.mark();    // mark at pos=2
        reader.consume(); // c consumed, pos=3
        reader.rewindToMark(); // pos=2
        assertEquals('c', reader.current());
        reader.consume(); // c consumed again
        assertEquals('d', reader.current());
    }

    @Test
    public void testCurrent_normalInput_returnsFirstChar() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals('a', reader.current());
    }

    @Test
    public void testConsume_normalInput_returnsFirstCharAndAdvances() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals('a', reader.consume());
        assertEquals('b', reader.current());
    }

    @Test
    public void testConsumeToCharAtEnd_returnsRemainingString() {
        CharacterReader reader = new CharacterReader("abc");
        reader.consume();
        reader.consume();
        reader.consume();
        String result = reader.consumeTo('x');
        assertEquals("", result);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeTo_seqNotFound_consumesToEnd() {
        CharacterReader reader = new CharacterReader("abcdef");
        String result = reader.consumeTo("xyz");
        assertEquals("abcdef", result);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeTo_emptySeq_returnsEmptyString() {
        CharacterReader reader = new CharacterReader("abc");
        String result = reader.consumeTo("");
        assertEquals("", result);
        assertEquals('a', reader.current());
    }

    @Test
    public void testConsumeToAny_noMatch_consumesAll() {
        CharacterReader reader = new CharacterReader("abcdef");
        String result = reader.consumeToAny('x', 'y', 'z');
        assertEquals("abcdef", result);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeToAny_matchInMiddle_consumesUpToMatch() {
        CharacterReader reader = new CharacterReader("abcdef");
        String result = reader.consumeToAny('c', 'd');
        assertEquals("ab", result);
        assertEquals('c', reader.current());
    }

    @Test
    public void testConsumeAsString_emptyInput_returnsEmptyString() {
        CharacterReader reader = new CharacterReader("");
        assertEquals("", reader.consumeAsString());
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testMatches_emptySeq_returnsTrue() {
        CharacterReader reader = new CharacterReader("abc");
        assertTrue(reader.matches(""));
    }

    @Test
    public void testMatches_matchingSeq_returnsTrue() {
        CharacterReader reader = new CharacterReader("abc");
        assertTrue(reader.matches("ab"));
    }

    @Test
    public void testMatches_nonMatchingSeq_returnsFalse() {
        CharacterReader reader = new CharacterReader("abc");
        assertFalse(reader.matches("ac"));
    }

    @Test
    public void testMatchesIgnoreCase_emptySeq_returnsTrue() {
        CharacterReader reader = new CharacterReader("abc");
        assertTrue(reader.matchesIgnoreCase(""));
    }

    @Test
    public void testMatchesIgnoreCase_longerSeq_returnsFalse() {
        CharacterReader reader = new CharacterReader("abc");
        assertFalse(reader.matchesIgnoreCase("abcd"));
    }

    @Test
    public void testMatchConsume_emptySeq_returnsTrueAndNoProgress() {
        CharacterReader reader = new CharacterReader("abc");
        assertTrue(reader.matchConsume(""));
        assertEquals('a', reader.current());
    }

    @Test
    public void testMatchConsume_shorterSeq_returnsFalse() {
        CharacterReader reader = new CharacterReader("abc");
        assertFalse(reader.matchConsume("abcde"));
        assertEquals('a', reader.current());
    }

    @Test
    public void testMatchesLetter_uppercase_returnsTrue() {
        CharacterReader reader = new CharacterReader("A");
        assertTrue(reader.matchesLetter());
    }

    @Test
    public void testMatchesLetter_atEnd_returnsFalse() {
        CharacterReader reader = new CharacterReader("ab");
        reader.consume();
        reader.consume();
        assertFalse(reader.matchesLetter());
    }

    @Test
    public void testMatchesDigit_nonDigit_returnsFalse() {
        CharacterReader reader = new CharacterReader("a");
        assertFalse(reader.matchesDigit());
    }

    @Test
    public void testMatchesDigit_atEnd_returnsFalse() {
        CharacterReader reader = new CharacterReader("12");
        reader.consume();
        reader.consume();
        assertFalse(reader.matchesDigit());
    }

    @Test
    public void testContainsIgnoreCase_caseSensitiveNotFound_returnsFalse() {
        CharacterReader reader = new CharacterReader("Some Text </TITLE>");
        assertFalse(reader.containsIgnoreCase("</body>"));
    }

    @Test
    public void testContainsIgnoreCase_atStart_returnsTrue() {
        CharacterReader reader = new CharacterReader("abcdef");
        assertTrue(reader.containsIgnoreCase("abc"));
    }

    @Test
    public void testMark_afterMark_stillWorks() {
        CharacterReader reader = new CharacterReader("abcdef");
        reader.mark();
        reader.consume();
        reader.consume();
        reader.rewindToMark();
        assertEquals('a', reader.current());
    }

    @Test
    public void testRewindToMark_withoutMark_doesNothing() {
        CharacterReader reader = new CharacterReader("abcdef");
        reader.consume();
        reader.rewindToMark();
        assertEquals('b', reader.current());
    }

    @Test
    public void testIsEmpty_onInitialInput_returnsFalse() {
        CharacterReader reader = new CharacterReader("abc");
        assertFalse(reader.isEmpty());
    }

    @Test
    public void testIsEmpty_atEnd_returnsTrue() {
        CharacterReader reader = new CharacterReader("a");
        reader.consume();
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConstructor_afterConsumeAll_returnsEOF() {
        CharacterReader reader = new CharacterReader("a");
        reader.consume();
        assertEquals(CharacterReader.EOF, reader.current());
    }

    @Test
    public void testConsumeToChar_withEOF_returnsEmptyString() {
        CharacterReader reader = new CharacterReader("");
        String result = reader.consumeTo('a');
        assertEquals("", result);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeToAny_withEOF_returnsEmptyString() {
        CharacterReader reader = new CharacterReader("");
        String result = reader.consumeToAny('a', 'b');
        assertEquals("", result);
        assertTrue(reader.isEmpty());
    }
}