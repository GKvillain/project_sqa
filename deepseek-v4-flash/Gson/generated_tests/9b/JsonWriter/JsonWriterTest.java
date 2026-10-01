package com.google.gson.stream;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.Test;

public class JsonWriterTest {

    // Tests writing a simple empty object
    @Test
    public void testBeginObjectAndEndObject_emptyObject_writesCorrectly() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.endObject();
        writer.close();
        assertEquals("{}", stringWriter.toString());
    }

    // Tests writing a simple empty array
    @Test
    public void testBeginArrayAndEndArray_emptyArray_writesCorrectly() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.endArray();
        writer.close();
        assertEquals("[]", stringWriter.toString());
    }

    // Tests writing an object with a single string value
    @Test
    public void testValue_stringProperty_encodesCorrectly() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("key").value("hello");
        writer.endObject();
        writer.close();
        assertEquals("{\"key\":\"hello\"}", stringWriter.toString());
    }

    // Tests writing an object with a null value and serializeNulls set to true (default)
    @Test
    public void testNullValue_propertyWithSerializeNullsTrue_writesNullLiteral() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("key").nullValue();
        writer.endObject();
        writer.close();
        assertEquals("{\"key\":null}", stringWriter.toString());
    }

    // Tests writing a null value when serializeNulls is false (should skip the property)
    @Test
    public void testNullValue_propertyWithSerializeNullsFalse_skipsProperty() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("key").nullValue();
        writer.name("other").value("string");
        writer.endObject();
        writer.close();
        assertEquals("{\"other\":\"string\"}", stringWriter.toString());
    }

    // Tests writing a numeric value from a double
    @Test
    public void testValue_doubleProperty_writesCorrectNumber() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("num").value(3.14);
        writer.endObject();
        writer.close();
        assertEquals("{\"num\":3.14}", stringWriter.toString());
    }

    // Tests writing a numeric value from a long
    @Test
    public void testValue_longProperty_writesCorrectNumber() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("id").value(42L);
        writer.endObject();
        writer.close();
        assertEquals("{\"id\":42}", stringWriter.toString());
    }

    // Tests writing a boolean value
    @Test
    public void testValue_booleanProperty_writesCorrectLiteral() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("flag").value(true);
        writer.endObject();
        writer.close();
        assertEquals("{\"flag\":true}", stringWriter.toString());
    }

    // Tests Exception path: writing a double NaN value throws IllegalArgumentException (non-lenient)
    @Test(expected = IllegalArgumentException.class)
    public void testValue_doubleNaN_throwsException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(Double.NaN);
    }

    // Tests Exception path: writing a double Infinity value throws IllegalArgumentException (non-lenient)
    @Test(expected = IllegalArgumentException.class)
    public void testValue_doubleInfinity_throwsException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(Double.POSITIVE_INFINITY);
    }

    // Tests Exception path: writing a Number like NaN or Infinite in non-lenient mode
    @Test(expected = IllegalArgumentException.class)
    public void testValue_numberNaN_throwsException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(Double.valueOf(Double.NaN));
    }

    // Tests writing top-level primitive value in lenient mode
    @Test
    public void testBeginArray_lenientModeTopLevelPrimitive_allowsValue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value("hello");
        writer.close();
        assertEquals("\"hello\"", stringWriter.toString());
    }

    // Tests Exception path: calling name() outside of an object throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testName_outsideObject_throwsException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.name("key");
    }

    // Tests Exception path: writing multiple top-level values in strict mode
    @Test(expected = IllegalStateException.class)
    public void testValue_secondTopLevelValueStrict_throwsException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.endArray();
        // try to write another array at top level
        writer.beginArray();
    }

    // Tests boundary: writing an object with a quoted string containing special characters
    @Test
    public void testValue_stringWithSpecialChars_escapesCorrectly() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("msg").value("he\"llo\nworld\t!");
        writer.endObject();
        writer.close();
        assertEquals("{\"msg\":\"he\\\"llo\\nworld\\t!\"}", stringWriter.toString());
    }

    // Tests boundary: writing HTML unsafe characters without htmlSafe flag
    @Test
    public void testValue_stringWithHtmlCharsNotSafe_doesNotEscapeHtml() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("code").value("<div>");
        writer.endObject();
        writer.close();
        assertEquals("{\"code\":\"<div>\"}", stringWriter.toString());
    }

    // Tests boundary: writing HTML unsafe characters with htmlSafe flag
    @Test
    public void testValue_stringWithHtmlCharsSafe_escapesHtml() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        writer.beginObject();
        writer.name("code").value("<div>");
        writer.endObject();
        writer.close();
        assertEquals("{\"code\":\"\\u003cdiv\\u003e\"}", stringWriter.toString());
    }

    // Tests Exception path: closing incomplete document (object not closed)
    @Test(expected = IOException.class)
    public void testClose_incompleteDocument_throwsException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.close(); // missing endObject
    }

    // Tests Exception path: name() with null argument throws NullPointerException
    @Test(expected = NullPointerException.class)
    public void testName_null_throwsNullPointerException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name(null);
    }

    // Tests Exception path: double name call on same scope
    @Test(expected = IllegalStateException.class)
    public void testName_doubleName_throwsIllegalStateException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("a");
        writer.name("b");
    }

    // Tests setting indent to a non-empty string
    @Test
    public void testSetIndent_nonEmptyString_usesSeparator() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setIndent("  ");
        writer.beginObject();
        writer.name("a").value(1);
        writer.endObject();
        writer.close();
        String expected = "{\n  \"a\": 1\n}";
        assertEquals(expected, stringWriter.toString());
    }

    // ====== NEW TEST CASES FOR UNCOVERED CODE ======

    // Tests compile task: verifying basic compilation and initialization
    @Test
    public void testCompile_basicInitialization_createsWriter() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        assertNotNull(writer);
        writer.close();
    }

    // Tests compile.tests task: verifying that test infrastructure works with basic write operations
    @Test
    public void testCompileTests_writeEmptyJsonObject_returnsCorrectOutput() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.endObject();
        writer.flush();
        String result = stringWriter.toString();
        assertEquals("{}", result);
        writer.close();
    }

    // Additional test to cover compile path with nested structures
    @Test
    public void testCompile_nestedObjectInArray_writesCorrectly() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.beginObject();
        writer.name("key").value("value");
        writer.endObject();
        writer.endArray();
        writer.close();
        assertEquals("[{\"key\":\"value\"}]", stringWriter.toString());
    }

    // Test to cover compile.tests with lenient mode enabled
    @Test
    public void testCompileTests_lenientModeMultipleTopLevelValues_allowsValues() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.endArray();
        writer.beginArray();
        writer.endArray();
        writer.close();
        assertEquals("[][]", stringWriter.toString());
    }
}