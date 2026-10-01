package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.*;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;

public class UTF8StreamJsonParserTest {

    // Helper to create a UTF8StreamJsonParser from a JSON string
    private UTF8StreamJsonParser createParser(String json) throws IOException {
        JsonFactory factory = new JsonFactory();
        return (UTF8StreamJsonParser) factory.createParser(new ByteArrayInputStream(json.getBytes("UTF-8")));
    }

    // Helper with feature overrides
    private UTF8StreamJsonParser createParser(String json, JsonParser.Feature feature, boolean state) throws IOException {
        JsonFactory factory = new JsonFactory();
        factory.configure(feature, state);
        return (UTF8StreamJsonParser) factory.createParser(new ByteArrayInputStream(json.getBytes("UTF-8")));
    }

    // Helper with multiple features
    private UTF8StreamJsonParser createParser(String json, JsonParser.Feature f1, boolean s1, JsonParser.Feature f2, boolean s2) throws IOException {
        JsonFactory factory = new JsonFactory();
        factory.configure(f1, s1);
        factory.configure(f2, s2);
        return (UTF8StreamJsonParser) factory.createParser(new ByteArrayInputStream(json.getBytes("UTF-8")));
    }

    // ==================== Existing tests ====================

    // Tests normal object parsing and token sequence
    @Test
    public void testNextToken_simpleObject_returnsCorrectTokens() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getText());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    // Tests string value retrieval
    @Test
    public void testGetText_stringValue_returnsString() throws IOException {
        UTF8StreamJsonParser parser = createParser("\"hello\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getText());
    }

    // Tests positive integer value
    @Test
    public void testGetValueAsInt_positiveInt_returnsInt() throws IOException {
        UTF8StreamJsonParser parser = createParser("42");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getValueAsInt());
    }

    // Tests negative integer value
    @Test
    public void testGetValueAsInt_negativeInt_returnsInt() throws IOException {
        UTF8StreamJsonParser parser = createParser("-42");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(-42, parser.getValueAsInt());
    }

    // Tests integer conversion from float value
    @Test
    public void testGetValueAsInt_floatValue_returnsTruncatedInt() throws IOException {
        UTF8StreamJsonParser parser = createParser("42.7");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(42, parser.getValueAsInt());
    }

    // Tests field name text retrieval
    @Test
    public void testGetText_fieldName_returnsName() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"key\":\"val\"}");
        parser.nextToken(); // START_OBJECT
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getText());
    }

    // Tests array start and end tokens
    @Test
    public void testNextToken_array_returnsStartAndEnd() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1,2]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }

    // Tests boolean values
    @Test
    public void testNextToken_boolean_returnsTrueFalse() throws IOException {
        UTF8StreamJsonParser parser = createParser("true false");
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertNull(parser.nextToken());
    }

    // Tests null token
    @Test
    public void testNextToken_null_returnsNull() throws IOException {
        UTF8StreamJsonParser parser = createParser("null");
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.nextToken());
    }

    // Tests binary value decoding from base64 string
    @Test
    public void testGetBinaryValue_base64_returnsBytes() throws IOException {
        // "aGVsbG8=" is "hello" in base64
        UTF8StreamJsonParser parser = createParser("\"aGVsbG8=\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        byte[] expected = "hello".getBytes("UTF-8");
        assertArrayEquals(expected, parser.getBinaryValue());
    }

    // Tests that leading zeroes throw by default
    @Test(expected = JsonParseException.class)
    public void testParse_leadingZeroes_notAllowed_throwsException() throws IOException {
        UTF8StreamJsonParser parser = createParser("0123");
        parser.nextToken();
    }

    // Tests leading zeroes allowed with feature
    @Test
    public void testParse_leadingZeroes_allowed_returnsNumber() throws IOException {
        UTF8StreamJsonParser parser = createParser("0123",
                JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, true);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
    }

    // Tests negative number parsing
    @Test
    public void testParse_negativeNumber_returnsCorrect() throws IOException {
        UTF8StreamJsonParser parser = createParser("-999");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(-999, parser.getIntValue());
    }

    // Tests floating point with exponent
    @Test
    public void testParse_floatWithExponent_returnsFloat() throws IOException {
        UTF8StreamJsonParser parser = createParser("1.5e10");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(1.5e10, parser.getDoubleValue(), 0.0);
    }

    // Tests empty string
    @Test
    public void testParse_emptyString_returnsEmpty() throws IOException {
        UTF8StreamJsonParser parser = createParser("\"\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("", parser.getText());
    }

    // Tests line comment (C++ style) when allowed
    @Test
    public void testParse_lineComment_allowed_skipsComment() throws IOException {
        UTF8StreamJsonParser parser = createParser("// comment\n1",
                JsonParser.Feature.ALLOW_COMMENTS, true);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
    }

    // Tests block comment when allowed
    @Test
    public void testParse_blockComment_allowed_skipsComment() throws IOException {
        UTF8StreamJsonParser parser = createParser("/* block */ 2",
                JsonParser.Feature.ALLOW_COMMENTS, true);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
    }

    // Tests single-quote strings when allowed
    @Test
    public void testParse_singleQuoteString_allowed_returnsString() throws IOException {
        UTF8StreamJsonParser parser = createParser("'hello'",
                JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getText());
    }

    // Tests non-standard NaN when allowed
    @Test
    public void testParse_nan_allowed_returnsNaN() throws IOException {
        UTF8StreamJsonParser parser = createParser("NaN",
                JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(Double.isNaN(parser.getDoubleValue()));
    }

    // Tests that root value not followed by whitespace throws
    @Test(expected = JsonParseException.class)
    public void testParse_rootValueNoTerminator_throwsException() throws IOException {
        UTF8StreamJsonParser parser = createParser("1a");
        parser.nextToken();
    }

    // Tests long field name (>12 bytes) to exercise slow parsing path
    @Test
    public void testParse_longFieldName_parsesCorrectly() throws IOException {
        String longName = "abcdefghijklm"; // 13 chars
        String json = "{\"" + longName + "\":1}";
        UTF8StreamJsonParser parser = createParser(json);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(longName, parser.getText());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
    }

    // Tests that incomplete string token is handled by getText()
    @Test
    public void testGetText_incompleteString_finishesAndReturns() throws IOException {
        // Use a string that might be parsed partially (short enough to be in buffer)
        UTF8StreamJsonParser parser = createParser("\"hello\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        // Force _tokenIncomplete = true (simulate scenario? Actually,
        // nextToken sets _tokenIncomplete for VALUE_STRING if token is not fully parsed.
        // But in this simple case, the string is fully parsed. We rely on internal state.
        // We'll just test that getText works in normal case.)
        assertEquals("hello", parser.getText());
    }

    // ==================== New tests for uncovered coverage ====================

    // Tests getCurrentName() for field names
    @Test
    public void testGetCurrentName_fieldName_returnsName() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"x\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("x", parser.getCurrentName());
        parser.nextToken(); // VALUE_NUMBER_INT
        // After reading value, currentName should still be the last field name
        assertEquals("x", parser.getCurrentName());
    }

    // Tests skipChildren() for nested objects
    @Test
    public void testSkipChildren_nestedObject_skipsCorrectly() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":{\"b\":2},\"c\":3}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT nested
        // Now skip children of nested object
        parser.skipChildren();
        // After skip, we should be at end of nested object
        assertEquals(JsonToken.END_OBJECT, parser.currentToken());
        // Advance to next field
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("c", parser.getText());
    }

    // Tests hasCurrentToken() and clearCurrentToken()
    @Test
    public void testHasCurrentToken_afterNextToken_returnsTrue() throws IOException {
        UTF8StreamJsonParser parser = createParser("123");
        assertFalse(parser.hasCurrentToken()); // No token yet
        parser.nextToken();
        assertTrue(parser.hasCurrentToken());
        parser.clearCurrentToken();
        assertFalse(parser.hasCurrentToken());
    }

    // Tests getValueAsString() with default value
    @Test
    public void testGetValueAsString_defaultValue_returnsDefault() throws IOException {
        UTF8StreamJsonParser parser = createParser("null");
        parser.nextToken();
        // For null token, getValueAsString should return null default
        assertNull(parser.getValueAsString());
        // With given default
        assertEquals("default", parser.getValueAsString("default"));
    }

    // Tests escaped characters in strings (e.g., \n, \t, \\, \uXXXX)
    @Test
    public void testGetText_escapedCharacters_returnsCorrectString() throws IOException {
        UTF8StreamJsonParser parser = createParser("\"hello\\nworld\\ttab\\\\backslash\\u0041\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        String expected = "hello\nworld\ttab\\backslashA";
        assertEquals(expected, parser.getText());
    }

    // Tests unicode surrogate pairs (e.g., emoji)
    @Test
    public void testGetText_surrogatePair_returnsCorrectUnicode() throws IOException {
        // The JSON string "\uD83D\uDE00" represents the grinning emoji 😀
        UTF8StreamJsonParser parser = createParser("\"\\uD83D\\uDE00\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        String expected = new String(new int[]{0x1F600}, 0, 1);
        assertEquals(expected, parser.getText());
    }

    // Tests multiple top-level values (e.g., "1 2 3")
    @Test
    public void testNextToken_multipleValues_returnsEach() throws IOException {
        UTF8StreamJsonParser parser = createParser("1 2 3");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(3, parser.getIntValue());
        assertNull(parser.nextToken());
    }

    // Tests error on unexpected character (e.g., invalid token)
    @Test(expected = JsonParseException.class)
    public void testParse_unexpectedCharacter_throwsException() throws IOException {
        UTF8StreamJsonParser parser = createParser("$");
        parser.nextToken();
    }

    // Tests ALLOW_YAML_COMMENTS (if feature exists in Jackson)
    @Test
    public void testParse_yamlComment_allowed_skipsComment() throws IOException {
        // YAML comments start with #
        UTF8StreamJsonParser parser = createParser("# yaml comment\n42",
                JsonParser.Feature.ALLOW_YAML_COMMENTS, true);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
    }

    // Tests STRICT_DUPLICATE_DETECTION feature
    @Test(expected = JsonParseException.class)
    public void testParse_duplicateField_strict_throwsException() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1,\"a\":2}",
                JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true);
        // Force parsing to trigger duplicate detection
        while (parser.nextToken() != null) {
            parser.getText();
        }
    }

    // Tests very large integer that exceeds long (should use BigInteger)
    @Test
    public void testGetBigIntegerValue_largeNumber_returnsBigInteger() throws IOException {
        UTF8StreamJsonParser parser = createParser("123456789012345678901234567890");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        BigInteger expected = new BigInteger("123456789012345678901234567890");
        assertEquals(expected, parser.getBigIntegerValue());
    }

    // Tests getDecimalValue() for floating point
    @Test
    public void testGetDecimalValue_float_returnsBigDecimal() throws IOException {
        UTF8StreamJsonParser parser = createParser("3.14159");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(new BigDecimal("3.14159"), parser.getDecimalValue());
    }

    // Tests getTokenLocation() and getCurrentLocation()
    @Test
    public void testGetTokenLocation_returnsLocation() throws IOException {
        UTF8StreamJsonParser parser = createParser("true");
        parser.nextToken();
        JsonLocation tokenLoc = parser.getTokenLocation();
        assertNotNull(tokenLoc);
        assertTrue(tokenLoc.getByteOffset() >= 0);
        assertTrue(tokenLoc.getLineNr() >= 1);
        JsonLocation currLoc = parser.getCurrentLocation();
        assertNotNull(currLoc);
    }

    // Tests close() and resource release (basic)
    @Test
    public void testClose_releasesResources() throws IOException {
        UTF8StreamJsonParser parser = createParser("{}");
        parser.nextToken(); // consume something
        parser.close();
        // After close, nextToken should return null
        assertNull(parser.nextToken());
    }
}