package com.fasterxml.jackson.core.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.core.io.SerializedString;

import static org.junit.Assert.*;

public class FilteringParserDelegateTest {

    private JsonFactory jsonFactory;

    @Before
    public void setUp() {
        jsonFactory = new JsonFactory();
    }

    // Helper method to create parser
    private FilteringParserDelegate createParser(String json, TokenFilter filter, boolean includePath, boolean allowMultipleMatches) throws IOException {
        JsonParser p = jsonFactory.createParser(json);
        return new FilteringParserDelegate(p, filter, includePath, allowMultipleMatches);
    }

    // Tests token accessors and initial state
    @Test
    public void testInitialState_emptyOrUnread_hasNoToken() throws IOException {
        FilteringParserDelegate parser = createParser("{\"a\":1}", TokenFilter.INCLUDE_ALL, true, true);
        assertNull(parser.getCurrentToken());
        assertNull(parser.currentToken());
        assertEquals(JsonTokenId.ID_NO_TOKEN, parser.getCurrentTokenId());
        assertEquals(JsonTokenId.ID_NO_TOKEN, parser.currentTokenId());
        assertFalse(parser.hasCurrentToken());
        assertTrue(parser.hasTokenId(JsonTokenId.ID_NO_TOKEN));
        assertFalse(parser.hasToken(JsonToken.START_OBJECT));
        assertFalse(parser.isExpectedStartArrayToken());
        assertFalse(parser.isExpectedStartObjectToken());
        assertEquals(0, parser.getMatchCount());
        assertSame(TokenFilter.INCLUDE_ALL, parser.getFilter());
        parser.close();
    }

    // Tests include all tokens with path and multiple matches
    @Test
    public void testNextToken_includeAll_traversesFullDocument() throws IOException {
        String json = "{\"name\":\"test\",\"count\":42,\"list\":[true,false]}";
        FilteringParserDelegate parser = createParser(json, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertTrue(parser.hasCurrentToken());
        assertTrue(parser.isExpectedStartObjectToken());
        assertEquals(JsonTokenId.ID_START_OBJECT, parser.getCurrentTokenId());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("count", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("list", parser.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertTrue(parser.isExpectedStartArrayToken());

        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertTrue(parser.getBooleanValue());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertFalse(parser.getBooleanValue());

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests filtering property by name with path included
    @Test
    public void testNextToken_filterProperty_withPathIncluded() throws IOException {
        String json = "{\"a\":1,\"b\":{\"c\":2,\"d\":3}}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("c".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return this;
            }
        };

        FilteringParserDelegate parser = createParser(json, filter, true, true);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("b", parser.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("c", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests filtering property by name without path included
    @Test
    public void testNextToken_filterProperty_withoutPathIncluded() throws IOException {
        String json = "{\"a\":1,\"b\":2,\"c\":3}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return "b".equals(name) ? TokenFilter.INCLUDE_ALL : null;
            }
        };

        FilteringParserDelegate parser = createParser(json, filter, false, true);

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("b", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests single match behavior when allowMultipleMatches is false
    @Test
    public void testNextToken_singleMatch_stopsAfterFirstMatch() throws IOException {
        String json = "[10, 20, 30]";
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter filterStartArray() {
                return this;
            }
            @Override
            public boolean includeValue(JsonParser p) throws IOException {
                return p.getIntValue() == 20;
            }
        };

        FilteringParserDelegate parser = createParser(json, filter, false, false);

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(20, parser.getIntValue());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests scalar root filtering with INCLUDE_ALL and single match
    @Test
    public void testNextToken_singleMatchScalarRoot_stopsImmediately() throws IOException {
        String json = "\"hello\"";
        FilteringParserDelegate parser = createParser(json, TokenFilter.INCLUDE_ALL, false, false);

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getText());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests filtering out whole array elements
    @Test
    public void testNextToken_filterArrayElements_skipsNonMatching() throws IOException {
        String json = "[[1,2],[3,4],[5,6]]";
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter filterStartArray() {
                return new TokenFilter() {
                    @Override
                    public TokenFilter filterStartArray() {
                        return this;
                    }
                    @Override
                    public boolean includeValue(JsonParser p) throws IOException {
                        return p.getIntValue() == 4;
                    }
                };
            }
        };

        FilteringParserDelegate parser = createParser(json, filter, true, true);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(4, parser.getIntValue());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests nextValue() advancement over FIELD_NAME
    @Test
    public void testNextValue_skipsFieldNameTokens() throws IOException {
        String json = "{\"a\":100,\"b\":200}";
        FilteringParserDelegate parser = createParser(json, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_OBJECT, parser.nextValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextValue());
        assertEquals(100, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextValue());
        assertEquals(200, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextValue());
        assertNull(parser.nextValue());
        parser.close();
    }

    // Tests clearCurrentToken and getLastClearedToken
    @Test
    public void testClearCurrentToken_clearsStateAndRetainsLast() throws IOException {
        String json = "{\"x\":1}";
        FilteringParserDelegate parser = createParser(json, TokenFilter.INCLUDE_ALL, true, true);

        assertNull(parser.getLastClearedToken());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        parser.clearCurrentToken();
        assertNull(parser.getCurrentToken());
        assertEquals(JsonToken.START_OBJECT, parser.getLastClearedToken());

        // Calling clear when null does nothing
        parser.clearCurrentToken();
        assertEquals(JsonToken.START_OBJECT, parser.getLastClearedToken());
        parser.close();
    }

    // Tests overrideCurrentName throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testOverrideCurrentName_throwsUnsupportedOperationException() throws IOException {
        String json = "{\"key\":\"val\"}";
        FilteringParserDelegate parser = createParser(json, TokenFilter.INCLUDE_ALL, true, true);
        parser.nextToken();
        parser.overrideCurrentName("otherKey");
        parser.close();
    }

    // Tests skipChildren on START_OBJECT and START_ARRAY
    @Test
    public void testSkipChildren_skipsNestedStructures() throws IOException {
        String json = "{\"obj\":{\"nested\":1},\"arr\":[10,20],\"after\":\"done\"}";
        FilteringParserDelegate parser = createParser(json, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("obj", parser.getCurrentName());

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        parser.skipChildren();
        assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("arr", parser.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        parser.skipChildren();
        assertEquals(JsonToken.END_ARRAY, parser.getCurrentToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("after", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("done", parser.getText());

        // Calling skipChildren on non-start token returns this without advancing
        parser.skipChildren();
        assertEquals(JsonToken.VALUE_STRING, parser.getCurrentToken());
        parser.close();
    }

    // Tests parsing context and current name at different token states
    @Test
    public void testParsingContext_andCurrentName() throws IOException {
        String json = "{\"parent\":{\"child\":123}}";
        FilteringParserDelegate parser = createParser(json, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        JsonStreamContext ctxt = parser.getParsingContext();
        assertNotNull(ctxt);
        assertNull(parser.getCurrentName());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("parent", parser.getCurrentName());

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals("parent", parser.getCurrentName());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("child", parser.getCurrentName());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("child", parser.getCurrentName());

        parser.close();
    }

    // Tests delegate accessors for numeric, text, and binary values
    @Test
    public void testDelegatedValueAccessors() throws IOException {
        String json = "{\"int\":1,\"long\":1234567890123,\"double\":3.14,\"float\":1.5,\"str\":\"text\",\"bool\":true,\"b64\":\"AQID\"}";
        FilteringParserDelegate parser = createParser(json, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertNotNull(parser.getCurrentLocation());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(1, parser.getValueAsInt());
        assertEquals(1, parser.getValueAsInt(99));
        assertEquals(1L, parser.getLongValue());
        assertEquals(1L, parser.getValueAsLong());
        assertEquals(1L, parser.getValueAsLong(99L));
        assertEquals((byte) 1, parser.getByteValue());
        assertEquals((short) 1, parser.getShortValue());
        assertEquals(BigInteger.valueOf(1), parser.getBigIntegerValue());
        assertEquals(BigDecimal.valueOf(1), parser.getDecimalValue());
        assertEquals(JsonParser.NumberType.INT, parser.getNumberType());
        assertEquals(1, parser.getNumberValue().intValue());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1234567890123L, parser.getLongValue());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        assertEquals(3.14, parser.getValueAsDouble(), 0.0001);
        assertEquals(3.14, parser.getValueAsDouble(0.0), 0.0001);
        assertEquals(3.14f, parser.getFloatValue(), 0.0001f);

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(1.5f, parser.getFloatValue(), 0.0001f);

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("text", parser.getText());
        assertEquals("text", parser.getValueAsString());
        assertEquals("text", parser.getValueAsString("default"));
        assertTrue(parser.hasTextCharacters());
        assertNotNull(parser.getTextCharacters());
        assertEquals(4, parser.getTextLength());
        assertEquals(0, parser.getTextOffset());
        assertNull(parser.getEmbeddedObject());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertTrue(parser.getBooleanValue());
        assertTrue(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(false));

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        byte[] binary = parser.getBinaryValue(Base64Variants.MIME);
        assertArrayEquals(new byte[]{1, 2, 3}, binary);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bytesRead = parser.readBinaryValue(Base64Variants.MIME, baos);
        assertEquals(3, bytesRead);
        assertArrayEquals(new byte[]{1, 2, 3}, baos.toByteArray());

        assertNotNull(parser.getTokenLocation());
        parser.close();
    }

    // Tests empty json object and array matching with INCLUDE_ALL
    @Test
    public void testEmptyObjectAndArray_withIncludeAll() throws IOException {
        String json = "{\"emptyObj\":{},\"emptyArr\":[]}";
        FilteringParserDelegate parser = createParser(json, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("emptyObj", parser.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("emptyArr", parser.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests filtering out everything returns null
    @Test
    public void testFilterAll_returnsNull() throws IOException {
        String json = "{\"a\":1,\"b\":[2,3]}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return null;
            }
        };
        FilteringParserDelegate parser = createParser(json, filter, true, true);
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests empty input
    @Test
    public void testEmptyInput_returnsNull() throws IOException {
        String json = "";
        FilteringParserDelegate parser = createParser(json, TokenFilter.INCLUDE_ALL, true, true);
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests nextXxx helper methods: nextFieldName, nextBooleanValue, nextIntValue, nextLongValue, nextTextValue
    @Test
    public void testNextAdvancementHelpers() throws IOException {
        String json = "{\"num\":42,\"big\":9876543210,\"flag\":true,\"str\":\"hello\"}";
        FilteringParserDelegate parser = createParser(json, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());

        assertEquals("num", parser.nextFieldName());
        assertEquals(42, parser.nextIntValue(-1));

        assertTrue(parser.nextFieldName(new SerializedString("big")));
        assertEquals(9876543210L, parser.nextLongValue(-1L));

        assertEquals("flag", parser.nextFieldName());
        assertEquals(Boolean.TRUE, parser.nextBooleanValue());

        assertEquals("str", parser.nextFieldName());
        assertEquals("hello", parser.nextTextValue());

        assertNull(parser.nextFieldName());
        assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests includeElement filtering in array with element index
    @Test
    public void testNextToken_filterIncludeElement_byIndex() throws IOException {
        String json = "[\"first\", \"second\", \"third\"]";
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeElement(int index) {
                return index == 1 ? TokenFilter.INCLUDE_ALL : null;
            }
        };

        FilteringParserDelegate parser = createParser(json, filter, true, true);

        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("second", parser.getText());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests includeRootValue filtering with root index
    @Test
    public void testNextToken_filterIncludeRootValue_byIndex() throws IOException {
        String json = "100 200";
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeRootValue(int index) {
                return index == 1 ? TokenFilter.INCLUDE_ALL : null;
            }
        };

        FilteringParserDelegate parser = createParser(json, filter, true, true);

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(200, parser.getIntValue());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests null value handling and filterFinishArray / filterFinishObject
    @Test
    public void testNextToken_filterWithNullValueAndFinishCallbacks() throws IOException {
        String json = "{\"data\":null,\"nested\":{}}";
        final boolean[] finishObjectCalled = new boolean[]{false};

        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                return this;
            }

            @Override
            public boolean includeNull() {
                return true;
            }

            @Override
            public void filterFinishObject() {
                finishObjectCalled[0] = true;
            }
        };

        FilteringParserDelegate parser = createParser(json, filter, true, true);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("data", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertTrue(finishObjectCalled[0]);
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests deep nesting path buffering and output when includePath is true
    @Test
    public void testNextToken_deepNesting_bufferedPathOutput() throws IOException {
        String json = "{\"lvl1\":{\"lvl2\":{\"lvl3\":[{\"target\":\"found\"}]}}}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("target".equals(name)) {
                    return TokenFilter.INCLUDE_ALL;
                }
                return this;
            }

            @Override
            public TokenFilter includeElement(int index) {
                return this;
            }
        };

        FilteringParserDelegate parser = createParser(json, filter, true, true);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("lvl1", parser.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("lvl2", parser.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("lvl3", parser.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("target", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("found", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests getText and characters on FIELD_NAME token
    @Test
    public void testGetText_onFieldNameToken() throws IOException {
        String json = "{\"targetField\":123}";
        FilteringParserDelegate parser = createParser(json, TokenFilter.INCLUDE_ALL, true, true);

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("targetField", parser.getText());
        assertNotNull(parser.getTextCharacters());
        assertEquals("targetField".length(), parser.getTextLength());
        assertEquals(0, parser.getTextOffset());
        parser.close();
    }
}