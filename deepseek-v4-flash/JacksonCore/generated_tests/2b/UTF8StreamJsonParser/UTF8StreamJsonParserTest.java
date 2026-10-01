package com.fasterxml.jackson.core.json;

import org.junit.Test;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.sym.BytesToNameCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import static org.junit.Assert.*;

/**
 * JUnit 4 Test Class for UTF8StreamJsonParser.
 * Tests parsing of JSON from UTF-8 byte input streams.
 */
public class UTF8StreamJsonParserTest {

    private IOContext createIOContext() {
        BufferRecycler br = new BufferRecycler();
        return new IOContext(br, null, false);
    }

    private BytesToNameCanonicalizer createSymbolTable() {
        return BytesToNameCanonicalizer.createRoot();
    }

    private UTF8StreamJsonParser createParser(String json) throws Exception {
        return createParser(json.getBytes("UTF-8"));
    }

    private UTF8StreamJsonParser createParser(byte[] data) throws Exception {
        InputStream in = new ByteArrayInputStream(data);
        IOContext ctxt = createIOContext();
        BytesToNameCanonicalizer sym = createSymbolTable();
        // Provide an initial buffer to avoid immediate loading
        byte[] initialBuf = new byte[0];
        return new UTF8StreamJsonParser(ctxt, JsonParser.Feature.collectDefaults(),
                in, null, sym, initialBuf, 0, 0, false);
    }

    // Tests nextToken() with simple string value
    @Test
    public void testNextToken_simpleString_returnsStringToken() throws Exception {
        UTF8StreamJsonParser parser = createParser("\"hello\"");
        assertNull(parser.getCurrentToken());
        JsonToken token = parser.nextToken();
        assertEquals(JsonToken.VALUE_STRING, token);
        assertEquals("hello", parser.getText());
    }

    // Tests nextToken() with integer number
    @Test
    public void testNextToken_integerNumber_returnsIntToken() throws Exception {
        UTF8StreamJsonParser parser = createParser("42");
        assertNull(parser.getCurrentToken());
        JsonToken token = parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, token);
        assertEquals(42, parser.getIntValue());
    }

    // Tests nextToken() with floating point number
    @Test
    public void testNextToken_floatNumber_returnsFloatToken() throws Exception {
        UTF8StreamJsonParser parser = createParser("3.14");
        assertNull(parser.getCurrentToken());
        JsonToken token = parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, token);
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
    }

    // Tests nextToken() with true literal
    @Test
    public void testNextToken_booleanTrue_returnsTrueToken() throws Exception {
        UTF8StreamJsonParser parser = createParser("true");
        assertNull(parser.getCurrentToken());
        JsonToken token = parser.nextToken();
        assertEquals(JsonToken.VALUE_TRUE, token);
        assertTrue(parser.getBooleanValue());
    }

    // Tests nextToken() with false literal
    @Test
    public void testNextToken_booleanFalse_returnsFalseToken() throws Exception {
        UTF8StreamJsonParser parser = createParser("false");
        assertNull(parser.getCurrentToken());
        JsonToken token = parser.nextToken();
        assertEquals(JsonToken.VALUE_FALSE, token);
        assertFalse(parser.getBooleanValue());
    }

    // Tests nextToken() with null literal
    @Test
    public void testNextToken_nullLiteral_returnsNullToken() throws Exception {
        UTF8StreamJsonParser parser = createParser("null");
        assertNull(parser.getCurrentToken());
        JsonToken token = parser.nextToken();
        assertEquals(JsonToken.VALUE_NULL, token);
        assertNull(parser.getEmbeddedObject());
    }

    // Tests nextToken() with empty object
    @Test
    public void testNextToken_emptyObject_returnsStartAndEndObject() throws Exception {
        UTF8StreamJsonParser parser = createParser("{}");
        assertNull(parser.getCurrentToken());
        JsonToken token = parser.nextToken();
        assertEquals(JsonToken.START_OBJECT, token);
        token = parser.nextToken();
        assertEquals(JsonToken.END_OBJECT, token);
    }

    // Tests nextToken() with empty array
    @Test
    public void testNextToken_emptyArray_returnsStartAndEndArray() throws Exception {
        UTF8StreamJsonParser parser = createParser("[]");
        assertNull(parser.getCurrentToken());
        JsonToken token = parser.nextToken();
        assertEquals(JsonToken.START_ARRAY, token);
        token = parser.nextToken();
        assertEquals(JsonToken.END_ARRAY, token);
    }

    // Tests nextToken() with field name and string value
    @Test
    public void testNextToken_fieldNameAndStringValue_returnsFieldNameThenString() throws Exception {
        UTF8StreamJsonParser parser = createParser("{\"name\":\"value\"}");
        assertNull(parser.getCurrentToken());
        JsonToken token = parser.nextToken();
        assertEquals(JsonToken.START_OBJECT, token);
        token = parser.nextToken();
        assertEquals(JsonToken.FIELD_NAME, token);
        assertEquals("name", parser.getCurrentName());
        token = parser.nextToken();
        assertEquals(JsonToken.VALUE_STRING, token);
        assertEquals("value", parser.getText());
        token = parser.nextToken();
        assertEquals(JsonToken.END_OBJECT, token);
    }

    // Tests nextFieldName() with matching name (fast path)
    @Test
    public void testNextFieldName_matchingName_returnsTrue() throws Exception {
        UTF8StreamJsonParser parser = createParser("{\"abc\":123}");
        JsonToken token = parser.nextToken();
        assertEquals(JsonToken.START_OBJECT, token);
        boolean found = parser.nextFieldName(new SerializableString() {
            public String getValue() { return "abc"; }
            public int charLength() { return 3; }
            public char[] asQuotedChars() { return "\"abc\"".toCharArray(); }
            public byte[] asUnquotedUTF8() { return "abc".getBytes(); }
            public byte[] asQuotedUTF8() { return "\"abc\"".getBytes(); }
        });
        assertTrue(found);
        assertEquals("abc", parser.getCurrentName());
    }

    // Tests nextTextValue() after field name with string value
    @Test
    public void testNextTextValue_afterFieldName_returnsStringValue() throws Exception {
        UTF8StreamJsonParser parser = createParser("{\"x\":\"test\"}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        String value = parser.nextTextValue();
        assertEquals("test", value);
    }

    // Tests getText() on VALUE_STRING with token incomplete (lazy parsing)
    @Test
    public void testGetText_stringTokenIncomplete_returnsCorrectString() throws Exception {
        UTF8StreamJsonParser parser = createParser("\"hello\"");
        parser.nextToken();
        String text = parser.getText();
        assertEquals("hello", text);
    }

    // Tests getIntValue() on integer value
    @Test
    public void testGetIntValue_numberToken_returnsCorrectInt() throws Exception {
        UTF8StreamJsonParser parser = createParser("100");
        parser.nextToken();
        assertEquals(100, parser.getIntValue());
    }

    // Tests getValueAsString() on string token
    @Test
    public void testGetValueAsString_stringToken_returnsStringValue() throws Exception {
        UTF8StreamJsonParser parser = createParser("\"foo\"");
        parser.nextToken();
        assertEquals("foo", parser.getValueAsString());
    }

    // Tests getValueAsString() with default on non-string token
    @Test
    public void testGetValueAsString_nonStringToken_returnsDefault() throws Exception {
        UTF8StreamJsonParser parser = createParser("123");
        parser.nextToken();
        assertEquals("default", parser.getValueAsString("default"));
    }

    // Tests getBinaryValue() base64 decoding
    @Test
    public void testGetBinaryValue_stringToken_returnsDecodedBytes() throws Exception {
        UTF8StreamJsonParser parser = createParser("\"aGVsbG8=\"");
        parser.nextToken();
        byte[] decoded = parser.getBinaryValue(Base64Variants.MIME);
        assertEquals("hello", new String(decoded, "UTF-8"));
    }

    // Tests getTextCharacters() for field name
    @Test
    public void testGetTextCharacters_fieldName_returnsNameChars() throws Exception {
        UTF8StreamJsonParser parser = createParser("{\"key\":1}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        char[] chars = parser.getTextCharacters();
        assertEquals("key", new String(chars, 0, parser.getTextLength()));
    }

    // Tests close() to ensure no exception
    @Test
    public void testClose_cleanParser_noException() throws Exception {
        UTF8StreamJsonParser parser = createParser("{}");
        parser.close();
        // Should not throw
    }

    // Tests nextToken() with negative number
    @Test
    public void testNextToken_negativeNumber_returnsIntToken() throws Exception {
        UTF8StreamJsonParser parser = createParser("-42");
        JsonToken token = parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, token);
        assertEquals(-42, parser.getIntValue());
    }

    // Tests parsing multiple fields and values
    @Test
    public void testNextToken_multipleFields_parsesCorrectly() throws Exception {
        UTF8StreamJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("b", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    }

    // Tests getLongValue() on a large integer value
    @Test
    public void testGetLongValue_largeInteger_returnsCorrectLong() throws Exception {
        UTF8StreamJsonParser parser = createParser("1234567890123");
        parser.nextToken();
        assertEquals(1234567890123L, parser.getLongValue());
    }

    // Tests getDecimalValue() on a floating point number
    @Test
    public void testGetDecimalValue_floatNumber_returnsCorrectDecimal() throws Exception {
        UTF8StreamJsonParser parser = createParser("3.14159");
        parser.nextToken();
        assertEquals(new java.math.BigDecimal("3.14159"), parser.getDecimalValue());
    }

    // Tests getValueAsInt() on integer value
    @Test
    public void testGetValueAsInt_numberToken_returnsIntValue() throws Exception {
        UTF8StreamJsonParser parser = createParser("42");
        parser.nextToken();
        assertEquals(42, parser.getValueAsInt());
    }

    // Tests getValueAsLong() on integer value
    @Test
    public void testGetValueAsLong_numberToken_returnsLongValue() throws Exception {
        UTF8StreamJsonParser parser = createParser("1000000000000");
        parser.nextToken();
        assertEquals(1000000000000L, parser.getValueAsLong());
    }

    // Tests getValueAsDouble() on float value
    @Test
    public void testGetValueAsDouble_floatToken_returnsDoubleValue() throws Exception {
        UTF8StreamJsonParser parser = createParser("2.71828");
        parser.nextToken();
        assertEquals(2.71828, parser.getValueAsDouble(), 0.00001);
    }

    // Tests getValueAsBoolean() on true token
    @Test
    public void testGetValueAsBoolean_trueToken_returnsTrue() throws Exception {
        UTF8StreamJsonParser parser = createParser("true");
        parser.nextToken();
        assertTrue(parser.getValueAsBoolean());
    }

    // Tests getValueAsBoolean() on false token
    @Test
    public void testGetValueAsBoolean_falseToken_returnsFalse() throws Exception {
        UTF8StreamJsonParser parser = createParser("false");
        parser.nextToken();
        assertFalse(parser.getValueAsBoolean());
    }

    // Tests skipChildren() on a nested object
    @Test
    public void testSkipChildren_nestedObject_skipsToEndOfObject() throws Exception {
        UTF8StreamJsonParser parser = createParser("{\"a\":{\"b\":1}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.skipChildren();
        assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());
        parser.nextToken(); // END_OBJECT of outer
        assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());
    }

    // Tests getParsingContext() returns non-null context
    @Test
    public void testGetParsingContext_returnsNonNullContext() throws Exception {
        UTF8StreamJsonParser parser = createParser("{}");
        assertNotNull(parser.getParsingContext());
    }

    // Tests getTokenLocation() returns non-null location after token
    @Test
    public void testGetTokenLocation_afterToken_returnsNonNullLocation() throws Exception {
        UTF8StreamJsonParser parser = createParser("42");
        parser.nextToken();
        assertNotNull(parser.getTokenLocation());
    }

    // Tests getCurrentLocation() returns non-null location
    @Test
    public void testGetCurrentLocation_returnsNonNullLocation() throws Exception {
        UTF8StreamJsonParser parser = createParser("42");
        assertNotNull(parser.getCurrentLocation());
    }
}