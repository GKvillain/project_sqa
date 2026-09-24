package com.fasterxml.jackson.core.base;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.exc.InputCoercionException;
import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;

public class ParserMinimalBaseTest {

    private static class MinimalParserStub extends ParserMinimalBase {
        private String _text;
        private String _currentName;
        private int _intValue;
        private long _longValue;
        private double _doubleValue;
        private Object _embeddedObject;
        private boolean _closed;
        private JsonToken[] _tokensToYield;
        private int _tokenIndex = 0;

        public MinimalParserStub() {
            super();
        }

        public MinimalParserStub(int features) {
            super(features);
        }

        public void setTokens(JsonToken... tokens) {
            _tokensToYield = tokens;
            _tokenIndex = 0;
        }

        public void setCurrentToken(JsonToken t) {
            _currToken = t;
        }

        public void setText(String t) {
            _text = t;
        }

        public void setCurrentName(String n) {
            _currentName = n;
        }

        public void setIntValue(int v) {
            _intValue = v;
        }

        public void setLongValue(long v) {
            _longValue = v;
        }

        public void setDoubleValue(double v) {
            _doubleValue = v;
        }

        public void setEmbeddedObject(Object obj) {
            _embeddedObject = obj;
        }

        @Override
        public JsonToken nextToken() throws IOException {
            if (_tokensToYield != null && _tokenIndex < _tokensToYield.length) {
                _currToken = _tokensToYield[_tokenIndex++];
                return _currToken;
            }
            return null;
        }

        @Override
        protected void _handleEOF() throws JsonParseException {
            _reportInvalidEOF();
        }

        @Override
        public String getCurrentName() throws IOException {
            return _currentName;
        }

        @Override
        public void close() throws IOException {
            _closed = true;
        }

        @Override
        public boolean isClosed() {
            return _closed;
        }

        @Override
        public JsonStreamContext getParsingContext() {
            return null;
        }

        @Override
        public void overrideCurrentName(String name) {
            _currentName = name;
        }

        @Override
        public String getText() throws IOException {
            return _text;
        }

        @Override
        public char[] getTextCharacters() throws IOException {
            return _text == null ? null : _text.toCharArray();
        }

        @Override
        public boolean hasTextCharacters() {
            return _text != null;
        }

        @Override
        public int getTextLength() throws IOException {
            return _text == null ? 0 : _text.length();
        }

        @Override
        public int getTextOffset() throws IOException {
            return 0;
        }

        @Override
        public byte[] getBinaryValue(Base64Variant b64variant) throws IOException {
            ByteArrayBuilder bb = new ByteArrayBuilder();
            _decodeBase64(getText(), bb, b64variant);
            return bb.toByteArray();
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
        public int getIntValue() throws IOException {
            return _intValue;
        }

        @Override
        public long getLongValue() throws IOException {
            return _longValue;
        }

        @Override
        public BigInteger getBigIntegerValue() throws IOException {
            return BigInteger.valueOf(_longValue);
        }

        @Override
        public float getFloatValue() throws IOException {
            return (float) _doubleValue;
        }

        @Override
        public double getDoubleValue() throws IOException {
            return _doubleValue;
        }

        @Override
        public BigDecimal getDecimalValue() throws IOException {
            return BigDecimal.valueOf(_doubleValue);
        }

        @Override
        public NumberType getNumberType() throws IOException {
            return NumberType.INT;
        }

        @Override
        public Number getNumberValue() throws IOException {
            return _intValue;
        }

        @Override
        public Object getEmbeddedObject() throws IOException {
            return _embeddedObject;
        }

        @Override
        public JsonLocation getTokenLocation() {
            return JsonLocation.NA;
        }

        @Override
        public JsonLocation getCurrentLocation() {
            return JsonLocation.NA;
        }
    }

    // Tests initial token state and token querying methods
    @Test
    public void testTokenQueries_initialAndAfterSet_returnsCorrectState() {
        MinimalParserStub parser = new MinimalParserStub();
        assertNull(parser.currentToken());
        assertEquals(JsonTokenId.ID_NO_TOKEN, parser.currentTokenId());
        assertNull(parser.getCurrentToken());
        assertEquals(JsonTokenId.ID_NO_TOKEN, parser.getCurrentTokenId());
        assertFalse(parser.hasCurrentToken());
        assertTrue(parser.hasTokenId(JsonTokenId.ID_NO_TOKEN));
        assertFalse(parser.hasTokenId(JsonTokenId.ID_START_OBJECT));
        assertFalse(parser.hasToken(JsonToken.START_OBJECT));
        assertFalse(parser.isExpectedStartArrayToken());
        assertFalse(parser.isExpectedStartObjectToken());

        parser.setCurrentToken(JsonToken.START_ARRAY);
        assertTrue(parser.isExpectedStartArrayToken());
        assertFalse(parser.isExpectedStartObjectToken());
        assertEquals(JsonToken.START_ARRAY, parser.currentToken());
        assertEquals(JsonTokenId.ID_START_ARRAY, parser.currentTokenId());
        assertTrue(parser.hasToken(JsonToken.START_ARRAY));
        assertTrue(parser.hasTokenId(JsonTokenId.ID_START_ARRAY));

        parser.setCurrentToken(JsonToken.START_OBJECT);
        assertTrue(parser.isExpectedStartObjectToken());
        assertFalse(parser.isExpectedStartArrayToken());
    }

    // Tests clearing and restoring last cleared token
    @Test
    public void testClearCurrentToken_tokenSet_clearsAndRecordsLastToken() {
        MinimalParserStub parser = new MinimalParserStub();
        parser.setCurrentToken(JsonToken.VALUE_STRING);
        assertEquals(JsonToken.VALUE_STRING, parser.currentToken());
        assertNull(parser.getLastClearedToken());

        parser.clearCurrentToken();
        assertNull(parser.currentToken());
        assertEquals(JsonToken.VALUE_STRING, parser.getLastClearedToken());

        // Calling clear again when null does not overwrite last cleared
        parser.clearCurrentToken();
        assertEquals(JsonToken.VALUE_STRING, parser.getLastClearedToken());
    }

    // Tests nextValue when next token is FIELD_NAME
    @Test
    public void testNextValue_fieldName_advancesToFieldValue() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();
        parser.setTokens(JsonToken.FIELD_NAME, JsonToken.VALUE_STRING);
        JsonToken val = parser.nextValue();
        assertEquals(JsonToken.VALUE_STRING, val);
    }

    // Tests skipChildren with nested objects and arrays
    @Test
    public void testSkipChildren_nestedStructures_skipsToEnd() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();
        parser.setTokens(
            JsonToken.START_OBJECT,
            JsonToken.FIELD_NAME,
            JsonToken.START_ARRAY,
            JsonToken.VALUE_NUMBER_INT,
            JsonToken.END_ARRAY,
            JsonToken.END_OBJECT
        );

        parser.nextToken(); // START_OBJECT
        parser.skipChildren();
        assertEquals(JsonToken.END_OBJECT, parser.currentToken());
    }

    // Tests skipChildren when current token is not a container
    @Test
    public void testSkipChildren_scalarToken_doesNothing() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();
        parser.setCurrentToken(JsonToken.VALUE_NUMBER_INT);
        JsonParser res = parser.skipChildren();
        assertSame(parser, res);
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.currentToken());
    }

    // Tests skipChildren with unexpected EOF
    @Test(expected = JsonParseException.class)
    public void testSkipChildren_unexpectedEOF_throwsException() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();
        parser.setTokens(JsonToken.START_OBJECT);
        parser.nextToken();
        parser.skipChildren();
    }

    // Tests getValueAsBoolean with various token types
    @Test
    public void testGetValueAsBoolean_variousTokens_returnsExpectedCoercion() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();
        assertTrue(parser.getValueAsBoolean(true));
        assertFalse(parser.getValueAsBoolean(false));

        parser.setCurrentToken(JsonToken.VALUE_TRUE);
        assertTrue(parser.getValueAsBoolean(false));

        parser.setCurrentToken(JsonToken.VALUE_FALSE);
        assertFalse(parser.getValueAsBoolean(true));

        parser.setCurrentToken(JsonToken.VALUE_NULL);
        assertFalse(parser.getValueAsBoolean(true));

        parser.setCurrentToken(JsonToken.VALUE_NUMBER_INT);
        parser.setIntValue(1);
        assertTrue(parser.getValueAsBoolean(false));
        parser.setIntValue(0);
        assertFalse(parser.getValueAsBoolean(true));

        parser.setCurrentToken(JsonToken.VALUE_STRING);
        parser.setText("true");
        assertTrue(parser.getValueAsBoolean(false));
        parser.setText("false");
        assertFalse(parser.getValueAsBoolean(true));
        parser.setText("null");
        assertFalse(parser.getValueAsBoolean(true));
        parser.setText("other");
        assertTrue(parser.getValueAsBoolean(true));

        parser.setCurrentToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        parser.setEmbeddedObject(Boolean.TRUE);
        assertTrue(parser.getValueAsBoolean(false));
        parser.setEmbeddedObject("not-boolean");
        assertFalse(parser.getValueAsBoolean(false));
    }

    // Tests getValueAsInt with various token types
    @Test
    public void testGetValueAsInt_variousTokens_returnsExpectedCoercion() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();
        assertEquals(42, parser.getValueAsInt(42));

        parser.setCurrentToken(JsonToken.VALUE_NUMBER_INT);
        parser.setIntValue(123);
        assertEquals(123, parser.getValueAsInt());
        assertEquals(123, parser.getValueAsInt(99));

        parser.setCurrentToken(JsonToken.VALUE_NUMBER_FLOAT);
        parser.setIntValue(456);
        assertEquals(456, parser.getValueAsInt());

        parser.setCurrentToken(JsonToken.VALUE_TRUE);
        assertEquals(1, parser.getValueAsInt(0));

        parser.setCurrentToken(JsonToken.VALUE_FALSE);
        assertEquals(0, parser.getValueAsInt(1));

        parser.setCurrentToken(JsonToken.VALUE_NULL);
        assertEquals(0, parser.getValueAsInt(1));

        parser.setCurrentToken(JsonToken.VALUE_STRING);
        parser.setText("789");
        assertEquals(789, parser.getValueAsInt(0));
        parser.setText("null");
        assertEquals(0, parser.getValueAsInt(5));

        parser.setCurrentToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        parser.setEmbeddedObject(Integer.valueOf(321));
        assertEquals(321, parser.getValueAsInt(0));
    }

    // Tests getValueAsLong with various token types
    @Test
    public void testGetValueAsLong_variousTokens_returnsExpectedCoercion() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();
        assertEquals(55L, parser.getValueAsLong(55L));

        parser.setCurrentToken(JsonToken.VALUE_NUMBER_INT);
        parser.setLongValue(100L);
        assertEquals(100L, parser.getValueAsLong());
        assertEquals(100L, parser.getValueAsLong(1L));

        parser.setCurrentToken(JsonToken.VALUE_TRUE);
        assertEquals(1L, parser.getValueAsLong(0L));

        parser.setCurrentToken(JsonToken.VALUE_FALSE);
        assertEquals(0L, parser.getValueAsLong(1L));

        parser.setCurrentToken(JsonToken.VALUE_NULL);
        assertEquals(0L, parser.getValueAsLong(1L));

        parser.setCurrentToken(JsonToken.VALUE_STRING);
        parser.setText("1234567890123");
        assertEquals(1234567890123L, parser.getValueAsLong(0L));
        parser.setText("null");
        assertEquals(0L, parser.getValueAsLong(5L));

        parser.setCurrentToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        parser.setEmbeddedObject(Long.valueOf(999L));
        assertEquals(999L, parser.getValueAsLong(0L));
    }

    // Tests getValueAsDouble with various token types
    @Test
    public void testGetValueAsDouble_variousTokens_returnsExpectedCoercion() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();
        assertEquals(1.5, parser.getValueAsDouble(1.5), 0.001);

        parser.setCurrentToken(JsonToken.VALUE_NUMBER_FLOAT);
        parser.setDoubleValue(3.14);
        assertEquals(3.14, parser.getValueAsDouble(0.0), 0.001);

        parser.setCurrentToken(JsonToken.VALUE_NUMBER_INT);
        parser.setDoubleValue(42.0);
        assertEquals(42.0, parser.getValueAsDouble(0.0), 0.001);

        parser.setCurrentToken(JsonToken.VALUE_TRUE);
        assertEquals(1.0, parser.getValueAsDouble(0.0), 0.001);

        parser.setCurrentToken(JsonToken.VALUE_FALSE);
        assertEquals(0.0, parser.getValueAsDouble(1.0), 0.001);

        parser.setCurrentToken(JsonToken.VALUE_NULL);
        assertEquals(0.0, parser.getValueAsDouble(1.0), 0.001);

        parser.setCurrentToken(JsonToken.VALUE_STRING);
        parser.setText("2.718");
        assertEquals(2.718, parser.getValueAsDouble(0.0), 0.001);
        parser.setText("null");
        assertEquals(0.0, parser.getValueAsDouble(5.0), 0.001);

        parser.setCurrentToken(JsonToken.VALUE_EMBEDDED_OBJECT);
        parser.setEmbeddedObject(Double.valueOf(8.25));
        assertEquals(8.25, parser.getValueAsDouble(0.0), 0.001);
    }

    // Tests getValueAsString for different token states
    @Test
    public void testGetValueAsString_variousTokens_returnsExpectedString() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();
        assertNull(parser.getValueAsString());
        assertEquals("default", parser.getValueAsString("default"));

        parser.setCurrentToken(JsonToken.VALUE_STRING);
        parser.setText("hello");
        assertEquals("hello", parser.getValueAsString());
        assertEquals("hello", parser.getValueAsString("default"));

        parser.setCurrentToken(JsonToken.FIELD_NAME);
        parser.setCurrentName("fieldA");
        assertEquals("fieldA", parser.getValueAsString());
        assertEquals("fieldA", parser.getValueAsString("default"));

        parser.setCurrentToken(JsonToken.VALUE_NUMBER_INT);
        parser.setText("123");
        assertEquals("123", parser.getValueAsString());
        assertEquals("123", parser.getValueAsString("default"));

        parser.setCurrentToken(JsonToken.START_OBJECT);
        assertEquals("default", parser.getValueAsString("default"));
    }

    // Tests Base64 decoding helper
    @Test
    public void testDecodeBase64_validAndInvalidInput_decodesOrThrows() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();
        parser.setText("SGVsbG8=");
        byte[] bytes = parser.getBinaryValue(Base64Variants.MIME);
        assertEquals("Hello", new String(bytes, "UTF-8"));
    }

    // Tests Base64 decoding with invalid base64 string throws JsonParseException
    @Test(expected = JsonParseException.class)
    public void testDecodeBase64_invalidString_throwsJsonParseException() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();
        parser.setText("%%%InvalidBase64%%%");
        parser.getBinaryValue(Base64Variants.MIME);
    }

    // Tests character description utility helper
    @Test
    public void testGetCharDesc_controlAndHighAsciiChars_returnsFormattedDescription() {
        String ctrl = ParserMinimalBase._getCharDesc('\n');
        assertTrue(ctrl.contains("CTRL-CHAR"));
        assertTrue(ctrl.contains("10"));

        String normal = ParserMinimalBase._getCharDesc('A');
        assertEquals("'A' (code 65)", normal);

        String high = ParserMinimalBase._getCharDesc(0x1234);
        assertTrue(high.contains("0x1234"));
    }

    // Tests long integer and long number descriptions for length >= 1000 and < 1000
    @Test
    public void testLongIntegerAndNumberDesc_shortAndLong_formatsCorrectly() {
        MinimalParserStub parser = new MinimalParserStub();
        assertEquals("12345", parser._longIntegerDesc("12345"));
        assertEquals("12345.67", parser._longNumberDesc("12345.67"));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1005; i++) {
            sb.append('9');
        }
        String bigInt = sb.toString();
        String desc = parser._longIntegerDesc(bigInt);
        assertEquals("[Integer with 1005 digits]", desc);

        String negBigInt = "-" + bigInt;
        String negDesc = parser._longIntegerDesc(negBigInt);
        assertEquals("[Integer with 1005 digits]", negDesc);

        String negNumDesc = parser._longNumberDesc(negBigInt);
        assertEquals("[number with 1005 characters]", negNumDesc);
    }

    // Tests ascii bytes conversion helper
    @Test
    public void testAsciiHelpers_validAscii_convertsProperly() {
        byte[] b = ParserMinimalBase._asciiBytes("test");
        assertEquals(4, b.length);
        assertEquals('t', b[0]);
        assertEquals('e', b[1]);
        assertEquals('s', b[2]);
        assertEquals('t', b[3]);

        String s = ParserMinimalBase._ascii(b);
        assertEquals("test", s);
    }

    // Tests reportUnexpectedNumberChar throws JsonParseException
    @Test(expected = JsonParseException.class)
    public void testReportUnexpectedNumberChar_withComment_throwsJsonParseException() throws JsonParseException {
        MinimalParserStub parser = new MinimalParserStub();
        parser.reportUnexpectedNumberChar('x', "invalid exponent");
    }

    // Tests reportInvalidNumber throws JsonParseException
    @Test(expected = JsonParseException.class)
    public void testReportInvalidNumber_throwsJsonParseException() throws JsonParseException {
        MinimalParserStub parser = new MinimalParserStub();
        parser.reportInvalidNumber("leading zeros not allowed");
    }

    // Tests reportOverflowInt throws JsonParseException
    @Test(expected = JsonParseException.class)
    public void testReportOverflowInt_throwsException() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();
        parser.setText("99999999999999999");
        parser.reportOverflowInt();
    }

    // Tests reportOverflowLong throws JsonParseException
    @Test(expected = JsonParseException.class)
    public void testReportOverflowLong_throwsException() throws IOException {
        MinimalParserStub parser = new MinimalParserStub();
        parser.setText("99999999999999999999999999");
        parser.reportOverflowLong();
    }

    // Tests _reportInputCoercion throws InputCoercionException
    @Test(expected = InputCoercionException.class)
    public void testReportInputCoercion_throwsInputCoercionException() throws InputCoercionException {
        MinimalParserStub parser = new MinimalParserStub();
        parser._reportInputCoercion("Cannot coerce", JsonToken.VALUE_STRING, Integer.class);
    }

    // Tests _reportInvalidEOFInValue with different tokens
    @Test(expected = JsonEOFException.class)
    public void testReportInvalidEOFInValue_stringToken_throwsJsonEOFException() throws JsonParseException {
        MinimalParserStub parser = new MinimalParserStub();
        parser._reportInvalidEOFInValue(JsonToken.VALUE_STRING);
    }

    // Tests _throwInvalidSpace throws JsonParseException
    @Test(expected = JsonParseException.class)
    public void testThrowInvalidSpace_throwsJsonParseException() throws JsonParseException {
        MinimalParserStub parser = new MinimalParserStub();
        parser._throwInvalidSpace('\u00A0');
    }

    // Tests _reportMissingRootWS throws JsonParseException
    @Test(expected = JsonParseException.class)
    public void testReportMissingRootWS_throwsJsonParseException() throws JsonParseException {
        MinimalParserStub parser = new MinimalParserStub();
        parser._reportMissingRootWS('a');
    }
}