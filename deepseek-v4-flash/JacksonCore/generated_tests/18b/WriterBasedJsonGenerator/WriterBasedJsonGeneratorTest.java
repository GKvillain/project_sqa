package com.fasterxml.jackson.core.json;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.SerializedString;

public class WriterBasedJsonGeneratorTest {

    private JsonGenerator createGenerator(StringWriter sw) throws IOException {
        return new JsonFactory().createGenerator(sw);
    }

    // Tests empty object output
    @Test
    public void testWriteStartObjectEndObject_emptyObject_writesEmptyObject() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        g.writeStartObject();
        g.writeEndObject();
        g.close();
        assertEquals("{}", sw.toString());
    }

    // Tests empty array output
    @Test
    public void testWriteStartArrayEndArray_emptyArray_writesEmptyArray() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        g.writeStartArray();
        g.writeEndArray();
        g.close();
        assertEquals("[]", sw.toString());
    }

    // Tests single field name/value object
    @Test
    public void testWriteFieldName_singleField_writesObject() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        g.writeStartObject();
        g.writeFieldName("a");
        g.writeNumber(1);
        g.writeEndObject();
        g.close();
        assertEquals("{\"a\":1}", sw.toString());
    }

    // Tests comma separator between multiple fields
    @Test
    public void testWriteFieldName_multipleFields_writesCommaSeparatedObject() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        g.writeStartObject();
        g.writeFieldName("a");
        g.writeNumber(1);
        g.writeFieldName("b");
        g.writeString("x");
        g.writeEndObject();
        g.close();
        assertEquals("{\"a\":1,\"b\":\"x\"}", sw.toString());
    }

    // Tests empty string value
    @Test
    public void testWriteString_emptyString_writesQuotedEmpty() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        g.writeString("");
        g.close();
        assertEquals("\"\"", sw.toString());
    }

    // Tests null string value is written as null literal
    @Test
    public void testWriteString_nullValue_writesNull() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        g.writeString(null);
        g.close();
        assertEquals("null", sw.toString());
    }

    // Tests quote and newline escaping
    @Test
    public void testWriteString_specialCharacters_writesEscapedValue() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        g.writeString("he said \"hi\"\n");
        g.close();
        assertEquals("\"he said \\\"hi\\\"\\n\"", sw.toString());
    }

    // Tests long string that must be streamed through segment logic
    @Test
    public void testWriteString_longStringWithEscape_writesValidEscapedString() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);

        int prefix = 1500;
        int suffix = 9000;
        StringBuilder sb = new StringBuilder(prefix + suffix + 1);
        for (int i = 0; i < prefix; i++) {
            sb.append('a');
        }
        sb.append('"');
        for (int i = 0; i < suffix; i++) {
            sb.append('b');
        }
        String input = sb.toString();

        g.writeString(input);
        g.close();

        JsonParser p = new JsonFactory().createParser(sw.toString());
        try {
            assertEquals(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals(input, p.getText());
        } finally {
            p.close();
        }
    }

    // Tests char[] string writing with an escaped character
    @Test
    public void testWriteString_charArrayWithEscape_writesEscapedValue() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        char[] text = new char[] {'a', '"', 'b'};
        g.writeString(text, 0, text.length);
        g.close();
        assertEquals("\"a\\\"b\"", sw.toString());
    }

    // Tests char[] writing with an unescaped span longer than SHORT_WRITE
    @Test
    public void testWriteString_charArrayLongSpan_writesEscapedValue() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);

        char[] text = new char[50];
        for (int i = 0; i < text.length; i++) {
            text[i] = 'a';
        }
        text[35] = '"';

        g.writeString(text, 0, text.length);
        g.close();

        JsonParser p = new JsonFactory().createParser(sw.toString());
        try {
            assertEquals(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals(new String(text), p.getText());
        } finally {
            p.close();
        }
    }

    // Tests SerializableString is not double-quoted
    @Test
    public void testWriteString_serializableString_writesSingleQuotedValue() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        g.writeString(new SerializedString("foo"));
        g.close();
        assertEquals("\"foo\"", sw.toString());
    }

    // Tests SerializableString field name is not double-quoted
    @Test
    public void testWriteFieldName_serializableString_writesSingleQuotedName() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        g.writeStartObject();
        g.writeFieldName(new SerializedString("foo"));
        g.writeString("bar");
        g.writeEndObject();
        g.close();
        assertEquals("{\"foo\":\"bar\"}", sw.toString());
    }

    // Tests raw string output
    @Test
    public void testWriteRaw_string_writesRaw() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        g.writeRaw("abc");
        g.close();
        assertEquals("abc", sw.toString());
    }

    // Tests raw string output with offset and length
    @Test
    public void testWriteRaw_stringSubstring_writesRawSubstring() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        g.writeRaw("abcdef", 2, 3);
        g.close();
        assertEquals("cde", sw.toString());
    }

    // Tests raw char[] output
    @Test
    public void testWriteRaw_charArray_writesRaw() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        g.writeRaw(new char[] {'x', 'y', 'z'}, 1, 2);
        g.close();
        assertEquals("yz", sw.toString());
    }

    // Tests short, int and long number output
    @Test
    public void testWriteNumber_integerTypes_writesNumbers() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        g.writeStartArray();
        g.writeNumber((short) -32768);
        g.writeNumber(42);
        g.writeNumber(Long.MIN_VALUE);
        g.writeEndArray();
        g.close();
        assertEquals("[-32768,42,-9223372036854775808]", sw.toString());
    }

    // Tests floating point, BigInteger and BigDecimal output
    @Test
    public void testWriteNumber_floatingPointAndBigNumbers_writesNumbers() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        g.writeStartArray();
        g.writeNumber(1.5d);
        g.writeNumber(2.5f);
        g.writeNumber(new BigInteger("12345678901234567890"));
        g.writeNumber(new BigDecimal("123.45"));
        g.writeEndArray();
        g.close();
        assertEquals("[1.5,2.5,12345678901234567890,123.45]", sw.toString());
    }

    // Tests non-numeric doubles are quoted when feature is enabled
    @Test
    public void testWriteNumber_nonNumericWithQuoting_writesQuotedValue() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        g.enable(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS);
        g.writeNumber(Double.NaN);
        g.close();
        assertEquals("\"NaN\"", sw.toString());
    }

    // Tests boolean and null literal output
    @Test
    public void testWriteBooleanAndNull_writesLiterals() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        g.writeStartArray();
        g.writeBoolean(true);
        g.writeBoolean(false);
        g.writeNull();
        g.writeEndArray();
        g.close();
        assertEquals("[true,false,null]", sw.toString());
    }

    // Tests base64 binary value output
    @Test
    public void testWriteBinary_byteArray_writesBase64() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        g.writeBinary(Base64Variants.getDefaultVariant(), new byte[] {1, 2, 3}, 0, 3);
        g.close();
        assertEquals("\"AQID\"", sw.toString());
    }

    // Tests invalid field name context raises exception
    @Test(expected = JsonGenerationException.class)
    public void testWriteFieldName_inArray_throwsJsonGenerationException() throws Exception {
        JsonGenerator g = createGenerator(new StringWriter());
        g.writeStartArray();
        g.writeFieldName("name");
        g.close();
    }

    // Tests close() auto-closes open object when feature is enabled
    @Test
    public void testClose_unclosedObject_autoClosesObject() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        g.enable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);
        g.writeStartObject();
        g.writeFieldName("a");
        g.writeNumber(1);
        g.close();
        assertEquals("{\"a\":1}", sw.toString());
    }

    // Additional tests for previously uncovered paths

    @Test
    public void testWriteString_controlCharactersAndNul_writesEscapedValue() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        String value = "a\u0000b\tc\rd\ne\"f\\g\fb";
        g.writeString(value);
        g.close();

        JsonParser p = new JsonFactory().createParser(sw.toString());
        try {
            assertEquals(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals(value, p.getText());
        } finally {
            p.close();
        }
    }

    @Test
    public void testWriteString_nonAscii_whenEscapingEnabled_writesEscapedValue() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        g.setHighestNonEscapedChar(127);
        String value = "héllo";
        g.writeString(value);
        g.close();

        assertTrue(sw.toString().toLowerCase().contains("\\u00e9"));

        JsonParser p = new JsonFactory().createParser(sw.toString());
        try {
            assertEquals(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals(value, p.getText());
        } finally {
            p.close();
        }
    }

    @Test
    public void testWriteString_surrogatePair_writesValidString() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        String value = "Hello \uD83D\uDE00!";
        g.writeString(value);
        g.close();

        JsonParser p = new JsonFactory().createParser(sw.toString());
        try {
            assertEquals(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals(value, p.getText());
        } finally {
            p.close();
        }
    }

    @Test
    public void testWriteFieldName_escapedChars_writesEscapedFieldName() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        g.writeStartObject();
        g.writeFieldName("a\"b\\c");
        g.writeString("x");
        g.writeEndObject();
        g.close();

        JsonParser p = new JsonFactory().createParser(sw.toString());
        try {
            assertEquals(JsonToken.START_OBJECT, p.nextToken());
            assertEquals(JsonToken.FIELD_NAME, p.nextToken());
            assertEquals("a\"b\\c", p.getCurrentName());
            assertEquals(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals("x", p.getText());
            assertEquals(JsonToken.END_OBJECT, p.nextToken());
        } finally {
            p.close();
        }
    }

    @Test
    public void testWriteFieldName_unquoted_whenFeatureDisabled_writesUnquotedFieldName() throws Exception {
        StringWriter sw = new StringWriter();
        JsonFactory f = new JsonFactory();
        f.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        JsonGenerator g = f.createGenerator(sw);
        g.writeStartObject();
        g.writeFieldName("foo");
        g.writeString("bar");
        g.writeEndObject();
        g.close();
        assertEquals("{foo:\"bar\"}", sw.toString());
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteFieldName_duplicateWithStrictDetection_throwsJsonGenerationException() throws Exception {
        StringWriter sw = new StringWriter();
        JsonFactory f = new JsonFactory();
        f.enable(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION);
        JsonGenerator g = f.createGenerator(sw);
        g.writeStartObject();
        g.writeFieldName("dup");
        g.writeNumber(1);
        g.writeFieldName("dup");
        g.writeNumber(2);
        g.close();
    }

    @Test
    public void testWriteRaw_char_writesRawCharacter() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        g.writeRaw('X');
        g.close();
        assertEquals("X", sw.toString());
    }

    @Test
    public void testWriteRawValue_string_writesRawValue() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        g.writeStartArray();
        g.writeRawValue("1");
        g.writeRawValue("2");
        g.writeEndArray();
        g.close();
        assertEquals("[1,2]", sw.toString());
    }

    @Test
    public void testWriteRawValue_stringSubstring_writesRawValueSubstring() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        g.writeStartArray();
        g.writeRawValue("12345", 1, 3);
        g.writeEndArray();
        g.close();
        assertEquals("[234]", sw.toString());
    }

    @Test
    public void testWriteRawValue_charArray_writesRawValueCharArray() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        g.writeStartArray();
        g.writeRawValue(new char[] {'1', '.', '5'}, 0, 3);
        g.writeEndArray();
        g.close();
        assertEquals("[1.5]", sw.toString());
    }

    @Test
    public void testWriteNumber_string_writesNumber() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        g.writeStartArray();
        g.writeNumber("1e5");
        g.writeEndArray();
        g.close();
        assertEquals("[1e5]", sw.toString());
    }

    @Test
    public void testWriteNumber_integerWithNumbersAsStrings_writesQuotedNumbers() throws Exception {
        StringWriter sw = new StringWriter();
        JsonFactory f = new JsonFactory();
        f.enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
        JsonGenerator g = f.createGenerator(sw);
        g.writeStartArray();
        g.writeNumber(42);
        g.writeNumber(1.5d);
        g.writeEndArray();
        g.close();
        assertEquals("[\"42\",\"1.5\"]", sw.toString());
    }

    @Test
    public void testWriteNumber_bigDecimalPlain_writesPlainString() throws Exception {
        StringWriter sw = new StringWriter();
        JsonFactory f = new JsonFactory();
        f.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        JsonGenerator g = f.createGenerator(sw);
        g.writeStartArray();
        g.writeNumber(new BigDecimal("1E+3"));
        g.writeEndArray();
        g.close();
        assertEquals("[1000]", sw.toString());
    }

    @Test
    public void testWriteBinary_offsetAndLength_writesBase64Subset() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        byte[] data = new byte[] {1, 2, 3, 4, 5};
        g.writeBinary(Base64Variants.MIME_NO_LINEFEEDS, data, 1, 3);
        g.close();
        assertEquals("\"AgME\"", sw.toString());
    }

    @Test
    public void testWriteBinary_emptyArray_writesEmptyString() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        g.writeBinary(Base64Variants.getDefaultVariant(), new byte[0], 0, 0);
        g.close();
        assertEquals("\"\"", sw.toString());
    }

    @Test
    public void testFlush_writesAndFlushesWithoutClosing() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = createGenerator(sw);
        g.writeStartArray();
        g.writeNumber(1);
        g.flush();
        g.writeNumber(2);
        g.writeEndArray();
        g.close();
        assertEquals("[1,2]", sw.toString());
    }

    @Test(expected = JsonGenerationException.class)
    public void testWriteEndObject_inArray_throwsJsonGenerationException() throws Exception {
        JsonGenerator g = createGenerator(new StringWriter());
        g.writeStartArray();
        g.writeEndObject();
        g.close();
    }
}