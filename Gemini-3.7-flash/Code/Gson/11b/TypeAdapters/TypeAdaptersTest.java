package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
    @SerializedName(value = "second_val", alternate = {"second_alt"})
    SECOND
  }

  private <T> String toJson(TypeAdapter<T> adapter, T value) throws IOException {
    StringWriter writer = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(writer);
    adapter.write(jsonWriter, value);
    return writer.toString();
  }

  private <T> T fromJson(TypeAdapter<T> adapter, String json) throws IOException {
    JsonReader jsonReader = new JsonReader(new StringReader(json));
    return adapter.read(jsonReader);
  }

  // Tests Number type adapter reading null and number values
  @Test
  public void testNumberAdapter_validInputs_returnsLazilyParsedNumber() throws IOException {
    assertNull(fromJson(TypeAdapters.NUMBER, "null"));
    assertEquals("123.45", fromJson(TypeAdapters.NUMBER, "123.45").toString());
    assertEquals("123", toJson(TypeAdapters.NUMBER, 123));
  }

  // Tests Number type adapter with invalid non-number token
  @Test(expected = JsonSyntaxException.class)
  public void testNumberAdapter_booleanInput_throwsJsonSyntaxException() throws IOException {
    fromJson(TypeAdapters.NUMBER, "true");
  }

  // Tests Class adapter serialization exception
  @Test(expected = UnsupportedOperationException.class)
  public void testClassAdapter_writeNonNull_throwsUnsupportedOperationException() throws IOException {
    toJson(TypeAdapters.CLASS, String.class);
  }

  // Tests Class adapter read null and non-null
  @Test
  public void testClassAdapter_nullReadAndWrite_handledCorrectly() throws IOException {
    assertNull(fromJson(TypeAdapters.CLASS, "null"));
    assertEquals("null", toJson(TypeAdapters.CLASS, null));
  }

  // Tests Class adapter deserialization exception
  @Test(expected = UnsupportedOperationException.class)
  public void testClassAdapter_readNonNull_throwsUnsupportedOperationException() throws IOException {
    fromJson(TypeAdapters.CLASS, "\"java.lang.String\"");
  }

  // Tests BitSet adapter with number, boolean, and string array elements
  @Test
  public void testBitSetAdapter_validInputs_readsAndWritesCorrectly() throws IOException {
    BitSet bitSet = fromJson(TypeAdapters.BIT_SET, "[1, 0, true, false, \"1\", \"0\"]");
    assertTrue(bitSet.get(0));
    assertFalse(bitSet.get(1));
    assertTrue(bitSet.get(2));
    assertFalse(bitSet.get(3));
    assertTrue(bitSet.get(4));
    assertFalse(bitSet.get(5));

    BitSet toSerialize = new BitSet();
    toSerialize.set(0);
    toSerialize.set(2);
    assertEquals("[1,0,1]", toJson(TypeAdapters.BIT_SET, toSerialize));
    assertEquals("null", toJson(TypeAdapters.BIT_SET, null));
    assertNull(fromJson(TypeAdapters.BIT_SET, "null"));
  }

  // Tests BitSet adapter invalid string element
  @Test(expected = JsonSyntaxException.class)
  public void testBitSetAdapter_invalidStringElement_throwsJsonSyntaxException() throws IOException {
    fromJson(TypeAdapters.BIT_SET, "[\"invalid\"]");
  }

  // Tests Boolean adapters
  @Test
  public void testBooleanAdapters_validValues_returnsCorrectResults() throws IOException {
    assertTrue(fromJson(TypeAdapters.BOOLEAN, "true"));
    assertTrue(fromJson(TypeAdapters.BOOLEAN, "\"true\""));
    assertNull(fromJson(TypeAdapters.BOOLEAN, "null"));
    assertEquals("true", toJson(TypeAdapters.BOOLEAN, true));

    assertTrue(fromJson(TypeAdapters.BOOLEAN_AS_STRING, "\"true\""));
    assertNull(fromJson(TypeAdapters.BOOLEAN_AS_STRING, "null"));
    assertEquals("\"true\"", toJson(TypeAdapters.BOOLEAN_AS_STRING, true));
    assertEquals("\"null\"", toJson(TypeAdapters.BOOLEAN_AS_STRING, null));
  }

  // Tests primitive number adapters (Byte, Short, Integer, Long, Float, Double)
  @Test
  public void testPrimitiveNumberAdapters_validInputs_readsAndWritesCorrectly() throws IOException {
    assertEquals((byte) 42, fromJson(TypeAdapters.BYTE, "42").byteValue());
    assertNull(fromJson(TypeAdapters.BYTE, "null"));

    assertEquals((short) 1000, fromJson(TypeAdapters.SHORT, "1000").shortValue());
    assertNull(fromJson(TypeAdapters.SHORT, "null"));

    assertEquals(123456, fromJson(TypeAdapters.INTEGER, "123456").intValue());
    assertNull(fromJson(TypeAdapters.INTEGER, "null"));

    assertEquals(9876543210L, fromJson(TypeAdapters.LONG, "9876543210").longValue());
    assertNull(fromJson(TypeAdapters.LONG, "null"));

    assertEquals(3.14f, fromJson(TypeAdapters.FLOAT, "3.14").floatValue(), 0.0001f);
    assertNull(fromJson(TypeAdapters.FLOAT, "null"));

    assertEquals(2.71828, fromJson(TypeAdapters.DOUBLE, "2.71828").doubleValue(), 0.00001);
    assertNull(fromJson(TypeAdapters.DOUBLE, "null"));
  }

  // Tests Atomic types
  @Test
  public void testAtomicTypes_validInputs_readsAndWritesCorrectly() throws IOException {
    AtomicInteger atomicInt = fromJson(TypeAdapters.ATOMIC_INTEGER, "42");
    assertEquals(42, atomicInt.get());
    assertEquals("42", toJson(TypeAdapters.ATOMIC_INTEGER, new AtomicInteger(42)));

    AtomicBoolean atomicBool = fromJson(TypeAdapters.ATOMIC_BOOLEAN, "true");
    assertTrue(atomicBool.get());
    assertEquals("true", toJson(TypeAdapters.ATOMIC_BOOLEAN, new AtomicBoolean(true)));

    AtomicIntegerArray atomicArray = fromJson(TypeAdapters.ATOMIC_INTEGER_ARRAY, "[1, 2, 3]");
    assertEquals(3, atomicArray.length());
    assertEquals(1, atomicArray.get(0));
    assertEquals(2, atomicArray.get(1));
    assertEquals(3, atomicArray.get(2));
    assertEquals("[1,2,3]", toJson(TypeAdapters.ATOMIC_INTEGER_ARRAY, atomicArray));
  }

  // Tests Character and String adapters
  @Test
  public void testCharacterAndStringAdapters_validInputs_returnsCorrectResults() throws IOException {
    assertEquals(Character.valueOf('a'), fromJson(TypeAdapters.CHARACTER, "\"a\""));
    assertNull(fromJson(TypeAdapters.CHARACTER, "null"));
    assertEquals("\"a\"", toJson(TypeAdapters.CHARACTER, 'a'));
    assertEquals("null", toJson(TypeAdapters.CHARACTER, null));

    assertEquals("hello", fromJson(TypeAdapters.STRING, "\"hello\""));
    assertEquals("true", fromJson(TypeAdapters.STRING, "true"));
    assertNull(fromJson(TypeAdapters.STRING, "null"));
    assertEquals("\"hello\"", toJson(TypeAdapters.STRING, "hello"));
  }

  // Tests Character adapter with multi-character string exception
  @Test(expected = JsonSyntaxException.class)
  public void testCharacterAdapter_multiCharString_throwsJsonSyntaxException() throws IOException {
    fromJson(TypeAdapters.CHARACTER, "\"abc\"");
  }

  // Tests BigDecimal and BigInteger adapters
  @Test
  public void testBigDecimalAndBigIntegerAdapters_validInputs_returnsExpected() throws IOException {
    assertEquals(new BigDecimal("12345.6789"), fromJson(TypeAdapters.BIG_DECIMAL, "\"12345.6789\""));
    assertNull(fromJson(TypeAdapters.BIG_DECIMAL, "null"));

    assertEquals(new BigInteger("12345678901234567890"), fromJson(TypeAdapters.BIG_INTEGER, "\"12345678901234567890\""));
    assertNull(fromJson(TypeAdapters.BIG_INTEGER, "null"));
  }

  // Tests StringBuilder and StringBuffer adapters
  @Test
  public void testStringBuilderAndStringBuffer_validInputs_returnsExpected() throws IOException {
    StringBuilder sb = fromJson(TypeAdapters.STRING_BUILDER, "\"testSB\"");
    assertEquals("testSB", sb.toString());
    assertNull(fromJson(TypeAdapters.STRING_BUILDER, "null"));
    assertEquals("\"testSB\"", toJson(TypeAdapters.STRING_BUILDER, new StringBuilder("testSB")));

    StringBuffer sbuf = fromJson(TypeAdapters.STRING_BUFFER, "\"testSBuf\"");
    assertEquals("testSBuf", sbuf.toString());
    assertNull(fromJson(TypeAdapters.STRING_BUFFER, "null"));
    assertEquals("\"testSBuf\"", toJson(TypeAdapters.STRING_BUFFER, new StringBuffer("testSBuf")));
  }

  // Tests URL, URI, and InetAddress adapters
  @Test
  public void testUrlUriInetAddressAdapters_validInputs_returnsExpected() throws IOException {
    URL url = fromJson(TypeAdapters.URL, "\"http://example.com\"");
    assertEquals("http://example.com", url.toExternalForm());
    assertNull(fromJson(TypeAdapters.URL, "null"));
    assertNull(fromJson(TypeAdapters.URL, "\"null\""));
    assertEquals("\"http://example.com\"", toJson(TypeAdapters.URL, new URL("http://example.com")));

    URI uri = fromJson(TypeAdapters.URI, "\"http://example.com\"");
    assertEquals("http://example.com", uri.toASCIIString());
    assertNull(fromJson(TypeAdapters.URI, "null"));
    assertNull(fromJson(TypeAdapters.URI, "\"null\""));
    assertEquals("\"http://example.com\"", toJson(TypeAdapters.URI, URI.create("http://example.com")));

    InetAddress inet = fromJson(TypeAdapters.INET_ADDRESS, "\"127.0.0.1\"");
    assertEquals("127.0.0.1", inet.getHostAddress());
    assertNull(fromJson(TypeAdapters.INET_ADDRESS, "null"));
    assertEquals("\"127.0.0.1\"", toJson(TypeAdapters.INET_ADDRESS, inet));
  }

  // Tests UUID, Currency, Calendar, and Locale adapters
  @Test
  public void testMiscAdapters_validInputs_returnsExpected() throws IOException {
    UUID uuid = UUID.randomUUID();
    assertEquals(uuid, fromJson(TypeAdapters.UUID, "\"" + uuid.toString() + "\""));
    assertNull(fromJson(TypeAdapters.UUID, "null"));
    assertEquals("\"" + uuid.toString() + "\"", toJson(TypeAdapters.UUID, uuid));

    Currency currency = Currency.getInstance("USD");
    assertEquals(currency, fromJson(TypeAdapters.CURRENCY, "\"USD\""));
    assertEquals("\"USD\"", toJson(TypeAdapters.CURRENCY, currency));

    Locale localeFull = fromJson(TypeAdapters.LOCALE, "\"en_US_WIN\"");
    assertEquals(new Locale("en", "US", "WIN"), localeFull);
    Locale localeCountry = fromJson(TypeAdapters.LOCALE, "\"en_US\"");
    assertEquals(new Locale("en", "US"), localeCountry);
    Locale localeLang = fromJson(TypeAdapters.LOCALE, "\"en\"");
    assertEquals(new Locale("en"), localeLang);
    assertNull(fromJson(TypeAdapters.LOCALE, "null"));
    assertEquals("\"en_US\"", toJson(TypeAdapters.LOCALE, Locale.US));

    String calJson = "{\"year\":2023,\"month\":5,\"dayOfMonth\":15,\"hourOfDay\":10,\"minute\":30,\"second\":45}";
    Calendar cal = fromJson(TypeAdapters.CALENDAR, calJson);
    assertEquals(2023, cal.get(Calendar.YEAR));
    assertEquals(5, cal.get(Calendar.MONTH));
    assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
    assertEquals(10, cal.get(Calendar.HOUR_OF_DAY));
    assertEquals(30, cal.get(Calendar.MINUTE));
    assertEquals(45, cal.get(Calendar.SECOND));
    assertNull(fromJson(TypeAdapters.CALENDAR, "null"));
    assertEquals("null", toJson(TypeAdapters.CALENDAR, null));
  }

  // Tests JsonElement adapter with primitive, array, object, and null
  @Test
  public void testJsonElementAdapter_allElementTypes_readsAndWritesCorrectly() throws IOException {
    JsonElement nullElem = fromJson(TypeAdapters.JSON_ELEMENT, "null");
    assertEquals(JsonNull.INSTANCE, nullElem);

    JsonElement primStr = fromJson(TypeAdapters.JSON_ELEMENT, "\"hello\"");
    assertEquals(new JsonPrimitive("hello"), primStr);

    JsonElement primNum = fromJson(TypeAdapters.JSON_ELEMENT, "123");
    assertEquals(123, primNum.getAsInt());

    JsonElement primBool = fromJson(TypeAdapters.JSON_ELEMENT, "true");
    assertTrue(primBool.getAsBoolean());

    JsonElement arrElem = fromJson(TypeAdapters.JSON_ELEMENT, "[1, \"a\", false]");
    assertTrue(arrElem.isJsonArray());
    assertEquals(3, arrElem.getAsJsonArray().size());

    JsonElement objElem = fromJson(TypeAdapters.JSON_ELEMENT, "{\"key\":\"val\"}");
    assertTrue(objElem.isJsonObject());
    assertEquals("val", objElem.getAsJsonObject().get("key").getAsString());

    assertEquals("null", toJson(TypeAdapters.JSON_ELEMENT, JsonNull.INSTANCE));
    assertEquals("null", toJson(TypeAdapters.JSON_ELEMENT, null));
    assertEquals("\"str\"", toJson(TypeAdapters.JSON_ELEMENT, new JsonPrimitive("str")));
    assertEquals("true", toJson(TypeAdapters.JSON_ELEMENT, new JsonPrimitive(true)));
    assertEquals("10", toJson(TypeAdapters.JSON_ELEMENT, new JsonPrimitive(10)));
  }

  // Tests Enum adapter and Enum factory with SerializedName and alternates
  @Test
  public void testEnumAdapter_serializedNameAndAlternates_readsAndWrites() throws IOException {
    Gson gson = new Gson();
    TypeAdapter<TestEnum> adapter = TypeAdapters.ENUM_FACTORY.create(gson, TypeToken.get(TestEnum.class));
    assertNotNull(adapter);

    assertEquals(TestEnum.FIRST, fromJson(adapter, "\"first_val\""));
    assertEquals(TestEnum.SECOND, fromJson(adapter, "\"second_val\""));
    assertEquals(TestEnum.SECOND, fromJson(adapter, "\"second_alt\""));
    assertNull(fromJson(adapter, "null"));

    assertEquals("\"first_val\"", toJson(adapter, TestEnum.FIRST));
    assertEquals("\"second_val\"", toJson(adapter, TestEnum.SECOND));
    assertEquals("null", toJson(adapter, null));
  }

  // Tests factory creators and toString representation
  @Test
  public void testFactories_creationAndToString_returnsExpected() {
    Gson gson = new Gson();

    TypeAdapterFactory typeTokenFactory = TypeAdapters.newFactory(new TypeToken<String>() {}, TypeAdapters.STRING);
    assertNotNull(typeTokenFactory.create(gson, TypeToken.get(String.class)));
    assertNull(typeTokenFactory.create(gson, TypeToken.get(Integer.class)));

    TypeAdapterFactory classFactory = TypeAdapters.newFactory(String.class, TypeAdapters.STRING);
    assertNotNull(classFactory.create(gson, TypeToken.get(String.class)));
    assertNull(classFactory.create(gson, TypeToken.get(Integer.class)));
    assertTrue(classFactory.toString().contains("Factory[type="));

    TypeAdapterFactory boxedUnboxedFactory = TypeAdapters.newFactory(int.class, Integer.class, TypeAdapters.INTEGER);
    assertNotNull(boxedUnboxedFactory.create(gson, TypeToken.get(int.class)));
    assertNotNull(boxedUnboxedFactory.create(gson, TypeToken.get(Integer.class)));
    assertNull(boxedUnboxedFactory.create(gson, TypeToken.get(String.class)));
    assertTrue(boxedUnboxedFactory.toString().contains("Factory[type="));

    TypeAdapterFactory multiFactory = TypeAdapters.newFactoryForMultipleTypes(
        Calendar.class, java.util.GregorianCalendar.class, TypeAdapters.CALENDAR);
    assertNotNull(multiFactory.create(gson, TypeToken.get(Calendar.class)));
    assertNotNull(multiFactory.create(gson, TypeToken.get(java.util.GregorianCalendar.class)));
    assertNull(multiFactory.create(gson, TypeToken.get(String.class)));
    assertTrue(multiFactory.toString().contains("Factory[type="));

    TypeAdapterFactory hierarchyFactory = TypeAdapters.newTypeHierarchyFactory(Number.class, TypeAdapters.NUMBER);
    assertNotNull(hierarchyFactory.create(gson, TypeToken.get(Number.class)));
    assertNotNull(hierarchyFactory.create(gson, TypeToken.get(Long.class)));
    assertNull(hierarchyFactory.create(gson, TypeToken.get(String.class)));
    assertTrue(hierarchyFactory.toString().contains("Factory[typeHierarchy="));
  }
}