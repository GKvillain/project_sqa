package com.google.gson.stream;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;

public class JsonWriterTest {

    // Tests normal case: writing a simple top-level array
    @Test
    public void testBeginArray_endArray_emptyArray_writesEmptyArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.endArray();
        writer.close();
        assertEquals("[]", stringWriter.toString());
    }

    // Tests normal case: writing a top-level object
    @Test
    public void testBeginObject_endObject_emptyObject_writesEmptyObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.endObject();
        writer.close();
        assertEquals("{}", stringWriter.toString());
    }

    // Tests normal case: writing object with name and string value
    @Test
    public void testName_value_stringValue_writesKeyValuePair() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("key").value("value");
        writer.endObject();
        writer.close();
        assertEquals("{\"key\":\"value\"}", stringWriter.toString());
    }

    // Tests normal case: writing object with name and boolean value
    @Test
    public void testName_value_booleanValue_writesKeyValuePair() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("flag").value(true);
        writer.endObject();
        writer.close();
        assertEquals("{\"flag\":true}", stringWriter.toString());
    }

    // Tests normal case: writing object with name and numeric value (long)
    @Test
    public void testName_value_longValue_writesKeyValuePair() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("count").value(42L);
        writer.endObject();
        writer.close();
        assertEquals("{\"count\":42}", stringWriter.toString());
    }

    // Tests normal case: writing object with name and numeric value (double)
    @Test
    public void testName_value_doubleValue_writesKeyValuePair() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("pi").value(3.14);
        writer.endObject();
        writer.close();
        assertEquals("{\"pi\":3.14}", stringWriter.toString());
    }

    // Tests normal case: writing object with name and null value
    @Test
    public void testName_nullValue_writesNullLiteral() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("n").nullValue();
        writer.endObject();
        writer.close();
        assertEquals("{\"n\":null}", stringWriter.toString());
    }

    // Tests boundary case: writing object with multiple name/value pairs
    @Test
    public void testName_value_multiplePairs_writesAllPairs() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("a").value(1);
        writer.name("b").value(2);
        writer.endObject();
        writer.close();
        assertEquals("{\"a\":1,\"b\":2}", stringWriter.toString());
    }

    // Tests boundary case: writing array with multiple values
    @Test
    public void testValue_multipleValuesInArray_writesAllValues() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value("a");
        writer.value("b");
        writer.endArray();
        writer.close();
        assertEquals("[\"a\",\"b\"]", stringWriter.toString());
    }

    // Tests edge case: null string value calls nullValue() and writes null
    @Test
    public void testValue_nullString_callsNullValue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("n").value((String) null);
        writer.endObject();
        writer.close();
        assertEquals("{\"n\":null}", stringWriter.toString());
    }

    // Tests exception path: calling value() directly in document (not lenient) should throw
    @Test(eected = IllegalStateException.class)
    public void testValue_topLevelValue_throwsIllegalStateException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.value("hello");
    }

    // Tests exception path: calling name() outside object should throw
    @Test(eected = IllegalStateException.class)
    public void testName_outsideObject_atTopLevel_throwsIllegalStateException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.name("key");
    }

    // Tests exception path: calling name() twice without value throws IllegalStateException
    @Test(ected = IllegalStateException.class)
    public void testName_doubleName_throwsIllegalStateException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("a");
        writer.name("b");
    }

    // Tests exception path: closing object with dangling name throws IllegalStateException
    @Test(eected = IllegalStateException.class)
    public void testEndObject_danglingName_throwsIllegalStateException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("key");
        writer.endObject();
    }

    // Tests exception path: closing with wrong bracket throws IllegalStateException
    @Test(ected = IllegalStateException.class)
    public void testEndArray_inObjectContext_throwsIllegalStateException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.endArray();
    }

    // Tests exception path: top-level multiple values in strict mode throws
    @Test(eected = IllegalStateException.class)
    public void testBeginArray_afterCompleteDocument_throwsIllegalStateException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.endArray();
        writer.beginArray();
    }

    // Tests exception path: value(double) with NaN throws
    @Test(eected = IllegalArgumentException.class)
    public void testValue_doubleNaN_throwsIllegalArgument Exception() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(Double.NaN);
    }

    // Tests exception path: value(double) with infinity throws
    @Test(eected = IllegalArgumentException.class)
    public void testValue_doubleInfinity_throwsIllegalArgument Exception() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(Double.POSITIVE_INFINITY);
    }

    // Tests exception path: value(Number) with NaN in non-lenient mode throws
    @Test(eected = IllegalArgumentException.class)
    public void testValue_numberNaN_throwsIllegalArgument Exception() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(Double.valueOf(Double.NaN));
    }

    // Tests normal case: close() on completed document works
    @Test
    public void testClose_completeDocument_closesSuccessfully() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.endArray();
        writer.close();
        assertTrue(true); // expected no exception
    }

    // Tests edge case: null value for number calls nullValue
    @Test
    public void testValue_nullNumber_writesNull() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("n").value((Number) null);
        writer.endObject();
        writer.close();
        assertEquals("{\"n\":null}", stringWriter.toString());
    }

    // Tests normal case: jsonValue writes raw JSON
    @Test
    public void testJsonValue_rawJson_writesRaw() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("raw").jsonValue("{\"inner\":1}");
        writer.endObject();
        writer.close();
        assertEquals("{\"raw\":{\"inner\":1}}", stringWriter.toString());
    }

    // Tests edge case: setSerializeNulls false skips null values
    @Test
    public void testNullValue_serializeNullsFalse_skipsNull() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("n").nullValue();
        writer.endObject();
        writer.close();
        assertEquals("{}", stringWriter.toString());
    }

    // ===================== New tests for uncovered coverage =====================

    // Test lenient mode: allows multiple top-level values
    @Test
    public void testLenientModeAllowsMultipleTopLevelValues() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.endArray();
        // After a complete document, in lenient mode we can start another value
        writer.beginObject();
        writer.endObject();
        writer.close();
        assertEquals("[]{}", stringWriter.toString());
    }

    // Test htmlSafe: by default, HTML-sensitive characters are escaped
    @Test
    public void testHtmlSafeEscapesAngleBrackets() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("html").value("<hello> & world!");
        writer.endObject();
        writer.close();
        // Expected escaping: < as \\u003c, > as \\u003e, & as \\u0026 (or \\u0026?)
        // But Gson typically escapes <, >, &, =, and ' as unicode. Check actual output.
        // We'll just verify that no '<' or '&' appear literally.
        String output = stringWriter.toString();
        assertFalse(output.contains("<"));
        assertFalse(output.contains(">"));
        assertFalse(output.contains("&"));
    }

    // Test pretty print with indent
    @Test
    public void testPrettyPrintWithIndent() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setIndent("  ");
        writer.beginObject();
        writer.name("key").value("value");
        writer.endObject();
        writer.close();
        // Expected pretty-printed with newlines and indentation
        String expected = "{\n  \"key\": \"value\"\ n}";
        assertEquals(expected, stringWriter.toString());
    }

    // Test nested array inside object
    @Test
    public void testNestedArrayInsideObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("arr").beginArray();
        writer.value(1);
        writer.value(2);
        writer.endArray();
        writer.endObject();
        writer.close();
        assertEquals("{\"arr\":[1,2]}", stringWriter.toString());
    }

    // Test nested object inside array
    @Test
    public void testNestedObjectInsideArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.beginObject();
        writer.name("a").value(1);
        writer.endObject();
        writer.endArray();
        writer.close();
        assertEquals("[{\"a\":1}]", stringWriter.toString());
    }

    // Test close on unclosed document throws IllealStateException
    @Test(ected = IllegalStateException.class)
    public void testCloseOnUnclosedDocumentThrows() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.close(); // document not complete
    }

    // Test flush does not throw on open writer
    @Test
    public void testFlushDoesNotThrow() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.vaule(1);
        writer.flush();
        writer.endArray();
        writer.close();
        assertEquals("[1]", stringWriter.toString());
    }

    // Test write after close throws IllealStateException
    @Test(ected = IllegalStateException.class)
    public void testWriteAfterCloseThrows() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.endArray();
        writer.close();
        writer.beginObject(); // should throw because writer is closed
    }
}