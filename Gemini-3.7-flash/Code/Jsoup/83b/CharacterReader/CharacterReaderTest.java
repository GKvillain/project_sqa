package org.jsoup.parser;

import org.jsoup.UncheckedIOException;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

import static org.junit.Assert.*;

public class CharacterReaderTest {

    // Tests constructor with null Reader throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullReader_throwsException() {
        new CharacterReader((Reader) null, 16);
    }

    // Tests constructor with unsupported mark Reader throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_unsupportedMarkReader_throwsException() {
        Reader unmarkableReader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) {
                return -1;
            }

            @Override
            public void close() {}

            @Override
            public boolean markSupported() {
                return false;
            }
        };
        new CharacterReader(unmarkableReader);
    }

    // Tests basic consumption and position tracking
    @Test
    public void testConsumeAndPos_normalSequence_returnsCharsAndCorrectPos() {
        CharacterReader r = new CharacterReader("abc");
        assertEquals(0, r.pos());
        assertEquals('a', r.current());
        assertEquals('a', r.consume());
        assertEquals(1, r.pos());
        assertEquals('b', r.consume());
        r.unconsume();
        assertEquals(1, r.pos());
        assertEquals('b', r.consume());
        r.advance();
        assertEquals(3, r.pos());
        assertTrue(r.isEmpty());
        assertEquals(CharacterReader.EOF, r.current());
        assertEquals(CharacterReader.EOF, r.consume());
    }

    // Tests mark and rewind functionality
    @Test
    public void testMarkAndRewind_markedPosition_rewindsCorrectly() {
        CharacterReader r = new CharacterReader("abcdef");
        r.consume(); // a
        r.mark();
        r.consume(); // b
        r.consume(); // c
        assertEquals(3, r.pos());
        r.rewindToMark();
        assertEquals(1, r.pos());
        assertEquals('b', r.consume());
    }

    // Tests nextIndexOf for char and CharSequence
    @Test
    public void testNextIndexOf_matchingAndNonMatching_returnsCorrectOffsets() {
        CharacterReader r = new CharacterReader("one two three two");
        assertEquals(3, r.nextIndexOf(' '));
        assertEquals(-1, r.nextIndexOf('z'));
        assertEquals(4, r.nextIndexOf("two"));
        assertEquals(-1, r.nextIndexOf("four"));
        r.consumeTo('t');
        assertEquals(0, r.nextIndexOf("two"));
    }

    // Tests consumeTo with char delimiter
    @Test
    public void testConsumeToChar_presentAndAbsent_returnsExpectedStrings() {
        CharacterReader r = new CharacterReader("foo&bar");
        assertEquals("foo", r.consumeTo('&'));
        assertEquals('&', r.consume());
        assertEquals("bar", r.consumeTo(';'));
        assertTrue(r.isEmpty());
    }

    // Tests consumeTo with String delimiter
    @Test
    public void testConsumeToString_presentAndAbsent_returnsExpectedStrings() {
        CharacterReader r = new CharacterReader("hello <script>world</script>");
        assertEquals("hello ", r.consumeTo("<script>"));
        assertEquals("<script>world", r.consumeTo("</script>"));
        assertEquals("</script>", r.consumeToEnd());
        assertEquals("", r.consumeTo("notfound"));
    }

    // Tests consumeToAny and consumeToAnySorted
    @Test
    public void testConsumeToAny_variousDelimiters_stopsAtFirstMatch() {
        CharacterReader r = new CharacterReader("one=two&three");
        assertEquals("one", r.consumeToAny('=', '&'));
        assertEquals('=', r.consume());

        char[] sortedDelims = new char[]{'&', '='};
        Arrays.sort(sortedDelims);
        assertEquals("two", r.consumeToAnySorted(sortedDelims));
        assertEquals('&', r.consume());
        assertEquals("three", r.consumeToAnySorted(sortedDelims));
    }

    // Tests consumeData method
    @Test
    public void testConsumeData_dataWithSpecialChars_consumesUntilSpecialChar() {
        CharacterReader r = new CharacterReader("text&data<tag\0after");
        assertEquals("text", r.consumeData());
        assertEquals('&', r.consume());
        assertEquals("data", r.consumeData());
        assertEquals('<', r.consume());
        assertEquals("tag", r.consumeData());
        assertEquals(TokeniserState.nullChar, r.consume());
        assertEquals("after", r.consumeData());
    }

    // Tests consumeTagName method
    @Test
    public void testConsumeTagName_variousEndings_stopsAtTagNameEnd() {
        CharacterReader r = new CharacterReader("div class='test'");
        assertEquals("div", r.consumeTagName());

        CharacterReader r2 = new CharacterReader("span/ >");
        assertEquals("span", r2.consumeTagName());

        CharacterReader r3 = new CharacterReader("p\t\n\r\f");
        assertEquals("p", r3.consumeTagName());
    }

    // Tests letter and digit sequence consumers
    @Test
    public void testConsumeSequences_variousInputs_matchesAndConsumesCorrectly() {
        CharacterReader r = new CharacterReader("abc123DEF 123 1A2F");
        assertEquals("abc", r.consumeLetterSequence());
        assertEquals("123DEF", r.consumeLetterThenDigitSequence());
        r.consume(); // space
        assertEquals("123", r.consumeDigitSequence());
        r.consume(); // space
        assertEquals("1A2F", r.consumeHexSequence());
    }

    // Tests matching methods (exact, ignoreCase, any, sorted)
    @Test
    public void testMatchesMethods_variousPatterns_returnsExpectedBooleans() {
        CharacterReader r = new CharacterReader("HelloWorld123");
        assertTrue(r.matches('H'));
        assertFalse(r.matches('h'));
        assertTrue(r.matches("Hello"));
        assertFalse(r.matches("World"));
        assertTrue(r.matchesIgnoreCase("hello"));
        assertFalse(r.matchesIgnoreCase("worlds"));
        assertTrue(r.matchesAny('a', 'e', 'H'));
        assertFalse(r.matchesAny('x', 'y', 'z'));

        char[] sorted = new char[]{'A', 'H', 'Z'};
        assertTrue(r.matchesAnySorted(sorted));

        assertTrue(r.matchesLetter());
        assertFalse(r.matchesDigit());

        r.consumeTo("123");
        assertTrue(r.matchesDigit());
        assertFalse(r.matchesLetter());

        CharacterReader empty = new CharacterReader("");
        assertFalse(empty.matches('a'));
        assertFalse(empty.matches("a"));
        assertFalse(empty.matchesIgnoreCase("a"));
        assertFalse(empty.matchesAny('a'));
        assertFalse(empty.matchesLetter());
        assertFalse(empty.matchesDigit());
    }

    // Tests matchConsume and matchConsumeIgnoreCase
    @Test
    public void testMatchConsume_caseSensitiveAndInsensitive_advancesWhenMatched() {
        CharacterReader r = new CharacterReader("HelloWorld");
        assertFalse(r.matchConsume("hello"));
        assertEquals('H', r.current());
        assertTrue(r.matchConsumeIgnoreCase("hello"));
        assertEquals("World", r.toString());
        assertTrue(r.matchConsume("World"));
        assertTrue(r.isEmpty());
    }

    // Tests containsIgnoreCase
    @Test
    public void testContainsIgnoreCase_varyingCases_findsSequence() {
        CharacterReader r = new CharacterReader("some text </TITLE> and more");
        assertTrue(r.containsIgnoreCase("</title>"));
        assertTrue(r.containsIgnoreCase("</TITLE>"));
        assertFalse(r.containsIgnoreCase("</style>"));
    }

    // Tests string cache mechanism and rangeEquals
    @Test
    public void testCacheString_repeatedStrings_usesCacheCorrectly() {
        CharacterReader r = new CharacterReader("tag tag tag longerthanmaxstringcachelength");
        String s1 = r.consumeTo(' ');
        r.advance();
        String s2 = r.consumeTo(' ');
        r.advance();
        String s3 = r.consumeTo(' ');
        r.advance();
        String sLong = r.consumeToEnd();

        assertEquals("tag", s1);
        assertSame(s1, s2);
        assertSame(s2, s3);
        assertEquals("longerthanmaxstringcachelength", sLong);
        assertTrue(r.rangeEquals(0, 3, "tag"));
        assertFalse(r.rangeEquals(0, 4, "tags"));
    }

    // Tests bufferUp IOException wrapping in UncheckedIOException
    @Test(expected = UncheckedIOException.class)
    public void testBufferUp_readerThrowsIOException_throwsUncheckedIOException() {
        Reader failingReader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("Simulated read failure");
            }

            @Override
            public void close() {}

            @Override
            public boolean markSupported() {
                return true;
            }
        };
        new CharacterReader(failingReader);
    }

    // Tests close method on CharacterReader and underlying Reader
    @Test
    public void testClose_closesUnderlyingReaderSuccessfully() {
        final boolean[] closed = {false};
        Reader reader = new StringReader("sample data") {
            @Override
            public void close() {
                super.close();
                closed[0] = true;
            }
        };
        CharacterReader cr = new CharacterReader(reader);
        cr.close();
        assertTrue(closed[0]);
    }

    // Tests buffer refill across small buffer capacity
    @Test
    public void testBufferRefill_smallBufferSize_readsAcrossBufferBoundaries() {
        String longText = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        CharacterReader r = new CharacterReader(new StringReader(longText), 16);

        assertEquals("abcdefghijklmnopqrstuvwxyz", r.consumeLetterSequence());
        assertEquals("0123456789", r.consumeDigitSequence());
        assertTrue(r.matches("ABC"));
        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ", r.consumeToEnd());
        assertTrue(r.isEmpty());
    }

    // Tests unmark functionality
    @Test
    public void testUnmark_clearsMarkedPosition() {
        CharacterReader r = new CharacterReader("abcdef");
        r.consume(); // a
        r.mark();
        r.consume(); // b
        r.unmark();
        r.consume(); // c
        r.rewindToMark(); // should rewind to 0 (or unconsume-safe start) as mark was cleared
        assertEquals(0, r.pos());
        assertEquals('a', r.consume());
    }

    // Tests nextIndexOf across small buffer boundaries
    @Test
    public void testNextIndexOf_acrossBufferBoundaries_findsCorrectIndex() {
        String data = "12345678901234567890TARGET567890";
        CharacterReader r = new CharacterReader(new StringReader(data), 16);

        assertEquals(20, r.nextIndexOf("TARGET"));
        assertEquals(20, r.nextIndexOf('T'));
        assertEquals(-1, r.nextIndexOf("NOT_EXIST"));
        assertEquals(-1, r.nextIndexOf('Z'));
    }

    // Tests containsIgnoreCase across buffer boundaries
    @Test
    public void testContainsIgnoreCase_acrossBufferBoundaries_findsMatch() {
        String data = "0123456789abcdef0123456789abcdef<sCrIpt>alert(1)</script>";
        CharacterReader r = new CharacterReader(new StringReader(data), 16);

        assertTrue(r.containsIgnoreCase("<SCRIPT>"));
        assertTrue(r.containsIgnoreCase("</SCRIPT>"));
        assertFalse(r.containsIgnoreCase("<STYLE>"));
    }

    // Tests rangeEquals edge cases including bounds and mismatches
    @Test
    public void testRangeEquals_boundaryAndMismatchConditions_returnsFalse() {
        CharacterReader r = new CharacterReader("abcdef");
        assertFalse(r.rangeEquals(-1, 3, "abc"));
        assertFalse(r.rangeEquals(0, 10, "abcdefghij"));
        assertFalse(r.rangeEquals(0, 3, "abd"));
        assertTrue(r.rangeEquals(0, 3, "abc"));
        assertTrue(r.rangeEquals(3, 3, "def"));
    }

    // Tests empty reader behaviour on sequence consume methods
    @Test
    public void testEmptyReader_sequenceMethods_returnEmptyStrings() {
        CharacterReader r = new CharacterReader("");
        assertEquals("", r.consumeLetterSequence());
        assertEquals("", r.consumeLetterThenDigitSequence());
        assertEquals("", r.consumeDigitSequence());
        assertEquals("", r.consumeHexSequence());
        assertEquals("", r.consumeTagName());
        assertEquals("", r.consumeData());
        assertEquals("", r.consumeToEnd());
        assertEquals("", r.toString());
    }

    // Tests single character Reader constructor (Reader without explicit buffer size)
    @Test
    public void testConstructor_readerOnly_initializesCorrectly() {
        CharacterReader r = new CharacterReader(new StringReader("hello world"));
        assertEquals("hello", r.consumeTo(' '));
        r.advance();
        assertEquals("world", r.consumeToEnd());
        assertTrue(r.isEmpty());
    }
}