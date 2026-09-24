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
import java.sql.Timestamp;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

import static org.junit.Assert.*;

public class TypeAdaptersTest {

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

  private enum SampleEnum {
    @SerializedName(value = "first", alternate = {"1st", "one"})
    FIRST,
    SECOND
  }

  // Tests class serialization and deserialization exceptions and null handling
  @Test(expected = UnsupportedOperationException.class)
  public void testClass_serializeNonNull_throwsException() throws Exception {
    toJson(TypeAdapters.CLASS, String.class);
  }

  // Tests class serialization with null
  @Test
  public void testClass_nullHandling_success() throws Exception {
    assertEquals("null", toJson(TypeAdapters.CLASS, null));
    assertNull(fromJson(TypeAdapters.CLASS, "null"));
  }

  // Tests bitset serialization and deserialization with mixed tokens
  @Test
  public void testBitSet_validTokens_readWriteCorrectly() throws Exception {
    BitSet bitSet = new BitSet();
    bitSet.set(0);
    bitSet.set(2);
    String json = toJson(TypeAdapters.BIT_SET, bitSet);
    assertEquals("[1,0,1]", json);

    BitSet parsed = fromJson(TypeAdapters.BIT_SET, "[1, false, \"1\", 0]");
    assertTrue(parsed.get(0));
    assertFalse(parsed.get(1));
    assertTrue(parsed.get(2));
    assertFalse(parsed.get(3));

    assertNull(fromJson(TypeAdapters.BIT_SET, "null"));
    assertEquals("null", toJson(TypeAdapters.BIT_SET, null));
  }

  // Tests bitset invalid string token throwing JsonSyntaxException
  @Test(expected = JsonSyntaxException.class)
  public void testBitSet_invalidString_throwsException() throws Exception {
    fromJson(TypeAdapters.BIT_SET, "[\"invalid\"]");
  }

  // Tests boolean adapters for both standard and string-based representation
  @Test
  public void testBoolean_standardAndAsString_success() throws Exception {
    assertEquals("true", toJson(TypeAdapters.BOOLEAN, true));
    assertEquals(Boolean.TRUE, fromJson(TypeAdapters.BOOLEAN, "true"));
    assertEquals(Boolean.TRUE, fromJson(TypeAdapters.BOOLEAN, "\"true\""));
    assertNull(fromJson(TypeAdapters.BOOLEAN, "null"));

    assertEquals("\"true\"", toJson(TypeAdapters.BOOLEAN_AS_STRING, true));
    assertEquals("\"null\"", toJson(TypeAdapters.BOOLEAN_AS_STRING, null));
    assertEquals(Boolean.TRUE, fromJson(TypeAdapters.BOOLEAN_AS_STRING, "\"true\""));
    assertNull(fromJson(TypeAdapters.BOOLEAN_AS_STRING, "null"));
  }

  // Tests numeric type adapters with normal and null values
  @Test
  public void testNumbers_primitiveAdapters_readWriteCorrectly() throws Exception {
    assertEquals("1", toJson(TypeAdapters.BYTE, (byte) 1));
    assertEquals(Byte.valueOf((byte) 1), fromJson(TypeAdapters.BYTE, "1"));
    assertNull(fromJson(TypeAdapters.BYTE, "null"));

    assertEquals("2", toJson(TypeAdapters.SHORT, (short) 2));
    assertEquals(Short.valueOf((short) 2), fromJson(TypeAdapters.SHORT, "2"));
    assertNull(fromJson(TypeAdapters.SHORT, "null"));

    assertEquals("3", toJson(TypeAdapters.INTEGER, 3));
    assertEquals(Integer.valueOf(3), fromJson(TypeAdapters.INTEGER, "3"));
    assertNull(fromJson(TypeAdapters.INTEGER, "null"));

    assertEquals("4", toJson(TypeAdapters.LONG, 4L));
    assertEquals(Long.valueOf(4L), fromJson(TypeAdapters.LONG, "4"));
    assertNull(fromJson(TypeAdapters.LONG, "null"));

    assertEquals("5.5", toJson(TypeAdapters.FLOAT, 5.5f));
    assertEquals(5.5f, fromJson(TypeAdapters.FLOAT, "5.5").floatValue(), 0.001f);
    assertNull(fromJson(TypeAdapters.FLOAT, "null"));

    assertEquals("6.5", toJson(TypeAdapters.DOUBLE, 6.5d));
    assertEquals(6.5d, fromJson(TypeAdapters.DOUBLE, "6.5").doubleValue(), 0.001d);
    assertNull(fromJson(TypeAdapters.DOUBLE, "null"));

    assertEquals("100", toJson(TypeAdapters.NUMBER, 100));
    assertEquals(100, fromJson(TypeAdapters.NUMBER, "100").intValue());
    assertNull(fromJson(TypeAdapters.NUMBER, "null"));
  }

  // Tests character adapter with single character, null, and invalid multi-character string
  @Test
  public void testCharacter_validAndNull_readWriteCorrectly() throws Exception {
    assertEquals("\"a\"", toJson(TypeAdapters.CHARACTER, 'a'));
    assertEquals(Character.valueOf('a'), fromJson(TypeAdapters.CHARACTER, "\"a\""));
    assertNull(fromJson(TypeAdapters.CHARACTER, "null"));
  }

  // Tests character adapter invalid length throwing JsonSyntaxException
  @Test(expected = JsonSyntaxException.class)
  public void testCharacter_multiCharString_throwsException() throws Exception {
    fromJson(TypeAdapters.CHARACTER, "\"abc\"");
  }

  // Tests string-related adapters (String, StringBuilder, StringBuffer, BigInteger, BigDecimal)
  @Test
  public void testTextAndBigNumbers_validAndNull_readWriteCorrectly() throws Exception {
    assertEquals("\"hello\"", toJson(TypeAdapters.STRING, "hello"));
    assertEquals("hello", fromJson(TypeAdapters.STRING, "\"hello\""));
    assertEquals("true", fromJson(TypeAdapters.STRING, "true"));
    assertNull(fromJson(TypeAdapters.STRING, "null"));

    assertEquals("\"sb\"", toJson(TypeAdapters.STRING_BUILDER, new StringBuilder("sb")));
    assertEquals("sb", fromJson(TypeAdapters.STRING_BUILDER, "\"sb\"").toString());

    assertEquals("\"sbuf\"", toJson(TypeAdapters.STRING_BUFFER, new StringBuffer("sbuf")));
    assertEquals("sbuf", fromJson(TypeAdapters.STRING_BUFFER, "\"sbuf\"").toString());

    BigDecimal dec = new BigDecimal("123.45");
    assertEquals("123.45", toJson(TypeAdapters.BIG_DECIMAL, dec));
    assertEquals(dec, fromJson(TypeAdapters.BIG_DECIMAL, "123.45"));

    BigInteger integer = new BigInteger("12345678901234567890");
    assertEquals("12345678901234567890", toJson(TypeAdapters.BIG_INTEGER, integer));
    assertEquals(integer, fromJson(TypeAdapters.BIG_INTEGER, "12345678901234567890"));
  }

  // Tests URL and URI adapters
  @Test
  public void testUrlAndUri_validAndNull_readWriteCorrectly() throws Exception {
    URL url = new URL("http://google.com");
    assertEquals("\"http://google.com\"", toJson(TypeAdapters.URL, url));
    assertEquals(url, fromJson(TypeAdapters.URL, "\"http://google.com\""));
    assertNull(fromJson(TypeAdapters.URL, "\"null\""));
    assertNull(fromJson(TypeAdapters.URL, "null"));

    URI uri = new URI("http://google.com");
    assertEquals("\"http://google.com\"", toJson(TypeAdapters.URI, uri));
    assertEquals(uri, fromJson(TypeAdapters.URI, "\"http://google.com\""));
    assertNull(fromJson(TypeAdapters.URI, "\"null\""));
    assertNull(fromJson(TypeAdapters.URI, "null"));
  }

  // Tests URI invalid syntax throwing JsonIOException
  @Test(expected = JsonIOException.class)
  public void testUri_invalidSyntax_throwsJsonIOException() throws Exception {
    fromJson(TypeAdapters.URI, "\"http://invalid url with spaces\"");
  }

  // Tests InetAddress and UUID adapters
  @Test
  public void testInetAddressAndUUID_validAndNull_readWriteCorrectly() throws Exception {
    InetAddress address = InetAddress.getByName("127.0.0.1");
    assertEquals("\"127.0.0.1\"", toJson(TypeAdapters.INET_ADDRESS, address));
    assertEquals(address, fromJson(TypeAdapters.INET_ADDRESS, "\"127.0.0.1\""));
    assertNull(fromJson(TypeAdapters.INET_ADDRESS, "null"));

    UUID uuid = UUID.randomUUID();
    assertEquals("\"" + uuid.toString() + "\"", toJson(TypeAdapters.UUID, uuid));
    assertEquals(uuid, fromJson(TypeAdapters.UUID, "\"" + uuid.toString() + "\""));
    assertNull(fromJson(TypeAdapters.UUID, "null"));
  }

  // Tests Locale adapter with language, country, and variant
  @Test
  public void testLocale_languageCountryVariant_readWriteCorrectly() throws Exception {
    assertEquals("\"en\"", toJson(TypeAdapters.LOCALE, new Locale("en")));
    assertEquals(new Locale("en"), fromJson(TypeAdapters.LOCALE, "\"en\""));
    assertEquals(new Locale("en", "US"), fromJson(TypeAdapters.LOCALE, "\"en_US\""));
    assertEquals(new Locale("en", "US", "POSIX"), fromJson(TypeAdapters.LOCALE, "\"en_US_POSIX\""));
    assertNull(fromJson(TypeAdapters.LOCALE, "null"));
  }

  // Tests Calendar adapter serialization and deserialization
  @Test
  public void testCalendar_allFields_readWriteCorrectly() throws Exception {
    Calendar cal = new GregorianCalendar(2023, Calendar.MARCH, 15, 10, 20, 30);
    String json = toJson(TypeAdapters.CALENDAR, cal);
    Calendar parsed = fromJson(TypeAdapters.CALENDAR, json);

    assertEquals(cal.get(Calendar.YEAR), parsed.get(Calendar.YEAR));
    assertEquals(cal.get(Calendar.MONTH), parsed.get(Calendar.MONTH));
    assertEquals(cal.get(Calendar.DAY_OF_MONTH), parsed.get(Calendar.DAY_OF_MONTH));
    assertEquals(cal.get(Calendar.HOUR_OF_DAY), parsed.get(Calendar.HOUR_OF_DAY));
    assertEquals(cal.get(Calendar.MINUTE), parsed.get(Calendar.MINUTE));
    assertEquals(cal.get(Calendar.SECOND), parsed.get(Calendar.SECOND));

    assertNull(fromJson(TypeAdapters.CALENDAR, "null"));
    assertEquals("null", toJson(TypeAdapters.CALENDAR, null));
  }

  // Tests Timestamp factory creation and conversion
  @Test
  public void testTimestampFactory_validTimestamp_readWriteCorrectly() throws Exception {
    Gson gson = new Gson();
    TypeAdapter<Timestamp> adapter = TypeAdapters.TIMESTAMP_FACTORY.create(gson, TypeToken.get(Timestamp.class));
    assertNotNull(adapter);
    assertNull(TypeAdapters.TIMESTAMP_FACTORY.create(gson, TypeToken.get(String.class)));

    long now = System.currentTimeMillis();
    Timestamp ts = new Timestamp(now);
    String json = toJson(adapter, ts);
    Timestamp parsed = fromJson(adapter, json);
    assertNotNull(parsed);
    assertEquals(ts.getTime() / 1000, parsed.getTime() / 1000);
  }

  // Tests Enum factory with SerializedName and alternate names
  @Test
  public void testEnumFactory_serializedNameAndAlternates_readWriteCorrectly() throws Exception {
    Gson gson = new Gson();
    TypeAdapter<SampleEnum> adapter = TypeAdapters.ENUM_FACTORY.create(gson, TypeToken.get(SampleEnum.class));
    assertNotNull(adapter);

    assertEquals("\"first\"", toJson(adapter, SampleEnum.FIRST));
    assertEquals(SampleEnum.FIRST, fromJson(adapter, "\"first\""));
    assertEquals(SampleEnum.FIRST, fromJson(adapter, "\"1st\""));
    assertEquals(SampleEnum.FIRST, fromJson(adapter, "\"one\""));
    assertEquals(SampleEnum.SECOND, fromJson(adapter, "\"SECOND\""));
    assertNull(fromJson(adapter, "null"));
  }

  // Tests JsonElement adapter for primitive, array, object, and null
  @Test
  public void testJsonElement_allVariants_readWriteCorrectly() throws Exception {
    assertEquals("null", toJson(TypeAdapters.JSON_ELEMENT, JsonNull.INSTANCE));
    assertEquals(JsonNull.INSTANCE, fromJson(TypeAdapters.JSON_ELEMENT, "null"));

    JsonPrimitive strPrim = new JsonPrimitive("text");
    assertEquals("\"text\"", toJson(TypeAdapters.JSON_ELEMENT, strPrim));
    assertEquals(strPrim, fromJson(TypeAdapters.JSON_ELEMENT, "\"text\""));

    JsonPrimitive numPrim = new JsonPrimitive(42);
    assertEquals("42", toJson(TypeAdapters.JSON_ELEMENT, numPrim));
    assertEquals(numPrim, fromJson(TypeAdapters.JSON_ELEMENT, "42"));

    JsonPrimitive boolPrim = new JsonPrimitive(true);
    assertEquals("true", toJson(TypeAdapters.JSON_ELEMENT, boolPrim));
    assertEquals(boolPrim, fromJson(TypeAdapters.JSON_ELEMENT, "true"));

    JsonArray array = new JsonArray();
    array.add(new JsonPrimitive(1));
    array.add(new JsonPrimitive("a"));
    assertEquals("[1,\"a\"]", toJson(TypeAdapters.JSON_ELEMENT, array));
    assertEquals(array, fromJson(TypeAdapters.JSON_ELEMENT, "[1,\"a\"]"));

    JsonObject obj = new JsonObject();
    obj.addProperty("key", "value");
    assertEquals("{\"key\":\"value\"}", toJson(TypeAdapters.JSON_ELEMENT, obj));
    assertEquals(obj, fromJson(TypeAdapters.JSON_ELEMENT, "{\"key\":\"value\"}"));
  }

  // Tests newTypeHierarchyFactory subtype hierarchy matching
  @Test
  public void testNewTypeHierarchyFactory_hierarchyMatching_returnsAdapter() {
    TypeAdapterFactory factory = TypeAdapters.newTypeHierarchyFactory(Number.class, TypeAdapters.INTEGER);
    assertNotNull(factory.create(new Gson(), TypeToken.get(Integer.class)));
    assertNotNull(factory.create(new Gson(), TypeToken.get(Double.class)));
    assertNull(factory.create(new Gson(), TypeToken.get(String.class)));
    assertTrue(factory.toString().contains("Factory[typeHierarchy="));
  }

  // Tests factory helper methods toString and type matching
  @Test
  public void testFactoryHelpers_typeMatchingAndToString_correctBehavior() {
    TypeAdapterFactory singleFactory = TypeAdapters.newFactory(String.class, TypeAdapters.STRING);
    assertNotNull(singleFactory.create(new Gson(), TypeToken.get(String.class)));
    assertNull(singleFactory.create(new Gson(), TypeToken.get(Integer.class)));
    assertTrue(singleFactory.toString().contains("Factory[type="));

    TypeAdapterFactory boxedFactory = TypeAdapters.newFactory(int.class, Integer.class, TypeAdapters.INTEGER);
    assertNotNull(boxedFactory.create(new Gson(), TypeToken.get(int.class)));
    assertNotNull(boxedFactory.create(new Gson(), TypeToken.get(Integer.class)));
    assertNull(boxedFactory.create(new Gson(), TypeToken.get(Long.class)));
    assertTrue(boxedFactory.toString().contains("Factory[type="));

    TypeAdapterFactory multiFactory = TypeAdapters.newFactoryForMultipleTypes(
        Calendar.class, GregorianCalendar.class, TypeAdapters.CALENDAR);
    assertNotNull(multiFactory.create(new Gson(), TypeToken.get(Calendar.class)));
    assertNotNull(multiFactory.create(new Gson(), TypeToken.get(GregorianCalendar.class)));
    assertNull(multiFactory.create(new Gson(), TypeToken.get(Date.class)));
    assertTrue(multiFactory.toString().contains("Factory[type="));
  }

  // Tests AtomicInteger, AtomicBoolean, and AtomicIntegerArray adapters
  @Test
  public void testAtomicTypes_readWriteCorrectly() throws Exception {
    assertEquals("42", toJson(TypeAdapters.ATOMIC_INTEGER, new AtomicInteger(42)));
    assertEquals(42, fromJson(TypeAdapters.ATOMIC_INTEGER, "42").get());

    assertEquals("true", toJson(TypeAdapters.ATOMIC_BOOLEAN, new AtomicBoolean(true)));
    assertEquals(true, fromJson(TypeAdapters.ATOMIC_BOOLEAN, "true").get());

    AtomicIntegerArray array = new AtomicIntegerArray(new int[]{1, 2, 3});
    assertEquals("[1,2,3]", toJson(TypeAdapters.ATOMIC_INTEGER_ARRAY, array));
    AtomicIntegerArray parsedArray = fromJson(TypeAdapters.ATOMIC_INTEGER_ARRAY, "[1,2,3]");
    assertEquals(3, parsedArray.length());
    assertEquals(1, parsedArray.get(0));
    assertEquals(2, parsedArray.get(1));
    assertEquals(3, parsedArray.get(2));
  }

  // Tests Currency adapter
  @Test
  public void testCurrency_readWriteCorrectly() throws Exception {
    Currency currency = Currency.getInstance("USD");
    assertEquals("\"USD\"", toJson(TypeAdapters.CURRENCY, currency));
    assertEquals(currency, fromJson(TypeAdapters.CURRENCY, "\"USD\""));
    assertNull(fromJson(TypeAdapters.CURRENCY, "null"));
  }

  // Tests TypeToken factory creation helper
  @Test
  public void testNewFactory_typeToken_correctBehavior() {
    TypeAdapterFactory factory = TypeAdapters.newFactory(TypeToken.get(String.class), TypeAdapters.STRING);
    assertNotNull(factory.create(new Gson(), TypeToken.get(String.class)));
    assertNull(factory.create(new Gson(), TypeToken.get(Integer.class)));
    assertTrue(factory.toString().contains("Factory[type="));
  }
}