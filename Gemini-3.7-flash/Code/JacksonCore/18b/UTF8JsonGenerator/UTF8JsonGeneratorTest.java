package com.fasterxml.jackson.core.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

public class UTF8JsonGeneratorTest {

    private UTF8JsonGenerator createGenerator(ByteArrayOutputStream out, int features) {
        IOContext ctxt = new IOContext(new BufferRecycler(), null, false);
        return new UTF8JsonGenerator(ctxt, features, null, out);
    }

    private UTF8JsonGenerator createGenerator(ByteArrayOutputStream out) {
        return createGenerator(out, 0);
    }

    // Tests basic object creation and property writing
    @Test
    public void testWriteSimpleObject_validInput_writesCorrectJson() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        gen.writeStartObject();
        gen.writeFieldName("name");
        gen.writeString("Jackson");
        gen.writeFieldName("age");
        gen.writeNumber(10);
        gen.writeFieldName("active");
        gen.writeBoolean(true);
        gen.writeFieldName("data");
        gen.writeNull();
        gen.writeEndObject();
        gen.close();

        assertEquals("{\"name\":\"Jackson\",\"age\":10,\"active\":true,\"data\":null}",
                new String(out.toByteArray(), "UTF-8"));
    }

    // Tests array creation and primitive values inside array
    @Test
    public void testWriteArray_validInput_writesCorrectJson() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        gen.writeStartArray();
        gen.writeNumber(1);
        gen.writeNumber(2);
        gen.writeBoolean(false);
        gen.writeString("item");
        gen.writeEndArray();
        gen.close();

        assertEquals("[1,2,false,\"item\"]", new String(out.toByteArray(), "UTF-8"));
    }

    // Tests numeric write methods for all primitive and standard number types
    @Test
    public void testWriteNumber_allNumericTypes_writesExpectedValues() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        gen.writeStartArray();
        gen.writeNumber((short) 123);
        gen.writeNumber(456789);
        gen.writeNumber(1234567890123L);
        gen.writeNumber(new BigInteger("98765432109876543210"));
        gen.writeNumber(12.5);
        gen.writeNumber(3.75f);
        gen.writeNumber("1000");
        gen.writeEndArray();
        gen.close();

        assertEquals("[123,456789,1234567890123,98765432109876543210,12.5,3.75,1000]",
                new String(out.toByteArray(), "UTF-8"));
    }

    // Tests BigDecimal output in default and plain modes
    @Test
    public void testWriteNumberBigDecimal_variousFormats_writesExpectedOutput() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        gen.writeStartArray();
        gen.writeNumber(new BigDecimal("1E+2"));
        gen.writeNumber(new BigDecimal("0.0050"));
        gen.writeNumber((BigDecimal) null);
        gen.writeEndArray();
        gen.close();

        assertTrue(out.toString("UTF-8").contains("null"));

        ByteArrayOutputStream outPlain = new ByteArrayOutputStream();
        int feat = JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN.getMask();
        UTF8JsonGenerator genPlain = createGenerator(outPlain, feat);

        genPlain.writeStartArray();
        genPlain.writeNumber(new BigDecimal("1E+2"));
        genPlain.writeNumber(new BigDecimal("1e-7"));
        genPlain.writeEndArray();
        genPlain.close();

        assertEquals("[100,0.0000001]", new String(outPlain.toByteArray(), "UTF-8"));
    }

    // Tests WRITE_NUMBERS_AS_STRINGS feature for numbers
    @Test
    public void testWriteNumbersAsStrings_featureEnabled_quotesNumbers() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int feat = JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask();
        UTF8JsonGenerator gen = createGenerator(out, feat);

        gen.writeStartArray();
        gen.writeNumber((short) 5);
        gen.writeNumber(42);
        gen.writeNumber(123456789L);
        gen.writeNumber(new BigInteger("999"));
        gen.writeNumber(new BigDecimal("12.34"));
        gen.writeNumber("5678");
        gen.writeEndArray();
        gen.close();

        assertEquals("[\"5\",\"42\",\"123456789\",\"999\",\"12.34\",\"5678\"]",
                new String(out.toByteArray(), "UTF-8"));
    }

    // Tests non-numeric double and float values when QUOTE_NON_NUMERIC_NUMBERS is enabled
    @Test
    public void testWriteNumber_nonNumericFloatsAndDoubles_writesQuoted() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int feat = JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS.getMask();
        UTF8JsonGenerator gen = createGenerator(out, feat);

        gen.writeStartArray();
        gen.writeNumber(Double.NaN);
        gen.writeNumber(Double.POSITIVE_INFINITY);
        gen.writeNumber(Float.NEGATIVE_INFINITY);
        gen.writeEndArray();
        gen.close();

        assertEquals("[\"NaN\",\"Infinity\",\"-Infinity\"]", new String(out.toByteArray(), "UTF-8"));
    }

    // Tests string escaping for control characters and multi-byte UTF-8 sequences
    @Test
    public void testWriteString_specialCharactersAndUnicode_escapesProperly() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        gen.writeStartArray();
        gen.writeString("Hello\n\"World\"\t\\");
        gen.writeString("Café \u00e9\u00e8");
        gen.writeString("\uD83D\uDE00"); // Surrogate pair: Emoji grinning face
        gen.writeEndArray();
        gen.close();

        String result = new String(out.toByteArray(), "UTF-8");
        assertTrue(result.contains("\\n\\\"World\\\"\\t\\\\"));
        assertTrue(result.contains("Café"));
    }

    // Tests ESCAPE_NON_ASCII feature
    @Test
    public void testWriteString_escapeNonAsciiEnabled_escapesUnicode() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int feat = JsonGenerator.Feature.ESCAPE_NON_ASCII.getMask();
        UTF8JsonGenerator gen = createGenerator(out, feat);

        gen.writeStartArray();
        gen.writeString("Tëst");
        gen.writeEndArray();
        gen.close();

        assertEquals("[\"T\\u00EBst\"]", new String(out.toByteArray(), "UTF-8"));
    }

    // Tests SerializableString field name and string writes
    @Test
    public void testWriteFieldNameAndString_serializableString_writesCorrectOutput() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        SerializedString key = new SerializedString("key");
        SerializedString val = new SerializedString("value");

        gen.writeStartObject();
        gen.writeFieldName(key);
        gen.writeString(val);
        gen.writeEndObject();
        gen.close();

        assertEquals("{\"key\":\"value\"}", new String(out.toByteArray(), "UTF-8"));
    }

    // Tests writeRaw methods with String, char array, and char
    @Test
    public void testWriteRaw_stringAndCharArray_outputsUnescaped() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        gen.writeStartArray();
        gen.writeRaw("{\"raw\":true}");
        gen.writeRaw(',');
        char[] chars = new char[] { '1', '2', '3' };
        gen.writeRaw(chars, 0, 3);
        gen.writeRawValue(new SerializedString("456"));
        gen.writeEndArray();
        gen.close();

        assertEquals("[{\"raw\":true},123,456]", new String(out.toByteArray(), "UTF-8"));
    }

    // Tests Base64 binary output from byte array and InputStream
    @Test
    public void testWriteBinary_byteArrayAndStream_outputsBase64() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        byte[] data = "Hello World".getBytes("UTF-8");

        gen.writeStartArray();
        gen.writeBinary(Base64Variants.MIME, data, 0, data.length);
        ByteArrayInputStream stream = new ByteArrayInputStream(data);
        gen.writeBinary(Base64Variants.MIME, stream, data.length);
        gen.writeEndArray();
        gen.close();

        assertEquals("[\"SGVsbG8gV29ybGQ=\",\"SGVsbG8gV29ybGQ=\"]",
                new String(out.toByteArray(), "UTF-8"));
    }

    // Tests writeUTF8String and writeRawUTF8String methods
    @Test
    public void testWriteUTF8String_byteArrays_outputsEscapedAndRaw() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        byte[] utf8Bytes = "foo\"bar".getBytes("UTF-8");
        byte[] rawBytes = "plain".getBytes("UTF-8");

        gen.writeStartArray();
        gen.writeUTF8String(utf8Bytes, 0, utf8Bytes.length);
        gen.writeRawUTF8String(rawBytes, 0, rawBytes.length);
        gen.writeEndArray();
        gen.close();

        assertEquals("[\"foo\\\"bar\",\"plain\"]", new String(out.toByteArray(), "UTF-8"));
    }

    // Tests formatting with pretty printer
    @Test
    public void testPrettyPrinter_objectAndArray_formatsOutput() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);
        gen.setPrettyPrinter(new DefaultPrettyPrinter());

        gen.writeStartObject();
        gen.writeFieldName("arr");
        gen.writeStartArray();
        gen.writeNumber(1);
        gen.writeEndArray();
        gen.writeEndObject();
        gen.close();

        String result = new String(out.toByteArray(), "UTF-8");
        assertTrue(result.contains("\n"));
        assertTrue(result.contains("\"arr\" : [ 1 ]"));
    }

    // Tests writing unquoted field names when feature QUOTE_FIELD_NAMES is disabled
    @Test
    public void testUnquotedFieldNames_featureDisabled_writesUnquotedKeys() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);
        gen.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);

        gen.writeStartObject();
        gen.writeFieldName("unquotedKey");
        gen.writeNumber(100);
        gen.writeFieldName(new SerializedString("secondKey"));
        gen.writeNumber(200);
        gen.writeEndObject();
        gen.close();

        assertEquals("{unquotedKey:100,secondKey:200}", new String(out.toByteArray(), "UTF-8"));
    }

    // Tests AUTO_CLOSE_JSON_CONTENT closes open arrays and objects
    @Test
    public void testClose_autoCloseJsonContent_closesOpenScopes() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int feat = JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT.getMask();
        UTF8JsonGenerator gen = createGenerator(out, feat);

        gen.writeStartObject();
        gen.writeFieldName("list");
        gen.writeStartArray();
        gen.writeNumber(1);
        gen.close();

        assertEquals("{\"list\":[1]}", new String(out.toByteArray(), "UTF-8"));
    }

    // Tests getOutputTarget and getOutputBuffered configuration methods
    @Test
    public void testGetOutputTargetAndBuffered_returnsCorrectValues() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        assertSame(out, gen.getOutputTarget());
        assertEquals(0, gen.getOutputBuffered());

        gen.writeStartArray();
        assertTrue(gen.getOutputBuffered() > 0);
        gen.flush();
        assertEquals(0, gen.getOutputBuffered());
        gen.close();
    }

    // Tests exception path when closing an object inside array context
    @Test(expected = JsonGenerationException.class)
    public void testWriteEndObject_whenInArray_throwsException() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        gen.writeStartArray();
        gen.writeEndObject();
    }

    // Tests exception path when writing a field name where value is expected
    @Test(expected = JsonGenerationException.class)
    public void testWriteFieldName_whenExpectingValue_throwsException() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        gen.writeStartObject();
        gen.writeFieldName("first");
        gen.writeFieldName("second");
    }

    // Tests writeString with char array variants and null handling
    @Test
    public void testWriteString_charArrayAndNull_writesCorrectJson() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        gen.writeStartArray();
        char[] chars = "hello world".toCharArray();
        gen.writeString(chars, 0, 5);
        gen.writeString((String) null);
        gen.writeString((char[]) null, 0, 0);
        gen.writeString((SerializableString) null);
        gen.writeEndArray();
        gen.close();

        assertEquals("[\"hello\",null,null,null]", new String(out.toByteArray(), "UTF-8"));
    }

    // Tests writeRawValue with String, char array, and offset/len
    @Test
    public void testWriteRawValue_variousInputs_writesUnquotedValues() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        gen.writeStartArray();
        gen.writeRawValue("123");
        gen.writeRawValue("xyz789abc", 3, 3);
        char[] chars = "foo{\"a\":1}bar".toCharArray();
        gen.writeRawValue(chars, 3, 7);
        gen.writeEndArray();
        gen.close();

        assertEquals("[123,789,{\"a\":1}]", new String(out.toByteArray(), "UTF-8"));
    }

    // Tests boundary numbers: Integer.MIN_VALUE, Long.MIN_VALUE, and negative numbers
    @Test
    public void testWriteNumber_boundaryMinValues_writesExpectedOutput() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        gen.writeStartArray();
        gen.writeNumber(Integer.MIN_VALUE);
        gen.writeNumber(Integer.MAX_VALUE);
        gen.writeNumber(Long.MIN_VALUE);
        gen.writeNumber(Long.MAX_VALUE);
        gen.writeNumber((short) -1);
        gen.writeNumber(0);
        gen.writeNumber(0L);
        gen.writeNumber(0.0);
        gen.writeNumber(-0.0);
        gen.writeEndArray();
        gen.close();

        assertEquals("[" + Integer.MIN_VALUE + "," + Integer.MAX_VALUE + "," +
                     Long.MIN_VALUE + "," + Long.MAX_VALUE + ",-1,0,0,0.0,-0.0]",
                new String(out.toByteArray(), "UTF-8"));
    }

    // Tests long string writing exceeding internal buffer capacity to force flushes and chunking
    @Test
    public void testWriteString_longStringExceedingBuffer_writesCorrectly() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4000; i++) {
            sb.append("abcdefghijklmnopqrstuvwxyz0123456789\u00E9\u00E8\u00E0");
        }
        String largeStr = sb.toString();

        gen.writeStartArray();
        gen.writeString(largeStr);
        gen.writeString(largeStr.toCharArray(), 0, largeStr.length());
        gen.writeEndArray();
        gen.close();

        String expectedSegment = "abcdefghijklmnopqrstuvwxyz0123456789\u00E9\u00E8\u00E0";
        String result = new String(out.toByteArray(), "UTF-8");
        assertTrue(result.startsWith("[\""));
        assertTrue(result.endsWith("\"]"));
        assertTrue(result.contains(expectedSegment));
    }

    // Tests Custom CharacterEscapes support
    @Test
    public void testCharacterEscapes_customEscapes_properlyEscapesConfiguredChars() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        CharacterEscapes customEscapes = new CharacterEscapes() {
            private final int[] asciiEscapes = CharacterEscapes.standardAsciiEscapesForJSON();
            {
                asciiEscapes['a'] = CharacterEscapes.ESCAPE_STANDARD;
                asciiEscapes['b'] = CharacterEscapes.ESCAPE_CUSTOM;
            }

            @Override
            public int[] getEscapeCodesForAscii() {
                return asciiEscapes;
            }

            @Override
            public com.fasterxml.jackson.core.SerializableString getEscapeSequence(int ch) {
                if (ch == 'b') {
                    return new SerializedString("[B]");
                }
                return null;
            }
        };

        gen.setCharacterEscapes(customEscapes);
        assertSame(customEscapes, gen.getCharacterEscapes());

        gen.writeStartObject();
        gen.writeFieldName("abc");
        gen.writeString("abc");
        gen.writeEndObject();
        gen.close();

        String result = new String(out.toByteArray(), "UTF-8");
        assertTrue(result.contains("\\u0061[B]c"));
    }

    // Tests surrogate pairs with ESCAPE_NON_ASCII enabled
    @Test
    public void testSurrogatePairs_withEscapeNonAscii_escapesCorrectly() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int feat = JsonGenerator.Feature.ESCAPE_NON_ASCII.getMask();
        UTF8JsonGenerator gen = createGenerator(out, feat);

        gen.writeStartArray();
        gen.writeString("\uD83D\uDE00"); // Grinning Face Emoji U+1F600
        gen.writeEndArray();
        gen.close();

        assertEquals("[\"\\uD83D\\uDE00\"]", new String(out.toByteArray(), "UTF-8"));
    }

    // Tests invalid surrogate pair (lone high surrogate) throws IOException
    @Test(expected = IOException.class)
    public void testWriteString_loneSurrogate_throwsException() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        gen.writeString("\uD83Dabc"); // High surrogate without low surrogate
        gen.close();
    }

    // Tests exception path when closing an array inside object context
    @Test(expected = JsonGenerationException.class)
    public void testWriteEndArray_whenInObject_throwsException() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        gen.writeStartObject();
        gen.writeEndArray();
    }

    // Tests writeRaw with String sub-range
    @Test
    public void testWriteRaw_stringWithOffsetAndLen_writesExactSubstring() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        gen.writeStartArray();
        gen.writeRaw("prefix12345suffix", 6, 5);
        gen.writeEndArray();
        gen.close();

        assertEquals("[12345]", new String(out.toByteArray(), "UTF-8"));
    }

    // Tests writeRaw with large byte/char content exceeding buffer
    @Test
    public void testWriteRaw_largeContent_flushesCorrectly() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5000; i++) {
            sb.append("x");
        }
        String large = sb.toString();

        gen.writeStartArray();
        gen.writeRaw(large);
        gen.writeRaw(large.toCharArray(), 0, large.length());
        gen.writeEndArray();
        gen.close();

        String result = new String(out.toByteArray(), "UTF-8");
        assertTrue(result.startsWith("[x"));
        assertTrue(result.endsWith("x]"));
        assertEquals(10002, result.length());
    }

    // Tests pretty printer with custom/unquoted field names and SerializableString
    @Test
    public void testPrettyPrinter_withSerializableStringAndUnquoted_formatsCorrectly() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);
        gen.setPrettyPrinter(new DefaultPrettyPrinter());
        gen.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);

        gen.writeStartObject();
        gen.writeFieldName(new SerializedString("unquotedKey"));
        gen.writeString(new SerializedString("val"));
        gen.writeEndObject();
        gen.close();

        String result = new String(out.toByteArray(), "UTF-8");
        assertTrue(result.contains("unquotedKey : \"val\""));
    }
}