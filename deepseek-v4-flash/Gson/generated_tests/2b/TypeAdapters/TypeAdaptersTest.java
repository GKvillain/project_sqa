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
import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.BitSet;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.UUID;
import org.junit.Test;
import static org.junit.Assert.*;

public class TypeAdaptersTest {

    // Test Class adapter write with null value
    @Test
    public void testClassWrite_nullValue_writesNull() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        TypeAdapters.CLASS.write(jsonWriter, null);
        jsonWriter.flush();
        assertEquals("null", stringWriter.toString());
    }

    // Test Class adapter write with non-null value throws exception
    @Test(expected = UnsupportedOperationException.class)
    public void testClassWrite_nonNullValue_throwsException() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        TypeAdapters.CLASS.write(jsonWriter, String.class);
    }

    // Test Class adapter read with null token
    @Test
    public void testClassRead_nullToken_returnsNull() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.CLASS.read(jsonReader));
    }

    // Test Class adapter read with non-null token throws exception
    @Test(expected = UnsupportedOperationException.class)
    public void testClassRead_nonNullToken_throwsException() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("\"abc\""));
        TypeAdapters.CLASS.read(jsonReader);
    }

    // Test BitSet read with array of numbers
    @Test
    public void testBitSetRead_numberArray_returnsBitSet() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("[1,0,1,0,1]"));
        BitSet bitSet = TypeAdapters.BIT_SET.read(jsonReader);
        assertNotNull(bitSet);
        assertTrue(bitSet.get(0));
        assertFalse(bitSet.get(1));
        assertTrue(bitSet.get(2));
        assertFalse(bitSet.get(3));
        assertTrue(bitSet.get(4));
    }

    // Test BitSet read with array of booleans
    @Test
    public void testBitSetRead_booleanArray_returnsBitSet() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("[true,false,true]"));
        BitSet bitSet = TypeAdapters.BIT_SET.read(jsonReader);
        assertNotNull(bitSet);
        assertTrue(bitSet.get(0));
        assertFalse(bitSet.get(1));
        assertTrue(bitSet.get(2));
    }

    // Test BitSet read with invalid string value throws exception
    @Test(expected = JsonSyntaxException.class)
    public void testBitSetRead_invalidString_throwsException() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("[\"abc\"]"));
        TypeAdapters.BIT_SET.read(jsonReader);
    }

    // Test BitSet read with invalid token type throws exception
    @Test(expected = JsonSyntaxException.class)
    public void testBitSetRead_invalidTokenType_throwsException() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("[{}]"));
        TypeAdapters.BIT_SET.read(jsonReader);
    }

    // Test BitSet write with non-null value
    @Test
    public void testBitSetWrite_nonNullValue_writesArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        BitSet bitSet = new BitSet();
        bitSet.set(0);
        bitSet.set(2);
        TypeAdapters.BIT_SET.write(jsonWriter, bitSet);
        jsonWriter.flush();
        assertEquals("[1,0,1]", stringWriter.toString());
    }

    // Test Boolean read with string value
    @Test
    public void testBooleanRead_stringValue_returnsParsedBoolean() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("\"true\""));
        assertEquals(Boolean.TRUE, TypeAdapters.BOOLEAN.read(jsonReader));
    }

    // Test Byte read with valid number
    @Test
    public void testByteRead_validNumber_returnsByte() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("10"));
        assertEquals((byte) 10, TypeAdapters.BYTE.read(jsonReader).byteValue());
    }

    // Test Byte read with invalid number throws exception
    @Test(expected = JsonSyntaxException.class)
    public void testByteRead_invalidNumber_throwsException() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("1000"));
        TypeAdapters.BYTE.read(jsonReader);
    }

    // Test Short read with valid number
    @Test
    public void testShortRead_validNumber_returnsShort() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("100"));
        assertEquals((short) 100, TypeAdapters.SHORT.read(jsonReader).shortValue());
    }

    // Test Integer read with valid number
    @Test
    public void testIntegerRead_validNumber_returnsInteger() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("42"));
        assertEquals(42, TypeAdapters.INTEGER.read(jsonReader).intValue());
    }

    // Test Integer read with null token
    @Test
    public void testIntegerRead_nullToken_returnsNull() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.INTEGER.read(jsonReader));
    }

    // Test Long read with valid number
    @Test
    public void testLongRead_validNumber_returnsLong() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("123456789"));
        assertEquals(123456789L, TypeAdapters.LONG.read(jsonReader).longValue());
    }

    // Test Float read with valid number
    @Test
    public void testFloatRead_validNumber_returnsFloat() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("3.14"));
        assertEquals(3.14f, TypeAdapters.FLOAT.read(jsonReader).floatValue(), 0.001f);
    }

    // Test Double read with valid number
    @Test
    public void testDoubleRead_validNumber_returnsDouble() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("3.14159"));
        assertEquals(3.14159, TypeAdapters.DOUBLE.read(jsonReader).doubleValue(), 0.00001);
    }

    // Test Number read with null token
    @Test
    public void testNumberRead_nullToken_returnsNull() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.NUMBER.read(jsonReader));
    }

    // Test Number read with number token returns LazilyParsedNumber
    @Test
    public void testNumberRead_numberToken_returnsLazilyParsedNumber() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("123.45"));
        Number number = TypeAdapters.NUMBER.read(jsonReader);
        assertNotNull(number);
        assertTrue(number instanceof LazilyParsedNumber);
        assertEquals("123.45", number.toString());
    }

    // Test Number read with invalid token throws exception
    @Test(expected = JsonSyntaxException.class)
    public void testNumberRead_invalidToken_throwsException() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("\"abc\""));
        TypeAdapters.NUMBER.read(jsonReader);
    }

    // Test Character read with single character string
    @Test
    public void testCharacterRead_singleChar_returnsChar() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("\"a\""));
        assertEquals(Character.valueOf('a'), TypeAdapters.CHARACTER.read(jsonReader));
    }

    // Test Character read with multi-character string throws exception
    @Test(expected = JsonSyntaxException.class)
    public void testCharacterRead_multiChar_throwsException() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("\"abc\""));
        TypeAdapters.CHARACTER.read(jsonReader);
    }

    // Test String read with boolean token coerced to string
    @Test
    public void testStringRead_booleanToken_returnsString() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("true"));
        assertEquals("true", TypeAdapters.STRING.read(jsonReader));
    }

    // Test BigDecimal read with valid number string
    @Test
    public void testBigDecimalRead_validNumberString_returnsBigDecimal() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("\"123.45\""));
        assertEquals(new BigDecimal("123.45"), TypeAdapters.BIG_DECIMAL.read(jsonReader));
    }

    // Test BigInteger read with valid number string
    @Test
    public void testBigIntegerRead_validNumberString_returnsBigInteger() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("\"123456789\""));
        assertEquals(new BigInteger("123456789"), TypeAdapters.BIG_INTEGER.read(jsonReader));
    }

    // Test StringBuilder read with string value
    @Test
    public void testStringBuilderRead_validString_returnsStringBuilder() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("\"hello\""));
        assertEquals(new StringBuilder("hello").toString(), TypeAdapters.STRING_BUILDER.read(jsonReader).toString());
    }

    // Test StringBuffer read with string value
    @Test
    public void testStringBufferRead_validString_returnsStringBuffer() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("\"hello\""));
        assertEquals(new StringBuffer("hello").toString(), TypeAdapters.STRING_BUFFER.read(jsonReader).toString());
    }

    // Test URL read with string value
    @Test
    public void testUrlRead_validString_returnsUrl() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("\"http://example.com\""));
        assertEquals(new URL("http://example.com"), TypeAdapters.URL.read(jsonReader));
    }

    // Test URL read with "null" string returns null
    @Test
    public void testUrlRead_nullString_returnsNull() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("\"null\""));
        assertNull(TypeAdapters.URL.read(jsonReader));
    }

    // Test URI read with valid string
    @Test
    public void testUriRead_validString_returnsUri() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("\"http://example.com\""));
        assertEquals(new URI("http://example.com"), TypeAdapters.URI.read(jsonReader));
    }

    // Test URI read with invalid string throws exception
    @Test(expected = JsonIOException.class)
    public void testUriRead_invalidString_throwsException() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("\"invalid uri\""));
        TypeAdapters.URI.read(jsonReader);
    }

    // Test UUID read with valid string
    @Test
    public void testUuidRead_validString_returnsUuid() throws IOException {
        String uuidString = "123e4567-e89b-12d3-a456-426614174000";
        JsonReader jsonReader = new JsonReader(new StringReader("\"" + uuidString + "\""));
        assertEquals(UUID.fromString(uuidString), TypeAdapters.UUID.read(jsonReader));
    }

    // Test Calendar read with valid object
    @Test
    public void testCalendarRead_validObject_returnsCalendar() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader(
            "{\"year\":2024,\"month\":1,\"dayOfMonth\":15,\"hourOfDay\":10,\"minute\":30,\"second\":45}"));
        Calendar calendar = TypeAdapters.CALENDAR.read(jsonReader);
        assertNotNull(calendar);
        assertEquals(2024, calendar.get(Calendar.YEAR));
        assertEquals(1, calendar.get(Calendar.MONTH));
        assertEquals(15, calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(10, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, calendar.get(Calendar.MINUTE));
        assertEquals(45, calendar.get(Calendar.SECOND));
    }

    // Test Calendar write with non-null value
    @Test
    public void testCalendarWrite_nonNullValue_writesObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        Calendar calendar = new GregorianCalendar(2024, 1, 15, 10, 30, 45);
        TypeAdapters.CALENDAR.write(jsonWriter, calendar);
        jsonWriter.flush();
        assertEquals("{\"year\":2024,\"month\":1,\"dayOfMonth\":15,\"hourOfDay\":10,\"minute\":30,\"second\":45}",
            stringWriter.toString());
    }

    // Test Locale read with language only
    @Test
    public void testLocaleRead_languageOnly_returnsLocale() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("\"en\""));
        assertEquals(new Locale("en"), TypeAdapters.LOCALE.read(jsonReader));
    }

    // Test Locale read with language and country
    @Test
    public void testLocaleRead_languageAndCountry_returnsLocale() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("\"en_US\""));
        assertEquals(new Locale("en", "US"), TypeAdapters.LOCALE.read(jsonReader));
    }

    // Test Locale read with language, country, and variant
    @Test
    public void testLocaleRead_languageCountryVariant_returnsLocale() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("\"en_US_WIN\""));
        assertEquals(new Locale("en", "US", "WIN"), TypeAdapters.LOCALE.read(jsonReader));
    }

    // Test JsonElement read with string token
    @Test
    public void testJsonElementRead_stringToken_returnsPrimitive() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("\"foo\""));
        JsonElement element = TypeAdapters.JSON_ELEMENT.read(jsonReader);
        assertTrue(element.isJsonPrimitive());
        assertEquals("foo", element.getAsString());
    }

    // Test JsonElement read with number token
    @Test
    public void testJsonElementRead_numberToken_returnsPrimitive() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("123.45"));
        JsonElement element = TypeAdapters.JSON_ELEMENT.read(jsonReader);
        assertTrue(element.isJsonPrimitive());
        assertEquals(123.45, element.getAsDouble(), 0.001);
    }

    // Test JsonElement read with boolean token
    @Test
    public void testJsonElementRead_booleanToken_returnsPrimitive() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("true"));
        JsonElement element = TypeAdapters.JSON_ELEMENT.read(jsonReader);
        assertTrue(element.isJsonPrimitive());
        assertTrue(element.getAsBoolean());
    }

    // Test JsonElement read with null token
    @Test
    public void testJsonElementRead_nullToken_returnsJsonNull() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("null"));
        JsonElement element = TypeAdapters.JSON_ELEMENT.read(jsonReader);
        assertTrue(element.isJsonNull());
    }

    // Test JsonElement read with array token
    @Test
    public void testJsonElementRead_arrayToken_returnsArray() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("[1,2,3]"));
        JsonElement element = TypeAdapters.JSON_ELEMENT.read(jsonReader);
        assertTrue(element.isJsonArray());
        JsonArray array = element.getAsJsonArray();
        assertEquals(3, array.size());
    }

    // Test JsonElement read with object token
    @Test
    public void testJsonElementRead_objectToken_returnsObject() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("{\"key\":\"value\"}"));
        JsonElement element = TypeAdapters.JSON_ELEMENT.read(jsonReader);
        assertTrue(element.isJsonObject());
        JsonObject object = element.getAsJsonObject();
        assertEquals("value", object.get("key").getAsString());
    }

    // Test JsonElement read with invalid token throws exception
    @Test(expected = IllegalArgumentException.class)
    public void testJsonElementRead_invalidToken_throwsException() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader(""));
        TypeAdapters.JSON_ELEMENT.read(jsonReader);
    }

    // Test EnumTypeAdapter read with null token
    @Test
    public void testEnumTypeAdapterRead_nullToken_returnsNull() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("null"));
        TypeAdapters.ENUM_FACTORY.create(new Gson(), TypeToken.get(TestEnum.class)).read(jsonReader);
    }

    // Test newFactory with class type
    @Test
    public void testNewFactory_classType_returnsAdapter() {
        TypeAdapterFactory factory = TypeAdapters.newFactory(String.class, TypeAdapters.STRING);
        TypeAdapter<String> adapter = factory.create(new Gson(), TypeToken.get(String.class));
        assertNotNull(adapter);
    }

    // Test newFactory with unboxed and boxed types
    @Test
    public void testNewFactory_unboxedAndBoxed_returnsAdapter() {
        TypeAdapterFactory factory = TypeAdapters.newFactory(int.class, Integer.class, TypeAdapters.INTEGER);
        assertNotNull(factory.create(new Gson(), TypeToken.get(int.class)));
        assertNotNull(factory.create(new Gson(), TypeToken.get(Integer.class)));
    }

    // Test newFactoryForMultipleTypes with base and sub types
    @Test
    public void testNewFactoryForMultipleTypes_baseAndSubTypes_returnsAdapter() {
        TypeAdapterFactory factory = TypeAdapters.newFactoryForMultipleTypes(Calendar.class,
            GregorianCalendar.class, TypeAdapters.CALENDAR);
        assertNotNull(factory.create(new Gson(), TypeToken.get(Calendar.class)));
        assertNotNull(factory.create(new Gson(), TypeToken.get(GregorianCalendar.class)));
    }

    // Test newTypeHierarchyFactory with assignable classes
    @Test
    public void testNewTypeHierarchyFactory_assignableClass_returnsAdapter() {
        TypeAdapterFactory factory = TypeAdapters.newTypeHierarchyFactory(JsonElement.class, TypeAdapters.JSON_ELEMENT);
        assertNotNull(factory.create(new Gson(), TypeToken.get(JsonArray.class)));
        assertNotNull(factory.create(new Gson(), TypeToken.get(JsonObject.class)));
    }

    // ===== New tests to increase coverage =====

    // Test InetAddress read with valid IP
    @Test
    public void testInetAddressRead_validIp_returnsInetAddress() throws IOException, UnknownHostException {
        JsonReader jsonReader = new JsonReader(new StringReader("\"127.0.0.1\""));
        InetAddress addr = TypeAdapters.INET_ADDRESS.read(jsonReader);
        assertNotNull(addr);
        assertEquals(InetAddress.getByName("127.0.0.1"), addr);
    }

    // Test InetAddress read with null token
    @Test
    public void testInetAddressRead_nullToken_returnsNull() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.INET_ADDRESS.read(jsonReader));
    }

    // Test InetAddress write
    @Test
    public void testInetAddressWrite_nonNullValue_writesString() throws IOException, UnknownHostException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        InetAddress addr = InetAddress.getByName("192.168.0.1");
        TypeAdapters.INET_ADDRESS.write(jsonWriter, addr);
        jsonWriter.flush();
        assertEquals("\"192.168.0.1\"", stringWriter.toString());
    }

    // Test Enum adapter read with valid string
    @Test
    public void testEnumTypeAdapterRead_validString_returnsEnum() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("\"VALUE1\""));
        TypeAdapter<TestEnum> adapter = TypeAdapters.ENUM_FACTORY.create(new Gson(), TypeToken.get(TestEnum.class));
        assertEquals(TestEnum.VALUE1, adapter.read(jsonReader));
    }

    // Test Enum adapter write
    @Test
    public void testEnumTypeAdapterWrite_nonNullValue_writesString() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        TypeAdapter<TestEnum> adapter = TypeAdapters.ENUM_FACTORY.create(new Gson(), TypeToken.get(TestEnum.class));
        adapter.write(jsonWriter, TestEnum.VALUE2);
        jsonWriter.flush();
        assertEquals("\"VALUE2\"", stringWriter.toString());
    }

    // Test Locale write
    @Test
    public void testLocaleWrite_nonNullValue_writesString() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        TypeAdapters.LOCALE.write(jsonWriter, new Locale("en", "US", "WIN"));
        jsonWriter.flush();
        assertEquals("\"en_US_WIN\"", stringWriter.toString());
    }

    // Test String read with null token
    @Test
    public void testStringRead_nullToken_returnsNull() throws IOException {
        JsonReader jsonReader = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.STRING.read(jsonReader));
    }

    // Test Boolean write
    @Test
    public void testBooleanWrite_nonNullValue_writesBoolean() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        TypeAdapters.BOOLEAN.write(jsonWriter, true);
        jsonWriter.flush();
        assertEquals("true", stringWriter.toString());
    }

    // Test Integer write
    @Test
    public void testIntegerWrite_nonNullValue_writesNumber() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        TypeAdapters.INTEGER.write(jsonWriter, 42);
        jsonWriter.flush();
        assertEquals("42", stringWriter.toString());
    }

    // Test Number write
    @Test
    public void testNumberWrite_nonNullValue_writesNumber() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);
        TypeAdapters.NUMBER.write(jsonWriter, 3.14);
        jsonWriter.flush();
        assertEquals("3.14", stringWriter.toString());
    }

    private enum TestEnum {
        VALUE1,
        VALUE2
    }
}