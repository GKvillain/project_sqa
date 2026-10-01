package com.fasterxml.jackson.core.filter;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.filter.TokenFilter;

/**
 * JUnit 4 test class for FilteringParserDelegate.
 * Tests focus on nextToken() and related filtering logic,
 * aiming to cover the defect from Defects4J bug 21b.
 */
public class FilteringParserDelegateTest {

    private JsonFactory factory;

    @Before
    public void setUp() throws Exception {
        factory = new JsonFactory();
    }

    // Helper: create a parser that returns tokens from a JSON string
    private JsonParser createParser(String json) throws IOException {
        return factory.createParser(json);
    }

    // Helper: create a FilteringParserDelegate with given filter and flags
    private FilteringParserDelegate createDelegate(String json, TokenFilter filter,
            boolean includePath, boolean allowMultipleMatches) throws IOException {
        JsonParser delegate = createParser(json);
        return new FilteringParserDelegate(delegate, filter, includePath, allowMultipleMatches);
    }

    // Helper: advance through tokens and collect them into a StringBuilder
    private String collectTokens(FilteringParserDelegate parser) throws IOException {
        StringBuilder sb = new StringBuilder();
        JsonToken t;
        while ((t = parser.nextToken()) != null) {
            if (sb.length() > 0) sb.append(",");
            sb.append(t.name());
            if (t == JsonToken.FIELD_NAME) {
                sb.append("(").append(parser.getCurrentName()).append(")");
            } else if (t.isScalarValue()) {
                sb.append("(").append(parser.getText()).append(")");
            }
        }
        return sb.toString();
    }

    // ============================================================
    // Tests for includeAll filter
    // ============================================================

    @Test
    public void testNextToken_includeAll_returnsAllTokens() throws IOException {
        // Normal case: includeAll filter, includePath=false, allowMultipleMatches=true
        String json = "{\"a\":1, \"b\":2}";
        FilteringParserDelegate parser = createDelegate(json, TokenFilter.INCLUDE_ALL, false, true);
        String tokens = collectTokens(parser);
        // Expected: START_OBJECT,FIELD_NAME(a),VALUE_NUMBER_INT(1),FIELD_NAME(b),VALUE_NUMBER_INT(2),END_OBJECT
        assertEquals("START_OBJECT,FIELD_NAME(a),VALUE_NUMBER_INT(1),FIELD_NAME(b),VALUE_NUMBER_INT(2),END_OBJECT", tokens);
    }

    @Test
    public void testNextToken_includeAllScalar_returnsScalar() throws IOException {
        String json = "42";
        FilteringParserDelegate parser = createDelegate(json, TokenFilter.INCLUDE_ALL, false, true);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("42", parser.getText());
        assertNull(parser.nextToken()); // no more tokens
    }

    // ============================================================
    // Tests for null filter (block everything)
    // ============================================================

    @Test
    public void testNextToken_nullFilter_returnsNullImmediately() throws IOException {
        // When filter is null, nextToken should return null at first call? Actually code handles null filter: in nextToken initial check may skip.
        // But we can test that no tokens are emitted.
        String json = "{\"a\":1}";
        TokenFilter nullFilter = null; // Note: TokenFilter is abstract, but we can pass null pointer? The constructor expects TokenFilter f, but null is allowed? In source code rootFilter is stored and used; if null, checkValue returns null, and skipChildren called. So we expect no tokens.
        FilteringParserDelegate parser = createDelegate(json, null, false, true);
        assertNull(parser.nextToken()); // should return null immediately or after first token?
        // Actually based on code: if f==null in switch, delegate.skipChildren() and then loop returns next token? This might be tricky.
        // Let's just test that no tokens are produced and parser does not throw.
    }

    // ============================================================
    // Tests for custom filter that includes only specific fields
    // ============================================================

    // Custom token filter that includes only field "a" and its value
    private static class IncludeAFieldFilter extends TokenFilter {
        @Override
        public TokenFilter includeProperty(String name) {
            if ("a".equals(name)) return TokenFilter.INCLUDE_ALL;
            return null;
        }
    }

    @Test
    public void testNextToken_customFilterIncludesFieldA() throws IOException {
        String json = "{\"a\":1, \"b\":2, \"c\":3}";
        TokenFilter filter = new IncludeAFieldFilter();
        FilteringParserDelegate parser = createDelegate(json, filter, false, true);
        String tokens = collectTokens(parser);
        // Expected: START_OBJECT, FIELD_NAME(a), VALUE_NUMBER_INT(1), END_OBJECT
        // But note: code includes START_OBJECT if field is included? With includePath=false, START_OBJECT not automatically included.
        // Actually with includePath=false, the START_OBJECT is not emitted unless includeImmediateParent is true (deprecated). So we get only the field and value? Let's check logic: In ID_FIELD_NAME case when _includePath is false, and _includeImmediateParent is false, we just return the field name token. But then the value token must be returned. However, the START_OBJECT is never emitted because _headContext.isStartHandled() is false and we don't return it. So result should be: FIELD_NAME(a), VALUE_NUMBER_INT(1). But then we also need END_OBJECT? When we see END_OBJECT, _headContext.isStartHandled() is false, so we don't return END_OBJECT. So only tokens: FIELD_NAME(a), VALUE_NUMBER_INT(1). Let's test.
        // Update: After field name returned, next call to nextToken will get value token (since itemFilter is INCLUDE_ALL). Then next call gets END_OBJECT but returns nothing. So output: "FIELD_NAME(a),VALUE_NUMBER_INT(1)"
        assertEquals("FIELD_NAME(a),VALUE_NUMBER_INT(1)", tokens);
    }

    @Test
    public void testNextToken_customFilterWithIncludePath_includesPath() throws IOException {
        String json = "{\"a\":1, \"b\":2}";
        TokenFilter filter = new IncludeAFieldFilter();
        FilteringParserDelegate parser = createDelegate(json, filter, true, true);
        String tokens = collectTokens(parser);
        // With includePath=true, the START_OBJECT is included (via _nextTokenWithBuffering?) and also END_OBJECT.
        // Expected: START_OBJECT, FIELD_NAME(a), VALUE_NUMBER_INT(1), END_OBJECT
        assertEquals("START_OBJECT,FIELD_NAME(a),VALUE_NUMBER_INT(1),END_OBJECT", tokens);
    }

    // ============================================================
    // Tests for allowMultipleMatches=false
    // ============================================================

    @Test
    public void testNextToken_allowMultipleMatchesFalse_stopsAfterFirstScalar() throws IOException {
        // Test case from defect area: when allowMultipleMatches=false, after first scalar match, nextToken should return null.
        String json = "{\"a\":1}";
        TokenFilter filter = new IncludeAFieldFilter();
        FilteringParserDelegate parser = createDelegate(json, filter, false, false);
        // First call returns FIELD_NAME(a)
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        // Second call returns VALUE_NUMBER_INT(1)
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        // Third call (should return null because allowMultipleMatches false, scalar matched, and not startHandled)
        assertNull(parser.nextToken());
    }

    @Test
    public void testNextToken_allowMultipleMatchesFalse_structEnd_returnsNull() throws IOException {
        // When _currToken is struct end and allowMultipleMatches false and start handled true, returns null.
        String json = "{\"a\":1}";
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate parser = createDelegate(json, filter, false, false);
        // Advance to END_OBJECT
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME(a)
        parser.nextToken(); // VALUE_NUMBER_INT(1)
        JsonToken t = parser.nextToken(); // END_OBJECT
        assertEquals(JsonToken.END_OBJECT, t);
        // Next call should return null because _currToken is struct end and conditions met.
        assertNull(parser.nextToken());
    }

    // ============================================================
    // Tests for nested structures
    // ============================================================

    @Test
    public void testNextToken_nestedObject_includePath() throws IOException {
        String json = "{\"outer\":{\"inner\":\"value\"}}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("inner".equals(name)) return TokenFilter.INCLUDE_ALL;
                return null;
            }
        };
        FilteringParserDelegate parser = createDelegate(json, filter, true, true);
        String tokens = collectTokens(parser);
        // With includePath true, we should get: START_OBJECT, FIELD_NAME(outer), START_OBJECT, FIELD_NAME(inner), VALUE_STRING(value), END_OBJECT, END_OBJECT
        assertEquals("START_OBJECT,FIELD_NAME(outer),START_OBJECT,FIELD_NAME(inner),VALUE_STRING(value),END_OBJECT,END_OBJECT", tokens);
    }

    @Test
    public void testNextToken_nestedArray_includePath() throws IOException {
        String json = "{\"arr\":[1,2,3]}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("arr".equals(name)) return TokenFilter.INCLUDE_ALL;
                return null;
            }
        };
        FilteringParserDelegate parser = createDelegate(json, filter, true, true);
        String tokens = collectTokens(parser);
        // Should include START_OBJECT, FIELD_NAME(arr), START_ARRAY, VALUE_NUMBER_INT(1), VALUE_NUMBER_INT(2), VALUE_NUMBER_INT(3), END_ARRAY, END_OBJECT
        assertEquals("START_OBJECT,FIELD_NAME(arr),START_ARRAY,VALUE_NUMBER_INT(1),VALUE_NUMBER_INT(2),VALUE_NUMBER_INT(3),END_ARRAY,END_OBJECT", tokens);
    }

    // ============================================================
    // Tests for filter that includes values based on condition
    // ============================================================

    @Test
    public void testNextToken_includeValueConditional_scalarPasses() throws IOException {
        // Custom filter that includes any value > 10
        TokenFilter filter = new TokenFilter() {
            @Override
            public boolean includeValue(JsonParser parser) throws IOException {
                // simple: include values that are numeric and > 10
                if (parser.currentToken() == JsonToken.VALUE_NUMBER_INT) {
                    return parser.getIntValue() > 10;
                }
                return false;
            }
        };
        String json = "1 20 3 40";
        FilteringParserDelegate parser = createDelegate(json, filter, false, true);
        // We expect only 20 and 40 to be returned
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("20", parser.getText());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("40", parser.getText());
        assertNull(parser.nextToken());
    }

    // ============================================================
    // Tests for nextValue()
    // ============================================================

    @Test
    public void testNextValue_afterFieldName_returnsValue() throws IOException {
        String json = "{\"x\":\"y\"}";
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate parser = createDelegate(json, filter, false, true);
        parser.nextToken(); // START_OBJECT
        JsonToken val = parser.nextValue();
        assertEquals(JsonToken.VALUE_STRING, val);
        assertEquals("y", parser.getText());
    }

    // ============================================================
    // Tests for skipChildren()
    // ============================================================

    @Test
    public void testSkipChildren_skipsNestedObject() throws IOException {
        String json = "{\"a\":{\"b\":1}}";
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate parser = createDelegate(json, filter, false, true);
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME(a)
        parser.nextToken(); // START_OBJECT
        parser.skipChildren();
        // After skipChildren, current token should be END_OBJECT of outer? Actually after skipping inner object, next token should be END_OBJECT of outer.
        assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());
        // And then no more tokens
        assertNull(parser.nextToken());
    }

    // ============================================================
    // Tests for boundary: empty object
    // ============================================================

    @Test
    public void testNextToken_emptyObject_returnsStartEnd() throws IOException {
        String json = "{}";
        FilteringParserDelegate parser = createDelegate(json, TokenFilter.INCLUDE_ALL, false, true);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    // ============================================================
    // Tests for empty array
    // ============================================================

    @Test
    public void testNextToken_emptyArray_nestedInObject() throws IOException {
        String json = "{\"arr\":[]}";
        TokenFilter filter = new TokenFilter() {
            @Override
            public TokenFilter includeProperty(String name) {
                if ("arr".equals(name)) return TokenFilter.INCLUDE_ALL;
                return null;
            }
        };
        FilteringParserDelegate parser = createDelegate(json, filter, true, true);
        String tokens = collectTokens(parser);
        // Should include START_OBJECT, FIELD_NAME(arr), START_ARRAY, END_ARRAY, END_OBJECT
        assertEquals("START_OBJECT,FIELD_NAME(arr),START_ARRAY,END_ARRAY,END_OBJECT", tokens);
    }

    // ============================================================
    // Tests for getCurrentToken() and clearCurrentToken()
    // ============================================================

    @Test
    public void testClearCurrentToken_clearsAndLastCleared() throws IOException {
        String json = "42";
        FilteringParserDelegate parser = createDelegate(json, TokenFilter.INCLUDE_ALL, false, true);
        parser.nextToken(); // VALUE_NUMBER_INT
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.getCurrentToken());
        parser.clearCurrentToken();
        assertNull(parser.getCurrentToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.getLastClearedToken());
    }

    // ============================================================
    // Tests for exception path: overrideCurrentName throws
    // ============================================================

    @Test(expected = UnsupportedOperationException.class)
    public void testOverrideCurrentName_throwsUnsupported() throws IOException {
        String json = "{}";
        FilteringParserDelegate parser = createDelegate(json, TokenFilter.INCLUDE_ALL, false, true);
        parser.overrideCurrentName("test");
    }

    // ============================================================
    // Tests for getCurrentName() inside array
    // ============================================================

    @Test
    public void testGetCurrentName_insideArray_returnsNull() throws IOException {
        String json = "[\"a\",\"b\"]";
        FilteringParserDelegate parser = createDelegate(json, TokenFilter.INCLUDE_ALL, false, true);
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // VALUE_STRING("a")
        // Current token is value, not field, so getCurrentName should return null (since context parent is array with no name)
        assertNull(parser.getCurrentName());
    }

    // ============================================================
    // Tests for filter that includes only root scalar
    // ============================================================

    @Test
    public void testNextToken_includeAllRootScalar_allowMultipleFalse() throws IOException {
        String json = "\"hello\"";
        FilteringParserDelegate parser = createDelegate(json, TokenFilter.INCLUDE_ALL, false, false);
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        // Since scalar value and _headContext.isStartHandled() false? Actually for root values, _headContext is root context with isStartHandled? It should be false. So allowMultipleMatches false and scalar => return null.
        assertNull(parser.nextToken());
    }

    // ============================================================
    // Additional regression tests for Defects4J bug 21b
    // Likely involves handling of _exposedContext or allowMultipleMatches
    // ============================================================

    @Test
    public void testNextToken_allowMultipleFalse_afterStructEnd_returnsNull() throws IOException {
        // Regression: When allowMultipleMatches=false and _currToken is struct end, and _headContext.isStartHandled() true, returns null.
        String json = "{\"a\":1}";
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate parser = createDelegate(json, filter, false, false);
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME(a)
        parser.nextToken(); // VALUE_NUMBER_INT(1)
        parser.nextToken(); // END_OBJECT
        assertNull(parser.nextToken());
    }

    @Test
    public void testNextToken_allowMultipleFalse_includePathFalse_scalarNotInContainer() throws IOException {
        // This tests the condition in nextToken: if scalar and not startHandled and itemFilter == INCLUDE_ALL, return null.
        String json = "42";
        TokenFilter filter = TokenFilter.INCLUDE_ALL;
        FilteringParserDelegate parser = createDelegate(json, filter, false, false);
        parser.nextToken(); // VALUE_NUMBER_INT
        assertNull(parser.nextToken());
    }

    // ============================================================
    // NEW TEST CASES - coverage for getNumberType, getIntValue, etc.
    // ============================================================

    @Test
    public void testGetNumberType_integer() throws IOException {
        String json = "123";
        FilteringParserDelegate parser = createDelegate(json, TokenFilter.INCLUDE_ALL, false, true);
        parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.getCurrentToken());
        assertEquals(JsonParser.NumberType.INT, parser.getNumberType());
    }

    @Test
    public void testGetNumberType_long() throws IOException {
        String json = "1234567890123";
        FilteringParserDelegate parser = createDelegate(json, TokenFilter.INCLUDE_ALL, false, true);
        parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.getCurrentToken());
        assertEquals(JsonParser.NumberType.LONG, parser.getNumberType());
    }

    @Test
    public void testGetNumberType_float() throws IOException {
        String json = "3.14";
        FilteringParserDelegate parser = createDelegate(json, TokenFilter.INCLUDE_ALL, false, true);
        parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.getCurrentToken());
        assertEquals(JsonParser.NumberType.FLOAT, parser.getNumberType());
    }

    @Test
    public void testGetNumberType_double() throws IOException {
        String json = "1.234567890123456";
        FilteringParserDelegate parser = createDelegate(json, TokenFilter.INCLUDE_ALL, false, true);
        parser.nextToken();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.getCurrentToken());
        assertEquals(JsonParser.NumberType.DOUBLE, parser.getNumberType());
    }

    @Test
    public void testGetIntValue() throws IOException {
        String json = "42";
        FilteringParserDelegate parser = createDelegate(json, TokenFilter.INCLUDE_ALL, false, true);
        parser.nextToken();
        assertEquals(42, parser.getIntValue());
    }

    @Test
    public void testGetLongValue() throws IOException {
        String json = "1234567890123";
        FilteringParserDelegate parser = createDelegate(json, TokenFilter.INCLUDE_ALL, false, true);
        parser.nextToken();
        assertEquals(1234567890123L, parser.getLongValue());
    }

    @Test
    public void testGetFloatValue() throws IOException {
        String json = "3.14";
        FilteringParserDelegate parser = createDelegate(json, TokenFilter.INCLUDE_ALL, false, true);
        parser.nextToken();
        assertEquals(3.14f, parser.getFloatValue(), 1e-6);
    }

    @Test
    public void testGetDoubleValue() throws IOException {
        String json = "2.718281828459045";
        FilteringParserDelegate parser = createDelegate(json, TokenFilter.INCLUDE_ALL, false, true);
        parser.nextToken();
        assertEquals(2.718281828459045, parser.getDoubleValue(), 1e-15);
    }

    @Test
    public void testGetDecimalValue() throws IOException {
        String json = "0.1";
        FilteringParserDelegate parser = createDelegate(json, TokenFilter.INCLUDE_ALL, false, true);
        parser.nextToken();
        assertEquals(new BigDecimal("0.1"), parser.getDecimalValue());
    }

    @Test
    public void testGetBigIntegerValue() throws IOException {
        String json = "12345678901234567890";
        FilteringParserDelegate parser = createDelegate(json, TokenFilter.INCLUDE_ALL, false, true);
        parser.nextToken();
        assertEquals(new BigInteger("12345678901234567890"), parser.getBigIntegerValue());
    }

    @Test
    public void testGetTextCharacters() throws IOException {
        String json = "\"hello\"";
        FilteringParserDelegate parser = createDelegate(json, TokenFilter.INCLUDE_ALL, false, true);
        parser.nextToken();
        char[] chars = parser.getTextCharacters();
        int offset = parser.getTextOffset();
        int len = parser.getTextLength();
        assertEquals("hello", new String(chars, offset, len));
    }

    @Test
    public void testGetValueAsString() throws IOException {
        String json = "\"world\"";
        FilteringParserDelegate parser = createDelegate(json, TokenFilter.INCLUDE_ALL, false, true);
        parser.nextToken();
        assertEquals("world", parser.getValueAsString());
    }

    @Test
    public void testGetValueAsInt() throws IOException {
        String json = "99";
        FilteringParserDelegate parser = createDelegate(json, TokenFilter.INCLUDE_ALL, false, true);
        parser.nextToken();
        assertEquals(99, parser.getValueAsInt());
        // test with default
        assertEquals(0, parser.getValueAsInt(0)); // after consuming, current token null
    }

    @Test
    public void testGetCurrentTokenId() throws IOException {
        String json = "null";
        FilteringParserDelegate parser = createDelegate(json, TokenFilter.INCLUDE_ALL, false, true);
        parser.nextToken();
        assertEquals(JsonToken.VALUE_NULL.id(), parser.getCurrentTokenId());
    }

    @Test
    public void testGetCurrentLocation() throws IOException {
        String json = "true";
        FilteringParserDelegate parser = createDelegate(json, TokenFilter.INCLUDE_ALL, false, true);
        parser.nextToken();
        assertNotNull(parser.getCurrentLocation());
        assertNotNull(parser.getTokenLocation());
    }

    @Test
    public void testGetParsingContext() throws IOException {
        String json = "{\"a\":1}";
        FilteringParserDelegate parser = createDelegate(json, TokenFilter.INCLUDE_ALL, false, true);
        parser.nextToken(); // START_OBJECT
        assertNotNull(parser.getParsingContext());
        assertTrue(parser.getParsingContext().inObject());
    }

    @Test
    public void testHasToken() throws IOException {
        String json = "false";
        FilteringParserDelegate parser = createDelegate(json, TokenFilter.INCLUDE_ALL, false, true);
        parser.nextToken();
        assertTrue(parser.hasToken(JsonToken.VALUE_FALSE));
        assertFalse(parser.hasToken(JsonToken.VALUE_TRUE));
    }

    @Test
    public void testHasTokenId() throws IOException {
        String json = "true";
        FilteringParserDelegate parser = createDelegate(json, TokenFilter.INCLUDE_ALL, false, true);
        parser.nextToken();
        assertTrue(parser.hasTokenId(JsonToken.VALUE_TRUE.id()));
        assertFalse(parser.hasTokenId(JsonToken.VALUE_FALSE.id()));
    }

    @Test
    public void testNextToken_filterStartObject() throws IOException {
        // Custom filter that filters out START_OBJECT and END_OBJECT
        TokenFilter filter = new TokenFilter() {
            @Override
            protected boolean filterStartObject() {
                return false; // exclude start object
            }
            @Override
            protected TokenFilter filterFinishObject() {
                // return null to skip end object as well? Actually filterFinishObject returns TokenFilter
                return null; // this will cause END_OBJECT to be filtered
            }
        };
        String json = "{\"a\":1}";
        FilteringParserDelegate parser = createDelegate(json, filter, true, true);
        String tokens = collectTokens(parser);
        // Since start object and end object are filtered, we expect no tokens?
        // Actually with filterStartObject returning false, start object is not emitted.
        // With filterFinishObject returning null, end object is not emitted.
        // But then the field "a" and value 1? The filter for property may still be INCLUDE_ALL? default includeProperty returns INCLUDE_ALL.
        // So we should get FIELD_NAME(a), VALUE_NUMBER_INT(1)
        assertEquals("FIELD_NAME(a),VALUE_NUMBER_INT(1)", tokens);
    }

    @Test
    public void testNextToken_includeRootValue_filtered() throws IOException {
        // Test includeRootValue returning false
        TokenFilter filter = new TokenFilter() {
            @Override
            protected boolean includeRootValue(int index) {
                return false; // exclude root value
            }
        };
        String json = "42";
        FilteringParserDelegate parser = createDelegate(json, filter, false, true);
        assertNull(parser.nextToken()); // root value excluded
    }

    @Test
    public void testNextToken_includeValue_boolean_conditional() throws IOException {
        // Use includeValue that checks parser for boolean
        TokenFilter filter = new TokenFilter() {
            @Override
            public boolean includeValue(JsonParser parser) throws IOException {
                return parser.currentToken() == JsonToken.VALUE_TRUE;
            }
        };
        String json = "true false true false";
        FilteringParserDelegate parser = createDelegate(json, filter, false, true);
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals("true", parser.getText());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals("true", parser.getText());
        assertNull(parser.nextToken());
    }
}