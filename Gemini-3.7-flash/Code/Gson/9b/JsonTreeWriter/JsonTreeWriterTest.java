package com.google.gson.internal.bind;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.Test;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JsonTreeWriterTest {

  // Tests initial state returns JsonNull instance
  @Test
  public void testGet_initialState_returnsJsonNull() {
    JsonTreeWriter writer = new JsonTreeWriter();
    assertEquals(JsonNull.INSTANCE, writer.get());
  }

  // Tests string value writes JsonPrimitive
  @Test
  public void testValue_string_producesJsonPrimitive() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.value("hello");
    assertEquals(new JsonPrimitive("hello"), writer.get());
  }

  // Tests null string value writes JsonNull
  @Test
  public void testValue_nullString_producesJsonNull() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.value((String) null);
    assertEquals(JsonNull.INSTANCE, writer.get());
  }

  // Tests boolean value writes JsonPrimitive
  @Test
  public void testValue_boolean_producesJsonPrimitive() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.value(true);
    assertEquals(new JsonPrimitive(true), writer.get());
  }

  // Tests long value writes JsonPrimitive
  @Test
  public void testValue_long_producesJsonPrimitive() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.value(12345L);
    assertEquals(new JsonPrimitive(12345L), writer.get());
  }

  // Tests finite double value writes JsonPrimitive
  @Test
  public void testValue_double_producesJsonPrimitive() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.value(3.14);
    assertEquals(new JsonPrimitive(3.14), writer.get());
  }

  // Tests non-lenient NaN double throws IllegalArgumentException
  @Test(expected = IllegalArgumentException.class)
  public void testValue_doubleNaNStrict_throwsIllegalArgumentException() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(false);
    writer.value(Double.NaN);
  }

  // Tests lenient NaN double writes JsonPrimitive
  @Test
  public void testValue_doubleNaNLenient_producesJsonPrimitive() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(true);
    writer.value(Double.NaN);
    assertEquals(new JsonPrimitive(Double.NaN), writer.get());
  }

  // Tests finite Number value writes JsonPrimitive
  @Test
  public void testValue_number_producesJsonPrimitive() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.value(Double.valueOf(5.5));
    assertEquals(new JsonPrimitive(5.5), writer.get());
  }

  // Tests null Number value writes JsonNull
  @Test
  public void testValue_nullNumber_producesJsonNull() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.value((Number) null);
    assertEquals(JsonNull.INSTANCE, writer.get());
  }

  // Tests non-lenient infinite Number throws IllegalArgumentException
  @Test(expected = IllegalArgumentException.class)
  public void testValue_numberInfiniteStrict_throwsIllegalArgumentException() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(false);
    writer.value(Double.valueOf(Double.POSITIVE_INFINITY));
  }

  // Tests building valid array structure
  @Test
  public void testArray_validStructure_producesJsonArray() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.value("item");
    writer.value(10L);
    writer.nullValue();
    writer.endArray();

    JsonArray expected = new JsonArray();
    expected.add(new JsonPrimitive("item"));
    expected.add(new JsonPrimitive(10L));
    expected.add(JsonNull.INSTANCE);
    assertEquals(expected, writer.get());
  }

  // Tests building valid object structure
  @Test
  public void testObject_validStructure_producesJsonObject() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginObject();
    writer.name("key1").value("val1");
    writer.name("key2").value(false);
    writer.endObject();

    JsonObject expected = new JsonObject();
    expected.addProperty("key1", "val1");
    expected.addProperty("key2", false);
    assertEquals(expected, writer.get());
  }

  // Tests serializeNulls false omits null properties
  @Test
  public void testObject_serializeNullsFalse_omitsNullProperty() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setSerializeNulls(false);
    writer.beginObject();
    writer.name("key").nullValue();
    writer.endObject();

    JsonObject obj = (JsonObject) writer.get();
    assertFalse(obj.has("key"));
  }

  // Tests serializeNulls true includes null properties
  @Test
  public void testObject_serializeNullsTrue_includesNullProperty() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setSerializeNulls(true);
    writer.beginObject();
    writer.name("key").nullValue();
    writer.endObject();

    JsonObject obj = (JsonObject) writer.get();
    assertTrue(obj.has("key"));
    assertTrue(obj.get("key").isJsonNull());
  }

  // Tests get throws IllegalStateException when unclosed elements exist
  @Test(expected = IllegalStateException.class)
  public void testGet_unclosedStructure_throwsIllegalStateException() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.get();
  }

  // Tests endArray on empty stack throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testEndArray_emptyStack_throwsIllegalStateException() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.endArray();
  }

  // Tests endObject on JsonArray throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testEndObject_onArray_throwsIllegalStateException() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.endObject();
  }

  // Tests name without enclosing object throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testName_outsideObject_throwsIllegalStateException() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.name("key");
  }

  // Tests close on open stack throws IOException
  @Test(expected = IOException.class)
  public void testClose_unclosedDocument_throwsIOException() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.close();
  }

  // Tests write after close throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testClose_subsequentWrite_throwsIllegalStateException() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray().endArray();
    writer.close();
    writer.value("fail");
  }

  // Tests flush does not throw exception
  @Test
  public void testFlush_doesNotThrow() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.flush();
  }

  // Tests boxed Boolean value writes JsonPrimitive
  @Test
  public void testValue_boxedBoolean_producesJsonPrimitive() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.value(Boolean.TRUE);
    assertEquals(new JsonPrimitive(true), writer.get());
  }

  // Tests null boxed Boolean value writes JsonNull
  @Test
  public void testValue_nullBoxedBoolean_producesJsonNull() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.value((Boolean) null);
    assertEquals(JsonNull.INSTANCE, writer.get());
  }

  // Tests non-lenient negative infinity double throws IllegalArgumentException
  @Test(expected = IllegalArgumentException.class)
  public void testValue_doubleNegativeInfinityStrict_throwsIllegalArgumentException() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(false);
    writer.value(Double.NEGATIVE_INFINITY);
  }

  // Tests lenient negative infinity double writes JsonPrimitive
  @Test
  public void testValue_doubleNegativeInfinityLenient_producesJsonPrimitive() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(true);
    writer.value(Double.NEGATIVE_INFINITY);
    assertEquals(new JsonPrimitive(Double.NEGATIVE_INFINITY), writer.get());
  }

  // Tests lenient infinite Number writes JsonPrimitive
  @Test
  public void testValue_numberInfiniteLenient_producesJsonPrimitive() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(true);
    writer.value(Double.valueOf(Double.POSITIVE_INFINITY));
    assertEquals(new JsonPrimitive(Double.POSITIVE_INFINITY), writer.get());
  }

  // Tests lenient NaN Number writes JsonPrimitive
  @Test
  public void testValue_numberNaNLenient_producesJsonPrimitive() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(true);
    writer.value(Double.valueOf(Double.NaN));
    assertEquals(new JsonPrimitive(Double.NaN), writer.get());
  }

  // Tests null property name throws NullPointerException
  @Test(expected = NullPointerException.class)
  public void testName_nullName_throwsNullPointerException() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginObject();
    writer.name(null);
  }

  // Tests duplicate name before value throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testName_duplicateName_throwsIllegalStateException() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginObject();
    writer.name("key1");
    writer.name("key2");
  }

  // Tests name call inside array throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testName_insideArray_throwsIllegalStateException() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.name("key");
  }

  // Tests value inside object without calling name throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testValue_insideObjectWithoutName_throwsIllegalStateException() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginObject();
    writer.value("val");
  }

  // Tests endObject on empty stack throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testEndObject_emptyStack_throwsIllegalStateException() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.endObject();
  }

  // Tests endArray on object throws IllegalStateException
  @Test(expected = IllegalStateException.class)
  public void testEndArray_onObject_throwsIllegalStateException() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginObject();
    writer.endArray();
  }

  // Tests jsonValue throws UnsupportedOperationException
  @Test(expected = UnsupportedOperationException.class)
  public void testJsonValue_throwsUnsupportedOperationException() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.jsonValue("true");
  }

  // Tests close on closed empty writer keeps product accessible
  @Test
  public void testClose_validDocument_preservesProduct() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray().endArray();
    writer.close();
    assertEquals(new JsonArray(), writer.get());
  }
}