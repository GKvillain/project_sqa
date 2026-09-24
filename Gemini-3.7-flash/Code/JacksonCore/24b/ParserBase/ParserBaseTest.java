package com.fasterxml.jackson.core.base;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ParserBaseTest {

    private static class TestableParserBase extends ParserBase {
        public TestableParserBase(IOContext ctxt, int features) {
            super(ctxt, features);
        }

        @Override
        protected void _closeInput() throws IOException {
            // No-op for test
        }

        @Override
        public JsonToken nextToken() throws IOException {
            return null;
        }

        @Override
        public String getText() throws IOException {
            return _textBuffer.contentsAsString();
        }

        @Override
        public char[] getTextCharacters() throws IOException {
            return _textBuffer.getTextBuffer();
        }

        @Override
        public int getTextLength() throws IOException {
            return _textBuffer.size();
        }

        @Override
        public int getTextOffset() throws IOException {
            return _textBuffer.getTextOffset();
        }

        @Override
        public ObjectCodec getCodec() {
            return null;
        }

        @Override
        public void setCodec(ObjectCodec c) {
        }

        // Expose protected methods for testing
        public boolean testLoadMoreGuaranteed() throws IOException {
            return loadMoreGuaranteed();
        }

        public void testReportMismatchedEndMarker(int actCh, char expCh) throws JsonParseException {
            _reportMismatchedEndMarker(actCh, expCh);
        }

        public char testDecodeCharForError(int c) throws JsonParseException {
            return _decodeCharForError(c);
        }

        public ByteArrayBuilder testGetByteArrayBuilder() {
            return _getByteArrayBuilder();
        }

        public void testReleaseBuffers() throws IOException {
            _releaseBuffers();
        }
    }

    private IOContext ioContext;
    private TestableParserBase parser;

    @Before
    public void setUp() {
        ioContext = new IOContext(new BufferRecycler(), "testSource", false);
        parser = new TestableParserBase(ioContext, 0);
    }

    // Tests enabling and disabling strict duplicate detection feature via enable/disable
    @Test
    public void testEnableDisableStrictDuplicateDetection() {
        assertNull(parser.getParsingContext().getDupDetector());

        parser.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        assertTrue(parser.isEnabled(JsonParser.Feature.STRICT_DUPLICATE_DETECTION));
        assertNotNull(parser.getParsingContext().getDupDetector());

        // Enabling again should keep dup detector active
        parser.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        assertNotNull(parser.getParsingContext().getDupDetector());

        parser.disable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        assertFalse(parser.isEnabled(JsonParser.Feature.STRICT_DUPLICATE_DETECTION));
        assertNull(parser.getParsingContext().getDupDetector());
    }

    // Tests changing feature mask with setFeatureMask
    @Test
    @SuppressWarnings("deprecation")
    public void testSetFeatureMask_duplicateDetection() {
        int maskWithDup = JsonParser.Feature.STRICT_DUPLICATE_DETECTION.getMask();
        
        parser.setFeatureMask(maskWithDup);
        assertTrue(parser.isEnabled(JsonParser.Feature.STRICT_DUPLICATE_DETECTION));
        assertNotNull(parser.getParsingContext().getDupDetector());

        // Disable via feature mask
        parser.setFeatureMask(0);
        assertFalse(parser.isEnabled(JsonParser.Feature.STRICT_DUPLICATE_DETECTION));
        assertNull(parser.getParsingContext().getDupDetector());
    }

    // Tests overrideStdFeatures enabling and disabling duplicate detection
    @Test
    public void testOverrideStdFeatures_duplicateDetection() {
        int mask = JsonParser.Feature.STRICT_DUPLICATE_DETECTION.getMask();

        parser.overrideStdFeatures(mask, mask);
        assertTrue(parser.isEnabled(JsonParser.Feature.STRICT_DUPLICATE_DETECTION));
        assertNotNull(parser.getParsingContext().getDupDetector());

        parser.overrideStdFeatures(0, mask);
        assertFalse(parser.isEnabled(JsonParser.Feature.STRICT_DUPLICATE_DETECTION));
        assertNull(parser.getParsingContext().getDupDetector());
    }

    // Tests parser version accessor
    @Test
    public void testVersion_returnsNonNull() {
        assertNotNull(parser.version());
        assertFalse(parser.version().isUnknownVersion());
    }

    // Tests current value get and set in parsing context
    @Test
    public void testCurrentValue_getAndSet() {
        assertNull(parser.getCurrentValue());
        String val = "customContextValue";
        parser.setCurrentValue(val);
        assertEquals(val, parser.getCurrentValue());
    }

    // Tests close and isClosed behavior
    @Test
    public void testClose_updatesClosedFlagAndPointers() throws IOException {
        assertFalse(parser.isClosed());
        parser._inputPtr = 2;
        parser._inputEnd = 5;

        parser.close();

        assertTrue(parser.isClosed());
        assertEquals(5, parser._inputPtr);

        // Multiple calls to close should be safe
        parser.close();
        assertTrue(parser.isClosed());
    }

    // Tests token location and current location getters
    @Test
    public void testLocations_returnsValidJsonLocations() {
        parser._currInputProcessed = 100;
        parser._inputPtr = 10;
        parser._currInputRow = 2;
        parser._currInputRowStart = 5;

        parser._tokenInputTotal = 95;
        parser._tokenInputRow = 2;
        parser._tokenInputCol = 3;

        JsonLocation tokenLoc = parser.getTokenLocation();
        assertNotNull(tokenLoc);
        assertEquals(95L, parser.getTokenCharacterOffset());
        assertEquals(2, parser.getTokenLineNr());
        assertEquals(4, parser.getTokenColumnNr());

        JsonLocation currentLoc = parser.getCurrentLocation();
        assertNotNull(currentLoc);
        assertEquals(110L, currentLoc.getCharOffset());
        assertEquals(2, currentLoc.getLineNr());
        assertEquals(6, currentLoc.getColumnNr());
    }

    // Tests numeric parsing with simple int value
    @Test
    public void testNumericParsing_simpleInt() throws IOException {
        parser._textBuffer.resetWithShared(new char[]{'1', '2', '3'}, 0, 3);
        parser._currToken = parser.resetInt(false, 3);

        assertEquals(123, parser.getIntValue());
        assertEquals(123L, parser.getLongValue());
        assertEquals(123.0, parser.getDoubleValue(), 0.00001);
        assertEquals(123.0f, parser.getFloatValue(), 0.00001f);
        assertEquals(BigInteger.valueOf(123), parser.getBigIntegerValue());
        assertEquals(BigDecimal.valueOf(123), parser.getDecimalValue());
        assertEquals(123, parser.getNumberValue());
        assertEquals(JsonParser.NumberType.INT, parser.getNumberType());
    }

    // Tests numeric parsing with negative long value
    @Test
    public void testNumericParsing_negativeLong() throws IOException {
        String valStr = "-1234567890123";
        parser._textBuffer.resetWithString(valStr);
        parser._currToken = parser.resetInt(true, 13);

        assertEquals(-1234567890123L, parser.getLongValue());
        assertEquals(-1234567890123L, parser.getNumberValue());
        assertEquals(JsonParser.NumberType.LONG, parser.getNumberType());
        assertEquals(BigInteger.valueOf(-1234567890123L), parser.getBigIntegerValue());
    }

    // Tests numeric parsing with large BigInteger value
    @Test
    public void testNumericParsing_bigInteger() throws IOException {
        String bigIntStr = "123456789012345678901234567890";
        parser._textBuffer.resetWithString(bigIntStr);
        parser._currToken = parser.resetInt(false, bigIntStr.length());

        BigInteger expected = new BigInteger(bigIntStr);
        assertEquals(expected, parser.getBigIntegerValue());
        assertEquals(expected, parser.getNumberValue());
        assertEquals(JsonParser.NumberType.BIG_INTEGER, parser.getNumberType());
    }

    // Tests numeric parsing with float/double/decimal value
    @Test
    public void testNumericParsing_floatAndDecimal() throws IOException {
        String floatStr = "123.45";
        parser._textBuffer.resetWithString(floatStr);
        parser._currToken = parser.resetFloat(false, 3, 2, 0);

        assertEquals(123.45, parser.getDoubleValue(), 0.00001);
        assertEquals(JsonParser.NumberType.DOUBLE, parser.getNumberType());

        BigDecimal expectedDecimal = new BigDecimal("123.45");
        assertEquals(expectedDecimal, parser.getDecimalValue());
        assertEquals(JsonParser.NumberType.BIG_DECIMAL, parser.getNumberType());
        assertEquals(expectedDecimal, parser.getNumberValue());
        assertEquals(123, parser.getIntValue());
        assertEquals(123L, parser.getLongValue());
    }

    // Tests isNaN handling for float tokens
    @Test
    public void testIsNaN_detection() {
        assertFalse(parser.isNaN());

        parser._currToken = parser.resetAsNaN("NaN", Double.NaN);
        assertTrue(parser.isNaN());

        parser._currToken = parser.resetAsNaN("Infinity", Double.POSITIVE_INFINITY);
        assertTrue(parser.isNaN());

        parser._currToken = parser.resetAsNaN("-Infinity", Double.NEGATIVE_INFINITY);
        assertTrue(parser.isNaN());
    }

    // Tests hasTextCharacters for various tokens
    @Test
    public void testHasTextCharacters_tokenChecks() {
        parser._currToken = JsonToken.VALUE_STRING;
        assertTrue(parser.hasTextCharacters());

        parser._currToken = JsonToken.FIELD_NAME;
        parser._nameCopied = false;
        assertFalse(parser.hasTextCharacters());

        parser._nameCopied = true;
        assertTrue(parser.hasTextCharacters());

        parser._currToken = JsonToken.VALUE_NUMBER_INT;
        assertFalse(parser.hasTextCharacters());
    }

    // Tests binary value parsing when current token is not string throws exception
    @Test(expected = JsonParseException.class)
    public void testGetBinaryValue_nonStringToken_throwsException() throws IOException {
        parser._currToken = JsonToken.VALUE_NUMBER_INT;
        parser.getBinaryValue(Base64Variants.getDefaultVariant());
    }

    // Tests binary value decoding with valid base64 string
    @Test
    public void testGetBinaryValue_validStringToken() throws IOException {
        parser._currToken = JsonToken.VALUE_STRING;
        parser._textBuffer.resetWithString("AQID"); // [1, 2, 3] in Base64

        byte[] result = parser.getBinaryValue(Base64Variants.MIME);
        assertNotNull(result);
        assertArrayEquals(new byte[]{1, 2, 3}, result);
    }

    // Tests int conversion overflow detection from long
    @Test(expected = JsonParseException.class)
    public void testConvertNumberToInt_overflow_throwsException() throws IOException {
        parser._textBuffer.resetWithString("999999999999");
        parser._currToken = parser.resetInt(false, 12);
        parser.getIntValue();
    }

    // Tests growArrayBy utility method
    @Test
    public void testGrowArrayBy_expandsCorrectly() {
        int[] original = new int[]{1, 2, 3};
        int[] grown = ParserBase.growArrayBy(original, 2);
        assertEquals(5, grown.length);
        assertEquals(1, grown[0]);
        assertEquals(2, grown[1]);
        assertEquals(3, grown[2]);
        assertEquals(0, grown[3]);
        assertEquals(0, grown[4]);

        int[] fromNull = ParserBase.growArrayBy(null, 3);
        assertEquals(3, fromNull.length);
    }

    // Tests unrecognized character escape handling
    @Test
    public void testHandleUnrecognizedCharacterEscape_withFeatureEnabled() throws Exception {
        parser.enable(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
        char result = parser._handleUnrecognizedCharacterEscape('x');
        assertEquals('x', result);

        parser.disable(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
        parser.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        char resultQuote = parser._handleUnrecognizedCharacterEscape('\'');
        assertEquals('\'', resultQuote);
    }

    // Tests unrecognized character escape exception when feature disabled
    @Test(expected = JsonProcessingException.class)
    public void testHandleUnrecognizedCharacterEscape_throwsException() throws Exception {
        parser.disable(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
        parser.disable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        parser._handleUnrecognizedCharacterEscape('z');
    }

    // Tests general reset method routing to int or float
    @Test
    public void testReset_dispatch() {
        JsonToken intToken = parser.reset(false, 4, 0, 0);
        assertEquals(JsonToken.VALUE_NUMBER_INT, intToken);

        JsonToken floatToken = parser.reset(true, 2, 3, 1);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, floatToken);
    }

    // Tests current name handling and overriding
    @Test
    public void testCurrentName_andOverrideCurrentName() throws IOException {
        assertNull(parser.getCurrentName());

        parser.overrideCurrentName("overriddenName");
        assertEquals("overriddenName", parser.getCurrentName());
    }

    // Tests clearCurrentToken and getLastClearedToken
    @Test
    public void testClearCurrentToken_andGetLastClearedToken() {
        assertNull(parser.getLastClearedToken());
        parser._currToken = JsonToken.VALUE_STRING;

        parser.clearCurrentToken();
        assertNull(parser.getCurrentToken());
        assertEquals(JsonToken.VALUE_STRING, parser.getLastClearedToken());
    }

    // Tests loadMoreGuaranteed throwing EOF exception when loadMore returns false
    @Test(expected = JsonParseException.class)
    public void testLoadMoreGuaranteed_throwsEOFException() throws IOException {
        parser.testLoadMoreGuaranteed();
    }

    // Tests readBinaryValue writing to OutputStream
    @Test
    public void testReadBinaryValue_toOutputStream() throws IOException {
        parser._currToken = JsonToken.VALUE_STRING;
        parser._textBuffer.resetWithString("AQIDBA=="); // [1, 2, 3, 4]

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bytesRead = parser.readBinaryValue(Base64Variants.MIME, out);

        assertEquals(4, bytesRead);
        assertArrayEquals(new byte[]{1, 2, 3, 4}, out.toByteArray());
    }

    // Tests numeric parsing when current token is not a numeric token throws exception
    @Test(expected = JsonParseException.class)
    public void testNumericParsing_invalidToken_throwsException() throws IOException {
        parser._currToken = JsonToken.START_OBJECT;
        parser.getIntValue();
    }

    // Tests long conversion overflow detection from BigInteger
    @Test(expected = JsonParseException.class)
    public void testConvertNumberToLong_overflow_throwsException() throws IOException {
        String hugeNumber = "123456789012345678901234567890";
        parser._textBuffer.resetWithString(hugeNumber);
        parser._currToken = parser.resetInt(false, hugeNumber.length());
        parser.getLongValue();
    }

    // Tests conversion to BigInteger from Double
    @Test
    public void testConvertNumberToBigInteger_fromDouble() throws IOException {
        parser._textBuffer.resetWithString("123.45");
        parser._currToken = parser.resetFloat(false, 3, 2, 0);

        BigInteger bigInt = parser.getBigIntegerValue();
        assertEquals(BigInteger.valueOf(123), bigInt);
    }

    // Tests conversion to BigDecimal from NaN / Infinity
    @Test(expected = NumberFormatException.class)
    public void testConvertNumberToBigDecimal_fromNaN_throwsException() throws IOException {
        parser._currToken = parser.resetAsNaN("NaN", Double.NaN);
        parser.getDecimalValue();
    }

    // Tests mismatched end marker reporting
    @Test(expected = JsonParseException.class)
    public void testReportMismatchedEndMarker_throwsException() throws JsonParseException {
        parser.testReportMismatchedEndMarker(']', '}');
    }

    // Tests character decoding for error messages
    @Test
    public void testDecodeCharForError() throws JsonParseException {
        char printable = parser.testDecodeCharForError('a');
        assertEquals('a', printable);

        char controlChar = parser.testDecodeCharForError(0);
        assertEquals('\0', controlChar);
    }

    // Tests byteArrayBuilder allocation and buffer release
    @Test
    public void testGetByteArrayBuilder_andReleaseBuffers() throws IOException {
        ByteArrayBuilder builder = parser.testGetByteArrayBuilder();
        assertNotNull(builder);

        // Calling again should reuse or return a valid builder
        ByteArrayBuilder builder2 = parser.testGetByteArrayBuilder();
        assertNotNull(builder2);

        parser.testReleaseBuffers();
    }

    // Tests hasTextCharacters on other tokens like null and embedded objects
    @Test
    public void testHasTextCharacters_nullAndOtherTokens() {
        parser._currToken = null;
        assertFalse(parser.hasTextCharacters());

        parser._currToken = JsonToken.VALUE_EMBEDDED_OBJECT;
        assertFalse(parser.hasTextCharacters());

        parser._currToken = JsonToken.VALUE_TRUE;
        assertFalse(parser.hasTextCharacters());
    }
}