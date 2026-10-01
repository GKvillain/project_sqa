package com.fasterxml.jackson.core.json.async;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.core.*;

public class NonBlockingJsonParserTest {
    
    // Helper methods
    private NonBlockingJsonParser createParser(JsonParser.Feature... features) throws IOException {
        JsonFactory factory = new JsonFactory();
        for (JsonParser.Feature f : features) {
            factory.enable(f);
        }
        return (NonBlockingJsonParser) factory.createNonBlockingByteArrayParser();
    }
    
    private void feedAndEnd(NonBlockingJsonParser parser, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        parser.feedInput(bytes, 0, bytes.length);
        parser.endOfInput();
    }
    
    private JsonToken nextToken(NonBlockingJsonParser parser) throws IOException {
        JsonToken t;
        while ((t = parser.nextToken()) == JsonToken.NOT_AVAILABLE) {
            // should not happen if data fully fed and ended
        }
        return t;
    }
    
    // Test 1: Simple object with field name and integer value
    @Test
    public void testSimpleObject_validInput_returnsCorrectTokens() throws IOException {
        NonBlockingJsonParser parser = createParser();
        feedAndEnd(parser, "{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, nextToken(parser));
        assertNull(nextToken(parser));
    }
    
    // Test 2: Simple array of integers
    @Test
    public void testSimpleArray_validInput_returnsCorrectTokens() throws IOException {
        NonBlockingJsonParser parser = createParser();
        feedAndEnd(parser, "[1,2,3]");
        assertEquals(JsonToken.START_ARRAY, nextToken(parser));
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(2, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(3, parser.getIntValue());
        assertEquals(JsonToken.END_ARRAY, nextToken(parser));
        assertNull(nextToken(parser));
    }
    
    // Test 3: Nested object and array
    @Test
    public void testNestedStructure_validInput_returnsCorrectTokens() throws IOException {
        NonBlockingJsonParser parser = createParser();
        feedAndEnd(parser, "{\"x\":{\"y\":[]}}");
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        assertEquals("x", parser.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        assertEquals("y", parser.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, nextToken(parser));
        assertEquals(JsonToken.END_ARRAY, nextToken(parser));
        assertEquals(JsonToken.END_OBJECT, nextToken(parser));
        assertEquals(JsonToken.END_OBJECT, nextToken(parser));
        assertNull(nextToken(parser));
    }
    
    // Test 4: String with escape sequence (newline)
    @Test
    public void testStringWithEscapes_validInput_returnsCorrectString() throws IOException {
        NonBlockingJsonParser parser = createParser();
        feedAndEnd(parser, "{\"s\":\"hello\\nworld\"}");
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        assertEquals("s", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, nextToken(parser));
        assertEquals("hello\nworld", parser.getText());
        assertEquals(JsonToken.END_OBJECT, nextToken(parser));
        assertNull(nextToken(parser));
    }
    
    // Test 5: Positive integer value
    @Test
    public void testPositiveInteger_validInput_returnsCorrectIntValue() throws IOException {
        NonBlockingJsonParser parser = createParser();
        feedAndEnd(parser, "42");
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(42, parser.getIntValue());
        assertNull(nextToken(parser));
    }
    
    // Test 6: Negative integer value
    @Test
    public void testNegativeNumber_validInput_returnsCorrectIntValue() throws IOException {
        NonBlockingJsonParser parser = createParser();
        feedAndEnd(parser, "-7");
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(-7, parser.getIntValue());
        assertNull(nextToken(parser));
    }
    
    // Test 7: Float number with decimal
    @Test
    public void testFloatNumber_validInput_returnsCorrectFloatValue() throws IOException {
        NonBlockingJsonParser parser = createParser();
        feedAndEnd(parser, "3.14");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, nextToken(parser));
        assertEquals(3.14, parser.getDoubleValue(), 0.001);
        assertNull(nextToken(parser));
    }
    
    // Test 8: Leading zero not allowed by default -> exception
    @Test(expected = JsonParseException.class)
    public void testLeadingZeroNotAllowed_throwsException() throws IOException {
        NonBlockingJsonParser parser = createParser();
        feedAndEnd(parser, "01");
        while (parser.nextToken() != null) {}
    }
    
    // Test 9: Leading zero allowed with feature ALLOW_NUMERIC_LEADING_ZEROS
    @Test
    public void testLeadingZeroAllowed_validInput_returnsCorrectNumber() throws IOException {
        NonBlockingJsonParser parser = createParser(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS);
        feedAndEnd(parser, "01");
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(1, parser.getIntValue());
        assertNull(nextToken(parser));
    }
    
    // Test 10: Boolean (true, false) and null
    @Test
    public void testBooleanAndNull_validInput_returnsCorrectTokens() throws IOException {
        NonBlockingJsonParser parser = createParser();
        feedAndEnd(parser, "[true,false,null]");
        assertEquals(JsonToken.START_ARRAY, nextToken(parser));
        assertEquals(JsonToken.VALUE_TRUE, nextToken(parser));
        assertEquals(JsonToken.VALUE_FALSE, nextToken(parser));
        assertEquals(JsonToken.VALUE_NULL, nextToken(parser));
        assertEquals(JsonToken.END_ARRAY, nextToken(parser));
        assertNull(nextToken(parser));
    }
    
    // Test 11: Trailing comma in object (ALLOW_TRAILING_COMMA)
    @Test
    public void testTrailingCommaInObject_allowed_parsesSuccessfully() throws IOException {
        NonBlockingJsonParser parser = createParser(JsonParser.Feature.ALLOW_TRAILING_COMMA);
        feedAndEnd(parser, "{\"a\":1,}");
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, nextToken(parser));
        assertNull(nextToken(parser));
    }
    
    // Test 12: Trailing comma in array (ALLOW_TRAILING_COMMA)
    @Test
    public void testTrailingCommaInArray_allowed_parsesSuccessfully() throws IOException {
        NonBlockingJsonParser parser = createParser(JsonParser.Feature.ALLOW_TRAILING_COMMA);
        feedAndEnd(parser, "[1,]");
        assertEquals(JsonToken.START_ARRAY, nextToken(parser));
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.END_ARRAY, nextToken(parser));
        assertNull(nextToken(parser));
    }
    
    // Test 13: Missing values in array (ALLOW_MISSING_VALUES)
    @Test
    public void testMissingValues_allowed_returnsNullTokens() throws IOException {
        NonBlockingJsonParser parser = createParser(JsonParser.Feature.ALLOW_MISSING_VALUES);
        feedAndEnd(parser, "[1,,2]");
        assertEquals(JsonToken.START_ARRAY, nextToken(parser));
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NULL, nextToken(parser));
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(2, parser.getIntValue());
        assertEquals(JsonToken.END_ARRAY, nextToken(parser));
        assertNull(nextToken(parser));
    }
    
    // Test 14: Single-quoted strings (ALLOW_SINGLE_QUOTES)
    @Test
    public void testSingleQuotes_allowed_parsesSuccessfully() throws IOException {
        NonBlockingJsonParser parser = createParser(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        feedAndEnd(parser, "{'a':1}");
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, nextToken(parser));
        assertNull(nextToken(parser));
    }
    
    // Test 15: Unquoted field names (ALLOW_UNQUOTED_FIELD_NAMES)
    @Test
    public void testUnquotedNames_allowed_parsesSuccessfully() throws IOException {
        NonBlockingJsonParser parser = createParser(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        feedAndEnd(parser, "{a:1}");
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, nextToken(parser));
        assertNull(nextToken(parser));
    }
    
    // Test 16: C++ style comment (//) with ALLOW_COMMENTS
    @Test
    public void testCppComment_allowed_parsesSuccessfully() throws IOException {
        NonBlockingJsonParser parser = createParser(JsonParser.Feature.ALLOW_COMMENTS);
        feedAndEnd(parser, "//comment\n{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, nextToken(parser));
        assertNull(nextToken(parser));
    }
    
    // Test 17: C style comment (/* */) with ALLOW_COMMENTS
    @Test
    public void testCComment_allowed_parsesSuccessfully() throws IOException {
        NonBlockingJsonParser parser = createParser(JsonParser.Feature.ALLOW_COMMENTS);
        feedAndEnd(parser, "/* comment */{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, nextToken(parser));
        assertNull(nextToken(parser));
    }
    
    // Test 18: YAML style comment (#) with ALLOW_YAML_COMMENTS
    @Test
    public void testYamlComment_allowed_parsesSuccessfully() throws IOException {
        NonBlockingJsonParser parser = createParser(JsonParser.Feature.ALLOW_YAML_COMMENTS);
        feedAndEnd(parser, "# comment\n{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, nextToken(parser));
        assertNull(nextToken(parser));
    }
    
    // Test 19: Invalid token (e.g., "xyz") throws exception
    @Test(expected = JsonParseException.class)
    public void testInvalidToken_throwsException() throws IOException {
        NonBlockingJsonParser parser = createParser();
        feedAndEnd(parser, "xyz");
        while (parser.nextToken() != null) {}
    }
    
    // Test 20: EOF inside a token (e.g., "nul") throws exception
    @Test(expected = JsonParseException.class)
    public void testEOFInsideToken_throwsException() throws IOException {
        NonBlockingJsonParser parser = createParser();
        feedAndEnd(parser, "nul");
        while (parser.nextToken() != null) {}
    }

    // ========== New tests for uncovered areas ==========

    // Test 21: Partial input (feed in chunks) - verify NOT_AVAILABLE handling
    @Test
    public void testPartialInput_chunks_returnsCorrectTokens() throws IOException {
        NonBlockingJsonParser parser = createParser();
        String json = "{\"a\":1}";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        // feed first character '{'
        parser.feedInput(bytes, 0, 1);
        assertEquals(JsonToken.NOT_AVAILABLE, parser.nextToken());
        // feed rest
        parser.feedInput(bytes, 1, bytes.length);
        parser.endOfInput();
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, nextToken(parser));
        assertNull(nextToken(parser));
    }

    // Test 22: getNonBlockingInputFeeder() returns non-null feeder
    @Test
    public void testGetNonBlockingInputFeeder_returnsNonNull() throws IOException {
        NonBlockingJsonParser parser = createParser();
        assertNotNull(parser.getNonBlockingInputFeeder());
    }

    // Test 23: ALLOW_NON_NUMERIC_NUMBERS - NaN
    @Test
    public void testNaN_allowed_returnsCorrectValue() throws IOException {
        NonBlockingJsonParser parser = createParser(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
        feedAndEnd(parser, "NaN");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, nextToken(parser));
        assertTrue(Double.isNaN(parser.getDoubleValue()));
        assertNull(nextToken(parser));
    }

    // Test 24: ALLOW_NON_NUMERIC_NUMBERS - Infinity
    @Test
    public void testInfinity_allowed_returnsCorrectValue() throws IOException {
        NonBlockingJsonParser parser = createParser(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
        feedAndEnd(parser, "Infinity");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, nextToken(parser));
        assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);
        assertNull(nextToken(parser));
    }

    // Test 25: ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER - e.g., \x
    @Test
    public void testBackslashEscapingAnyCharacter_allowed_parsesSuccessfully() throws IOException {
        NonBlockingJsonParser parser = createParser(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
        feedAndEnd(parser, "\"\\x\"");
        assertEquals(JsonToken.VALUE_STRING, nextToken(parser));
        assertEquals("x", parser.getText());
        assertNull(nextToken(parser));
    }

    // Test 26: STRICT_DUPLICATE_DETECTION - duplicate key throws exception
    @Test(expected = JsonParseException.class)
    public void testStrictDuplicateDetection_duplicateKey_throwsException() throws IOException {
        NonBlockingJsonParser parser = createParser(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        feedAndEnd(parser, "{\"a\":1,\"a\":2}");
        while (parser.nextToken() != null) {}
    }

    // Test 27: Unclosed object (missing '}') throws exception
    @Test(expected = JsonParseException.class)
    public void testUnclosedObject_throwsException() throws IOException {
        NonBlockingJsonParser parser = createParser();
        feedAndEnd(parser, "{\"a\":1");
        while (parser.nextToken() != null) {}
    }

    // Test 28: Invalid escape sequence in string (e.g., \z) without ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER
    @Test(expected = JsonParseException.class)
    public void testInvalidEscapeSequence_throwsException() throws IOException {
        NonBlockingJsonParser parser = createParser();
        feedAndEnd(parser, "\"\\z\"");
        while (parser.nextToken() != null) {}
    }
}