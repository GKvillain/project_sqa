package com.google.gson.internal.bind;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonToken;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JsonTreeReaderTest {

  // Tests skipping a top-level primitive value to ensure stack index bounds
  @Test
  public void testSkipValue_topLevelPrimitive_skipsSuccessfully() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive("test"));
    reader.skipValue();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  // Tests skipping a field name and field value inside a JsonObject
  @Test
  public void testSkipValue_objectProperties_skipsNameAndValue() throws IOException {
    JsonObject obj = new JsonObject();
    obj.addProperty("key1", "val1");
    obj.addProperty("key2", "val2");

    JsonTreeReader reader = new JsonTreeReader(obj);
    reader.beginObject();
    reader.skipValue(); // skip key1
    reader.skipValue(); // skip val1
    assertEquals("key2", reader.nextName());
    assertEquals("val2", reader.nextString());
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  // Tests skipping elements inside a JsonArray
  @Test
  public void testSkipValue_arrayElements_skipsCorrectly() throws IOException {
    JsonArray array = new JsonArray();
    array.add(new JsonPrimitive(1));
    array.add(new JsonPrimitive(2));

    JsonTreeReader reader = new JsonTreeReader(array);
    reader.beginArray();
    reader.skipValue();
    assertEquals(2, reader.nextInt());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  // Tests reading primitive values: string, boolean, and null
  @Test
  public void testReadPrimitives_validInputs_returnsCorrectValues() throws IOException {
    JsonArray array = new JsonArray();
    array.add(new JsonPrimitive("hello"));
    array.add(new JsonPrimitive(true));
    array.add(JsonNull.INSTANCE);

    JsonTreeReader reader = new JsonTreeReader(array);
    reader.beginArray();
    assertEquals("hello", reader.nextString());
    assertTrue(reader.nextBoolean());
    reader.nextNull();
    reader.endArray();
  }

  // Tests reading numeric types: int, long, and double
  @Test
  public void testReadNumbers_validNumbers_returnsParsedValues() throws IOException {
    JsonArray array = new JsonArray();
    array.add(new JsonPrimitive(42));
    array.add(new JsonPrimitive(12345678901L));
    array.add(new JsonPrimitive(3.14159));

    JsonTreeReader reader = new JsonTreeReader(array);
    reader.beginArray();
    assertEquals(42, reader.nextInt());
    assertEquals(12345678901L, reader.nextLong());
    assertEquals(3.14159, reader.nextDouble(), 0.00001);
    reader.endArray();
  }

  // Tests iterating through an object with key-value pairs
  @Test
  public void testReadObject_validJsonObject_traversesEntries() throws IOException {
    JsonObject obj = new JsonObject();
    obj.addProperty("name", "Alice");
    obj.addProperty("age", 30);

    JsonTreeReader reader = new JsonTreeReader(obj);
    reader.beginObject();
    assertTrue(reader.hasNext());
    assertEquals("name", reader.nextName());
    assertEquals("Alice", reader.nextString());
    assertTrue(reader.hasNext());
    assertEquals("age", reader.nextName());
    assertEquals(30, reader.nextInt());
    assertFalse(reader.hasNext());
    reader.endObject();
  }

  // Tests promoteNameToValue converting a property name to a value token
  @Test
  public void testPromoteNameToValue_objectProperty_promotesNameToStringToken() throws IOException {
    JsonObject obj = new JsonObject();
    obj.addProperty("key", "value");

    JsonTreeReader reader = new JsonTreeReader(obj);
    reader.beginObject();
    reader.promoteNameToValue();
    assertEquals("key", reader.nextString());
    assertEquals("value", reader.nextString());
    reader.endObject();
  }

  // Tests getPath calculation for nested objects and arrays
  @Test
  public void testGetPath_nestedStructures_returnsAccurateJsonPath() throws IOException {
    JsonObject root = new JsonObject();
    JsonArray items = new JsonArray();
    JsonObject nested = new JsonObject();
    nested.addProperty("target", "found");
    items.add(nested);
    root.add("items", items);

    JsonTreeReader reader = new JsonTreeReader(root);
    assertEquals("$", reader.getPath());
    reader.beginObject();
    assertEquals("$.items", "$" + "." + reader.nextName());
    reader.beginArray();
    reader.beginObject();
    assertEquals("target", reader.nextName());
    assertEquals("$.items[0].target", reader.getPath());
    assertEquals("found", reader.nextString());
    reader.endObject();
    reader.endArray();
    reader.endObject();
  }

  // Tests exception when reading wrong token type
  @Test(expected = IllegalStateException.class)
  public void testExpect_tokenMismatch_throwsIllegalStateException() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive(true));
    reader.nextInt();
  }

  // Tests exception when reading NaN without lenient mode enabled
  @Test(expected = NumberFormatException.class)
  public void testNextDouble_nanNotLenient_throwsNumberFormatException() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive(Double.NaN));
    reader.nextDouble();
  }

  // Tests reading NaN with lenient mode enabled
  @Test
  public void testNextDouble_nanWithLenient_returnsNan() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive(Double.NaN));
    reader.setLenient(true);
    assertTrue(Double.isNaN(reader.nextDouble()));
  }

  // Tests closing reader and subsequent operation throwing exception
  @Test(expected = IllegalStateException.class)
  public void testClose_peekAfterClose_throwsIllegalStateException() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive("value"));
    reader.close();
    reader.peek();
  }

  // Tests stack resizing when nesting exceeds initial stack capacity (32)
  @Test
  public void testPush_deeplyNestedArray_expandsStackCorrectly() throws IOException {
    JsonArray root = new JsonArray();
    JsonArray current = root;
    for (int i = 0; i < 35; i++) {
      JsonArray next = new JsonArray();
      current.add(next);
      current = next;
    }
    current.add(new JsonPrimitive("deep"));

    JsonTreeReader reader = new JsonTreeReader(root);
    for (int i = 0; i < 36; i++) {
      reader.beginArray();
    }
    assertEquals("deep", reader.nextString());
    for (int i = 0; i < 36; i++) {
      reader.endArray();
    }
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  // Tests toString method of JsonTreeReader
  @Test
  public void testToString_default_returnsSimpleClassName() {
    JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive("a"));
    assertEquals("JsonTreeReader", reader.toString());
  }
}