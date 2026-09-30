package com.google.gson.internal.bind;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.InetAddress;
import java.util.BitSet;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class TypeAdaptersTest {

    private JsonReader reader(String json) {
        return new JsonReader(new StringReader(json));
    }

    private String write(TypeAdapter<?> adapter, Object value) throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        adapter.write(jw, value);
        jw.close();
        return sw.toString();
    }

    private <T> T read(TypeAdapter<T> adapter, String json) throws IOException {
        JsonReader jr = reader(json);
        T result = adapter.read(jr);
        jr.close();
        return result;
    }

    // Tests null input for BOOLEAN
    @Test
    public void testBOOLEAN_nullValue_returnsNull() throws IOException {
        assertNull(TypeAdapters.BOOLEAN.read(reader("null")));
    }

    // Tests boolean value true
    @Test
    public void testBOOLEAN_true_returnsTrue() throws IOException {
        assertTrue(TypeAdapters.BOOLEAN.read(reader("true")));
    }

    // Tests string "true" which is coerced for backwards compatibility
    @Test
    public void testBOOLEAN_stringTrue_returnsTrue() throws IOException {
        assertTrue(TypeAdapters.BOOLEAN.read(reader("\"true\"")));
    }

    // Tests positive integer
    @Test
    public void testINTEGER_positive_returnsInt() throws IOException {
        assertEquals(42, (int) TypeAdapters.INTEGER.read(reader("42")));
    }

    // Tests null integer
    @Test
    public void testINTEGER_null_returnsNull() throws IOException {
        assertNull(TypeAdapters.INTEGER.read(reader("null")));
    }

    // Tests negative long
    @Test
    public void testLONG_negative_returnsLong() throws IOException {
        assertEquals(-123L, (long) TypeAdapters.LONG.read(reader("-123")));
    }

    // Tests fraction double
    @Test
    public void testDOUBLE_fraction_returnsDouble() throws IOException {
        assertEquals(3.14, TypeAdapters.DOUBLE.read(reader("3.14")), 1e-9);
    }

    // Tests null BigDecimal
    @Test
    public void testBIG_DECIMAL_null_returnsNull() throws IOException {
        assertNull(TypeAdapters.BIG_DECIMAL.read(reader("null")));
    }

    // Tests BigDecimal with scale, important for defect detection
    @Test
    public void testBIG_DECIMAL_zeroScale_returnsCorrectScale() throws IOException {
        BigDecimal result = TypeAdapters.BIG_DECIMAL.read(reader("0.00"));
        assertEquals(new BigDecimal("0.00"), result);
        assertEquals(2, result.scale());
        String jsonOut = write(TypeAdapters.BIG_DECIMAL, new BigDecimal("0.00"));
        assertEquals("0.00", jsonOut);
    }

    // Tests large BigInteger
    @Test
    public void testBIG_INTEGER_large_returnsBigInteger() throws IOException {
        BigInteger result = TypeAdapters.BIG_INTEGER.read(reader("12345678901234567890"));
        assertEquals(new BigInteger("12345678901234567890"), result);
    }

    // Tests null Number
    @Test
    public void testNUMBER_null_returnsNull() throws IOException {
        assertNull(TypeAdapters.NUMBER.read(reader("null")));
    }

    // Tests NUMBER with invalid token type (string) throws exception
    @Test(expected = JsonSyntaxException.class)
    public void testNUMBER_invalidType_throwsJsonSyntaxException() throws IOException {
        TypeAdapters.NUMBER.read(reader("\"not a number\""));
    }

    // Tests null String
    @Test
    public void testSTRING_null_returnsNull() throws IOException {
        assertNull(TypeAdapters.STRING.read(reader("null")));
    }

    // Tests boolean coercion to string
    @Test
    public void testSTRING_booleanCoercion_returnsString() throws IOException {
        assertEquals("false", TypeAdapters.STRING.read(reader("false")));
    }

    // Tests valid char
    @Test
    public void testCHARACTER_single_returnsChar() throws IOException {
        assertEquals('A', (char) TypeAdapters.CHARACTER.read(reader("\"A\"")));
    }

    // Tests empty string char throws exception
    @Test(expected = JsonSyntaxException.class)
    public void testCHARACTER_empty_throwsJsonSyntaxException() throws IOException {
        TypeAdapters.CHARACTER.read(reader("\"\""));
    }

    // Tests valid URI
    @Test
    public void testURI_valid_returnsURI() throws IOException {
        URI expected = URI.create("http://example.com");
        assertEquals(expected, TypeAdapters.URI.read(reader("\"http://example.com\"")));
    }

    // Tests invalid URI throws JsonIOException
    @Test(expected = com.google.gson.JsonIOException.class)
    public void testURI_invalidSyntax_throwsJsonIOException() throws IOException {
        TypeAdapters.URI.read(reader("\"not a valid uri\""));
    }

    // Tests valid UUID
    @Test
    public void testUUID_valid_returnsUUID() throws IOException {
        UUID expected = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        assertEquals(expected, TypeAdapters.UUID.read(reader("\"550e8400-e29b-41d4-a716-446655440000\"")));
    }

    // Tests Calendar with all fields
    @Test
    public void testCALENDAR_full_returnsCalendar() throws IOException {
        String json = "{\"year\":2024,\"month\":0,\"dayOfMonth\":1,\"hourOfDay\":10,\"minute\":30,\"second\":0}";
        Calendar cal = TypeAdapters.CALENDAR.read(reader(json));
        assertEquals(2024, cal.get(Calendar.YEAR));
        assertEquals(0, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(10, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
    }

    // Tests null Calendar
    @Test
    public void testCALENDAR_null_returnsNull() throws IOException {
        assertNull(TypeAdapters.CALENDAR.read(reader("null")));
    }

    // Tests Locale with language and country
    @Test
    public void testLOCALE_languageCountry_returnsLocale() throws IOException {
        Locale result = TypeAdapters.LOCALE.read(reader("\"en_US\""));
        assertEquals(Locale.US, result);
    }

    // Tests null Locale
    @Test
    public void testLOCALE_null_returnsNull() throws IOException {
        assertNull(TypeAdapters.LOCALE.read(reader("null")));
    }

    // Tests BitSet from array of numbers
    @Test
    public void testBIT_SET_numberArray_returnsBitSet() throws IOException {
        BitSet bs = TypeAdapters.BIT_SET.read(reader("[1,0,1]"));
        assertTrue(bs.get(0));
        assertFalse(bs.get(1));
        assertTrue(bs.get(2));
    }

    // Tests null BitSet
    @Test
    public void testBIT_SET_null_returnsNull() throws IOException {
        assertNull(TypeAdapters.BIT_SET.read(reader("null")));
    }

    // Tests BitSet with invalid token type throws exception
    @Test(expected = JsonSyntaxException.class)
    public void testBIT_SET_invalidType_throwsJsonSyntaxException() throws IOException {
        TypeAdapters.BIT_SET.read(reader("{}"));
    }

    // Tests Enum adapter via ENUM_FACTORY
    @SuppressWarnings("unchecked")
    @Test
    public void testENUM_FACTORY_valid_returnsEnum() throws IOException {
        TypeAdapter<TimeUnit> adapter =
            (TypeAdapter<TimeUnit>) TypeAdapters.ENUM_FACTORY.create(null, TypeToken.get(TimeUnit.class));
        TimeUnit result = adapter.read(reader("\"SECONDS\""));
        assertEquals(TimeUnit.SECONDS, result);
        String jsonOut = write(adapter, TimeUnit.SECONDS);
        assertEquals("\"SECONDS\"", jsonOut);
    }

    // Tests AtomicInteger read/write round trip
    @Test
    public void testATOMIC_INTEGER_readWrite_roundTrip() throws IOException {
        AtomicInteger result = TypeAdapters.ATOMIC_INTEGER.read(reader("42"));
        assertEquals(42, result.get());
        String jsonOut = write(TypeAdapters.ATOMIC_INTEGER, new AtomicInteger(42));
        assertEquals("42", jsonOut);
    }

    // Tests JsonElement object
    @Test
    public void testJSON_ELEMENT_object_returnsJsonObject() throws IOException {
        String json = "{\"name\":\"John\",\"age\":30}";
        JsonElement elem = TypeAdapters.JSON_ELEMENT.read(reader(json));
        assertTrue(elem.isJsonObject());
        JsonObject obj = elem.getAsJsonObject();
        assertEquals("John", obj.get("name").getAsString());
        assertEquals(30, obj.get("age").getAsInt());
    }

    // Tests JsonElement null
    @Test
    public void testJSON_ELEMENT_null_returnsJsonNull() throws IOException {
        JsonElement elem = TypeAdapters.JSON_ELEMENT.read(reader("null"));
        assertTrue(elem.isJsonNull());
        assertSame(JsonNull.INSTANCE, elem);
    }

    // ========== New tests for uncovered areas ==========

    // BYTE
    @Test
    public void testBYTE_positive_returnsByte() throws IOException {
        assertEquals(Byte.valueOf((byte) 100), TypeAdapters.BYTE.read(reader("100")));
    }

    @Test
    public void testBYTE_null_returnsNull() throws IOException {
        assertNull(TypeAdapters.BYTE.read(reader("null")));
    }

    // SHORT
    @Test
    public void testSHORT_positive_returnsShort() throws IOException {
        assertEquals(Short.valueOf((short) 200), TypeAdapters.SHORT.read(reader("200")));
    }

    @Test
    public void testSHORT_null_returnsNull() throws IOException {
        assertNull(TypeAdapters.SHORT.read(reader("null")));
    }

    // FLOAT
    @Test
    public void testFLOAT_fraction_returnsFloat() throws IOException {
        assertEquals(3.14f, TypeAdapters.FLOAT.read(reader("3.14")), 1e-9f);
    }

    @Test
    public void testFLOAT_null_returnsNull() throws IOException {
        assertNull(TypeAdapters.FLOAT.read(reader("null")));
    }

    // STRING_BUILDER
    @Test
    public void testSTRING_BUILDER_readWriteRoundTrip() throws IOException {
        StringBuilder sb = TypeAdapters.STRING_BUILDER.read(reader("\"hello\""));
        assertEquals("hello", sb.toString());
        String jsonOut = write(TypeAdapters.STRING_BUILDER, new StringBuilder("world"));
        assertEquals("\"world\"", jsonOut);
    }

    @Test
    public void testSTRING_BUILDER_null_returnsNull() throws IOException {
        assertNull(TypeAdapters.STRING_BUILDER.read(reader("null")));
    }

    // STRING_BUFFER
    @Test
    public void testSTRING_BUFFER_readWriteRoundTrip() throws IOException {
        StringBuffer sb = TypeAdapters.STRING_BUFFER.read(reader("\"test\""));
        assertEquals("test", sb.toString());
        String jsonOut = write(TypeAdapters.STRING_BUFFER, new StringBuffer("foo"));
        assertEquals("\"foo\"", jsonOut);
    }

    @Test
    public void testSTRING_BUFFER_null_returnsNull() throws IOException {
        assertNull(TypeAdapters.STRING_BUFFER.read(reader("null")));
    }

    // URL
    @Test
    public void testURL_valid_returnsURL() throws IOException {
        URL expected = new URL("http://example.com");
        assertEquals(expect, TypeAdapters.URL.read(reader("\"http://example.com\"")));
    }

    @Test(expected = com.google.gson.JsonIOException.class)
    public void testURL_invalid_throwsJsonIOException() throws IOException {
        TypeAdapters.URL.read(reader("\"not a valid url\""));
    }

    @Test
    public void testURL_null_returnsNull() throws IOException {
        assertNull(TypeAdapters.URL.read(reader("null")));
    }

    // INET_ADDRESS
    @Test
    public void testINET_ADDRESS_validIPv4() throws IOException {
        InetAddress addr = TypeAdapters.INET_ADDRESS.read(reader("\"192.168.1.1\""));
        assertEquals("192.168.1.1", addr.getHostAddress());
    }

    @Test(expected = com.google.gson.JsonIOException.class)
    public void testINET_ADDRESS_invalid_throwsJsonIOException() throws IOException {
        TypeAdapters.INET_ADDRESS.read(reader("\"invalid\""));
    }

    @Test
    public void testINET_ADDRESS_null_returnsNull() throws IOException {
        assertNull(TypeAdapters.INET_ADDRESS.read(reader("null")));
    }

    // CLASS
    @SuppressWarnings("unchecked")
    @Test
    public void testCLASS_valid_returnsClass() throws IOException {
        Class<String> clazz = (Class<String>) TypeAdapters.CLASS.read(reader("\"java.lang.String\""));
        assertEquals(String.class, clazz);
    }

    @Test(expected = com.google.gson.JsonIOException.class)
    public void testCLASS_invalidClassName_throwsJsonIOException() throws IOException {
        TypeAdapters.CLASS.read(reader("\"nonexistent.Class\""));
    }

    @Test
    public void testCLASS_null_returnsNull() throws IOException {
        assertNull(TypeAdapters.CLASS.read(reader("null")));
    }

    // ATOMIC_BOOLEAN
    @Test
    public void testATOMIC_BOOLEAN_true_roundTrip() throws IOException {
        AtomicBoolean result = TypeAdapters.ATOMIC_BOOLEAN.read(reader("true"));
        assertTrue(result.get());
        String jsonOut = write(TypeAdapters.ATOMIC_BOOLEAN, new AtomicBoolean(true));
        assertEquals("true", jsonOut);
    }

    @Test
    public void testATOMIC_BOOLEAN_false_roundTrip() throws IOException {
        AtomicBoolean result = TypeAdapters.ATOMIC_BOOLEAN.read(reader("false"));
        assertFalse(result.get());
        String jsonOut = write(TypeAdapters.ATOMIC_BOOLEAN, new AtomicBoolean(false));
        assertEquals("false", jsonOut);
    }

    // JSON_ELEMENT additional types
    @Test
    public void testJSON_ELEMENT_array_returnsJsonArray() throws IOException {
        JsonElement elem = TypeAdapters.JSON_ELEMENT.read(reader("[1, \"two\", true]"));
        assertTrue(elem.isJsonArray());
        JsonArray arr = elem.getAsJsonArray();
        assertEquals(3, arr.size());
        assertEquals(1, arr.get(0).getAsInt());
        assertEquals("two", arr.get(1).getAsString());
        assertTrue(arr.get(2).getAsBoolean());
    }

    @Test
    public void testJSON_ELEMENT_primitiveString() throws IOException {
        JsonElement elem = TypeAdapters.JSON_ELEMENT.read(reader("\"text\""));
        assertTrue(elem.isJsonPrimitive());
        assertEquals("text", elem.getAsString());
    }

    @Test
    public void testJSON_ELEMENT_primitiveNumber() throws IOException {
        JsonElement elem = TypeAdapters.JSON_ELEMENT.read(reader("42"));
        assertTrue(elem.isJsonPrimitive());
        assertEquals(42, elem.getAsInt());
    }

    @Test
    public void testJSON_ELEMENT_primitiveBoolean() throws IOException {
        JsonElement elem = TypeAdapters.JSON_ELEMENT.read(reader("true"));
        assertTrue(elem.isJsonPrimitive());
        assertTrue(elem.getAsBoolean());
    }
}