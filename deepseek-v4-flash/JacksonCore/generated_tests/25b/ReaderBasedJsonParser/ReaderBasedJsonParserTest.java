package com.fasterxml.jackson.core.json;

import static org.junit.Assert.*;
import org.junit.Test;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.*;
import com.fasterxml.jackson.core.sym.*;

import java.io.*;

public class ReaderBasedJsonParserTest {

    // ------------------------------------------------------------
    // Helper methods to create parser instances
    // ------------------------------------------------------------

    private ReaderBasedJsonParser createParser(String input, int features, int bufferSize) throws IOException {
        IOContext ioContext = new IOContext(null, null, false);
        CharsToNameCanonicalizer symbolTable = CharsToNameCanonicalizer.createRoot();
        Reader reader = new StringReader(input);
        char[] buffer = new char[bufferSize];
        return new ReaderBasedJsonParser(ioContext, features, reader, null, symbolTable, buffer, 0, 0, false);
    }

    private ReaderBasedJsonParser createParser(String input, int features) throws IOException {
        // default buffer size (large enough to hold most test inputs)
        return createParser(input, features, 1000);
    }

    private void assertToken(JsonToken expected, JsonToken actual) {
        assertEquals("Token type mismatch", expected, actual);
    }

    // ------------------------------------------------------------
    // Tests – normal cases
    // ------------------------------------------------------------

    // Tests that empty input returns null token and parser closes
    @Test
    public void testNextToken_emptyInput_returnsNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("", 0);
        assertNull(parser.nextToken());
        assertNull(parser.getCurrentToken());
    }

    // Tests simple true, false, null tokens
    @Test
    public void testNextToken_booleanAndNull_returnsCorrectTokens() throws IOException {
        ReaderBasedJsonParser parser = createParser("true false null", 0);
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        assertToken(JsonToken.VALUE_FALSE, parser.nextToken());
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.nextToken());
    }

    // Tests integer, negative integer, float, and exponent numbers
    @Test
    public void testNextToken_numberTypes_parsesCorrectly() throws IOException {
        // integer
        ReaderBasedJsonParser p1 = createParser("42", 0);
        assertToken(JsonToken.VALUE_NUMBER_INT, p1.nextToken());
        assertEquals("42", p1.getText());
        assertEquals(42, p1.getIntValue());

        // negative
        ReaderBasedJsonParser p2 = createParser("-7", 0);
        assertToken(JsonToken.VALUE_NUMBER_INT, p2.nextToken());
        assertEquals("-7", p2.getText());
        assertEquals(-7, p2.getIntValue());

        // float
        ReaderBasedJsonParser p3 = createParser("3.14", 0);
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p3.nextToken());
        assertEquals("3.14", p3.getText());

        // exponent
        ReaderBasedJsonParser p4 = createParser("2e10", 0);
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, p4.nextToken());
        assertEquals("2e10", p4.getText());
    }

    // Tests simple string value
    @Test
    public void testNextToken_simpleString_returnsString() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"hello\"", 0);
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getText());
    }

    // Tests string with escape sequences
    @Test
    public void testNextToken_escapedString_correctDecoding() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"line1\\nline2\"", 0);
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("line1\nline2", parser.getText());
    }

    // Tests Unicode escape \\u0041 -> 'A'
    @Test
    public void testNextToken_unicodeEscape_correctDecoding() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"\\u0041\"", 0);
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("A", parser.getText());
    }

    // Tests simple array
    @Test
    public void testNextToken_array_returnsTokens() throws IOException {
        ReaderBasedJsonParser parser = createParser("[1,2,3]", 0);
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(3, parser.getIntValue());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }

    // Tests simple object
    @Test
    public void testNextToken_object_returnsTokens() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"key\":123}", 0);
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getText());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    // Tests object with multiple fields using nextTextValue shortcut
    @Test
    public void testNextTextValue_stringValue_returnsString() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":\"val\"}", 0);
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("val", parser.nextTextValue());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
    }

    // ------------------------------------------------------------
    // Tests – boundary / buffer boundary
    // ------------------------------------------------------------

    // Tests that a long string spanning buffer boundaries is correctly parsed
    @Test
    public void testNextToken_stringTraversingBuffer_parsesCorrectly() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"abcdefghij\"", 0, 4);
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("abcdefghij", parser.getText());
    }

    // Tests that a long number spanning buffer boundaries is correctly parsed
    @Test
    public void testNextToken_numberTraversingBuffer_parsesCorrectly() throws IOException {
        ReaderBasedJsonParser parser = createParser("1234567890", 0, 4);
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("1234567890", parser.getText());
    }

    // Tests that a keyword spanning buffer boundaries is correctly parsed
    @Test
    public void testNextToken_keywordTraversingBuffer_parsesCorrectly() throws IOException {
        ReaderBasedJsonParser parser = createParser("  true  ", 0, 3);
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
    }

    // Tests string with escape that spans buffer boundaries
    @Test
    public void testNextToken_escapedStringSpanningBuffer_correctDecoding() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"ab\\ncdef\"", 0, 3);
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("ab\ncdef", parser.getText());
    }

    // Tests float with decimal part crossing buffer boundary
    @Test
    public void testNextToken_floatTraversingBuffer_parsesCorrectly() throws IOException {
        ReaderBasedJsonParser parser = createParser("123.456", 0, 3);
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals("123.456", parser.getText());
    }

    // ------------------------------------------------------------
    // Tests – feature flags
    // ------------------------------------------------------------

    // Tests trailing comma when ALLOW_TRAILING_COMMA is enabled
    @Test
    public void testNextToken_trailingComma_allowed_accepts() throws IOException {
        int features = Feature.ALLOW_TRAILING_COMMA.getMask();
        ReaderBasedJsonParser parser = createParser("[1,2,]", features);
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
    }

    // Tests trailing comma when flag is not set (should throw)
    @Test(expected = JsonParseException.class)
    public void testNextToken_trailingComma_notAllowed_throwsException() throws IOException {
        ReaderBasedJsonParser parser = createParser("[1,]", 0);
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // 1
        parser.nextToken(); // should fail on trailing comma
    }

    // Tests missing values with ALLOW_MISSING_VALUES
    @Test
    public void testNextToken_missingValues_allowed_parsesNull() throws IOException {
        int features = Feature.ALLOW_MISSING_VALUES.getMask();
        ReaderBasedJsonParser parser = createParser("[,1]", features);
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
    }

    // Tests single-quoted strings with ALLOW_SINGLE_QUOTES
    @Test
    public void testNextToken_singleQuotes_allowed_parsesString() throws IOException {
        int features = Feature.ALLOW_SINGLE_QUOTES.getMask();
        ReaderBasedJsonParser parser = createParser("'hello'", features);
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getText());
    }

    // Tests unquoted field names with ALLOW_UNQUOTED_FIELD_NAMES
    @Test
    public void testNextToken_unquotedFieldNames_allowed_parsesName() throws IOException {
        int features = Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask();
        ReaderBasedJsonParser parser = createParser("{abc:123}", features);
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("abc", parser.getText());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
    }

    // Tests comments with ALLOW_COMMENTS
    @Test
    public void testNextToken_comments_allowed_skipsAndParsesValue() throws IOException {
        int features = Feature.ALLOW_COMMENTS.getMask();
        ReaderBasedJsonParser parser = createParser("/* comment */ 789", features);
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(789, parser.getIntValue());
    }

    // Tests non-numeric numbers with ALLOW_NON_NUMERIC_NUMBERS
    @Test
    public void testNextToken_nonNumericNumbers_allowed_parsesNaN() throws IOException {
        int features = Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask();
        ReaderBasedJsonParser parser = createParser("NaN", features);
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(Double.isNaN(parser.getDoubleValue()));
    }

    // ------------------------------------------------------------
    // Tests – error/invalid cases
    // ------------------------------------------------------------

    // Tests that two leading zeros (e.g. "00") throw when not allowed
    @Test(expected = JsonParseException.class)
    public void testNextToken_leadingZero_notAllowed_throwsException() throws IOException {
        ReaderBasedJsonParser parser = createParser("00", 0);
        parser.nextToken();
    }

    // Tests that an unexpected character (e.g. 'x') throws properly
    @Test(expected = JsonParseException.class)
    public void testNextToken_unexpectedCharacter_throwsException() throws IOException {
        ReaderBasedJsonParser parser = createParser("x", 0);
        parser.nextToken();
    }

    // Tests invalid character after a root value triggers error
    @Test(expected = JsonParseException.class)
    public void testNextToken_invalidCharAfterRootValue_throwsException() throws IOException {
        ReaderBasedJsonParser parser = createParser("42x", 0);
        parser.nextToken(); // while parsing number, after digits sees 'x' and should fail
    }

    // ------------------------------------------------------------
    // Additional tests for uncovered area: SKIPPED: AI test suite compile failed
    // ------------------------------------------------------------

    @Test
    public void testNextToken_emptyArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("[]", 0);
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test
    public void testNextToken_emptyObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{}", 0);
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test
    public void testNextToken_nestedArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("[[[1]]]", 0);
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test
    public void testGetValueAsString_string() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"abc\"", 0);
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("abc", parser.getValueAsString());
    }

    @Test
    public void testGetValueAsString_number() throws IOException {
        ReaderBasedJsonParser parser = createParser("42", 0);
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("42", parser.getValueAsString());
    }

    @Test
    public void testGetValueAsInt_number() throws IOException {
        ReaderBasedJsonParser parser = createParser("42", 0);
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getValueAsInt());
    }

    @Test
    public void testGetValueAsInt_stringNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"42\"", 0);
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(42, parser.getValueAsInt());
    }

    @Test
    public void testGetValueAsBoolean_true() throws IOException {
        ReaderBasedJsonParser parser = createParser("true", 0);
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        assertTrue(parser.getValueAsBoolean());
    }

    @Test
    public void testGetValueAsBoolean_false() throws IOException {
        ReaderBasedJsonParser parser = createParser("false", 0);
        assertToken(JsonToken.VALUE_FALSE, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
    }

    @Test
    public void testNextToken_infinity_allowed() throws IOException {
        int features = Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask();
        ReaderBasedJsonParser parser = createParser("Infinity", features);
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(Double.isInfinite(parser.getDoubleValue()));
    }

    @Test
    public void testNextToken_negativeInfinity_allowed() throws IOException {
        int features = Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask();
        ReaderBasedJsonParser parser = createParser("-Infinity", features);
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(Double.isInfinite(parser.getDoubleValue()));
        assertTrue(parser.getDoubleValue() < 0);
    }

    @Test
    public void testNextToken_escapedBackslash() throws IOException {
        // JSON string "\\" produces a single backslash
        ReaderBasedJsonParser parser = createParser("\"\\\\\"", 0);
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("\\", parser.getText());
    }

    @Test
    public void testNextToken_escapedQuote() throws IOException {
        // JSON string "\"" produces a double quote
        ReaderBasedJsonParser parser = createParser("\"\\\"\"", 0);
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("\"", parser.getText());
    }

    @Test
    public void testNextToken_nullValueInObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null}", 0);
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getText());
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test
    public void testNextToken_negativeFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("-3.14", 0);
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals("-3.14", parser.getText());
        assertEquals(-3.14, parser.getDoubleValue(), 1e-9);
    }

    @Test
    public void testNextToken_exponentNegative() throws IOException {
        ReaderBasedJsonParser parser = createParser("2e-3", 0);
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(0.002, parser.getDoubleValue(), 1e-9);
    }

    @Test
    public void testNextToken_exponentCapitalE() throws IOException {
        ReaderBasedJsonParser parser = createParser("2E10", 0);
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(2e10, parser.getDoubleValue(), 1e-9);
    }
}