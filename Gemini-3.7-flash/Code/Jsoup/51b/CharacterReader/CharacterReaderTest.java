package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class CharacterReaderTest {

    // Tests null input throws IllegalArgumentException
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullInput_throwsException() {
        new CharacterReader(null);
    }

    // Tests consume, current, advance, unconsume, and isEmpty on normal input
    @Test
    public void testConsume_basicNavigation_returnsExpectedCharsAndAdvances() {
        CharacterReader r = new CharacterReader("abc");
        assertEquals(0, r.pos());
        assertFalse(r.isEmpty());
        assertEquals('a', r.current());
        assertEquals('a', r.consume());
        assertEquals(1, r.pos());
        assertEquals('b', r.consume());
        r.unconsume();
        assertEquals(1, r.pos());
        assertEquals('b', r.current());
        r.advance();
        assertEquals(2, r.pos());
        assertEquals('c', r.consume());
        assertTrue(r.isEmpty());
        assertEquals(CharacterReader.EOF, r.current());
        assertEquals(CharacterReader.EOF, r.consume());
    }

    // Tests mark and rewindToMark
    @Test
    public void testMark_andRewindToMark_restoresPosition() {
        CharacterReader r = new CharacterReader("hello world");
        r.consume(); // h
        r.consume(); // e
        r.mark();
        r.consume(); // l
        r.consume(); // l
        assertEquals(4, r.pos());
        r.rewindToMark();
        assertEquals(2, r.pos());
        assertEquals('l', r.current());
    }

    // Tests consumeAsString
    @Test
    public void testConsumeAsString_validInput_returnsSingleCharacterString() {
        CharacterReader r = new CharacterReader("test");
        assertEquals("t", r.consumeAsString());
        assertEquals(1, r.pos());
        assertEquals("e", r.consumeAsString());
        assertEquals(2, r.pos());
    }

    // Tests nextIndexOf for char and CharSequence
    @Test
    public void testNextIndexOf_charAndSequence_returnsCorrectOffset() {
        CharacterReader r = new CharacterReader("one two three two");
        assertEquals(3, r.nextIndexOf(' '));
        assertEquals(-1, r.nextIndexOf('z'));
        assertEquals(4, r.nextIndexOf("two"));
        assertEquals(-1, r.nextIndexOf("four"));
        r.consumeTo('t');
        assertEquals(0, r.nextIndexOf("two"));
    }

    // Tests consumeTo char
    @Test
    public void testConsumeTo_char_consumesUntilTargetOrEnd() {
        CharacterReader r1 = new CharacterReader("foo/bar");
        assertEquals("foo", r1.consumeTo('/'));
        assertEquals('/', r1.current());

        CharacterReader r2 = new CharacterReader("foobar");
        assertEquals("foobar", r2.consumeTo('/'));
        assertTrue(r2.isEmpty());
    }

    // Tests consumeTo String
    @Test
    public void testConsumeTo_string_consumesUntilSequenceOrEnd() {
        CharacterReader r1 = new CharacterReader("Hello <!-- comment --> World");
        assertEquals("Hello ", r1.consumeTo("<!--"));
        assertEquals("<!--", r1.consumeTo(" comment"));

        CharacterReader r2 = new CharacterReader("foobar");
        assertEquals("foobar", r2.consumeTo("baz"));
        assertTrue(r2.isEmpty());
    }

    // Tests consumeToAny
    @Test
    public void testConsumeToAny_variousChars_consumesUntilMatch() {
        CharacterReader r = new CharacterReader("foo & bar < baz");
        assertEquals("foo ", r.consumeToAny('&', '<'));
        assertEquals('&', r.consume());
        assertEquals(" bar ", r.consumeToAny('&', '<'));
        assertEquals('<', r.consume());
        assertEquals(" baz", r.consumeToAny('&', '<'));
        assertTrue(r.isEmpty());
        assertEquals("", r.consumeToAny('&', '<'));
    }

    // Tests consumeToAnySorted
    @Test
    public void testConsumeToAnySorted_sortedArray_consumesUntilMatch() {
        char[] delims = new char[]{' ', '-', '/'};
        CharacterReader r = new CharacterReader("path/to-file name");
        assertEquals("path", r.consumeToAnySorted(delims));
        assertEquals('/', r.consume());
        assertEquals("to", r.consumeToAnySorted(delims));
        assertEquals('-', r.consume());
        assertEquals("file", r.consumeToAnySorted(delims));
        assertEquals(' ', r.consume());
        assertEquals("name", r.consumeToAnySorted(delims));
        assertTrue(r.isEmpty());
    }

    // Tests consumeData
    @Test
    public void testConsumeData_stopsAtSpecialChars() {
        CharacterReader r = new CharacterReader("text&more<text" + TokeniserState.nullChar + "end");
        assertEquals("text", r.consumeData());
        assertEquals('&', r.consume());
        assertEquals("more", r.consumeData());
        assertEquals('<', r.consume());
        assertEquals("text", r.consumeData());
        assertEquals(TokeniserState.nullChar, r.consume());
        assertEquals("end", r.consumeData());
        assertTrue(r.isEmpty());
    }

    // Tests consumeTagName
    @Test
    public void testConsumeTagName_stopsAtTagNameDelimiters() {
        CharacterReader r1 = new CharacterReader("div class='foo'");
        assertEquals("div", r1.consumeTagName());

        CharacterReader r2 = new CharacterReader("span/ >");
        assertEquals("span", r2.consumeTagName());

        CharacterReader r3 = new CharacterReader("br\r\n\t\f");
        assertEquals("br", r3.consumeTagName());
    }

    // Tests consumeToEnd
    @Test
    public void testConsumeToEnd_fromStartAndMiddle_consumesRemaining() {
        CharacterReader r = new CharacterReader("consume all of this");
        r.consumeTo('a');
        assertEquals("all of this", r.consumeToEnd());
        assertTrue(r.isEmpty());
        assertEquals("", r.consumeToEnd());
    }

    // Tests consumeLetterSequence and consumeLetterThenDigitSequence
    @Test
    public void testConsumeLetterSequence_andLetterThenDigitSequence_consumesCorrectly() {
        CharacterReader r1 = new CharacterReader("abcDEF123!@#");
        assertEquals("abcDEF", r1.consumeLetterSequence());
        assertEquals('1', r1.current());

        CharacterReader r2 = new CharacterReader("var123 = 5;");
        assertEquals("var123", r2.consumeLetterThenDigitSequence());
        assertEquals(' ', r2.current());

        CharacterReader r3 = new CharacterReader("123abc");
        assertEquals("", r3.consumeLetterSequence());
    }

    // Tests consumeHexSequence and consumeDigitSequence
    @Test
    public void testConsumeHexSequence_andDigitSequence_consumesCorrectly() {
        CharacterReader r1 = new CharacterReader("0123456789ABCDEFabcdefGHI");
        assertEquals("0123456789ABCDEFabcdef", r1.consumeHexSequence());
        assertEquals('G', r1.current());

        CharacterReader r2 = new CharacterReader("9876543210abc");
        assertEquals("9876543210", r2.consumeDigitSequence());
        assertEquals('a', r2.current());
    }

    // Tests matches and matchesIgnoreCase
    @Test
    public void testMatches_andMatchesIgnoreCase_returnsCorrectBoolean() {
        CharacterReader r = new CharacterReader("HelloWorld");
        assertTrue(r.matches('H'));
        assertFalse(r.matches('h'));
        assertTrue(r.matches("Hello"));
        assertFalse(r.matches("hello"));
        assertFalse(r.matches("HelloWorldLongerThanInput"));

        assertTrue(r.matchesIgnoreCase("HELLO"));
        assertTrue(r.matchesIgnoreCase("helloworld"));
        assertFalse(r.matchesIgnoreCase("hellothere"));
        assertFalse(r.matchesIgnoreCase("HelloWorldLongerThanInput"));
    }

    // Tests matchesAny and matchesAnySorted
    @Test
    public void testMatchesAny_andMatchesAnySorted_returnsCorrectBoolean() {
        CharacterReader r = new CharacterReader("test");
        assertTrue(r.matchesAny('a', 't', 'z'));
        assertFalse(r.matchesAny('x', 'y', 'z'));

        char[] sorted = new char[]{'a', 'm', 't', 'z'};
        assertTrue(r.matchesAnySorted(sorted));

        char[] notInSorted = new char[]{'a', 'b', 'c'};
        assertFalse(r.matchesAnySorted(notInSorted));

        r.consumeToEnd();
        assertFalse(r.matchesAny('t'));
        assertFalse(r.matchesAnySorted(sorted));
    }

    // Tests matchesLetter and matchesDigit
    @Test
    public void testMatchesLetter_andMatchesDigit_identifiesCategories() {
        CharacterReader rLetterLower = new CharacterReader("a");
        assertTrue(rLetterLower.matchesLetter());
        assertFalse(rLetterLower.matchesDigit());

        CharacterReader rLetterUpper = new CharacterReader("Z");
        assertTrue(rLetterUpper.matchesLetter());
        assertFalse(rLetterUpper.matchesDigit());

        CharacterReader rDigit = new CharacterReader("5");
        assertFalse(rDigit.matchesLetter());
        assertTrue(rDigit.matchesDigit());

        CharacterReader rSymbol = new CharacterReader("!");
        assertFalse(rSymbol.matchesLetter());
        assertFalse(rSymbol.matchesDigit());

        CharacterReader rEmpty = new CharacterReader("");
        assertFalse(rEmpty.matchesLetter());
        assertFalse(rEmpty.matchesDigit());

        CharacterReader rUnicode = new CharacterReader("ü");
        assertFalse(rUnicode.matchesLetter());
    }

    // Tests matchConsume and matchConsumeIgnoreCase
    @Test
    public void testMatchConsume_andMatchConsumeIgnoreCase_advancesOnMatch() {
        CharacterReader r = new CharacterReader("OneTwoThree");
        assertFalse(r.matchConsume("one"));
        assertEquals(0, r.pos());
        assertTrue(r.matchConsume("One"));
        assertEquals(3, r.pos());

        assertFalse(r.matchConsumeIgnoreCase("four"));
        assertEquals(3, r.pos());
        assertTrue(r.matchConsumeIgnoreCase("TWO"));
        assertEquals(6, r.pos());
    }

    // Tests containsIgnoreCase
    @Test
    public void testContainsIgnoreCase_detectsCaseInsensitiveSubstrings() {
        CharacterReader r = new CharacterReader("<html><HEAD><Title>Page</Title></head></html>");
        assertTrue(r.containsIgnoreCase("</title>"));
        assertTrue(r.containsIgnoreCase("</head>"));
        assertFalse(r.containsIgnoreCase("</body>"));
    }

    // Tests stringCache hit, collision, rangeEquals, and toString
    @Test
    public void testCacheString_andRangeEquals_andToString() {
        CharacterReader r = new CharacterReader("tag tag tag longerThanMaxCacheLengthString");
        String t1 = r.consumeTo(' ');
        r.consume(); // ' '
        String t2 = r.consumeTo(' ');
        r.consume(); // ' '
        String t3 = r.consumeTo(' ');
        r.consume(); // ' '

        assertSame(t1, t2);
        assertSame(t1, t3);
        assertEquals("tag", t1);

        String longStr = r.consumeToEnd();
        assertEquals("longerThanMaxCacheLengthString", longStr);

        CharacterReader r2 = new CharacterReader("fullcontent");
        assertEquals("fullcontent", r2.toString());
        r2.consume();
        assertEquals("ullcontent", r2.toString());
    }
}