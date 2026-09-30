package com.google.gson.stream;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.StringReader;
import java.io.EOFException;

public class JsonReaderTest {

    private JsonReader newReader(String s) {
        return new JsonReader(new StringReader(s));
    }

    private JsonReader newReader(String s, boolean lenient) {
        JsonReader reader = new JsonReader(new StringReader(s));
        reader.setLenient(lenient);
        return reader;
    }

    // Tests NullPointerException when constructor receives null
    @Test(expected = NullPointerException.class)
    public void testConstructor_nullInput_throwsNullPointerException() {
        new JsonReader(null);
    }

    // Tests normal array reading
    @Test
    public void testBeginEndArray_normal_consumes() throws Exception {
        JsonReader reader = newReader("[1,2,3]");
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        assertEquals(2, reader.nextInt());
        assertEquals(3, reader.nextInt());
        reader.endArray();
    }

    // Tests normal object reading
    @Test
    public void testBeginEndObject_normal_consumes() throws Exception {
        JsonReader reader = newReader("{\"a\":\"b\"}");
        reader.beginObject();
        assertEquals("a", reader.nextName());
        assertEquals("b", reader.nextString());
        reader.endObject();
    }

    // Tests hasNext in array
    @Test
    public void testHasNext_inArray_returnsCorrect() throws Exception {
        JsonReader reader = newReader("[1]");
        reader.beginArray();
        assertTrue(reader.hasNext());
        reader.nextInt();
        assertFalse(reader.hasNext());
        reader.endArray();
    }

    // Tests nextString with quoted value
    @Test
    public void testNextString_quoted_returnsValue() throws Exception {
        JsonReader reader = newReader("\"hello\"");
        assertEquals("hello", reader.nextString());
    }

    // Tests nextString parses a number as string in lenient mode
    @Test
    public void testNextString_numberAsStringLenient_returnsString() throws Exception {
        JsonReader reader = newReader("[123]", true);
        reader.beginArray();
        assertEquals("123", reader.nextString());
        reader.endArray();
    }

    // Tests nextBoolean with true and false
    @Test
    public void testNextBoolean_trueAndFalse_returnsCorrect() throws Exception {
        JsonReader reader = newReader("[true,false]");
        reader.beginArray();
        assertTrue(reader.nextBoolean());
        assertFalse(reader.nextBoolean());
        reader.endArray();
    }

    // Tests nextNull consumes null literal
    @Test
    public void testNextNull_nullLiteral_consumes() throws Exception {
        JsonReader reader = newReader("null");
        reader.nextNull();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    // Tests nextInt normal value
    @Test
    public void testNextInt_positive_returnsInt() throws Exception {
        JsonReader reader = newReader("42");
        assertEquals(42, reader.nextInt());
    }

    // Tests nextInt with overflow throws NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testNextInt_overflow_throwsNumberFormatException() throws Exception {
        JsonReader reader = newReader("2147483648");
        reader.nextInt();
    }

    // Tests nextLong normal value
    @Test
    public void testNextLong_positive_returnsLong() throws Exception {
        JsonReader reader = newReader("123456789012345");
        assertEquals(123456789012345L, reader.nextLong());
    }

    // Tests nextLong with overflow throws NumberFormatException
    @Test(expected = NumberFormatException.class)
    public void testNextLong_overflow_throwsNumberFormatException() throws Exception {
        JsonReader reader = newReader("9223372036854775808");
        reader.nextLong();
    }

    // Tests nextDouble normal value
    @Test
    public void testNextDouble_normal_returnsDouble() throws Exception {
        JsonReader reader = newReader("3.14e0");
        assertEquals(3.14, reader.nextDouble(), 1e-9);
    }

    // Tests nextDouble with NaN (quoted) in non-lenient mode throws MalformedJsonException
    @Test(expected = MalformedJsonException.class)
    public void testNextDouble_quotedNanNonLenient_throws() throws Exception {
        JsonReader reader = newReader("\"NaN\"", false);
        reader.nextDouble();
    }

    // Tests nextDouble with NaN (quoted) in lenient mode returns NaN
    @Test
    public void testNextDouble_quotedNanLenient_returnsNan() throws Exception {
        JsonReader reader = newReader("\"NaN\"", true);
        assertTrue(Double.isNaN(reader.nextDouble()));
    }

    // Tests leading zero in strict mode throws MalformedJsonException
    @Test(expected = MalformedJsonException.class)
    public void testLeadingZeroStrict_throws() throws Exception {
        JsonReader reader = newReader("01");
        reader.nextInt();
    }

    // Tests skipValue on an object advances to the next element
    @Test
    public void testSkipValue_skipObject_advances() throws Exception {
        JsonReader reader = newReader("{\"a\":1,\"b\":2}");
        reader.beginObject();
        reader.skipValue();
        assertEquals("b", reader.nextName());
        assertEquals(2, reader.nextInt());
        reader.endObject();
    }

    // Tests peek returns correct tokens
    @Test
    public void testPeek_returnsCorrectTokens() throws Exception {
        JsonReader reader = newReader("{\"x\":null}");
        assertEquals(JsonToken.BEGIN_OBJECT, reader.peek());
        reader.beginObject();
        assertEquals(JsonToken.NAME, reader.peek());
        reader.nextName();
        assertEquals(JsonToken.NULL, reader.peek());
        reader.nextNull();
        assertEquals(JsonToken.END_OBJECT, reader.peek());
        reader.endObject();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    // Tests endArray on an object state throws IllegalStateException
    @Test(expected = IllegalStateException.class)
    public void testEndArray_noArray_throwsIllegalStateException() throws Exception {
        newReader("{}").endArray();
    }

    // Tests EOFException on empty input when token is expected
    @Test(expected = EOFException.class)
    public void testEOFException_onEmptyInput_throws() throws Exception {
        newReader("").beginArray();
    }

    // --- New test cases to increase coverage ---

    // Tests lenient mode accepts unquoted strings
    @Test
    public void testLenient_acceptsUnquotedString() throws Exception {
        JsonReader reader = newReader("[hello]", true);
        reader.beginArray();
        assertEquals("hello", reader.nextString());
        reader.endArray();
    }

    // Tests lenient mode accepts single-quoted strings
    @Test
    public void testLenient_acceptsSingleQuotedString() throws Exception {
        JsonReader reader = newReader("['hello']", true);
        reader.beginArray();
        assertEquals("hello", reader.nextString());
        reader.endArray();
    }

    // Tests lenient mode accepts comments (// and /* */)
    @Test
    public void testLenient_acceptsComment() throws Exception {
        JsonReader reader = newReader("{\"a\"/* comment */:1}", true);
        reader.beginObject();
        assertEquals("a", reader.nextName());
        assertEquals(1, reader.nextInt());
        reader.endObject();
    }

    // Tests strict mode rejects unquoted string
    @Test(expected = MalformedJsonException.class)
    public void testStrict_rejectsUnquotedString() throws Exception {
        JsonReader reader = newReader("[hello]", false);
        reader.beginArray();
        reader.nextString();
    }

    // Tests skipValue on an array advances to the next element
    @Test
    public void testSkipValue_skipArray_advances() throws Exception {
        JsonReader reader = newReader("{\"a\":[1,2,3],\"b\":4}");
        reader.beginObject();
        reader.skipValue(); // skip the array value
        assertEquals("b", reader.nextName());
        assertEquals(4, reader.nextInt());
        reader.endObject();
    }

    // Tests nextString with escape sequences (newline and quote)
    @Test
    public void testNextString_escapedCharacters_returnsDecoded() throws Exception {
        JsonReader reader = newReader("\"hello\\nworld\\\"\"");
        assertEquals("hello\nworld\"", reader.nextString());
    }

    // Tests nextString with Unicode escape
    @Test
    public void testNextString_unicodeEscape_returnsDecoded() throws Exception {
        JsonReader reader = newReader("\"\\u0041\"");
        assertEquals("A", reader.nextString());
    }
}