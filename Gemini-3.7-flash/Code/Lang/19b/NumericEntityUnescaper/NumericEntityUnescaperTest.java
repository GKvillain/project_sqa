package org.apache.commons.lang3.text.translate;

import org.junit.Test;
import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class NumericEntityUnescaperTest {

    // Tests translating standard decimal entity with semicolon
    @Test
    public void testTranslate_decimalEntityWithSemicolon_translatesCorrectly() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter writer = new StringWriter();
        String input = "&#65;";
        int consumed = unescaper.translate(input, 0, writer);

        assertEquals(5, consumed);
        assertEquals("A", writer.toString());
    }

    // Tests translating standard hex entity with lowercase 'x' and semicolon
    @Test
    public void testTranslate_hexEntityLowerCaseX_translatesCorrectly() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter writer = new StringWriter();
        String input = "&#x41;";
        int consumed = unescaper.translate(input, 0, writer);

        assertEquals(6, consumed);
        assertEquals("A", writer.toString());
    }

    // Tests translating standard hex entity with uppercase 'X' and semicolon
    @Test
    public void testTranslate_hexEntityUpperCaseX_translatesCorrectly() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter writer = new StringWriter();
        String input = "&#X42;";
        int consumed = unescaper.translate(input, 0, writer);

        assertEquals(6, consumed);
        assertEquals("B", writer.toString());
    }

    // Tests translating supplementary character code point (> 0xFFFF)
    @Test
    public void testTranslate_supplementaryCodePoint_writesSurrogatePair() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter writer = new StringWriter();
        String input = "&#x10000;";
        int consumed = unescaper.translate(input, 0, writer);

        assertEquals(9, consumed);
        assertEquals("\uD800\uDC00", writer.toString());
    }

    // Tests input that does not start with '&'
    @Test
    public void testTranslate_notAnEntity_returnsZero() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter writer = new StringWriter();
        String input = "Hello";
        int consumed = unescaper.translate(input, 0, writer);

        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    // Tests input with only '&' at the end of the sequence
    @Test
    public void testTranslate_ampersandAtEndOfInput_returnsZero() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter writer = new StringWriter();
        String input = "&";
        int consumed = unescaper.translate(input, 0, writer);

        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    // Tests incomplete entity ending with "&#" at the end of string (Defect Lang-19 boundary)
    @Test
    public void testTranslate_ampersandHashAtEndOfInput_doesNotThrowException() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter writer = new StringWriter();
        String input = "&#";
        int consumed = unescaper.translate(input, 0, writer);

        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    // Tests incomplete entity ending with "&#x" at the end of string
    @Test
    public void testTranslate_hexPrefixAtEndOfInput_doesNotThrowException() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter writer = new StringWriter();
        String input = "&#x";
        int consumed = unescaper.translate(input, 0, writer);

        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    // Tests entity without terminating semicolon (supports semi-colon optional behavior)
    @Test
    public void testTranslate_entityWithoutSemicolon_translatesCorrectly() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter writer = new StringWriter();
        String input = "&#65";
        int consumed = unescaper.translate(input, 0, writer);

        assertEquals(4, consumed);
        assertEquals("A", writer.toString());
    }

    // Tests entity followed by non-digits and semicolon leading to NumberFormatException
    @Test
    public void testTranslate_invalidNumberFormat_returnsZero() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter writer = new StringWriter();
        String input = "&#XYZ;";
        int consumed = unescaper.translate(input, 0, writer);

        assertEquals(0, consumed);
        assertEquals("", writer.toString());
    }

    // Tests translating entity located at non-zero index
    @Test
    public void testTranslate_entityAtNonZeroIndex_translatesCorrectly() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter writer = new StringWriter();
        String input = "abc&#65;def";
        int consumed = unescaper.translate(input, 3, writer);

        assertEquals(5, consumed);
        assertEquals("A", writer.toString());
    }

    // Tests translate method inherited from CharSequenceTranslator with full string
    @Test
    public void testTranslate_fullStringTranslation_unescapesEntities() {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        String input = "Test &#65; and &#x42; end";
        String result = unescaper.translate(input);

        assertEquals("Test A and B end", result);
    }
}