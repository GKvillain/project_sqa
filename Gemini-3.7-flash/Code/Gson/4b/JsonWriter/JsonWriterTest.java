package com.google.gson.stream;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JsonWriterTest {

    // Tests empty JSON object creation
    @Test
    public void testBeginEndObject_emptyObject_producesCorrectJson() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.endObject();
        writer.close();
        assertEquals("{}", stringWriter.toString());
    }

    // Tests empty JSON array creation
    @Test
    public void testBeginEndArray_emptyArray_producesCorrectJson() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.endArray();
        writer.close();
        assertEquals("[]", stringWriter.toString());
    }

    // Tests writing nested JSON object with various primitive values
    @Test
    public void testWriteObject_multipleTypes_producesCorrectJson() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("name").value("Gson");
        writer.name("version").value(1L);
        writer.name("score").value(99.5);
        writer.name("active").value(true);
        writer.name("notes").nullValue();
        writer.endObject();
        writer.close();
        assertEquals("{\"name\":\"Gson\",\"version\":1,\"score\":99.5,\"active\":true,\"notes\":null}", stringWriter.toString());
    }

    // Tests indentation and pretty printing
    @Test
    public void testSetIndent_prettyPrint_formatsWithWhitespace() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setIndent("  ");
        writer.beginArray();
        writer.value("a");
        writer.value("b");
        writer.endArray();
        writer.close();
        assertEquals("[\n  \"a\",\n  \"b\"\n]", stringWriter.toString());
    }

    // Tests resetting indentation to empty string
    @Test
    public void testSetIndent_emptyString_disablesPrettyPrint() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setIndent("  ");
        writer.setIndent("");
        writer.beginArray();
        writer.value(1);
        writer.endArray();
        writer.close();
        assertEquals("[1]", stringWriter.toString());
    }

    // Tests HTML characters escaping when htmlSafe is enabled
    @Test
    public void testSetHtmlSafe_htmlCharacters_escapesSpecialChars() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        assertTrue(writer.isHtmlSafe());
        writer.beginArray();
        writer.value("<tag>&'\"=");
        writer.endArray();
        writer.close();
        assertEquals("[\"\\u003ctag\\u003e\\u0026\\u0027\\\"\\u003d\"]", stringWriter.toString());
    }

    // Tests serialization of nulls when serializeNulls is false
    @Test
    public void testSetSerializeNulls_false_skipsNullObjectProperties() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setSerializeNulls(false);
        assertFalse(writer.getSerializeNulls());
        writer.beginObject();
        writer.name("keep").value("value");
        writer.name("skip").nullValue();
        writer.endObject();
        writer.close();
        assertEquals("{\"keep\":\"value\"}", stringWriter.toString());
    }

    // Tests string escaping for control characters and newlines
    @Test
    public void testValue_specialCharacters_escapesCorrectly() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value("slash\\quote\"tab\tnewline\ncarriage\rformfeed\fbackspace\blineSep\u2028paraSep\u2029");
        writer.endArray();
        writer.close();
        assertEquals("[\"slash\\\\quote\\\"tab\\tnewline\\ncarriage\\rformfeed\\fbackspace\\blineSep\\u2028paraSep\\u2029\"]", stringWriter.toString());
    }

    // Tests jsonValue direct insertion
    @Test
    public void testJsonValue_rawJson_appendsVerbatim() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.jsonValue("{\"raw\":true}");
        writer.jsonValue(null);
        writer.endArray();
        writer.close();
        assertEquals("[{\"raw\":true},null]", stringWriter.toString());
    }

    // Tests value with Number object
    @Test
    public void testValue_numberObject_writesCorrectly() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Number) 123.45);
        writer.value((Number) null);
        writer.endArray();
        writer.close();
        assertEquals("[123.45,null]", stringWriter.toString());
    }

    // Tests lenient mode with top-level primitive and NaN/Infinity values
    @Test
    public void testSetLenient_lenientMode_allowsTopLevelValueAndSpecialDoubles() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        assertTrue(writer.isLenient());
        writer.value(Double.NaN);
        writer.close();
        assertEquals("NaN", stringWriter.toString());
    }

    // Tests non-finite double in non-lenient mode throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testValue_doubleNaNStrict_throwsIllegalArgumentException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(Double.NaN);
    }

    // Tests non-finite Number in non-lenient mode throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testValue_numberInfinityStrict_throwsIllegalArgumentException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(Double.valueOf(Double.POSITIVE_INFINITY));
    }

    // Tests null writer in constructor
    @Test(expected = NullPointerException.class)
    public void testConstructor_nullWriter_throwsNullPointerException() {
        new JsonWriter(null);
    }

    // Tests null property name
    @Test(expected = NullPointerException.class)
    public void testName_nullName_throwsNullPointerException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name(null);
    }

    // Tests duplicate name call without value
    @Test(expected = IllegalStateException.class)
    public void testName_consecutiveNames_throwsIllegalStateException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("name1");
        writer.name("name2");
    }

    // Tests top-level primitive value in strict mode
    @Test(expected = IllegalStateException.class)
    public void testStrictTopLevelValue_throwsIllegalStateException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.value("top-level");
    }

    // Tests mismatched closing bracket
    @Test(expected = IllegalStateException.class)
    public void testEndArray_insideObject_throwsIllegalStateException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.endArray();
    }

    // Tests closing with unclosed scopes throws IOException
    @Test(expected = IOException.class)
    public void testClose_incompleteDocument_throwsIOException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.close();
    }

    // Tests calling flush on closed writer throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testFlush_closedWriter_throwsIllegalStateException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.endArray();
        writer.close();
        writer.flush();
    }

    // Tests Boolean wrapper objects including null
    @Test
    public void testValue_booleanWrapper_writesCorrectly() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(Boolean.TRUE);
        writer.value(Boolean.FALSE);
        writer.value((Boolean) null);
        writer.endArray();
        writer.close();
        assertEquals("[true,false,null]", stringWriter.toString());
    }

    // Tests null String value
    @Test
    public void testValue_nullString_writesNull() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((String) null);
        writer.endArray();
        writer.close();
        assertEquals("[null]", stringWriter.toString());
    }

    // Tests BigInteger and BigDecimal numbers
    @Test
    public void testValue_arbitraryPrecisionNumbers_writesExactRepresentation() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(new BigInteger("12345678901234567890"));
        writer.value(new BigDecimal("12345.678901234567890"));
        writer.endArray();
        writer.close();
        assertEquals("[12345678901234567890,12345.678901234567890]", stringWriter.toString());
    }

    // Tests low ASCII control character escaping (e.g., \u0000, \u001f)
    @Test
    public void testValue_lowControlCharacters_escapesToUnicodeSequence() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value("\u0000\u001f");
        writer.endArray();
        writer.close();
        assertEquals("[\"\\u0000\\u001f\"]", stringWriter.toString());
    }

    // Tests stack reallocation for deep nesting (depth > 32)
    @Test
    public void testDeepNesting_stackExpansion_producesValidJson() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        for (int i = 0; i < 40; i++) {
            writer.beginArray();
        }
        writer.value("deep");
        for (int i = 0; i < 40; i++) {
            writer.endArray();
        }
        writer.close();
        StringBuilder expected = new StringBuilder();
        for (int i = 0; i < 40; i++) {
            expected.append('[');
        }
        expected.append("\"deep\"");
        for (int i = 0; i < 40; i++) {
            expected.append(']');
        }
        assertEquals(expected.toString(), stringWriter.toString());
    }

    // Tests multiple top-level values and special doubles in lenient mode
    @Test
    public void testLenient_multipleTopLevelValuesAndNegativeInfinity_allowed() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(Double.NEGATIVE_INFINITY);
        writer.value(Double.POSITIVE_INFINITY);
        writer.value("second-top-level");
        writer.close();
        assertEquals("-InfinityInfinity\"second-top-level\"", stringWriter.toString());
    }

    // Tests calling endObject on empty writer (top-level)
    @Test(expected = IllegalStateException.class)
    public void testEndObject_emptyWriter_throwsIllegalStateException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.endObject();
    }

    // Tests calling endArray on empty writer (top-level)
    @Test(expected = IllegalStateException.class)
    public void testEndArray_emptyWriter_throwsIllegalStateException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.endArray();
    }

    // Tests calling endObject inside array throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testEndObject_insideArray_throwsIllegalStateException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.endObject();
    }

    // Tests calling name() outside of an object
    @Test(expected = IllegalStateException.class)
    public void testName_outsideObject_throwsIllegalStateException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.name("name");
    }

    // Tests calling value() inside an object without calling name() first
    @Test(expected = IllegalStateException.class)
    public void testValue_insideObjectWithoutName_throwsIllegalStateException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.value("value");
    }

    // Tests close on empty document throws IOException
    @Test(expected = IOException.class)
    public void testClose_emptyDocument_throwsIOException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.close();
    }

    // Tests calling value on closed writer
    @Test(expected = IllegalStateException.class)
    public void testValue_closedWriter_throwsIllegalStateException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.endArray();
        writer.close();
        writer.value("test");
    }

    // Tests calling beginObject on closed writer
    @Test(expected = IllegalStateException.class)
    public void testBeginObject_closedWriter_throwsIllegalStateException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.endArray();
        writer.close();
        writer.beginObject();
    }

    // Tests calling beginArray on closed writer
    @Test(expected = IllegalStateException.class)
    public void testBeginArray_closedWriter_throwsIllegalStateException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.endArray();
        writer.close();
        writer.beginArray();
    }

    // Tests flush writes buffered data to underlying writer
    @Test
    public void testFlush_openWriter_flushesData() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value("flushed");
        writer.flush();
        assertEquals("[\"flushed\"", stringWriter.toString());
        writer.endArray();
        writer.close();
    }
}