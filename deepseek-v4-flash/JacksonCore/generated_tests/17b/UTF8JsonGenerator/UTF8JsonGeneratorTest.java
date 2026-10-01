package com.fasterxml.jackson.core.json;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import org.junit.Test;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SerializedString;

public class UTF8JsonGeneratorTest {

    private JsonGenerator newGenerator(ByteArrayOutputStream out) throws IOException {
        return new JsonFactory().createGenerator(out, JsonEncoding.UTF8);
    }

    // Tests that writeRaw(String) does not split a surrogate pair across the internal buffer boundary
    @Test
    public void testWriteRaw_longStringWithSupplementaryChar_encodesUtf8WithoutSplitting() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonGenerator jg = newGenerator(out);
        int bufferSize = ((UTF8JsonGenerator) jg)._charBuffer.length;
        char[] chars = new char[bufferSize + 20];
        Arrays.fill(chars, 'a');
        chars[bufferSize - 1] = '\uD83D';
        chars[bufferSize] = '\uDE00';
        String value = new String(chars);

        try (JsonGenerator gen = jg) {
            gen.writeRaw(value);
            gen.flush();
        }

        assertArrayEquals(value.getBytes("UTF-8"), out.toByteArray());
    }

    // Tests normal compact object writing
    @Test
    public void testWriteStartObjectAndEndObject_writesCompactJson() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeStartObject();
            gen.writeFieldName("name");
            gen.writeString("value");
            gen.writeFieldName("num");
            gen.writeNumber(42);
            gen.writeEndObject();
        }
        assertEquals("{\"name\":\"value\",\"num\":42}", out.toString("UTF-8"));
    }

    // Tests escaping of quotes, backslashes, control characters and UTF-8 encoding
    @Test
    public void testWriteString_specialCharacters_escapesAndEncodesUtf8() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeString("a\"b\\c\n\u00e9");
        }
        assertEquals("\"a\\\"b\\\\c\\n\u00e9\"", out.toString("UTF-8"));
    }

    // Tests null String value
    @Test
    public void testWriteString_null_writesNullLiteral() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeString(null);
        }
        assertEquals("null", out.toString("UTF-8"));
    }

    // Tests char[] String overload
    @Test
    public void testWriteString_charArray_writesQuotedString() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeString(new char[] { 'h', 'e', 'l', 'l', 'o' }, 1, 4);
        }
        assertEquals("\"ello\"", out.toString("UTF-8"));
    }

    // Tests segmented writeString(String) path
    @Test
    public void testWriteString_longString_usesSegmentedWrites() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StringBuilder sb = new StringBuilder(12000);
        for (int i = 0; i < 1200; i++) {
            sb.append("abcdefghij");
        }
        String value = sb.toString();

        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeString(value);
        }
        assertArrayEquals(("\"" + value + "\"").getBytes("UTF-8"), out.toByteArray());
    }

    // Tests SerializableString overload
    @Test
    public void testWriteString_serializableString_writesQuotedString() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeString(new SerializedString("a\"b"));
        }
        assertEquals("\"a\\\"b\"", out.toString("UTF-8"));
    }

    // Tests SerializableString field name
    @Test
    public void testWriteFieldName_serializableString_writesQuotedFieldName() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeStartObject();
            gen.writeFieldName(new SerializedString("na\"me"));
            gen.writeNumber(1);
            gen.writeEndObject();
        }
        assertEquals("{\"na\\\"me\":1}", out.toString("UTF-8"));
    }

    // Tests integer and long number writing
    @Test
    public void testWriteNumber_integerAndLong_writesExpected() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeStartArray();
            gen.writeNumber(123);
            gen.writeNumber(-456);
            gen.writeNumber(789L);
            gen.writeEndArray();
        }
        assertEquals("[123,-456,789]", out.toString("UTF-8"));
    }

    // Tests BigInteger and BigDecimal writing
    @Test
    public void testWriteNumber_bigIntegerAndBigDecimal_writesExpected() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeStartArray();
            gen.writeNumber(new BigInteger("12345678901234567890"));
            gen.writeNumber(new BigDecimal("1.25"));
            gen.writeEndArray();
        }
        assertEquals("[12345678901234567890,1.25]", out.toString("UTF-8"));
    }

    // Tests WRITE_BIGDECIMAL_AS_PLAIN feature branch
    @Test
    public void testWriteNumber_bigDecimalAsPlain_writesPlainString() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
            gen.writeNumber(new BigDecimal("1E+3"));
        }
        assertEquals("1000", out.toString("UTF-8"));
    }

    // Tests non-numeric number handling when quoting is disabled
    @Test
    public void testWriteNumber_nonNumeric_rawWhenFeatureDisabled() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.disable(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS);
            gen.writeNumber(Double.NaN);
        }
        assertEquals("NaN", out.toString("UTF-8"));
    }

    // Tests non-numeric number handling when quoting is enabled
    @Test
    public void testWriteNumber_nonNumeric_quotedWhenFeatureEnabled() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.enable(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS);
            gen.writeNumber(Float.POSITIVE_INFINITY);
        }
        assertEquals("\"Infinity\"", out.toString("UTF-8"));
    }

    // Tests WRITE_NUMBERS_AS_STRINGS feature
    @Test
    public void testWriteNumber_numbersAsStrings_writesQuotedNumbers() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
            gen.writeStartArray();
            gen.writeNumber(123);
            gen.writeNumber(1.5);
            gen.writeBoolean(true);
            gen.writeEndArray();
        }
        assertEquals("[\"123\",\"1.5\",true]", out.toString("UTF-8"));
    }

    // Tests boolean and null writing
    @Test
    public void testWriteBooleanAndNull_writesLiterals() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeStartArray();
            gen.writeBoolean(true);
            gen.writeNull();
            gen.writeBoolean(false);
            gen.writeEndArray();
        }
        assertEquals("[true,null,false]", out.toString("UTF-8"));
    }

    // Tests writeRawUTF8String
    @Test
    public void testWriteRawUTF8String_writesRawBytesBetweenQuotes() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] data = "hello".getBytes("UTF-8");
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeRawUTF8String(data, 0, data.length);
        }
        assertEquals("\"hello\"", out.toString("UTF-8"));
    }

    // Tests writeUTF8String with control character escaping
    @Test
    public void testWriteUTF8String_escapesControlChars() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] data = "a\nb".getBytes("UTF-8");
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeUTF8String(data, 0, data.length);
        }
        assertEquals("\"a\\nb\"", out.toString("UTF-8"));
    }

    // Tests binary Base64 writing
    @Test
    public void testWriteBinary_bytes_encodesBase64() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeBinary(Base64Variants.getDefaultVariant(), new byte[] { 1, 2, 3 }, 0, 3);
        }
        assertEquals("\"AQID\"", out.toString("UTF-8"));
    }

    // Tests invalid state: writing a value in an object without a field name
    @Test(expected = JsonGenerationException.class)
    public void testWriteString_inObjectWithoutFieldName_throwsException() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeStartObject();
            gen.writeString("oops");
        }
    }

    // ========== New test cases for uncovered coverage ==========

    // Tests writeRaw(char[]) method
    @Test
    public void testWriteRaw_charArray_writesRawCharacters() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeRaw(new char[] { 'h', 'e', 'l', 'l', 'o' }, 1, 4);
        }
        assertEquals("ello", out.toString("UTF-8"));
    }

    // Tests writeRaw(String, int, int) method
    @Test
    public void testWriteRaw_stringWithOffset_writesSubstring() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeRaw("hello world", 6, 5);
        }
        assertEquals("world", out.toString("UTF-8"));
    }

    // Tests writeRawValue(String) method
    @Test
    public void testWriteRawValue_string_writesRawValue() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeRawValue("123");
        }
        assertEquals("123", out.toString("UTF-8"));
    }

    // Tests writeRawValue(char[], int, int) method
    @Test
    public void testWriteRawValue_charArray_writesRawValue() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeRawValue(new char[] { 't', 'r', 'u', 'e' }, 0, 4);
        }
        assertEquals("true", out.toString("UTF-8"));
    }

    // Tests writeNumber(double) method with normal double
    @Test
    public void testWriteNumber_double_writesExpected() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeNumber(3.14);
        }
        assertEquals("3.14", out.toString("UTF-8"));
    }

    // Tests writeNumber(float) method
    @Test
    public void testWriteNumber_float_writesExpected() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeNumber(2.5f);
        }
        assertEquals("2.5", out.toString("UTF-8"));
    }

    // Tests writeStringField shortcut method
    @Test
    public void testWriteStringField_writesObjectFieldAndValue() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeStartObject();
            gen.writeStringField("key", "value");
            gen.writeEndObject();
        }
        assertEquals("{\"key\":\"value\"}", out.toString("UTF-8"));
    }

    // Tests writeNumberField shortcut method
    @Test
    public void testWriteNumberField_writesObjectFieldAndNumber() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeStartObject();
            gen.writeNumberField("count", 100);
            gen.writeEndObject();
        }
        assertEquals("{\"count\":100}", out.toString("UTF-8"));
    }

    // Tests writeBooleanField shortcut method
    @Test
    public void testWriteBooleanField_writesObjectFieldAndBoolean() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeStartObject();
            gen.writeBooleanField("flag", true);
            gen.writeEndObject();
        }
        assertEquals("{\"flag\":true}", out.toString("UTF-8"));
    }

    // Tests writeNullField shortcut method
    @Test
    public void testWriteNullField_writesObjectFieldAndNull() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeStartObject();
            gen.writeNullField("nothing");
            gen.writeEndObject();
        }
        assertEquals("{\"nothing\":null}", out.toString("UTF-8"));
    }

    // Tests writeArrayFieldStart shortcut method
    @Test
    public void testWriteArrayFieldStart_writesObjectFieldAndStartArray() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeStartObject();
            gen.writeArrayFieldStart("items");
            gen.writeNumber(1);
            gen.writeNumber(2);
            gen.writeEndArray();
            gen.writeEndObject();
        }
        assertEquals("{\"items\":[1,2]}", out.toString("UTF-8"));
    }

    // Tests writeObjectFieldStart shortcut method
    @Test
    public void testWriteObjectFieldStart_writesNestedObject() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeStartObject();
            gen.writeObjectFieldStart("nested");
            gen.writeStringField("a", "b");
            gen.writeEndObject();
            gen.writeEndObject();
        }
        assertEquals("{\"nested\":{\"a\":\"b\"}}", out.toString("UTF-8"));
    }

    // Tests writeStartArray and writeEndArray with nested arrays
    @Test
    public void testWriteStartArrayAndEndArray_nestedArrays_writesCorrectly() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeStartArray();
            gen.writeStartArray();
            gen.writeNumber(1);
            gen.writeEndArray();
            gen.writeEndArray();
        }
        assertEquals("[[1]]", out.toString("UTF-8"));
    }

    // Tests writeRawUTF8String with empty byte array
    @Test
    public void testWriteRawUTF8String_emptyBytes_writesEmptyQuotes() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeRawUTF8String(new byte[0], 0, 0);
        }
        assertEquals("\"\"", out.toString("UTF-8"));
    }

    // Tests writeUTF8String with empty byte array
    @Test
    public void testWriteUTF8String_emptyBytes_writesEmptyQuotes() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeUTF8String(new byte[0], 0, 0);
        }
        assertEquals("\"\"", out.toString("UTF-8"));
    }

    // Tests writeBinary with empty byte array
    @Test
    public void testWriteBinary_emptyBytes_writesEmptyBase64() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonGenerator gen = newGenerator(out)) {
            gen.writeBinary(Base64Variants.getDefaultVariant(), new byte[0], 0, 0);
        }
        assertEquals("\"\"", out.toString("UTF-8"));
    }
}