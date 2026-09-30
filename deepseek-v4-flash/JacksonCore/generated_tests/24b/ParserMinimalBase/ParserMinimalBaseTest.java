package com.fasterxml.jackson.core.base;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Deque;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.exc.InputCoercionException;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;

public class ParserMinimalBaseTest {

    private TestParser parser;

    @Before
    public void setUp() {
        parser = new TestParser();
    }

    // Tests default token state when no token has been read
    @Test
    public void testCurrentToken_initialState_returnsNoToken() {
        assertNull(parser.currentToken());
        assertNull(parser.getCurrentToken());
        assertEquals(JsonTokenId.ID_NO_TOKEN, parser.currentTokenId());
        assertEquals(JsonTokenId.ID_NO_TOKEN, parser.getCurrentTokenId());
        assertFalse(parser.hasCurrentToken());
        assertTrue(parser.hasTokenId(JsonTokenId.ID_NO_TOKEN));
        assertFalse(parser.hasTokenId(JsonTokenId.ID_TRUE));
    }

    // Tests token accessors when a token is present
    @Test
    public void testCurrentToken_setToken_returnsTokenAndIds() {
        parser.setCurrentToken(JsonToken.VALUE_TRUE);
        assertSame(JsonToken.VALUE_TRUE, parser.currentToken());
        assertSame(JsonToken.VALUE_TRUE, parser.getCurrentToken());
        assertEquals(JsonTokenId.ID_TRUE, parser.currentTokenId());
        assertEquals(JsonTokenId.ID_TRUE, parser.getCurrentTokenId());
        assertTrue(parser.hasCurrentToken());
        assertTrue(parser.hasTokenId(JsonTokenId.ID_TRUE));
        assertTrue(parser.hasToken(JsonToken.VALUE_TRUE));
        assertFalse(parser.hasToken(JsonToken.VALUE_FALSE));
        assertFalse(parser.isExpectedStartArrayToken());
        assertFalse(parser.isExpectedStartObjectToken());
    }

    // Tests start-array/start-object detection helpers
    @Test
    public void testIsExpectedStartArrayToken_andObjectToken() {
        parser.setCurrentToken(JsonToken.START_ARRAY);
        assertTrue(parser.isExpectedStartArrayToken());
        assertFalse(parser.isExpectedStartObjectToken());

        parser.setCurrentToken(JsonToken.START_OBJECT);
        assertFalse(parser.isExpectedStartArrayToken());
        assertTrue(parser.isExpectedStartObjectToken());
    }

    // Tests clearCurrentToken and last-cleared-token bookkeeping
    @Test
    public void testClearCurrentToken_setsLastClearedToken() {
        assertNull(parser.getLastClearedToken());

        parser.clearCurrentToken();
        assertNull(parser.getLastClearedToken());

        parser.setCurrentToken(JsonToken.VALUE_STRING);
        parser.clearCurrentToken();
        assertNull(parser.getCurrentToken());
        assertSame(JsonToken.VALUE_STRING, parser.getLastClearedToken());
    }

    // Tests nextValue() when the first token is a field name
    @Test
    public void testNextValue_fieldName_skipsToValue() throws Exception {
        parser.queueTokens(JsonToken.FIELD_NAME, JsonToken.VALUE_NUMBER_INT);
        JsonToken token = parser.nextValue();
        assertSame(JsonToken.VALUE_NUMBER_INT, token);
        assertSame(JsonToken.VALUE_NUMBER_INT, parser.getCurrentToken());
    }

    // Tests nextValue() when the first token is not a field name
    @Test
    public void testNextValue_nonFieldName_returnsNextToken() throws Exception {
        parser.queueTokens(JsonToken.VALUE_STRING);
        JsonToken token = parser.nextValue();
        assertSame(JsonToken.VALUE_STRING, token);
        assertSame(JsonToken.VALUE_STRING, parser.getCurrentToken());
    }

    // Tests skipChildren() on a non-struct token
    @Test
    public void testSkipChildren_nonStruct_returnsThis() throws Exception {
        parser.setCurrentToken(JsonToken.VALUE_NUMBER_INT);
        assertSame(parser, parser.skipChildren());
        assertSame(JsonToken.VALUE_NUMBER_INT, parser.getCurrentToken());
    }

    // Tests skipChildren() on an empty object
    @Test
    public void testSkipChildren_emptyStruct_returnsThis() throws Exception {
        parser.setCurrentToken(JsonToken.START_OBJECT);
        parser.queueTokens(JsonToken.END_OBJECT);
        assertSame(parser, parser.skipChildren());
        assertSame(JsonToken.END_OBJECT, parser.getCurrentToken());
    }

    // Tests skipChildren() with nested structures
    @Test
    public void testSkipChildren_nestedStruct_returnsAfterMatchingClose() throws Exception {
        parser.setCurrentToken(JsonToken.START_ARRAY);
        parser.queueTokens(JsonToken.START_OBJECT, JsonToken.END_OBJECT, JsonToken.END_ARRAY);
        assertSame(parser, parser.skipChildren());
        assertSame(JsonToken.END_ARRAY, parser.getCurrentToken());
    }

    // Tests EOF handling inside skipChildren()
    @Test(expected = JsonEOFException.class)
    public void testSkipChildren_eofInsideStruct_throwsJsonEOFException() throws Exception {
        parser.setCurrentToken(JsonToken.START_ARRAY);
        parser.skipChildren();
    }

    // Tests non-blocking NOT_AVAILABLE handling inside skipChildren()
    @Test(timeout = 5000, expected = JsonParseException.class)
    public void testSkipChildren_notAvailable_throwsJsonParseException() throws Exception {
        parser.setCurrentToken(JsonToken.START_ARRAY);
        parser.queueTokens(JsonToken.NOT_AVAILABLE);
        parser.skipChildren();
    }

    // Tests getValueAsBoolean() across string, number, boolean, null, embedded-object, and default paths
    @Test
    public void testGetValueAsBoolean_variousTokens_returnsExpected() throws Exception {
        assertTrue(parser.getValueAsBoolean(true));

        parser.setCurrentToken(JsonToken.VALUE_STRING);
        parser.setText("true");
        assertTrue(parser.getValueAsBoolean(false));
        parser.setText("false");
        assertFalse(parser.getValueAsBoolean(true));
        parser.setText("null");
        assertFalse(parser.getValueAsBoolean(true));
        parser.setText("other");
        assertTrue(parser.getValueAsBoolean(true));

        parser.setCurrentToken(JsonToken.VALUE_NUMBER_INT);
        parser.setText("0");
        assertFalse(parser.getValueAsBoolean(true));
        parser.setText("1");
        assertTrue(parser.getValueAsBoolean(false));

        parser.setCurrentToken(JsonToken.VALUE_NUMBER_FLOAT);
        parser.setText("0.0");
        assertTrue(parser.getValueAsBoolean(true));

        parser.setCurrentToken(JsonToken.VALUE_TRUE);
        assertTrue(parser.getValueAsBoolean(false));
        parser.setCurrentToken(JsonToken.VALUE_FALSE);
        assertFalse(parser.getValueAsBoolean(true));
        parser.setCurrentToken(JsonToken.VALUE_NULL);
        assertFalse(parser.getValueAsBoolean(true));

        parser.setCurrentToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        parser.setEmbedded(Boolean.TRUE);
        assertTrue(parser.getValueAsBoolean(false));
        parser.setEmbedded(Boolean.FALSE);
        assertFalse(parser.getValueAsBoolean(true));
        parser.setEmbedded("not-a-boolean");
        assertTrue(parser.getValueAsBoolean(true));
    }

    // Tests getValueAsInt() across numeric, string, boolean, null, embedded-object, and default paths
    @Test
    public void testGetValueAsInt_variousTokens_returnsExpected() throws Exception {
        parser.setCurrentToken(JsonToken.VALUE_NUMBER_INT);
        parser.setText("42");
        assertEquals(42, parser.getValueAsInt());

        parser.setCurrentToken(JsonToken.VALUE_NUMBER_FLOAT);
        parser.setText("4.2");
        assertEquals(4, parser.getValueAsInt());

        parser.setCurrentToken(JsonToken.VALUE_STRING);
        parser.setText("17");
        assertEquals(17, parser.getValueAsInt(0));
        parser.setText("null");
        assertEquals(0, parser.getValueAsInt(9));
        parser.setText("abc");
        assertEquals(9, parser.getValueAsInt(9));

        parser.setCurrentToken(JsonToken.VALUE_TRUE);
        assertEquals(1, parser.getValueAsInt(0));
        parser.setCurrentToken(JsonToken.VALUE_FALSE);
        assertEquals(0, parser.getValueAsInt(9));
        parser.setCurrentToken(JsonToken.VALUE_NULL);
        assertEquals(0, parser.getValueAsInt(9));

        parser.setCurrentToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        parser.setEmbedded(42L);
        assertEquals(42, parser.getValueAsInt(0));
        parser.setEmbedded("not-a-number");
        assertEquals(0, parser.getValueAsInt(0));

        parser.setCurrentToken(JsonToken.START_OBJECT);
        assertEquals(7, parser.getValueAsInt(7));
    }

    // Tests getValueAsLong() across numeric, string, boolean, null, embedded-object, and default paths
    @Test
    public void testGetValueAsLong_variousTokens_returnsExpected() throws Exception {
        parser.setCurrentToken(JsonToken.VALUE_NUMBER_INT);
        parser.setText("123");
        assertEquals(123L, parser.getValueAsLong());

        parser.setCurrentToken(JsonToken.VALUE_NUMBER_FLOAT);
        parser.setText("4.9");
        assertEquals(4L, parser.getValueAsLong());

        parser.setCurrentToken(JsonToken.VALUE_STRING);
        parser.setText("456");
        assertEquals(456L, parser.getValueAsLong(0L));
        parser.setText("null");
        assertEquals(0L, parser.getValueAsLong(9L));
        parser.setText("abc");
        assertEquals(9L, parser.getValueAsLong(9L));

        parser.setCurrentToken(JsonToken.VALUE_TRUE);
        assertEquals(1L, parser.getValueAsLong(0L));
        parser.setCurrentToken(JsonToken.VALUE_FALSE);
        assertEquals(0L, parser.getValueAsLong(9L));
        parser.setCurrentToken(JsonToken.VALUE_NULL);
        assertEquals(0L, parser.getValueAsLong(9L));

        parser.setCurrentToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        parser.setEmbedded(123L);
        assertEquals(123L, parser.getValueAsLong(0L));

        parser.setCurrentToken(JsonToken.START_OBJECT);
        assertEquals(7L, parser.getValueAsLong(7L));
    }

    // Tests getValueAsDouble() across numeric, string, boolean, null, embedded-object, and default paths
    @Test
    public void testGetValueAsDouble_variousTokens_returnsExpected() throws Exception {
        parser.setCurrentToken(JsonToken.VALUE_NUMBER_INT);
        parser.setText("1");
        assertEquals(1.0, parser.getValueAsDouble(0.0), 0.0);

        parser.setCurrentToken(JsonToken.VALUE_NUMBER_FLOAT);
        parser.setText("1.5");
        assertEquals(1.5, parser.getValueAsDouble(0.0), 0.0);

        parser.setCurrentToken(JsonToken.VALUE_STRING);
        parser.setText("2.5");
        assertEquals(2.5, parser.getValueAsDouble(0.0), 0.0);
        parser.setText("null");
        assertEquals(0.0, parser.getValueAsDouble(9.0), 0.0);
        parser.setText("abc");
        assertEquals(9.0, parser.getValueAsDouble(9.0), 0.0);

        parser.setCurrentToken(JsonToken.VALUE_TRUE);
        assertEquals(1.0, parser.getValueAsDouble(0.0), 0.0);
        parser.setCurrentToken(JsonToken.VALUE_FALSE);
        assertEquals(0.0, parser.getValueAsDouble(9.0), 0.0);
        parser.setCurrentToken(JsonToken.VALUE_NULL);
        assertEquals(0.0, parser.getValueAsDouble(9.0), 0.0);

        parser.setCurrentToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        parser.setEmbedded(2.5);
        assertEquals(2.5, parser.getValueAsDouble(0.0), 0.0);

        parser.setCurrentToken(JsonToken.START_ARRAY);
        assertEquals(7.0, parser.getValueAsDouble(7.0), 0.0);
    }

    // Tests getValueAsString() across scalar, field-name, null, and non-scalar paths
    @Test
    public void testGetValueAsString_variousTokens_returnsExpected() throws Exception {
        parser.setCurrentToken(JsonToken.VALUE_STRING);
        parser.setText("abc");
        assertEquals("abc", parser.getValueAsString());
        assertEquals("abc", parser.getValueAsString("def"));

        parser.setCurrentToken(JsonToken.FIELD_NAME);
        parser.setCurrentName("field");
        assertEquals("field", parser.getValueAsString());
        assertEquals("field", parser.getValueAsString("def"));

        parser.setCurrentToken(JsonToken.VALUE_NUMBER_INT);
        parser.setText("42");
        assertEquals("42", parser.getValueAsString("def"));

        parser.setCurrentToken(JsonToken.VALUE_NULL);
        assertEquals("def", parser.getValueAsString("def"));

        parser.setCurrentToken(JsonToken.START_OBJECT);
        assertEquals("def", parser.getValueAsString("def"));

        parser.setCurrentToken(null);
        assertEquals("def", parser.getValueAsString("def"));
        assertNull(parser.getValueAsString());
    }

    // Tests reportOverflowInt(): method meant to throw InputCoercionException
    @Test(expected = InputCoercionException.class)
    public void testReportOverflowInt_longNumber_throwsInputCoercionException() throws Exception {
        parser.callReportOverflowInt("123456789012345678901234567890");
    }

    // Tests reportOverflowInt() using current text
    @Test(expected = InputCoercionException.class)
    public void testReportOverflowInt_withCurrentText_throwsInputCoercionException() throws Exception {
        parser.setText("123456789012345678901234567890");
        parser.callReportOverflowInt();
    }

    // Tests reportOverflowLong(): method meant to throw InputCoercionException
    @Test(expected = InputCoercionException.class)
    public void testReportOverflowLong_longNumber_throwsInputCoercionException() throws Exception {
        parser.callReportOverflowLong("123456789012345678901234567890");
    }

    // Tests reportOverflowLong() using current text
    @Test(expected = InputCoercionException.class)
    public void testReportOverflowLong_withCurrentText_throwsInputCoercionException() throws Exception {
        parser.setText("123456789012345678901234567890");
        parser.callReportOverflowLong();
    }

    // Tests _decodeBase64() with valid input
    @Test
    public void testDecodeBase64_validInput_appendsDecodedBytes() throws Exception {
        ByteArrayBuilder builder = new ByteArrayBuilder();
        parser.callDecodeBase64("YQ==", builder, Base64Variants.getDefaultVariant());
        assertArrayEquals(new byte[] { 97 }, builder.toByteArray());
    }

    // Tests _decodeBase64() with invalid input
    @Test(expected = JsonParseException.class)
    public void testDecodeBase64_invalidInput_throwsJsonParseException() throws Exception {
        parser.callDecodeBase64("!!!", new ByteArrayBuilder(), Base64Variants.getDefaultVariant());
    }

    // Tests _longIntegerDesc() for short, long, and negative-long numeric descriptions
    @Test
    public void testLongIntegerDesc_shortAndLargeValues_returnsDescription() {
        assertEquals("123", parser.callLongIntegerDesc("123"));

        String digits = repeatChar('9', 1000);
        assertEquals("[Integer with 1000 digits]", parser.callLongIntegerDesc(digits));
        assertEquals("[Integer with 1000 digits]", parser.callLongIntegerDesc("-" + digits));
    }

    // Tests _hasTextualNull() helper
    @Test
    public void testHasTextualNull_nullVariants_returnsExpected() {
        assertTrue(parser.callHasTextualNull("null"));
        assertFalse(parser.callHasTextualNull("NULL"));
        assertFalse(parser.callHasTextualNull(""));
    }

    // ---------- Additional tests to cover previously skipped areas ----------

    // Tests getValueAsString() without default for various tokens
    @Test
    public void testGetValueAsString_noArgForNumberAndBoolean() throws Exception {
        parser.setCurrentToken(JsonToken.VALUE_NUMBER_INT);
        parser.setText("99");
        assertEquals("99", parser.getValueAsString());

        parser.setCurrentToken(JsonToken.VALUE_TRUE);
        // VALUE_TRUE typically has no text representation, should return null
        assertNull(parser.getValueAsString());

        parser.setCurrentToken(JsonToken.VALUE_FALSE);
        assertNull(parser.getValueAsString());

        parser.setCurrentToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        parser.setEmbedded("embedded");
        // Embedded object may or may not be used; assume null
        assertNull(parser.getValueAsString());

        parser.setCurrentToken(JsonToken.VALUE_NULL);
        assertNull(parser.getValueAsString());
    }

    // Tests getValueAsString() with default for boolean and embedded object
    @Test
    public void testGetValueAsString_withDefaultForNonString() throws Exception {
        parser.setCurrentToken(JsonToken.VALUE_TRUE);
        assertEquals("default", parser.getValueAsString("default"));

        parser.setCurrentToken(JsonToken.VALUE_FALSE);
        assertEquals("default", parser.getValueAsString("default"));

        parser.setCurrentToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        assertEquals("default", parser.getValueAsString("default"));
    }

    // Tests getValueAsBoolean() with non-boolean token (START_OBJECT)
    @Test
    public void testGetValueAsBoolean_startObject_returnsDefault() throws Exception {
        parser.setCurrentToken(JsonToken.START_OBJECT);
        assertTrue(parser.getValueAsBoolean(true));
        assertFalse(parser.getValueAsBoolean(false));
    }

    // Tests getValueAsInt() with string that is not a valid number
    @Test
    public void testGetValueAsInt_nonNumericString_returnsDefault() throws Exception {
        parser.setCurrentToken(JsonToken.VALUE_STRING);
        parser.setText("true");
        assertEquals(0, parser.getValueAsInt(0));
        assertEquals(42, parser.getValueAsInt(42));

        parser.setText("false");
        assertEquals(0, parser.getValueAsInt(0));
    }

    // Tests getValueAsLong() with non-numeric string
    @Test
    public void testGetValueAsLong_nonNumericString_returnsDefault() throws Exception {
        parser.setCurrentToken(JsonToken.VALUE_STRING);
        parser.setText("true");
        assertEquals(0L, parser.getValueAsLong(0L));
        assertEquals(42L, parser.getValueAsLong(42L));
    }

    // Tests getValueAsDouble() with non-numeric string
    @Test
    public void testGetValueAsDouble_nonNumericString_returnsDefault() throws Exception {
        parser.setCurrentToken(JsonToken.VALUE_STRING);
        parser.setText("true");
        assertEquals(0.0, parser.getValueAsDouble(0.0), 0.0);
        assertEquals(4.2, parser.getValueAsDouble(4.2), 0.0);
    }

    // Tests nextValue() when token queue is empty
    @Test
    public void testNextValue_emptyQueue_returnsNull() throws Exception {
        // No tokens queued
        assertNull(parser.nextValue());
        assertNull(parser.getCurrentToken());
    }

    // Tests skipChildren() with an object containing multiple fields
    @Test
    public void testSkipChildren_objectWithMultipleFields_skipsToEnd() throws Exception {
        parser.setCurrentToken(JsonToken.START_OBJECT);
        parser.queueTokens(
            JsonToken.FIELD_NAME, JsonToken.VALUE_NUMBER_INT,
            JsonToken.FIELD_NAME, JsonToken.VALUE_STRING,
            JsonToken.END_OBJECT
        );
        assertSame(parser, parser.skipChildren());
        assertSame(JsonToken.END_OBJECT, parser.getCurrentToken());
    }

    // Tests getCurrentName() default null and overrideCurrentName()
    @Test
    public void testGetCurrentName_defaultAndOverride() throws Exception {
        assertNull(parser.getCurrentName());

        parser.setCurrentName("original");
        assertEquals("original", parser.getCurrentName());

        parser.overrideCurrentName("overridden");
        assertEquals("overridden", parser.getCurrentName());
    }

    // Tests isClosed() before and after close()
    @Test
    public void testIsClosed_defaultAndAfterClose() throws Exception {
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
    }

    private static String repeatChar(char c, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    private static class TestParser extends ParserMinimalBase {

        private String text = "";
        private String currentName;
        private Object embedded;
        private boolean closed;
        private final Deque<JsonToken> tokenQueue = new ArrayDeque<JsonToken>();

        TestParser() {
            super(0);
        }

        void setCurrentToken(JsonToken t) {
            _currToken = t;
        }

        void setText(String s) {
            this.text = s;
        }

        void setCurrentName(String name) {
            this.currentName = name;
        }

        void setEmbedded(Object value) {
            this.embedded = value;
        }

        void queueTokens(JsonToken... tokens) {
            for (JsonToken t : tokens) {
                tokenQueue.add(t);
            }
        }

        void callReportOverflowInt() throws IOException {
            reportOverflowInt();
        }

        void callReportOverflowInt(String numDesc) throws IOException {
            reportOverflowInt(numDesc);
        }

        void callReportOverflowLong() throws IOException {
            reportOverflowLong();
        }

        void callReportOverflowLong(String numDesc) throws IOException {
            reportOverflowLong(numDesc);
        }

        String callLongIntegerDesc(String rawNum) {
            return _longIntegerDesc(rawNum);
        }

        boolean callHasTextualNull(String value) {
            return _hasTextualNull(value);
        }

        void callDecodeBase64(String str, ByteArrayBuilder builder, Base64Variant b64variant) throws IOException {
            _decodeBase64(str, builder, b64variant);
        }

        @Override
        public JsonToken nextToken() throws IOException {
            _currToken = tokenQueue.poll();
            return _currToken;
        }

        @Override
        protected void _handleEOF() throws JsonParseException {
            _reportInvalidEOF();
        }

        @Override
        public String getCurrentName() throws IOException {
            return currentName;
        }

        @Override
        public void overrideCurrentName(String name) {
            this.currentName = name;
        }

        @Override
        public void close() throws IOException {
            this.closed = true;
        }

        @Override
        public boolean isClosed() {
            return closed;
        }

        @Override
        public JsonStreamContext getParsingContext() {
            return null;
        }

        @Override
        public JsonLocation getTokenLocation() {
            return null;
        }

        @Override
        public JsonLocation getCurrentLocation() {
            return null;
        }

        @Override
        public String getText() throws IOException {
            return text;
        }

        @Override
        public char[] getTextCharacters() throws IOException {
            return text.toCharArray();
        }

        @Override
        public boolean hasTextCharacters() {
            return true;
        }

        @Override
        public int getTextLength() throws IOException {
            return text.length();
        }

        @Override
        public int getTextOffset() throws IOException {
            return 0;
        }

        @Override
        public byte[] getBinaryValue(Base64Variant b64variant) throws IOException {
            return new byte[0];
        }

        @Override
        public Object getEmbeddedObject() throws IOException {
            return embedded;
        }

        @Override
        public int getIntValue() throws IOException {
            return new BigDecimal(text).intValue();
        }

        @Override
        public long getLongValue() throws IOException {
            return new BigDecimal(text).longValue();
        }

        @Override
        public BigInteger getBigIntegerValue() throws IOException {
            return new BigDecimal(text).toBigInteger();
        }

        @Override
        public float getFloatValue() throws IOException {
            return new BigDecimal(text).floatValue();
        }

        @Override
        public double getDoubleValue() throws IOException {
            return new BigDecimal(text).doubleValue();
        }

        @Override
        public BigDecimal getDecimalValue() throws IOException {
            return new BigDecimal(text);
        }

        @Override
        public Number getNumberValue() throws IOException {
            return new BigDecimal(text);
        }

        @Override
        public JsonParser.NumberType getNumberType() throws IOException {
            return null;
        }

        @Override
        public Version version() {
            return null;
        }
    }
}