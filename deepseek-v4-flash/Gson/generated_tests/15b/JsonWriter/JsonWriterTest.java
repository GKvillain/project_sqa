package com.google.gson.stream;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * JUnit 4 test class for com.google.gson.stream.JsonWriter.
 * Targets Defects4J bug 15b.
 */
public class JsonWriterTest {

    // Helper to create a new JsonWriter with a StringWriter
    private JsonWriter createWriter() {
        StringWriter stringWriter = new StringWriter();
        return new JsonWriter(stringWriter);
    }

    private JsonWriter createLenientWriter() {
        JsonWriter writer = createWriter();
        writer.setLenient(true);
        return writer;
    }

    // Helper to get the output string
    private String getOutput(JsonWriter writer) {
        return writer.toString(); // toString on StringWriter is inherited from Object
        // We need to use the StringWriter directly
        // Actually, JsonWriter doesn't expose the Writer. We'll use a custom approach.
        // Let's create a helper that returns the StringWriter content.
    }

    // Better helper: use StringWriter and return its content after closing.
    private String writeAndGet(JsonWriter writer) throws IOException {
        // Assuming the writer is closed, but we can flush and get content without closing
        StringWriter sw = (StringWriter) writer.out; // Accessing private field out is not allowed
        // We need to avoid accessing private fields. Let's use a different approach:
        // We'll pass the StringWriter to the constructor and track it.
        // For simplicity in the test, we'll use the helper that passes StringWriter.
        // This is standard practice for testing JsonWriter.
        return null; // placeholder
    }

    // To avoid accessing private fields, we'll inline the creation and inspection.
    // We'll create a method that takes a StringWriter and JsonWriter and returns the string.

    private String getStringFromWriter(StringWriter sw) {
        return sw.toString();
    }

    // ---------- Tests ----------

    // Tests basic writing of a top-level string value
    @Test
    public void testValue_string_basic() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.setLenient(true);
        writer.value("hello");
        writer.close();
        assertEquals("\"hello\"", sw.toString());
    }

    // Tests writing null value
    @Test
    public void testNullValue_basic() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.setLenient(true);
        writer.nullValue();
        writer.close();
        assertEquals("null", sw.toString());
    }

    // Tests writing a boolean value
    @Test
    public void testValue_boolean_basic() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.setLenient(true);
        writer.value(true);
        writer.close();
        assertEquals("true", sw.toString());
    }

    // Tests writing a double value
    @Test
    public void testValue_double_basic() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.setLenient(true);
        writer.value(3.14);
        writer.close();
        assertEquals("3.14", sw.toString());
    }

    // Tests writing a long value
    @Test
    public void testValue_long_basic() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.setLenient(true);
        writer.value(42L);
        writer.close();
        assertEquals("42", sw.toString());
    }

    // Tests writing a Number (integer)
    @Test
    public void testValue_number_basic() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.setLenient(true);
        writer.value(100);
        writer.close();
        assertEquals("100", sw.toString());
    }

    // Tests beginArray and endArray
    @Test
    public void testBeginArray_endArray_basic() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.setLenient(true);
        writer.beginArray();
        writer.endArray();
        writer.close();
        assertEquals("[]", sw.toString());
    }

    // Tests beginObject and endObject
    @Test
    public void testBeginObject_endObject_basic() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.setLenient(true);
        writer.beginObject();
        writer.endObject();
        writer.close();
        assertEquals("{}", sw.toString());
    }

    // Tests writing a name and value in an object
    @Test
    public void testName_value_basic() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.beginObject();
        writer.name("a").value(1);
        writer.endObject();
        writer.close();
        assertEquals("{\"a\":1}", sw.toString());
    }

    // Tests serializing null with serializeNulls = true (default)
    @Test
    public void testNullValue_serializeNullsTrue() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.beginObject();
        writer.name("a").nullValue();
        writer.endObject();
        writer.close();
        assertEquals("{\"a\":null}", sw.toString());
    }

    // Tests serializing null with serializeNulls = false (should skip)
    @Test
    public void testNullValue_serializeNullsFalse_skips() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("a").nullValue();
        writer.endObject();
        writer.close();
        assertEquals("{}", sw.toString());
    }

    // Tests writing HTML-safe characters
    @Test
    public void testValue_htmlSafe_escapes() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.setHtmlSafe(true);
        writer.setLenient(true);
        writer.value("<>&");
        writer.close();
        assertEquals("\"\\u003c\\u003e\\u0026\"", sw.toString());
    }

    // Tests deferredName behavior: calling name() without value should throw
    @Test(expected = IllegalStateException.class)
    public void testName_deferredName_dangling_throws() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.beginObject();
        writer.name("a");
        writer.name("b"); // should throw
    }

    // Tests calling value() after close() – should throw IllegalStateException via peek()
    @Test(expected = IllegalStateException.class)
    public void testValue_closed_throws() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.setLenient(true);
        writer.close();
        writer.value("x");
    }

    // Tests close() with incomplete document (top-level array not closed)
    @Test(expected = IOException.class)
    public void testClose_incompleteDocument_throws() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.beginArray();
        writer.close(); // should throw because array not ended
    }

    // Tests writing NaN in strict mode should throw
    @Test(expected = IllegalArgumentException.class)
    public void testValue_double_NaN_strict_throws() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.setLenient(false);
        writer.value(Double.NaN);
    }

    // Tests writing NaN in lenient mode should work (but careful: may produce "NaN")
    @Test
    public void testValue_double_NaN_lenient_writes() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.setLenient(true);
        writer.value(Double.NaN);
        writer.close();
        assertEquals("NaN", sw.toString());
    }

    // Tests Number with NaN string representation and lenient false
    @Test(expected = IllegalArgumentException.class)
    public void testValue_number_NaN_strict_throws() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.setLenient(false);
        writer.value(Double.NaN);
    }

    // Tests writing number Infinity in lenient mode
    @Test
    public void testValue_number_Infinity_lenient_writes() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.setLenient(true);
        writer.value(Double.POSITIVE_INFINITY);
        writer.close();
        assertEquals("Infinity", sw.toString());
    }

    // Tests that jsonValue writes raw string
    @Test
    public void testJsonValue_basic() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.setLenient(true);
        writer.jsonValue("raw");
        writer.close();
        assertEquals("raw", sw.toString());
    }

    // Tests that jsonValue with null writes null
    @Test
    public void testJsonValue_null_writesNull() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.setLenient(true);
        writer.jsonValue(null);
        writer.close();
        assertEquals("null", sw.toString());
    }

    // Tests writing nested objects with names
    @Test
    public void testNestedObject() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.beginObject();
        writer.name("user");
        writer.beginObject();
        writer.name("name").value("Alice");
        writer.endObject();
        writer.endObject();
        writer.close();
        assertEquals("{\"user\":{\"name\":\"Alice\"}}", sw.toString());
    }

    // ---------- Additional tests to improve coverage ----------

    // Tests string value with escaped characters (quote, backslash, newline, tab)
    @Test
    public void testValue_string_escapes() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.setLenient(true);
        writer.value("a\"b\\c\nd\te");
        writer.close();
        assertEquals("\"a\\\"b\\\\c\\nd\\te\"", sw.toString());
    }

    // Tests writing a negative number
    @Test
    public void testValue_number_negative() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.setLenient(true);
        writer.value(-42);
        writer.close();
        assertEquals("-42", sw.toString());
    }

    // Tests nested arrays
    @Test
    public void testBeginArray_nested() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.setLenient(true);
        writer.beginArray();
        writer.beginArray();
        writer.value(1);
        writer.endArray();
        writer.endArray();
        writer.close();
        assertEquals("[[1]]", sw.toString());
    }

    // Tests nested objects inside arrays
    @Test
    public void testBeginArray_nestedObject() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.setLenient(true);
        writer.beginArray();
        writer.beginObject();
        writer.name("key").value("val");
        writer.endObject();
        writer.endArray();
        writer.close();
        assertEquals("[{\"key\":\"val\"}]", sw.toString());
    }

    // Tests pretty printing with indent
    @Test
    public void testIndent_prettyPrint() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.setIndent("  ");
        writer.beginObject();
        writer.name("a").value(1);
        writer.name("b").value(2);
        writer.endObject();
        writer.close();
        String expected = "{\n  \"a\": 1,\n  \"b\": 2\n}";
        assertEquals(expected, sw.toString());
    }

    // Tests flush() behavior – output should be flushed to the underlying writer
    @Test
    public void testFlush() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.setLenient(true);
        writer.value("test");
        writer.flush();
        assertEquals("\"test\"", sw.toString());
    }

    // Tests object with multiple name-value pairs
    @Test
    public void testName_value_multiple() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.beginObject();
        writer.name("x").value(10);
        writer.name("y").value(20);
        writer.endObject();
        writer.close();
        assertEquals("{\"x\":10,\"y\":20}", sw.toString());
    }

    // Tests string with unicode escape sequences
    @Test
    public void testValue_string_unicode() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.setLenient(true);
        writer.value("A\u0042C"); // A, B (unicode 0042), C
        writer.close();
        assertEquals("\"ABC\"", sw.toString()); // \u0042 is B, so it's written as "ABC"
    }

    // Tests deferred name reset after value is written (calling name again should work)
    @Test
    public void testDeferredName_afterValue() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.beginObject();
        writer.name("a").value(1);
        writer.name("b").value(2); // should not throw after previous value
        writer.endObject();
        writer.close();
        assertEquals("{\"a\":1,\"b\":2}", sw.toString());
    }

    // Tests closing a writer twice (should not throw)
    @Test
    public void testClose_twice() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        writer.setLenient(true);
        writer.value("ok");
        writer.close();
        writer.close(); // second close should be harmless
        assertEquals("\"ok\"", sw.toString());
    }
}