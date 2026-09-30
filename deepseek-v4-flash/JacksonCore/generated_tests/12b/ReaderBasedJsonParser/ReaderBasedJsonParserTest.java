package com.fasterxml.jackson.core.json;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.*;
import com.fasterxml.jackson.core.*;

public class ReaderBasedJsonParserTest {

    // Helper: tiny chunk reader to force buffer reloads
    private static class ChunkedReader extends Reader {
        private final Reader delegate;
        private final int chunkSize;
        ChunkedReader(Reader delegate, int chunkSize) {
            this.delegate = delegate;
            this.chunkSize = chunkSize;
        }
        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            return delegate.read(cbuf, off, Math.min(len, chunkSize));
        }
        @Override
        public void close() throws IOException { delegate.close(); }
    }

    // Helper: create parser with default factory
    private JsonParser createParser(String json) throws IOException {
        JsonFactory factory = new JsonFactory();
        return factory.createParser(new StringReader(json));
    }

    // Helper: create parser with chunked reader (forces buffer boundary)
    private JsonParser createChunkedParser(String json, int chunk) throws IOException {
        JsonFactory factory = new JsonFactory();
        return factory.createParser(new ChunkedReader(new StringReader(json), chunk));
    }

    // Normal positive integer
    @Test
    public void testParsePositiveInteger_returnsCorrectValue() throws Exception {
        JsonParser p = createParser("123");
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(123, p.getIntValue());
        assertEquals("123", p.getText());
        p.close();
    }

    // Normal negative integer
    @Test
    public void testParseNegativeInteger_returnsCorrectValue() throws Exception {
        JsonParser p = createParser("-42");
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(-42, p.getIntValue());
        assertEquals("-42", p.getText());
        p.close();
    }

    // Normal floating point number
    @Test
    public void testParseFloat_returnsCorrectValue() throws Exception {
        JsonParser p = createParser("3.14");
        assertSame(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(3.14, p.getDoubleValue(), 1e-9);
        assertEquals("3.14", p.getText());
        p.close();
    }

    // Normal string
    @Test
    public void testParseString_returnsCorrectText() throws Exception {
        JsonParser p = createParser("\"hello\"");
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("hello", p.getText());
        p.close();
    }

    // Field name and string value in object
    @Test
    public void testParseFieldNameAndValue_returnsCorrectTokens() throws Exception {
        JsonParser p = createParser("{\"key\":\"value\"}");
        assertSame(JsonToken.START_OBJECT, p.nextToken());
        assertSame(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("key", p.getCurrentName());
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("value", p.getText());
        assertSame(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    // Array with integers
    @Test
    public void testParseArray_returnsCorrectStructure() throws Exception {
        JsonParser p = createParser("[1,2,3]");
        assertSame(JsonToken.START_ARRAY, p.nextToken());
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(3, p.getIntValue());
        assertSame(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    // Boolean true
    @Test
    public void testParseTrue_returnsTokenTrue() throws Exception {
        JsonParser p = createParser("true");
        assertSame(JsonToken.VALUE_TRUE, p.nextToken());
        assertTrue(p.getBooleanValue());
        p.close();
    }

    // Boolean false
    @Test
    public void testParseFalse_returnsTokenFalse() throws Exception {
        JsonParser p = createParser("false");
        assertSame(JsonToken.VALUE_FALSE, p.nextToken());
        assertFalse(p.getBooleanValue());
        p.close();
    }

    // Null
    @Test
    public void testParseNull_returnsTokenNull() throws Exception {
        JsonParser p = createParser("null");
        assertSame(JsonToken.VALUE_NULL, p.nextToken());
        assertNull(p.getValueAsString());
        p.close();
    }

    // Leading zeros are not allowed by default
    @Test(expected = JsonParseException.class)
    public void testParseLeadingZero_throwsException() throws Exception {
        JsonParser p = createParser("00");
        p.nextToken();
        p.close();
    }

    // Negative leading zeros are not allowed by default
    @Test(expected = JsonParseException.class)
    public void testParseNegativeLeadingZero_throwsException() throws Exception {
        JsonParser p = createParser("-00");
        p.nextToken();
        p.close();
    }

    // Minus sign without digit
    @Test(expected = JsonParseException.class)
    public void testParseMinusOnly_throwsException() throws Exception {
        JsonParser p = createParser("-");
        p.nextToken();
        p.close();
    }

    // Negative number split across buffer boundary (chunk size 1)
    @Test
    public void testParseNegativeIntegerSplit_returnsCorrectValue() throws Exception {
        JsonParser p = createChunkedParser("-12345", 1);
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(-12345, p.getIntValue());
        p.close();
    }

    // Positive long number split across buffer boundary (chunk size 2)
    @Test
    public void testParsePositiveLongSplit_returnsCorrectValue() throws Exception {
        // Use a number longer than typical buffer to force reload (but chunked ensures it)
        JsonParser p = createChunkedParser("9876543210", 2);
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(9876543210L, p.getLongValue()); // fits in long
        p.close();
    }

    // Number with fraction and exponent split across buffer (chunk size 1)
    @Test
    public void testParseFloatSplit_returnsCorrectValue() throws Exception {
        JsonParser p = createChunkedParser("0.5e2", 1);
        assertSame(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(50.0, p.getDoubleValue(), 1e-9);
        p.close();
    }

    // Escape sequences in string
    @Test
    public void testParseStringWithEscape_returnsCorrectText() throws Exception {
        JsonParser p = createParser("\"line1\\nline2\"");
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("line1\nline2", p.getText());
        p.close();
    }

    // Empty string
    @Test
    public void testParseEmptyString_returnsEmptyString() throws Exception {
        JsonParser p = createParser("\"\"");
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("", p.getText());
        p.close();
    }

    // getValueAsString on current string token
    @Test
    public void testGetValueAsStringOnString_returnsValue() throws Exception {
        JsonParser p = createParser("\"abc\"");
        p.nextToken();
        assertEquals("abc", p.getValueAsString());
        p.close();
    }

    // getValueAsString on field name
    @Test
    public void testGetValueAsStringOnFieldName_returnsName() throws Exception {
        JsonParser p = createParser("{\"key\":1}");
        assertSame(JsonToken.START_OBJECT, p.nextToken());
        assertSame(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("key", p.getValueAsString()); // should return current name
        p.close();
    }

    // getValueAsString with default on non-string token
    @Test
    public void testGetValueAsStringWithDefault_returnsDefault() throws Exception {
        JsonParser p = createParser("true");
        p.nextToken();
        assertEquals("defaultValue", p.getValueAsString("defaultValue"));
        p.close();
    }

    // ========== New tests to cover missing parts ==========

    // Test compile with specific factory features
    @Test
    public void testParseWithFactoryFeature_returnsCorrectToken() throws Exception {
        JsonFactory factory = new JsonFactory();
        factory.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
        JsonParser p = factory.createParser(new StringReader("NaN"));
        assertSame(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertTrue(Double.isNaN(p.getDoubleValue()));
        p.close();
    }

    // Test parser close and reopen (simulate compile reset)
    @Test
    public void testParseAfterClose_throwsException() throws Exception {
        JsonParser p = createParser("true");
        p.close();
        try {
            p.nextToken();
            fail("Expected IOException after close");
        } catch (IOException e) {
            // expected
        }
    }

    // Test empty input (simulate compile of empty file)
    @Test
    public void testParseEmptyInput_returnsNull() throws Exception {
        JsonParser p = createParser("");
        assertNull(p.nextToken());
        p.close();
    }

    // Test whitespace only input (simulate compile with spaces)
    @Test
    public void testParseWhitespaceOnly_returnsNull() throws Exception {
        JsonParser p = createParser("   \n\t  ");
        assertNull(p.nextToken());
        p.close();
    }
}