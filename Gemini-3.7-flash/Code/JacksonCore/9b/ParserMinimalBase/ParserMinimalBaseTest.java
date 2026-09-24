package com.fasterxml.jackson.core.base;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;

public class ParserMinimalBaseTest {

    static class MinimalParserStub extends ParserMinimalBase {
        private final List<JsonToken> tokens;
        private int tokenIndex = 0;
        private String text;
        private int intVal;
        private long longVal;
        private double doubleVal;
        private Object embeddedObj;
        private String currentName;
        private boolean closed = false;

        public MinimalParserStub(JsonToken... tokens) {
            super();
            this.tokens = new ArrayList<JsonToken>(Arrays.asList(tokens));
        }

        public MinimalParserStub(int features, JsonToken... tokens) {
            super(features);
            this.tokens = new ArrayList<JsonToken>(Arrays.asList(tokens));
        }

        public void setCurrToken(JsonToken t) {
            this._currToken = t;
        }

        public void setText(String text) {
            this.text = text;
        }

        public void setNumberValues(int i, long l, double d) {
            this.intVal = i;
            this.longVal = l;
            this.doubleVal = d;
        }

        public void setEmbeddedObject(Object obj) {
            this.embeddedObj = obj;
        }

        public void setCurrentName(String name) {
            this.currentName = name;
        }

        @Override
        public JsonToken nextToken() throws IOException {
            if (tokenIndex < tokens.size()) {
                _currToken = tokens.get(tokenIndex++);
            } else {
                _currToken = null;
            }
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
            closed = true;
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
            return JsonLocation.NA;
        }

        @Override
        public JsonLocation getCurrentLocation() {
            return JsonLocation.NA;
        }

        @Override
        public ObjectCodec getCodec() {
            return null;
        }

        @Override
        public void setCodec(ObjectCodec c) { }

        @Override
        public Version version() {
            return Version.unknownVersion();
        }

        @Override
        public String getText() throws IOException {
            return text;
        }

        @Override
        public char[] getTextCharacters() throws IOException {
            return (text != null) ? text.toCharArray() : null;
        }

        @Override
        public boolean hasTextCharacters() {
            return text != null;
        }

        @Override
        public int getTextLength() throws IOException {
            return (text != null) ? text.length() : 0;
        }

        @Override
        public int getTextOffset() throws IOException {
            return 0;
        }

        @Override
        public byte[] getBinaryValue(Base64Variant b64variant) throws IOException {
            return null;
        }

        @Override
        public Number getNumberValue() throws IOException {
            return intVal;
        }

        @Override
        public NumberType getNumberType() throws IOException {
            return NumberType.INT;
        }

        @Override
        public int getIntValue() throws IOException {
            return intVal;
        }

        @Override
        public long getLongValue() throws IOException {
            return longVal;
        }

        @Override
        public BigInteger getBigIntegerValue() throws IOException {
            return BigInteger.valueOf(longVal);
        }

        @Override
        public float getFloatValue() throws IOException {
            return (float) doubleVal;
        }

        @Override
        public double getDoubleValue() throws IOException {
            return doubleVal;
        }

        @Override
        public BigDecimal getDecimalValue() throws IOException {
            return BigDecimal.valueOf(doubleVal);
        }

        @Override
        public Object getEmbeddedObject() throws IOException {
            return embeddedObj;
        }
    }

    // Tests getValueAsString with FIELD_NAME token
    @Test
    public void testGetValueAsString_fieldName_returnsCurrentName() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();
        parser.setCurrToken(JsonToken.FIELD_NAME);
        parser.setCurrentName("myField");
        parser.setText("myField");
        assertEquals("myField", parser.getValueAsString());
        assertEquals("myField", parser.getValueAsString("default"));
    }

    // Tests getValueAsString with string token and default value fallback
    @Test
    public void testGetValueAsString_stringAndNullTokens_returnsExpectedString() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();
        
        parser.setCurrToken(JsonToken.VALUE_STRING);
        parser.setText("hello");
        assertEquals("hello", parser.getValueAsString());
        assertEquals("hello", parser.getValueAsString("default"));

        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        parser.setText("123");
        assertEquals("123", parser.getValueAsString("default"));

        parser.setCurrToken(JsonToken.VALUE_NULL);
        assertEquals("default", parser.getValueAsString("default"));

        parser.setCurrToken(JsonToken.START_OBJECT);
        assertEquals("default", parser.getValueAsString("default"));

        parser.setCurrToken(null);
        assertNull(parser.getValueAsString());
        assertEquals("default", parser.getValueAsString("default"));
    }

    // Tests getValueAsBoolean with various token types
    @Test
    public void testGetValueAsBoolean_variousTokens_coercesCorrectly() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();

        parser.setCurrToken(JsonToken.VALUE_TRUE);
        assertTrue(parser.getValueAsBoolean(false));

        parser.setCurrToken(JsonToken.VALUE_FALSE);
        assertFalse(parser.getValueAsBoolean(true));

        parser.setCurrToken(JsonToken.VALUE_NULL);
        assertFalse(parser.getValueAsBoolean(true));

        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        parser.setNumberValues(1, 1L, 1.0);
        assertTrue(parser.getValueAsBoolean(false));
        parser.setNumberValues(0, 0L, 0.0);
        assertFalse(parser.getValueAsBoolean(true));

        parser.setCurrToken(JsonToken.VALUE_STRING);
        parser.setText("true");
        assertTrue(parser.getValueAsBoolean(false));
        parser.setText("false");
        assertFalse(parser.getValueAsBoolean(true));
        parser.setText("null");
        assertFalse(parser.getValueAsBoolean(true));
        parser.setText("unrecognized");
        assertTrue(parser.getValueAsBoolean(true));

        parser.setCurrToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        parser.setEmbeddedObject(Boolean.TRUE);
        assertTrue(parser.getValueAsBoolean(false));
        parser.setEmbeddedObject("notABoolean");
        assertTrue(parser.getValueAsBoolean(true));

        parser.setCurrToken(null);
        assertTrue(parser.getValueAsBoolean(true));
    }

    // Tests getValueAsInt with int, float, string, boolean and textual null
    @Test
    public void testGetValueAsInt_variousTokens_coercesCorrectly() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();

        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        parser.setNumberValues(42, 42L, 42.0);
        assertEquals(42, parser.getValueAsInt());
        assertEquals(42, parser.getValueAsInt(10));

        parser.setCurrToken(JsonToken.VALUE_NUMBER_FLOAT);
        assertEquals(42, parser.getValueAsInt());
        assertEquals(42, parser.getValueAsInt(10));

        parser.setCurrToken(JsonToken.VALUE_TRUE);
        assertEquals(1, parser.getValueAsInt(0));

        parser.setCurrToken(JsonToken.VALUE_FALSE);
        assertEquals(0, parser.getValueAsInt(5));

        parser.setCurrToken(JsonToken.VALUE_NULL);
        assertEquals(0, parser.getValueAsInt(5));

        parser.setCurrToken(JsonToken.VALUE_STRING);
        parser.setText("null");
        assertEquals(0, parser.getValueAsInt(5));
        parser.setText("123");
        assertEquals(123, parser.getValueAsInt(5));
        parser.setText("notAnInt");
        assertEquals(99, parser.getValueAsInt(99));

        parser.setCurrToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        parser.setEmbeddedObject(Integer.valueOf(77));
        assertEquals(77, parser.getValueAsInt(0));
        parser.setEmbeddedObject("nonNumber");
        assertEquals(55, parser.getValueAsInt(55));

        parser.setCurrToken(null);
        assertEquals(0, parser.getValueAsInt());
        assertEquals(10, parser.getValueAsInt(10));
    }

    // Tests getValueAsLong with int, float, string, boolean and textual null
    @Test
    public void testGetValueAsLong_variousTokens_coercesCorrectly() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();

        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        parser.setNumberValues(0, 100L, 100.0);
        assertEquals(100L, parser.getValueAsLong());
        assertEquals(100L, parser.getValueAsLong(5L));

        parser.setCurrToken(JsonToken.VALUE_NUMBER_FLOAT);
        assertEquals(100L, parser.getValueAsLong());
        assertEquals(100L, parser.getValueAsLong(5L));

        parser.setCurrToken(JsonToken.VALUE_TRUE);
        assertEquals(1L, parser.getValueAsLong(0L));

        parser.setCurrToken(JsonToken.VALUE_FALSE);
        assertEquals(0L, parser.getValueAsLong(5L));

        parser.setCurrToken(JsonToken.VALUE_NULL);
        assertEquals(0L, parser.getValueAsLong(5L));

        parser.setCurrToken(JsonToken.VALUE_STRING);
        parser.setText("null");
        assertEquals(0L, parser.getValueAsLong(5L));
        parser.setText("9999999999");
        assertEquals(9999999999L, parser.getValueAsLong(5L));

        parser.setCurrToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        parser.setEmbeddedObject(Long.valueOf(888L));
        assertEquals(888L, parser.getValueAsLong(0L));

        parser.setCurrToken(null);
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(123L, parser.getValueAsLong(123L));
    }

    // Tests getValueAsDouble with int, float, string, boolean and textual null
    @Test
    public void testGetValueAsDouble_variousTokens_coercesCorrectly() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();

        parser.setCurrToken(JsonToken.VALUE_NUMBER_FLOAT);
        parser.setNumberValues(0, 0L, 12.5);
        assertEquals(12.5, parser.getValueAsDouble(1.0), 0.0001);

        parser.setCurrToken(JsonToken.VALUE_NUMBER_INT);
        assertEquals(12.5, parser.getValueAsDouble(1.0), 0.0001);

        parser.setCurrToken(JsonToken.VALUE_TRUE);
        assertEquals(1.0, parser.getValueAsDouble(0.0), 0.0001);

        parser.setCurrToken(JsonToken.VALUE_FALSE);
        assertEquals(0.0, parser.getValueAsDouble(5.0), 0.0001);

        parser.setCurrToken(JsonToken.VALUE_NULL);
        assertEquals(0.0, parser.getValueAsDouble(5.0), 0.0001);

        parser.setCurrToken(JsonToken.VALUE_STRING);
        parser.setText("null");
        assertEquals(0.0, parser.getValueAsDouble(5.0), 0.0001);
        parser.setText("3.14159");
        assertEquals(3.14159, parser.getValueAsDouble(0.0), 0.0001);

        parser.setCurrToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        parser.setEmbeddedObject(Double.valueOf(4.5));
        assertEquals(4.5, parser.getValueAsDouble(0.0), 0.0001);

        parser.setCurrToken(null);
        assertEquals(7.5, parser.getValueAsDouble(7.5), 0.0001);
    }

    // Tests token state inquiry and clearCurrentToken / getLastClearedToken
    @Test
    public void testTokenStateOperations_updatesStateCorrectly() {
        MinimalParserStub parser = new MinimalParserStub();
        
        assertNull(parser.getCurrentToken());
        assertEquals(JsonTokenId.ID_NO_TOKEN, parser.getCurrentTokenId());
        assertFalse(parser.hasCurrentToken());
        assertTrue(parser.hasTokenId(JsonTokenId.ID_NO_TOKEN));
        assertFalse(parser.hasTokenId(JsonTokenId.ID_START_OBJECT));
        assertFalse(parser.hasToken(JsonToken.START_OBJECT));
        assertFalse(parser.isExpectedStartArrayToken());
        assertFalse(parser.isExpectedStartObjectToken());

        parser.setCurrToken(JsonToken.START_ARRAY);
        assertTrue(parser.isExpectedStartArrayToken());
        assertFalse(parser.isExpectedStartObjectToken());

        parser.setCurrToken(JsonToken.START_OBJECT);
        assertEquals(JsonToken.START_OBJECT, parser.getCurrentToken());
        assertEquals(JsonTokenId.ID_START_OBJECT, parser.getCurrentTokenId());
        assertTrue(parser.hasCurrentToken());
        assertTrue(parser.hasTokenId(JsonTokenId.ID_START_OBJECT));
        assertTrue(parser.hasToken(JsonToken.START_OBJECT));
        assertTrue(parser.isExpectedStartObjectToken());

        parser.clearCurrentToken();
        assertNull(parser.getCurrentToken());
        assertEquals(JsonToken.START_OBJECT, parser.getLastClearedToken());

        parser.clearCurrentToken();
        assertEquals(JsonToken.START_OBJECT, parser.getLastClearedToken());
    }

    // Tests nextValue skips FIELD_NAME token
    @Test
    public void testNextValue_withFieldName_advancesToValue() throws IOException {
        MinimalParserStub parser = new MinimalParserStub(
            JsonToken.FIELD_NAME,
            JsonToken.VALUE_STRING,
            JsonToken.VALUE_NUMBER_INT
        );

        JsonToken t1 = parser.nextValue();
        assertEquals(JsonToken.VALUE_STRING, t1);

        JsonToken t2 = parser.nextValue();
        assertEquals(JsonToken.VALUE_NUMBER_INT, t2);

        JsonToken t3 = parser.nextValue();
        assertNull(t3);
    }

    // Tests skipChildren with nested structure
    @Test
    public void testSkipChildren_nestedStructure_skipsEntireObject() throws IOException {
        MinimalParserStub parser = new MinimalParserStub(
            JsonToken.START_OBJECT,
            JsonToken.FIELD_NAME,
            JsonToken.START_ARRAY,
            JsonToken.VALUE_NUMBER_INT,
            JsonToken.END_ARRAY,
            JsonToken.END_OBJECT,
            JsonToken.VALUE_STRING
        );

        parser.nextToken(); // START_OBJECT
        JsonParser result = parser.skipChildren();
        assertSame(parser, result);
        assertEquals(JsonToken.END_OBJECT, parser.getCurrentToken());

        parser.nextToken(); // VALUE_STRING
        result = parser.skipChildren();
        assertSame(parser, result);
        assertEquals(JsonToken.VALUE_STRING, parser.getCurrentToken());
    }

    // Tests skipChildren EOF handling throws exception
    @Test(expected = JsonParseException.class)
    public void testSkipChildren_unclosedStructure_throwsEOF() throws IOException {
        MinimalParserStub parser = new MinimalParserStub(
            JsonToken.START_OBJECT,
            JsonToken.FIELD_NAME
        );
        parser.nextToken(); // START_OBJECT
        parser.skipChildren();
    }

    // Tests Base64 decoding helper
    @Test
    public void testDecodeBase64_validInput_decodesBytes() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();
        ByteArrayBuilder builder = new ByteArrayBuilder();
        parser._decodeBase64("SGVsbG8=", builder, Base64Variants.MIME);
        byte[] bytes = builder.toByteArray();
        assertEquals("Hello", new String(bytes, "UTF-8"));
    }

    // Tests Base64 decoding invalid input reports error
    @Test(expected = JsonParseException.class)
    public void testDecodeBase64_invalidInput_throwsException() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();
        ByteArrayBuilder builder = new ByteArrayBuilder();
        parser._decodeBase64("Invalid!@#Base64", builder, Base64Variants.MIME);
    }

    // Tests _getCharDesc helper
    @Test
    public void testGetCharDesc_variousChars_returnsDescriptions() {
        assertEquals("(CTRL-CHAR, code 0)", ParserMinimalBase._getCharDesc(0));
        assertEquals("'a' (code 97)", ParserMinimalBase._getCharDesc('a'));
        assertEquals("'\u0100' (code 256 / 0x100)", ParserMinimalBase._getCharDesc(256));
    }

    // Tests error reporting methods throw JsonParseException
    @Test(expected = JsonParseException.class)
    public void testReportUnexpectedChar_throwsJsonParseException() throws JsonParseException {
        MinimalParserStub parser = new MinimalParserStub();
        parser._reportUnexpectedChar('x', "unexpected");
    }

    // Tests invalid space error reporting
    @Test(expected = JsonParseException.class)
    public void testThrowInvalidSpace_throwsJsonParseException() throws JsonParseException {
        MinimalParserStub parser = new MinimalParserStub();
        parser._throwInvalidSpace(0);
    }

    // Tests unrecognized escape character handling
    @Test
    public void testHandleUnrecognizedCharacterEscape_withEnabledFeature_returnsChar() throws Exception {
        int features = JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.getMask();
        MinimalParserStub parser = new MinimalParserStub(features);
        char result = parser._handleUnrecognizedCharacterEscape('z');
        assertEquals('z', result);
    }

    // Tests unrecognized escape character throwing exception when not allowed
    @Test(expected = JsonProcessingException.class)
    public void testHandleUnrecognizedCharacterEscape_notAllowed_throwsException() throws Exception {
        MinimalParserStub parser = new MinimalParserStub(0);
        parser._handleUnrecognizedCharacterEscape('z');
    }

    // Tests ascii byte and string conversion helpers
    @Test
    public void testAsciiHelpers_roundTrip_convertsCorrectly() {
        byte[] bytes = ParserMinimalBase._asciiBytes("Jackson");
        assertEquals(7, bytes.length);
        assertEquals("Jackson", ParserMinimalBase._ascii(bytes));
    }

    // Tests additional error helper methods
    @Test(expected = JsonParseException.class)
    public void testReportInvalidEOFInValue_throwsJsonParseException() throws JsonParseException {
        MinimalParserStub parser = new MinimalParserStub();
        parser._reportInvalidEOFInValue();
    }

    @Test(expected = JsonParseException.class)
    public void testReportMissingRootSeparator_throwsJsonParseException() throws JsonParseException {
        MinimalParserStub parser = new MinimalParserStub();
        parser._reportMissingRootSeparator('a');
    }

    @Test(expected = JsonParseException.class)
    public void testThrowUnquotedSpace_throwsJsonParseException() throws JsonParseException {
        MinimalParserStub parser = new MinimalParserStub();
        parser._throwUnquotedSpace('\t', "string value");
    }

    @Test(expected = JsonParseException.class)
    public void testReportInvalidEOFWithCustomMessage_throwsJsonParseException() throws JsonParseException {
        MinimalParserStub parser = new MinimalParserStub();
        parser._reportInvalidEOF(": custom EOF message");
    }

    @Test(expected = JsonParseException.class)
    public void testDecodeBase64_withTruncatedInput_throwsJsonParseException() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();
        ByteArrayBuilder builder = new ByteArrayBuilder();
        parser._decodeBase64("A", builder, Base64Variants.MIME);
    }

    @Test(expected = JsonParseException.class)
    public void testDecodeBase64_withInvalidPadding_throwsJsonParseException() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();
        ByteArrayBuilder builder = new ByteArrayBuilder();
        parser._decodeBase64("QQ===", builder, Base64Variants.MIME);
    }

    @Test
    public void testDecodeBase64_withWhitespace_decodesCorrectly() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();
        ByteArrayBuilder builder = new ByteArrayBuilder();
        parser._decodeBase64(" SG Vsb G8= ", builder, Base64Variants.MIME);
        byte[] bytes = builder.toByteArray();
        assertEquals("Hello", new String(bytes, "UTF-8"));
    }

    @Test
    public void testWrapError_constructsJsonParseException() {
        MinimalParserStub parser = new MinimalParserStub();
        Throwable cause = new RuntimeException("root cause");
        JsonParseException ex = parser._wrapError("custom error", cause);
        assertNotNull(ex);
        assertTrue(ex.getMessage().contains("custom error"));
        assertEquals(cause, ex.getCause());
    }

    @Test
    public void testStubBigIntegerAndBigDecimalMethods() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();
        parser.setNumberValues(10, 100L, 5.5);
        assertEquals(BigInteger.valueOf(100L), parser.getBigIntegerValue());
        assertEquals(BigDecimal.valueOf(5.5), parser.getDecimalValue());
    }
}