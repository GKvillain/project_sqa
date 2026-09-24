package org.jsoup.parser;

import org.junit.Test;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

import static org.junit.Assert.*;

public class CharacterReaderTest {

    // Tests null Reader in constructor throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testCharacterReader_nullReader_throwsException() {
        new CharacterReader((java.io.Reader) null, 16);
    }

    // Tests reading, cursor position, and EOF behavior
    @Test
    public void testConsume_basicSequence_tracksPositionAndReturnsEof() {
        CharacterReader r = new CharacterReader("abc");
        assertEquals(0, r.pos());
        assertFalse(r.isEmpty());
        assertEquals('a', r.current());
        assertEquals('a', r.consume());
        assertEquals(1, r.pos());
        assertEquals('b', r.consume());
        assertEquals('c', r.consume());
        assertEquals(3, r.pos());
        assertTrue(r.isEmpty());
        assertEquals(CharacterReader.EOF, r.current());
        assertEquals(CharacterReader.EOF, r.consume());
    }

    // Tests unconsuming a char and advancing position
    @Test
    public void testUnconsumeAndAdvance_validInput_adjustsPosition() {
        CharacterReader r = new CharacterReader("abc");
        r.consume(); // at 'b', pos 1
        r.unconsume(); // back to 'a', pos 0
        assertEquals('a', r.current());
        r.advance(); // at 'b', pos 1
        assertEquals('b', r.current());
    }

    // Tests mark and rewind functionality
    @Test
    public void testMarkAndRewindToMark_validMark_restoresPosition() {
        CharacterReader r = new CharacterReader("abcdef");
        r.consume(); // pos 1
        r.mark();
        r.consume();
        r.consume(); // pos 3 ('d')
        assertEquals('d', r.current());
        r.rewindToMark();
        assertEquals(1, r.pos());
        assertEquals('b', r.current());
    }

    // Tests nextIndexOf for single character and character sequence
    @Test
    public void testNextIndexOf_charAndCharSequence_returnsCorrectOffsets() {
        CharacterReader r = new CharacterReader("one two three two one");
        assertEquals(4, r.nextIndexOf('t'));
        assertEquals(-1, r.nextIndexOf('z'));
        assertEquals(4, r.nextIndexOf("two"));
        assertEquals(14, r.nextIndexOf("two one"));
        assertEquals(-1, r.nextIndexOf("four"));
    }

    // Tests consumeTo with char and String delimiters
    @Test
    public void testConsumeTo_charAndString_consumesExpectedSubstring() {
        CharacterReader r1 = new CharacterReader("hello world");
        assertEquals("hello", r1.consumeTo(' '));
        assertEquals(" world", r1.consumeToEnd());

        CharacterReader r2 = new CharacterReader("foo <!-- comment --> bar");
        assertEquals("foo ", r2.consumeTo("<!--"));
        assertEquals("<!-- comment --> bar", r2.consumeToEnd());

        CharacterReader r3 = new CharacterReader("no delimiter here");
        assertEquals("no delimiter here", r3.consumeTo('z'));
        assertTrue(r3.isEmpty());
    }

    // Tests consumeToAny with array of char delimiters
    @Test
    public void testConsumeToAny_multipleDelimiters_consumesUntilFirstMatch() {
        CharacterReader r = new CharacterReader("foo & bar < baz");
        assertEquals("foo ", r.consumeToAny('&', '<'));
        assertEquals('&', r.consume());
        assertEquals(" bar ", r.consumeToAny('&', '<'));
        assertEquals('<', r.consume());
        assertEquals(" baz", r.consumeToEnd());

        CharacterReader empty = new CharacterReader("");
        assertEquals("", empty.consumeToAny('a', 'b'));
    }

    // Tests consumeToAnySorted with sorted delimiter array
    @Test
    public void testConsumeToAnySorted_sortedDelimiters_consumesCorrectly() {
        char[] sortedDelims = new char[]{'&', '<', '>'};
        Arrays.sort(sortedDelims);
        CharacterReader r = new CharacterReader("text<tag>more&done");
        assertEquals("text", r.consumeToAnySorted(sortedDelims));
        assertEquals('<', r.consume());
        assertEquals("tag", r.consumeToAnySorted(sortedDelims));
        assertEquals('>', r.consume());
        assertEquals("more", r.consumeToAnySorted(sortedDelims));
    }

    // Tests consumeData stops at &, <, and nullChar
    @Test
    public void testConsumeData_specialTokens_stopsAtExpectedDelimiters() {
        CharacterReader r = new CharacterReader("data&more<tag\0end");
        assertEquals("data", r.consumeData());
        assertEquals('&', r.consume());
        assertEquals("more", r.consumeData());
        assertEquals('<', r.consume());
        assertEquals("tag", r.consumeData());
        assertEquals(TokeniserState.nullChar, r.consume());
        assertEquals("end", r.consumeData());
    }

    // Tests consumeTagName stops at HTML tag delimiters
    @Test
    public void testConsumeTagName_variousDelimiters_stopsCorrectly() {
        CharacterReader r1 = new CharacterReader("div class='foo'");
        assertEquals("div", r1.consumeTagName());

        CharacterReader r2 = new CharacterReader("input/ >");
        assertEquals("input", r2.consumeTagName());

        CharacterReader r3 = new CharacterReader("p\t\r\n\f>");
        assertEquals("p", r3.consumeTagName());

        CharacterReader r4 = new CharacterReader("tag\0rest");
        assertEquals("tag", r4.consumeTagName());
    }

    // Tests consumeLetterSequence, consumeLetterThenDigitSequence, and consumeDigitSequence
    @Test
    public void testConsumeLetterAndDigitSequences_mixedInputs_consumesMatchingPrefix() {
        CharacterReader r = new CharacterReader("Alpha123_Beta456 7890");
        assertEquals("Alpha", r.consumeLetterSequence());
        assertEquals("123", r.consumeDigitSequence());
        r.consume(); // skip '_'
        assertEquals("Beta456", r.consumeLetterThenDigitSequence());
        r.consume(); // skip ' '
        assertEquals("7890", r.consumeDigitSequence());
    }

    // Tests consumeHexSequence
    @Test
    public void testConsumeHexSequence_hexAndNonHex_consumesHexPrefixOnly() {
        CharacterReader r = new CharacterReader("12afAFg34");
        assertEquals("12afAF", r.consumeHexSequence());
        assertEquals('g', r.current());
    }

    // Tests matches and matchConsume with exact and case-insensitive matching
    @Test
    public void testMatchesAndMatchConsume_exactAndIgnoreCase_matchesAndAdvances() {
        CharacterReader r = new CharacterReader("One TWO three");
        assertTrue(r.matches('O'));
        assertFalse(r.matches('o'));
        assertTrue(r.matches("One"));
        assertFalse(r.matches("one"));
        assertTrue(r.matchesIgnoreCase("one"));

        assertTrue(r.matchConsume("One"));
        assertEquals(" TWO three", r.toString());

        assertTrue(r.matchConsumeIgnoreCase(" two"));
        assertEquals(" three", r.toString());

        assertFalse(r.matchConsume("four"));
        assertFalse(r.matchConsumeIgnoreCase("four"));
    }

    // Tests matchesAny, matchesAnySorted, matchesLetter, and matchesDigit
    @Test
    public void testMatchesLetterAndDigit_variousCharacters_returnsExpectedBooleans() {
        CharacterReader r = new CharacterReader("a1#");
        assertTrue(r.matchesLetter());
        assertFalse(r.matchesDigit());
        assertTrue(r.matchesAny('x', 'a', 'z'));
        assertTrue(r.matchesAnySorted(new char[]{'a', 'b', 'c'}));

        r.consume(); // at '1'
        assertFalse(r.matchesLetter());
        assertTrue(r.matchesDigit());

        r.consume(); // at '#'
        assertFalse(r.matchesLetter());
        assertFalse(r.matchesDigit());
        assertFalse(r.matchesAny('a', 'b'));

        r.consume(); // EOF
        assertFalse(r.matchesLetter());
        assertFalse(r.matchesDigit());
        assertFalse(r.matchesAny('a'));
        assertFalse(r.matchesAnySorted(new char[]{'a'}));
    }

    // Tests containsIgnoreCase for present and absent substrings
    @Test
    public void testContainsIgnoreCase_presentAndAbsent_returnsExpectedBoolean() {
        CharacterReader r = new CharacterReader("<html><BODY>Hello</body</html>");
        assertTrue(r.containsIgnoreCase("body"));
        assertTrue(r.containsIgnoreCase("BODY"));
        assertTrue(r.containsIgnoreCase("html"));
        assertFalse(r.containsIgnoreCase("script"));
    }

    // Tests bufferUp reading over small buffer chunk boundaries
    @Test
    public void testBufferUp_smallBufferCapacity_buffersAcrossBoundaries() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("abcdefghij");
        }
        String full = sb.toString();
        CharacterReader r = new CharacterReader(new StringReader(full), 16);

        assertEquals("abcdefghij", r.consumeTo('j') + r.consume());
        assertEquals(10, r.pos());
        String rest = r.consumeToEnd();
        assertEquals(full.substring(10), rest);
        assertTrue(r.isEmpty());
    }

    // Tests rangeEquals and toString representation
    @Test
    public void testRangeEqualsAndToString_variousRanges_returnsCorrectResults() {
        CharacterReader r = new CharacterReader("abcdef");
        assertTrue(r.rangeEquals(0, 3, "abc"));
        assertFalse(r.rangeEquals(0, 3, "abd"));
        assertFalse(r.rangeEquals(0, 4, "abc"));
        assertEquals("abcdef", r.toString());
        r.consume();
        r.consume();
        assertEquals("cdef", r.toString());
    }

    // Tests constructor with default buffer size using Reader
    @Test
    public void testCharacterReader_defaultReaderConstructor_readsSuccessfully() {
        CharacterReader r = new CharacterReader(new StringReader("sample reader"));
        assertEquals("sample reader", r.consumeToEnd());
        assertTrue(r.isEmpty());
    }

    // Tests unmark, unconsume at start, and close method
    @Test
    public void testUnmarkAndClose_validState_behavesAsExpected() {
        CharacterReader r = new CharacterReader("abcdef");
        r.mark();
        r.consume();
        r.unmark();
        r.close();

        // unconsume at position 0 should not throw and should remain at position 0
        CharacterReader r2 = new CharacterReader("a");
        r2.unconsume();
        assertEquals(0, r2.pos());
        assertEquals('a', r2.current());
    }

    // Tests close when Reader throws IOException wrapped in UncheckedIOException
    @Test
    public void testClose_readerThrowsIOException_handledProperly() {
        Reader failingReader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) {
                return -1;
            }

            @Override
            public void close() throws IOException {
                throw new IOException("close error");
            }
        };
        CharacterReader r = new CharacterReader(failingReader, 16);
        r.close();
    }

    // Tests line number, column number, cursorPos, and trackNewlines
    @Test
    public void testLineAndColumnNumber_trackingNewlines_calculatesAccurately() {
        CharacterReader r = new CharacterReader("line1\r\nline2\nline3\rline4");
        r.trackNewlines(true);

        assertEquals(1, r.lineNumber());
        assertEquals(1, r.columnNumber());
        assertEquals("1:1", r.cursorPos());

        r.consumeTo('\n');
        r.consume(); // consume '\n', now at line 2
        assertEquals(2, r.lineNumber());
        assertEquals(1, r.columnNumber());

        r.consumeTo('\n');
        r.consume(); // consume '\n', now at line 3
        assertEquals(3, r.lineNumber());
        assertEquals(1, r.columnNumber());

        r.consumeTo('\r');
        r.consume(); // consume '\r', now at line 4
        assertEquals(4, r.lineNumber());
        assertEquals(1, r.columnNumber());

        r.trackNewlines(false);
        assertEquals(1, r.lineNumber());
        assertEquals(r.pos() + 1, r.columnNumber());
    }

    // Tests empty sequence consumption methods for code coverage branches
    @Test
    public void testConsumeSequences_whenNoMatchOrEmpty_returnsEmptyString() {
        CharacterReader r = new CharacterReader("#$%");
        assertEquals("", r.consumeLetterSequence());
        assertEquals("", r.consumeLetterThenDigitSequence());
        assertEquals("", r.consumeDigitSequence());
        assertEquals("", r.consumeHexSequence());
        assertEquals("", r.consumeTagName());

        CharacterReader empty = new CharacterReader("");
        assertEquals("", empty.consumeToEnd());
        assertEquals("", empty.consumeTo("missing"));
        assertEquals("", empty.consumeLetterSequence());
        assertEquals("", empty.consumeDigitSequence());
        assertEquals("", empty.consumeHexSequence());
        assertEquals("", empty.consumeTagName());
        assertEquals("", empty.consumeData());
        assertFalse(empty.matchesLetter());
        assertFalse(empty.matchesDigit());
        assertFalse(empty.matches('a'));
        assertFalse(empty.matches("a"));
        assertFalse(empty.matchesIgnoreCase("a"));
        assertFalse(empty.matchesAny());
    }

    // Tests StringCache caching in consumeTo / consumeTagName / consumeLetterSequence
    @Test
    public void testStringCache_repeatedStrings_returnsCachedInstances() {
        CharacterReader r = new CharacterReader("tag-tag-tag-tag");
        String s1 = r.consumeTo('-');
        r.consume();
        String s2 = r.consumeTo('-');
        r.consume();
        String s3 = r.consumeTo('-');
        assertSame(s1, s2);
        assertSame(s2, s3);
    }
}