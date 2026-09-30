package com.google.gson.stream;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

public class JsonReaderTest {

    private static final String FAIL_ON_INTEGER_OVERFLOW = "{\"a\":12345678901234567890}";

    // Tests basic reading of a simple object with string and integer
    @Test
    public void testNextString_simpleObject_returnsCorrectValues() throws IOException {
        String json = "{\"name\":\"test\",\"value\":123}";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.beginObject();
        assertEquals("name", reader.nextName());
        assertEquals("test", reader.nextString());
        assertEquals("value", reader.nextName());
        assertEquals(123, reader.nextInt());
        reader.endObject();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        reader.close();
    }

    // Tests reading nested objects and arrays
    @Test
    public void testBeginArray_nestedArray_parsesCorrectly() throws IOException {
        String json = "{\"a\":[1,2,3]}";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.beginObject();
        assertEquals("a", reader.nextName());
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        assertEquals(2, reader.nextInt());
        assertEquals(3, reader.nextInt());
        reader.endArray();
        reader.endObject();
        reader.close();
    }

    // Tests lenient mode with single-quoted strings
    @Test
    public void testNextString_singleQuotedLenient_returnsString() throws IOException {
        String json = "{'name':'test'}";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        reader.beginObject();
        assertEquals("name", reader.nextName());
        assertEquals("test", reader.nextString());
        reader.endObject();
        reader.close();
    }

    // Tests lenient mode with unquoted strings
    @Test
    public void testNextString_unquotedLenient_returnsString() throws IOException {
        String json = "{name:test}";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        reader.beginObject();
        assertEquals("name", reader.nextName());
        assertEquals("test", reader.nextString());
        reader.endObject();
        reader.close();
    }

    // Tests the null literal
    @Test
    public void testNextNull_nullValue_consumesNull() throws IOException {
        String json = "null";
        JsonReader reader = new JsonReader(new StringReader(json));
        assertEquals(JsonToken.NULL, reader.peek());
        reader.nextNull();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        reader.close();
    }

    // Tests nextBoolean with true
    @Test
    public void testNextBoolean_trueValue_returnsTrue() throws IOException {
        String json = "true";
        JsonReader reader = new JsonReader(new StringReader(json));
        assertTrue(reader.nextBoolean());
        reader.close();
    }

    // Tests nextBoolean with false
    @Test
    public void testNextBoolean_falseValue_returnsFalse() throws IOException {
        String json = "false";
        JsonReader reader = new JsonReader(new StringReader(json));
        assertFalse(reader.nextBoolean());
        reader.close();
    }

    // Tests nextLong with long value
    @Test
    public void testNextLong_validLong_returnsLong() throws IOException {
        String json = "123456789012345";
        JsonReader reader = new JsonReader(new StringReader(json));
        assertEquals(123456789012345L, reader.nextLong());
        reader.close();
    }

    // Tests nextDouble with decimal value
    @Test
    public void testNextDouble_decimalValue_returnsDouble() throws IOException {
        String json = "3.14";
        JsonReader reader = new JsonReader(new StringReader(json));
        assertEquals(3.14, reader.nextDouble(), 0.0001);
        reader.close();
    }

    // Tests hasNext at the end of an array
    @Test
    public void testHasNext_emptyArray_returnsFalse() throws IOException {
        String json = "[]";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.beginArray();
        assertFalse(reader.hasNext());
        reader.endArray();
        reader.close();
    }

    // Tests skipValue on a nested object
    @Test
    public void testSkipValue_nestedObject_skipsCorrectly() throws IOException {
        String json = "{\"a\":{\"b\":1},\"c\":2}";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.beginObject();
        assertEquals("a", reader.nextName());
        reader.skipValue();
        assertEquals("c", reader.nextName());
        assertEquals(2, reader.nextInt());
        reader.endObject();
        reader.close();
    }

    // Tests lenient mode with top-level primitive
    @Test
    public void testPeek_topLevelPrimitiveLenient_returnsPrimitive() throws IOException {
        String json = "42";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        assertEquals(JsonToken.NUMBER, reader.peek());
        assertEquals(42, reader.nextInt());
        reader.close();
    }

    // Tests exception when calling nextName on non-name token
    @Test(expected = IllegalStateException.class)
    public void testNextName_nonNameToken_throwsException() throws IOException {
        String json = "123";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        reader.nextName();
    }

    // Tests exception when parsing an invalid number (leading zero)
    @Test(expected = MalformedJsonException.class)
    public void testPeekNumber_leadingZero_throwsException() throws IOException {
        String json = "0123";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        reader.peek();
        reader.nextInt();
    }

    // Tests lenient mode with multiple top-level values
    @Test
    public void testNextInt_multipleTopLevelValuesLenient_returnsFirst() throws IOException {
        String json = "1 2";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        assertEquals(1, reader.nextInt());
        assertEquals(2, reader.nextInt());
        reader.close();
    }

    // Tests nextString for quoted string with escape sequence
    @Test
    public void testNextString_escapedQuotes_containsQuote() throws IOException {
        String json = "\"hello\\\"world\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        assertEquals("hello\"world", reader.nextString());
        reader.close();
    }

    // Tests reading a long string (requires buffer to fill multiple times)
    @Test
    public void testNextString_longString_returnsCorrectString() throws IOException {
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < 2000; i++) {
            sb.append("a");
        }
        sb.append("\"");
        JsonReader reader = new JsonReader(new StringReader(sb.toString()));
        String result = reader.nextString();
        assertEquals(2000, result.length());
        for (int i = 0; i < 2000; i++) {
            assertEquals('a', result.charAt(i));
        }
        reader.close();
    }

    // Tests exception when integer overflow occurs in nextInt
    @Test(expected = NumberFormatException.class)
    public void testNextInt_integerOverflow_throwsException() throws IOException {
        JsonReader reader = new JsonReader(new StringReader(FAIL_ON_INTEGER_OVERFLOW));
        reader.beginObject();
        reader.nextName();
        reader.nextInt();
    }

    // Tests close method transitions to closed state
    @Test(expected = IllegalStateException.class)
    public void testClose_closedReader_throwsException() throws IOException {
        String json = "{}";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.close();
        reader.beginObject();
    }

    // Tests getPath() after reading nested objects
    @Test
    public void testGetPath_nestedObject_returnsCorrectPath() throws IOException {
        String json = "{\"a\":{\"b\":1}}";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.beginObject();
        assertEquals("$.", reader.getPath());
        reader.nextName();
        assertEquals("$.a", reader.getPath());
        reader.beginObject();
        assertEquals("$.a.", reader.getPath());
        reader.nextName();
        assertEquals("$.a.b", reader.getPath());
        reader.nextInt();
        reader.endObject();
        reader.endObject();
        reader.close();
    }

    // ======================= NEW TEST CASES =======================

    // Tests skipping an array value
    @Test
    public void testSkipValue_array_skipsCorrectly() throws IOException {
        String json = "{\"a\":[1,2,3],\"b\":4}";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.beginObject();
        assertEquals("a", reader.nextName());
        reader.skipValue();
        assertEquals("b", reader.nextName());
        assertEquals(4, reader.nextInt());
        reader.endObject();
        reader.close();
    }

    // Tests skipping a scalar value
    @Test
    public void testSkipValue_scalarValue_skipsCorrectly() throws IOException {
        String json = "{\"a\":123,\"b\":456}";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.beginObject();
        assertEquals("a", reader.nextName());
        reader.skipValue();
        assertEquals("b", reader.nextName());
        assertEquals(456, reader.nextInt());
        reader.endObject();
        reader.close();
    }

    // Tests empty array peek returns END_ARRAY
    @Test
    public void testPeek_emptyArray_returnsEndArray() throws IOException {
        String json = "[]";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.beginArray();
        assertEquals(JsonToken.END_ARRAY, reader.peek());
        reader.endArray();
        reader.close();
    }

    // Tests nested arrays inside objects
    @Test
    public void testBeginArray_nestedArrayInsideObject_parsesCorrectly() throws IOException {
        String json = "{\"a\":[{\"b\":1},{\"c\":2}]}";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.beginObject();
        assertEquals("a", reader.nextName());
        reader.beginArray();
        reader.beginObject();
        assertEquals("b", reader.nextName());
        assertEquals(1, reader.nextInt());
        reader.endObject();
        reader.beginObject();
        assertEquals("c", reader.nextName());
        assertEquals(2, reader.nextInt());
        reader.endObject();
        reader.endArray();
        reader.endObject();
        reader.close();
    }

    // Tests peek after reading a value
    @Test
    public void testPeek_afterReadingValue_returnsEndDocument() throws IOException {
        String json = "true";
        JsonReader reader = new JsonReader(new StringReader(json));
        assertTrue(reader.nextBoolean());
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        reader.close();
    }

    // Tests nextInt with negative number
    @Test
    public void testNextInt_negativeNumber_returnsNegativeInt() throws IOException {
        String json = "-42";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        assertEquals(-42, reader.nextInt());
        reader.close();
    }

    // Tests nextDouble with negative decimal
    @Test
    public void testNextDouble_negativeDecimal_returnsNegativeDouble() throws IOException {
        String json = "-3.14";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        assertEquals(-3.14, reader.nextDouble(), 0.0001);
        reader.close();
    }

    // Tests lenient mode with unquoted name
    @Test
    public void testNextName_unquotedLenient_returnsName() throws IOException {
        String json = "{abc:123}";
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        reader.beginObject();
        assertEquals("abc", reader.nextName());
        assertEquals(123, reader.nextInt());
        reader.endObject();
        reader.close();
    }

    // Tests nextString with unicode escapes
    @Test
    public void testNextString_unicodeEscape_returnsUnicodeChar() throws IOException {
        String json = "\"hello\\u0041\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        assertEquals("helloA", reader.nextString());
        reader.close();
    }

    // Tests peek on top-level string
    @Test
    public void testPeek_topLevelString_returnsString() throws IOException {
        String json = "\"test\"";
        JsonReader reader = new JsonReader(new StringReader(json));
        assertEquals(JsonToken.STRING, reader.peek());
        assertEquals("test", reader.nextString());
        reader.close();
    }
}