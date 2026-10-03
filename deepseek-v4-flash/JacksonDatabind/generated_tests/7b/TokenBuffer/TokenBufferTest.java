package com.fasterxml.jackson.databind.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TokenBufferTest {

    // Helper to create a parser from buffer written with a simple object
    private JsonParser bufferToParser(TokenBuffer buf) throws IOException {
        buf.close();
        return buf.asParser();
    }

    // Basic write and read of tokens
    @Test
    public void testWriteStartObjectAndEndObject_roundTrip_returnsCorrectTokens() throws IOException {
        TokenBuffer buf = new TokenBuffer(new ObjectMapper());
        buf.writeStartObject();
        buf.writeEndObject();
        JsonParser p = bufferToParser(buf);
        assertNotNull(p);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    // Write field name and string value, then read back
    @Test
    public void testWriteFieldNameAndString_roundTrip_returnsCorrectValues() throws IOException {
        TokenBuffer buf = new TokenBuffer(new ObjectMapper());
        buf.writeStartObject();
        buf.writeFieldName("name");
        buf.writeString("John");
        buf.writeEndObject();
        JsonParser p = bufferToParser(buf);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("name", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("John", p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    // Write integer numbers (boundary: int, long, BigInteger)
    @Test
    public void testWriteNumberIntLongBigInteger_roundTrip_returnsCorrectNumbers() throws IOException {
        TokenBuffer buf = new TokenBuffer(new ObjectMapper());
        buf.writeNumber(42);
        buf.writeNumber(1234567890123L);
        buf.writeNumber(new BigInteger("999999999999999999999999999"));
        JsonParser p = bufferToParser(buf);
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1234567890123L, p.getLongValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(new BigInteger("999999999999999999999999999"), p.getBigIntegerValue());
        p.close();
    }

    // Write floating-point numbers (double, float, BigDecimal)
    @Test
    public void testWriteNumberDoubleFloatBigDecimal_roundTrip_returnsCorrectNumbers() throws IOException {
        TokenBuffer buf = new TokenBuffer(new ObjectMapper());
        buf.writeNumber(3.14);
        buf.writeNumber(2.5f);
        buf.writeNumber(new BigDecimal("1e+300"));
        JsonParser p = bufferToParser(buf);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(3.14, p.getDoubleValue(), 1e-9);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(2.5f, p.getFloatValue(), 1e-9);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(new BigDecimal("1e+300"), p.getDecimalValue());
        p.close();
    }

    // Write boolean and null
    @Test
    public void testWriteBooleanAndNull_roundTrip_returnsCorrectTokens() throws IOException {
        TokenBuffer buf = new TokenBuffer(new ObjectMapper());
        buf.writeBoolean(true);
        buf.writeBoolean(false);
        buf.writeNull();
        JsonParser p = bufferToParser(buf);
        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, p.nextToken());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        p.close();
    }

    // Write array and nested structure
    @Test
    public void testWriteArrayAndNestedStructure_roundTrip_returnsCorrectTokens() throws IOException {
        TokenBuffer buf = new TokenBuffer(new ObjectMapper());
        buf.writeStartArray();
        buf.writeNumber(1);
        buf.writeStartObject();
        buf.writeFieldName("x");
        buf.writeString("y");
        buf.writeEndObject();
        buf.writeEndArray();
        JsonParser p = bufferToParser(buf);
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("x", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("y", p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    // Test serialize method (copy to another generator)
    @Test
    public void testSerialize_copyToTokenBuffer_createsIdenticalContent() throws IOException {
        TokenBuffer src = new TokenBuffer(new ObjectMapper());
        src.writeStartObject();
        src.writeFieldName("val");
        src.writeNumber(99);
        src.writeEndObject();

        TokenBuffer dest = new TokenBuffer(new ObjectMapper());
        src.serialize(dest);
        src.close();
        dest.close();

        JsonParser pSrc = src.asParser();
        JsonParser pDest = dest.asParser();
        while (pSrc.nextToken() != null) {
            assertNotNull(pDest.nextToken());
            assertEquals(pSrc.getCurrentToken(), pDest.getCurrentToken());
            if (pSrc.getCurrentToken() == JsonToken.FIELD_NAME) {
                assertEquals(pSrc.getCurrentName(), pDest.getCurrentName());
            } else if (pSrc.getCurrentToken().isNumeric()) {
                assertEquals(pSrc.getNumberValue(), pDest.getNumberValue());
            } else if (pSrc.getCurrentToken() == JsonToken.VALUE_STRING) {
                assertEquals(pSrc.getText(), pDest.getText());
            }
        }
        assertNull(pDest.nextToken());
        pSrc.close();
        pDest.close();
    }

    // Test copyCurrentEvent
    @Test
    public void testCopyCurrentEvent_parsingFromEvent_createsIdenticalToken() throws IOException {
        TokenBuffer buf = new TokenBuffer(new ObjectMapper());
        buf.writeStartObject();
        buf.writeFieldName("a");
        buf.writeString("b");
        buf.writeEndObject();
        buf.close();
        JsonParser src = buf.asParser();
        src.nextToken(); // START_OBJECT
        src.nextToken(); // FIELD_NAME
        TokenBuffer target = new TokenBuffer(new ObjectMapper());
        target.copyCurrentEvent(src);
        target.close();
        JsonParser p = target.asParser();
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());
        assertNull(p.nextToken());
        p.close();
        src.close();
    }

    // Test copyCurrentStructure with nested object
    @Test
    public void testCopyCurrentStructure_nestedObject_createsIdenticalStructure() throws IOException {
        TokenBuffer buf = new TokenBuffer(new ObjectMapper());
        buf.writeStartObject();
        buf.writeFieldName("inner");
        buf.writeStartObject();
        buf.writeFieldName("x");
        buf.writeNumber(1);
        buf.writeEndObject();
        buf.writeEndObject();
        buf.close();
        JsonParser src = buf.asParser();
        src.nextToken(); // START_OBJECT
        TokenBuffer target = new TokenBuffer(new ObjectMapper());
        target.copyCurrentStructure(src);
        target.close();
        JsonParser p = target.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("inner", p.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("x", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        src.close();
    }

    // Test writing more than 16 tokens (segment boundary)
    @Test
    public void testWriteMoreThanSegmentSize_bufferGrowsAndReadsCorrectly() throws IOException {
        TokenBuffer buf = new TokenBuffer(new ObjectMapper());
        int count = 20;
        for (int i = 0; i < count; i++) {
            buf.writeNumber(i);
        }
        buf.close();
        JsonParser p = buf.asParser();
        for (int i = 0; i < count; i++) {
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(i, p.getIntValue());
        }
        assertNull(p.nextToken());
        p.close();
    }

    // Test writeString with null (should call writeNull)
    @Test
    public void testWriteString_null_writesNullValue() throws IOException {
        TokenBuffer buf = new TokenBuffer(new ObjectMapper());
        buf.writeString(null);
        buf.close();
        JsonParser p = buf.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    // Test writeNumber(BigDecimal) with null
    @Test
    public void testWriteNumberBigDecimal_null_writesNullValue() throws IOException {
        TokenBuffer buf = new TokenBuffer(new ObjectMapper());
        buf.writeNumber((BigDecimal) null);
        buf.close();
        JsonParser p = buf.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        p.close();
    }

    // Test writeNumber(BigInteger) with null
    @Test
    public void testWriteNumberBigInteger_null_writesNullValue() throws IOException {
        TokenBuffer buf = new TokenBuffer(new ObjectMapper());
        buf.writeNumber((BigInteger) null);
        buf.close();
        JsonParser p = buf.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        p.close();
    }

    // Test feature enable/disable and isEnabled
    @Test
    public void testEnableAndDisable_Feature_modifiesGeneratorFeatures() {
        TokenBuffer buf = new TokenBuffer(new ObjectMapper());
        assertTrue(buf.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
        buf.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertFalse(buf.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
        buf.enable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertTrue(buf.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
    }

    // Test close and isClosed
    @Test
    public void testCloseBuffer_isClosed_returnsTrue() throws IOException {
        TokenBuffer buf = new TokenBuffer(new ObjectMapper());
        assertFalse(buf.isClosed());
        buf.close();
        assertTrue(buf.isClosed());
    }

    // Test native type ids (canWriteTypeId, canWriteObjectId, writeTypeId, writeObjectId)
    @Test
    public void testNativeIds_WithNativeIdsEnabled_CanWriteAndRead() throws IOException {
        // Create buffer with native ids enabled via constructor with boolean
        TokenBuffer buf = new TokenBuffer(null, true); // no codec needed for this test
        assertTrue(buf.canWriteTypeId());
        assertTrue(buf.canWriteObjectId());
        buf.writeStartObject();
        buf.writeFieldName("item");
        buf.writeTypeId("type1");
        buf.writeObjectId("obj1");
        buf.writeString("value");
        buf.writeEndObject();
        buf.close();
        JsonParser p = buf.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("item", p.getCurrentName());
        assertEquals("type1", p.getTypeId());  // check that type id is available at this token
        assertEquals("obj1", p.getObjectId());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("value", p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    // Test getOutputContext()
    @Test
    public void testGetOutputContext_afterWriteStartObject_returnsChildObjectContext() throws IOException {
        TokenBuffer buf = new TokenBuffer(new ObjectMapper());
        assertTrue(buf.getOutputContext().inRoot());
        buf.writeStartObject();
        assertTrue(buf.getOutputContext().inObject());
        buf.writeEndObject();
        assertTrue(buf.getOutputContext().inRoot());
    }

    // Test append method (concatenates another TokenBuffer)
    @Test
    public void testAppend_otherBuffer_combinedContentReadable() throws IOException {
        TokenBuffer buf1 = new TokenBuffer(new ObjectMapper());
        buf1.writeNumber(1);
        TokenBuffer buf2 = new TokenBuffer(new ObjectMapper());
        buf2.writeNumber(2);
        buf1.append(buf2);
        buf1.close();
        JsonParser p = buf1.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertNull(p.nextToken());
        p.close();
    }

    // Test toString (basic coverage)
    @Test
    public void testToString_nonEmptyBuffer_containsTokens() throws IOException {
        TokenBuffer buf = new TokenBuffer(new ObjectMapper());
        buf.writeString("hello");
        buf.close();
        String result = buf.toString();
        assertTrue(result.contains("VALUE_STRING"));
        assertTrue(result.contains("hello"));
    }

    // Test deserialize method (at least call it, not much to assert without context)
    @Test
    public void testDeserialize_withFieldNameToken_returnsThis() throws IOException {
        TokenBuffer buf = new TokenBuffer(new ObjectMapper());
        // Create a parser that starts with FIELD_NAME (simulate missing START_OBJECT)
        TokenBuffer src = new TokenBuffer(new ObjectMapper());
        src.writeFieldName("key");
        src.writeString("val");
        src.close();
        JsonParser jp = src.asParser();
        jp.nextToken(); // FIELD_NAME
        TokenBuffer result = buf.deserialize(jp, null); // DeserializationContext is null, but method doesn't use it
        assertSame(buf, result);
    }

    // Test writeRaw* methods throw UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRaw_UnsupportedOperation_throwsException() throws IOException {
        TokenBuffer buf = new TokenBuffer(new ObjectMapper());
        buf.writeRaw("test");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRawUTF8String_UnsupportedOperation_throwsException() throws IOException {
        TokenBuffer buf = new TokenBuffer(new ObjectMapper());
        buf.writeRawUTF8String(new byte[]{65}, 0, 1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteUTF8String_UnsupportedOperation_throwsException() throws IOException {
        TokenBuffer buf = new TokenBuffer(new ObjectMapper());
        buf.writeUTF8String(new byte[]{65}, 0, 1);
    }
}