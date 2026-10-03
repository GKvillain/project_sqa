package com.fasterxml.jackson.databind.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.JsonParser.NumberType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectCodec;

public class TokenBufferTest {

    private ObjectCodec codec;

    @Before
    public void setUp() {
        codec = new ObjectMapper();
    }

    // Tests basic construction and default state
    @Test
    public void testConstructor_defaultValues_returnsCorrectState() {
        TokenBuffer buffer = new TokenBuffer(codec);
        assertNotNull(buffer);
        assertFalse(buffer.isClosed());
        assertNotNull(buffer.getOutputContext());
        assertNull(buffer.firstToken());
        assertEquals(JsonGenerator.Feature.collectDefaults(), buffer.getFeatureMask());
    }

    // Tests constructor with hasNativeIds = true
    @Test
    public void testConstructor_withNativeIds_hasNativeSupport() {
        TokenBuffer buffer = new TokenBuffer(codec, true);
        assertTrue(buffer.canWriteTypeId());
        assertTrue(buffer.canWriteObjectId());
    }

    // Tests constructor with hasNativeIds = false
    @Test
    public void testConstructor_withoutNativeIds_noNativeSupport() {
        TokenBuffer buffer = new TokenBuffer(codec, false);
        assertFalse(buffer.canWriteTypeId());
        assertFalse(buffer.canWriteObjectId());
    }

    // Tests constructor from JsonParser
    @Test
    public void testConstructor_fromJsonParser_inheritsCodecAndNativeFlags() throws Exception {
        String json = "{\"a\":1}";
        JsonParser jp = new ObjectMapper().getFactory().createParser(json);
        jp.nextToken(); // move to START_OBJECT
        TokenBuffer buffer = new TokenBuffer(jp);
        assertNotNull(buffer);
        assertNotNull(buffer.getCodec());
        // Since parser does not support native ids, should be false
        assertFalse(buffer.canWriteTypeId());
        assertFalse(buffer.canWriteObjectId());
        jp.close();
    }

    // Tests empty buffer asParser returns no tokens
    @Test
    public void testAsParser_emptyBuffer_returnsNoTokens() throws Exception {
        TokenBuffer buffer = new TokenBuffer(codec);
        JsonParser parser = buffer.asParser();
        assertNull(parser.nextToken());
    }

    // Tests appending a simple field and value, then parsing back
    @Test
    public void testWriteFieldAndString_appendAndParse_returnsSameContent() throws Exception {
        TokenBuffer buffer = new TokenBuffer(codec);
        buffer.writeStartObject();
        buffer.writeFieldName("name");
        buffer.writeString("value");
        buffer.writeEndObject();
        buffer.close();

        JsonParser parser = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    // Tests writing all numeric types and parsing them back
    @Test
    public void testWriteNumbers_variousTypes_returnsCorrectValues() throws Exception {
        TokenBuffer buffer = new TokenBuffer(codec);
        buffer.writeNumber(42);
        buffer.writeNumber(123L);
        buffer.writeNumber(3.14);
        buffer.writeNumber(2.5f);
        buffer.writeNumber(new BigDecimal("10.5"));
        buffer.writeNumber(new BigInteger("10000000000000000000"));
        buffer.close();

        JsonParser parser = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123L, parser.getLongValue());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(2.5f, parser.getFloatValue(), 0.0);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(new BigDecimal("10.5"), parser.getDecimalValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(new BigInteger("10000000000000000000"), parser.getBigIntegerValue());
        assertNull(parser.nextToken());
    }

    // Tests writing boolean, null, and embedded object
    @Test
    public void testWriteBooleanNullAndObject_mixedTypes_returnsCorrectTokens() throws Exception {
        TokenBuffer buffer = new TokenBuffer(codec);
        buffer.writeBoolean(true);
        buffer.writeBoolean(false);
        buffer.writeNull();
        buffer.writeObject("embedded");
        buffer.close();

        JsonParser parser = buffer.asParser();
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertEquals("embedded", parser.getEmbeddedObject());
        assertNull(parser.nextToken());
    }

    // Tests writeStartArray / writeEndArray with nesting
    @Test
    public void testWriteArray_nestedArray_returnsCorrectStructure() throws Exception {
        TokenBuffer buffer = new TokenBuffer(codec);
        buffer.writeStartArray();
        buffer.writeStartObject();
        buffer.writeFieldName("x");
        buffer.writeNumber(1);
        buffer.writeEndObject();
        buffer.writeEndArray();
        buffer.close();

        JsonParser parser = buffer.asParser();
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("x", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }

    // Tests writeFieldName with SerializableString
    @Test
    public void testWriteFieldName_serializableString_acceptsAndStores() throws Exception {
        TokenBuffer buffer = new TokenBuffer(codec);
        buffer.writeFieldName(new SerializableString() {
            @Override
            public String getValue() { return "key"; }
            @Override
            public int charLength() { return 3; }
            @Override
            public char[] asQuotedChars() { return "key".toCharArray(); }
            @Override
            public byte[] asUnquotedUTF8() { return "key".getBytes(); }
            @Override
            public byte[] asQuotedUTF8() { return "key".getBytes(); }
        });
        buffer.writeString("value");
        buffer.close();

        JsonParser parser = buffer.asParser();
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());
    }

    // Tests closing and isClosed
    @Test
    public void testClose_closedBuffer_isClosedReturnsTrue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(codec);
        assertFalse(buffer.isClosed());
        buffer.close();
        assertTrue(buffer.isClosed());
        // Further writes should likely work (no close check in code), but asParser should return null
        JsonParser parser = buffer.asParser();
        assertNull(parser.nextToken());
    }

    // Tests flush does nothing
    @Test
    public void testFlush_noException_doesNothing() throws Exception {
        TokenBuffer buffer = new TokenBuffer(codec);
        buffer.flush();
        assertFalse(buffer.isClosed());
    }

    // Tests enable/disable features
    @Test
    public void testEnableAndDisableFeatures_flagsChangeCorrectly() {
        TokenBuffer buffer = new TokenBuffer(codec);
        int defaultMask = buffer.getFeatureMask();
        buffer.enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
        assertTrue(buffer.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
        buffer.disable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
        assertFalse(buffer.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));
        assertEquals(defaultMask, buffer.getFeatureMask());
    }

    // Tests copyCurrentEvent for each token type
    @Test
    public void testCopyCurrentEvent_allTokenTypes_copiesCorrectly() throws Exception {
        // Build a source buffer with all token types
        TokenBuffer src = new TokenBuffer(codec);
        src.writeStartObject();
        src.writeFieldName("f");
        src.writeString("s");
        src.writeNumber(1);
        src.writeNumber(2L);
        src.writeNumber(new BigInteger("3"));
        src.writeNumber(4.0);
        src.writeNumber(5.0f);
        src.writeNumber(new BigDecimal("6"));
        src.writeBoolean(true);
        src.writeBoolean(false);
        src.writeNull();
        src.writeObject("embedded");
        src.writeEndObject();
        src.close();

        // Copy into target
        TokenBuffer dest = new TokenBuffer(codec);
        JsonParser srcParser = src.asParser();
        while (srcParser.nextToken() != null) {
            dest.copyCurrentEvent(srcParser);
        }
        dest.close();

        // Verify structure
        JsonParser destParser = dest.asParser();
        assertEquals(JsonToken.START_OBJECT, destParser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, destParser.nextToken());
        assertEquals("f", destParser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, destParser.nextToken());
        assertEquals("s", destParser.getText());
        assertEquals(JsonToken.VALUE_NUMBER_INT, destParser.nextToken());
        assertEquals(1, destParser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, destParser.nextToken());
        assertEquals(2L, destParser.getLongValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, destParser.nextToken());
        assertEquals(new BigInteger("3"), destParser.getBigIntegerValue());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, destParser.nextToken());
        assertEquals(4.0, destParser.getDoubleValue(), 0);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, destParser.nextToken());
        assertEquals(5.0f, destParser.getFloatValue(), 0);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, destParser.nextToken());
        assertEquals(new BigDecimal("6"), destParser.getDecimalValue());
        assertEquals(JsonToken.VALUE_TRUE, destParser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, destParser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, destParser.nextToken());
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, destParser.nextToken());
        assertEquals("embedded", destParser.getEmbeddedObject());
        assertEquals(JsonToken.END_OBJECT, destParser.nextToken());
        assertNull(destParser.nextToken());
    }

    // Tests copyCurrentStructure with nested arrays and objects
    @Test
    public void testCopyCurrentStructure_nestedStructure_copiesCorrectly() throws Exception {
        // Source buffer: {"a":[1,2],"b":{"c":3}}
        TokenBuffer src = new TokenBuffer(codec);
        src.writeStartObject();
        src.writeFieldName("a");
        src.writeStartArray();
        src.writeNumber(1);
        src.writeNumber(2);
        src.writeEndArray();
        src.writeFieldName("b");
        src.writeStartObject();
        src.writeFieldName("c");
        src.writeNumber(3);
        src.writeEndObject();
        src.writeEndObject();
        src.close();

        TokenBuffer dest = new TokenBuffer(codec);
        JsonParser srcParser = src.asParser();
        srcParser.nextToken(); // START_OBJECT
        dest.copyCurrentStructure(srcParser);
        dest.close();

        // Verify
        JsonParser destParser = dest.asParser();
        assertEquals(JsonToken.START_OBJECT, destParser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, destParser.nextToken());
        assertEquals("a", destParser.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, destParser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, destParser.nextToken());
        assertEquals(1, destParser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, destParser.nextToken());
        assertEquals(2, destParser.getIntValue());
        assertEquals(JsonToken.END_ARRAY, destParser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, destParser.nextToken());
        assertEquals("b", destParser.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, destParser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, destParser.nextToken());
        assertEquals("c", destParser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, destParser.nextToken());
        assertEquals(3, destParser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, destParser.nextToken());
        assertEquals(JsonToken.END_OBJECT, destParser.nextToken());
        assertNull(destParser.nextToken());
    }

    // Tests append of another TokenBuffer
    @Test
    public void testAppend_anotherBuffer_combinesTokens() throws Exception {
        TokenBuffer first = new TokenBuffer(codec);
        first.writeNumber(1);

        TokenBuffer second = new TokenBuffer(codec);
        second.writeNumber(2);

        first.append(second);
        first.close();

        JsonParser parser = first.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertNull(parser.nextToken());
    }

    // Tests serialize writing to another generator
    @Test
    public void testSerialize_writeContentsToGenerator_copiesTokens() throws Exception {
        TokenBuffer buffer = new TokenBuffer(codec);
        buffer.writeStartArray();
        buffer.writeBoolean(true);
        buffer.writeEndArray();
        buffer.close();

        TokenBuffer target = new TokenBuffer(codec);
        buffer.serialize(target);
        target.close();

        JsonParser parser = target.asParser();
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }

    // Tests writeBinary with byte array
    @Test
    public void testWriteBinary_byteArray_storesEmbeddedObject() throws Exception {
        TokenBuffer buffer = new TokenBuffer(codec);
        byte[] data = new byte[]{1,2,3};
        buffer.writeBinary(Base64Variants.getDefaultVariant(), data, 0, data.length);
        buffer.close();

        JsonParser parser = buffer.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        byte[] result = parser.getBinaryValue(Base64Variants.getDefaultVariant());
        assertArrayEquals(data, result);
    }

    // Tests writeString with null triggers writeNull
    @Test
    public void testWriteString_null_shouldWriteNull() throws Exception {
        TokenBuffer buffer = new TokenBuffer(codec);
        buffer.writeString((String) null);
        buffer.close();

        JsonParser parser = buffer.asParser();
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
    }

    // Tests writeNumber with null BigDecimal/BigInteger writes null
    @Test
    public void testWriteNumber_nullBigDecimal_shouldWriteNull() throws Exception {
        TokenBuffer buffer = new TokenBuffer(codec);
        buffer.writeNumber((BigDecimal) null);
        buffer.close();

        JsonParser parser = buffer.asParser();
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
    }

    // Tests writeNumber with null BigInteger writes null
    @Test
    public void testWriteNumber_nullBigInteger_shouldWriteNull() throws Exception {
        TokenBuffer buffer = new TokenBuffer(codec);
        buffer.writeNumber((BigInteger) null);
        buffer.close();

        JsonParser parser = buffer.asParser();
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
    }

    // Tests firstToken returns correct token
    @Test
    public void testFirstToken_afterWrite_returnsFirstToken() throws Exception {
        TokenBuffer buffer = new TokenBuffer(codec);
        buffer.writeStartObject();
        buffer.writeFieldName("x");
        buffer.writeNumber(1);
        buffer.writeEndObject();
        assertEquals(JsonToken.START_OBJECT, buffer.firstToken());
    }

    // Tests that writeRaw throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRaw_unsupported_throwsException() throws Exception {
        TokenBuffer buffer = new TokenBuffer(codec);
        buffer.writeRaw("test");
    }

    // Tests writeRawValue throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRawValue_unsupported_throwsException() throws Exception {
        TokenBuffer buffer = new TokenBuffer(codec);
        buffer.writeRawValue("test");
    }

    // Tests writeUTF8String throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testWriteUTF8String_unsupported_throwsException() throws Exception {
        TokenBuffer buffer = new TokenBuffer(codec);
        buffer.writeUTF8String(new byte[0], 0, 0);
    }

    // Tests writing native type id and object id
    @Test
    public void testNativeIds_writeAndParse_retrievesIds() throws Exception {
        TokenBuffer buffer = new TokenBuffer(codec, true);
        buffer.writeTypeId("type123");
        buffer.writeObjectId("obj456");
        buffer.writeStartObject();
        buffer.writeEndObject();
        buffer.close();

        JsonParser parser = buffer.asParser();
        parser.nextToken(); // START_OBJECT
        assertEquals("type123", parser.getTypeId());
        assertEquals("obj456", parser.getObjectId());
    }

    // Tests that toString returns proper format
    @Test
    public void testToString_afterTokens_containsTokenNames() throws Exception {
        TokenBuffer buffer = new TokenBuffer(codec);
        buffer.writeString("hello");
        String str = buffer.toString();
        assertTrue(str.contains("VALUE_STRING"));
        assertTrue(str.startsWith("[TokenBuffer:"));
        assertTrue(str.endsWith("]"));
    }

    // Tests that setCodec and getCodec work
    @Test
    public void testSetCodec_afterConstruction_updatesCodec() {
        TokenBuffer buffer = new TokenBuffer(codec);
        assertSame(codec, buffer.getCodec());
        ObjectCodec newCodec = new ObjectMapper();
        buffer.setCodec(newCodec);
        assertSame(newCodec, buffer.getCodec());
    }

    // Tests getOutputContext returns proper context after start/end
    @Test
    public void testGetOutputContext_afterStartArray_contextIsArray() throws Exception {
        TokenBuffer buffer = new TokenBuffer(codec);
        buffer.writeStartArray();
        assertTrue(buffer.getOutputContext().inArray());
        buffer.writeEndArray();
        assertFalse(buffer.getOutputContext().inArray());
    }

    // Tests that property used in Defects4J bug (e.g., unbalanced end)
    @Test
    public void testWriteEndObject_unbalanced_closesParent() throws Exception {
        TokenBuffer buffer = new TokenBuffer(codec);
        buffer.writeStartObject();
        buffer.writeEndObject();
        buffer.writeEndObject(); // extra end
        // Should not throw, just stay at root
        assertNotNull(buffer.getOutputContext());
    }

    // Tests that writeString with char array
    @Test
    public void testWriteString_charArray_storesCorrectString() throws Exception {
        TokenBuffer buffer = new TokenBuffer(codec);
        buffer.writeString("hello".toCharArray(), 0, 5);
        buffer.close();

        JsonParser parser = buffer.asParser();
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getText());
    }

    // Tests that writeNumber with short
    @Test
    public void testWriteNumber_short_parsedAsInt() throws Exception {
        TokenBuffer buffer = new TokenBuffer(codec);
        buffer.writeNumber((short) 10);
        buffer.close();

        JsonParser parser = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(10, parser.getIntValue());
    }

    // Tests that writeNumber with string-encoded value
    @Test
    public void testWriteNumber_stringEncoded_storedAsNumberFloat() throws Exception {
        TokenBuffer buffer = new TokenBuffer(codec);
        buffer.writeNumber("1.25");
        buffer.close();

        JsonParser parser = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(1.25, parser.getDoubleValue(), 0.0);
    }

    // Tests that canWriteBinaryNatively returns true
    @Test
    public void testCanWriteBinaryNatively_returnsTrue() {
        TokenBuffer buffer = new TokenBuffer(codec);
        assertTrue(buffer.canWriteBinaryNatively());
    }

    // Tests that useDefaultPrettyPrinter returns same instance
    @Test
    public void testUseDefaultPrettyPrinter_returnsThis() {
        TokenBuffer buffer = new TokenBuffer(codec);
        assertSame(buffer, buffer.useDefaultPrettyPrinter());
    }
}