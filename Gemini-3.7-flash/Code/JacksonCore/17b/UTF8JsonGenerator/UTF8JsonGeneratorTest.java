package com.fasterxml.jackson.core.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.util.BufferRecycler;

public class UTF8JsonGeneratorTest {

    private ByteArrayOutputStream out;
    private IOContext ioContext;

    @Before
    public void setUp() {
        out = new ByteArrayOutputStream();
        ioContext = new IOContext(new BufferRecycler(), null, false);
    }

    private UTF8JsonGenerator createGenerator(int features) {
        return new UTF8JsonGenerator(ioContext, features, null, out);
    }

    // Tests writing start and end object brackets
    @Test
    public void testWriteStartEndObject_normal_createsJsonObject() throws IOException {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeStartObject();
        gen.writeEndObject();
        gen.close();
        assertEquals("{}", out.toString("UTF-8"));
    }

    // Tests writing start and end array brackets
    @Test
    public void testWriteStartEndArray_normal_createsJsonArray() throws IOException {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeStartArray();
        gen.writeEndArray();
        gen.close();
        assertEquals("[]", out.toString("UTF-8"));
    }

    // Tests writing field names and standard string values
    @Test
    public void testWriteFieldNameAndString_normal_writesKeyAndValue() throws IOException {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeStartObject();
        gen.writeFieldName("name");
        gen.writeString("Jackson");
        gen.writeFieldName("count");
        gen.writeString(new char[] { '1', '2', '3' }, 0, 3);
        gen.writeEndObject();
        gen.close();
        assertEquals("{\"name\":\"Jackson\",\"count\":\"123\"}", out.toString("UTF-8"));
    }

    // Tests writing field name and string using SerializableString
    @Test
    public void testWriteFieldName_withSerializableString_writesQuotedField() throws IOException {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeStartObject();
        gen.writeFieldName(new SerializedString("key"));
        gen.writeString(new SerializedString("value"));
        gen.writeEndObject();
        gen.close();
        assertEquals("{\"key\":\"value\"}", out.toString("UTF-8"));
    }

    // Tests writing null string value
    @Test
    public void testWriteString_nullValue_writesNullLiteral() throws IOException {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeStartArray();
        gen.writeString((String) null);
        gen.writeEndArray();
        gen.close();
        assertEquals("[null]", out.toString("UTF-8"));
    }

    // Tests writing string with characters requiring escaping and 2-byte UTF-8
    @Test
    public void testWriteString_withSpecialAndEscapedChars_escapesCorrectly() throws IOException {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeStartArray();
        gen.writeString("Hello\n\"World\"\t\u0001\u00A9");
        gen.writeEndArray();
        gen.close();
        assertEquals("[\"Hello\\n\\\"World\\\"\\t\\u0001\u00A9\"]", out.toString("UTF-8"));
    }

    // Tests writing primitive numbers (int, long, short)
    @Test
    public void testWriteNumbers_primitives_writesNumericValues() throws IOException {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeStartArray();
        gen.writeNumber((short) 10);
        gen.writeNumber(12345);
        gen.writeNumber(9876543210L);
        gen.writeEndArray();
        gen.close();
        assertEquals("[10,12345,9876543210]", out.toString("UTF-8"));
    }

    // Tests writing numbers as strings when feature is enabled
    @Test
    public void testWriteNumbers_featureNumbersAsStrings_quotesNumericValues() throws IOException {
        UTF8JsonGenerator gen = createGenerator(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask());
        gen.writeStartArray();
        gen.writeNumber((short) 1);
        gen.writeNumber(2);
        gen.writeNumber(3L);
        gen.writeEndArray();
        gen.close();
        assertEquals("[\"1\",\"2\",\"3\"]", out.toString("UTF-8"));
    }

    // Tests writing NaN and Infinite floating point numbers
    @Test
    public void testWriteNumber_specialDoubleAndFloat_quotesNonNumeric() throws IOException {
        int mask = JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS.getMask();
        UTF8JsonGenerator gen = createGenerator(mask);
        gen.writeStartArray();
        gen.writeNumber(Double.NaN);
        gen.writeNumber(Float.POSITIVE_INFINITY);
        gen.writeEndArray();
        gen.close();
        assertEquals("[\"NaN\",\"Infinity\"]", out.toString("UTF-8"));
    }

    // Tests writing BigInteger and BigDecimal
    @Test
    public void testWriteNumber_bigDecimalAndBigInteger_writesCorrectly() throws IOException {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeStartArray();
        gen.writeNumber(new BigInteger("12345678901234567890"));
        gen.writeNumber(new BigDecimal("1234.5678"));
        gen.writeNumber((BigInteger) null);
        gen.writeNumber((BigDecimal) null);
        gen.writeEndArray();
        gen.close();
        assertEquals("[12345678901234567890,1234.5678,null,null]", out.toString("UTF-8"));
    }

    // Tests writing boolean and null values
    @Test
    public void testWriteBooleanAndNull_normal_writesKeywords() throws IOException {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeStartArray();
        gen.writeBoolean(true);
        gen.writeBoolean(false);
        gen.writeNull();
        gen.writeEndArray();
        gen.close();
        assertEquals("[true,false,null]", out.toString("UTF-8"));
    }

    // Tests writing raw string and raw character array
    @Test
    public void testWriteRaw_stringAndCharArray_outputsRawContent() throws IOException {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeStartArray();
        gen.writeRaw("123");
        gen.writeRaw(',');
        gen.writeRaw(new char[] { '4', '5', '6' }, 0, 3);
        gen.writeEndArray();
        gen.close();
        assertEquals("[123,456]", out.toString("UTF-8"));
    }

    // Tests writing raw value with SerializableString
    @Test
    public void testWriteRawValue_serializableString_writesRawValue() throws IOException {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeStartArray();
        gen.writeRawValue(new SerializedString("{\"raw\":true}"));
        gen.writeEndArray();
        gen.close();
        assertEquals("[{\"raw\":true}]", out.toString("UTF-8"));
    }

    // Tests writing surrogate pair with raw text
    @Test
    public void testWriteRaw_surrogatePairs_outputsUtf8Bytes() throws IOException {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeStartArray();
        // Emoji grinning face: \uD83D\uDE00
        gen.writeRaw("\uD83D\uDE00");
        gen.writeEndArray();
        gen.close();
        assertEquals("[\uD83D\uDE00]", out.toString("UTF-8"));
    }

    // Tests writing UTF-8 raw strings and segments
    @Test
    public void testWriteUTF8String_byteArrays_outputsQuotedString() throws IOException {
        UTF8JsonGenerator gen = createGenerator(0);
        byte[] utf8 = "test\"data".getBytes("UTF-8");
        gen.writeStartArray();
        gen.writeUTF8String(utf8, 0, utf8.length);
        gen.writeEndArray();
        gen.close();
        assertEquals("[\"test\\\"data\"]", out.toString("UTF-8"));
    }

    // Tests base64 binary encoding from byte array and InputStream
    @Test
    public void testWriteBinary_bytesAndInputStream_outputsBase64() throws IOException {
        UTF8JsonGenerator gen = createGenerator(0);
        byte[] data = "Hello World!".getBytes("UTF-8");
        gen.writeStartArray();
        gen.writeBinary(Base64Variants.MIME_NO_LINEFEEDS, data, 0, data.length);
        gen.writeBinary(Base64Variants.MIME_NO_LINEFEEDS, new ByteArrayInputStream(data), data.length);
        gen.writeEndArray();
        gen.close();
        assertEquals("[\"SGVsbG8gV29ybGQh\",\"SGVsbG8gV29ybGQh\"]", out.toString("UTF-8"));
    }

    // Tests invalid context when calling writeEndObject on array
    @Test(expected = JsonGenerationException.class)
    public void testWriteEndObject_inArrayContext_throwsJsonGenerationException() throws IOException {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeStartArray();
        gen.writeEndObject();
    }

    // Tests invalid context when calling writeEndArray on object
    @Test(expected = JsonGenerationException.class)
    public void testWriteEndArray_inObjectContext_throwsJsonGenerationException() throws IOException {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeStartObject();
        gen.writeEndArray();
    }

    // Tests writing field name when a value is expected
    @Test(expected = JsonGenerationException.class)
    public void testWriteFieldName_expectingValue_throwsJsonGenerationException() throws IOException {
        UTF8JsonGenerator gen = createGenerator(0);
        gen.writeStartObject();
        gen.writeFieldName("a");
        gen.writeFieldName("b");
    }

    // Tests automatic closing of open JSON scopes
    @Test
    public void testClose_openScopesWithAutoClose_closesAllScopes() throws IOException {
        int mask = JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT.getMask();
        UTF8JsonGenerator gen = createGenerator(mask);
        gen.writeStartObject();
        gen.writeFieldName("array");
        gen.writeStartArray();
        gen.writeString("item");
        gen.close();
        assertEquals("{\"array\":[\"item\"]}", out.toString("UTF-8"));
    }
}