package org.apache.commons.lang3.text.translate;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class NumericEntityUnescaperTest {

    private NumericEntityUnescaper unescaper;

    @Before
    public void setUp() {
        unescaper = new NumericEntityUnescaper();
    }

    // Tests standard decimal entity translation with semicolon
    @Test
    public void testTranslate_decimalEntityWithSemicolon_translatesSuccessfully() throws IOException {
        String input = "&#65;";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);
        assertEquals(5, consumed);
        assertEquals("A", out.toString());
    }

    // Tests lowercase hex entity translation with semicolon
    @Test
    public void testTranslate_hexEntityLowerCaseWithSemicolon_translatesSuccessfully() throws IOException {
        String input = "&#x41;";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);
        assertEquals(6, consumed);
        assertEquals("A", out.toString());
    }

    // Tests uppercase hex entity translation with semicolon
    @Test
    public void testTranslate_hexEntityUpperCaseWithSemicolon_translatesSuccessfully() throws IOException {
        String input = "&#X42;";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);
        assertEquals(6, consumed);
        assertEquals("B", out.toString());
    }

    // Tests supplementary character translation (code point > 0xFFFF)
    @Test
    public void testTranslate_supplementaryCharacter_translatesSupplementaryCorrectly() throws IOException {
        String input = "&#x10000;";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);
        assertEquals(9, consumed);
        assertEquals(new String(Character.toChars(0x10000)), out.toString());
    }

    // Tests non-entity prefix returns zero consumed characters
    @Test
    public void testTranslate_notAnEntity_returnsZero() throws IOException {
        String input = "Hello";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);
        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    // Tests ampersand followed by non-hash character returns zero
    @Test
    public void testTranslate_ampersandWithoutHash_returnsZero() throws IOException {
        String input = "&abc;";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);
        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    // Tests single ampersand at the end of input
    @Test
    public void testTranslate_ampersandAtEndOfInput_returnsZero() throws IOException {
        String input = "&";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);
        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    // Tests invalid entity causing NumberFormatException returns zero
    @Test
    public void testTranslate_invalidNumberFormat_returnsZero() throws IOException {
        String input = "&#xyz;";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);
        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    // Tests unfinished entity "&#" at the end of input without remaining characters
    @Test
    public void testTranslate_unfinishedEntityAtEnd_doesNotThrowOutOfBounds() throws IOException {
        String input = "&#";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);
        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    // Tests unfinished hex entity "&#x" at the end of input without remaining characters
    @Test
    public void testTranslate_unfinishedHexEntityAtEnd_doesNotThrowOutOfBounds() throws IOException {
        String input = "&#x";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);
        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    // Tests decimal entity without closing semicolon
    @Test
    public void testTranslate_decimalEntityWithoutSemicolon_translatesSuccessfully() throws IOException {
        String input = "&#65";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);
        assertEquals(4, consumed);
        assertEquals("A", out.toString());
    }

    // Tests hex entity without closing semicolon
    @Test
    public void testTranslate_hexEntityWithoutSemicolon_translatesSuccessfully() throws IOException {
        String input = "&#x41";
        StringWriter out = new StringWriter();
        int consumed = unescaper.translate(input, 0, out);
        assertEquals(5, consumed);
        assertEquals("A", out.toString());
    }
}