package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.SerializedString;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

/**
 * JUnit 4 test class for UTF8StreamJsonParser
 * targeting Defects4J bug 12b.
 */
public class UTF8StreamJsonParserTest {

    // Helper: create UTF8StreamJsonParser from a JSON string
    private UTF8StreamJsonParser createParser(String json) throws Exception {
        JsonFactory factory = new JsonFactory();
        InputStream in = new ByteArrayInputStream(json.getBytes("UTF-8"));
        JsonParser jp = factory.createParser(in);
        assertTrue("Parser must be UTF8StreamJsonParser", jp instanceof UTF8StreamJsonParser);
        return (UTF8StreamJsonParser) jp;
    }

    @Test
    public void testSimpleString_returnsCorrectString() throws Exception {
        UTF8StreamJsonParser p = createParser("\"hello\"");
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("hello", p.getText());
    }

    @Test
    public void testNumberInteger_parsesCorrectly() throws Exception {
        UTF8StreamJsonParser p = createParser("42");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getIntValue());
    }

    @Test
    public void testNumberNegative_parsesCorrectly() throws Exception {
        UTF8StreamJsonParser p = createParser("-17");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(-17, p.getIntValue());
    }

    @Test(expected = JsonParseException.class)
    public void testLeadingZero_notAllowed_throwsException() throws Exception {
        // By default ALLOW_NUMERIC_LEADING_ZEROS is disabled
        UTF8StreamJsonParser p = createParser("0123");
        p.nextToken();
    }

    @Test
    public void testNumberFloat_parsesCorrectly() throws Exception {
        UTF8StreamJsonParser p = createParser("3.14");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(3.14, p.getDoubleValue(), 0.0001);
    }

    @Test
    public void testTrueFalseNull_returnsCorrectTokens() throws Exception {
        UTF8StreamJsonParser p = createParser("true false null");
        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, p.nextToken());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
    }

    @Test
    public void testEmptyArray_returnsStartEnd() throws Exception {
        UTF8StreamJsonParser p = createParser("[]");
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
    }

    @Test
    public void testEmptyObject_returnsStartEnd() throws Exception {
        UTF8StreamJsonParser p = createParser("{}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
    }

    @Test
    public void testNestedObject_parsesCorrectly() throws Exception {
        UTF8StreamJsonParser p = createParser("{\"a\":{\"b\":1}}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("b", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
    }

    @Test
    public void testFieldNameWithEscape_returnsCorrectName() throws Exception {
        UTF8StreamJsonParser p = createParser("{\"foo\\nbar\":1}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        // name contains newline character
        assertEquals("foo\nbar", p.getCurrentName());
    }

    @Test
    public void testUnquotedFieldName_allowed_parses() throws Exception {
        JsonFactory factory = new JsonFactory();
        factory.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        InputStream in = new ByteArrayInputStream("{foo:1}".getBytes("UTF-8"));
        UTF8StreamJsonParser p = (UTF8StreamJsonParser) factory.createParser(in);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("foo", p.getCurrentName());
    }

    @Test
    public void testGetValueAsInt_onNumber_returnsInt() throws Exception {
        UTF8StreamJsonParser p = createParser("123");
        p.nextToken();
        assertEquals(123, p.getValueAsInt());
    }

    @Test
    public void testGetValueAsInt_defaultValue_onNonNumber() throws Exception {
        UTF8StreamJsonParser p = createParser("\"abc\"");
        p.nextToken();
        assertEquals(42, p.getValueAsInt(42));
    }

    @Test
    public void testNextFieldName_matches_returnsTrue() throws Exception {
        UTF8StreamJsonParser p = createParser("{\"key\":10}");
        p.nextToken(); // START_OBJECT
        assertTrue(p.nextFieldName(new SerializedString("key")));
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(10, p.getIntValue());
    }

    @Test
    public void testNextFieldName_notMatch_returnsFalse() throws Exception {
        UTF8StreamJsonParser p = createParser("{\"other\":10}");
        p.nextToken();
        assertFalse(p.nextFieldName(new SerializedString("key")));
    }

    @Test(expected = JsonParseException.class)
    public void testExceptionMissingColon_throws() throws Exception {
        UTF8StreamJsonParser p = createParser("{\"a\" 1}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        p.nextToken(); // should fail: missing colon
    }

    @Test(expected = JsonParseException.class)
    public void testExceptionUnexpectedChar_throws() throws Exception {
        UTF8StreamJsonParser p = createParser("{x}");
        p.nextToken(); // START_OBJECT, then x is unexpected
    }

    @Test
    public void testNextTextValue_returnsString() throws Exception {
        UTF8StreamJsonParser p = createParser("{\"field\":\"value\"}");
        p.nextToken(); // START_OBJECT
        assertEquals("field", p.nextFieldName());
        assertEquals("value", p.nextTextValue());
    }

    @Test
    public void testNextIntValue_returnsInt() throws Exception {
        UTF8StreamJsonParser p = createParser("{\"num\":99}");
        p.nextToken();
        p.nextFieldName(); // skip "num"
        assertEquals(99, p.nextIntValue(0));
    }

    // ===== New test cases to improve coverage =====

    @Test
    public void testUnicodeEscapeInString() throws Exception {
        // JSON string containing Unicode escape \u0041 which is 'A'
        UTF8StreamJsonParser p = createParser("\"\\u0041\"");
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("A", p.getText());
    }

    @Test
    public void testNumberWithExponent() throws Exception {
        UTF8StreamJsonParser p = createParser("1.5e10");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(1.5e10, p.getDoubleValue(), 0.0);
    }

    @Test
    public void testFieldNameGetText() throws Exception {
        // Verify that getText() on a FIELD_NAME token returns the field name
        UTF8StreamJsonParser p = createParser("{\"myField\":123}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("myField", p.getText());
    }

    @Test
    public void testSkipChildren() throws Exception {
        // skipChildren on nested array
        UTF8StreamJsonParser p = createParser("[ [1,2], 3 ]");
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        p.skipChildren(); // should skip inner array and stop at its END_ARRAY
        // Now current token should be END_ARRAY of the inner array
        assertEquals(JsonToken.END_ARRAY, p.getCurrentToken());
        // Next token should be VALUE_NUMBER_INT 3
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(3, p.getIntValue());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
    }

    @Test(expected = IOException.class)
    public void testCloseThenNextTokenThrows() throws Exception {
        UTF8StreamJsonParser p = createParser("42");
        p.close();
        p.nextToken(); // should throw IOException because parser is closed
    }
}