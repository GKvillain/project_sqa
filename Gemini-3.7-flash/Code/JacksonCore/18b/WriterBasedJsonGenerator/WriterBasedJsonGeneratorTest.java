package com.fasterxml.jackson.core.json;

import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import org.junit.Test;
import static org.junit.Assert.*;

public class WriterBasedJsonGeneratorTest {

    private WriterBasedJsonGenerator _createGenerator(StringWriter sw, int features) {
        IOContext ctxt = new IOContext(new BufferRecycler(), null, false);
        return new WriterBasedJsonGenerator(ctxt, features, null, sw);
    }

    private WriterBasedJsonGenerator _createGenerator(StringWriter sw) {
        return _createGenerator(sw, 0);
    }

    // Tests writing a basic JSON object structure and primitive fields
    @Test
    public void testWriteObject_validFields_outputsCorrectJson() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw);

        gen.writeStartObject();
        gen.writeFieldName("name");
        gen.writeString("John");
        gen.writeFieldName("age");
        gen.writeNumber(30);
        gen.writeFieldName("isMember");
        gen.writeBoolean(true);
        gen.writeFieldName("score");
        gen.writeNumber(99.5d);
        gen.writeFieldName("extra");
        gen.writeNull();
        gen.writeEndObject();
        gen.close();

        assertEquals("{\"name\":\"John\",\"age\":30,\"isMember\":true,\"score\":99.5,\"extra\":null}", sw.toString());
    }

    // Tests writing array structure with numbers and strings
    @Test
    public void testWriteArray_multipleElements_outputsCorrectJson() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw);

        gen.writeStartArray();
        gen.writeNumber((short) 1);
        gen.writeNumber(2L);
        gen.writeNumber(3.14f);
        gen.writeString("item");
        gen.writeEndArray();
        gen.close();

        assertEquals("[1,2,3.14,\"item\"]", sw.toString());
    }

    // Tests writing BigDecimal with and without WRITE_BIGDECIMAL_AS_PLAIN feature
    @Test
    public void testWriteNumber_bigDecimalValues_outputsPlainOrScientific() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw);

        BigDecimal dec = new BigDecimal("1E-5");
        gen.writeStartArray();
        gen.writeNumber(dec);

        gen.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        gen.writeNumber(dec);
        gen.writeNumber((BigDecimal) null);
        gen.writeEndArray();
        gen.close();

        assertEquals("[1E-5,0.00001,null]", sw.toString());
    }

    // Tests writing BigInteger and String encoded numbers
    @Test
    public void testWriteNumber_bigIntegerAndEncodedNumber_outputsCorrectly() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw);

        BigInteger bigInt = new BigInteger("12345678901234567890");
        gen.writeStartArray();
        gen.writeNumber(bigInt);
        gen.writeNumber((BigInteger) null);
        gen.writeNumber("987654321");
        gen.writeEndArray();
        gen.close();

        assertEquals("[12345678901234567890,null,987654321]", sw.toString());
    }

    // Tests WRITE_NUMBERS_AS_STRINGS feature for all number types
    @Test
    public void testWriteNumber_numbersAsStringsEnabled_quotesNumbers() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw, JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask());

        gen.writeStartArray();
        gen.writeNumber((short) 10);
        gen.writeNumber(20);
        gen.writeNumber(30L);
        gen.writeNumber(40.5d);
        gen.writeNumber(50.5f);
        gen.writeNumber(new BigInteger("60"));
        gen.writeNumber(new BigDecimal("70.5"));
        gen.writeNumber("80");
        gen.writeEndArray();
        gen.close();

        assertEquals("[\"10\",\"20\",\"30\",\"40.5\",\"50.5\",\"60\",\"70.5\",\"80\"]", sw.toString());
    }

    // Tests special float and double values like NaN and Infinity
    @Test
    public void testWriteNumber_specialDoubleAndFloat_quotesWhenConfigured() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw, JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS.getMask());

        gen.writeStartArray();
        gen.writeNumber(Double.NaN);
        gen.writeNumber(Double.POSITIVE_INFINITY);
        gen.writeNumber(Float.NEGATIVE_INFINITY);
        gen.writeEndArray();
        gen.close();

        assertEquals("[\"NaN\",\"Infinity\",\"-Infinity\"]", sw.toString());
    }

    // Tests escaping standard control characters and quotes in strings
    @Test
    public void testWriteString_controlAndEscapedChars_escapesProperly() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw);

        gen.writeStartArray();
        gen.writeString("Hello \"World\"\n\t\r\0");
        gen.writeString((String) null);
        gen.writeString(new char[]{'a', 'b', 'c', '"'}, 1, 3);
        gen.writeEndArray();
        gen.close();

        assertEquals("[\"Hello \\\"World\\\"\\n\\t\\r\\u0000\",null,\"bc\\\"\"]", sw.toString());
    }

    // Tests long string writing exceeding internal buffer limit
    @Test
    public void testWriteString_longString_splitsAndFlushesCorrectly() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3000; ++i) {
            sb.append("abcdefghij");
        }
        String longText = sb.toString();

        gen.writeStartArray();
        gen.writeString(longText);
        gen.writeEndArray();
        gen.close();

        String result = sw.toString();
        assertTrue(result.startsWith("[\"abcdefghij"));
        assertTrue(result.endsWith("abcdefghij\"]"));
        assertEquals(longText.length() + 4, result.length());
    }

    // Tests writing using SerializableString
    @Test
    public void testWriteString_serializableString_writesQuoted() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw);

        SerializedString key = new SerializedString("key");
        SerializedString value = new SerializedString("value");

        gen.writeStartObject();
        gen.writeFieldName((SerializableString) key);
        gen.writeString((SerializableString) value);
        gen.writeEndObject();
        gen.close();

        assertEquals("{\"key\":\"value\"}", sw.toString());
    }

    // Tests raw character and string output methods
    @Test
    public void testWriteRaw_stringAndCharArray_outputsDirectly() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw);

        gen.writeStartArray();
        gen.writeRaw("raw1");
        gen.writeRaw(',');
        gen.writeRaw("prefix_raw2_suffix", 7, 4);
        gen.writeRaw(',');
        gen.writeRaw(new SerializedString("raw3"));
        gen.writeRaw(',');
        gen.writeRaw(new char[]{'x', 'r', 'a', 'w', '4', 'y'}, 1, 4);
        gen.writeEndArray();
        gen.close();

        assertEquals("[raw1,raw2,raw3,raw4]", sw.toString());
    }

    // Tests Base64 binary output from byte array and InputStream
    @Test
    public void testWriteBinary_byteArrayAndInputStream_outputsBase64() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw);

        byte[] data = "Hello Base64!".getBytes("UTF-8");
        gen.writeStartArray();
        gen.writeBinary(Base64Variants.MIME_NO_LINEFEEDS, data, 0, data.length);
        gen.writeBinary(Base64Variants.MIME_NO_LINEFEEDS, new ByteArrayInputStream(data), data.length);
        gen.writeBinary(Base64Variants.MIME_NO_LINEFEEDS, new ByteArrayInputStream(data), -1);
        gen.writeEndArray();
        gen.close();

        assertEquals("[\"SGVsbG8gQmFzZTY0IQ==\",\"SGVsbG8gQmFzZTY0IQ==\",\"SGVsbG8gQmFzZTY0IQ==\"]", sw.toString());
    }

    // Tests pretty printer output formatting
    @Test
    public void testWrite_withPrettyPrinter_formatsWithIndentsAndSpaces() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw);
        gen.setPrettyPrinter(new DefaultPrettyPrinter());

        gen.writeStartObject();
        gen.writeFieldName("a");
        gen.writeNumber(1);
        gen.writeFieldName(new SerializedString("b"));
        gen.writeString("val");
        gen.writeEndObject();
        gen.close();

        String out = sw.toString();
        assertTrue(out.contains("\"a\" : 1"));
        assertTrue(out.contains("\"b\" : \"val\""));
    }

    // Tests unquoted field names feature
    @Test
    public void testWriteFieldName_unquotedNames_omitsQuotes() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw, JsonGenerator.Feature.QUOTE_FIELD_NAMES.getMask());
        gen.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);

        gen.writeStartObject();
        gen.writeFieldName("unquoted");
        gen.writeNumber(100);
        gen.writeFieldName(new SerializedString("second"));
        gen.writeBoolean(false);
        gen.writeEndObject();
        gen.close();

        assertEquals("{unquoted:100,second:false}", sw.toString());
    }

    // Tests custom CharacterEscapes and ASCII-only escaping
    @Test
    public void testWriteString_customEscapesAndAscii_escapesCorrectly() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw);
        gen.setHighestNonEscapedChar(127);

        gen.writeStartArray();
        gen.writeString("Price: \u20AC50");
        gen.writeString(new char[]{'V', 'a', 'l', '\u00E9'}, 0, 4);
        gen.writeEndArray();
        gen.close();

        assertEquals("[\"Price: \\u20AC50\",\"Val\\u00E9\"]", sw.toString());
    }

    // Tests auto-close JSON content on generator close
    @Test
    public void testClose_autoCloseJsonContent_closesOpenStructures() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw, JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT.getMask());

        gen.writeStartObject();
        gen.writeFieldName("arr");
        gen.writeStartArray();
        gen.writeNumber(1);
        gen.close();

        assertEquals("{\"arr\":[1]}", sw.toString());
    }

    // Tests getOutputTarget and getOutputBuffered methods
    @Test
    public void testGetOutputTargetAndBuffered_returnsWriterAndCount() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw);

        assertSame(sw, gen.getOutputTarget());
        assertEquals(0, gen.getOutputBuffered());

        gen.writeRaw("123");
        assertEquals(3, gen.getOutputBuffered());
        gen.flush();
        assertEquals(0, gen.getOutputBuffered());
        gen.close();
    }

    // Tests exception when writeEndObject is called without startObject
    @Test(expected = JsonGenerationException.class)
    public void testWriteEndObject_notInObject_throwsException() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw);
        gen.writeEndObject();
    }

    // Tests exception when writeEndArray is called without startArray
    @Test(expected = JsonGenerationException.class)
    public void testWriteEndArray_notInArray_throwsException() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw);
        gen.writeEndArray();
    }

    // Tests exception when writeFieldName is called expecting value
    @Test(expected = JsonGenerationException.class)
    public void testWriteFieldName_expectingValue_throwsException() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw);
        gen.writeStartObject();
        gen.writeFieldName("k1");
        gen.writeFieldName("k2");
    }

    // Tests unsupported operations for raw UTF8 methods
    @Test(expected = UnsupportedOperationException.class)
    public void testWriteRawUTF8String_always_throwsUnsupportedOperationException() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw);
        gen.writeRawUTF8String(new byte[]{0}, 0, 1);
    }

    // Tests writeRawValue variations (String, char[], and SerializableString)
    @Test
    public void testWriteRawValue_variousInputs_outputsUnquoted() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw);

        gen.writeStartArray();
        gen.writeRawValue("{\"k\":1}");
        gen.writeRawValue("prefix_{\"k\":2}_suffix", 7, 7);
        gen.writeRawValue(new char[]{'x', '1', '2', '3', 'y'}, 1, 3);
        gen.writeRawValue(new SerializedString("true"));
        gen.writeEndArray();
        gen.close();

        assertEquals("[{\"k\":1},{\"k\":2},123,true]", sw.toString());
    }

    // Tests custom CharacterEscapes with custom escape sequences
    @Test
    public void testWriteString_customCharacterEscapesSubclass_escapesWithCustomStrings() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw);

        CharacterEscapes customEscapes = new CharacterEscapes() {
            @Override
            public int[] getEscapeCodesForAscii() {
                int[] escapes = CharacterEscapes.standardAsciiEscapesForJSON();
                escapes['a'] = CharacterEscapes.ESCAPE_CUSTOM;
                return escapes;
            }

            @Override
            public SerializableString getEscapeSequence(int ch) {
                if (ch == 'a') {
                    return new SerializedString("[CUSTOM_A]");
                }
                return null;
            }
        };
        gen.setCharacterEscapes(customEscapes);
        assertSame(customEscapes, gen.getCharacterEscapes());

        gen.writeStartObject();
        gen.writeFieldName("apple");
        gen.writeString("cat");
        gen.writeFieldName(new SerializedString("about"));
        gen.writeString(new char[]{'b', 'a', 't'}, 0, 3);
        gen.writeEndObject();
        gen.close();

        assertEquals("{\"[CUSTOM_A]pple\":\"c[CUSTOM_A]t\",\"[CUSTOM_A]bout\":\"b[CUSTOM_A]t\"}", sw.toString());
    }

    // Tests surrogate pair escaping and handling with highestNonEscapedChar
    @Test
    public void testWriteString_surrogatePairWithHighestNonEscapedChar_escapesSurrogates() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw);
        gen.setHighestNonEscapedChar(127);
        assertEquals(127, gen.getHighestNonEscapedChar());

        String emoji = "\uD83D\uDE00"; // 😀 Grinning Face
        gen.writeStartArray();
        gen.writeString("Hi " + emoji + " !");
        gen.writeString(new char[]{'a', '\uD83D', '\uDE00', 'b'}, 0, 4);
        gen.writeEndArray();
        gen.close();

        assertEquals("[\"Hi \\uD83D\\uDE00 !\",\"a\\uD83D\\uDE00b\"]", sw.toString());
    }

    // Tests exception when writing unpaired surrogate character
    @Test(expected = JsonGenerationException.class)
    public void testWriteString_unpairedSurrogate_throwsJsonGenerationException() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw);

        gen.writeStartArray();
        gen.writeString("Unpaired \uD83D surrogate");
        gen.writeEndArray();
        gen.close();
    }

    // Tests exception when writing NaN/Infinity without QUOTE_NON_NUMERIC_NUMBERS feature
    @Test(expected = JsonGenerationException.class)
    public void testWriteNumber_nanWithoutQuoteFeature_throwsJsonGenerationException() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw, 0);
        gen.disable(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS);

        gen.writeNumber(Double.NaN);
    }

    // Tests exception when writing Float Infinity without QUOTE_NON_NUMERIC_NUMBERS feature
    @Test(expected = JsonGenerationException.class)
    public void testWriteNumber_floatInfinityWithoutQuoteFeature_throwsJsonGenerationException() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw, 0);
        gen.disable(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS);

        gen.writeNumber(Float.POSITIVE_INFINITY);
    }

    // Tests Base64 writing with chunked lines and custom variants
    @Test
    public void testWriteBinary_withLinefeeds_formatsCorrectly() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw);

        byte[] largeData = new byte[120];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i & 0xFF);
        }

        gen.writeStartArray();
        gen.writeBinary(Base64Variants.MIME, largeData, 0, largeData.length);
        gen.writeEndArray();
        gen.close();

        String json = sw.toString();
        assertTrue(json.startsWith("[\""));
        assertTrue(json.contains("\\n"));
        assertTrue(json.endsWith("\"]"));
    }

    // Tests pretty printer with unquoted field names and raw values
    @Test
    public void testPrettyPrinter_unquotedKeysAndRawValue_formatsProperly() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw);
        gen.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        gen.setPrettyPrinter(new DefaultPrettyPrinter());

        gen.writeStartObject();
        gen.writeFieldName("count");
        gen.writeRawValue("123");
        gen.writeFieldName(new SerializedString("obj"));
        gen.writeStartObject();
        gen.writeFieldName("flag");
        gen.writeBoolean(false);
        gen.writeEndObject();
        gen.writeEndObject();
        gen.close();

        String out = sw.toString();
        assertTrue(out.contains("count : 123"));
        assertTrue(out.contains("flag : false"));
    }

    // Tests writing large raw characters that exceed output buffer size
    @Test
    public void testWriteRaw_largeBuffers_flushesCorrectly() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = _createGenerator(sw);

        char[] chars = new char[5000];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) ('a' + (i % 26));
        }

        gen.writeRaw(chars, 0, chars.length);
        gen.writeRaw(new String(chars));
        gen.close();

        assertEquals(10000, sw.toString().length());
    }
}