package com.fasterxml.jackson.core.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.*;

public class FilteringParserDelegateTest {

    private final JsonFactory JSON_F = new JsonFactory();

    // Tests initialization and filter accessors
    @Test
    public void testGetFilter_initialization_returnsRootFilter() throws IOException {
        JsonParser p = JSON_F.createParser("{\"a\": 1}");
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate delegate = new FilteringParserDelegate(p, filter, true, true);

        assertSame(filter, delegate.getFilter());
        assertEquals(0, delegate.getMatchCount());
        assertNull(delegate.getCurrentToken());
        assertEquals(JsonTokenId.ID_NO_TOKEN, delegate.getCurrentTokenId());
        assertEquals(JsonTokenId.ID_NO_TOKEN, delegate.currentTokenId());
        assertFalse(delegate.hasCurrentToken());
        assertTrue(delegate.hasTokenId(JsonTokenId.ID_NO_TOKEN));
        delegate.close();
    }

    // Tests token traversal with TokenFilter.INCLUDE_ALL
    @Test
    public void testNextToken_includeAll_traversesEntireJson() throws IOException {
        String json = "{\"a\": 123, \"b\": [true, null, \"str\"]}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate delegate = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_OBJECT, delegate.nextToken());
        assertTrue(delegate.isExpectedStartObjectToken());
        assertFalse(delegate.isExpectedStartArrayToken());
        assertTrue(delegate.hasToken(JsonToken.START_OBJECT));
        assertTrue(delegate.hasCurrentToken());

        assertEquals(JsonToken.FIELD_NAME, delegate.nextToken());
        assertEquals("a", delegate.getCurrentName());
        assertEquals("a", delegate.getText());

        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.nextToken());
        assertEquals(123, delegate.getIntValue());
        assertEquals(123L, delegate.getLongValue());
        assertEquals(123.0, delegate.getDoubleValue(), 0.0001);
        assertEquals(123.0f, delegate.getFloatValue(), 0.0001f);
        assertEquals((short) 123, delegate.getShortValue());
        assertEquals((byte) 123, delegate.getByteValue());
        assertEquals(BigInteger.valueOf(123), delegate.getBigIntegerValue());
        assertEquals(BigDecimal.valueOf(123), delegate.getDecimalValue());
        assertEquals(123, delegate.getValueAsInt());
        assertEquals(123, delegate.getValueAsInt(0));
        assertEquals(123L, delegate.getValueAsLong());
        assertEquals(123L, delegate.getValueAsLong(0L));
        assertEquals(123.0, delegate.getValueAsDouble(), 0.0001);
        assertEquals(123.0, delegate.getValueAsDouble(0.0), 0.0001);
        assertEquals("123", delegate.getValueAsString());
        assertEquals("123", delegate.getValueAsString(""));
        assertEquals(JsonParser.NumberType.INT, delegate.getNumberType());
        assertEquals(Integer.valueOf(123), delegate.getNumberValue());

        assertEquals(JsonToken.FIELD_NAME, delegate.nextToken());
        assertEquals("b", delegate.getCurrentName());

        assertEquals(JsonToken.START_ARRAY, delegate.nextToken());
        assertTrue(delegate.isExpectedStartArrayToken());

        assertEquals(JsonToken.VALUE_TRUE, delegate.nextToken());
        assertTrue(delegate.getBooleanValue());
        assertTrue(delegate.getValueAsBoolean());
        assertTrue(delegate.getValueAsBoolean(false));

        assertEquals(JsonToken.VALUE_NULL, delegate.nextToken());

        assertEquals(JsonToken.VALUE_STRING, delegate.nextToken());
        assertEquals("str", delegate.getText());
        assertTrue(delegate.hasTextCharacters());
        assertNotNull(delegate.getTextCharacters());
        assertEquals(3, delegate.getTextLength());
        assertEquals(0, delegate.getTextOffset());

        assertEquals(JsonToken.END_ARRAY, delegate.nextToken());
        assertEquals(JsonToken.END_OBJECT, delegate.nextToken());
        assertNull(delegate.nextToken());
        delegate.close();
    }

    // Tests scalar root value when allowMultipleMatches is false
    @Test
    public void testNextToken_scalarAllowMultipleFalse_returnsSingleTokenAndNullNext() throws IOException {
        JsonParser p = JSON_F.createParser("42 99");
        FilteringParserDelegate delegate = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, false, false);

        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.nextToken());
        assertEquals(42, delegate.getIntValue());
        assertNull(delegate.nextToken());
        delegate.close();
    }

    // Tests single property filter with includePath = true
    @Test
    public void testNextToken_filterPropertyIncludePathTrue_includesPathAndMatchedProperty() throws IOException {
        String json = "{\"a\": 1, \"b\": 2, \"c\": 3}";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("b".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return null;
            }
        };

        FilteringParserDelegate delegate = new FilteringParserDelegate(p, filter, true, true);

        assertEquals(JsonToken.START_OBJECT, delegate.nextToken());
        assertEquals(JsonToken.FIELD_NAME, delegate.nextToken());
        assertEquals("b", delegate.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.nextToken());
        assertEquals(2, delegate.getIntValue());
        assertEquals(JsonToken.END_OBJECT, delegate.nextToken());
        assertNull(delegate.nextToken());
        delegate.close();
    }

    // Tests single property filter with includePath = false
    @Test
    public void testNextToken_filterPropertyIncludePathFalse_onlyReturnsValue() throws IOException {
        String json = "{\"a\": 1, \"b\": 2}";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("b".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return null;
            }
        };

        FilteringParserDelegate delegate = new FilteringParserDelegate(p, filter, false, true);

        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.nextToken());
        assertEquals(2, delegate.getIntValue());
        assertNull(delegate.nextToken());
        delegate.close();
    }

    // Tests array filtering with nested objects
    @Test
    public void testNextToken_filterArrayElements_filtersUnmatchedItems() throws IOException {
        String json = "[{\"id\": 1}, {\"id\": 2}]";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeElement(int index) {
                if (index == 1) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return null;
            }
        };

        FilteringParserDelegate delegate = new FilteringParserDelegate(p, filter, true, true);

        assertEquals(JsonToken.START_ARRAY, delegate.nextToken());
        assertEquals(JsonToken.START_OBJECT, delegate.nextToken());
        assertEquals(JsonToken.FIELD_NAME, delegate.nextToken());
        assertEquals("id", delegate.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.nextToken());
        assertEquals(2, delegate.getIntValue());
        assertEquals(JsonToken.END_OBJECT, delegate.nextToken());
        assertEquals(JsonToken.END_ARRAY, delegate.nextToken());
        assertNull(delegate.nextToken());
        delegate.close();
    }

    // Tests clearCurrentToken and getLastClearedToken
    @Test
    public void testClearCurrentToken_clearsTokenAndStoresLastCleared() throws IOException {
        JsonParser p = JSON_F.createParser("{\"x\": 1}");
        FilteringParserDelegate delegate = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        assertNull(delegate.getLastClearedToken());
        assertEquals(JsonToken.START_OBJECT, delegate.nextToken());
        assertEquals(JsonToken.START_OBJECT, delegate.currentToken());
        assertEquals(JsonToken.START_OBJECT, delegate.getCurrentToken());

        delegate.clearCurrentToken();
        assertNull(delegate.getCurrentToken());
        assertEquals(JsonToken.START_OBJECT, delegate.getLastClearedToken());

        // Clearing again when currToken is null should not overwrite lastClearedToken
        delegate.clearCurrentToken();
        assertEquals(JsonToken.START_OBJECT, delegate.getLastClearedToken());

        delegate.close();
    }

    // Tests overrideCurrentName throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testOverrideCurrentName_alwaysThrowsUnsupportedOperationException() throws IOException {
        JsonParser p = JSON_F.createParser("{\"x\": 1}");
        FilteringParserDelegate delegate = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);
        delegate.nextToken();
        delegate.overrideCurrentName("newName");
    }

    // Tests nextValue navigation skipping field names
    @Test
    public void testNextValue_skipsFieldNameTokens() throws IOException {
        String json = "{\"a\": 1, \"b\": 2}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate delegate = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_OBJECT, delegate.nextValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.nextValue());
        assertEquals(1, delegate.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.nextValue());
        assertEquals(2, delegate.getIntValue());
        assertEquals(JsonToken.END_OBJECT, delegate.nextValue());
        assertNull(delegate.nextValue());
        delegate.close();
    }

    // Tests skipChildren on structured token
    @Test
    public void testSkipChildren_skipsInnerElements() throws IOException {
        String json = "{\"obj\": {\"nested\": 1}, \"after\": 2}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate delegate = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_OBJECT, delegate.nextToken());
        assertEquals(JsonToken.FIELD_NAME, delegate.nextToken());
        assertEquals(JsonToken.START_OBJECT, delegate.nextToken());

        delegate.skipChildren();
        assertEquals(JsonToken.END_OBJECT, delegate.getCurrentToken());

        assertEquals(JsonToken.FIELD_NAME, delegate.nextToken());
        assertEquals("after", delegate.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.nextToken());
        assertEquals(2, delegate.getIntValue());
        assertEquals(JsonToken.END_OBJECT, delegate.nextToken());
        assertNull(delegate.nextToken());
        delegate.close();
    }

    // Tests skipChildren on scalar token does not advance parser
    @Test
    public void testSkipChildren_onScalarToken_doesNothing() throws IOException {
        String json = "{\"a\": 1}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate delegate = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_OBJECT, delegate.nextToken());
        assertEquals(JsonToken.FIELD_NAME, delegate.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.nextToken());

        delegate.skipChildren();
        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.getCurrentToken());
        assertEquals(JsonToken.END_OBJECT, delegate.nextToken());
        assertNull(delegate.nextToken());
        delegate.close();
    }

    // Tests context accessors: getParsingContext, getCurrentLocation, getTokenLocation
    @Test
    public void testContextAndLocationAccessors() throws IOException {
        String json = "{\"k\": \"v\"}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate delegate = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        assertNotNull(delegate.getParsingContext());
        assertNotNull(delegate.getCurrentLocation());

        assertEquals(JsonToken.START_OBJECT, delegate.nextToken());
        assertNotNull(delegate.getTokenLocation());
        assertNotNull(delegate.getParsingContext());
        assertNull(delegate.getCurrentName());

        assertEquals(JsonToken.FIELD_NAME, delegate.nextToken());
        assertEquals("k", delegate.getCurrentName());

        assertEquals(JsonToken.VALUE_STRING, delegate.nextToken());
        assertEquals("k", delegate.getCurrentName());

        assertEquals(JsonToken.END_OBJECT, delegate.nextToken());
        delegate.close();
    }

    // Tests no match token filter returns null nextToken
    @Test
    public void testNextToken_noMatchFilter_returnsNull() throws IOException {
        String json = "{\"a\": 1, \"b\": [2, 3]}";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return null;
            }
        };

        FilteringParserDelegate delegate = new FilteringParserDelegate(p, filter, true, true);
        assertNull(delegate.nextToken());
        delegate.close();
    }

    // Tests empty JSON document
    @Test
    public void testNextToken_emptyJson_returnsNull() throws IOException {
        JsonParser p = JSON_F.createParser("");
        FilteringParserDelegate delegate = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);
        assertNull(delegate.nextToken());
        delegate.close();
    }

    // Tests delegate binary, embedded object, and fallback methods
    @Test
    public void testDelegatedValueMethods() throws IOException {
        String json = "null";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate delegate = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.VALUE_NULL, delegate.nextToken());
        assertNull(delegate.getEmbeddedObject());
        assertNull(delegate.getBinaryValue(Base64Variants.MIME));
        delegate.close();
    }

    // Tests location aliases and text writer method
    @Test
    public void testGetTextWriterAndLocationAliases() throws IOException {
        String json = "{\"msg\": \"hello\"}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate delegate = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        assertNotNull(delegate.currentLocation());
        assertEquals(JsonToken.START_OBJECT, delegate.nextToken());
        assertNotNull(delegate.currentTokenLocation());

        assertEquals(JsonToken.FIELD_NAME, delegate.nextToken());
        assertEquals("msg", delegate.currentName());

        assertEquals(JsonToken.VALUE_STRING, delegate.nextToken());
        StringWriter sw = new StringWriter();
        int written = delegate.getText(sw);
        assertEquals(5, written);
        assertEquals("hello", sw.toString());

        assertEquals(JsonToken.END_OBJECT, delegate.nextToken());
        assertNull(delegate.nextToken());
        delegate.close();
    }

    // Tests binary read through OutputStream and default binary variant
    @Test
    public void testReadBinaryValueAndDefaultBinary() throws IOException {
        String json = "\"SGVsbG8gV29ybGQ=\""; // Base64 of "Hello World"
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate delegate = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.VALUE_STRING, delegate.nextToken());
        byte[] bytes = delegate.getBinaryValue();
        assertArrayEquals("Hello World".getBytes("UTF-8"), bytes);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int count = delegate.readBinaryValue(Base64Variants.MIME, out);
        assertEquals(11, count);
        assertArrayEquals("Hello World".getBytes("UTF-8"), out.toByteArray());

        ByteArrayOutputStream outDef = new ByteArrayOutputStream();
        int countDef = delegate.readBinaryValue(outDef);
        assertEquals(11, countDef);
        assertArrayEquals("Hello World".getBytes("UTF-8"), outDef.toByteArray());

        assertNull(delegate.nextToken());
        delegate.close();
    }

    // Tests allowMultipleMatches = false when filtering in objects
    @Test
    public void testSingleMatchInObject_terminatesAfterFirstMatch() throws IOException {
        String json = "{\"a\": 1, \"b\": 2, \"c\": 3}";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return TokenFilter.INCLUDE_ALL;
            }
        };

        FilteringParserDelegate delegate = new FilteringParserDelegate(p, filter, false, false);

        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.nextToken());
        assertEquals(1, delegate.getIntValue());
        assertEquals(1, delegate.getMatchCount());
        assertNull(delegate.nextToken());
        delegate.close();
    }

    // Tests array filtering with allowMultipleMatches = false
    @Test
    public void testSingleMatchInArray_terminatesAfterFirstMatch() throws IOException {
        String json = "[10, 20, 30]";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate delegate = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, false, false);

        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.nextToken());
        assertEquals(10, delegate.getIntValue());
        assertEquals(1, delegate.getMatchCount());
        assertNull(delegate.nextToken());
        delegate.close();
    }

    // Tests nested structure filtering with includePath = false
    @Test
    public void testNestedStructureFilter_includePathFalse() throws IOException {
        String json = "{\"wrapper\": {\"inner\": 42}}";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("wrapper".equals(name)) {
                    return this;
                }
                if ("inner".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return null;
            }
        };

        FilteringParserDelegate delegate = new FilteringParserDelegate(p, filter, false, true);

        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.nextToken());
        assertEquals(42, delegate.getIntValue());
        assertNull(delegate.nextToken());
        delegate.close();
    }

    // Tests skipChildren inside an array when filtering
    @Test
    public void testSkipChildren_insideArray() throws IOException {
        String json = "{\"arr\": [1, 2, 3], \"tail\": 99}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate delegate = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_OBJECT, delegate.nextToken());
        assertEquals(JsonToken.FIELD_NAME, delegate.nextToken());
        assertEquals(JsonToken.START_ARRAY, delegate.nextToken());

        delegate.skipChildren();
        assertEquals(JsonToken.END_ARRAY, delegate.getCurrentToken());

        assertEquals(JsonToken.FIELD_NAME, delegate.nextToken());
        assertEquals("tail", delegate.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.nextToken());
        assertEquals(99, delegate.getIntValue());

        assertEquals(JsonToken.END_OBJECT, delegate.nextToken());
        assertNull(delegate.nextToken());
        delegate.close();
    }

    // Tests filter returning boolean includeValue
    @Test
    public void testIncludeValue_filterCondition() throws IOException {
        String json = "{\"a\": 1, \"b\": 2, \"c\": 3}";
        JsonParser p = JSON_F.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return new TokenFilter() {
                    @Override
                    public boolean includeValue(JsonParser p) throws IOException {
                        return p.getIntValue() == 2;
                    }
                };
            }
        };

        FilteringParserDelegate delegate = new FilteringParserDelegate(p, filter, true, true);

        assertEquals(JsonToken.START_OBJECT, delegate.nextToken());
        assertEquals(JsonToken.FIELD_NAME, delegate.nextToken());
        assertEquals("b", delegate.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, delegate.nextToken());
        assertEquals(2, delegate.getIntValue());
        assertEquals(JsonToken.END_OBJECT, delegate.nextToken());
        assertNull(delegate.nextToken());
        delegate.close();
    }

    // Tests filtering out false boolean token
    @Test
    public void testFalseBooleanValue() throws IOException {
        String json = "{\"flag\": false}";
        JsonParser p = JSON_F.createParser(json);
        FilteringParserDelegate delegate = new FilteringParserDelegate(p, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_OBJECT, delegate.nextToken());
        assertEquals(JsonToken.FIELD_NAME, delegate.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, delegate.nextToken());
        assertFalse(delegate.getBooleanValue());
        assertFalse(delegate.getValueAsBoolean());
        assertFalse(delegate.getValueAsBoolean(true));
        assertEquals(JsonToken.END_OBJECT, delegate.nextToken());
        assertNull(delegate.nextToken());
        delegate.close();
    }
}