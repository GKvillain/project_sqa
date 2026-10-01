package com.fasterxml.jackson.core.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.junit.Test;
import static org.junit.Assert.*;

public class ReaderBasedJsonParserTest {

    private JsonParser newParser(String json) throws IOException {
        return new JsonFactory().createParser(new StringReader(json));
    }

    // Tests a normal object/array token sequence.
    @Test
    public void testNextToken_simpleObject_returnsTokenSequence() throws Exception {
        try (JsonParser p = newParser("{\"a\":1,\"b\":[true,null]}")) {
            assertEquals(JsonToken.START_OBJECT, p.nextToken());
            assertEquals(JsonToken.FIELD_NAME, p.nextToken());
            assertEquals("a", p.getCurrentName());
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(1, p.getIntValue());
            assertEquals(JsonToken.FIELD_NAME, p.nextToken());
            assertEquals("b", p.getCurrentName());
            assertEquals(JsonToken.START_ARRAY, p.nextToken());
            assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
            assertEquals(JsonToken.VALUE_NULL, p.nextToken());
            assertEquals(JsonToken.END_ARRAY, p.nextToken());
            assertEquals(JsonToken.END_OBJECT, p.nextToken());
            assertNull(p.nextToken());
        }
    }

    // Tests string parsing with escape sequences.
    @Test
    public void testNextToken_stringWithEscapes_returnsUnescapedText() throws Exception {
        try (JsonParser p = newParser("\"a\\nb\\u0041\"")) {
            assertEquals(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals("a\nbA", p.getText());
        }
    }

    // Tests integer and floating-point number parsing.
    @Test
    public void testNextToken_numberVariants_returnsNumericValues() throws Exception {
        try (JsonParser p = newParser("[0,-1,1.25,2e3]")) {
            assertEquals(JsonToken.START_ARRAY, p.nextToken());

            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(0, p.getIntValue());

            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(-1, p.getIntValue());

            assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
            assertEquals(1.25, p.getDoubleValue(), 0.0);

            assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
            assertEquals(2000.0, p.getDoubleValue(), 0.0);

            assertEquals(JsonToken.END_ARRAY, p.nextToken());
        }
    }

    // Tests literal tokens true, false and null.
    @Test
    public void testNextToken_literals_returnsTokens() throws Exception {
        try (JsonParser p = newParser("[true,false,null]")) {
            assertEquals(JsonToken.START_ARRAY, p.nextToken());
            assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
            assertTrue(p.getBooleanValue());
            assertEquals(JsonToken.VALUE_FALSE, p.nextToken());
            assertFalse(p.getBooleanValue());
            assertEquals(JsonToken.VALUE_NULL, p.nextToken());
            assertEquals(JsonToken.END_ARRAY, p.nextToken());
        }
    }

    // Tests nextFieldName() traversal.
    @Test
    public void testNextFieldName_afterStartObject_returnsFieldNames() throws Exception {
        try (JsonParser p = newParser("{\"a\":1,\"b\":2}")) {
            assertEquals(JsonToken.START_OBJECT, p.nextToken());
            assertEquals("a", p.nextFieldName());
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(1, p.getIntValue());
            assertEquals("b", p.nextFieldName());
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(2, p.getIntValue());
            assertEquals(JsonToken.END_OBJECT, p.nextToken());
        }
    }

    // Tests nextTextValue() for both string and non-string values.
    @Test
    public void testNextTextValue_afterFieldName_returnsStringOrNull() throws Exception {
        try (JsonParser p = newParser("{\"a\":\"x\",\"b\":2}")) {
            assertEquals(JsonToken.START_OBJECT, p.nextToken());
            assertEquals(JsonToken.FIELD_NAME, p.nextToken());
            assertEquals("x", p.nextTextValue());
            assertEquals(JsonToken.FIELD_NAME, p.nextToken());
            assertNull(p.nextTextValue());
            assertEquals(JsonToken.END_OBJECT, p.nextToken());
        }
    }

    // Tests nextIntValue() default handling.
    @Test
    public void testNextIntValue_afterFieldName_returnsIntOrDefault() throws Exception {
        try (JsonParser p = newParser("{\"a\":1,\"b\":\"x\"}")) {
            assertEquals(JsonToken.START_OBJECT, p.nextToken());
            assertEquals(JsonToken.FIELD_NAME, p.nextToken());
            assertEquals(1, p.nextIntValue(0));
            assertEquals(JsonToken.FIELD_NAME, p.nextToken());
            assertEquals(42, p.nextIntValue(42));
            assertEquals(JsonToken.END_OBJECT, p.nextToken());
        }
    }

    // Tests that an unread string is skipped by the next nextToken() call.
    @Test
    public void testNextToken_skipsUnreadStringBeforeNextValue() throws Exception {
        try (JsonParser p = newParser("{\"a\":\"skip\",\"b\":2}")) {
            assertEquals(JsonToken.START_OBJECT, p.nextToken());
            assertEquals(JsonToken.FIELD_NAME, p.nextToken());
            assertEquals(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals(JsonToken.FIELD_NAME, p.nextToken());
            assertEquals("b", p.getCurrentName());
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(2, p.getIntValue());
        }
    }

    // Tests comments as whitespace inside an array when ALLOW_COMMENTS is enabled.
    @Test
    public void testNextToken_commentsInArray_allowed_returnsValues() throws Exception {
        try (JsonParser p = newParser("[/*c*/1, // line\n2]")) {
            p.enable(JsonParser.Feature.ALLOW_COMMENTS);
            assertEquals(JsonToken.START_ARRAY, p.nextToken());
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(1, p.getIntValue());
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(2, p.getIntValue());
            assertEquals(JsonToken.END_ARRAY, p.nextToken());
        }
    }

    // Tests root-value separation by comments when comment features are enabled.
    @Test
    public void testParseNumber_rootValueSeparatedByCommentEnabled_acceptsCommentAsSeparator() throws Exception {
        assertRootValueSeparatedByComment(JsonParser.Feature.ALLOW_COMMENTS, "1/*sep*/2");
        assertRootValueSeparatedByComment(JsonParser.Feature.ALLOW_YAML_COMMENTS, "1#sep\n2");
    }

    private void assertRootValueSeparatedByComment(JsonParser.Feature feature, String json) throws Exception {
        try (JsonParser p = newParser(json)) {
            p.enable(feature);
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(1, p.getIntValue());
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(2, p.getIntValue());
        }
    }

    // Tests rejection of leading zeroes by default.
    @Test(expected = JsonParseException.class)
    public void testParseNumber_leadingZero_disabled_throwsJsonParseException() throws Exception {
        try (JsonParser p = newParser("01")) {
            p.nextToken();
        }
    }

    // Tests acceptance of leading zeroes when the feature is enabled.
    @Test
    public void testParseNumber_leadingZero_enabled_parsesValue() throws Exception {
        try (JsonParser p = newParser("0001")) {
            p.enable(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS);
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(1, p.getIntValue());
        }
    }

    // Tests single-quoted strings and field names when ALLOW_SINGLE_QUOTES is enabled.
    @Test
    public void testNextToken_singleQuotes_enabled_parsesObject() throws Exception {
        try (JsonParser p = newParser("{'a':'x'}")) {
            p.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
            assertEquals(JsonToken.START_OBJECT, p.nextToken());
            assertEquals(JsonToken.FIELD_NAME, p.nextToken());
            assertEquals("a", p.getCurrentName());
            assertEquals(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals("x", p.getText());
            assertEquals(JsonToken.END_OBJECT, p.nextToken());
        }
    }

    // Tests unquoted field names when ALLOW_UNQUOTED_FIELD_NAMES is enabled.
    @Test
    public void testNextToken_unquotedFieldNames_enabled_parsesObject() throws Exception {
        try (JsonParser p = newParser("{a:1}")) {
            p.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
            assertEquals(JsonToken.START_OBJECT, p.nextToken());
            assertEquals(JsonToken.FIELD_NAME, p.nextToken());
            assertEquals("a", p.getCurrentName());
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(1, p.getIntValue());
            assertEquals(JsonToken.END_OBJECT, p.nextToken());
        }
    }

    // Tests base64 decoding through getBinaryValue().
    @Test
    public void testGetBinaryValue_base64String_decodesBytes() throws Exception {
        try (JsonParser p = newParser("\"YWJj\"")) {
            assertEquals(JsonToken.VALUE_STRING, p.nextToken());
            assertArrayEquals(new byte[] {'a', 'b', 'c'},
                    p.getBinaryValue(Base64Variants.getDefaultVariant()));
        }
    }

    // Tests incremental base64 decoding through readBinaryValue().
    @Test
    public void testReadBinaryValue_base64String_writesBytes() throws Exception {
        try (JsonParser p = newParser("\"YWJj\"")) {
            assertEquals(JsonToken.VALUE_STRING, p.nextToken());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int count = p.readBinaryValue(Base64Variants.getDefaultVariant(), out);
            assertEquals(3, count);
            assertArrayEquals(new byte[] {'a', 'b', 'c'}, out.toByteArray());
        }
    }

    // Tests malformed JSON: missing value after field name.
    @Test(expected = JsonParseException.class)
    public void testNextToken_missingValue_throwsJsonParseException() throws Exception {
        try (JsonParser p = newParser("{\"a\":}")) {
            p.nextToken();
            p.nextToken();
        }
    }

    // Tests that a root number must be followed by a valid separator.
    @Test(expected = JsonParseException.class)
    public void testParseNumber_missingRootValueSeparator_throwsJsonParseException() throws Exception {
        try (JsonParser p = newParser("1true")) {
            p.nextToken();
        }
    }

    // ========== New tests to improve coverage ==========

    // Tests ALLOW_TRAILING_COMMA: array with trailing comma
    @Test
    public void testNextToken_trailingComma_enabled_parsesArray() throws Exception {
        try (JsonParser p = newParser("[1,2,]")) {
            p.enable(JsonParser.Feature.ALLOW_TRAILING_COMMA);
            assertEquals(JsonToken.START_ARRAY, p.nextToken());
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(1, p.getIntValue());
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(2, p.getIntValue());
            assertEquals(JsonToken.END_ARRAY, p.nextToken());
            assertNull(p.nextToken());
        }
    }

    // Tests ALLOW_MISSING_VALUES: array with missing value (null placeholder)
    @Test
    public void testNextToken_missingValueInArray_enabled_returnsNullValue() throws Exception {
        try (JsonParser p = newParser("[1,,2]")) {
            p.enable(JsonParser.Feature.ALLOW_MISSING_VALUES);
            assertEquals(JsonToken.START_ARRAY, p.nextToken());
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(1, p.getIntValue());
            assertEquals(JsonToken.VALUE_NULL, p.nextToken());
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(2, p.getIntValue());
            assertEquals(JsonToken.END_ARRAY, p.nextToken());
        }
    }

    // Tests STRICT_DUPLICATE_DETECTION: duplicate field name throws exception
    @Test(expected = JsonParseException.class)
    public void testNextToken_duplicateDetection_throwsException() throws Exception {
        try (JsonParser p = newParser("{\"a\":1,\"a\":2}")) {
            p.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
            // Must parse fully to trigger detection
            while (p.nextToken() != null) {
                // consume everything
            }
        }
    }

    // Tests ALLOW_NON_NUMBERS: parsing NaN and Infinity
    @Test
    public void testNextToken_nonNumbers_enabled_returnsFloatValues() throws Exception {
        try (JsonParser p = newParser("[NaN, Infinity, -Infinity]")) {
            p.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
            assertEquals(JsonToken.START_ARRAY, p.nextToken());

            assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
            assertTrue(Double.isNaN(p.getDoubleValue()));

            assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
            assertEquals(Double.POSITIVE_INFINITY, p.getDoubleValue(), 0.0);

            assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
            assertEquals(Double.NEGATIVE_INFINITY, p.getDoubleValue(), 0.0);

            assertEquals(JsonToken.END_ARRAY, p.nextToken());
        }
    }

    // Tests getTextCharacters(), getTextLength(), getTextOffset() on a string token
    @Test
    public void testGetTextCharacters_onStringToken_returnsCharacters() throws Exception {
        try (JsonParser p = newParser("\"hello\"")) {
            assertEquals(JsonToken.VALUE_STRING, p.nextToken());
            char[] chars = p.getTextCharacters();
            int offset = p.getTextOffset();
            int len = p.getTextLength();
            assertEquals("hello", new String(chars, offset, len));
        }
    }

    // Tests close() and isClosed()
    @Test
    public void testCloseAndIsClosed_closed_returnsTrue() throws Exception {
        JsonParser p = newParser("{}");
        assertFalse(p.isClosed());
        p.close();
        assertTrue(p.isClosed());
    }

    // Tests getCurrentLocation() returns non-null location
    @Test
    public void testGetCurrentLocation_afterToken_returnsLocation() throws Exception {
        try (JsonParser p = newParser("{}")) {
            p.nextToken();
            assertNotNull(p.getCurrentLocation());
            assertTrue(p.getCurrentLocation().getCharOffset() > 0);
        }
    }

    // Tests getTokenLocation() returns non-null location
    @Test
    public void testGetTokenLocation_afterToken_returnsLocation() throws Exception {
        try (JsonParser p = newParser("{}")) {
            p.nextToken();
            assertNotNull(p.getTokenLocation());
            assertTrue(p.getTokenLocation().getCharOffset() > 0);
        }
    }

    // Tests error: invalid escape sequence
    @Test(expected = JsonParseException.class)
    public void testNextToken_invalidEscape_throwsException() throws Exception {
        try (JsonParser p = newParser("\"\\x\"")) {
            p.nextToken();
        }
    }

    // Tests error: unterminated string
    @Test(expected = JsonParseException.class)
    public void testNextToken_unterminatedString_throwsException() throws Exception {
        try (JsonParser p = newParser("\"unfinished")) {
            p.nextToken();
        }
    }
}