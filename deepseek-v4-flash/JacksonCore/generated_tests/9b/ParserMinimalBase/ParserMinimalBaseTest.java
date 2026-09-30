package com.fasterxml.jackson.core.base;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.IOException;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.JsonParser.Feature;

public class ParserMinimalBaseTest {

    private static class TestParser extends ParserMinimalBase {
        private JsonToken nextTokenValue;
        private JsonToken[] tokenQueue;
        private int tokenIdx;
        private String currentText;
        private int intVal;
        private long longVal;
        private double doubleVal;
        private Object embeddedObj;
        private boolean closed;
        private boolean featEnabled;

        TestParser() { super(0); }

        void setNextToken(JsonToken t) { this.nextTokenValue = t; }
        void setTokenQueue(JsonToken... tokens) {
            this.tokenQueue = tokens;
            tokenIdx = 0;
        }
        void setCurrentText(String s) { this.currentText = s; }
        void setIntVal(int v) { this.intVal = v; }
        void setLongVal(long v) { this.longVal = v; }
        void setDoubleVal(double v) { this.doubleVal = v; }
        void setEmbeddedObj(Object o) { this.embeddedObj = o; }
        void setClosed(boolean c) { this.closed = c; }
        void setFeatEnabled(boolean f) { this.featEnabled = f; }

        @Override public JsonToken nextToken() {
            if (tokenQueue != null && tokenIdx < tokenQueue.length) {
                return tokenQueue[tokenIdx++];
            }
            return nextTokenValue;
        }
        @Override public String getCurrentName() { return null; }
        @Override public void close() { closed = true; }
        @Override public boolean isClosed() { return closed; }
        @Override public JsonStreamContext getParsingContext() { return null; }
        @Override public String getText() { return currentText; }
        @Override public char[] getTextCharacters() { return currentText.toCharArray(); }
        @Override public boolean hasTextCharacters() { return true; }
        @Override public int getTextLength() { return currentText.length(); }
        @Override public int getTextOffset() { return 0; }
        @Override public byte[] getBinaryValue(Base64Variant b64variant) { return null; }
        @Override public void overrideCurrentName(String name) {}
        @Override protected void _handleEOF() throws JsonParseException {
            throw new JsonParseException("EOF", null);
        }
        @Override public int getIntValue() { return intVal; }
        @Override public long getLongValue() { return longVal; }
        @Override public double getDoubleValue() { return doubleVal; }
        @Override public Object getEmbeddedObject() { return embeddedObj; }
        @Override public JsonLocation getCurrentLocation() { return null; }
        @Override public boolean isEnabled(Feature f) { return featEnabled; }
    }

    // Tests initial state
    @Test
    public void testGetCurrentToken_null_returnsNull() {
        TestParser parser = new TestParser();
        assertNull(parser.getCurrentToken());
    }

    @Test
    public void testGetCurrentTokenId_null_returnsNoToken() {
        TestParser parser = new TestParser();
        assertEquals(JsonTokenId.ID_NO_TOKEN, parser.getCurrentTokenId());
    }

    @Test
    public void testHasTokenId_noTokenMatchingNoToken_true() {
        TestParser parser = new TestParser();
        assertTrue(parser.hasTokenId(JsonTokenId.ID_NO_TOKEN));
    }

    @Test
    public void testHasToken_equals_true() {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.VALUE_TRUE;
        assertTrue(parser.hasToken(JsonToken.VALUE_TRUE));
    }

    @Test
    public void testClearCurrentToken_savesLastCleared() {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.VALUE_NUMBER_INT;
        parser.clearCurrentToken();
        assertNull(parser._currToken);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser._lastClearedToken);
    }

    @Test
    public void testGetLastClearedToken_returnsClearedToken() {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.VALUE_STRING;
        parser.clearCurrentToken();
        assertEquals(JsonToken.VALUE_STRING, parser.getLastClearedToken());
    }

    // nextValue
    @Test
    public void testNextValue_fieldNameAdvancesToken() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.FIELD_NAME;
        parser.setNextToken(JsonToken.VALUE_NUMBER_INT);
        JsonToken result = parser.nextValue();
        assertEquals(JsonToken.VALUE_NUMBER_INT, result);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.getCurrentToken());
    }

    // skipChildren
    @Test
    public void testSkipChildren_notStruct_returnsThis() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.VALUE_TRUE;
        assertSame(parser, parser.skipChildren());
    }

    @Test
    public void testSkipChildren_structWithNesting_returnsAfterClose() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.START_OBJECT;
        parser.setTokenQueue(JsonToken.FIELD_NAME, JsonToken.VALUE_NUMBER_INT, JsonToken.END_OBJECT);
        JsonParser result = parser.skipChildren();
        assertSame(parser, result);
        assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());
    }

    @Test(expected = JsonParseException.class)
    public void testSkipChildren_eof_throwsException() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.START_ARRAY;
        parser.setNextToken(null);
        parser.skipChildren();
    }

    // getValueAsBoolean
    @Test
    public void testGetValueAsBoolean_stringTrue_returnsTrue() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.VALUE_STRING;
        parser.setCurrentText("true");
        assertTrue(parser.getValueAsBoolean(false));
    }

    @Test
    public void testGetValueAsBoolean_trueToken_returnsTrue() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.VALUE_TRUE;
        assertTrue(parser.getValueAsBoolean(false));
    }

    @Test
    public void testGetValueAsBoolean_default_returnsDefault() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.START_OBJECT;
        assertTrue(parser.getValueAsBoolean(true));
        assertFalse(parser.getValueAsBoolean(false));
    }

    // getValueAsInt
    @Test
    public void testGetValueAsInt_intToken_returnsIntValue() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.VALUE_NUMBER_INT;
        parser.setIntVal(42);
        assertEquals(42, parser.getValueAsInt());
    }

    @Test
    public void testGetValueAsInt_stringNumber_returnsParsed() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.VALUE_STRING;
        parser.setCurrentText("123");
        assertEquals(123, parser.getValueAsInt(0));
    }

    @Test
    public void testGetValueAsInt_falseToken_returnsZero() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.VALUE_FALSE;
        assertEquals(0, parser.getValueAsInt(99));
    }

    // getValueAsLong
    @Test
    public void testGetValueAsLong_longToken_returnsLongValue() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.VALUE_NUMBER_INT;
        parser.setLongVal(100L);
        assertEquals(100L, parser.getValueAsLong());
    }

    // getValueAsDouble
    @Test
    public void testGetValueAsDouble_doubleToken_returnsDoubleValue() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.VALUE_NUMBER_FLOAT;
        parser.setDoubleVal(3.14);
        assertEquals(3.14, parser.getValueAsDouble(), 1e-9);
    }

    @Test
    public void testGetValueAsDouble_stringNumber_returnsParsed() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.VALUE_STRING;
        parser.setCurrentText("2.5");
        assertEquals(2.5, parser.getValueAsDouble(0.0), 1e-9);
    }

    // getValueAsString
    @Test
    public void testGetValueAsString_stringToken_returnsText() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.VALUE_STRING;
        parser.setCurrentText("hello");
        assertEquals("hello", parser.getValueAsString());
    }

    @Test
    public void testGetValueAsString_nonScalar_returnsDefault() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.START_ARRAY;
        assertEquals("default", parser.getValueAsString("default"));
    }

    // Exception paths
    @Test(expected = JsonParseException.class)
    public void testThrowInvalidSpace_throwsException() throws JsonParseException {
        TestParser parser = new TestParser();
        parser._throwInvalidSpace(' ');
    }

    @Test(expected = JsonParseException.class)
    public void testReportInvalidEOF_throwsException() throws JsonParseException {
        TestParser parser = new TestParser();
        parser._reportInvalidEOF();
    }

    // ========== New tests for uncovered areas ==========

    // Test getCurrentToken with a set token
    @Test
    public void testGetCurrentToken_withToken_returnsToken() {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.VALUE_STRING;
        assertEquals(JsonToken.VALUE_STRING, parser.getCurrentToken());
    }

    // Test getCurrentTokenId with a set token
    @Test
    public void testGetCurrentTokenId_withToken_returnsCorrectId() {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.VALUE_NUMBER_INT;
        assertEquals(JsonTokenId.ID_NUMBER_INT, parser.getCurrentTokenId());
    }

    // Test hasTokenId with matching ID
    @Test
    public void testHasTokenId_matchingId_returnsTrue() {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.VALUE_TRUE;
        assertTrue(parser.hasTokenId(JsonTokenId.ID_TRUE));
    }

    // Test hasTokenId with non-matching ID
    @Test
    public void testHasTokenId_nonMatchingId_returnsFalse() {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.VALUE_TRUE;
        assertFalse(parser.hasTokenId(JsonTokenId.ID_FALSE));
    }

    // Test hasToken with different token returns false
    @Test
    public void testHasToken_notEquals_returnsFalse() {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.VALUE_TRUE;
        assertFalse(parser.hasToken(JsonToken.VALUE_FALSE));
    }

    // Test nextValue when not FIELD_NAME returns same token
    @Test
    public void testNextValue_nonFieldName_returnsCurrentToken() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.VALUE_NUMBER_INT;
        JsonToken result = parser.nextValue();
        assertEquals(JsonToken.VALUE_NUMBER_INT, result);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.getCurrentToken());
    }

    // Test getValueAsBoolean with string "false"
    @Test
    public void testGetValueAsBoolean_stringFalse_returnsFalse() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.VALUE_STRING;
        parser.setCurrentText("false");
        assertFalse(parser.getValueAsBoolean(true));
    }

    // Test getValueAsBoolean with null token
    @Test
    public void testGetValueAsBoolean_nullToken_returnsDefault() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = null;
        assertFalse(parser.getValueAsBoolean(false));
    }

    // Test getValueAsBoolean with number token
    @Test
    public void testGetValueAsBoolean_numberToken_returnsDefault() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.VALUE_NUMBER_INT;
        parser.setIntVal(1);
        assertFalse(parser.getValueAsBoolean(false));
    }

    // Test getValueAsInt with float token
    @Test
    public void testGetValueAsInt_floatToken_returnsParsedInt() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.VALUE_NUMBER_FLOAT;
        parser.setDoubleVal(3.14);
        assertEquals(3, parser.getValueAsInt());
    }

    // Test getValueAsInt with null token returns default
    @Test
    public void testGetValueAsInt_nullToken_returnsDefault() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = null;
        assertEquals(10, parser.getValueAsInt(10));
    }

    // Test getValueAsLong with null token
    @Test
    public void testGetValueAsLong_nullToken_returnsDefault() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = null;
        assertEquals(50L, parser.getValueAsLong(50L));
    }

    // Test getValueAsLong with string number
    @Test
    public void testGetValueAsLong_stringNumber_returnsParsed() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.VALUE_STRING;
        parser.setCurrentText("200");
        assertEquals(200L, parser.getValueAsLong(0L));
    }

    // Test getValueAsDouble with null token
    @Test
    public void testGetValueAsDouble_nullToken_returnsDefault() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = null;
        assertEquals(1.5, parser.getValueAsDouble(1.5), 1e-9);
    }

    // Test getValueAsDouble with float token
    @Test
    public void testGetValueAsDouble_floatToken_returnsValue() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.VALUE_NUMBER_FLOAT;
        parser.setDoubleVal(2.71);
        assertEquals(2.71, parser.getValueAsDouble(), 1e-9);
    }

    // Test getValueAsString with null token
    @Test
    public void testGetValueAsString_nullToken_returnsDefault() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = null;
        assertEquals("fallback", parser.getValueAsString("fallback"));
    }

    // Test getValueAsString with number token
    @Test
    public void testGetValueAsString_numberToken_returnsText() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.VALUE_NUMBER_INT;
        parser.setCurrentText("42");
        assertEquals("42", parser.getValueAsString());
    }

    // Test skipChildren with START_ARRAY followed by nested tokens
    @Test
    public void testSkipChildren_startArrayWithNesting_returnsAfterClose() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.START_ARRAY;
        parser.setTokenQueue(JsonToken.START_OBJECT, JsonToken.END_OBJECT, JsonToken.END_ARRAY);
        JsonParser result = parser.skipChildren();
        assertSame(parser, result);
        assertEquals(JsonToken.END_ARRAY, parser.getCurrentToken());
    }

    // Test skipChildren with START_OBJECT that immediately ends
    @Test
    public void testSkipChildren_emptyStruct_returnsAfterClose() throws IOException {
        TestParser parser = new TestParser();
        parser._currToken = JsonToken.START_OBJECT;
        parser.setTokenQueue(JsonToken.END_OBJECT);
        JsonParser result = parser.skipChildren();
        assertSame(parser, result);
        assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());
    }

    // Test isEnabled with feature enabled
    @Test
    public void testIsEnabled_featureEnabled_returnsTrue() {
        TestParser parser = new TestParser();
        parser.setFeatEnabled(true);
        assertTrue(parser.isEnabled(Feature.ALLOW_COMMENTS));
    }

    // Test isEnabled with feature disabled
    @Test
    public void testIsEnabled_featureDisabled_returnsFalse() {
        TestParser parser = new TestParser();
        parser.setFeatEnabled(false);
        assertFalse(parser.isEnabled(Feature.ALLOW_COMMENTS));
    }
}