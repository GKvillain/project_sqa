package com.fasterxml.jackson.databind.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;

public class TokenBufferTest {

    // Tests write and read of basic JSON object structure
    @Test
    public void testWriteAndReadBasicStructure_validInput_tokensMatch() throws IOException {
        TokenBuffer buffer = new TokenBuffer((com.fasterxml.jackson.core.ObjectCodec) null, false);
        buffer.writeStartObject();
        buffer.writeFieldName("name");
        buffer.writeString("test");
        buffer.writeFieldName("count");
        buffer.writeNumber(42);
        buffer.writeFieldName("active");
        buffer.writeBoolean(true);
        buffer.writeFieldName("nothing");
        buffer.writeNull();
        buffer.writeEndObject();

        assertEquals(JsonToken.START_OBJECT, buffer.firstToken());

        JsonParser parser = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("count", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("active", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("nothing", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests deserialization when starting token is FIELD_NAME (Defects4J bug 7 trigger)
    @Test
    public void testDeserialize_startingAtFieldName_wrapsInObject() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser jp = mapper.getFactory().createParser("{\"field\":\"value\"}");
        assertEquals(JsonToken.START_OBJECT, jp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());

        TokenBuffer tb = new TokenBuffer(mapper, false);
        tb.deserialize(jp, mapper.getDeserializationContext());

        JsonParser parser = tb.asParser();
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("field", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
        jp.close();
    }

    // Tests Segment boundary crossing with more than 16 tokens
    @Test
    public void testSegmentOverflow_moreThan16Tokens_iteratesCorrectly() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeStartArray();
        for (int i = 0; i < 25; i++) {
            buffer.writeNumber(i);
        }
        buffer.writeEndArray();

        JsonParser parser = buffer.asParser();
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        for (int i = 0; i < 25; i++) {
            assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            assertEquals(i, parser.getIntValue());
            assertEquals((long) i, parser.getLongValue());
        }
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests various numeric types: long, short, float, double, BigInteger, BigDecimal, and String-encoded numbers
    @Test
    public void testNumericTypes_variousValues_parsedCorrectly() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeStartArray();
        buffer.writeNumber((short) 10);
        buffer.writeNumber(100000000000L);
        buffer.writeNumber(1.25f);
        buffer.writeNumber(3.1415926535);
        buffer.writeNumber(new BigInteger("12345678901234567890"));
        buffer.writeNumber(new BigDecimal("12345.6789"));
        buffer.writeNumber("9876.54");
        buffer.writeEndArray();

        JsonParser parser = buffer.asParser();
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(10, parser.getIntValue());
        assertEquals(JsonParser.NumberType.INT, parser.getNumberType());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(100000000000L, parser.getLongValue());
        assertEquals(JsonParser.NumberType.LONG, parser.getNumberType());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(1.25f, parser.getFloatValue(), 0.0001f);
        assertEquals(JsonParser.NumberType.FLOAT, parser.getNumberType());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.1415926535, parser.getDoubleValue(), 0.0000001);
        assertEquals(JsonParser.NumberType.DOUBLE, parser.getNumberType());

        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(new BigInteger("12345678901234567890"), parser.getBigIntegerValue());
        assertEquals(JsonParser.NumberType.BIG_INTEGER, parser.getNumberType());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(new BigDecimal("12345.6789"), parser.getDecimalValue());
        assertEquals(JsonParser.NumberType.BIG_DECIMAL, parser.getNumberType());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(9876.54, parser.getDoubleValue(), 0.01);

        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        parser.close();
    }

    // Tests null inputs for BigInteger, BigDecimal, and String
    @Test
    public void testWriteNullOverloads_nullInputs_emitsNullTokens() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeStartArray();
        buffer.writeString((String) null);
        buffer.writeNumber((BigDecimal) null);
        buffer.writeNumber((BigInteger) null);
        buffer.writeEndArray();

        JsonParser parser = buffer.asParser();
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests binary data write and read
    @Test
    public void testBinaryData_byteArrays_writtenAndRead() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        assertTrue(buffer.canWriteBinaryNatively());

        byte[] data = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };
        buffer.writeBinary(Base64Variants.MIME, data, 0, data.length);

        JsonParser parser = buffer.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        assertArrayEquals(data, parser.getBinaryValue(Base64Variants.MIME));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertEquals(data.length, parser.readBinaryValue(Base64Variants.MIME, out));
        assertArrayEquals(data, out.toByteArray());
        parser.close();
    }

    // Tests native type and object ID support
    @Test
    public void testNativeIds_typeAndObjectId_storedAndRetrieved() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, true);
        assertTrue(buffer.canWriteObjectId());
        assertTrue(buffer.canWriteTypeId());

        buffer.writeObjectId("obj-123");
        buffer.writeTypeId("MyType");
        buffer.writeStartObject();
        buffer.writeFieldName("id");
        buffer.writeNumber(1);
        buffer.writeEndObject();

        JsonParser parser = buffer.asParser();
        assertTrue(parser.canReadObjectId());
        assertTrue(parser.canReadTypeId());

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals("obj-123", parser.getObjectId());
        assertEquals("MyType", parser.getTypeId());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    // Tests serialize method writing contents to another JsonGenerator
    @Test
    public void testSerialize_copiesToAnotherGenerator_structureMatches() throws IOException {
        TokenBuffer src = new mechanicalTokenBuffer();
        src.writeStartObject();
        src.writeFieldName("key");
        src.writeString("value");
        src.writeFieldName("num");
        src.writeNumber(100);
        src.writeFieldName("flag");
        src.writeBoolean(false);
        src.writeEndObject();

        TokenBuffer target = new TokenBuffer(null, false);
        src.serialize(target);

        JsonParser parser = target.asParser();
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("num", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(100, parser.getIntValue());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("flag", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    private TokenBuffer mechanicalTokenBuffer() {
        return new TokenBuffer(null, false);
    }

    // Tests append method combining two TokenBuffers
    @Test
    public void testAppend_twoBuffers_concatenatesContent() throws IOException {
        TokenBuffer tb1 = new TokenBuffer(null, false);
        tb1.writeString("first");

        TokenBuffer tb2 = new TokenBuffer(null, false);
        tb2.writeString("second");

        tb1.append(tb2);

        JsonParser parser = tb1.asParser();
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("first", parser.getText());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("second", parser.getText());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests peekNextToken functionality
    @Test
    public void testPeekNextToken_beforeAndAfterAdvance_returnsExpectedTokens() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeStartArray();
        buffer.writeNumber(1);
        buffer.writeEndArray();

        TokenBuffer.Parser parser = (TokenBuffer.Parser) buffer.asParser();
        assertEquals(JsonToken.START_ARRAY, parser.peekNextToken());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.peekNextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.peekNextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.peekNextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests overrideCurrentName functionality
    @Test
    public void testOverrideCurrentName_modifiesFieldName() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeStartObject();
        buffer.writeFieldName("oldName");
        buffer.writeString("val");
        buffer.writeEndObject();

        JsonParser parser = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("oldName", parser.getCurrentName());
        parser.overrideCurrentName("newName");
        assertEquals("newName", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.close();
    }

    // Tests toString representation of TokenBuffer
    @Test
    public void testToString_formatsTokens_containsTokenInfo() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeStartObject();
        buffer.writeFieldName("foo");
        buffer.writeString("bar");
        buffer.writeEndObject();

        String str = buffer.toString();
        assertNotNull(str);
        assertTrue(str.contains("START_OBJECT"));
        assertTrue(str.contains("FIELD_NAME(foo)"));
        assertTrue(str.contains("VALUE_STRING"));
        assertTrue(str.contains("END_OBJECT"));
    }

    // Tests generator feature configuration and lifecycle methods
    @Test
    public void testFeatureAndLifecycleMethods_enableDisableClose_operatesCorrectly() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        assertFalse(buffer.isClosed());
        buffer.enable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertTrue(buffer.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
        buffer.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertFalse(buffer.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));

        buffer.useDefaultPrettyPrinter();
        buffer.flush();
        buffer.close();
        assertTrue(buffer.isClosed());

        Version version = buffer.version();
        assertNotNull(version);
    }

    // Tests exception on unsupported raw operations
    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRaw_unsupportedOperation_throwsException() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeRaw("raw text");
    }

    // Tests exception when numeric accessor is called on non-numeric token
    @Test(expected = IOException.class)
    public void testGetNumberValue_onStringToken_throwsException() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeString("notANumber");

        JsonParser parser = buffer.asParser();
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        parser.getIntValue();
        parser.close();
    }

    // Tests writeObject with POJO delegation and embedded objects
    @Test
    public void testWriteObject_withCodecAndEmbedded_serializesProperly() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TokenBuffer buffer = new TokenBuffer(mapper, false);
        assertEquals(mapper, buffer.getCodec());

        buffer.writeObject(null);
        buffer.writeObject(new byte[] { 10, 20 });

        JsonParser parser = buffer.asParser();
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
    }

    // Tests constructors taking ObjectCodec or JsonParser
    @Test
    public void testConstructorsAndAsParserVariants() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TokenBuffer tb1 = new TokenBuffer((ObjectCodec) mapper);
        assertSame(mapper, tb1.getCodec());

        tb1.writeString("hello");
        JsonParser p1 = tb1.asParser(mapper);
        assertSame(mapper, p1.getCodec());
        assertEquals(JsonToken.VALUE_STRING, p1.nextToken());
        assertEquals("hello", p1.getText());
        p1.close();

        // asParserOnFirstToken
        JsonParser pFirst = tb1.asParserOnFirstToken();
        assertEquals(JsonToken.VALUE_STRING, pFirst.currentToken());
        assertEquals("hello", pFirst.getText());
        pFirst.close();

        // Constructor from JsonParser
        JsonParser srcParser = mapper.getFactory().createParser("{\"k\":\"v\"}");
        TokenBuffer tb2 = new TokenBuffer(srcParser);
        tb2.writeStartArray();
        tb2.writeEndArray();
        JsonParser p2 = tb2.asParser(srcParser);
        assertEquals(JsonToken.START_ARRAY, p2.nextToken());
        assertEquals(JsonToken.END_ARRAY, p2.nextToken());
        p2.close();
        srcParser.close();
    }

    // Tests copyCurrentEvent and copyCurrentStructure
    @Test
    public void testCopyCurrentEventAndStructure() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser jp = mapper.getFactory().createParser("{\"nested\":[1,2], \"val\":\"test\"}");

        TokenBuffer tb = new TokenBuffer(mapper, false);
        assertEquals(JsonToken.START_OBJECT, jp.nextToken());
        tb.copyCurrentEvent(jp);

        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        tb.copyCurrentEvent(jp);

        assertEquals(JsonToken.START_ARRAY, jp.nextToken());
        tb.copyCurrentStructure(jp);

        assertEquals(JsonToken.FIELD_NAME, jp.nextToken());
        tb.copyCurrentEvent(jp);

        assertEquals(JsonToken.VALUE_STRING, jp.nextToken());
        tb.copyCurrentEvent(jp);

        tb.writeEndObject();
        jp.close();

        JsonParser parser = tb.asParser();
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("nested", parser.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("val", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests parser text characters, offsets, locations, and contexts
    @Test
    public void testParserTextAndContextProperties() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeStartObject();
        buffer.writeFieldName(new SerializedString("key"));
        char[] chars = new char[] { 'a', 'b', 'c', 'd', 'e' };
        buffer.writeString(chars, 1, 3);
        buffer.writeRawValue("123");
        buffer.writeEmbeddedObject("customEmbedded");
        buffer.writeTree(new IntNode(999));
        buffer.writeEndObject();

        JsonParser parser = buffer.asParser();
        assertNull(parser.currentToken());
        assertFalse(parser.hasCurrentToken());

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertTrue(parser.hasCurrentToken());
        assertEquals(JsonToken.START_OBJECT, parser.getCurrentToken());
        assertNotNull(parser.getParsingContext());
        assertNotNull(parser.getTokenLocation());
        assertNotNull(parser.getCurrentLocation());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getText());
        assertTrue(parser.hasTextCharacters());
        assertNotNull(parser.getTextCharacters());
        assertEquals("key".length(), parser.getTextLength());
        assertEquals(0, parser.getTextOffset());

        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("bcd", parser.getText());

        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());

        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertEquals("customEmbedded", parser.getEmbeddedObject());

        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertEquals(new IntNode(999), parser.getEmbeddedObject());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        parser.clearCurrentToken();
        assertNull(parser.currentToken());

        assertNull(parser.nextToken());
        parser.close();
        assertTrue(parser.isClosed());
    }

    // Tests skipChildren on Parser
    @Test
    public void testParserSkipChildren() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeStartObject();
        buffer.writeFieldName("arr");
        buffer.writeStartArray();
        buffer.writeNumber(1);
        buffer.writeNumber(2);
        buffer.writeEndArray();
        buffer.writeFieldName("num");
        buffer.writeNumber(3);
        buffer.writeEndObject();

        JsonParser parser = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        parser.skipChildren();
        assertEquals(JsonToken.END_ARRAY, parser.currentToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("num", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(3, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    // Tests additional unsupported methods on TokenBuffer to ensure proper exceptions
    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRawChar_unsupported_throwsException() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeRaw('c');
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRawCharArray_unsupported_throwsException() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeRaw(new char[] { 'a', 'b' }, 0, 2);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRawStringRange_unsupported_throwsException() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeRaw("test", 0, 2);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRawUTF8String_unsupported_throwsException() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeRawUTF8String(new byte[] { 1, 2 }, 0, 2);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteUTF8String_unsupported_throwsException() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeUTF8String(new byte[] { 1, 2 }, 0, 2);
    }

    // Tests codec manipulation and context access on TokenBuffer
    @Test
    public void testBufferCodecAndOutputContext() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, false);
        assertNull(buffer.getCodec());
        ObjectMapper mapper = new ObjectMapper();
        buffer.setCodec(mapper);
        assertSame(mapper, buffer.getCodec());

        JsonStreamContext context = buffer.getOutputContext();
        assertNotNull(context);
        assertTrue(context.inRoot());

        buffer.writeStartObject();
        assertTrue(buffer.getOutputContext().inObject());
        buffer.writeEndObject();
        assertTrue(buffer.getOutputContext().inRoot());
    }

    // Tests native type and object ID Segment boundary crossing
    @Test
    public void testNativeIds_segmentChaining() throws IOException {
        TokenBuffer buffer = new TokenBuffer(null, true);
        for (int i = 0; i < 20; i++) {
            buffer.writeObjectId("id-" + i);
            buffer.writeTypeId("type-" + i);
            buffer.writeString("val-" + i);
        }

        JsonParser parser = buffer.asParser();
        for (int i = 0; i < 20; i++) {
            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            assertEquals("id-" + i, parser.getObjectId());
            assertEquals("type-" + i, parser.getTypeId());
            assertEquals("val-" + i, parser.getText());
        }
        assertNull(parser.nextToken());
        parser.close();
    }
}