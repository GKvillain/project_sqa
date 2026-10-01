package com.fasterxml.jackson.core.json;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.BufferRecycler;

public class UTF8JsonGeneratorTest {
    private ByteArrayOutputStream out;
    private UTF8JsonGenerator gen;
    private IOContext ctxt;

    @Before
    public void setUp() throws Exception {
        out = new ByteArrayOutputStream();
        ctxt = new IOContext(new BufferRecycler(), null, false);
        gen = new UTF8JsonGenerator(ctxt, 0, null, out);
    }

    @After
    public void tearDown() throws Exception {
        if (gen != null) {
            gen.close();
        }
    }

    @Test
    // Tests writing empty string produces just quotes
    public void testWriteString_emptyString_writesQuotes() throws IOException {
        gen.writeString("");
        gen.flush();
        String result = out.toString("UTF-8");
        assertEquals("\"\"", result);
    }

    @Test
    // Tests writing null string writes "null" without quotes
    public void testWriteString_nullValue_writesNull() throws IOException {
        gen.writeString(null);
        gen.flush();
        String result = out.toString("UTF-8");
        assertEquals("null", result);
    }

    @Test
    // Tests simple ASCII string is enclosed in quotes
    public void testWriteString_simpleString_writesCorrectly() throws IOException {
        gen.writeString("hello");
        gen.flush();
        String result = out.toString("UTF-8");
        assertEquals("\"hello\"", result);
    }

    @Test
    // Tests control characters (LF, CR, TAB) are escaped correctly
    public void testWriteString_stringWithControlChars_escapesCorrectly() throws IOException {
        String input = "line1\nline2\rline3\tend";
        gen.writeString(input);
        gen.flush();
        String result = out.toString("UTF-8");
        assertEquals("\"line1\\nline2\\rline3\\tend\"", result);
    }

    @Test
    // Tests non-ASCII characters are UTF-8 encoded
    public void testWriteString_stringWithNonAscii_utf8Encodes() throws IOException {
        String input = "café";
        gen.writeString(input);
        gen.flush();
        String result = out.toString("UTF-8");
        assertEquals("\"café\"", result);
    }

    @Test
    // Tests surrogate pair (supplementary character) is encoded as UTF-8, not as unicode escapes
    // This test targets the defect (bug 18b) where the generator incorrectly wrote \u escapes
    public void testWriteString_surrogatePair_encodesSupplementary() throws IOException {
        String input = new String(new int[]{0x1D11E}, 0, 1); // musical symbol G clef
        gen.writeString(input);
        gen.flush();
        byte[] outputBytes = out.toByteArray();
        // Check that no unicode escape "\u" appears in the output
        String outputStr = new String(outputBytes, "UTF-8");
        assertFalse("Output should not contain unicode escape \\u", outputStr.contains("\\u"));
        // Verify that the decoded string between quotes matches the input
        int start = (outputBytes[0] == '"') ? 1 : 0;
        int end = (outputBytes[outputBytes.length - 1] == '"') ? outputBytes.length - 1 : outputBytes.length;
        String decodedContent = new String(outputBytes, start, end - start, "UTF-8");
        assertEquals(input, decodedContent);
    }

    @Test
    // Tests long string that exceeds one segment boundary
    public void testWriteString_longString_exceedsBuffer_handlesSegments() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append('a');
        }
        String input = sb.toString();
        gen.writeString(input);
        gen.flush();
        byte[] outputBytes = out.toByteArray();
        assertEquals(1002, outputBytes.length);
        assertEquals('"', outputBytes[0]);
        assertEquals('"', outputBytes[1001]);
        String content = new String(outputBytes, 1, 1000, "UTF-8");
        assertEquals(input, content);
    }

    @Test
    // Tests backslash and quote inside string are escaped
    public void testWriteString_backslashAndQuote_escapesProperly() throws IOException {
        String input = "back\\slash and \"quote\"";
        gen.writeString(input);
        gen.flush();
        String result = out.toString("UTF-8");
        assertEquals("\"back\\\\slash and \\\"quote\\\"\"", result);
    }

    @Test
    // Tests simple field name in an object
    public void testWriteFieldName_simpleField_writesCorrectly() throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("name");
        gen.writeString("value");
        gen.writeEndObject();
        gen.flush();
        String result = out.toString("UTF-8");
        assertEquals("{\"name\":\"value\"}", result);
    }

    @Test
    // Tests integer number
    public void testWriteNumber_integer_writesDigits() throws IOException {
        gen.writeNumber(12345);
        gen.flush();
        String result = out.toString("UTF-8");
        assertEquals("12345", result);
    }

    @Test
    // Tests negative integer
    public void testWriteNumber_negativeInt_writesMinusSign() throws IOException {
        gen.writeNumber(-100);
        gen.flush();
        String result = out.toString("UTF-8");
        assertEquals("-100", result);
    }

    @Test
    // Tests zero
    public void testWriteNumber_zero_writesZero() throws IOException {
        gen.writeNumber(0);
        gen.flush();
        String result = out.toString("UTF-8");
        assertEquals("0", result);
    }

    @Test
    // Tests true boolean
    public void testWriteBoolean_true_writesTrue() throws IOException {
        gen.writeBoolean(true);
        gen.flush();
        String result = out.toString("UTF-8");
        assertEquals("true", result);
    }

    @Test
    // Tests false boolean
    public void testWriteBoolean_false_writesFalse() throws IOException {
        gen.writeBoolean(false);
        gen.flush();
        String result = out.toString("UTF-8");
        assertEquals("false", result);
    }

    @Test
    // Tests null value
    public void testWriteNull_writesNull() throws IOException {
        gen.writeNull();
        gen.flush();
        String result = out.toString("UTF-8");
        assertEquals("null", result);
    }

    @Test
    // Tests array brackets
    public void testWriteStartArray_writeEndArray_writesBrackets() throws IOException {
        gen.writeStartArray();
        gen.writeEndArray();
        gen.flush();
        String result = out.toString("UTF-8");
        assertEquals("[]", result);
    }

    @Test
    // Tests object braces
    public void testWriteStartObject_writeEndObject_writesBraces() throws IOException {
        gen.writeStartObject();
        gen.writeEndObject();
        gen.flush();
        String result = out.toString("UTF-8");
        assertEquals("{}", result);
    }

    // ================== New test cases for uncovered coverage ==================

    @Test
    // Tests long value that exceeds int range
    public void testWriteNumber_longValue_writesCorrectly() throws IOException {
        long value = 3000000000L; // larger than Integer.MAX_VALUE
        gen.writeNumber(value);
        gen.flush();
        String result = out.toString("UTF-8");
        assertEquals("3000000000", result);
    }

    @Test
    // Tests negative long value
    public void testWriteNumber_negativeLong_writesMinusSign() throws IOException {
        long value = -2147483649L; // less than Integer.MIN_VALUE
        gen.writeNumber(value);
        gen.flush();
        String result = out.toString("UTF-8");
        assertEquals("-2147483649", result);
    }

    @Test
    // Tests double value with fraction
    public void testWriteNumber_doubleWithFraction_writesCorrectly() throws IOException {
        gen.writeNumber(3.14159);
        gen.flush();
        String result = out.toString("UTF-8");
        // Jackson outputs double with enough precision, default is to use Double.toString
        assertTrue("Result should contain decimal point", result.contains("."));
        // Verify that it is a valid number (not NaN or Infinity)
        assertEquals("3.14159", result);
    }

    @Test
    // Tests negative double value
    public void testWriteNumber_negativeDouble_writesMinusSign() throws IOException {
        gen.writeNumber(-2.5);
        gen.flush();
        String result = out.toString("UTF-8");
        assertEquals("-2.5", result);
    }

    @Test
    // Tests BigDecimal value
    public void testWriteNumber_BigDecimal_writesCorrectly() throws IOException {
        BigDecimal value = new BigDecimal("12345678901234567890.123456789");
        gen.writeNumber(value);
        gen.flush();
        String result = out.toString("UTF-8");
        assertEquals(value.toString(), result);
    }

    @Test
    // Tests writeRaw with a simple string
    public void testWriteRaw_simpleString_writesWithoutQuotes() throws IOException {
        gen.writeRaw("raw text");
        gen.flush();
        String result = out.toString("UTF-8");
        assertEquals("raw text", result);
    }

    @Test
    // Tests writeRawValue (should be identical to writeRaw for unquoted value)
    public void testWriteRawValue_simpleString_writesWithoutQuotes() throws IOException {
        gen.writeRawValue("12345");
        gen.flush();
        String result = out.toString("UTF-8");
        assertEquals("12345", result);
    }

    @Test
    // Tests that calling flush after close does not throw exception
    public void testFlushAfterClose_doesNotThrow() throws IOException {
        gen.close();
        // Should not throw
        gen.flush();
    }

    @Test
    // Tests that writeString with char array works correctly
    public void testWriteString_charArray_writesSameAsString() throws IOException {
        char[] chars = "hello".toCharArray();
        gen.writeString(chars, 0, chars.length);
        gen.flush();
        String result = out.toString("UTF-8");
        assertEquals("\"hello\"", result);
    }

    @Test(expected = JsonGenerationException.class)
    // Tests that writing a field name with null value throws an exception (if Jackson does so)
    public void testWriteFieldName_null_throwsException() throws IOException {
        gen.writeStartObject();
        gen.writeFieldName((String) null);
    }
}