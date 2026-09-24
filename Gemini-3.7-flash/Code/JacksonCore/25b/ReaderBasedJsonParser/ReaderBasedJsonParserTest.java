package com.fasterxml.jackson.core.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.sym.CharsToNameCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.io.SerializedString;

public class ReaderBasedJsonParserTest {

    private ReaderBasedJsonParser _createParser(String doc) {
        return _createParser(doc, 0);
    }

    private ReaderBasedJsonParser _createParser(String doc, int features) {
        BufferRecycler br = new BufferRecycler();
        IOContext ctxt = new IOContext(br, doc, false);
        CharsToNameCanonicalizer sym = CharsToNameCanonicalizer.createRoot();
        return new ReaderBasedJsonParser(ctxt, features, new StringReader(doc), null, sym);
    }

    // Tests parsing standard JSON structure with objects and arrays
    @Test
    public void testNextToken_validObjectAndArray_parsesTokensCorrectly() throws IOException {
        String json = "{\"key\": [123, true, false, null, \"text\"]}";
        ReaderBasedJsonParser parser = _createParser(json);

        assertNull(parser.getCurrentToken());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getCurrentName());
        assertEquals("key", parser.getText());

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        assertEquals(123L, parser.getLongValue());
        assertEquals("123", parser.getText());

        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("text", parser.getText());
        assertEquals("text", parser.getValueAsString());

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests unquoted field name containing 256+ / non-Latin1 chars (Defects4J Bug 25b regression)
    @Test
    public void testHandleOddName_unquotedNameWithNonAscii_parsesSuccessfully() throws IOException {
        int features = JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask();
        // '\u0100' triggers index >= 256 which exposes array boundary bugs in odd name decoding
        String json = "{ a\u0100b : 123 }";
        ReaderBasedJsonParser parser = _createParser(json, features);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a\u0100b", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    // Tests nextFieldName() with SerializableString matching and mismatching
    @Test
    public void testNextFieldName_matchingAndMismatching_returnsExpected() throws IOException {
        String json = "{\"name\":\"John\", \"age\":30}";
        ReaderBasedJsonParser parser = _createParser(json);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertTrue(parser.nextFieldName(new SerializedString("name")));
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("John", parser.getText());

        assertFalse(parser.nextFieldName(new SerializedString("unknown")));
        assertEquals("age", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(30, parser.getIntValue());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    // Tests nextFieldName() returning String
    @Test
    public void testNextFieldName_stringReturn_returnsFieldNames() throws IOException {
        String json = "{\"first\":\"value1\", \"second\":2}";
        ReaderBasedJsonParser parser = _createParser(json);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals("first", parser.nextFieldName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value1", parser.getText());

        assertEquals("second", parser.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextFieldName());
        parser.close();
    }

    // Tests parsing numbers: positive, negative, floating point with exponent
    @Test
    public void testParseNumber_positiveNegativeAndFloat_parsesCorrectly() throws IOException {
        String json = "[-0, -42, 3.1415, -1.25e2, 2.5E-1]";
        ReaderBasedJsonParser parser = _createParser(json);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(0, parser.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(-42, parser.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.1415, parser.getDoubleValue(), 0.0001);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(-125.0, parser.getDoubleValue(), 0.0001);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(0.25, parser.getDoubleValue(), 0.0001);

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    // Tests single quotes feature for field names and string values
    @Test
    public void testSingleQuotes_enabled_parsesSuccessfully() throws IOException {
        int features = JsonParser.Feature.ALLOW_SINGLE_QUOTES.getMask();
        String json = "{ 'key' : 'value \\'with\\' quotes' }";
        ReaderBasedJsonParser parser = _createParser(json, features);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getText());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value 'with' quotes", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    // Tests C-style and YAML-style comments
    @Test
    public void testComments_cAndYamlStyles_ignoredCorrectly() throws IOException {
        int features = JsonParser.Feature.ALLOW_COMMENTS.getMask() 
                | JsonParser.Feature.ALLOW_YAML_COMMENTS.getMask();
        String json = "/* comment */ {\n" +
                      "  // line comment\n" +
                      "  # yaml comment\n" +
                      "  \"key\": /* block */ 100\n" +
                      "}";
        ReaderBasedJsonParser parser = _createParser(json, features);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(100, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    // Tests decoding Base64 binary value
    @Test
    public void testGetBinaryValue_validBase64_returnsDecodedBytes() throws IOException {
        String base64Str = "SGVsbG8gV29ybGQ="; // "Hello World"
        String json = "[\"" + base64Str + "\"]";
        ReaderBasedJsonParser parser = _createParser(json);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());

        byte[] binary = parser.getBinaryValue(Base64Variants.MIME);
        assertEquals("Hello World", new String(binary, "UTF-8"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bytesRead = parser.readBinaryValue(Base64Variants.MIME, out);
        assertEquals(11, bytesRead);
        assertEquals("Hello World", out.toString("UTF-8"));

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    // Tests specialized nextXxxValue methods (nextIntValue, nextLongValue, nextBooleanValue, nextTextValue)
    @Test
    public void testNextSpecializedValues_variousTypes_returnsExpectedValues() throws IOException {
        String json = "{\"intVal\": 42, \"longVal\": 9999999999, \"boolVal\": true, \"strVal\": \"hello\"}";
        ReaderBasedJsonParser parser = _createParser(json);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(42, parser.nextIntValue(-1));

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(9999999999L, parser.nextLongValue(-1L));

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(Boolean.TRUE, parser.nextBooleanValue());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("hello", parser.nextTextValue());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    // Tests getText(Writer) and getTextCharacters()
    @Test
    public void testGetText_writerAndCharacters_outputsCorrectText() throws IOException {
        String json = "{\"title\":\"Jackson Parser\"}";
        ReaderBasedJsonParser parser = _createParser(json);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());

        StringWriter sw = new StringWriter();
        int len = parser.getText(sw);
        assertEquals("title", sw.toString());
        assertEquals(5, len);

        char[] chars = parser.getTextCharacters();
        assertNotNull(chars);
        assertTrue(parser.getTextLength() >= 5);

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        sw = new StringWriter();
        len = parser.getText(sw);
        assertEquals("Jackson Parser", sw.toString());
        assertEquals(14, len);

        parser.finishToken();
        assertEquals("Jackson Parser", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    // Tests trailing comma feature
    @Test
    public void testTrailingComma_enabled_allowsTrailingCommaInArrayAndObject() throws IOException {
        int features = JsonParser.Feature.ALLOW_TRAILING_COMMA.getMask();
        String json = "{\"a\": [1, ], }";
        ReaderBasedJsonParser parser = _createParser(json, features);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    // Tests missing values feature in arrays
    @Test
    public void testAllowMissingValues_enabled_emitsNullToken() throws IOException {
        int features = JsonParser.Feature.ALLOW_MISSING_VALUES.getMask();
        String json = "[1,,3]";
        ReaderBasedJsonParser parser = _createParser(json, features);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(3, parser.getIntValue());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    // Tests non-numeric numbers (NaN, Infinity, -Infinity)
    @Test
    public void testNonNumericNumbers_enabled_parsesCorrectly() throws IOException {
        int features = JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask();
        String json = "[NaN, Infinity, -Infinity, +INF, -INF]";
        ReaderBasedJsonParser parser = _createParser(json, features);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(Double.isNaN(parser.getDoubleValue()));

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.NEGATIVE_INFINITY, parser.getDoubleValue(), 0.0);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.NEGATIVE_INFINITY, parser.getDoubleValue(), 0.0);

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    // Tests location tracking: getTokenLocation and getCurrentLocation
    @Test
    public void testGetLocation_tracksOffsetsAndRows() throws IOException {
        String json = "{\n  \"field\": 123\n}";
        ReaderBasedJsonParser parser = _createParser(json);

        JsonLocation locStart = parser.getCurrentLocation();
        assertEquals(1, locStart.getLineNr());

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());

        JsonLocation nameLoc = parser.getTokenLocation();
        assertEquals(2, nameLoc.getLineNr());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    // Tests releaseBuffered on custom buffer
    @Test
    public void testReleaseBuffered_withUnreadChars_writesToWriter() throws IOException {
        String json = "123 extra";
        BufferRecycler br = new BufferRecycler();
        IOContext ctxt = new IOContext(br, json, false);
        CharsToNameCanonicalizer sym = CharsToNameCanonicalizer.createRoot();
        char[] buf = json.toCharArray();
        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(ctxt, 0, null, null, sym, buf, 0, buf.length, false);

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());

        StringWriter sw = new StringWriter();
        int released = parser.releaseBuffered(sw);
        assertEquals(" extra", sw.toString());
        assertEquals(6, released);
        parser.close();
    }

    // Tests exception on unexpected end-of-input inside unclosed string
    @Test(expected = JsonParseException.class)
    public void testNextToken_unclosedString_throwsException() throws IOException {
        String json = "\"unclosed string";
        ReaderBasedJsonParser parser = _createParser(json);
        parser.nextToken();
        parser.getText();
        parser.close();
    }

    // Tests exception on unexpected character for boolean value
    @Test(expected = JsonParseException.class)
    public void testNextToken_corruptBoolean_throwsException() throws IOException {
        String json = "[truX]";
        ReaderBasedJsonParser parser = _createParser(json);
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        parser.nextToken();
        parser.close();
    }

    // Tests escape sequences in strings including unicode and standard escapes
    @Test
    public void testEscapeSequences_inString_parsesEscapedCharacters() throws IOException {
        String json = "[\"\\\"\\\\\\/\\b\\f\\n\\r\\t\\u0041\"]";
        ReaderBasedJsonParser parser = _createParser(json);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("\"\\/\b\f\n\r\tA", parser.getText());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    // Tests leading zeroes in numbers when allowed
    @Test
    public void testAllowNumericLeadingZeros_enabled_parsesNumberCorrectly() throws IOException {
        int features = JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask();
        String json = "[0123, -007]";
        ReaderBasedJsonParser parser = _createParser(json, features);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(-7, parser.getIntValue());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    // Tests unquoted control characters when allowed
    @Test
    public void testAllowUnquotedControlChars_enabled_parsesStringWithControlChar() throws IOException {
        int features = JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS.getMask();
        String json = "[\"line1\nline2\ttab\"]";
        ReaderBasedJsonParser parser = _createParser(json, features);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("line1\nline2\ttab", parser.getText());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    // Tests getCodec, setCodec and canReadObjectId/canReadTypeId methods
    @Test
    public void testParserCodecAndCapabilities() throws IOException {
        ReaderBasedJsonParser parser = _createParser("{}");
        assertNull(parser.getCodec());
        parser.setCodec(null);
        assertFalse(parser.canReadObjectId());
        assertFalse(parser.canReadTypeId());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    // Tests parsing long string exceeding internal buffer size to exercise buffer expansion
    @Test
    public void testParseLongString_exceedsBuffer_parsesCorrectly() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4000; i++) {
            sb.append("abcdefghij");
        }
        String longText = sb.toString();
        String json = "[\"" + longText + "\"]";
        ReaderBasedJsonParser parser = _createParser(json);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(longText, parser.getText());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }
}