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

public class TokenBufferTest {

    // Tests basic write and read lifecycle of structured object
    @Test
    public void testWriteAndRead_simpleObject_readsExpectedTokens() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, false);
        assertNull(buf.firstToken());

        buf.writeStartObject();
        buf.writeFieldName("name");
        buf.writeString("Jackson");
        buf.writeFieldName("age");
        buf.writeNumber(10);
        buf.writeFieldName("active");
        buf.writeBoolean(true);
        buf.writeFieldName("extra");
        buf.writeNull();
        buf.writeEndObject();
        buf.close();

        assertTrue(buf.isClosed());
        assertEquals(JsonToken.START_OBJECT, buf.firstToken());

        JsonParser p = buf.asParser();
        assertFalse(p.isClosed());

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("name", p.getCurrentName());
        assertEquals("name", p.getText());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("Jackson", p.getText());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("age", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(10, p.getIntValue());
        assertEquals(10L, p.getLongValue());
        assertEquals(JsonParser.NumberType.INT, p.getNumberType());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        assertEquals("true", p.getText());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertEquals("null", p.getText());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());

        p.close();
        assertTrue(p.isClosed());
    }

    // Tests segment boundary crossing when tokens exceed segment size (16 tokens)
    @Test
    public void testMultiSegment_over16Tokens_createsSegmentsAndReadsCorrectly() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, false);

        buf.writeStartArray();
        for (int i = 0; i < 25; i++) {
            buf.writeNumber(i);
        }
        buf.writeEndArray();

        JsonParser p = buf.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        for (int i = 0; i < 25; i++) {
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(i, p.getIntValue());
        }
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    // Tests various numeric representations and conversions
    @Test
    public void testNumericTypes_allNumberVariants_returnsExactValues() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, false);

        buf.writeStartArray();
        buf.writeNumber((short) 5);
        buf.writeNumber(100);
        buf.writeNumber(10000000000L);
        buf.writeNumber(12.5f);
        buf.writeNumber(99.99d);
        buf.writeNumber(new BigInteger("12345678901234567890"));
        buf.writeNumber(new BigDecimal("12345.67890"));
        buf.writeNumber("42.0");
        buf.writeEndArray();

        JsonParser p = buf.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(5, p.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(100, p.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(10000000000L, p.getLongValue());
        assertEquals(JsonParser.NumberType.LONG, p.getNumberType());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(12.5f, p.getFloatValue(), 0.001f);
        assertEquals(JsonParser.NumberType.FLOAT, p.getNumberType());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(99.99d, p.getDoubleValue(), 0.001d);
        assertEquals(JsonParser.NumberType.DOUBLE, p.getNumberType());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(new BigInteger("12345678901234567890"), p.getBigIntegerValue());
        assertEquals(JsonParser.NumberType.BIG_INTEGER, p.getNumberType());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(new BigDecimal("12345.67890"), p.getDecimalValue());
        assertEquals(JsonParser.NumberType.BIG_DECIMAL, p.getNumberType());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(42.0d, p.getDoubleValue(), 0.001d);

        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    // Tests binary data writing and reading
    @Test
    public void testBinaryData_byteContent_encodesAndDecodesCorrectly() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, false);
        byte[] input = new byte[] { 1, 2, 3, 4, 5, 6, 7 };

        buf.writeStartArray();
        buf.writeBinary(Base64Variants.MIME, input, 0, input.length);
        buf.writeEndArray();

        JsonParser p = buf.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());

        Object embedded = p.getEmbeddedObject();
        assertTrue(embedded instanceof byte[]);
        assertArrayEquals(input, (byte[]) embedded);

        byte[] binary = p.getBinaryValue(Base64Variants.MIME);
        assertArrayEquals(input, binary);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bytesRead = p.readBinaryValue(Base64Variants.MIME, out);
        assertEquals(input.length, bytesRead);
        assertArrayEquals(input, out.toByteArray());

        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    // Tests native type and object ID handling
    @Test
    public void testNativeIds_typeAndObjectIds_bufferedAndRetrieved() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, true);
        assertTrue(buf.canWriteObjectId());
        assertTrue(buf.canWriteTypeId());

        buf.writeObjectId("obj-123");
        buf.writeTypeId("type-XYZ");
        buf.writeStartObject();
        buf.writeEndObject();

        JsonParser p = buf.asParser();
        assertTrue(p.canReadObjectId());
        assertTrue(p.canReadTypeId());

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("obj-123", p.getObjectId());
        assertEquals("type-XYZ", p.getTypeId());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.getObjectId());
        assertNull(p.getTypeId());
        p.close();
    }

    // Tests peekNextToken and nextFieldName optimized traversal
    @Test
    public void testPeekAndNextFieldName_variousPositions_behavesCorrectly() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartObject();
        buf.writeFieldName("first");
        buf.writeString("value1");
        buf.writeFieldName("second");
        buf.writeString("value2");
        buf.writeEndObject();

        TokenBuffer.Parser p = (TokenBuffer.Parser) buf.asParser();
        assertEquals(JsonToken.START_OBJECT, p.peekNextToken());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        assertEquals("first", p.nextFieldName());
        assertEquals(JsonToken.FIELD_NAME, p.getCurrentToken());
        assertEquals("first", p.getCurrentName());

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("value1", p.getText());

        assertEquals("second", p.nextFieldName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("value2", p.getText());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.peekNextToken());
        assertNull(p.nextToken());
        p.close();
    }

    // Tests append method merging two TokenBuffers
    @Test
    public void testAppend_twoBuffers_combinesContents() throws IOException {
        TokenBuffer buf1 = new TokenBuffer(null, false);
        buf1.writeStartArray();
        buf1.writeNumber(1);
        buf1.writeEndArray();

        TokenBuffer buf2 = new TokenBuffer(null, false);
        buf2.writeStartArray();
        buf2.writeNumber(2);
        buf2.writeEndArray();

        buf1.append(buf2);

        JsonParser p = buf1.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());

        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    // Tests serialize method writing to a target TokenBuffer
    @Test
    public void testSerialize_toTargetGenerator_reproducesStructure() throws IOException {
        TokenBuffer src = new TokenBuffer(null, false);
        src.writeStartObject();
        src.writeFieldName("count");
        src.writeNumber(42);
        src.writeFieldName("flag");
        src.writeBoolean(false);
        src.writeEndObject();

        TokenBuffer dst = new TokenBuffer(null, false);
        src.serialize(dst);

        JsonParser p = dst.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("count", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getIntValue());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("flag", p.getCurrentName());
        assertEquals(JsonToken.VALUE_FALSE, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    // Tests copyCurrentStructure and copyCurrentEvent
    @Test
    public void testCopyCurrentStructure_fromParser_copiesCompleteSubtree() throws IOException {
        TokenBuffer source = new TokenBuffer(null, false);
        source.writeStartObject();
        source.writeFieldName("items");
        source.writeStartArray();
        source.writeString("a");
        source.writeString("b");
        source.writeEndArray();
        source.writeEndObject();

        JsonParser srcParser = source.asParser();
        srcParser.nextToken(); // START_OBJECT

        TokenBuffer copy = new TokenBuffer(null, false);
        copy.copyCurrentStructure(srcParser);

        JsonParser copyParser = copy.asParser();
        assertEquals(JsonToken.START_OBJECT, copyParser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, copyParser.nextToken());
        assertEquals("items", copyParser.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, copyParser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, copyParser.nextToken());
        assertEquals("a", copyParser.getText());
        assertEquals(JsonToken.VALUE_STRING, copyParser.nextToken());
        assertEquals("b", copyParser.getText());
        assertEquals(JsonToken.END_ARRAY, copyParser.nextToken());
        assertEquals(JsonToken.END_OBJECT, copyParser.nextToken());
        assertNull(copyParser.nextToken());

        srcParser.close();
        copyParser.close();
    }

    // Tests feature bitmask enabling and disabling
    @Test
    public void testGeneratorFeatures_enableDisable_updatesState() {
        TokenBuffer buf = new TokenBuffer(null, false);
        JsonGenerator.Feature feat = JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT;

        buf.disable(feat);
        assertFalse(buf.isEnabled(feat));

        buf.enable(feat);
        assertTrue(buf.isEnabled(feat));

        int mask = buf.getFeatureMask();
        buf.setFeatureMask(0);
        assertEquals(0, buf.getFeatureMask());
        assertFalse(buf.isEnabled(feat));

        buf.setFeatureMask(mask);
        assertEquals(mask, buf.getFeatureMask());
    }

    // Tests raw value output and string text properties
    @Test
    public void testRawValueAndTextVariants_variousInputs_storesCorrectly() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, false);
        char[] chars = "hello world".toCharArray();

        buf.writeStartArray();
        buf.writeString(chars, 0, 5); // "hello"
        buf.writeRawValue("{\"raw\":true}");
        buf.writeRawValue("prefix123suffix", 6, 3); // "123"
        buf.writeRawValue(chars, 6, 5); // "world"
        buf.writeEndArray();

        JsonParser p = buf.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("hello", p.getText());
        assertEquals(5, p.getTextLength());
        assertEquals(0, p.getTextOffset());
        assertArrayEquals("hello".toCharArray(), p.getTextCharacters());

        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    // Tests toString representation with tokens and truncation
    @Test
    public void testToString_withBufferedTokens_producesReadableString() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartObject();
        buf.writeFieldName("key");
        buf.writeString("val");
        buf.writeEndObject();

        String str = buf.toString();
        assertNotNull(str);
        assertTrue(str.contains("START_OBJECT"));
        assertTrue(str.contains("FIELD_NAME(key)"));
        assertTrue(str.contains("VALUE_STRING"));
        assertTrue(str.contains("END_OBJECT"));
    }

    // Tests overrideCurrentName and parsing context
    @Test
    public void testOverrideCurrentName_updatesCurrentNameInContext() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartObject();
        buf.writeFieldName("original");
        buf.writeNumber(1);
        buf.writeEndObject();

        JsonParser p = buf.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("original", p.getCurrentName());

        p.overrideCurrentName("overridden");
        assertEquals("overridden", p.getCurrentName());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    // Tests unsupported operations throwing UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRaw_unsupported_throwsException() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeRaw("raw text");
    }

    // Tests calling numeric accessors on non-numeric tokens throwing exception
    @Test(expected = IOException.class)
    public void testGetNumberValue_onStringToken_throwsException() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeString("not_a_number");

        JsonParser p = buf.asParser();
        p.nextToken();
        p.getIntValue();
    }

    // Tests writeObject with null, byte array, and POJO without codec
    @Test
    public void testWriteObject_variousObjects_handlesGracefully() throws IOException {
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartArray();
        buf.writeObject(null);
        buf.writeObject(new byte[] { 10, 20 });
        buf.writeObject("customObject");
        buf.writeEndArray();

        JsonParser p = buf.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertEquals("customObject", p.getEmbeddedObject());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }
}