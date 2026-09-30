package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.StringReader;

public class CharacterReaderTest {

    // Tests constructor and basic navigation methods: isEmpty, current, consume, advance, unconsume, mark, rewindToMark
    @Test
    public void testConstructorAndBasicNavigation() {
        CharacterReader reader = new CharacterReader("abc");
        assertFalse(reader.isEmpty());
        assertEquals('a', reader.current());
        assertEquals('a', reader.consume());
        assertEquals('b', reader.current());
        reader.advance();
        assertEquals('c', reader.current());
        reader.unconsume();
        assertEquals('b', reader.current());
        reader.mark();
        reader.advance();
        assertEquals('c', reader.current());
        reader.rewindToMark();
        assertEquals('b', reader.current());
    }

    // Tests nextIndexOf(char) for found and not found cases
    @Test
    public void testNextIndexOfChar_foundAndNotFound() {
        CharacterReader reader = new CharacterReader("hello world");
        assertEquals(1, reader.nextIndexOf('e'));
        assertEquals(0, reader.nextIndexOf('h'));
        assertEquals(-1, reader.nextIndexOf('z'));
        reader.consume(); // consume 'h'
        assertEquals(0, reader.nextIndexOf('e')); // now at 'e'
        assertEquals(-1, reader.nextIndexOf('h')); // not found after current
    }

    // Tests nextIndexOf(CharSequence) for found and not found cases
    @Test
    public void testNextIndexOfSequence_foundAndNotFound() {
        CharacterReader reader = new CharacterReader("hello world");
        assertEquals(0, reader.nextIndexOf("he"));
        assertEquals(1, reader.nextIndexOf("el"));
        assertEquals(-1, reader.nextIndexOf("xyz"));
        assertEquals(-1, reader.nextIndexOf("worldx")); // too long
    }

    // Tests consumeTo(char) when char is found
    @Test
    public void testConsumeToChar_found_consumesToChar() {
        CharacterReader reader = new CharacterReader("abc?def");
        String consumed = reader.consumeTo('?');
        assertEquals("abc", consumed);
        assertEquals('?', reader.current());
    }

    // Tests consumeTo(char) when char is not found, should consume to end
    @Test
    public void testConsumeToChar_notFound_consumesToEnd() {
        CharacterReader reader = new CharacterReader("abcdef");
        String consumed = reader.consumeTo('z');
        assertEquals("abcdef", consumed);
        assertTrue(reader.isEmpty());
    }

    // Tests consumeTo(String) when sequence is found
    @Test
    public void testConsumeToSequence_found_consumesToSequence() {
        CharacterReader reader = new CharacterReader("beforeENDafter");
        String consumed = reader.consumeTo("END");
        assertEquals("before", consumed);
        assertEquals('E', reader.current());
    }

    // Tests consumeToAny(char...) when one of the chars is found
    @Test
    public void testConsumeToAny_found_consumesToFirstMatch() {
        CharacterReader reader = new CharacterReader("abcd!efg");
        String consumed = reader.consumeToAny('!', '?');
        assertEquals("abcd", consumed);
        assertEquals('!', reader.current());
    }

    // Tests consumeToEnd consumes remaining characters and isEmpty becomes true
    @Test
    public void testConsumeToEnd_consumesAll() {
        CharacterReader reader = new CharacterReader("hello");
        String consumed = reader.consumeToEnd();
        assertEquals("hello", consumed);
        assertTrue(reader.isEmpty());
    }

    // Tests matches(char) for true and false cases
    @Test
    public void testMatchesChar_trueAndFalse() {
        CharacterReader reader = new CharacterReader("abc");
        assertTrue(reader.matches('a'));
        assertFalse(reader.matches('b'));
        reader.advance();
        assertTrue(reader.matches('b'));
    }

    // Tests matches(String) for true and false cases (full match required)
    @Test
    public void testMatchesString_trueAndFalse() {
        CharacterReader reader = new CharacterReader("hello");
        assertTrue(reader.matches("hello"));
        assertFalse(reader.matches("hell")); // full match required
        assertFalse(reader.matches("x"));
    }

    // Tests matchConsume(String) for true (advances position) and false (does not advance)
    @Test
    public void testMatchConsume_trueAndFalse() {
        CharacterReader reader = new CharacterReader("hello");
        assertTrue(reader.matchConsume("hel"));
        assertEquals(3, reader.pos());
        assertEquals('l', reader.current());
        assertFalse(reader.matchConsume("xyz"));
        assertEquals(3, reader.pos()); // unchanged
    }

    // Tests matchesIgnoreCase(String) for true case
    @Test
    public void testMatchesIgnoreCase_true() {
        CharacterReader reader = new CharacterReader("Hello World");
        assertTrue(reader.matchesIgnoreCase("hello"));
        assertFalse(reader.matchesIgnoreCase("world")); // starts with H not W
    }

    // Tests consumeLetterSequence consumes letters until non-letter
    @Test
    public void testConsumeLetterSequence_consumesLetters() {
        CharacterReader reader = new CharacterReader("abc123");
        String letters = reader.consumeLetterSequence();
        assertEquals("abc", letters);
        assertEquals('1', reader.current());
    }

    // Tests consumeDigitSequence consumes digits until non-digit
    @Test
    public void testConsumeDigitSequence_consumesDigits() {
        CharacterReader reader = new CharacterReader("123abc");
        String digits = reader.consumeDigitSequence();
        assertEquals("123", digits);
        assertEquals('a', reader.current());
    }

    // Tests consumeHexSequence consumes hexadecimal digits until non-hex
    @Test
    public void testConsumeHexSequence_consumesHex() {
        CharacterReader reader = new CharacterReader("a1b2x");
        String hex = reader.consumeHexSequence();
        assertEquals("a1b2", hex);
        assertEquals('x', reader.current());
    }

    // Tests bufferUp triggers when bufPos exceeds bufSplitPoint, and pos() remains correct after bufferUp
    @Test
    public void testBufferUp_triggered_whenBufPosExceedsSplitPoint() {
        StringBuilder sb = new StringBuilder(100);
        for (int i = 0; i < 100; i++) {
            sb.append('x');
        }
        String input = sb.toString();
        CharacterReader reader = new CharacterReader(new StringReader(input), 100);
        // bufSplitPoint = min(bufLength(100), readAheadLimit(75)) = 75
        // consume 76 chars to cross bufSplitPoint
        for (int i = 0; i < 76; i++) {
            reader.consume();
        }
        // next bufferUp will be triggered by current()
        char c = reader.current();
        assertEquals('x', c);
        assertEquals(76, reader.pos());
    }

    // Tests static rangeEquals method for true, false due to content mismatch, and false due to length mismatch
    @Test
    public void testRangeEquals_trueAndFalse() {
        char[] buf = "hello".toCharArray();
        assertTrue(CharacterReader.rangeEquals(buf, 0, 5, "hello"));
        assertFalse(CharacterReader.rangeEquals(buf, 0, 5, "world"));
        assertFalse(CharacterReader.rangeEquals(buf, 0, 4, "hell"));
    }

    // Tests unconsume at the start of input is a safe no-op
    @Test
    public void testUnconsumeAtStart_isNoOp() {
        CharacterReader reader = new CharacterReader("abc");
        reader.unconsume();
        assertEquals(0, reader.pos());
        assertEquals('a', reader.current());
    }

    // Tests advance to end of input and reports empty
    @Test
    public void testAdvanceToEnd_reportsEmpty() {
        CharacterReader reader = new CharacterReader("a");
        reader.advance();
        assertTrue(reader.isEmpty());
    }

    // Tests consumeToAny when no delimiter is present
    @Test
    public void testConsumeToAny_notFound_consumesToEnd() {
        CharacterReader reader = new CharacterReader("abcdef");
        assertEquals("abcdef", reader.consumeToAny('!', '?'));
        assertTrue(reader.isEmpty());
    }

    // Tests consumeToAny when current char is a delimiter returns empty
    @Test
    public void testConsumeToAny_currentIsDelimiter_returnsEmpty() {
        CharacterReader reader = new CharacterReader("!abc");
        assertEquals("", reader.consumeToAny('!', '?'));
        assertEquals('!', reader.current());
    }

    // Tests consumeTo(char) when current char matches returns empty
    @Test
    public void testConsumeToChar_currentMatches_returnsEmpty() {
        CharacterReader reader = new CharacterReader("?abc");
        assertEquals("", reader.consumeTo('?'));
        assertEquals('?', reader.current());
    }

    // Tests consumeTo(String) when sequence is absent consumes to end
    @Test
    public void testConsumeToSequence_notFound_consumesToEnd() {
        CharacterReader reader = new CharacterReader("abcdef");
        assertEquals("abcdef", reader.consumeTo("xyz"));
        assertTrue(reader.isEmpty());
    }

    // Tests consumeTo(String) when current position starts the sequence returns empty
    @Test
    public void testConsumeToSequence_currentMatches_returnsEmpty() {
        CharacterReader reader = new CharacterReader("ENDabc");
        assertEquals("", reader.consumeTo("END"));
        assertEquals('E', reader.current());
    }

    // Tests consumeToEnd after some characters have already been consumed
    @Test
    public void testConsumeToEnd_afterPartialConsume() {
        CharacterReader reader = new CharacterReader("abcdef");
        reader.advance();
        reader.advance();
        assertEquals("cdef", reader.consumeToEnd());
        assertTrue(reader.isEmpty());
    }

    // Tests matchConsumeIgnoreCase
    @Test
    public void testMatchConsumeIgnoreCase_trueAndFalse() {
        CharacterReader reader = new CharacterReader("Hello World");
        assertTrue(reader.matchConsumeIgnoreCase("hello"));
        assertEquals(5, reader.pos());
        assertEquals(' ', reader.current());
        assertFalse(reader.matchConsumeIgnoreCase("world"));
        assertEquals(5, reader.pos());
    }

    // Tests consumeToIgnoreCase found and not found
    @Test
    public void testConsumeToIgnoreCase_foundAndNotFound() {
        CharacterReader reader = new CharacterReader("Hello World");
        assertEquals("Hello ", reader.consumeToIgnoreCase("world"));
        assertEquals('W', reader.current());

        CharacterReader reader2 = new CharacterReader("Hello");
        assertEquals("Hello", reader2.consumeToIgnoreCase("world"));
        assertTrue(reader2.isEmpty());
    }

    // Tests matchesAny
    @Test
    public void testMatchesAny_trueAndFalse() {
        CharacterReader reader = new CharacterReader("abc");
        assertTrue(reader.matchesAny('a', 'x'));
        assertFalse(reader.matchesAny('b', 'x'));
        reader.advance();
        assertTrue(reader.matchesAny('b', 'c'));
    }

    // Tests matchesAny on empty input
    @Test
    public void testMatchesAny_empty_false() {
        CharacterReader reader = new CharacterReader("");
        assertFalse(reader.matchesAny('a'));
    }

    // Tests matches(char) on empty input
    @Test
    public void testMatchesChar_empty_false() {
        CharacterReader reader = new CharacterReader("");
        assertFalse(reader.matches('a'));
    }

    // Tests matches(String) when the sequence is longer than remaining input
    @Test
    public void testMatchesString_tooLong_false() {
        CharacterReader reader = new CharacterReader("abc");
        assertFalse(reader.matches("abcd"));
    }

    // Tests matchesIgnoreCase when the sequence is longer than remaining input
    @Test
    public void testMatchesIgnoreCase_tooLong_false() {
        CharacterReader reader = new CharacterReader("abc");
        assertFalse(reader.matchesIgnoreCase("ABCD"));
    }

    // Tests matchesLetter, matchesDigit, matchesHex
    @Test
    public void testMatchesLetterDigitHex() {
        CharacterReader reader = new CharacterReader("A1f!");
        assertTrue(reader.matchesLetter());
        assertFalse(reader.matchesDigit());
        assertTrue(reader.matchesHex());
        reader.advance();
        assertFalse(reader.matchesLetter());
        assertTrue(reader.matchesDigit());
        assertTrue(reader.matchesHex());
        reader.advance();
        assertFalse(reader.matchesLetter());
        assertFalse(reader.matchesDigit());
        assertTrue(reader.matchesHex());
        reader.advance();
        assertFalse(reader.matchesLetter());
        assertFalse(reader.matchesDigit());
        assertFalse(reader.matchesHex());
    }

    // Tests matchesLetter/Digit/Hex on empty input
    @Test
    public void testMatchesLetterDigitHex_empty_false() {
        CharacterReader reader = new CharacterReader("");
        assertFalse(reader.matchesLetter());
        assertFalse(reader.matchesDigit());
        assertFalse(reader.matchesHex());
    }

    // Tests consumeLetterThenDigitSequence consumes letters then digits
    @Test
    public void testConsumeLetterThenDigitSequence_lettersThenDigits() {
        CharacterReader reader = new CharacterReader("abc123def");
        assertEquals("abc123", reader.consumeLetterThenDigitSequence());
        assertEquals('d', reader.current());
    }

    // Tests consumeLetterThenDigitSequence with letters only
    @Test
    public void testConsumeLetterThenDigitSequence_onlyLetters() {
        CharacterReader reader = new CharacterReader("abcdef");
        assertEquals("abcdef", reader.consumeLetterThenDigitSequence());
        assertTrue(reader.isEmpty());
    }

    // Tests consumeLetterThenDigitSequence starting with a digit returns empty
    @Test
    public void testConsumeLetterThenDigitSequence_startsWithDigit() {
        CharacterReader reader = new CharacterReader("123abc");
        assertEquals("", reader.consumeLetterThenDigitSequence());
        assertEquals('1', reader.current());
    }

    // Tests consumeLetterSequence when no letter is present
    @Test
    public void testConsumeLetterSequence_noLetters_returnsEmpty() {
        CharacterReader reader = new CharacterReader("123abc");
        assertEquals("", reader.consumeLetterSequence());
        assertEquals('1', reader.current());
    }

    // Tests consumeDigitSequence when no digit is present
    @Test
    public void testConsumeDigitSequence_noDigits_returnsEmpty() {
        CharacterReader reader = new CharacterReader("abc123");
        assertEquals("", reader.consumeDigitSequence());
        assertEquals('a', reader.current());
    }

    // Tests consumeHexSequence when no hex digit is present
    @Test
    public void testConsumeHexSequence_noHex_returnsEmpty() {
        CharacterReader reader = new CharacterReader("xyz");
        assertEquals("", reader.consumeHexSequence());
        assertEquals('x', reader.current());
    }
}