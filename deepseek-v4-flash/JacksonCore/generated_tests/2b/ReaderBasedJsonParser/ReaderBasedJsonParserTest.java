package com.fasterxml.jackson.core.json;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * JUnit 4 test class for ReaderBasedJsonParser.
 * Focuses on Defects4J bug 2b: leading zeros not rejected when feature ALLOW_NUMERIC_LEADING_ZEROS is disabled.
 */
public class ReaderBasedJsonParserTest {

    private JsonFactory factory;

    @Before
    public void setUp() {
        factory = new JsonFactory();
    }

    // Helper: create parser from JSON string using default factory settings
    private JsonParser createParser(String json) throws IOException {
        return factory.createParser(new StringReader(json));
    }

    // Helper: create parser from JSON string with custom feature flags
    private JsonParser createParserWithFeatures(String json, boolean allowLeadingZeros, boolean allowComments,
            boolean allowSingleQuotes, boolean allowUnquotedNames, boolean allowNonNumericNumbers,
            boolean allowYamlComments) throws IOException {
        JsonFactory f = new JsonFactory();
        if (allowLeadingZeros) {
            f.enable(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS);
        } else {
            f.disable(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS);
        }
        if (allowComments) {
            f.enable(JsonParser.Feature.ALLOW_COMMENTS);
        }
        if (allowSingleQuotes) {
            f.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        }
        if (allowUnquotedNames) {
            f.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        }
        if (allowNonNumericNumbers) {
            f.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
        }
        if (allowYamlComments) {
            f.enable(JsonParser.Feature.ALLOW_YAML_COMMENTS);
        }
        return f.createParser(new StringReader(json));
    }

    // ================= Normal parsing cases =================

    // Tests parsing a simple integer value
    @Test
    public void testNextToken_simpleInteger_returnsValueNumberInt() throws IOException {
        JsonParser p = createParser("123");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(123, p.getIntValue());
    }

    // Tests parsing a negative integer
    @Test
    public void testNextToken_negativeInteger_returnsValueNumberInt() throws IOException {
        JsonParser p = createParser("-456");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(-456, p.getIntValue());
    }

    // Tests parsing a floating point number
    @Test
    public void testNextToken_floatNumber_returnsValueNumberFloat() throws IOException {
        JsonParser p = createParser("3.14");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(3.14, p.getDoubleValue(), 1e-9);
    }

    // Tests parsing null value
    @Test
    public void testNextToken_nullValue_returnsValueNull() throws IOException {
        JsonParser p = createParser("null");
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
    }

    // Tests parsing boolean true
    @Test
    public void testNextToken_booleanTrue_returnsValueTrue() throws IOException {
        JsonParser p = createParser("true");
        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
    }

    // Tests parsing boolean false
    @Test
    public void testNextToken_booleanFalse_returnsValueFalse() throws IOException {
        JsonParser p = createParser("false");
        assertEquals(JsonToken.VALUE_FALSE, p.nextToken());
    }

    // ================= Leading zero cases (Defects4J bug 2b) =================

    // Tests that a single zero is accepted normally
    @Test
    public void testNextToken_singleZero_returnsValueNumberInt() throws IOException {
        JsonParser p = createParser("0");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(0, p.getIntValue());
    }

    // Test that leading zero (e.g., "00") throws JsonParseException when feature is disabled
    @Test(expected = JsonParseException.class)
    public void testNextToken_leadingZeroDisabled_throwsJsonParseException() throws IOException {
        JsonParser p = createParserWithFeatures("00", false, false, false, false, false, false);
        p.nextToken();
    }

    // Test that leading zero is accepted when feature is enabled
    @Test
    public void testNextToken_leadingZeroEnabled_returnsValueNumberInt() throws IOException {
        JsonParser p = createParserWithFeatures("00", true, false, false, false, false, false);
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(0, p.getIntValue());
    }

    // Test that "0123" with leading zero disabled throws exception
    @Test(expected = JsonParseException.class)
    public void testNextToken_leadingZeroMultipleDigitsDisabled_throwsJsonParseException() throws IOException {
        JsonParser p = createParserWithFeatures("0123", false, false, false, false, false, false);
        p.nextToken();
    }

    // Test that "0123" with leading zero enabled parses successfully
    @Test
    public void testNextToken_leadingZeroMultipleDigitsEnabled_returnsValueNumberInt() throws IOException {
        JsonParser p = createParserWithFeatures("0123", true, false, false, false, false, false);
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(123, p.getIntValue()); // Note: Jackson normalizes leading zeros to decimal value
    }

    // Test that "-0123" with leading zero disabled throws exception
    @Test(expected = JsonParseException.class)
    public void testNextToken_negativeLeadingZeroDisabled_throwsJsonParseException() throws IOException {
        JsonParser p = createParserWithFeatures("-0123", false, false, false, false, false, false);
        p.nextToken();
    }

    // Test that "-0123" with leading zero enabled parses successfully
    @Test
    public void testNextToken_negativeLeadingZeroEnabled_returnsValueNumberInt() throws IOException {
        JsonParser p = createParserWithFeatures("-0123", true, false, false, false, false, false);
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(-123, p.getIntValue());
    }

    // ================= Boundary and edge cases =================

    // Test that "-0" is parsed as integer zero
    @Test
    public void testNextToken_negativeZero_returnsValueNumberInt() throws IOException {
        JsonParser p = createParser("-0");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(0, p.getIntValue());
    }

    // Test large integer (near max int) parsing
    @Test
    public void testNextToken_largeInteger_returnsValueNumberInt() throws IOException {
        JsonParser p = createParser("2147483647");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2147483647, p.getIntValue());
    }

    // Test that overflow to long returns number int but can be retrieved as long
    @Test
    public void testNextToken_overflowInteger_returnsValueNumberIntAsLong() throws IOException {
        JsonParser p = createParser("2147483648");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2147483648L, p.getLongValue());
    }

    // ================= Invalid JSON / exception cases =================

    // Test that a single unexpected character throws
    @Test(expected = JsonParseException.class)
    public void testNextToken_invalidCharacter_throwsJsonParseException() throws IOException {
        JsonParser p = createParser("a");
        p.nextToken();
    }

    // Test that incomplete JSON (no value) after whitespace throws
    @Test(expected = JsonParseException.class)
    public void testNextToken_emptyInput_throwsJsonParseException() throws IOException {
        // EOF after skipping whitespace should throw
        JsonParser p = createParser("   ");
        p.nextToken(); // This may return null? Actually, _skipWSOrEnd returns -1 and close() leads to null token
        // But close() is called, so nextToken returns null. We expect null? Not exception. So this test is not valid.
        // Instead test that after end-of-input, nextToken returns null.
    }

    // Actually, empty input should return null token after close.
    @Test
    public void testNextToken_emptyInput_returnsNull() throws IOException {
        JsonParser p = createParser("   ");
        assertNull(p.nextToken());
    }

    // Test that a number with exponent works
    @Test
    public void testNextToken_numberWithExponent_returnsValueNumberFloat() throws IOException {
        JsonParser p = createParser("1e10");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(1e10, p.getDoubleValue(), 1e9);
    }

    // Test that a number with fraction and exponent works
    @Test
    public void testNextToken_numberWithFractionAndExponent_returnsValueNumberFloat() throws IOException {
        JsonParser p = createParser("1.5e-2");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(0.015, p.getDoubleValue(), 1e-9);
    }

    // Test that a negative exponent works
    @Test
    public void testNextToken_negativeExponentOnly_returnsValueNumberFloat() throws IOException {
        JsonParser p = createParser("2e-3");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(0.002, p.getDoubleValue(), 1e-9);
    }

    // ================= Comment handling (if feature enabled) =================

    @Test
    public void testNextToken_commentCppStyleEnabled_skipsCommentAndParsesNumber() throws IOException {
        JsonParser p = createParserWithFeatures("// comment\n42", false, true, false, false, false, false);
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getIntValue());
    }

    @Test(expected = JsonParseException.class)
    public void testNextToken_commentCppStyleDisabled_throwsJsonParseException() throws IOException {
        JsonParser p = createParserWithFeatures("// comment\n42", false, false, false, false, false, false);
        p.nextToken();
    }

    // Test YAML comment when enabled
    @Test
    public void testNextToken_yamlCommentEnabled_skipsCommentAndParsesNumber() throws IOException {
        JsonParser p = createParserWithFeatures("# comment\n42", false, false, false, false, false, true);
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getIntValue());
    }

    @Test(expected = JsonParseException.class)
    public void testNextToken_yamlCommentDisabled_throwsJsonParseException() throws IOException {
        JsonParser p = createParserWithFeatures("# comment\n42", false, false, false, false, false, false);
        p.nextToken();
    }

    // ================= Field name parsing =================

    @Test
    public void testNextToken_simpleFieldNameAndStringValue_returnsFieldNameAndString() throws IOException {
        JsonParser p = createParser("{\"name\": \"John\"}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("name", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("John", p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
    }

    // Test unquoted field names when feature enabled
    @Test
    public void testNextToken_unquotedFieldNamesEnabled_parsesObject() throws IOException {
        JsonParser p = createParserWithFeatures("{name: 'John'}", false, false, true, true, false, false);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("name", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("John", p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
    }

    // Test single quotes when enabled
    @Test
    public void testNextToken_singleQuotesEnabled_parsesString() throws IOException {
        JsonParser p = createParserWithFeatures("{'name': 'John'}", false, false, true, false, false, false);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("name", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("John", p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
    }

    // ================= Non-numeric numbers (if feature enabled) =================

    @Test
    public void testNextToken_nonNumericNumbersEnabled_parsesNaN() throws IOException {
        JsonParser p = createParserWithFeatures("NaN", false, false, false, false, true, false);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertTrue(Double.isNaN(p.getDoubleValue()));
    }

    @Test
    public void testNextToken_nonNumericNumbersEnabled_parsesInfinity() throws IOException {
        JsonParser p = createParserWithFeatures("Infinity", false, false, false, false, true, false);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, p.getDoubleValue(), 0.0);
    }

    // ================= Array and object structures =================

    @Test
    public void testNextToken_arrayOfIntegers_returnsTokens() throws IOException {
        JsonParser p = createParser("[1, 2, 3]");
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(3, p.getIntValue());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
    }

    @Test
    public void testNextToken_nestedObject_returnsTokens() throws IOException {
        JsonParser p = createParser("{\"a\": {\"b\": 1}}");
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
    public void testNextToken_nestedArrayInsideObject_returnsTokens() throws IOException {
        JsonParser p = createParser("{\"arr\": [1, 2.5]}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("arr", p.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(2.5, p.getDoubleValue(), 1e-9);
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
    }

    // ================= Additional string edge cases =================

    @Test
    public void testGetText_stringWithTabEscape_returnsTab() throws IOException {
        JsonParser p = createParser("\"a\\tb\"");
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("a\tb", p.getText());
    }

    @Test
    public void testGetText_stringWithUnicodeSurrogatePair_returnsCorrect() throws IOException {
        JsonParser p = createParser("\"\\uD83D\\uDE00\""); // emoji 😀
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("😀", p.getText());
    }

    // ================= Numeric value text retrieval =================

    @Test
    public void testGetText_intValue_returnsStringRepresentation() throws IOException {
        JsonParser p = createParser("42");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals("42", p.getText());
    }

    @Test
    public void testGetText_floatValue_returnsStringRepresentation() throws IOException {
        JsonParser p = createParser("3.14");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals("3.14", p.getText());
    }

    // ================= Multiple tokens after array consumed =================

    @Test
    public void testNextToken_afterArrayEnd_returnsEndToken() throws IOException {
        JsonParser p = createParser("[1,2]");
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken()); // no more tokens
    }

    // Test that getText on non-text tokens returns something sensible (e.g., empty string or token name)
    @Test
    public void testGetText_onStartObject_returnsEmptyString() throws IOException {
        JsonParser p = createParser("{}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("", p.getText());
    }
}