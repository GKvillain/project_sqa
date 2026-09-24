package com.fasterxml.jackson.core.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.core.*;

public class FilteringParserDelegateTest {

    private JsonFactory jsonFactory;

    @Before
    public void setUp() {
        jsonFactory = new JsonFactory();
    }

    // Helper to count remaining tokens
    private int countTokens(JsonParser p) throws IOException {
        int count = 0;
        while (p.nextToken() != null) {
            count++;
        }
        return count;
    }

    // Tests reading with TokenFilter.INCLUDE_ALL
    @Test
    public void testNextToken_includeAll_returnsAllTokens() throws IOException {
        String json = "{\"a\": 1, \"b\": [true, false]}";
        JsonParser p = jsonFactory.createParser(json);
        FilteringParserDelegate filterParser = new FilteringParserDelegate(
                p, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_OBJECT, filterParser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, filterParser.nextToken());
        assertEquals("a", filterParser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, filterParser.nextToken());
        assertEquals(1, filterParser.getIntValue());
        assertEquals(JsonToken.FIELD_NAME, filterParser.nextToken());
        assertEquals("b", filterParser.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, filterParser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, filterParser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, filterParser.nextToken());
        assertEquals(JsonToken.END_ARRAY, filterParser.nextToken());
        assertEquals(JsonToken.END_OBJECT, filterParser.nextToken());
        assertNull(filterParser.nextToken());
        filterParser.close();
    }

    // Tests filtering a specific property including path
    @Test
    public void testNextToken_filterPropertyWithPath_returnsPathAndValue() throws IOException {
        String json = "{\"a\": 123, \"b\": 456}";
        JsonParser p = jsonFactory.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return "b".equals(name) ? TokenFilter.INCLUDE_ALL : null;
            }
        };
        FilteringParserDelegate filterParser = new FilteringParserDelegate(
                p, filter, true, true);

        assertEquals(JsonToken.START_OBJECT, filterParser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, filterParser.nextToken());
        assertEquals("b", filterParser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, filterParser.nextToken());
        assertEquals(456, filterParser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, filterParser.nextToken());
        assertNull(filterParser.nextToken());
        filterParser.close();
    }

    // Tests filtering a specific property without path
    @Test
    public void testNextToken_filterPropertyWithoutPath_returnsOnlyValue() throws IOException {
        String json = "{\"a\": 123, \"b\": 456}";
        JsonParser p = jsonFactory.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return "b".equals(name) ? TokenFilter.INCLUDE_ALL : null;
            }
        };
        FilteringParserDelegate filterParser = new FilteringParserDelegate(
                p, filter, false, true);

        assertEquals(JsonToken.VALUE_NUMBER_INT, filterParser.nextToken());
        assertEquals(456, filterParser.getIntValue());
        assertNull(filterParser.nextToken());
        filterParser.close();
    }

    // Tests allowMultipleMatches = false stops after the first match
    @Test
    public void testNextToken_allowMultipleMatchesFalse_stopsAfterFirstMatch() throws IOException {
        String json = "[1, 2, 3]";
        JsonParser p = jsonFactory.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeValue(JsonParser p) throws IOException {
                return TokenFilter.INCLUDE_ALL;
            }
        };
        FilteringParserDelegate filterParser = new FilteringParserDelegate(
                p, filter, false, false);

        JsonToken t1 = filterParser.nextToken();
        assertNotNull(t1);
        assertEquals(1, filterParser.getIntValue());
        
        JsonToken t2 = filterParser.nextToken();
        assertNull(t2);
        filterParser.close();
    }

    // Tests filtering array elements with includePath = true
    @Test
    public void testNextToken_filterArrayElementWithPath_preservesEnclosingArray() throws IOException {
        String json = "[10, 20, 30]";
        JsonParser p = jsonFactory.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeElement(int index) {
                return (index == 1) ? TokenFilter.INCLUDE_ALL : null;
            }
        };
        FilteringParserDelegate filterParser = new FilteringParserDelegate(
                p, filter, true, true);

        assertEquals(JsonToken.START_ARRAY, filterParser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, filterParser.nextToken());
        assertEquals(20, filterParser.getIntValue());
        assertEquals(JsonToken.END_ARRAY, filterParser.nextToken());
        assertNull(filterParser.nextToken());
        filterParser.close();
    }

    // Tests nextValue() behavior skipping FIELD_NAME
    @Test
    public void testNextValue_objectContent_returnsValueTokens() throws IOException {
        String json = "{\"x\": 10, \"y\": 20}";
        JsonParser p = jsonFactory.createParser(json);
        FilteringParserDelegate filterParser = new FilteringParserDelegate(
                p, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_OBJECT, filterParser.nextValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, filterParser.nextValue());
        assertEquals(10, filterParser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, filterParser.nextValue());
        assertEquals(20, filterParser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, filterParser.nextValue());
        assertNull(filterParser.nextValue());
        filterParser.close();
    }

    // Tests skipChildren() on an object
    @Test
    public void testSkipChildren_startObject_skipsChildren() throws IOException {
        String json = "{\"obj\": {\"nested\": 1}, \"after\": 2}";
        JsonParser p = jsonFactory.createParser(json);
        FilteringParserDelegate filterParser = new FilteringParserDelegate(
                p, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_OBJECT, filterParser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, filterParser.nextToken());
        assertEquals(JsonToken.START_OBJECT, filterParser.nextToken());
        assertTrue(filterParser.isExpectedStartObjectToken());

        filterParser.skipChildren();
        assertEquals(JsonToken.END_OBJECT, filterParser.getCurrentToken());

        assertEquals(JsonToken.FIELD_NAME, filterParser.nextToken());
        assertEquals("after", filterParser.getCurrentName());
        filterParser.close();
    }

    // Tests skipChildren() when current token is not a container
    @Test
    public void testSkipChildren_scalarToken_noOp() throws IOException {
        String json = "123";
        JsonParser p = jsonFactory.createParser(json);
        FilteringParserDelegate filterParser = new FilteringParserDelegate(
                p, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.VALUE_NUMBER_INT, filterParser.nextToken());
        JsonParser same = filterParser.skipChildren();
        assertSame(filterParser, same);
        assertEquals(JsonToken.VALUE_NUMBER_INT, filterParser.getCurrentToken());
        filterParser.close();
    }

    // Tests clearCurrentToken and getLastClearedToken
    @Test
    public void testClearCurrentToken_clearsStateAndTracksLastToken() throws IOException {
        String json = "[1]";
        JsonParser p = jsonFactory.createParser(json);
        FilteringParserDelegate filterParser = new FilteringParserDelegate(
                p, TokenFilter.INCLUDE_ALL, true, true);

        assertNull(filterParser.getCurrentToken());
        assertNull(filterParser.getLastClearedToken());
        assertFalse(filterParser.hasCurrentToken());

        assertEquals(JsonToken.START_ARRAY, filterParser.nextToken());
        assertTrue(filterParser.hasCurrentToken());
        assertTrue(filterParser.isExpectedStartArrayToken());
        assertTrue(filterParser.hasToken(JsonToken.START_ARRAY));
        assertTrue(filterParser.hasTokenId(JsonTokenId.ID_START_ARRAY));
        assertEquals(JsonTokenId.ID_START_ARRAY, filterParser.getCurrentTokenId());

        filterParser.clearCurrentToken();
        assertNull(filterParser.getCurrentToken());
        assertFalse(filterParser.hasCurrentToken());
        assertEquals(JsonToken.START_ARRAY, filterParser.getLastClearedToken());
        assertEquals(JsonTokenId.ID_NO_TOKEN, filterParser.getCurrentTokenId());
        assertTrue(filterParser.hasTokenId(JsonTokenId.ID_NO_TOKEN));
        filterParser.close();
    }

    // Tests exception on overrideCurrentName
    @Test(expected = UnsupportedOperationException.class)
    public void testOverrideCurrentName_throwsUnsupportedOperationException() throws IOException {
        String json = "{\"a\": 1}";
        JsonParser p = jsonFactory.createParser(json);
        FilteringParserDelegate filterParser = new FilteringParserDelegate(
                p, TokenFilter.INCLUDE_ALL, true, true);
        try {
            filterParser.nextToken();
            filterParser.overrideCurrentName("other");
        } finally {
            filterParser.close();
        }
    }

    // Tests numeric and scalar accessor delegations
    @Test
    public void testScalarAccessors_variousTypes_delegatesCorrectly() throws IOException {
        String json = "{\"int\": 42, \"long\": 9999999999, \"double\": 3.14, \"bigInt\": 10000000000000000000, \"dec\": 12.34, \"bool\": true, \"str\": \"test\"}";
        JsonParser p = jsonFactory.createParser(json);
        FilteringParserDelegate filterParser = new FilteringParserDelegate(
                p, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_OBJECT, filterParser.nextToken());

        // int
        assertEquals(JsonToken.FIELD_NAME, filterParser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, filterParser.nextToken());
        assertEquals(42, filterParser.getIntValue());
        assertEquals(42, filterParser.getValueAsInt());
        assertEquals(42, filterParser.getValueAsInt(0));
        assertEquals((byte) 42, filterParser.getByteValue());
        assertEquals((short) 42, filterParser.getShortValue());
        assertEquals(JsonParser.NumberType.INT, filterParser.getNumberType());
        assertEquals(42, filterParser.getNumberValue().intValue());

        // long
        assertEquals(JsonToken.FIELD_NAME, filterParser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, filterParser.nextToken());
        assertEquals(9999999999L, filterParser.getLongValue());
        assertEquals(9999999999L, filterParser.getValueAsLong());
        assertEquals(9999999999L, filterParser.getValueAsLong(0L));

        // double
        assertEquals(JsonToken.FIELD_NAME, filterParser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, filterParser.nextToken());
        assertEquals(3.14, filterParser.getDoubleValue(), 0.001);
        assertEquals(3.14f, filterParser.getFloatValue(), 0.001f);
        assertEquals(3.14, filterParser.getValueAsDouble(), 0.001);
        assertEquals(3.14, filterParser.getValueAsDouble(0.0), 0.001);

        // bigInt
        assertEquals(JsonToken.FIELD_NAME, filterParser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, filterParser.nextToken());
        assertEquals(new BigInteger("10000000000000000000"), filterParser.getBigIntegerValue());

        // dec
        assertEquals(JsonToken.FIELD_NAME, filterParser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, filterParser.nextToken());
        assertEquals(new BigDecimal("12.34"), filterParser.getDecimalValue());

        // bool
        assertEquals(JsonToken.FIELD_NAME, filterParser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, filterParser.nextToken());
        assertTrue(filterParser.getBooleanValue());
        assertTrue(filterParser.getValueAsBoolean());
        assertTrue(filterParser.getValueAsBoolean(false));

        // str
        assertEquals(JsonToken.FIELD_NAME, filterParser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, filterParser.nextToken());
        assertEquals("test", filterParser.getText());
        assertEquals("test", filterParser.getValueAsString());
        assertEquals("test", filterParser.getValueAsString("def"));
        assertTrue(filterParser.hasTextCharacters());
        assertNotNull(filterParser.getTextCharacters());
        assertEquals(4, filterParser.getTextLength());
        assertEquals(0, filterParser.getTextOffset());

        filterParser.close();
    }

    // Tests binary value delegation and location accessors
    @Test
    public void testBinaryAndLocationAccessors_delegatesCorrectly() throws IOException {
        String json = "{\"bin\": \"AQID\"}";
        JsonParser p = jsonFactory.createParser(json);
        FilteringParserDelegate filterParser = new FilteringParserDelegate(
                p, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_OBJECT, filterParser.nextToken());
        assertNotNull(filterParser.getCurrentLocation());
        assertNotNull(filterParser.getTokenLocation());
        assertNotNull(filterParser.getParsingContext());
        assertEquals(TokenFilter.INCLUDE_ALL, filterParser.getFilter());
        assertEquals(0, filterParser.getMatchCount());

        assertEquals(JsonToken.FIELD_NAME, filterParser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, filterParser.nextToken());

        byte[] binary = filterParser.getBinaryValue(Base64Variants.MIME);
        assertArrayEquals(new byte[]{1, 2, 3}, binary);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read = filterParser.readBinaryValue(Base64Variants.MIME, out);
        assertEquals(3, read);
        assertArrayEquals(new byte[]{1, 2, 3}, out.toByteArray());
        assertNull(filterParser.getEmbeddedObject());

        filterParser.close();
    }

    // Tests empty result when all content is filtered out
    @Test
    public void testNextToken_allExcluded_returnsNull() throws IOException {
        String json = "{\"a\": 1, \"b\": [2, 3]}";
        JsonParser p = jsonFactory.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return null;
            }
        };
        FilteringParserDelegate filterParser = new FilteringParserDelegate(
                p, filter, true, true);

        assertNull(filterParser.nextToken());
        assertEquals(0, countTokens(filterParser));
        filterParser.close();
    }

    // Tests root scalar inclusion and null handling
    @Test
    public void testNextToken_scalarRootIncluded() throws IOException {
        String json = "\"hello\"";
        JsonParser p = jsonFactory.createParser(json);
        FilteringParserDelegate filterParser = new FilteringParserDelegate(
                p, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.VALUE_STRING, filterParser.nextToken());
        assertEquals("hello", filterParser.getText());
        assertNull(filterParser.nextToken());
        filterParser.close();
    }

    // Tests root scalar exclusion
    @Test
    public void testNextToken_scalarRootExcluded() throws IOException {
        String json = "\"hello\"";
        JsonParser p = jsonFactory.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeRootValue(int index) {
                return null;
            }
        };
        FilteringParserDelegate filterParser = new FilteringParserDelegate(
                p, filter, true, true);

        assertNull(filterParser.nextToken());
        filterParser.close();
    }

    // Tests current value getter and setter
    @Test
    public void testCurrentValue_getterAndSetter() throws IOException {
        String json = "{\"a\": 1}";
        JsonParser p = jsonFactory.createParser(json);
        FilteringParserDelegate filterParser = new FilteringParserDelegate(
                p, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_OBJECT, filterParser.nextToken());
        filterParser.setCurrentValue("myContextObject");
        assertEquals("myContextObject", filterParser.getCurrentValue());
        filterParser.close();
    }

    // Tests nested object filtering with includePath=false
    @Test
    public void testNestedObject_filterWithoutPath() throws IOException {
        String json = "{\"outer\": {\"inner\": {\"target\": 99}}}";
        JsonParser p = jsonFactory.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("outer".equals(name) || "inner".equals(name)) {
                    return this;
                }
                return "target".equals(name) ? TokenFilter.INCLUDE_ALL : null;
            }
        };
        FilteringParserDelegate filterParser = new FilteringParserDelegate(
                p, filter, false, true);

        assertEquals(JsonToken.VALUE_NUMBER_INT, filterParser.nextToken());
        assertEquals(99, filterParser.getIntValue());
        assertNull(filterParser.nextToken());
        filterParser.close();
    }

    // Tests nested object filtering with includePath=true
    @Test
    public void testNestedObject_filterWithPath() throws IOException {
        String json = "{\"outer\": {\"inner\": {\"target\": 99, \"extra\": 100}}}";
        JsonParser p = jsonFactory.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("outer".equals(name) || "inner".equals(name)) {
                    return this;
                }
                return "target".equals(name) ? TokenFilter.INCLUDE_ALL : null;
            }
        };
        FilteringParserDelegate filterParser = new FilteringParserDelegate(
                p, filter, true, true);

        assertEquals(JsonToken.START_OBJECT, filterParser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, filterParser.nextToken());
        assertEquals("outer", filterParser.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, filterParser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, filterParser.nextToken());
        assertEquals("inner", filterParser.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, filterParser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, filterParser.nextToken());
        assertEquals("target", filterParser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, filterParser.nextToken());
        assertEquals(99, filterParser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, filterParser.nextToken());
        assertEquals(JsonToken.END_OBJECT, filterParser.nextToken());
        assertEquals(JsonToken.END_OBJECT, filterParser.nextToken());
        assertNull(filterParser.nextToken());
        filterParser.close();
    }

    // Tests filtering array elements with allowMultipleMatches=false
    @Test
    public void testFilterArrayElements_singleMatch_stopsEarly() throws IOException {
        String json = "[10, 20, 30, 40]";
        JsonParser p = jsonFactory.createParser(json);
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeElement(int index) {
                return (index >= 1) ? TokenFilter.INCLUDE_ALL : null;
            }
        };
        FilteringParserDelegate filterParser = new FilteringParserDelegate(
                p, filter, false, false);

        assertEquals(JsonToken.VALUE_NUMBER_INT, filterParser.nextToken());
        assertEquals(20, filterParser.getIntValue());
        assertNull(filterParser.nextToken());
        filterParser.close();
    }

    // Tests filtering null and boolean values
    @Test
    public void testNextToken_nullAndBooleanValues() throws IOException {
        String json = "{\"n\": null, \"f\": false}";
        JsonParser p = jsonFactory.createParser(json);
        FilteringParserDelegate filterParser = new FilteringParserDelegate(
                p, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_OBJECT, filterParser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, filterParser.nextToken());
        assertEquals("n", filterParser.getCurrentName());
        assertEquals(JsonToken.VALUE_NULL, filterParser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, filterParser.nextToken());
        assertEquals("f", filterParser.getCurrentName());
        assertEquals(JsonToken.VALUE_FALSE, filterParser.nextToken());
        assertFalse(filterParser.getBooleanValue());
        assertEquals(JsonToken.END_OBJECT, filterParser.nextToken());
        assertNull(filterParser.nextToken());
        filterParser.close();
    }

    // Tests skipChildren inside an array filter
    @Test
    public void testSkipChildren_insideFilteredArray() throws IOException {
        String json = "[{\"a\": 1}, {\"b\": 2}]";
        JsonParser p = jsonFactory.createParser(json);
        FilteringParserDelegate filterParser = new FilteringParserDelegate(
                p, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_ARRAY, filterParser.nextToken());
        assertEquals(JsonToken.START_OBJECT, filterParser.nextToken());
        filterParser.skipChildren();
        assertEquals(JsonToken.END_OBJECT, filterParser.getCurrentToken());
        assertEquals(JsonToken.START_OBJECT, filterParser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, filterParser.nextToken());
        assertEquals("b", filterParser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, filterParser.nextToken());
        assertEquals(2, filterParser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, filterParser.nextToken());
        assertEquals(JsonToken.END_ARRAY, filterParser.nextToken());
        assertNull(filterParser.nextToken());
        filterParser.close();
    }
}