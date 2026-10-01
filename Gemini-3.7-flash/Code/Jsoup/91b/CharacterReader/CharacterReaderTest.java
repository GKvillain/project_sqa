package org.jsoup.parser;

import org.jsoup.UncheckedIOException;
import org.junit.Test;

import java.io.StringReader;
import java.util.Arrays;

import static org.junit.Assert.*;

public class CharacterReaderTest {

    // Tests null Reader input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCharacterReader_nullReader_throwsException() {
        new CharacterReader((java.io.Reader) null);
    }

    // Tests normal consumption and character positioning
    @Test
    public void testConsumeAndCurrent_normalString_readsCharactersInOrder() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals(0, reader.pos());
        assertFalse(reader.isEmpty());
        assertEquals('a', reader.current());
        assertEquals('a', reader.consume());
        assertEquals(1, reader.pos());
        reader.advance();
        assertEquals(2, reader.pos());
        assertEquals('c', reader.consume());
        assertTrue(reader.isEmpty());
        assertEquals(CharacterReader.EOF, reader.current());
        assertEquals(CharacterReader.EOF, reader.consume());
    }

    // Tests exception when unconsuming at buffer start
    @Test(expected = UncheckedIOException.class)
    public void testUnconsume_atBufferStart_throwsException() {
        CharacterReader reader = new CharacterReader("test");
        reader.unconsume();
    }

    // Tests unconsume after advancing restores previous character
    @Test
    public void testUnconsume_afterConsume_rewindsOneChar() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals('a', reader.consume());
        assertEquals(1, reader.pos());
        reader.unconsume();
        assertEquals(0, reader.pos());
        assertEquals('a', reader.current());
    }

    // Tests mark and rewindToMark restores position
    @Test
    public void testMarkAndRewind_validMark_restoresPosition() {
        CharacterReader reader = new CharacterReader("abcdef");
        reader.consume(); // at 'b'
        reader.mark();
        reader.consume(); // 'b'
        reader.consume(); // 'c'
        assertEquals(3, reader.pos());
        reader.rewindToMark();
        assertEquals(1, reader.pos());
        assertEquals('b', reader.current());
    }

    // Tests rewindToMark without prior mark throws exception
    @Test(expected = UncheckedIOException.class)
    public void testRewindToMark_withoutMark_throwsException() {
        CharacterReader reader = new CharacterReader("abc");
        reader.rewindToMark();
    }

    // Tests finding next index of char and CharSequence
    @Test
    public void testNextIndexOf_charAndSequence_returnsExpectedOffsets() {
        CharacterReader reader = new CharacterReader("hello world");
        assertEquals(4, reader.nextIndexOf('o'));
        assertEquals(-1, reader.nextIndexOf('z'));
        assertEquals(6, reader.nextIndexOf("world"));
        assertEquals(-1, reader.nextIndexOf("missing"));
    }

    // Tests consuming up to a char or string delimiter
    @Test
    public void testConsumeTo_charAndString_consumesSubstrings() {
        CharacterReader reader1 = new CharacterReader("foo:bar");
        assertEquals("foo", reader1.consumeTo(':'));
        assertEquals(':', reader1.consume());
        assertEquals("bar", reader1.consumeTo(';')); // not found, consumes to end

        CharacterReader reader2 = new CharacterReader("key:=value");
        assertEquals("key", reader2.consumeTo(":="));
        assertEquals(":=", reader2.consumeTo("missing"));
    }

    // Tests consumeToAny and consumeToAnySorted with multiple delimiters
    @Test
    public void testConsumeToAny_delimitersPresent_stopsAtFirstMatch() {
        CharacterReader reader = new CharacterReader("one, two; three");
        assertEquals("one", reader.consumeToAny(',', ';'));
        reader.advance(); // skip ','
        reader.advance(); // skip ' '
        char[] sorted = new char[]{';', ','};
        Arrays.sort(sorted);
        assertEquals("two", reader.consumeToAnySorted(sorted));
    }

    // Tests consuming data up to HTML data delimiters
    @Test
    public void testConsumeData_htmlEntitiesAndTags_stopsAtDelimiters() {
        CharacterReader reader = new CharacterReader("Hello &amp; <b>world</b>");
        assertEquals("Hello ", reader.consumeData());
        assertEquals('&', reader.consume());
        assertEquals("amp;", reader.consumeTo(' '));
        reader.advance();
        assertEquals("", reader.consumeData()); // at '<'
    }

    // Tests consuming tag names up to whitespace or tag end
    @Test
    public void testConsumeTagName_variousEndDelimiters_extractsTag() {
        CharacterReader reader1 = new CharacterReader("div class='foo'");
        assertEquals("div", reader1.consumeTagName());

        CharacterReader reader2 = new CharacterReader("span/ >");
        assertEquals("span", reader2.consumeTagName());
    }

    // Tests letter, digit, and hex sequence consumption
    @Test
    public void testConsumeSequences_lettersDigitsHex_consumesCorrectRanges() {
        CharacterReader reader = new CharacterReader("abc123DEF 789 1a2F zzz");
        assertEquals("abc", reader.consumeLetterSequence());
        assertEquals("123DEF", reader.consumeLetterThenDigitSequence()); // at '1', no letters then '123'
        reader.advance(); // skip space
        assertEquals("789", reader.consumeDigitSequence());
        reader.advance(); // skip space
        assertEquals("1a2F", reader.consumeHexSequence());
    }

    // Tests matches, matchesIgnoreCase and boundary matching
    @Test
    public void testMatches_exactAndIgnoreCase_returnsExpectedBooleans() {
        CharacterReader reader = new CharacterReader("Hello World");
        assertTrue(reader.matches('H'));
        assertFalse(reader.matches('h'));
        assertTrue(reader.matches("Hello"));
        assertFalse(reader.matches("hello"));
        assertTrue(reader.matchesIgnoreCase("hello"));
        assertFalse(reader.matches("Hello World Longer Than Buffer"));
        assertFalse(reader.matchesIgnoreCase("Hello World Longer Than Buffer"));
    }

    // Tests matchesAny, matchesAnySorted, matchesLetter, and matchesDigit
    @Test
    public void testMatchesAnyAndLetterDigit_validCharacters_returnsExpectedBooleans() {
        CharacterReader reader = new CharacterReader("A1!");
        assertTrue(reader.matchesAny('B', 'A'));
        assertFalse(reader.matchesAny('X', 'Y'));
        assertTrue(reader.matchesLetter());
        assertFalse(reader.matchesDigit());

        char[] sorted = new char[]{'A', 'C'};
        assertTrue(reader.matchesAnySorted(sorted));

        reader.consume(); // move to '1'
        assertFalse(reader.matchesLetter());
        assertTrue(reader.matchesDigit());

        reader.consume(); // move to '!'
        assertFalse(reader.matchesLetter());
        assertFalse(reader.matchesDigit());
    }

    // Tests matchConsume and matchConsumeIgnoreCase advance position only on match
    @Test
    public void testMatchConsume_caseSensitiveAndInsensitive_consumesOnMatch() {
        CharacterReader reader = new CharacterReader("TestString");
        assertFalse(reader.matchConsume("test"));
        assertEquals(0, reader.pos());

        assertTrue(reader.matchConsumeIgnoreCase("test"));
        assertEquals(4, reader.pos());

        assertTrue(reader.matchConsume("String"));
        assertEquals(10, reader.pos());
        assertTrue(reader.isEmpty());
    }

    // Tests containsIgnoreCase finds matching sequence in either case
    @Test
    public void testContainsIgnoreCase_variousCasing_findsPresence() {
        CharacterReader reader = new CharacterReader("The </TITLE> tag and </style>");
        assertTrue(reader.containsIgnoreCase("</title>"));
        assertTrue(reader.containsIgnoreCase("</STYLE>"));
        assertFalse(reader.containsIgnoreCase("</head>"));
    }

    // Tests consumeToEnd and toString representations
    @Test
    public void testConsumeToEndAndToString_normalString_returnsRemaining() {
        CharacterReader reader = new CharacterReader("sample text content");
        reader.consumeTo(' ');
        reader.advance();
        assertEquals("text content", reader.toString());
        assertEquals("text content", reader.consumeToEnd());
        assertEquals("", reader.toString());
        assertTrue(reader.isEmpty());
    }

    // Tests rangeEquals comparing char buffer slice with string
    @Test
    public void testRangeEquals_validAndInvalidRanges_returnsCorrectBooleans() {
        CharacterReader reader = new CharacterReader("abcdef");
        assertTrue(reader.rangeEquals(0, 3, "abc"));
        assertFalse(reader.rangeEquals(0, 3, "abd"));
        assertFalse(reader.rangeEquals(0, 4, "abc"));
    }

    // Tests unmark clears the active mark
    @Test(expected = UncheckedIOException.class)
    public void testUnmark_afterMark_clearsMarkAndThrowsOnRewind() {
        CharacterReader reader = new CharacterReader("abcdef");
        reader.mark();
        reader.consume();
        reader.unmark();
        reader.rewindToMark();
    }

    // Tests tracking newlines and line/column calculations
    @Test
    public void testTrackNewlines_multilineContent_tracksLineAndColumn() {
        CharacterReader reader = new CharacterReader("line1\nline2\r\nline3");
        reader.trackNewlines(true);
        assertTrue(reader.isTrackNewlines());
        assertEquals(1, reader.lineNumber());
        assertEquals(1, reader.columnNumber());
        assertEquals("1:1", reader.posLineCol());

        reader.consumeTo('\n');
        reader.consume(); // '\n'
        assertEquals(2, reader.lineNumber());
        assertEquals(1, reader.columnNumber());

        reader.consumeTo('\n');
        reader.consume(); // '\n' after '\r'
        assertEquals(3, reader.lineNumber());
        assertEquals(1, reader.columnNumber());

        assertEquals(1, reader.lineNumber(0));
        assertEquals(1, reader.columnNumber(0));
    }

    // Tests whitespace matching and consumption
    @Test
    public void testConsumeWhitespaceAndMatchesWhitespace_whitespaceChars_matchesAndConsumes() {
        CharacterReader reader = new CharacterReader("   \t\r\n\fhello");
        assertTrue(reader.matchesWhitespace());
        assertTrue(reader.consumeWhitespace());
        assertFalse(reader.matchesWhitespace());
        assertFalse(reader.consumeWhitespace());
        assertEquals("hello", reader.consumeToEnd());
    }

    // Tests ASCII alpha matching
    @Test
    public void testMatchesAsciiAlpha_variousCharacters_identifiesCorrectly() {
        CharacterReader reader = new CharacterReader("aZ1_");
        assertTrue(reader.matchesAsciiAlpha());
        reader.consume();
        assertTrue(reader.matchesAsciiAlpha());
        reader.consume();
        assertFalse(reader.matchesAsciiAlpha());
        reader.consume();
        assertFalse(reader.matchesAsciiAlpha());
    }

    // Tests consuming raw data up to closing tag
    @Test
    public void testConsumeRawData_scriptOrStyleContent_stopsAtEndTag() {
        CharacterReader reader = new CharacterReader("function foo() { return 1 < 2; } </script>");
        assertEquals("function foo() { return 1 < 2; } ", reader.consumeRawData());
        assertEquals("</script>", reader.consumeToEnd());
    }

    // Tests Reader constructor and buffer refilling with large input
    @Test
    public void testReaderConstructorAndBuffering_streamInput_readsAcrossBuffers() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3000; i++) {
            sb.append("a");
        }
        sb.append("needle");
        for (int i = 0; i < 3000; i++) {
            sb.append("b");
        }
        CharacterReader reader = new CharacterReader(new StringReader(sb.toString()));
        assertEquals(3000, reader.nextIndexOf("needle"));
        assertEquals(sb.substring(0, 3000), reader.consumeTo("needle"));
        assertTrue(reader.matchConsume("needle"));
        assertTrue(reader.containsIgnoreCase("bbb"));
        reader.close();
    }

    // Tests consuming letter then digit sequence starting with letters
    @Test
    public void testConsumeLetterThenDigitSequence_lettersFollowedByDigits_consumesBoth() {
        CharacterReader reader = new CharacterReader("item42;more");
        assertEquals("item42", reader.consumeLetterThenDigitSequence());
        assertEquals(";more", reader.consumeToEnd());
    }

    // Tests close method on CharacterReader wrapping a Reader
    @Test
    public void testClose_readerInput_closesUnderlyingReader() {
        CharacterReader reader = new CharacterReader(new StringReader("test"));
        reader.close();
    }
}