package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Arrays;

public class CharacterReaderTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullInput_throwsException() {
        new CharacterReader(null);
    }

    @Test
    public void testConstructor_emptyInput_isEmpty() {
        CharacterReader reader = new CharacterReader("");
        assertTrue(reader.isEmpty());
        assertEquals(CharacterReader.EOF, reader.current());
    }

    @Test
    public void testPosAndAdvance() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals(0, reader.pos());
        reader.advance();
        assertEquals(1, reader.pos());
        reader.advance();
        assertEquals(2, reader.pos());
        reader.advance();
        assertEquals(3, reader.pos());
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsume_basic() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals('a', reader.consume());
        assertEquals('b', reader.consume());
        assertEquals('c', reader.consume());
        assertEquals(CharacterReader.EOF, reader.consume());
    }

    @Test
    public void testUnconsume() {
        CharacterReader reader = new CharacterReader("abc");
        reader.consume();
        reader.unconsume();
        assertEquals('a', reader.current());
        assertEquals(0, reader.pos());
    }

    @Test
    public void testMarkAndRewindToMark() {
        CharacterReader reader = new CharacterReader("hello");
        reader.consume(); // 'h'
        reader.consume(); // 'e'
        reader.mark();    // pos=2
        reader.consume(); // 'l'
        reader.consume(); // 'l'
        reader.rewindToMark(); // pos=2
        assertEquals('l', reader.current());
        assertEquals(2, reader.pos());
    }

    @Test
    public void testConsumeAsString() {
        CharacterReader reader = new CharacterReader("xyz");
        assertEquals("x", reader.consumeAsString());
        assertEquals("y", reader.consumeAsString());
        assertEquals("z", reader.consumeAsString());
        assertEquals(CharacterReader.EOF, reader.current());
    }

    // nextIndexOf char tests
    @Test
    public void testNextIndexOf_char_found() {
        CharacterReader reader = new CharacterReader("hello");
        assertEquals(0, reader.nextIndexOf('h'));
        assertEquals(1, reader.nextIndexOf('e'));
        assertEquals(2, reader.nextIndexOf('l'));
        assertEquals(4, reader.nextIndexOf('o'));
    }

    @Test
    public void testNextIndexOf_char_notFound() {
        CharacterReader reader = new CharacterReader("hello");
        assertEquals(-1, reader.nextIndexOf('z'));
    }

    @Test
    public void testNextIndexOf_char_emptyInput() {
        CharacterReader reader = new CharacterReader("");
        assertEquals(-1, reader.nextIndexOf('a'));
    }

    // nextIndexOf CharSequence tests
    @Test
    public void testNextIndexOf_sequence_found() {
        CharacterReader reader = new CharacterReader("abc123");
        assertEquals(0, reader.nextIndexOf("ab"));
        assertEquals(1, reader.nextIndexOf("bc"));
        assertEquals(3, reader.nextIndexOf("12"));
        assertEquals(4, reader.nextIndexOf("23"));
    }

    @Test
    public void testNextIndexOf_sequence_notFound() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals(-1, reader.nextIndexOf("xyz"));
        assertEquals(-1, reader.nextIndexOf("abcd"));
    }

    @Test
    public void testNextIndexOf_sequence_atEnd() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals(1, reader.nextIndexOf("bc")); // bc ends at last index
    }

    @Test
    public void testNextIndexOf_sequence_longerThanRemaining() {
        CharacterReader reader = new CharacterReader("ab");
        assertEquals(-1, reader.nextIndexOf("abc"));
    }

    @Test
    public void testNextIndexOf_sequence_singleChar() {
        CharacterReader reader = new CharacterReader("a");
        assertEquals(0, reader.nextIndexOf("a"));
        assertEquals(-1, reader.nextIndexOf("b"));
    }

    // consumeTo tests
    @Test
    public void testConsumeTo_char_found() {
        CharacterReader reader = new CharacterReader("hello world");
        assertEquals("hello", reader.consumeTo(' '));
        assertEquals(' ', reader.current());
    }

    @Test
    public void testConsumeTo_char_notFound_consumesToEnd() {
        CharacterReader reader = new CharacterReader("hello");
        assertEquals("hello", reader.consumeTo('x'));
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeTo_String_found() {
        CharacterReader reader = new CharacterReader("hello world");
        assertEquals("hello ", reader.consumeTo("wo"));
        assertEquals('w', reader.current());
    }

    @Test
    public void testConsumeTo_String_atStart_returnsEmpty() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals("", reader.consumeTo("a"));
        assertEquals('a', reader.current());
    }

    // consumeToAny
    @Test
    public void testConsumeToAny_found() {
        CharacterReader reader = new CharacterReader("abc;def");
        assertEquals("abc", reader.consumeToAny(';', ':'));
        assertEquals(';', reader.current());
    }

    @Test
    public void testConsumeToAny_noMatch_consumesAll() {
        CharacterReader reader = new CharacterReader("abcdef");
        assertEquals("abcdef", reader.consumeToAny('x', 'y'));
        assertTrue(reader.isEmpty());
    }

    // consumeToAnySorted
    @Test
    public void testConsumeToAnySorted_found() {
        CharacterReader reader = new CharacterReader("abc;def");
        // chars must be sorted for binarySearch
        char[] sorted = new char[]{';', ':'};
        Arrays.sort(sorted);
        assertEquals("abc", reader.consumeToAnySorted(sorted));
        assertEquals(';', reader.current());
    }

    // consumeData
    @Test
    public void testConsumeData_stopsAtAmpersand() {
        CharacterReader reader = new CharacterReader("abc&def");
        assertEquals("abc", reader.consumeData());
        assertEquals('&', reader.current());
    }

    @Test
    public void testConsumeData_stopsAtLessThan() {
        CharacterReader reader = new CharacterReader("abc<def");
        assertEquals("abc", reader.consumeData());
        assertEquals('<', reader.current());
    }

    // consumeTagName
    @Test
    public void testConsumeTagName_stopsAtSpace() {
        CharacterReader reader = new CharacterReader("div>");
        assertEquals("div", reader.consumeTagName());
        assertEquals('>', reader.current());
    }

    // consumeToEnd
    @Test
    public void testConsumeToEnd() {
        CharacterReader reader = new CharacterReader("hello");
        assertEquals("hello", reader.consumeToEnd());
        assertTrue(reader.isEmpty());
    }

    // matches
    @Test
    public void testMatches_char_true() {
        CharacterReader reader = new CharacterReader("a");
        assertTrue(reader.matches('a'));
    }

    @Test
    public void testMatches_char_false() {
        CharacterReader reader = new CharacterReader("a");
        assertFalse(reader.matches('b'));
    }

    @Test
    public void testMatches_String_prefix() {
        CharacterReader reader = new CharacterReader("hello");
        assertTrue(reader.matches("hel"));
        assertFalse(reader.matches("world"));
    }

    @Test
    public void testMatchesIgnoreCase() {
        CharacterReader reader = new CharacterReader("Hello");
        assertTrue(reader.matchesIgnoreCase("hello"));
        assertTrue(reader.matchesIgnoreCase("HELLO"));
        assertFalse(reader.matchesIgnoreCase("world"));
    }

    // matchConsume
    @Test
    public void testMatchConsume_success() {
        CharacterReader reader = new CharacterReader("abc");
        assertTrue(reader.matchConsume("ab"));
        assertEquals('c', reader.current());
        assertEquals(2, reader.pos());
    }

    @Test
    public void testMatchConsume_failure() {
        CharacterReader reader = new CharacterReader("abc");
        assertFalse(reader.matchConsume("xyz"));
        assertEquals(0, reader.pos());
    }

    // containsIgnoreCase
    @Test
    public void testContainsIgnoreCase_found() {
        CharacterReader reader = new CharacterReader("Hello World");
        assertTrue(reader.containsIgnoreCase("world"));
        assertTrue(reader.containsIgnoreCase("hello"));
        assertFalse(reader.containsIgnoreCase("xyz"));
    }

    // toString
    @Test
    public void testToString_returnsRemaining() {
        CharacterReader reader = new CharacterReader("abcdef");
        reader.consume(); // 'a'
        reader.consume(); // 'b'
        assertEquals("cdef", reader.toString());
    }

    // empty reader edge cases
    @Test
    public void testEmptyReader_behavior() {
        CharacterReader reader = new CharacterReader("");
        assertTrue(reader.isEmpty());
        assertEquals(CharacterReader.EOF, reader.current());
        assertEquals(CharacterReader.EOF, reader.consume());
        assertEquals(-1, reader.nextIndexOf('a'));
        assertEquals(-1, reader.nextIndexOf("abc"));
        assertEquals("", reader.consumeTo('x'));
        assertEquals("", reader.consumeToEnd());
        assertFalse(reader.matches('a'));
        assertFalse(reader.matchesAny('a'));
        assertFalse(reader.matchesLetter());
        assertFalse(reader.matchesDigit());
        assertFalse(reader.matchConsume("a"));
    }

    // sequence consumers
    @Test
    public void testConsumeLetterSequence() {
        CharacterReader reader = new CharacterReader("abc123");
        assertEquals("abc", reader.consumeLetterSequence());
        assertEquals('1', reader.current());
    }

    @Test
    public void testConsumeDigitSequence() {
        CharacterReader reader = new CharacterReader("123abc");
        assertEquals("123", reader.consumeDigitSequence());
        assertEquals('a', reader.current());
    }

    @Test
    public void testConsumeHexSequence() {
        CharacterReader reader = new CharacterReader("1a2f");
        assertEquals("1a2f", reader.consumeHexSequence());
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeLetterThenDigitSequence() {
        CharacterReader reader = new CharacterReader("ab12cd");
        assertEquals("ab12", reader.consumeLetterThenDigitSequence());
        assertEquals('c', reader.current());
    }

    // cacheString for long string (>12 chars) should not cache
    @Test
    public void testConsumeToEnd_longString_noCache() {
        String longStr = "abcdefghijklmnopqrstuvwxyz";
        CharacterReader reader = new CharacterReader(longStr);
        assertEquals(longStr, reader.consumeToEnd());
    }

    // matchesAnySorted
    @Test
    public void testMatchesAnySorted_found() {
        CharacterReader reader = new CharacterReader("b");
        char[] sorted = new char[]{'a', 'b', 'c'};
        Arrays.sort(sorted);
        assertTrue(reader.matchesAnySorted(sorted));
        reader.consume();
        assertFalse(reader.matchesAnySorted(sorted));
    }
}