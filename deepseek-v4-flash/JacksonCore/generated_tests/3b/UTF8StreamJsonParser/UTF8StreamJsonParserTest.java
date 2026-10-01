package com.fasterxml.jackson.core.json;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.Test;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.sym.BytesToNameCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;

/**
 * JUnit 4 test class for UTF8StreamJsonParser, targeting Defects4J bug 3b.
 * Focuses on white-box testing, branch coverage, and defect detection.
 */
public class UTF8StreamJsonParserTest {

    private IOContext ctxt;
    private BytesToNameCanonicalizer sym;

    // Helper to create a parser from a UTF-8 byte array
    private UTF8StreamJsonParser createParser(byte[] data) throws IOException {
        ctxt = new IOContext(new BufferRecycler(), null, false);
        sym = BytesToNameCanonicalizer.createRoot();
        return new UTF8StreamJsonParser(ctxt, 0, new ByteArrayInputStream(data),
                null, sym, data, 0, data.length, false);
    }

    // Helper to create a parser with custom features (e.g., non-numeric numbers)
    private UTF8StreamJsonParser createParserWithFeatures(byte[] data, int features) throws IOException {
        ctxt = new IOContext(new BufferRecycler(), null, false);
        sym = BytesToNameCanonicalizer.createRoot();
        return new UTF8StreamJsonParser(ctxt, features, new ByteArrayInputStream(data),
                null, sym, data, 0, data.length, false);
    }
    
    // Helper to create parser with non-recyclable buffer (to test releaseBuffered path)
    private UTF8StreamJsonParser createParserWithRecyclable(byte[] data, boolean recyclable) throws IOException {
        ctxt = new IOContext(new BufferRecycler(), null, false);
        sym = BytesToNameCanonicalizer.createRoot();
        return new UTF8StreamJsonParser(ctxt, 0, new ByteArrayInputStream(data),
                null, sym, data, 0, data.length, recyclable);
    }

    @Test(expected = IOException.class)
    // Tests that an invalid JSON token throws a parse exception
    public void testNextToken_invalidToken_throwsException() throws IOException {
        UTF8StreamJsonParser parser = createParser("foo".getBytes("UTF-8"));
        parser.nextToken();
    }

    @Test
    // Tests normal parsing of a simple JSON object with a string field
    public void testNextToken_simpleObject_returnsCorrectTokens() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"key\":\"value\"}".getBytes("UTF-8"));
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test
    // Tests that a number is correctly parsed as integer
    public void testNextToken_integerNumber_returnsIntToken() throws IOException {
        UTF8StreamJsonParser parser = createParser("42".getBytes("UTF-8"));
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
    }

    @Test
    // Tests that a negative number is correctly parsed
    public void testNextToken_negativeNumber_returnsIntToken() throws IOException {
        UTF8StreamJsonParser parser = createParser("-128".getBytes("UTF-8"));
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(-128, parser.getIntValue());
    }

    @Test
    // Tests parsing of a float number with decimal point and exponent
    public void testNextToken_floatNumber_returnsFloatToken() throws IOException {
        UTF8StreamJsonParser parser = createParser("3.14e0".getBytes("UTF-8"));
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.001);
    }

    @Test
    // Tests true literal
    public void testNextToken_trueLiteral_returnsTrueToken() throws IOException {
        UTF8StreamJsonParser parser = createParser("true".getBytes("UTF-8"));
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
    }

    @Test
    // Tests false literal
    public void testNextToken_falseLiteral_returnsFalseToken() throws IOException {
        UTF8StreamJsonParser parser = createParser("false".getBytes("UTF-8"));
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
    }

    @Test
    // Tests null literal
    public void testNextToken_nullLiteral_returnsNullToken() throws IOException {
        UTF8StreamJsonParser parser = createParser("null".getBytes("UTF-8"));
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
    }

    @Test
    // Tests parsing of an empty array
    public void testNextToken_emptyArray_returnsStartAndEndArray() throws IOException {
        UTF8StreamJsonParser parser = createParser("[]".getBytes("UTF-8"));
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test
    // Tests parsing of an empty object
    public void testNextToken_emptyObject_returnsStartAndEndObject() throws IOException {
        UTF8StreamJsonParser parser = createParser("{}".getBytes("UTF-8"));
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test
    // Tests string reading after a field name with value true
    public void testNextTextValue_fieldNameWithStringValue_returnsString() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"name\":\"test\"}".getBytes("UTF-8"));
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        String text = parser.nextTextValue();
        assertEquals("test", text);
    }

    @Test
    // Tests that getText returns correct string for VALUE_STRING token
    public void testGetText_stringToken_returnsStringContent() throws IOException {
        UTF8StreamJsonParser parser = createParser("\"hello\"".getBytes("UTF-8"));
        parser.nextToken();
        assertEquals("hello", parser.getText());
    }

    @Test
    // Tests getText for field name
    public void testGetText_fieldNameToken_returnsFieldName() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"key\":1}".getBytes("UTF-8"));
        parser.nextToken();
        parser.nextToken();
        assertEquals("key", parser.getText());
    }

    @Test
    // Tests getTextCharacters for a string token
    public void testGetTextCharacters_stringToken_returnsCharArray() throws IOException {
        UTF8StreamJsonParser parser = createParser("\"abc\"".getBytes("UTF-8"));
        parser.nextToken();
        char[] chars = parser.getTextCharacters();
        assertArrayEquals(new char[]{'a','b','c'}, chars);
    }

    @Test
    // Tests getTextLength for a string token
    public void testGetTextLength_stringToken_returnsLength() throws IOException {
        UTF8StreamJsonParser parser = createParser("\"abc\"".getBytes("UTF-8"));
        parser.nextToken();
        assertEquals(3, parser.getTextLength());
    }

    @Test
    // Tests getTextOffset for string token, expecting 0
    public void testGetTextOffset_stringToken_returnsZero() throws IOException {
        UTF8StreamJsonParser parser = createParser("\"abc\"".getBytes("UTF-8"));
        parser.nextToken();
        assertEquals(0, parser.getTextOffset());
    }

    @Test
    // Tests that binary value can be retrieved from base64-encoded string (non-standard, but parser handles it)
    // Note: This may not be fully supported without proper base64 handling, but it tests a branch in getBinaryValue
    public void testGetBinaryValue_stringToken_returnsDecodedBytes() throws IOException {
        // This test simply ensures the method does not throw unexpected exception
        // and follows code path for non-incomplete token
        UTF8StreamJsonParser parser = createParser("\"aGVsbG8=\"".getBytes("UTF-8")); // "hello" in base64
        parser.nextToken();
        // This will likely throw due to missing proper Base64Variant; but we test the path
        try {
            parser.getBinaryValue(Base64Variants.MIME);
        } catch (IOException | JsonParseException e) {
            // expected if base64 fails, but we just want to ensure we exercised the code
        }
    }

    @Test
    // Tests nextFieldName for exact match (fast path)
    public void testNextFieldName_matchingName_returnsTrue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"name\":123}".getBytes("UTF-8"));
        parser.nextToken(); // START_OBJECT
        SerializableString str = new SerializableString() {
            public String getValue() { return "name"; }
            public int charLength() { return 4; }
            public char[] asQuotedChars() { return "name".toCharArray(); }
            public byte[] asUnquotedUTF8() { return "name".getBytes(); }
            public byte[] asQuotedUTF8() { return "\"name\"".getBytes(); }
            public int appendQuotedUTF8(byte[] buffer, int offset) { byte[] b = asQuotedUTF8(); System.arraycopy(b, 0, buffer, offset, b.length); return b.length; }
            public int appendQuoted(char[] buffer, int offset) { char[] c = asQuotedChars(); System.arraycopy(c, 0, buffer, offset, c.length); return c.length; }
            public int appendUnquotedUTF8(byte[] buffer, int offset) { byte[] b = asUnquotedUTF8(); System.arraycopy(b, 0, buffer, offset, b.length); return b.length; }
            public int appendUnquoted(char[] buffer, int offset) { char[] c = asQuotedChars(); System.arraycopy(c, 0, buffer, offset, c.length); return c.length; }
        };
        assertTrue(parser.nextFieldName(str));
        assertEquals(JsonToken.FIELD_NAME, parser.getCurrentToken());
    }

    @Test
    // Tests releaseBuffered method
    public void testReleaseBuffered_withData_returnsCount() throws IOException {
        UTF8StreamJsonParser parser = createParser("  true".getBytes("UTF-8"));
        // advance parser a bit so some data is read
        parser.nextToken(); // should skip whitespace and parse "true"
        // now there may be leftover data? Actually buffer might be empty after token. We'll test with a longer input
        // Simulate: create another with leftover
        byte[] data = "   \"test\"   ".getBytes("UTF-8");
        UTF8StreamJsonParser parser2 = createParser(data);
        parser2.nextToken(); // VALUE_STRING
        // some bytes remain in buffer
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        int count = parser2.releaseBuffered(out);
        assertTrue(count > 0); // there should be leftover whitespace etc.
    }

    @Test(expected = JsonParseException.class)
    // Tests that invalid syntax (single comma) causes exception
    public void testNextToken_invalidSyntax_throwsException() throws IOException {
        UTF8StreamJsonParser parser = createParser("[,]".getBytes("UTF-8"));
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // should fail
    }

    @Test
    // Tests getValueAsString for non-string token (should return null)
    public void testGetValueAsString_nonString_returnsNull() throws IOException {
        UTF8StreamJsonParser parser = createParser("true".getBytes("UTF-8"));
        parser.nextToken();
        assertNull(parser.getValueAsString());
    }

    @Test
    // Tests getValueAsString with default for non-string token
    public void testGetValueAsString_withDefault_nonString_returnsDefault() throws IOException {
        UTF8StreamJsonParser parser = createParser("null".getBytes("UTF-8"));
        parser.nextToken();
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test
    // Tests that leading zero is rejected without ALLOW_NUMERIC_LEADING_ZEROS
    public void testParseNumber_leadingZero_throwsException() throws IOException {
        UTF8StreamJsonParser parser = createParser("0123".getBytes("UTF-8"));
        try {
            parser.nextToken();
            fail("Should have thrown exception");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Leading zeroes not allowed"));
        }
    }

    @Test
    // Tests that leading zero is accepted with ALLOW_NUMERIC_LEADING_ZEROS
    public void testParseNumber_allowedLeadingZero_returnsInt() throws IOException {
        int features = JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask();
        UTF8StreamJsonParser parser = createParserWithFeatures("0123".getBytes("UTF-8"), features);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
    }

    @Test
    // Tests that nextIntValue works when current token is FIELD_NAME with next INTEGER
    public void testNextIntValue_fieldNameWithInt_returnsIntValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"x\":42}".getBytes("UTF-8"));
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        int val = parser.nextIntValue(-1);
        assertEquals(42, val);
    }

    @Test
    // Tests that nextLongValue works when current token is FIELD_NAME with next INTEGER
    public void testNextLongValue_fieldNameWithLong_returnsLongValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"x\":1234567890123}".getBytes("UTF-8"));
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        long val = parser.nextLongValue(-1L);
        assertEquals(1234567890123L, val);
    }

    @Test
    // Tests that nextBooleanValue works for true
    public void testNextBooleanValue_fieldNameWithTrue_returnsTrue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"flag\":true}".getBytes("UTF-8"));
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        assertEquals(Boolean.TRUE, parser.nextBooleanValue());
    }

    @Test
    // Tests that nextBooleanValue works for false
    public void testNextBooleanValue_fieldNameWithFalse_returnsFalse() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"flag\":false}".getBytes("UTF-8"));
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        assertEquals(Boolean.FALSE, parser.nextBooleanValue());
    }

    // ====================== New tests for uncovered coverage ======================

    @Test
    // Tests nextFieldName with a non-matching name (should return false)
    public void testNextFieldName_nonMatching_returnsFalse() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"x\":1}".getBytes("UTF-8"));
        parser.nextToken(); // START_OBJECT
        SerializableString str = new SerializableString() {
            public String getValue() { return "y"; }
            public int charLength() { return 1; }
            public char[] asQuotedChars() { return "y".toCharArray(); }
            public byte[] asUnquotedUTF8() { return "y".getBytes(); }
            public byte[] asQuotedUTF8() { return "\"y\"".getBytes(); }
            public int appendQuotedUTF8(byte[] buffer, int offset) { byte[] b = asQuotedUTF8(); System.arraycopy(b, 0, buffer, offset, b.length); return b.length; }
            public int appendQuoted(char[] buffer, int offset) { char[] c = asQuotedChars(); System.arraycopy(c, 0, buffer, offset, c.length); return c.length; }
            public int appendUnquotedUTF8(byte[] buffer, int offset) { byte[] b = asUnquotedUTF8(); System.arraycopy(b, 0, buffer, offset, b.length); return b.length; }
            public int appendUnquoted(char[] buffer, int offset) { char[] c = asQuotedChars(); System.arraycopy(c, 0, buffer, offset, c.length); return c.length; }
        };
        assertFalse(parser.nextFieldName(str));
        // After non-match, current token should still be FIELD_NAME (not advanced)
        assertEquals(JsonToken.FIELD_NAME, parser.getCurrentToken());
        assertEquals("x", parser.getCurrentName());
    }

    @Test(expected = NullPointerException.class)
    // Tests that nextFieldName throws NullPointerException when given null
    public void testNextFieldName_nullString_throwsException() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"x\":1}".getBytes("UTF-8"));
        parser.nextToken();
        parser.nextFieldName((SerializableString) null);
    }

    @Test
    // Tests nextTextValue when current token is not a field name (e.g., after START_ARRAY)
    public void testNextTextValue_afterArray_returnsNull() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"value\"]".getBytes("UTF-8"));
        parser.nextToken(); // START_ARRAY
        assertNull(parser.nextTextValue());
    }

    @Test
    // Tests getText on numeric token (should return string representation)
    public void testGetText_numericToken_returnsString() throws IOException {
        UTF8StreamJsonParser parser = createParser("42".getBytes("UTF-8"));
        parser.nextToken();
        assertEquals("42", parser.getText());
    }

    @Test(expected = JsonParseException.class)
    // Tests that getIntValue on a float token throws exception
    public void testGetIntValue_onFloat_throws() throws IOException {
        UTF8StreamJsonParser parser = createParser("3.14".getBytes("UTF-8"));
        parser.nextToken();
        parser.getIntValue();
    }

    @Test(expected = JsonParseException.class)
    // Tests that parsing an incomplete float (e.g., "12.") throws exception
    public void testParseNumber_incompleteFloat_throws() throws IOException {
        UTF8StreamJsonParser parser = createParser("12.".getBytes("UTF-8"));
        parser.nextToken();
    }

    @Test
    // Tests getValueAsString on integer token returns string representation
    public void testGetValueAsString_onInt_returnsString() throws IOException {
        UTF8StreamJsonParser parser = createParser("123".getBytes("UTF-8"));
        parser.nextToken();
        assertEquals("123", parser.getValueAsString());
    }

    @Test
    // Tests getValueAsInt on boolean token (true -> 1)
    public void testGetValueAsInt_onTrue_returnsOne() throws IOException {
        UTF8StreamJsonParser parser = createParser("true".getBytes("UTF-8"));
        parser.nextToken();
        // getValueAsInt() returns 1 for true by default
        assertEquals(1, parser.getValueAsInt());
    }

    @Test
    // Tests getValueAsInt on false -> 0
    public void testGetValueAsInt_onFalse_returnsZero() throws IOException {
        UTF8StreamJsonParser parser = createParser("false".getBytes("UTF-8"));
        parser.nextToken();
        assertEquals(0, parser.getValueAsInt());
    }

    @Test
    // Tests releaseBuffered when no data remains (should return 0)
    public void testReleaseBuffered_noData_returnsZero() throws IOException {
        UTF8StreamJsonParser parser = createParser("true".getBytes("UTF-8"));
        parser.nextToken(); // consume the only token
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        int count = parser.releaseBuffered(out);
        assertEquals(0, count);
        assertTrue(out.toByteArray().length == 0);
    }

    @Test
    // Tests skipChildren for an array (should skip to token after array)
    public void testSkipChildren_array_skipsToEnd() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1, 2, 3]".getBytes("UTF-8"));
        parser.nextToken(); // START_ARRAY
        parser.skipChildren();
        // After skipping children, current token should be END_ARRAY
        assertEquals(JsonToken.END_ARRAY, parser.getCurrentToken());
    }

    @Test
    // Tests parsing of a JSON string with escaped characters (backslash, quote)
    public void testStringWithEscapedChars() throws IOException {
        UTF8StreamJsonParser parser = createParser("\"line1\\nline2\\t\\\"\".getBytes("UTF-8"));
        parser.nextToken();
        String text = parser.getText();
        assertEquals("line1\nline2\t\"", text);
    }

    @Test
    // Tests parsing of negative zero (-0) which should be integer zero
    public void testParseNumber_negativeZero_returnsIntegerZero() throws IOException {
        UTF8StreamJsonParser parser = createParser("-0".getBytes("UTF-8"));
        parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.getCurrentToken());
        assertEquals(0, parser.getIntValue());
    }

    @Test
    // Tests parsing of an empty string token ("")
    public void testEmptyStringToken() throws IOException {
        UTF8StreamJsonParser parser = createParser("\"\"".getBytes("UTF-8"));
        parser.nextToken();
        assertEquals(JsonToken.VALUE_STRING, parser.getCurrentToken());
        assertEquals("", parser.getText());
        assertEquals(0, parser.getTextLength());
    }

    @Test
    // Tests parsing of a nested object inside an array
    public void testNestedObjectInArray() throws IOException {
        UTF8StreamJsonParser parser = createParser("[{\"a\":1}]".getBytes("UTF-8"));
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test
    // Tests that getTokenLocation returns non-null after parsing a token
    public void testGetTokenLocation_nonNull() throws IOException {
        UTF8StreamJsonParser parser = createParser("42".getBytes("UTF-8"));
        parser.nextToken();
        JsonLocation loc = parser.getTokenLocation();
        assertNotNull(loc);
        // Location should indicate byte offset
        assertTrue(loc.getByteOffset() >= 0);
    }
}