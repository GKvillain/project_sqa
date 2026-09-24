package org.jsoup.parser;

import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CharacterReaderTest {

    // Tests constructor with null Reader throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCharacterReader_nullReader_throwsException() {
        new CharacterReader((java.io.Reader) null);
    }

    // Tests constructor with null String throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testCharacterReader_nullString_throwsException() {
        new CharacterReader((String) null);
    }

    // Tests empty string handling on pos, isEmpty, current, and consume
    @Test
    public void testConsume_emptyInput_returnsEofAndIsEmpty() {
        CharacterReader r = new CharacterReader("");
        assertEquals(0, r.pos());
        assertTrue(r.isEmpty());
        assertEquals(CharacterReader.EOF, r.current());
        assertEquals(CharacterReader.EOF, r.consume());
        assertEquals("", r.toString());
    }

    // Tests consume, unconsume, advance, mark, and rewindToMark
    @Test
    public void testMarkAndRewind_validInput_restoresPosition() {
        CharacterReader r = new CharacterReader("abcdef");
        assertEquals('a', r.consume());
        assertEquals(1, r.pos());
        r.mark();
        assertEquals('b', r.consume());
        r.advance();
        assertEquals('d', r.current());
        r.rewindToMark();
        assertEquals(1, r.pos());
        assertEquals('b', r.consume());
        r.unconsume();
        assertEquals(1, r.pos());
        assertEquals('b', r.current());
    }

    // Tests nextIndexOf for char and CharSequence
    @Test
    public void testNextIndexOf_charAndSequence_returnsExpectedOffset() {
        CharacterReader r = new CharacterReader("one two three two");
        assertEquals(3, r.nextIndexOf(' '));
        assertEquals(-1, r.nextIndexOf('z'));
        assertEquals(4, r.nextIndexOf("two"));
        assertEquals(-1, r.nextIndexOf("four"));
    }

    // Tests consumeTo with char and with sequence
    @Test
    public void testConsumeTo_matchingAndNonMatching_consumesCorrectString() {
        CharacterReader r1 = new CharacterReader("hello world");
        assertEquals("hello", r1.consumeTo(' '));
        assertEquals(' ', r1.current());

        CharacterReader r2 = new CharacterReader("foo bar baz");
        assertEquals("foo ", r2.consumeTo("bar"));
        assertEquals("bar", r2.consumeTo("qux")); // not found -> consumeToEnd

        CharacterReader r3 = new CharacterReader("testing");
        assertEquals("testing", r3.consumeTo('z')); // not found -> consumeToEnd
    }

    // Tests consumeToAny and consumeToAnySorted
    @Test
    public void testConsumeToAny_variousDelimiters_stopsAtFirstMatch() {
        CharacterReader r1 = new CharacterReader("apple,banana;cherry");
        assertEquals("apple", r1.consumeToAny(',', ';'));
        r1.advance();
        assertEquals("banana", r1.consumeToAnySorted(new char[]{',', ';'}));

        CharacterReader r2 = new CharacterReader("no delimiters");
        assertEquals("no delimiters", r2.consumeToAny('x', 'y', 'z'));
    }

    // Tests consumeData and consumeTagName
    @Test
    public void testConsumeDataAndTagName_specialCharacters_stopsAtDelimiters() {
        CharacterReader rData = new CharacterReader("data&more<tag\0null");
        assertEquals("data", rData.consumeData());
        rData.advance(); // skip &
        assertEquals("more", rData.consumeData());

        CharacterReader rTag = new CharacterReader("tag-name<next");
        assertEquals("tag-name", rTag.consumeTagName());

        CharacterReader rTagSpace = new CharacterReader("div class='test'");
        assertEquals("div", rTagSpace.consumeTagName());
    }

    // Tests consumeLetterSequence, consumeDigitSequence, and consumeHexSequence
    @Test
    public void testConsumeSequences_lettersDigitsHex_returnsMatchingTokens() {
        CharacterReader r = new CharacterReader("abcDEF1234g56");
        assertEquals("abcDEF", r.consumeLetterSequence());
        assertEquals("1234", r.consumeDigitSequence());

        CharacterReader rHex = new CharacterReader("1aF8z99");
        assertEquals("1aF8", rHex.consumeHexSequence());
    }

    // Tests consumeLetterThenDigitSequence
    @Test
    public void testConsumeLetterThenDigitSequence_lettersFollowedByDigits_consumesBoth() {
        CharacterReader r1 = new CharacterReader("h123 and more");
        assertEquals("h123", r1.consumeLetterThenDigitSequence());

        CharacterReader r2 = new CharacterReader("header only");
        assertEquals("header", r2.consumeLetterThenDigitSequence());

        CharacterReader r3 = new CharacterReader("123noletters");
        assertEquals("", r3.consumeLetterThenDigitSequence());
    }

    // Tests matches, matchesIgnoreCase, matchesAny, and matchesAnySorted
    @Test
    public void testMatches_variousPatterns_returnsExpectedBoolean() {
        CharacterReader r = new CharacterReader("One Two 3");
        assertTrue(r.matches('O'));
        assertFalse(r.matches('o'));
        assertTrue(r.matches("One"));
        assertFalse(r.matches("one"));
        assertTrue(r.matchesIgnoreCase("one"));
        assertFalse(r.matchesIgnoreCase("two"));
        assertTrue(r.matchesAny('X', 'O', 'Y'));
        assertFalse(r.matchesAny('x', 'y', 'z'));
        assertTrue(r.matchesAnySorted(new char[]{'A', 'O', 'Z'}));
        assertFalse(r.matchesAnySorted(new char[]{'A', 'B', 'C'}));
        assertTrue(r.matchesLetter());
        assertFalse(r.matchesDigit());

        CharacterReader rEmpty = new CharacterReader("");
        assertFalse(rEmpty.matches('a'));
        assertFalse(rEmpty.matches("abc"));
        assertFalse(rEmpty.matchesIgnoreCase("abc"));
        assertFalse(rEmpty.matchesAny('a', 'b'));
        assertFalse(rEmpty.matchesAnySorted(new char[]{'a'}));
        assertFalse(rEmpty.matchesLetter());
        assertFalse(rEmpty.matchesDigit());
    }

    // Tests matchConsume and matchConsumeIgnoreCase
    @Test
    public void testMatchConsume_matchingAndNonMatching_consumesOnlyOnMatch() {
        CharacterReader r = new CharacterReader("<html><BODY>");
        assertTrue(r.matchConsume("<html>"));
        assertEquals(6, r.pos());
        assertFalse(r.matchConsume("<head>"));
        assertEquals(6, r.pos());
        assertTrue(r.matchConsumeIgnoreCase("<body>"));
        assertEquals(12, r.pos());
        assertTrue(r.isEmpty());
    }

    // Tests containsIgnoreCase
    @Test
    public void testContainsIgnoreCase_targetPresentOrAbsent_returnsExpectedBoolean() {
        CharacterReader r = new CharacterReader("Some </Title> content");
        assertTrue(r.containsIgnoreCase("</title>"));
        assertTrue(r.containsIgnoreCase("</TITLE>"));
        assertFalse(r.containsIgnoreCase("</style>"));
    }

    // Tests string caching with repeated short strings and long strings
    @Test
    public void testStringCache_repeatedStrings_reusesCachedString() {
        CharacterReader r = new CharacterReader("div div a-very-long-tag-name-that-exceeds-cache div");
        String s1 = r.consumeTagName();
        r.advance(); // skip space
        String s2 = r.consumeTagName();
        r.advance();
        String s3 = r.consumeTagName();
        r.advance();
        String s4 = r.consumeTagName();

        assertEquals("div", s1);
        assertEquals("div", s2);
        assertTrue(s1 == s2); // Flywheel cache should return same instance
        assertEquals("a-very-long-tag-name-that-exceeds-cache", s3);
        assertEquals("div", s4);
        assertTrue(s1 == s4);
    }

    // Tests rangeEquals edge cases including offset and empty range
    @Test
    public void testRangeEquals_variousRanges_returnsCorrectBoolean() {
        CharacterReader r = new CharacterReader("sample text");
        assertTrue(r.rangeEquals(0, 6, "sample"));
        assertFalse(r.rangeEquals(0, 6, "simple"));
        assertFalse(r.rangeEquals(0, 5, "sample")); // length mismatch
        assertTrue(r.rangeEquals(0, 0, ""));
        assertTrue(CharacterReader.rangeEquals("sample".toCharArray(), 0, 6, "sample"));
        assertFalse(CharacterReader.rangeEquals("sample".toCharArray(), 0, 6, "different"));
    }

    // Tests bufferUp via custom Reader with size smaller than total input
    @Test
    public void testBufferUp_bufferedReader_readsBeyondBuffer() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("token").append(i).append(" ");
        }
        CharacterReader r = new CharacterReader(new StringReader(sb.toString()), 64);
        String consumed = r.consumeTo(' ');
        assertEquals("token0", consumed);
        r.advance();
        String all = r.consumeToEnd();
        assertTrue(all.startsWith("token1 "));
        assertTrue(r.isEmpty());
    }
}