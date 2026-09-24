package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TypeAdaptersTest {

    private enum TestEnum {
        @SerializedName("first_val")
        FIRST,
        @SerializedName(value = "second_val", alternate = {"alt_second"})
        SECOND,
        THIRD
    }

    private <T> String toJson(TypeAdapter<T> adapter, T value) throws IOException {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        jsonWriter.setLenient(true);
        adapter.write(jsonWriter, value);
        return writer.toString();
    }

    private <T> T fromJson(TypeAdapter<T> adapter, String json) throws IOException {
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        return adapter.read(reader);
    }

    // Tests class serialization exception
    @Test(expected = UnsupportedOperationException.class)
    public void testClass_serializeNonNull_throwsException() throws IOException {
        toJson(TypeAdapters.CLASS, String.class);
    }

    // Tests class serialization and deserialization with null
    @Test
    public void testClass_nullInput_returnsNull() throws IOException {
        assertEquals("null", toJson(TypeAdapters.CLASS, null));
        assertNull(fromJson(TypeAdapters.CLASS, "null"));
    }

    // Tests bitset serialization and deserialization across types
    @Test
    public void testBitSet_validInputs_roundTripsCorrectly() throws IOException {
        BitSet bitSet = new BitSet();
        bitSet.set(0);
        bitSet.set(2);
        bitSet.set(3);
        String json = toJson(TypeAdapters.BIT_SET, bitSet);
        assertEquals("[1,0,1,1]", json);

        BitSet readFromNumeric = fromJson(TypeAdapters.BIT_SET, "[1,0,1,1]");
        assertEquals(bitSet, readFromNumeric);

        BitSet readFromBoolean = fromJson(TypeAdapters.BIT_SET, "[true,false,true,true]");
        assertEquals(bitSet, readFromBoolean);

        BitSet readFromString = fromJson(TypeAdapters.BIT_SET, "[\"1\",\"0\",\"1\",\"1\"]");
        assertEquals(bitSet, readFromString);

        assertNull(fromJson(TypeAdapters.BIT_SET, "null"));
        assertEquals("null", toJson(TypeAdapters.BIT_SET, null));
    }

    // Tests bitset deserialization with invalid string
    @Test(expected = JsonSyntaxException.class)
    public void testBitSet_invalidStringElement_throwsException() throws IOException {
        fromJson(TypeAdapters.BIT_SET, "[\"invalid\"]");
    }

    // Tests boolean and boolean as string adapters
    @Test
    public void testBoolean_validAndNullInputs_correctResult() throws IOException {
        assertEquals("true", toJson(TypeAdapters.BOOLEAN, true));
        assertEquals("false", toJson(TypeAdapters.BOOLEAN, false));
        assertEquals("null", toJson(TypeAdapters.BOOLEAN, null));
        assertTrue(fromJson(TypeAdapters.BOOLEAN, "true"));
        assertFalse(fromJson(TypeAdapters.BOOLEAN, "false"));
        assertTrue(fromJson(TypeAdapters.BOOLEAN, "\"true\""));
        assertNull(fromJson(TypeAdapters.BOOLEAN, "null"));

        assertEquals("\"true\"", toJson(TypeAdapters.BOOLEAN_AS_STRING, true));
        assertEquals("\"null\"", toJson(TypeAdapters.BOOLEAN_AS_STRING, null));
        assertTrue(fromJson(TypeAdapters.BOOLEAN_AS_STRING, "\"true\""));
        assertNull(fromJson(TypeAdapters.BOOLEAN_AS_STRING, "null"));
    }

    // Tests numeric primitive adapters (Byte, Short, Integer, Long, Float, Double, Number)
    @Test
    public void testNumberAdapters_validAndNull_roundTripsCorrectly() throws IOException {
        assertEquals((byte) 12, fromJson(TypeAdapters.BYTE, "12").byteValue());
        assertNull(fromJson(TypeAdapters.BYTE, "null"));

        assertEquals((short) 1234, fromJson(TypeAdapters.SHORT, "1234").shortValue());
        assertNull(fromJson(TypeAdapters.SHORT, "null"));

        assertEquals(123456, fromJson(TypeAdapters.INTEGER, "123456").intValue());
        assertNull(fromJson(TypeAdapters.INTEGER, "null"));

        assertEquals(12345678901L, fromJson(TypeAdapters.LONG, "12345678901").longValue());
        assertNull(fromJson(TypeAdapters.LONG, "null"));

        assertEquals(12.34f, fromJson(TypeAdapters.FLOAT, "12.34").floatValue(), 0.001f);
        assertNull(fromJson(TypeAdapters.FLOAT, "null"));

        assertEquals(123.456, fromJson(TypeAdapters.DOUBLE, "123.456").doubleValue(), 0.001);
        assertNull(fromJson(TypeAdapters.DOUBLE, "null"));

        Number lazilyParsed = fromJson(TypeAdapters.NUMBER, "98765432109876543210");
        assertEquals("98765432109876543210", lazilyParsed.toString());
        assertNull(fromJson(TypeAdapters.NUMBER, "null"));
    }

    // Tests atomic types
    @Test
    public void testAtomicTypes_validInputs_correctResult() throws IOException {
        AtomicInteger ai = fromJson(TypeAdapters.ATOMIC_INTEGER, "42");
        assertEquals(42, ai.get());
        assertEquals("42", toJson(TypeAdapters.ATOMIC_INTEGER, new AtomicInteger(42)));

        AtomicBoolean ab = fromJson(TypeAdapters.ATOMIC_BOOLEAN, "true");
        assertTrue(ab.get());
        assertEquals("true", toJson(TypeAdapters.ATOMIC_BOOLEAN, new AtomicBoolean(true)));

        AtomicIntegerArray aia = fromJson(TypeAdapters.ATOMIC_INTEGER_ARRAY, "[1,2,3]");
        assertEquals(3, aia.length());
        assertEquals(2, aia.get(1));
        assertEquals("[1,2,3]", toJson(TypeAdapters.ATOMIC_INTEGER_ARRAY, aia));
    }

    // Tests Character adapter
    @Test
    public void testCharacter_validAndInvalidInputs_correctResult() throws IOException {
        assertEquals(Character.valueOf('a'), fromJson(TypeAdapters.CHARACTER, "\"a\""));
        assertEquals("\"a\"", toJson(TypeAdapters.CHARACTER, 'a'));
        assertEquals("null", toJson(TypeAdapters.CHARACTER, null));
        assertNull(fromJson(TypeAdapters.CHARACTER, "null"));
    }

    // Tests Character exception when string length != 1
    @Test(expected = JsonSyntaxException.class)
    public void testCharacter_multiCharacterString_throwsException() throws IOException {
        fromJson(TypeAdapters.CHARACTER, "\"abc\"");
    }

    // Tests String, StringBuilder, StringBuffer, BigDecimal, BigInteger
    @Test
    public void testStringAndBigMathAdapters_validInputs_roundTripsCorrectly() throws IOException {
        assertEquals("test", fromJson(TypeAdapters.STRING, "\"test\""));
        assertEquals("true", fromJson(TypeAdapters.STRING, "true"));
        assertNull(fromJson(TypeAdapters.STRING, "null"));

        assertEquals("hello", fromJson(TypeAdapters.STRING_BUILDER, "\"hello\"").toString());
        assertEquals("\"hello\"", toJson(TypeAdapters.STRING_BUILDER, new StringBuilder("hello")));

        assertEquals("world", fromJson(TypeAdapters.STRING_BUFFER, "\"world\"").toString());
        assertEquals("\"world\"", toJson(TypeAdapters.STRING_BUFFER, new StringBuffer("world")));

        assertEquals(new BigDecimal("123.456789"), fromJson(TypeAdapters.BIG_DECIMAL, "\"123.456789\""));
        assertEquals(new BigInteger("12345678901234567890"), fromJson(TypeAdapters.BIG_INTEGER, "\"12345678901234567890\""));
    }

    // Tests URL, URI, InetAddress, UUID, Currency
    @Test
    public void testNetworkingAndMiscAdapters_validInputs_roundTripsCorrectly() throws Exception {
        URL url = new URL("http://google.com");
        assertEquals(url, fromJson(TypeAdapters.URL, "\"http://google.com\""));
        assertEquals("\"http://google.com\"", toJson(TypeAdapters.URL, url));
        assertNull(fromJson(TypeAdapters.URL, "\"null\""));

        URI uri = new URI("http://google.com");
        assertEquals(uri, fromJson(TypeAdapters.URI, "\"http://google.com\""));
        assertEquals("\"http://google.com\"", toJson(TypeAdapters.URI, uri));
        assertNull(fromJson(TypeAdapters.URI, "\"null\""));

        InetAddress inet = InetAddress.getByName("127.0.0.1");
        assertEquals(inet, fromJson(TypeAdapters.INET_ADDRESS, "\"127.0.0.1\""));
        assertEquals("\"127.0.0.1\"", toJson(TypeAdapters.INET_ADDRESS, inet));

        UUID uuid = UUID.randomUUID();
        assertEquals(uuid, fromJson(TypeAdapters.UUID, "\"" + uuid.toString() + "\""));
        assertEquals("\"" + uuid.toString() + "\"", toJson(TypeAdapters.UUID, uuid));

        Currency currency = Currency.getInstance("USD");
        assertEquals(currency, fromJson(TypeAdapters.CURRENCY, "\"USD\""));
        assertEquals("\"USD\"", toJson(TypeAdapters.CURRENCY, currency));
    }

    // Tests Locale adapter with various string formats
    @Test
    public void testLocale_variousFormats_parsedCorrectly() throws IOException {
        assertEquals(new Locale("en"), fromJson(TypeAdapters.LOCALE, "\"en\""));
        assertEquals(new Locale("en", "US"), fromJson(TypeAdapters.LOCALE, "\"en_US\""));
        assertEquals(new Locale("en", "US", "WIN"), fromJson(TypeAdapters.LOCALE, "\"en_US_WIN\""));
        assertEquals("\"en_US\"", toJson(TypeAdapters.LOCALE, new Locale("en", "US")));
        assertNull(fromJson(TypeAdapters.LOCALE, "null"));
        assertEquals("null", toJson(TypeAdapters.LOCALE, null));
    }

    // Tests Calendar adapter serialization and deserialization
    @Test
    public void testCalendar_validInput_roundTripsCorrectly() throws IOException {
        Calendar cal = new GregorianCalendar(2023, Calendar.MARCH, 15, 10, 20, 30);
        String json = toJson(TypeAdapters.CALENDAR, cal);
        Calendar deserialized = fromJson(TypeAdapters.CALENDAR, json);

        assertEquals(2023, deserialized.get(Calendar.YEAR));
        assertEquals(Calendar.MARCH, deserialized.get(Calendar.MONTH));
        assertEquals(15, deserialized.get(Calendar.DAY_OF_MONTH));
        assertEquals(10, deserialized.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, deserialized.get(Calendar.MINUTE));
        assertEquals(30, deserialized.get(Calendar.SECOND));

        assertNull(fromJson(TypeAdapters.CALENDAR, "null"));
        assertEquals("null", toJson(TypeAdapters.CALENDAR, null));
    }

    // Tests JsonElement serialization and deserialization
    @Test
    public void testJsonElement_allVariants_roundTripsCorrectly() throws IOException {
        JsonObject obj = new JsonObject();
        obj.addProperty("key1", "val1");
        obj.addProperty("key2", 42);
        obj.addProperty("key3", true);
        obj.add("key4", JsonNull.INSTANCE);
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(1));
        array.add(new JsonPrimitive("item"));
        obj.add("array", array);

        String json = toJson(TypeAdapters.JSON_ELEMENT, obj);
        JsonElement parsed = fromJson(TypeAdapters.JSON_ELEMENT, json);

        assertEquals(obj, parsed);
        assertEquals("null", toJson(TypeAdapters.JSON_ELEMENT, JsonNull.INSTANCE));
        assertEquals(JsonNull.INSTANCE, fromJson(TypeAdapters.JSON_ELEMENT, "null"));
    }

    // Tests Enum adapter with @SerializedName and alternate names
    @Test
    public void testEnumAdapter_withSerializedNameAndAlternate_matchesCorrectConstants() throws IOException {
        Gson gson = new Gson();
        TypeAdapter<TestEnum> adapter = TypeAdapters.ENUM_FACTORY.create(gson, TypeToken.get(TestEnum.class));
        assertNotNull(adapter);

        assertEquals("\"first_val\"", toJson(adapter, TestEnum.FIRST));
        assertEquals("\"second_val\"", toJson(adapter, TestEnum.SECOND));
        assertEquals("\"THIRD\"", toJson(adapter, TestEnum.THIRD));

        assertEquals(TestEnum.FIRST, fromJson(adapter, "\"first_val\""));
        assertEquals(TestEnum.SECOND, fromJson(adapter, "\"second_val\""));
        assertEquals(TestEnum.SECOND, fromJson(adapter, "\"alt_second\""));
        assertEquals(TestEnum.THIRD, fromJson(adapter, "\"THIRD\""));
        assertNull(fromJson(adapter, "null"));
    }

    // Tests factory helper methods creation and toString representations
    @Test
    public void testFactoryCreationMethods_validTypes_returnAppropriateAdapters() {
        Gson gson = new Gson();

        TypeAdapterFactory factory1 = TypeAdapters.newFactory(String.class, TypeAdapters.STRING);
        assertNotNull(factory1.create(gson, TypeToken.get(String.class)));
        assertNull(factory1.create(gson, TypeToken.get(Integer.class)));
        assertTrue(factory1.toString().contains("Factory[type="));

        TypeAdapterFactory factory2 = TypeAdapters.newFactory(int.class, Integer.class, TypeAdapters.INTEGER);
        assertNotNull(factory2.create(gson, TypeToken.get(int.class)));
        assertNotNull(factory2.create(gson, TypeToken.get(Integer.class)));
        assertNull(factory2.create(gson, TypeToken.get(Long.class)));
        assertTrue(factory2.toString().contains("Factory[type="));

        TypeAdapterFactory factory3 = TypeAdapters.newFactoryForMultipleTypes(Calendar.class, GregorianCalendar.class, TypeAdapters.CALENDAR);
        assertNotNull(factory3.create(gson, TypeToken.get(Calendar.class)));
        assertNotNull(factory3.create(gson, TypeToken.get(GregorianCalendar.class)));
        assertNull(factory3.create(gson, TypeToken.get(String.class)));
        assertTrue(factory3.toString().contains("Factory[type="));

        TypeAdapterFactory hierarchyFactory = TypeAdapters.newTypeHierarchyFactory(Number.class, TypeAdapters.NUMBER);
        assertNotNull(hierarchyFactory.create(gson, TypeToken.get(Number.class)));
        assertNotNull(hierarchyFactory.create(gson, TypeToken.get(Integer.class)));
        assertNull(hierarchyFactory.create(gson, TypeToken.get(String.class)));
        assertTrue(hierarchyFactory.toString().contains("Factory[typeHierarchy="));
    }
}