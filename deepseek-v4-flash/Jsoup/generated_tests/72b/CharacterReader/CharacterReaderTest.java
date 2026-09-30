package org.jsoup.parser;

import org.junit.Test;
import java.io.StringReader;
import static org.junit.Assert.*;

public class CharacterReaderTest {

    // Tests constructor with null Reader input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullInput_throwsException() {
        new CharacterReader((java.io.Reader) null);
    }

    // Tests empty reader returns true for isEmpty()
    @Test
    public void testIsEmpty_returnsTrue_whenEmpty() {
        CharacterReader reader = new CharacterReader("");
        assertTrue(reader.isEmpty());
    }

    // Tests empty reader returns EOF from current()
    @Test
    public void testCurrent_returnsEOF_whenEmpty() {
        CharacterReader reader = new CharacterReader("");
        assertEquals(CharacterReader.EOF, reader.current());
    }

    // Tests consumeToEnd consumes all remaining content
    @Test
    public void testConsumeToEnd_returnsAllContent() {
        CharacterReader reader = new CharacterReader("hello");
        assertEquals("hello", reader.consumeToEnd());
        assertTrue(reader.isEmpty());
    }

    // Tests nextIndexOf(char) finds character
    @Test
    public void testNextIndexOf_char_found() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals(1, reader.nextIndexOf('b'));
    }

    // Tests nextIndexOf(char) returns -1 when not found
    @Test
    public void testNextIndexOf_char_notFound() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals(-1, reader.nextIndexOf('d'));
    }

    // Tests nextIndexOf(CharSequence) finds sequence
    @Test
    public void testNextIndexOf_sequence_found() {
        CharacterReader reader = new CharacterReader("abcde");
        assertEquals(2, reader.nextIndexOf("cd"));
    }

    // Tests nextIndexOf(CharSequence) returns -1 for partial match
    @Test
    public void testNextIndexOf_sequence_partialMatch_notFound() {
        CharacterReader reader = new CharacterReader("abac");
        assertEquals(-1, reader.nextIndexOf("abb"));
    }

    // Tests consumeTo(char) stops at delimiter
    @Test
    public void testConsumeTo_char_delimiterFound() {
        CharacterReader reader = new CharacterReader("a,bc");
        assertEquals("a", reader.consumeTo(','));
        assertEquals(2, reader.pos());
    }

    // Tests consumeTo(String) stops at delimiter sequence
    @Test
    public void testConsumeTo_seq_delimiterFound() {
        CharacterReader reader = new CharacterReader("hello world");
        assertEquals("hello", reader.consumeTo(" wo"));
        assertEquals(5, reader.pos());
    }

    // Tests consumeToAny stops at first of given delimiters
    @Test
    public void testConsumeToAny_delimiterFound() {
        CharacterReader reader = new CharacterReader("hello;world");
        assertEquals("hello", reader.consumeToAny(';', ','));
        assertEquals(5, reader.pos());
    }

    // Tests consumeToAnySorted with sorted char array
    @Test
    public void testConsumeToAnySorted_delimiterFound() {
        CharacterReader reader = new CharacterReader("hello;world");
        assertEquals("hello", reader.consumeToAnySorted(new char[]{' ', ',', ';'}));
        assertEquals(5, reader.pos());
    }

    // Tests consumeData stops at '&'
    @Test
    public void testConsumeData_stopsAtAmpersand() {
        CharacterReader reader = new CharacterReader("abc&def");
        assertEquals("abc", reader.consumeData());
        assertEquals(3, reader.pos());
    }

    // Tests consumeTagName stops at space
    @Test
    public void testConsumeTagName_stopsAtSpace() {
        CharacterReader reader = new CharacterReader("div class");
        assertEquals("div", reader.consumeTagName());
        assertEquals(3, reader.pos());
    }

    // Tests matches(char) returns true when matches
    @Test
    public void testMatches_char_true() {
        CharacterReader reader = new CharacterReader("a");
        assertTrue(reader.matches('a'));
    }

    // Tests matchesAnySorted with sorted char array
    @Test
    public void testMatchesAnySorted_true() {
        CharacterReader reader = new CharacterReader("c");
        assertTrue(reader.matchesAnySorted(new char[]{'a', 'b', 'c'}));
    }

    // Tests matchConsume consumes and returns true
    @Test
    public void testMatchConsume_true() {
        CharacterReader reader = new CharacterReader("hello");
        assertTrue(reader.matchConsume("he"));
        assertEquals(2, reader.pos());
    }

    // Tests matchConsumeIgnoreCase consumes and returns true
    @Test
    public void testMatchConsumeIgnoreCase_true() {
        CharacterReader reader = new CharacterReader("HELLO");
        assertTrue(reader.matchConsumeIgnoreCase("hel"));
        assertEquals(3, reader.pos());
    }

    // Tests containsIgnoreCase finds string regardless of case
    @Test
    public void testContainsIgnoreCase_true() {
        CharacterReader reader = new CharacterReader("Hello World");
        assertTrue(reader.containsIgnoreCase("hELLo"));
    }

    // Tests bufferUp rebuffer when bufPos reaches bufSplitPoint
    @Test
    public void testBufferUp_rebufferAfterSplitPoint() {
        int length = 30000;
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) sb.append('a');
        String longInput = sb.toString();
        int maxBufLen = 1024 * 32; // CharacterReader.maxBufferLen
        CharacterReader reader = new CharacterReader(new StringReader(longInput), maxBufLen);

        // consume up to split point (readAheadLimit = 0.75 * maxBufLen)
        int splitPoint = (int) (maxBufLen * 0.75);
        for (int i = 0; i < splitPoint; i++) {
            reader.advance();
        }
        assertEquals(splitPoint, reader.pos());

        // trigger bufferUp by calling current()
        assertEquals('a', reader.current());
        // position should still be same after rebuffer
        assertEquals(splitPoint, reader.pos());

        // consume remaining
        String remaining = reader.consumeToEnd();
        assertEquals(length - splitPoint, remaining.length());
    }

    // ---- Additional tests for uncovered branches ----

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullInputWithSize_throwsException() {
        new CharacterReader((java.io.Reader) null, 100);
    }

    @Test
    public void testConstructor_smallBufferSize_isNormalized() {
        CharacterReader reader = new CharacterReader(new StringReader("abc"), 1);
        assertEquals('a', reader.current());
        assertEquals("abc", reader.consumeToEnd());
    }

    @Test
    public void testConsumeToEnd_empty_returnsEmpty() {
        CharacterReader reader = new CharacterReader("");
        assertEquals("", reader.consumeToEnd());
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testNextIndexOf_char_atCurrentPosition() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals(0, reader.nextIndexOf('a'));
    }

    @Test
    public void testNextIndexOf_char_empty_returnsMinusOne() {
        CharacterReader reader = new CharacterReader("");
        assertEquals(-1, reader.nextIndexOf('a'));
    }

    @Test
    public void testNextIndexOf_seq_atCurrentPosition() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals(0, reader.nextIndexOf("ab"));
    }

    @Test
    public void testNextIndexOf_seq_longerThanRemaining_returnsMinusOne() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals(-1, reader.nextIndexOf("abcd"));
    }

    @Test
    public void testConsumeTo_char_notFound_consumesAll() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals("abc", reader.consumeTo('z'));
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeTo_seq_notFound_consumesAll() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals("abc", reader.consumeTo("z"));
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeToAny_noDelimiter_consumesAll() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals("abc", reader.consumeToAny(';', ','));
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeToAny_delimiterAtStart_returnsEmpty() {
        CharacterReader reader = new CharacterReader(";abc");
        assertEquals("", reader.consumeToAny(';', ','));
        assertEquals(0, reader.pos());
    }

    @Test
    public void testConsumeToAnySorted_noDelimiter_consumesAll() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals("abc", reader.consumeToAnySorted(new char[]{',', ';'}));
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeToAnySorted_delimiterAtStart_returnsEmpty() {
        CharacterReader reader = new CharacterReader(";abc");
        assertEquals("", reader.consumeToAnySorted(new char[]{',', ';'}));
        assertEquals(0, reader.pos());
    }

    @Test
    public void testConsumeData_stopsAtLessThan() {
        CharacterReader reader = new CharacterReader("abc<def");
        assertEquals("abc", reader.consumeData());
        assertEquals(3, reader.pos());
    }

    @Test
    public void testConsumeData_stopsAtNull() {
        CharacterReader reader = new CharacterReader("abc" + (char) 0 + "def");
        assertEquals("abc", reader.consumeData());
        assertEquals(3, reader.pos());
    }

    @Test
    public void testConsumeData_allData_consumesAll() {
        CharacterReader reader = new CharacterReader("abcdef");
        assertEquals("abcdef", reader.consumeData());
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeData_delimiterAtStart_returnsEmpty() {
        CharacterReader reader = new CharacterReader("<abc");
        assertEquals("", reader.consumeData());
        assertEquals(0, reader.pos());
    }

    @Test
    public void testConsumeTagName_stopsAtSlash() {
        CharacterReader reader = new CharacterReader("div/class");
        assertEquals("div", reader.consumeTagName());
        assertEquals(3, reader.pos());
    }

    @Test
    public void testConsumeTagName_stopsAtGt() {
        CharacterReader reader = new CharacterReader("div>class");
        assertEquals("div", reader.consumeTagName());
        assertEquals(3, reader.pos());
    }

    @Test
    public void testConsumeTagName_stopsAtTab() {
        CharacterReader reader = new CharacterReader("div\tclass");
        assertEquals("div", reader.consumeTagName());
        assertEquals(3, reader.pos());
    }

    @Test
    public void testConsumeTagName_stopsAtNewline() {
        CharacterReader reader = new CharacterReader("div\nclass");
        assertEquals("div", reader.consumeTagName());
        assertEquals(3, reader.pos());
    }

    @Test
    public void testConsumeTagName_stopsAtCarriageReturn() {
        CharacterReader reader = new CharacterReader("div\rclass");
        assertEquals("div", reader.consumeTagName());
        assertEquals(3, reader.pos());
    }

    @Test
    public void testConsumeTagName_stopsAtLessThan() {
        CharacterReader reader = new CharacterReader("div<");
        assertEquals("div", reader.consumeTagName());
        assertEquals(3, reader.pos());
    }

    @Test
    public void testConsumeTagName_allName_consumesAll() {
        CharacterReader reader = new CharacterReader("divclass");
        assertEquals("divclass", reader.consumeTagName());
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeTagName_delimiterAtStart_returnsEmpty() {
        CharacterReader reader = new CharacterReader("/div");
        assertEquals("", reader.consumeTagName());
        assertEquals(0, reader.pos());
    }

    @Test
    public void testConsumeLetterSequence_lettersAndStopsAtDigit() {
        CharacterReader reader = new CharacterReader("abc123");
        assertEquals("abc", reader.consumeLetterSequence());
        assertEquals(3, reader.pos());
    }

    @Test
    public void testConsumeLetterSequence_unicodeLetter() {
        CharacterReader reader = new CharacterReader((char) 0xe9 + "1");
        assertEquals("é", reader.consumeLetterSequence());
        assertEquals(1, reader.pos());
    }

    @Test
    public void testConsumeLetterSequence_startsAtNonLetter_returnsEmpty() {
        CharacterReader reader = new CharacterReader("1abc");
        assertEquals("", reader.consumeLetterSequence());
        assertEquals(0, reader.pos());
    }

    @Test
    public void testConsumeLetterThenDigitSequence_lettersThenDigits() {
        CharacterReader reader = new CharacterReader("abc123def");
        assertEquals("abc123", reader.consumeLetterThenDigitSequence());
        assertEquals(6, reader.pos());
    }

    @Test
    public void testAdvanceAndUnconsume() {
        CharacterReader reader = new CharacterReader("abc");
        reader.advance();
        assertEquals(1, reader.pos());
        reader.unconsume();
        assertEquals(0, reader.pos());
        assertEquals('a', reader.current());
    }

    @Test
    public void testMarkAndRewindToMark() {
        CharacterReader reader = new CharacterReader("hello world");
        reader.advance();
        reader.advance();
        reader.mark();
        assertEquals("llo", reader.consumeTo(' '));
        reader.rewindToMark();
        assertEquals(2, reader.pos());
        assertEquals('l', reader.current());
    }

    @Test
    public void testMatches_false() {
        CharacterReader reader = new CharacterReader("abc");
        assertFalse(reader.matches('z'));
    }

    @Test
    public void testMatches_seq_true() {
        CharacterReader reader = new CharacterReader("abc");
        assertTrue(reader.matches("ab"));
        assertEquals(0, reader.pos());
    }

    @Test
    public void testMatches_seq_false() {
        CharacterReader reader = new CharacterReader("abc");
        assertFalse(reader.matches("ax"));
    }

    @Test
    public void testMatches_seq_longerThanRemaining_false() {
        CharacterReader reader = new CharacterReader("abc");
        assertFalse(reader.matches("abcd"));
    }

    @Test
    public void testMatchesIgnoreCase_true() {
        CharacterReader reader = new CharacterReader("ABC");
        assertTrue(reader.matchesIgnoreCase("abc"));
    }

    @Test
    public void testMatchesIgnoreCase_false() {
        CharacterReader reader = new CharacterReader("ABC");
        assertFalse(reader.matchesIgnoreCase("abd"));
    }

    @Test
    public void testMatchesIgnoreCase_seqLongerThanRemaining_false() {
        CharacterReader reader = new CharacterReader("ABC");
        assertFalse(reader.matchesIgnoreCase("abcd"));
    }

    @Test
    public void testMatchesAny_true() {
        CharacterReader reader = new CharacterReader("b");
        assertTrue(reader.matchesAny('a', 'b', 'c'));
    }

    @Test
    public void testMatchesAny_false() {
        CharacterReader reader = new CharacterReader("d");
        assertFalse(reader.matchesAny('a', 'b', 'c'));
    }

    @Test
    public void testMatchesAny_empty_returnsFalse() {
        CharacterReader reader = new CharacterReader("");
        assertFalse(reader.matchesAny('a'));
    }

    @Test
    public void testMatchesAnySorted_false() {
        CharacterReader reader = new CharacterReader("d");
        assertFalse(reader.matchesAnySorted(new char[]{'a', 'b', 'c'}));
    }

    @Test
    public void testMatchesLetter_true() {
        CharacterReader reader = new CharacterReader("a");
        assertTrue(reader.matchesLetter());
    }

    @Test
    public void testMatchesLetter_false() {
        CharacterReader reader = new CharacterReader("1");
        assertFalse(reader.matchesLetter());
    }

    @Test
    public void testMatchesLetter_empty_returnsFalse() {
        CharacterReader reader = new CharacterReader("");
        assertFalse(reader.matchesLetter());
    }

    @Test
    public void testMatchesDigit_true() {
        CharacterReader reader = new CharacterReader("5");
        assertTrue(reader.matchesDigit());
    }

    @Test
    public void testMatchesDigit_false() {
        CharacterReader reader = new CharacterReader("a");
        assertFalse(reader.matchesDigit());
    }

    @Test
    public void testMatchesDigit_empty_returnsFalse() {
        CharacterReader reader = new CharacterReader("");
        assertFalse(reader.matchesDigit());
    }

    @Test
    public void testMatchesWhitespace_true() {
        CharacterReader reader = new CharacterReader(" ");
        assertTrue(reader.matchesWhitespace());
    }

    @Test
    public void testMatchesWhitespace_false() {
        CharacterReader reader = new CharacterReader("a");
        assertFalse(reader.matchesWhitespace());
    }

    @Test
    public void testMatchesWhitespace_empty_returnsFalse() {
        CharacterReader reader = new CharacterReader("");
        assertFalse(reader.matchesWhitespace());
    }

    @Test
    public void testMatchesEOF_true() {
        CharacterReader reader = new CharacterReader("");
        assertTrue(reader.matchesEOF());
    }

    @Test
    public void testMatchesEOF_false() {
        CharacterReader reader = new CharacterReader("a");
        assertFalse(reader.matchesEOF());
    }

    @Test
    public void testMatchConsume_false() {
        CharacterReader reader = new CharacterReader("abc");
        assertFalse(reader.matchConsume("abz"));
        assertEquals(0, reader.pos());
    }

    @Test
    public void testMatchConsumeIgnoreCase_false() {
        CharacterReader reader = new CharacterReader("ABC");
        assertFalse(reader.matchConsumeIgnoreCase("abd"));
        assertEquals(0, reader.pos());
    }

    @Test
    public void testContainsIgnoreCase_false() {
        CharacterReader reader = new CharacterReader("Hello World");
        assertFalse(reader.containsIgnoreCase("xyz"));
    }

    @Test
    public void testContainsIgnoreCase_seqLongerThanRemaining_false() {
        CharacterReader reader = new CharacterReader("abc");
        assertFalse(reader.containsIgnoreCase("abcd"));
    }

    @Test
    public void testContainsIgnoreCase_emptySequence_returnsTrue() {
        CharacterReader reader = new CharacterReader("");
        assertTrue(reader.containsIgnoreCase(""));
    }

    @Test
    public void testToString_returnsRemaining() {
        CharacterReader reader = new CharacterReader("abc");
        reader.advance();
        assertEquals("bc", reader.toString());
    }

    @Test
    public void testCurrent_afterConsumingAll_returnsEOF() {
        CharacterReader reader = new CharacterReader("a");
        reader.advance();
        assertEquals(CharacterReader.EOF, reader.current());
    }
}