package com.fasterxml.jackson.databind.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import static org.junit.Assert.*;

public class TokenBufferTest {

    private final JsonFactory JSON_F = new JsonFactory();
    private final ObjectMapper MAPPER = new ObjectMapper();

    // Tests simple write and parse sequence for scalar values
    @Test
    public void testSimpleWriteAndRead_scalarValues_parsedCorrectly() throws IOException {
        TokenBuffer buf = new TokenBuffer(MAPPER, false);
        buf.writeStartObject();
        buf.writeFieldName("str");
        buf.writeString("value");
        buf.writeFieldName("int");
        buf.writeNumber(42);
        buf.writeFieldName("long");
        buf.writeNumber(12345678901L);
        buf.writeFieldName("boolTrue");
        buf.writeBoolean(true);
        buf.writeFieldName("boolFalse");
        buf.writeBoolean(false);
        buf.writeFieldName("nullVal");
        buf.writeNull();
        buf.writeEndObject();
        buf.close();

        assertTrue(buf.isClosed());
        assertEquals(JsonToken.START_OBJECT, buf.firstToken());

        JsonParser p = buf.asParser();
        assertFalse(p.isClosed());

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("str", p.nextFieldName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("value", p.getText());
        assertEquals("str", p.getCurrentName());

        assertEquals("int", p.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getIntValue());
        assertEquals(JsonParser.NumberType.INT, p.getNumberType());

        assertEquals("long", p.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(12345678901L, p.getLongValue());
        assertEquals(JsonParser.NumberType.LONG, p.getNumberType());

        assertEquals("boolTrue", p.nextFieldName());
        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());

        assertEquals("boolFalse", p.nextFieldName());
        assertEquals(JsonToken.VALUE_FALSE, p.nextToken());

        assertEquals("nullVal", p.nextFieldName());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        assertTrue(p.isClosed());
    }

    // Tests segment boundaries crossing Segment.TOKENS_PER_SEGMENT (16 tokens)
    @Test
    public void testSegmentOverflow_moreThan16Tokens_readAcrossSegments() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartArray();
        for (int i = 0; i < 35; i++) {
            buf.writeNumber(i);
        }
        buf.writeEndArray();

        JsonParser p = buf.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        for (int i = 0; i < 35; i++) {
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(i, p.getIntValue());
        }
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    // Tests peekNextToken functionality before and across segment boundaries
    @Test
    public void testPeekNextToken_variousTokens_correctPeeking() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartArray();
        buf.writeString("first");
        buf.writeEndArray();

        TokenBuffer.Parser p = (TokenBuffer.Parser) buf.asParser();
        assertEquals(JsonToken.START_ARRAY, p.peekNextToken());
        assertEquals(JsonToken.START_ARRAY, p.nextToken());

        assertEquals(JsonToken.VALUE_STRING, p.peekNextToken());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());

        assertEquals(JsonToken.END_ARRAY, p.peekNextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());

        assertNull(p.peekNextToken());
        assertNull(p.nextToken());
        p.close();
        assertNull(p.peekNextToken());
    }

    // Tests number types conversion including float, double, BigInteger, and BigDecimal
    @Test
    public void testNumberTypes_floatingPointAndBigNumbers_correctValuesAndTypes() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartArray();
        buf.writeNumber(1.5f);
        buf.writeNumber(3.1415926535);
        buf.writeNumber(new BigInteger("123456789012345678901234567890"));
        buf.writeNumber(new BigDecimal("123.45678901234567890"));
        buf.writeNumber((short) 7);
        buf.writeNumber("987.65");
        buf.writeEndArray();

        JsonParser p = buf.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(1.5f, p.getFloatValue(), 0.001f);
        assertEquals(JsonParser.NumberType.FLOAT, p.getNumberType());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(3.1415926535, p.getDoubleValue(), 0.00000001);
        assertEquals(JsonParser.NumberType.DOUBLE, p.getNumberType());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(new BigInteger("123456789012345678901234567890"), p.getBigIntegerValue());
        assertEquals(JsonParser.NumberType.BIG_INTEGER, p.getNumberType());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(new BigDecimal("123.45678901234567890"), p.getDecimalValue());
        assertEquals(JsonParser.NumberType.BIG_DECIMAL, p.getNumberType());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(7, p.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(987.65, p.getDoubleValue(), 0.001);

        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    // Tests forceUseOfBigDecimal behavior when copying float events
    @Test
    public void testForceUseOfBigDecimal_floatInput_copiedAsBigDecimal() throws IOException {
        JsonParser src = JSON_F.createParser("{\"val\": 1.25}");
        src.nextToken(); // START_OBJECT
        src.nextToken(); // FIELD_NAME
        src.nextToken(); // VALUE_NUMBER_FLOAT

        TokenBuffer buf = new TokenBuffer(src, null);
        buf.forceUseOfBigDecimal(true);
        buf.copyCurrentEvent(src);

        JsonParser p = buf.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(new BigDecimal("1.25"), p.getDecimalValue());
        assertEquals(JsonParser.NumberType.BIG_DECIMAL, p.getNumberType());
        src.close();
        p.close();
    }

    // Tests native type IDs and object IDs buffering and serialization
    @Test
    public void testNativeIds_typeAndObjectId_storedAndRetrieved() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, true);
        assertTrue(buf.canWriteTypeId());
        assertTrue(buf.canWriteObjectId());

        buf.writeTypeId("myTypeId");
        buf.writeObjectId("myObjectId");
        buf.writeStartObject();
        buf.writeFieldName("a");
        buf.writeNumber(1);
        buf.writeEndObject();

        TokenBuffer.Parser p = (TokenBuffer.Parser) buf.asParser();
        assertTrue(p.canReadTypeId());
        assertTrue(p.canReadObjectId());

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("myTypeId", p.getTypeId());
        assertEquals("myObjectId", p.getObjectId());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertNull(p.getTypeId());
        assertNull(p.getObjectId());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    // Tests serialize method reproducing all buffered tokens to target generator
    @Test
    public void testSerialize_allTokenTypes_writtenToTargetGenerator() throws IOException {
        TokenBuffer src = new TokenBuffer(null, false);
        src.writeStartObject();
        src.writeFieldName(new SerializedString("field1"));
        src.writeString(new SerializedString("strValue"));
        src.writeFieldName("numInt");
        src.writeNumber(100);
        src.writeFieldName("numBigInt");
        src.writeNumber(BigInteger.valueOf(200));
        src.writeFieldName("numLong");
        src.writeNumber(300L);
        src.writeFieldName("numShort");
        src.writeNumber((short) 400);
        src.writeFieldName("numDouble");
        src.writeNumber(5.5);
        src.writeFieldName("numBigDecimal");
        src.writeNumber(new BigDecimal("6.6"));
        src.writeFieldName("numFloat");
        src.writeNumber(7.7f);
        src.writeFieldName("numStr");
        src.writeNumber("8.8");
        src.writeFieldName("boolT");
        src.writeBoolean(true);
        src.writeFieldName("boolF");
        src.writeBoolean(false);
        src.writeFieldName("nullField");
        src.writeNull();
        src.writeFieldName("embedded");
        src.writeObject(new byte[]{1, 2, 3});
        src.writeFieldName("arr");
        src.writeStartArray();
        src.writeEndArray();
        src.writeEndObject();

        TokenBuffer target = new TokenBuffer(null, false);
        src.serialize(target);

        JsonParser p = target.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("field1", p.nextFieldName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("strValue", p.getText());
        assertEquals("numInt", p.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(100, p.getIntValue());
        assertEquals("numBigInt", p.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(200, p.getIntValue());
        assertEquals("numLong", p.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(300L, p.getLongValue());
        assertEquals("numShort", p.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(400, p.getIntValue());
        assertEquals("numDouble", p.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(5.5, p.getDoubleValue(), 0.01);
        assertEquals("numBigDecimal", p.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(new BigDecimal("6.6"), p.getDecimalValue());
        assertEquals("numFloat", p.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(7.7f, p.getFloatValue(), 0.01f);
        assertEquals("numStr", p.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals("boolT", p.nextFieldName());
        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        assertEquals("boolF", p.nextFieldName());
        assertEquals(JsonToken.VALUE_FALSE, p.nextToken());
        assertEquals("nullField", p.nextFieldName());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertEquals("embedded", p.nextFieldName());
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertArrayEquals(new byte[]{1, 2, 3}, (byte[]) p.getEmbeddedObject());
        assertEquals("arr", p.nextFieldName());
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    // Tests append method concatenating two TokenBuffers
    @Test
    public void testAppend_twoBuffers_concatenatedContents() throws IOException {
        TokenBuffer buf1 = new TokenBuffer(null, false);
        buf1.writeNumber(1);

        TokenBuffer buf2 = new TokenBuffer(null, false);
        buf2.writeNumber(2);

        buf1.append(buf2);

        JsonParser p = buf1.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertNull(p.nextToken());
        p.close();
    }

    // Tests binary data writing and decoding
    @Test
    public void testBinary_writeAndGetBinaryValue_matchingData() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, false);
        assertTrue(buf.canWriteBinaryNatively());
        byte[] data = new byte[]{10, 20, 30, 40, 50};
        buf.writeBinary(Base64Variants.MIME, data, 1, 3); // writes [20, 30, 40]

        JsonParser p = buf.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        byte[] retrieved = p.getBinaryValue();
        assertArrayEquals(new byte[]{20, 30, 40}, retrieved);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int count = p.readBinaryValue(Base64Variants.MIME, baos);
        assertEquals(3, count);
        assertArrayEquals(new byte[]{20, 30, 40}, baos.toByteArray());
        p.close();
    }

    // Tests binary parsing from string token
    @Test
    public void testBinary_fromStringToken_decodedProperly() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeString("AQID"); // Base64 for byte[]{1, 2, 3}

        JsonParser p = buf.asParser();
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        byte[] retrieved = p.getBinaryValue(Base64Variants.MIME);
        assertArrayEquals(new byte[]{1, 2, 3}, retrieved);
        p.close();
    }

    // Tests copyCurrentStructure for nested object and array hierarchies
    @Test
    public void testCopyCurrentStructure_nestedStructure_copiedAccurately() throws IOException {
        JsonParser p = JSON_F.createParser("{\"arr\":[1,{\"k\":\"v\"}],\"num\":99}");
        p.nextToken(); // START_OBJECT

        TokenBuffer buf = new TokenBuffer(null, false);
        buf.copyCurrentStructure(p);

        JsonParser reader = buf.asParser();
        assertEquals(JsonToken.START_OBJECT, reader.nextToken());
        assertEquals("arr", reader.nextFieldName());
        assertEquals(JsonToken.START_ARRAY, reader.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, reader.nextToken());
        assertEquals(1, reader.getIntValue());
        assertEquals(JsonToken.START_OBJECT, reader.nextToken());
        assertEquals("k", reader.nextFieldName());
        assertEquals(JsonToken.VALUE_STRING, reader.nextToken());
        assertEquals("v", reader.getText());
        assertEquals(JsonToken.END_OBJECT, reader.nextToken());
        assertEquals(JsonToken.END_ARRAY, reader.nextToken());
        assertEquals("num", reader.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, reader.nextToken());
        assertEquals(99, reader.getIntValue());
        assertEquals(JsonToken.END_OBJECT, reader.nextToken());
        assertNull(reader.nextToken());

        p.close();
        reader.close();
    }

    // Tests overrideCurrentName and getCurrentName inside structure
    @Test
    public void testOverrideCurrentName_insideObject_updatesName() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartObject();
        buf.writeFieldName("original");
        buf.writeString("val");
        buf.writeEndObject();

        JsonParser p = buf.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("original", p.getCurrentName());
        p.overrideCurrentName("modified");
        assertEquals("modified", p.getCurrentName());
        p.close();
    }

    // Tests text methods: getTextCharacters, getTextLength, getTextOffset, hasTextCharacters
    @Test
    public void testTextMethods_stringValue_returnsCorrectCharInfo() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeString("hello");

        JsonParser p = buf.asParser();
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("hello", p.getText());
        assertArrayEquals("hello".toCharArray(), p.getTextCharacters());
        assertEquals(5, p.getTextLength());
        assertEquals(0, p.getTextOffset());
        assertFalse(p.hasTextCharacters());
        p.close();
    }

    // Tests toString formatting with small and large token sequences
    @Test
    public void testToString_truncatedTokens_containsExpectedTokens() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartObject();
        buf.writeFieldName("name");
        buf.writeString("test");
        buf.writeEndObject();

        String str = buf.toString();
        assertTrue(str.contains("START_OBJECT"));
        assertTrue(str.contains("FIELD_NAME(name)"));
        assertTrue(str.contains("VALUE_STRING"));
        assertTrue(str.contains("END_OBJECT"));

        TokenBuffer largeBuf = new TokenBuffer(null, false);
        largeBuf.writeStartArray();
        for (int i = 0; i < 110; i++) {
            largeBuf.writeNumber(i);
        }
        largeBuf.writeEndArray();

        String largeStr = largeBuf.toString();
        assertTrue(largeStr.contains("truncated"));
    }

    // Tests raw value writing and serialization
    @Test
    public void testWriteRawValue_stringAndCharArray_buffersEmbeddedObject() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeRawValue("{\"raw\":1}");
        char[] chars = "{\"chars\":2}".toCharArray();
        buf.writeRawValue(chars, 0, chars.length);

        JsonParser p = buf.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertNotNull(p.getEmbeddedObject());
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertEquals("{\"chars\":2}", p.getEmbeddedObject());
        p.close();
    }

    // Tests generator feature toggles and version
    @Test
    public void testGeneratorFeaturesAndVersion_modifyAndCheckMask_updatedState() {
        TokenBuffer buf = new TokenBuffer(null, false);
        assertNotNull(buf.version());

        buf.enable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertTrue(buf.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));

        buf.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertFalse(buf.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));

        buf.setFeatureMask(123);
        assertEquals(123, buf.getFeatureMask());

        assertSame(buf, buf.useDefaultPrettyPrinter());
    }

    // Tests unsupported operations throw UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRaw_unsupportedOperation_throwsException() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeRaw("raw text");
    }

    // Tests null writes for numbers and strings emit VALUE_NULL
    @Test
    public void testNullHandling_nullValues_writtenAsNullTokens() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeString((String) null);
        buf.writeNumber((BigDecimal) null);
        buf.writeNumber((BigInteger) null);
        buf.writeObject(null);
        buf.writeTree(null);

        JsonParser p = buf.asParser();
        for (int i = 0; i < 5; i++) {
            assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        }
        assertNull(p.nextToken());
        p.close();
    }
}