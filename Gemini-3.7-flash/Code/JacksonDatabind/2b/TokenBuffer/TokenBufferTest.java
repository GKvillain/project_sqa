package com.fasterxml.jackson.databind.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TokenBufferTest {

    // Tests basic structured write and parse traversal for simple object
    @Test
    public void testWriteAndRead_simpleObject_returnsMatchingTokens() throws IOException {
        TokenBuffer tb = new TokenBuffer((com.fasterxml.jackson.core.ObjectCodec) null, false);
        tb.writeStartObject();
        tb.writeFieldName("name");
        tb.writeString("Jackson");
        tb.writeFieldName("age");
        tb.writeNumber(10);
        tb.writeFieldName("active");
        tb.writeBoolean(true);
        tb.writeFieldName("extra");
        tb.writeNull();
        tb.writeEndObject();
        tb.close();

        assertTrue(tb.isClosed());
        assertEquals(JsonToken.START_OBJECT, tb.firstToken());

        JsonParser p = tb.asParser();
        assertNull(p.getCurrentToken());
        assertEquals(JsonToken.START_OBJECT, p.peekNextToken());

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("name", p.getCurrentName());
        assertEquals("name", p.getText());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("Jackson", p.getText());
        assertFalse(p.hasTextCharacters());
        assertEquals(7, p.getTextLength());
        assertEquals(0, p.getTextOffset());

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
        assertNull(p.getText());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        assertTrue(p.isClosed());
    }

    // Tests segment overflow across multiple segments (> 16 tokens)
    @Test
    public void testSegmentBoundary_multipleTokens_correctlyCrossesSegments() throws IOException {
        TokenBuffer tb = new TokenBuffer((com.fasterxml.jackson.core.ObjectCodec) null, false);
        tb.writeStartArray();
        for (int i = 0; i < 35; i++) {
            tb.writeNumber(i);
        }
        tb.writeEndArray();

        JsonParser p = tb.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        for (int i = 0; i < 35; i++) {
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(i, p.getIntValue());
        }
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    // Tests various numeric types and their conversions
    @Test
    public void testNumericTypes_variousNumbers_correctNumberTypesAndValues() throws IOException {
        TokenBuffer tb = new TokenBuffer((com.fasterxml.jackson.core.ObjectCodec) null, false);
        tb.writeStartArray();
        tb.writeNumber((short) 1);
        tb.writeNumber(100L);
        tb.writeNumber(2.5f);
        tb.writeNumber(3.14159);
        tb.writeNumber(new BigInteger("12345678901234567890"));
        tb.writeNumber(new BigDecimal("9876.54321"));
        tb.writeNumber("42.0");
        tb.writeEndArray();

        JsonParser p = tb.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(100L, p.getLongValue());
        assertEquals(JsonParser.NumberType.LONG, p.getNumberType());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(2.5f, p.getFloatValue(), 0.0001f);
        assertEquals(JsonParser.NumberType.FLOAT, p.getNumberType());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(3.14159, p.getDoubleValue(), 0.00001);
        assertEquals(JsonParser.NumberType.DOUBLE, p.getNumberType());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(new BigInteger("12345678901234567890"), p.getBigIntegerValue());
        assertEquals(JsonParser.NumberType.BIG_INTEGER, p.getNumberType());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(new BigDecimal("9876.54321"), p.getDecimalValue());
        assertEquals(JsonParser.NumberType.BIG_DECIMAL, p.getNumberType());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(42.0, p.getDoubleValue(), 0.001);
        assertEquals(42.0, p.getNumberValue().doubleValue(), 0.001);

        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    // Tests null BigInteger and BigDecimal inputs resulting in writeNull
    @Test
    public void testWriteNumber_nullValues_appendsNullToken() throws IOException {
        TokenBuffer tb = new TokenBuffer((com.fasterxml.jackson.core.ObjectCodec) null, false);
        tb.writeNumber((BigDecimal) null);
        tb.writeNumber((BigInteger) null);

        JsonParser p = tb.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    // Tests writeBinary and reading back binary data via parser
    @Test
    public void testWriteAndGetBinary_byteData_matchesExpectedBytes() throws IOException {
        TokenBuffer tb = new TokenBuffer((com.fasterxml.jackson.core.ObjectCodec) null, false);
        assertTrue(tb.canWriteBinaryNatively());
        byte[] testData = new byte[] { 1, 2, 3, 4, 5, 10, 20, 30 };
        tb.writeBinary(Base64Variants.MIME, testData, 1, 6);

        JsonParser p = tb.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        byte[] result = p.getBinaryValue(Base64Variants.MIME);
        assertNotNull(result);
        assertEquals(6, result.length);
        assertEquals(2, result[0]);
        assertEquals(20, result[result.length - 1]);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int readLen = p.readBinaryValue(Base64Variants.MIME, out);
        assertEquals(6, readLen);
        assertArrayEquals(result, out.toByteArray());
        p.close();
    }

    // Tests reading binary from base64 encoded string token
    @Test
    public void testGetBinaryValue_fromEncodedString_decodesCorrectly() throws IOException {
        TokenBuffer tb = new TokenBuffer((com.fasterxml.jackson.core.ObjectCodec) null, false);
        tb.writeString("AQIDBA==");

        JsonParser p = tb.asParser();
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        byte[] decoded = p.getBinaryValue(Base64Variants.MIME);
        assertNotNull(decoded);
        assertArrayEquals(new byte[] { 1, 2, 3, 4 }, decoded);
        p.close();
    }

    // Tests native type and object ID handling
    @Test
    public void testNativeIds_typeAndObjectId_storedAndRetrieved() throws IOException {
        TokenBuffer tb = new TokenBuffer((com.fasterxml.jackson.core.ObjectCodec) null, true);
        assertTrue(tb.canWriteTypeId());
        assertTrue(tb.canWriteObjectId());

        tb.writeTypeId("myTypeId");
        tb.writeObjectId("myObjectId");
        tb.writeString("sample");

        JsonParser p = tb.asParser();
        assertTrue(p.canReadTypeId());
        assertTrue(p.canReadObjectId());

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("myTypeId", p.getTypeId());
        assertEquals("myObjectId", p.getObjectId());
        p.close();
    }

    // Tests append method to concatenate contents of another TokenBuffer
    @Test
    public void testAppend_twoBuffers_combinesContents() throws IOException {
        TokenBuffer tb1 = new TokenBuffer((com.fasterxml.jackson.core.ObjectCodec) null, false);
        tb1.writeString("first");

        TokenBuffer tb2 = new TokenBuffer((com.fasterxml.jackson.core.ObjectCodec) null, false);
        tb2.writeString("second");

        tb1.append(tb2);

        JsonParser p = tb1.asParser();
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("first", p.getText());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("second", p.getText());
        assertNull(p.nextToken());
        p.close();
    }

    // Tests serialize method writing all buffered tokens into target JsonGenerator
    @Test
    public void testSerialize_intoAnotherTokenBuffer_reproducesStructure() throws IOException {
        TokenBuffer source = new TokenBuffer((com.fasterxml.jackson.core.ObjectCodec) null, true);
        source.writeStartObject();
        source.writeFieldName("num");
        source.writeNumber(123);
        source.writeFieldName("flag");
        source.writeBoolean(false);
        source.writeFieldName("rawFloat");
        source.writeNumber(12.34);
        source.writeFieldName("embedded");
        source.writeObject("embeddedObj");
        source.writeEndObject();

        TokenBuffer target = new TokenBuffer((com.fasterxml.jackson.core.ObjectCodec) null, true);
        source.serialize(target);

        JsonParser p = target.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("num", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(123, p.getIntValue());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(12.34, p.getDoubleValue(), 0.001);
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertEquals("embeddedObj", p.getEmbeddedObject());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    // Tests copyCurrentStructure and copyCurrentEvent from external parser
    @Test
    public void testCopyCurrentStructure_nestedJson_copiesFullStructure() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser src = mapper.getFactory().createParser("{\"a\":[1,{\"b\":true}],\"c\":\"text\"}");
        src.nextToken();

        TokenBuffer tb = new TokenBuffer(src);
        tb.copyCurrentStructure(src);

        JsonParser p = tb.asParser(src);
        assertNotNull(p.getCodec());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("b", p.getCurrentName());
        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("c", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("text", p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());

        src.close();
        p.close();
    }

    // Tests overrideCurrentName functionality on parser
    @Test
    public void testOverrideCurrentName_fieldAndNestedContext_modifiesContextName() throws IOException {
        TokenBuffer tb = new TokenBuffer((com.fasterxml.jackson.core.ObjectCodec) null, false);
        tb.writeStartObject();
        tb.writeFieldName("oldField");
        tb.writeString("val");
        tb.writeEndObject();

        JsonParser p = tb.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("oldField", p.getCurrentName());
        p.overrideCurrentName("newField");
        assertEquals("newField", p.getCurrentName());

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    // Tests toString method contains formatted token descriptions
    @Test
    public void testToString_variousTokens_containsTokenSummary() throws IOException {
        TokenBuffer tb = new TokenBuffer((com.fasterxml.jackson.core.ObjectCodec) null, false);
        tb.writeStartObject();
        tb.writeFieldName("key");
        tb.writeString("value");
        tb.writeEndObject();

        String str = tb.toString();
        assertNotNull(str);
        assertTrue(str.startsWith("[TokenBuffer:"));
        assertTrue(str.contains("START_OBJECT"));
        assertTrue(str.contains("FIELD_NAME(key)"));
        assertTrue(str.contains("VALUE_STRING"));
        assertTrue(str.contains("END_OBJECT"));
    }

    // Tests unsupported raw write operations throw UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRaw_stringInput_throwsUnsupportedOperationException() throws IOException {
        TokenBuffer tb = new TokenBuffer((com.fasterxml.jackson.core.ObjectCodec) null, false);
        tb.writeRaw("raw text");
    }

    // Tests unsupported binary InputStream operation throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testWriteBinary_inputStreamInput_throwsUnsupportedOperationException() throws IOException {
        TokenBuffer tb = new TokenBuffer((com.fasterxml.jackson.core.ObjectCodec) null, false);
        tb.writeBinary(Base64Variants.MIME, (java.io.InputStream) null, 10);
    }

    // Tests parser exception when calling numeric accessor on non-numeric token
    @Test(expected = JsonParseException.class)
    public void testGetIntValue_onStringToken_throwsJsonParseException() throws IOException {
        TokenBuffer tb = new TokenBuffer((com.fasterxml.jackson.core.ObjectCodec) null, false);
        tb.writeString("notANumber");

        JsonParser p = tb.asParser();
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        p.getIntValue();
    }

    // Tests generator feature enable/disable and mask configuration
    @Test
    public void testGeneratorFeatures_enableDisable_updatesFeatureMask() {
        TokenBuffer tb = new TokenBuffer((com.fasterxml.jackson.core.ObjectCodec) null, false);
        tb.enable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        assertTrue(tb.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));

        tb.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        assertFalse(tb.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));

        tb.setFeatureMask(0xFF);
        assertEquals(0xFF, tb.getFeatureMask());

        tb.useDefaultPrettyPrinter();
        tb.flush();
        assertNotNull(tb.version());
        assertNotNull(tb.getOutputContext());
    }
}