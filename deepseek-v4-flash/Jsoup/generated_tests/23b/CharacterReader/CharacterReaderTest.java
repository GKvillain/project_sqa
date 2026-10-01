package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class CharacterReaderTest {

    @Test(expected = NullPointerException.class)
    public void testConstructor_nullInput_throwsNullPointerException() {
        new CharacterReader(null);
    }

    @Test
    public void testConstructor_carriageReturn_returnsNormalized() {
        CharacterReader reader = new CharacterReader("a\r\nb\rc");
        assertEquals('a', reader.current());
        reader.consume();
        assertEquals('\n', reader.current());
        reader.consume();
        assertEquals('b', reader.current());
        reader.consume();
        assertEquals('\n', reader.current());
        reader.consume();
        assertEquals('c', reader.current());
    }

    @Test
    public void testPos_and_isEmpty_and_current() {
        CharacterReader reader = new CharacterReader("xyz");
        assertEquals(0, reader.pos());
        assertFalse(reader.isEmpty());
        assertEquals('x', reader.current());

        reader = new CharacterReader("");
        assertTrue(reader.isEmpty());
        assertEquals(CharacterReader.EOF, reader.current());
    }

    @Test
    public void testConsume_and_unconsume() {
        CharacterReader reader = new CharacterReader("ab");
        assertEquals('a', reader.consume());
        assertEquals(1, reader.pos());
        assertEquals('b', reader.current());

        reader.unconsume();
        assertEquals(0, reader.pos());
        assertEquals('a', reader.current());

        // consume on empty input
        reader = new CharacterReader("");
        assertEquals(CharacterReader.EOF, reader.consume());
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testAdvance() {
        CharacterReader reader = new CharacterReader("abc");
        reader.advance();
        assertEquals(1, reader.pos());
        reader.advance();
        assertEquals(2, reader.pos());
    }

    @Test
    public void testMark_and_rewindToMark() {
        CharacterReader reader = new CharacterReader("hello");
        reader.consume(); // consume 'h'
        reader.consume(); // consume 'e'
        reader.mark();    // mark at pos=2 (pointing to 'l')
        reader.consume(); // consume 'l'
        reader.consume(); // consume 'l'
        reader.rewindToMark();
        assertEquals(2, reader.pos());
        assertEquals('l', reader.current());
    }

    // Defect: consumeAsString returns empty string instead of the character
    @Test
    public void testConsumeAsString_normalInput_returnsFirstCharAsString() {
        CharacterReader reader = new CharacterReader("abc");
        String result = reader.consumeAsString();
        assertEquals("a", result);   // bug: actual returns "" → test fails, exposing defect
        assertEquals(1, reader.pos());
    }

    @Test
    public void testConsumeTo_char_variations() {
        // found
        CharacterReader reader = new CharacterReader("abcdef");
        assertEquals("abc", reader.consumeTo('d'));
        assertEquals(3, reader.pos());

        // not found – consumes to end
        reader = new CharacterReader("abc");
        assertEquals("abc", reader.consumeTo('x'));
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeTo_string_variations() {
        // found
        CharacterReader reader = new CharacterReader("abcxyz");
        assertEquals("abc", reader.consumeTo("xy"));
        assertEquals(3, reader.pos());

        // not found – consumes to end
        reader = new CharacterReader("abcdef");
        assertEquals("abcdef", reader.consumeTo("zz"));
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeToAny_variations() {
        // matching
        CharacterReader reader = new CharacterReader("abcdefgh");
        assertEquals("abcde", reader.consumeToAny('f', 'g'));
        assertEquals(5, reader.pos());

        // no match – consumes to end
        reader = new CharacterReader("abc");
        assertEquals("abc", reader.consumeToAny('x', 'y'));
        assertTrue(reader.isEmpty());

        // empty input
        reader = new CharacterReader("");
        assertEquals("", reader.consumeToAny('a'));
    }

    @Test
    public void testConsumeToEnd() {
        CharacterReader reader = new CharacterReader("abcdef");
        reader.consume(); // consume 'a'
        assertEquals("bcdef", reader.consumeToEnd());
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeLetterSequence_variations() {
        // letters only
        CharacterReader reader = new CharacterReader("abc123");
        assertEquals("abc", reader.consumeLetterSequence());
        assertEquals(3, reader.pos());

        // empty input
        reader = new CharacterReader("");
        assertEquals("", reader.consumeLetterSequence());
    }

    @Test
    public void testMatchesOperations() {
        CharacterReader reader = new CharacterReader("Hello 2");

        // matches char
        assertTrue(reader.matches('H'));
        assertFalse(reader.matches('h'));

        // matches string
        assertTrue(reader.matches("Hel"));
        assertFalse(reader.matches("Helo"));

        // matchesIgnoreCase
        assertTrue(reader.matchesIgnoreCase("hello"));
        assertFalse(reader.matchesIgnoreCase("Hello!"));

        // matchesAny
        assertTrue(reader.matchesAny('H', 'e'));
        assertFalse(reader.matchesAny('x', 'y'));

        // matchesAny on empty input
        reader = new CharacterReader("");
        assertFalse(reader.matchesAny('a'));

        // matchesLetter
        reader = new CharacterReader("a1");
        assertTrue(reader.matchesLetter());
        reader.consume(); // consume 'a'
        assertFalse(reader.matchesLetter());

        // matchesDigit
        reader = new CharacterReader("1a");
        assertTrue(reader.matchesDigit());
        reader.consume(); // consume '1'
        assertFalse(reader.matchesDigit());
    }

    @Test
    public void testMatchConsume_variations() {
        CharacterReader reader = new CharacterReader("hello world");

        // matchConsume true
        assertTrue(reader.matchConsume("hello"));
        assertEquals(5, reader.pos());

        // matchConsume false – pos unchanged
        assertFalse(reader.matchConsume("planet"));
        assertEquals(5, reader.pos());

        // matchConsumeIgnoreCase
        reader = new CharacterReader("Goodbye");
        assertTrue(reader.matchConsumeIgnoreCase("goodbye"));
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testContainsIgnoreCase_and_toString() {
        // containsIgnoreCase true
        CharacterReader reader = new CharacterReader("Title: Something");
        assertTrue(reader.containsIgnoreCase("title"));
        assertFalse(reader.containsIgnoreCase("titles"));

        // toString
        reader = new CharacterReader("abcdef");
        reader.consume(); // consume 'a'
        assertEquals("bcdef", reader.toString());
    }

    // ========== New tests to improve coverage ==========

    @Test
    public void testUnconsumeAtStart() {
        CharacterReader reader = new CharacterReader("abc");
        reader.unconsume(); // pos is 0, should do nothing
        assertEquals(0, reader.pos());
        assertEquals('a', reader.current());
    }

    @Test
    public void testRewindToMarkWithoutMark() {
        CharacterReader reader = new CharacterReader("abc");
        reader.rewindToMark(); // no mark set, pos should remain unchanged or become 0
        // Accept any safe behaviour: either pos stays 0 or goes to 0
        assertEquals(0, reader.pos());
    }

    @Test
    public void testConsumeToAnyEmptyVarargs() {
        CharacterReader reader = new CharacterReader("abcdef");
        // call with no arguments; should consume nothing or everything?
        // We just verify no exception and a string is returned
        String result = reader.consumeToAny();
        assertNotNull(result);
    }

    @Test
    public void testMatchesAnyEmptyVarargs() {
        CharacterReader reader = new CharacterReader("a");
        // no arguments -> should return false (no match)
        assertFalse(reader.matchesAny());
    }

    @Test
    public void testContainsIgnoreCaseEmptyString() {
        CharacterReader reader = new CharacterReader("hello");
        // containsIgnoreCase("") should return true (substring is everywhere)
        assertTrue(reader.containsIgnoreCase(""));
    }

    @Test
    public void testConsumeToEndOnEmpty() {
        CharacterReader reader = new CharacterReader("");
        assertEquals("", reader.consumeToEnd());
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeAsStringOnEmpty() {
        CharacterReader reader = new CharacterReader("");
        String result = reader.consumeAsString();
        // On empty, result might be "" or EOF string
        assertNotNull(result);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testMatchesLetterOnEmpty() {
        CharacterReader reader = new CharacterReader("");
        assertFalse(reader.matchesLetter());
    }

    @Test
    public void testMatchesDigitOnEmpty() {
        CharacterReader reader = new CharacterReader("");
        assertFalse(reader.matchesDigit());
    }

    @Test
    public void testMatchConsumeEmptyString() {
        CharacterReader reader = new CharacterReader("abc");
        assertTrue(reader.matchConsume(""));
        // Empty string always matches, pos unchanged
        assertEquals(0, reader.pos());
    }

    @Test
    public void testAdvanceOnEmpty() {
        CharacterReader reader = new CharacterReader("");
        reader.advance(); // should not throw; pos may become 1 or stay
        // Just ensure we can still call isEmpty/current without exception
        assertTrue(reader.isEmpty());
        assertEquals(CharacterReader.EOF, reader.current());
    }

    @Test
    public void testMultipleMarkAndRewind() {
        CharacterReader reader = new CharacterReader("hello");
        reader.mark(); // pos 0
        reader.consume(); // 'h', pos=1
        reader.mark(); // new mark at 1
        reader.consume(); // 'e', pos=2
        reader.rewindToMark(); // back to 1
        assertEquals(1, reader.pos());
        assertEquals('e', reader.current()); // current at 'e'
    }

    @Test
    public void testConsumeLetterThenDigitSequence() {
        // letters then digits
        CharacterReader reader = new CharacterReader("abc123def");
        assertEquals("abc123", reader.consumeLetterThenDigitSequence());
        assertEquals(6, reader.pos());

        // letters only
        reader = new CharacterReader("abc");
        assertEquals("abc", reader.consumeLetterThenDigitSequence());
        assertEquals(3, reader.pos());

        // digits only (no letters at start -> empty)
        reader = new CharacterReader("123");
        assertEquals("", reader.consumeLetterThenDigitSequence());
        assertEquals(0, reader.pos());

        // empty input
        reader = new CharacterReader("");
        assertEquals("", reader.consumeLetterThenDigitSequence());
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeHexSequence() {
        CharacterReader reader = new CharacterReader("1A2Bx");
        assertEquals("1A2B", reader.consumeHexSequence());
        assertEquals(4, reader.pos());

        // non-hex start
        reader = new CharacterReader("x1A");
        assertEquals("", reader.consumeHexSequence());
        assertEquals(0, reader.pos());

        // empty
        reader = new CharacterReader("");
        assertEquals("", reader.consumeHexSequence());
    }

    @Test
    public void testConsumeNumericSequence() {
        CharacterReader reader = new CharacterReader("123abc");
        assertEquals("123", reader.consumeNumericSequence());
        assertEquals(3, reader.pos());

        // non-digit start
        reader = new CharacterReader("a123");
        assertEquals("", reader.consumeNumericSequence());
        assertEquals(0, reader.pos());

        // empty
        reader = new CharacterReader("");
        assertEquals("", reader.consumeNumericSequence());
    }

    @Test
    public void testMatchesHexSequence() {
        CharacterReader reader = new CharacterReader("1A2B");
        assertTrue(reader.matchesHexSequence());
        reader.consume();
        assertTrue(reader.matchesHexSequence());

        // non-hex
        reader = new CharacterReader("G");
        assertFalse(reader.matchesHexSequence());

        // empty
        reader = new CharacterReader("");
        assertFalse(reader.matchesHexSequence());
    }

    @Test
    public void testMatchesNumericSequence() {
        CharacterReader reader = new CharacterReader("123");
        assertTrue(reader.matchesNumericSequence());
        reader.consume();
        assertTrue(reader.matchesNumericSequence());

        // non-digit
        reader = new CharacterReader("a");
        assertFalse(reader.matchesNumericSequence());

        // empty
        reader = new CharacterReader("");
        assertFalse(reader.matchesNumericSequence());
    }

    @Test
    public void testCacheString() {
        // cacheString is used for string interning; just verify it returns something
        // and doesn't alter position
        CharacterReader reader = new CharacterReader("hello");
        String cached = reader.cacheString(0, 3); // "hel"
        assertEquals("hel", cached);
        assertEquals(0, reader.pos());
    }
}