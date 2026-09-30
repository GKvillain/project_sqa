package com.google.gson.stream;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.EOFException;
import java.io.IOException;
import java.io.StringReader;

public class JsonReaderTest {

    // Test basic read of a simple string value
    @Test
    public void testNextString_simpleString_returnsString() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"hello\""));
        assertEquals("hello", reader.nextString());
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        reader.close();
    }

    // Test reading integer value as long
    @Test
    public void testNextLong_positiveInteger_returnsLong() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("42"));
        assertEquals(42L, reader.nextLong());
        reader.close();
    }

    // Test reading integer value as int
    @Test
    public void testNextInt_positiveInteger_returnsInt() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("123"));
        assertEquals(123, reader.nextInt());
        reader.close();
    }

    // Test reading double value
    @Test
    public void testNextDouble_positiveDouble_returnsDouble() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("3.14"));
        assertEquals(3.14, reader.nextDouble(), 0.0001);
        reader.close();
    }

    // Test boolean true
    @Test
    public void testNextBoolean_trueValue_returnsTrue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("true"));
        assertTrue(reader.nextBoolean());
        reader.close();
    }

    // Test boolean false
    @Test
    public void testNextBoolean_falseValue_returnsFalse() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("false"));
        assertFalse(reader.nextBoolean());
        reader.close();
    }

    // Test null value
    @Test
    public void testNextNull_nullLiteral_consumesNull() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("null"));
        reader.nextNull();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        reader.close();
    }

    // Test simple object with one name-value pair
    @Test
    public void testBeginEndObject_singlePair_readsCorrectly() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\"key\":\"value\"}"));
        reader.beginObject();
        assertEquals("key", reader.nextName());
        assertEquals("value", reader.nextString());
        reader.endObject();
        reader.close();
    }

    // Test simple array with one value
    @Test
    public void testBeginEndArray_singleValue_readsCorrectly() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1]"));
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        reader.endArray();
        reader.close();
    }

    // Test hasNext in array
    @Test
    public void testHasNext_nonEmptyArray_returnsTrue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1,2]"));
        reader.beginArray();
        assertTrue(reader.hasNext());
        reader.nextInt();
        assertTrue(reader.hasNext());
        reader.nextInt();
        assertFalse(reader.hasNext());
        reader.endArray();
        reader.close();
    }

    // Test EOFException for empty input in strict mode
    @Test(expected = EOFException.class)
    public void testPeek_emptyInput_throwsEOFException() throws IOException {
        JsonReader reader = new JsonReader(new StringReader(""));
        reader.peek();
        reader.close();
    }

    // Test IllegalStateException when expecting BEGIN_ARRAY
    @Test(expected = IllegalStateException.class)
    public void testBeginArray_nonArray_throwsException() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"string\""));
        reader.beginArray();
        reader.close();
    }

    // Test IllegalStateException when expecting NAME
    @Test(expected = IllegalStateException.class)
    public void testNextName_nonObject_throwsException() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1]"));
        reader.nextName();
        reader.close();
    }

    // Test reading number from string (lenient mode)
    @Test
    public void testNextLong_stringNumberInLenient_parsesLong() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"123\""));
        reader.setLenient(true);
        assertEquals(123L, reader.nextLong());
        reader.close();
    }

    // Test skipValue skips nested object
    @Test
    public void testSkipValue_nestedObject_skipsCorrectly() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\"a\":{\"b\":1}}"));
        reader.beginObject();
        assertEquals("a", reader.nextName());
        reader.skipValue();
        reader.endObject();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        reader.close();
    }

    // Test getPath after reading object value
    @Test
    public void testGetPath_insideObject_returnsPath() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\"a\":1}"));
        reader.beginObject();
        assertEquals("$.a", reader.getPath());
        reader.nextName();
        reader.nextInt();
        reader.endObject();
        reader.close();
    }

    // Test reading negative integer as long
    @Test
    public void testNextLong_negativeInteger_returnsLong() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("-100"));
        assertEquals(-100L, reader.nextLong());
        reader.close();
    }

    // Test IllegalStateException when reading string from null
    @Test(expected = IllegalStateException.class)
    public void testNextString_nullValue_throwsException() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("null"));
        reader.nextString();
        reader.close();
    }

    // Test NumberFormatException for int overflow
    @Test(expected = NumberFormatException.class)
    public void testNextInt_overflow_throwsException() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("2147483648"));
        reader.nextInt();
        reader.close();
    }

    // ========== New test cases for uncovered coverage ==========

    // Test reading an empty string
    @Test
    public void testNextString_emptyString_returnsEmpty() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"\""));
        assertEquals("", reader.nextString());
        reader.close();
    }

    // Test reading the maximum long value
    @Test
    public void testNextLong_maxLong_returnsLong() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("9223372036854775807"));
        assertEquals(Long.MAX_VALUE, reader.nextLong());
        reader.close();
    }

    // Test reading double with scientific notation
    @Test
    public void testNextDouble_scientificNotation_returnsDouble() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("1.0e10"));
        assertEquals(1.0e10, reader.nextDouble(), 0.0);
        reader.close();
    }

    // Test nextBoolean on a string "true" in lenient mode
    @Test
    public void testNextBoolean_stringTrueInLenient_returnsTrue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"true\""));
        reader.setLenient(true);
        assertTrue(reader.nextBoolean());
        reader.close();
    }

    // Test skipValue on a simple array
    @Test
    public void testSkipValue_array_skipsCorrectly() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1, 2, 3]"));
        reader.beginArray();
        reader.skipValue();
        assertEquals(JsonToken.NUMBER, reader.peek());
        reader.nextInt();
        reader.skipValue();
        assertEquals(JsonToken.END_ARRAY, reader.peek());
        reader.endArray();
        reader.close();
    }

    // Test hasNext on an empty object
    @Test
    public void testHasNext_emptyObject_returnsFalse() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{}"));
        reader.beginObject();
        assertFalse(reader.hasNext());
        reader.endObject();
        reader.close();
    }

    // Test hasNext on an empty array
    @Test
    public void testHasNext_emptyArray_returnsFalse() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[]"));
        reader.beginArray();
        assertFalse(reader.hasNext());
        reader.endArray();
        reader.close();
    }

    // Test getPath inside an array
    @Test
    public void testGetPath_insideArray_returnsPath() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[10, 20]"));
        reader.beginArray();
        reader.nextInt();
        assertEquals("$[0]", reader.getPath());
        reader.nextInt();
        reader.endArray();
        reader.close();
    }

    // Test that closing the reader multiple times does not throw
    @Test
    public void testClose_twice_doesNotThrow() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("42"));
        reader.close();
        reader.close(); // should be safe
    }

    // Test reading a lenient unquoted string (non-numeric)
    @Test
    public void testNextString_lenientUnquoted_returnsString() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("abc"));
        reader.setLenient(true);
        assertEquals("abc", reader.nextString());
        reader.close();
    }
}