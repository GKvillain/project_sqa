package com.fasterxml.jackson.core.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.SerializedString;

public class ReaderBasedJsonParserTest {

    private final JsonFactory JSON_F = new JsonFactory();

    private JsonParser createParser(String doc) throws IOException {
        return JSON_F.createParser(new StringReader(doc));
    }

    // Tests standard JSON structure traversal with diverse token types
    @Test
    public void testNextToken_standardStructure_parsesCorrectly() throws IOException {
        String json = "{\"str\":\"hello\", \"num\":123, \"flag\":true, \"empty\":null, \"arr\":[false]}";
        JsonParser p = createParser(json);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("str", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("hello", p.getText());
        assertEquals("hello", p.getValueAsString());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("num", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(123, p.getIntValue());
        assertEquals("123", p.getText());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("flag", p.getCurrentName());
        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        assertTrue(p.getBooleanValue());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("empty", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("arr", p.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, p.nextToken());
        assertFalse(p.getBooleanValue());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    // Tests token locations within nested objects and field names
    @Test
    public void testGetTokenLocation_inObjectStructure_returnsExpectedLocation() throws IOException {
        String json = "{\n  \"field\": 42\n}";
        JsonParser p = createParser(json);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(1, p.getTokenLocation().getLineNr());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("field", p.getCurrentName());
        JsonLocation nameLoc = p.getTokenLocation();
        assertEquals(2, nameLoc.getLineNr());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getIntValue());
        JsonLocation valLoc = p.getTokenLocation();
        assertEquals(2, valLoc.getLineNr());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertEquals(3, p.getTokenLocation().getLineNr());
        p.close();
    }

    // Tests fast-path methods: nextFieldName, nextIntValue, nextTextValue, nextBooleanValue
    @Test
    public void testNextValueOptimizationMethods_validInput_matchesValues() throws IOException {
        String json = "{\"name\":\"Alice\",\"age\":30,\"valid\":true,\"score\":10000000000}";
        JsonParser p = createParser(json);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        assertTrue(p.nextFieldName(new SerializedString("name")));
        assertEquals("Alice", p.nextTextValue());

        assertEquals("age", p.nextFieldName());
        assertEquals(30, p.nextIntValue(0));

        assertEquals("valid", p.nextFieldName());
        assertEquals(Boolean.TRUE, p.nextBooleanValue());

        assertEquals("score", p.nextFieldName());
        assertEquals(10000000000L, p.nextLongValue(0L));

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    // Tests negative, decimal, and exponential floating point numbers
    @Test
    public void testParseNumbers_floatingPointAndNegative_parsesValues() throws IOException {
        String json = "[-0.5, -123, 1.25e2, -3.5E-1, 0]";
        JsonParser p = createParser(json);

        assertEquals(JsonToken.START_ARRAY, p.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(-0.5, p.getDoubleValue(), 0.0001);

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(-123, p.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(125.0, p.getDoubleValue(), 0.0001);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(-0.35, p.getDoubleValue(), 0.0001);

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(0, p.getIntValue());

        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    // Tests string escape sequences including unicode escapes
    @Test
    public void testFinishString_withEscapeSequences_unescapesCorrectly() throws IOException {
        String json = "\"\\\"\\\\\\/\\b\\f\\n\\r\\t\\u0041\"";
        JsonParser p = createParser(json);

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("\"\\/\b\f\n\r\tA", p.getText());
        assertNotNull(p.getTextCharacters());
        assertTrue(p.getTextLength() > 0);
        assertEquals(0, p.getTextOffset());
        p.close();
    }

    // Tests Base64 binary decoding and binary streaming
    @Test
    public void testGetBinaryValue_validBase64_returnsDecodedBytes() throws IOException {
        // "Hello World!" in base64 is "SGVsbG8gV29ybGQh"
        String json = "[\"SGVsbG8gV29ybGQh\", \"SGVsbG8=\", \"\"]";
        JsonParser p = createParser(json);

        assertEquals(JsonToken.START_ARRAY, p.nextToken());

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        byte[] b1 = p.getBinaryValue(Base64Variants.MIME);
        assertEquals("Hello World!", new String(b1, "UTF-8"));

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bytesRead = p.readBinaryValue(Base64Variants.MIME, out);
        assertEquals(5, bytesRead);
        assertEquals("Hello", new String(out.toByteArray(), "UTF-8"));

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        byte[] b3 = p.getBinaryValue(Base64Variants.MIME);
        assertEquals(0, b3.length);

        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    // Tests single quotes feature for field names and string values
    @Test
    public void testHandleOdd_singleQuotesEnabled_parsesSuccessfully() throws IOException {
        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        f.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);

        String json = "{'a':'b', unquoted: 'test'}";
        JsonParser p = f.createParser(new StringReader(json));

        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("b", p.getText());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("unquoted", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("test", p.getText());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    // Tests non-standard numbers like NaN and Infinity
    @Test
    public void testHandleOddValue_nonNumericNumbers_parsesConstants() throws IOException {
        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);

        String json = "[NaN, Infinity, -Infinity, -INF, +INF]";
        JsonParser p = f.createParser(new StringReader(json));

        assertEquals(JsonToken.START_ARRAY, p.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertTrue(Double.isNaN(p.getDoubleValue()));

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, p.getDoubleValue(), 0.0);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.NEGATIVE_INFINITY, p.getDoubleValue(), 0.0);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.NEGATIVE_INFINITY, p.getDoubleValue(), 0.0);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, p.getDoubleValue(), 0.0);

        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    // Tests comment skipping with C-style, line, and YAML comments
    @Test
    public void testSkipComments_enabledFeatures_skipsProperly() throws IOException {
        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.ALLOW_COMMENTS);
        f.enable(JsonParser.Feature.ALLOW_YAML_COMMENTS);

        String json = "/* header comment */\n"
                + "{\n"
                + "  // line comment\n"
                + "  \"key\": /* inline */ \"value\" # yaml comment\n"
                + "}";
        JsonParser p = f.createParser(new StringReader(json));

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("key", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("value", p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    // Tests releasing buffered content into a Writer
    @Test
    public void testReleaseBuffered_withRemainingInput_writesContent() throws IOException {
        String json = "{\"a\":1}   remaining content";
        JsonParser p = createParser(json);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());

        StringWriter sw = new StringWriter();
        int count = p.releaseBuffered(sw);
        assertTrue(count >= 0);
        p.close();
    }

    // Tests mismatched closing bracket in array context throws exception
    @Test(expected = JsonParseException.class)
    public void testNextToken_mismatchedEndArray_throwsException() throws IOException {
        String json = "[1, 2}";
        JsonParser p = createParser(json);
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        p.nextToken();
    }

    // Tests mismatched closing curly in object context throws exception
    @Test(expected = JsonParseException.class)
    public void testNextToken_mismatchedEndObject_throwsException() throws IOException {
        String json = "{\"key\": 1]";
        JsonParser p = createParser(json);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        p.nextToken();
    }

    // Tests unexpected character when value is expected throws exception
    @Test(expected = JsonParseException.class)
    public void testNextToken_unexpectedCharForValue_throwsException() throws IOException {
        String json = "{\"key\": ]}";
        JsonParser p = createParser(json);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        p.nextToken();
    }

    // Tests invalid unquoted token throws exception
    @Test(expected = JsonParseException.class)
    public void testNextToken_invalidUnquotedToken_throwsException() throws IOException {
        String json = "[invalidToken]";
        JsonParser p = createParser(json);
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        p.nextToken();
    }

    // Tests invalid floating point format without digits after decimal point
    @Test(expected = JsonParseException.class)
    public void testParseFloat_missingDecimalDigits_throwsException() throws IOException {
        String json = "[1.]";
        JsonParser p = createParser(json);
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        p.nextToken();
    }

    // Tests invalid floating point exponent without digits
    @Test(expected = JsonParseException.class)
    public void testParseFloat_missingExponentDigits_throwsException() throws IOException {
        String json = "[1.0e]";
        JsonParser p = createParser(json);
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        p.nextToken();
    }

    // Tests invalid number leading zero behavior
    @Test(expected = JsonParseException.class)
    public void testParseNumber_invalidLeadingZero_throwsException() throws IOException {
        String json = "[0123]";
        JsonParser p = createParser(json);
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        p.nextToken();
    }

    // Tests binary access on non-string token throws exception
    @Test(expected = JsonParseException.class)
    public void testGetBinaryValue_nonStringToken_throwsException() throws IOException {
        String json = "[123]";
        JsonParser p = createParser(json);
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        p.getBinaryValue(Base64Variants.MIME);
    }

    // --- Added coverage test cases ---

    // Tests getText(Writer) writing token text to Writer
    @Test
    public void testGetText_withWriter_writesOutput() throws IOException {
        String json = "{\"key\": \"long value string\"}";
        JsonParser p = createParser(json);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());

        StringWriter sw1 = new StringWriter();
        int len1 = p.getText(sw1);
        assertEquals(3, len1);
        assertEquals("key", sw1.toString());

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        StringWriter sw2 = new StringWriter();
        int len2 = p.getText(sw2);
        assertEquals(17, len2);
        assertEquals("long value string", sw2.toString());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    // Tests large string crossing parser buffer boundary
    @Test
    public void testParseLongString_exceedsBuffer_parsesCorrectly() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5000; i++) {
            sb.append((char) ('a' + (i % 26)));
        }
        String bigStr = sb.toString();
        String json = "[\"" + bigStr + "\"]";
        JsonParser p = createParser(json);

        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(bigStr, p.getText());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    // Tests nextFieldName mismatch and default fallback in nextIntValue/nextBooleanValue/nextLongValue/nextTextValue
    @Test
    public void testNextOptimizations_mismatchesAndDefaults() throws IOException {
        String json = "{\"k1\":\"v1\",\"k2\":123,\"k3\":true,\"k4\":null}";
        JsonParser p = createParser(json);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        // Mismatched expected SerializedString
        assertFalse(p.nextFieldName(new SerializedString("not_k1")));
        assertEquals(JsonToken.FIELD_NAME, p.currentToken());
        assertEquals("k1", p.getCurrentName());

        // Calling nextIntValue on string token should return default
        assertEquals(999, p.nextIntValue(999));
        assertEquals(JsonToken.VALUE_STRING, p.currentToken());
        assertEquals("v1", p.getText());

        assertEquals("k2", p.nextFieldName());
        // Calling nextBooleanValue on int token should return null
        assertNull(p.nextBooleanValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.currentToken());
        assertEquals(123, p.getIntValue());

        assertEquals("k3", p.nextFieldName());
        // Calling nextTextValue on boolean token should return null
        assertNull(p.nextTextValue());
        assertEquals(JsonToken.VALUE_TRUE, p.currentToken());

        assertEquals("k4", p.nextFieldName());
        // Calling nextLongValue on null token should return default
        assertEquals(888L, p.nextLongValue(888L));
        assertEquals(JsonToken.VALUE_NULL, p.currentToken());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextFieldName());
        assertNull(p.nextToken());
        p.close();
    }

    // Tests BigInteger and BigDecimal number parsing
    @Test
    public void testParseNumbers_bigIntegerAndBigDecimal() throws IOException {
        String bigIntStr = "123456789012345678901234567890";
        String bigDecStr = "12345678901234567890.12345678901234567890";
        String json = "[" + bigIntStr + ", " + bigDecStr + "]";
        JsonParser p = createParser(json);

        assertEquals(JsonToken.START_ARRAY, p.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(new BigInteger(bigIntStr), p.getBigIntegerValue());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(new BigDecimal(bigDecStr), p.getDecimalValue());

        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    // Tests empty array and empty object parsing
    @Test
    public void testParseEmptyContainers() throws IOException {
        String json = "[{}, []]";
        JsonParser p = createParser(json);

        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    // Tests unclosed string throws exception
    @Test(expected = JsonParseException.class)
    public void testUnclosedString_throwsException() throws IOException {
        String json = "\"unclosed string";
        JsonParser p = createParser(json);
        p.nextToken();
    }

    // Tests invalid unicode escape sequence throws exception
    @Test(expected = JsonParseException.class)
    public void testInvalidUnicodeEscape_throwsException() throws IOException {
        String json = "\"\\u00AG\"";
        JsonParser p = createParser(json);
        p.nextToken();
    }

    // Tests comments when feature disabled throws exception
    @Test(expected = JsonParseException.class)
    public void testCommentsDisabled_throwsException() throws IOException {
        String json = "/* comment */ 123";
        JsonParser p = createParser(json);
        p.nextToken();
    }

    // Tests single quote with escape sequence
    @Test
    public void testSingleQuoteWithEscape() throws IOException {
        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        String json = "['it\\'s a test\\n']";
        JsonParser p = f.createParser(new StringReader(json));

        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("it's a test\n", p.getText());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    // Tests root scalar values
    @Test
    public void testRootScalars() throws IOException {
        JsonParser p1 = createParser("\"root string\"");
        assertEquals(JsonToken.VALUE_STRING, p1.nextToken());
        assertEquals("root string", p1.getText());
        assertNull(p1.nextToken());
        p1.close();

        JsonParser p2 = createParser("true");
        assertEquals(JsonToken.VALUE_TRUE, p2.nextToken());
        assertNull(p2.nextToken());
        p2.close();

        JsonParser p3 = createParser("null");
        assertEquals(JsonToken.VALUE_NULL, p3.nextToken());
        assertNull(p3.nextToken());
        p3.close();
    }
}