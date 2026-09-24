package com.fasterxml.jackson.databind.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.SerializedString;

public class TokenBufferTest {

    // Tests simple write and read of primitive tokens
    @Test
    public void testWriteAndReadPrimitives_standardTypes_matchesExpected() throws Exception {
        TokenBuffer buf = new TokenBuffer(null, false);

        buf.writeStartObject();
        buf.writeFieldName("intVal");
        buf.writeNumber(123);
        buf.writeFieldName("longVal");
        buf.writeNumber(1234567890123L);
        buf.writeFieldName("shortVal");
        buf.writeNumber((short) 12);
        buf.writeFieldName("doubleVal");
        buf.writeNumber(1.25);
        buf.writeFieldName("floatVal");
        buf.writeNumber(2.5f);
        buf.writeFieldName("boolTrue");
        buf.writeBoolean(true);
        buf.writeFieldName("boolFalse");
        buf.writeBoolean(false);
        buf.writeFieldName("nullVal");
        buf.writeNull();
        buf.writeEndObject();

        JsonParser parser = buf.asParser();
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("intVal", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        assertEquals(JsonParser.NumberType.INT, parser.getNumberType());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("longVal", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1234567890123L, parser.getLongValue());
        assertEquals(JsonParser.NumberType.LONG, parser.getNumberType());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("shortVal", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(12, parser.getIntValue());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("doubleVal", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(1.25, parser.getDoubleValue(), 0.0001);
        assertEquals(JsonParser.NumberType.DOUBLE, parser.getNumberType());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("floatVal", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(2.5f, parser.getFloatValue(), 0.0001f);
        assertEquals(JsonParser.NumberType.FLOAT, parser.getNumberType());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests BigInteger and BigDecimal numbers
    @Test
    public void testWriteAndReadBigNumbers_bigValues_matchesExpected() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);

        BigInteger bigInt = new BigInteger("123456789012345678901234567890");
        BigDecimal bigDec = new BigDecimal("12345678901234567890.1234567890");

        buf.writeStartArray();
        buf.writeNumber(bigInt);
        buf.writeNumber(bigDec);
        buf.writeNumber((BigInteger) null);
        buf.writeNumber((BigDecimal) null);
        buf.writeNumber("987.65");
        buf.writeEndArray();

        JsonParser parser = buf.asParser();
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(bigInt, parser.getBigIntegerValue());
        assertEquals(JsonParser.NumberType.BIG_INTEGER, parser.getNumberType());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(bigDec, parser.getDecimalValue());
        assertEquals(JsonParser.NumberType.BIG_DECIMAL, parser.getNumberType());

        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(987.65, parser.getDoubleValue(), 0.001);

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests string variations and text accessor methods
    @Test
    public void testStringsAndText_variousInputs_matchesExpected() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);

        buf.writeString("hello");
        buf.writeString((String) null);
        char[] chars = "world".toCharArray();
        buf.writeString(chars, 0, chars.length);
        buf.writeString(new SerializedString("serializable"));
        buf.writeString((SerializedString) null);

        JsonParser parser = buf.asParser();

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getText());
        assertEquals(5, parser.getTextLength());
        assertEquals(0, parser.getTextOffset());
        assertArrayEquals("hello".toCharArray(), parser.getTextCharacters());
        assertFalse(parser.hasTextCharacters());

        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL.asString(), parser.getText());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("world", parser.getText());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("serializable", parser.getText());

        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());

        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests Segment boundary rollover by writing more than 16 tokens
    @Test
    public void testSegmentBoundary_moreThan16Tokens_handlesCorrectly() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);

        for (int i = 0; i < 40; i++) {
            buf.writeNumber(i);
        }

        JsonParser parser = buf.asParser();
        for (int i = 0; i < 40; i++) {
            assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            assertEquals(i, parser.getIntValue());
        }
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests serialization from TokenBuffer to another TokenBuffer
    @Test
    public void testSerialize_toAnotherTokenBuffer_replicatesContents() throws Exception {
        TokenBuffer source = new TokenBuffer(null);
        source.writeStartObject();
        source.writeFieldName(new SerializedString("key"));
        source.writeString("value");
        source.writeFieldName("num");
        source.writeNumber(42);
        source.writeFieldName("embedded");
        source.writeObject("embeddedObj");
        source.writeEndObject();

        TokenBuffer target = new TokenBuffer(null);
        source.serialize(target);

        JsonParser parser = target.asParser();
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("num", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("embedded", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertEquals("embeddedObj", parser.getEmbeddedObject());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests appending one buffer to another
    @Test
    public void testAppend_combiningBuffers_containsAppendedTokens() throws Exception {
        TokenBuffer buf1 = new TokenBuffer(null);
        buf1.writeNumber(1);

        TokenBuffer buf2 = new TokenBuffer(null);
        buf2.writeNumber(2);

        buf1.append(buf2);

        JsonParser parser = buf1.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests native type and object IDs buffering and propagation
    @Test
    public void testNativeIds_enabled_tracksIdsProperly() throws Exception {
        TokenBuffer buf = new TokenBuffer(null, true);
        assertTrue(buf.canWriteTypeId());
        assertTrue(buf.canWriteObjectId());

        buf.writeTypeId("myTypeId");
        buf.writeObjectId("myObjectId");
        buf.writeStartObject();
        buf.writeEndObject();

        JsonParser parser = buf.asParser();
        assertTrue(parser.canReadTypeId());
        assertTrue(parser.canReadObjectId());

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals("myTypeId", parser.getTypeId());
        assertEquals("myObjectId", parser.getObjectId());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.getTypeId());
        assertNull(parser.getObjectId());

        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests peekNextToken functionality
    @Test
    public void testPeekNextToken_variousPositions_peeksCorrectly() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeNumber(10);
        buf.writeNumber(20);

        TokenBuffer.Parser parser = (TokenBuffer.Parser) buf.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.peekNextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(10, parser.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.peekNextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(20, parser.getIntValue());

        assertNull(parser.peekNextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests binary data writing and reading
    @Test
    public void testBinaryData_byteArrays_encodedAndDecoded() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        assertTrue(buf.canWriteBinaryNatively());

        byte[] raw = new byte[]{1, 2, 3, 4, 5};
        buf.writeBinary(Base64Variants.MIME, raw, 0, raw.length);

        JsonParser parser = buf.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        byte[] readBack = parser.getBinaryValue(Base64Variants.MIME);
        assertArrayEquals(raw, readBack);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bytesWritten = parser.readBinaryValue(Base64Variants.MIME, out);
        assertEquals(raw.length, bytesWritten);
        assertArrayEquals(raw, out.toByteArray());
        parser.close();
    }

    // Tests binary reading from string token
    @Test
    public void testBinaryData_fromStringToken_decodesBase64() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeString("AQIDBAU=");

        JsonParser parser = buf.asParser();
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        byte[] decoded = parser.getBinaryValue(Base64Variants.MIME);
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5}, decoded);
        parser.close();
    }

    // Tests toString method formatting and truncation limit
    @Test
    public void testToString_variousTokenCounts_formatsOutputString() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeStartObject();
        buf.writeFieldName("a");
        buf.writeNumber(1);
        buf.writeEndObject();

        String str = buf.toString();
        assertTrue(str.startsWith("[TokenBuffer: "));
        assertTrue(str.contains("START_OBJECT"));
        assertTrue(str.contains("FIELD_NAME(a)"));
        assertTrue(str.endsWith("]"));

        TokenBuffer largeBuf = new TokenBuffer(null);
        for (int i = 0; i < 110; i++) {
            largeBuf.writeNumber(i);
        }
        String largeStr = largeBuf.toString();
        assertTrue(largeStr.contains("truncated"));
    }

    // Tests generator feature configuration and basic accessors
    @Test
    public void testGeneratorFeaturesAndContext_enablingAndDisabling_updatesState() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        assertFalse(buf.isClosed());
        assertNotNull(buf.getOutputContext());
        assertNotNull(buf.version());

        buf.enable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertTrue(buf.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));

        buf.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertFalse(buf.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));

        buf.setFeatureMask(4);
        assertEquals(4, buf.getFeatureMask());

        assertSame(buf, buf.useDefaultPrettyPrinter());

        buf.flush();
        buf.close();
        assertTrue(buf.isClosed());
    }

    // Tests firstToken accessor on empty and populated buffer
    @Test
    public void testFirstToken_emptyAndNonEmpty_returnsFirst() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        assertNull(buf.firstToken());

        buf.writeNumber(99);
        assertEquals(JsonToken.VALUE_NUMBER_INT, buf.firstToken());
    }

    // Tests overrideCurrentName in parsing context
    @Test
    public void testOverrideCurrentName_nestedStructure_overridesName() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeStartObject();
        buf.writeFieldName("oldName");
        buf.writeNumber(1);
        buf.writeEndObject();

        JsonParser parser = buf.asParser();
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("oldName", parser.getCurrentName());

        parser.overrideCurrentName("newName");
        assertEquals("newName", parser.getCurrentName());
        parser.close();
    }

    // Tests copyCurrentStructure for nested object and array
    @Test
    public void testCopyCurrentStructure_nested_copiesEntireSubtree() throws Exception {
        TokenBuffer source = new TokenBuffer(null);
        source.writeStartObject();
        source.writeFieldName("arr");
        source.writeStartArray();
        source.writeNumber(1);
        source.writeNumber(2);
        source.writeEndArray();
        source.writeEndObject();

        JsonParser sourceParser = source.asParser();
        sourceParser.nextToken();

        TokenBuffer target = new TokenBuffer(null);
        target.copyCurrentStructure(sourceParser);

        JsonParser targetParser = target.asParser();
        assertEquals(JsonToken.START_OBJECT, targetParser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, targetParser.nextToken());
        assertEquals("arr", targetParser.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, targetParser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, targetParser.nextToken());
        assertEquals(1, targetParser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, targetParser.nextToken());
        assertEquals(2, targetParser.getIntValue());
        assertEquals(JsonToken.END_ARRAY, targetParser.nextToken());
        assertEquals(JsonToken.END_OBJECT, targetParser.nextToken());
        assertNull(targetParser.nextToken());
        targetParser.close();
        sourceParser.close();
    }

    // Tests exception on unsupported raw write operations
    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRaw_unsupported_throwsException() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeRaw("test");
    }

    // Tests exception on checking numeric values when current token is not numeric
    @Test(expected = IOException.class)
    public void testCheckIsNumber_nonNumericToken_throwsException() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeBoolean(true);

        JsonParser parser = buf.asParser();
        parser.nextToken();
        parser.getIntValue();
    }

    // Tests exception on reading binary when token is not binary or string
    @Test(expected = IOException.class)
    public void testGetBinaryValue_invalidToken_throwsException() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeNumber(123);

        JsonParser parser = buf.asParser();
        parser.nextToken();
        parser.getBinaryValue(Base64Variants.MIME);
    }
}