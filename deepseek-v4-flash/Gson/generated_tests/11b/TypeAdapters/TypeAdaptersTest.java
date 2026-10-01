package com.google.gson.internal.bind;

import static org.junit.Assert.*;
import org.junit.Test;
import com.google.gson.*;
import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.*;

public class TypeAdaptersTest {

    private enum Color { RED, GREEN, BLUE }

    @Test
    public void testBoolean_readNull_returnsNull() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.BOOLEAN.read(reader));
    }

    @Test
    public void testBoolean_readStringTrue_returnsTrue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"true\""));
        assertTrue(TypeAdapters.BOOLEAN.read(reader));
    }

    @Test
    public void testByte_readValid_returnsByte() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("65"));
        assertEquals((byte)65, TypeAdapters.BYTE.read(reader));
    }

    @Test
    public void testInteger_readNull_returnsNull() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.INTEGER.read(reader));
    }

    @Test(expected = JsonSyntaxException.class)
    public void testInteger_readNonNumber_throwsJsonSyntaxException() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"abc\""));
        TypeAdapters.INTEGER.read(reader);
    }

    @Test
    public void testLong_readNull_returnsNull() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.LONG.read(reader));
    }

    @Test
    public void testLong_readMaxLong_returnsMaxLong() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("9223372036854775807"));
        assertEquals(9223372036854775807L, TypeAdapters.LONG.read(reader));
    }

    @Test
    public void testNumber_readNull_returnsNull() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.NUMBER.read(reader));
    }

    @Test
    public void testNumber_readAndWrite_roundTrip() throws IOException {
        String input = "3.14";
        JsonReader reader = new JsonReader(new StringReader(input));
        Number num = TypeAdapters.NUMBER.read(reader);
        assertTrue(num instanceof LazilyParsedNumber);
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        TypeAdapters.NUMBER.write(writer, num);
        writer.flush();
        assertEquals(input, sw.toString());
    }

    @Test
    public void testBigDecimal_readNull_returnsNull() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.BIG_DECIMAL.read(reader));
    }

    @Test
    public void testBigDecimal_readValid_returnsBigDecimal() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"3.14\""));
        assertEquals(new BigDecimal("3.14"), TypeAdapters.BIG_DECIMAL.read(reader));
    }

    @Test
    public void testString_readNull_returnsNull() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.STRING.read(reader));
    }

    @Test
    public void testString_readBoolean_returnsString() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("true"));
        assertEquals("true", TypeAdapters.STRING.read(reader));
    }

    @Test
    public void testCharacter_readNull_returnsNull() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.CHARACTER.read(reader));
    }

    @Test(expected = JsonSyntaxException.class)
    public void testCharacter_readInvalidLength_throwsJsonSyntaxException() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"ab\""));
        TypeAdapters.CHARACTER.read(reader);
    }

    @Test
    public void testBitSet_readArrayOfNumbers_returnsBitSet() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1,0,1]"));
        BitSet bitset = TypeAdapters.BIT_SET.read(reader);
        assertTrue(bitset.get(0));
        assertFalse(bitset.get(1));
        assertTrue(bitset.get(2));
    }

    @Test(expected = JsonSyntaxException.class)
    public void testBitSet_readInvalidString_throwsJsonSyntaxException() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[\"abc\"]"));
        TypeAdapters.BIT_SET.read(reader);
    }

    @Test
    public void testLocale_readAllParts_returnsLocale() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"en_US_WIN\""));
        Locale locale = TypeAdapters.LOCALE.read(reader);
        assertEquals("en", locale.getLanguage());
        assertEquals("US", locale.getCountry());
        assertEquals("WIN", locale.getVariant());
    }

    @Test
    public void testJsonElement_readObject_returnsJsonObject() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\"key\":\"value\"}"));
        JsonElement elem = TypeAdapters.JSON_ELEMENT.read(reader);
        assertTrue(elem.isJsonObject());
        JsonObject obj = elem.getAsJsonObject();
        assertEquals("value", obj.get("key").getAsString());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testEnumFactory_createsAdapter_serializesDeserializes() throws IOException {
        TypeAdapter<Color> adapter = (TypeAdapter<Color>) TypeAdapters.ENUM_FACTORY.create(
            new Gson(), TypeToken.get(Color.class));
        assertNotNull(adapter);
        JsonReader reader = new JsonReader(new StringReader("\"RED\""));
        assertEquals(Color.RED, adapter.read(reader));
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        adapter.write(writer, Color.GREEN);
        writer.flush();
        assertEquals("\"GREEN\"", sw.toString());
    }

    @Test
    public void testBitSet_writeNull_writesNull() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        TypeAdapters.BIT_SET.write(writer, null);
        writer.flush();
        assertEquals("null", sw.toString());
    }

    @Test
    public void testCalendar_readAndWrite_roundTrip() throws IOException {
        String json = "{\"year\":2020,\"month\":0,\"dayOfMonth\":15,\"hourOfDay\":10,\"minute\":30,\"second\":0}";
        JsonReader reader = new JsonReader(new StringReader(json));
        Calendar cal = TypeAdapters.CALENDAR.read(reader);
        assertNotNull(cal);
        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(0, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(10, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));

        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        TypeAdapters.CALENDAR.write(writer, cal);
        writer.flush();
        assertEquals(json, sw.toString());
    }

    // === New tests for uncovered parts ===

    // SHORT
    @Test
    public void testShort_readNull_returnsNull() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.SHORT.read(reader));
    }

    @Test
    public void testShort_readValid_returnsShort() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("123"));
        assertEquals((short)123, TypeAdapters.SHORT.read(reader).shortValue());
    }

    @Test
    public void testShort_writeRoundTrip() throws IOException {
        short value = 42;
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        TypeAdapters.SHORT.write(writer, value);
        writer.flush();
        JsonReader reader = new JsonReader(new StringReader(sw.toString()));
        assertEquals(value, TypeAdapters.SHORT.read(reader).shortValue());
    }

    // FLOAT
    @Test
    public void testFloat_readNull_returnsNull() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.FLOAT.read(reader));
    }

    @Test
    public void testFloat_readValid_returnsFloat() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("3.14"));
        assertEquals(3.14f, TypeAdapters.FLOAT.read(reader), 0.0f);
    }

    @Test
    public void testFloat_writeRoundTrip() throws IOException {
        float value = 2.718f;
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        TypeAdapters.FLOAT.write(writer, value);
        writer.flush();
        JsonReader reader = new JsonReader(new StringReader(sw.toString()));
        assertEquals(value, TypeAdapters.FLOAT.read(reader), 0.0f);
    }

    // DOUBLE
    @Test
    public void testDouble_readNull_returnsNull() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.DOUBLE.read(reader));
    }

    @Test
    public void testDouble_readValid_returnsDouble() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("3.14"));
        assertEquals(3.14, TypeAdapters.DOUBLE.read(reader), 0.0);
    }

    @Test
    public void testDouble_writeRoundTrip() throws IOException {
        double value = 6.283;
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        TypeAdapters.DOUBLE.write(writer, value);
        writer.flush();
        JsonReader reader = new JsonReader(new StringReader(sw.toString()));
        assertEquals(value, TypeAdapters.DOUBLE.read(reader), 0.0);
    }

    // BIG_INTEGER
    @Test
    public void testBigInteger_readNull_returnsNull() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.BIG_INTEGER.read(reader));
    }

    @Test
    public void testBigInteger_readValid_returnsBigInteger() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("12345678901234567890"));
        assertEquals(new BigInteger("12345678901234567890"), TypeAdapters.BIG_INTEGER.read(reader));
    }

    @Test
    public void testBigInteger_writeRoundTrip() throws IOException {
        BigInteger value = new BigInteger("9876543210");
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        TypeAdapters.BIG_INTEGER.write(writer, value);
        writer.flush();
        JsonReader reader = new JsonReader(new StringReader(sw.toString()));
        assertEquals(value, TypeAdapters.BIG_INTEGER.read(reader));
    }

    // URL
    @Test
    public void testURL_readNull_returnsNull() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.URL.read(reader));
    }

    @Test
    public void testURL_readValid_returnsURL() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"http://example.com\""));
        assertEquals(new URL("http://example.com"), TypeAdapters.URL.read(reader));
    }

    @Test
    public void testURL_writeRoundTrip() throws IOException {
        URL value = new URL("https://google.com");
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        TypeAdapters.URL.write(writer, value);
        writer.flush();
        JsonReader reader = new JsonReader(new StringReader(sw.toString()));
        assertEquals(value, TypeAdapters.URL.read(reader));
    }

    // URI
    @Test
    public void testURI_readNull_returnsNull() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.URI.read(reader));
    }

    @Test
    public void testURI_readValid_returnsURI() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"http://example.com/path\""));
        assertEquals(new URI("http://example.com/path"), TypeAdapters.URI.read(reader));
    }

    @Test
    public void testURI_writeRoundTrip() throws IOException {
        URI value = new URI("urn:isbn:0-486-27557-4");
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        TypeAdapters.URI.write(writer, value);
        writer.flush();
        JsonReader reader = new JsonReader(new StringReader(sw.toString()));
        assertEquals(value, TypeAdapters.URI.read(reader));
    }

    // UUID
    @Test
    public void testUUID_readNull_returnsNull() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.UUID.read(reader));
    }

    @Test
    public void testUUID_readValid_returnsUUID() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"550e8400-e29b-41d4-a716-446655440000\""));
        assertEquals(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), TypeAdapters.UUID.read(reader));
    }

    @Test
    public void testUUID_writeRoundTrip() throws IOException {
        UUID value = UUID.randomUUID();
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        TypeAdapters.UUID.write(writer, value);
        writer.flush();
        JsonReader reader = new JsonReader(new StringReader(sw.toString()));
        assertEquals(value, TypeAdapters.UUID.read(reader));
    }

    // TIME_ZONE
    @Test
    public void testTimeZone_readNull_returnsNull() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.TIME_ZONE.read(reader));
    }

    @Test
    public void testTimeZone_readValid_returnsTimeZone() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"America/New_York\""));
        assertEquals(TimeZone.getTimeZone("America/New_York"), TypeAdapters.TIME_ZONE.read(reader));
    }

    @Test
    public void testTimeZone_writeRoundTrip() throws IOException {
        TimeZone value = TimeZone.getTimeZone("Europe/London");
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        TypeAdapters.TIME_ZONE.write(writer, value);
        writer.flush();
        JsonReader reader = new JsonReader(new StringReader(sw.toString()));
        assertEquals(value, TypeAdapters.TIME_ZONE.read(reader));
    }

    // Write tests for existing adapters (to increase coverage)
    @Test
    public void testBoolean_writeTrue_writesTrue() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        TypeAdapters.BOOLEAN.write(writer, true);
        writer.flush();
        assertEquals("true", sw.toString());
    }

    @Test
    public void testByte_writeValue_writesNumber() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        TypeAdapters.BYTE.write(writer, (byte)12);
        writer.flush();
        assertEquals("12", sw.toString());
    }

    @Test
    public void testInteger_writeValue_writesNumber() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        TypeAdapters.INTEGER.write(writer, 123);
        writer.flush();
        assertEquals("123", sw.toString());
    }

    @Test
    public void testLong_writeValue_writesNumber() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        TypeAdapters.LONG.write(writer, 123456789L);
        writer.flush();
        assertEquals("123456789", sw.toString());
    }

    @Test
    public void testString_writeValue_writesString() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        TypeAdapters.STRING.write(writer, "hello");
        writer.flush();
        assertEquals("\"hello\"", sw.toString());
    }

    @Test
    public void testCharacter_writeValue_writesString() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        TypeAdapters.CHARACTER.write(writer, 'A');
        writer.flush();
        assertEquals("\"A\"", sw.toString());
    }

    @Test
    public void testBigDecimal_writeValue_writesNumber() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter writer = new JsonWriter(sw);
        TypeAdapters.BIG_DECIMAL.write(writer, new BigDecimal("3.14"));
        writer.flush();
        assertEquals("3.14", sw.toString());
    }
}