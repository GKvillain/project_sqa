package com.google.gson.internal.bind;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.Excluder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ReflectiveTypeAdapterFactoryTest {

  private Gson gson;
  private ReflectiveTypeAdapterFactory factory;

  @Before
  public void setUp() {
    gson = new Gson();
    factory = new ReflectiveTypeAdapterFactory(
        new ConstructorConstructor(Collections.<java.lang.reflect.Type, com.google.gson.InstanceCreator<?>>emptyMap()),
        FieldNamingPolicy.IDENTITY,
        Excluder.DEFAULT
    );
  }

  // Tests create with primitive type returns null
  @Test
  public void testCreate_primitiveType_returnsNull() {
    TypeAdapter<Integer> adapter = factory.create(gson, TypeToken.get(int.class));
    assertNull(adapter);
  }

  // Tests serialization of a basic POJO
  @Test
  public void testWrite_simpleObject_serializesCorrectly() {
    SimpleClass obj = new SimpleClass();
    obj.intValue = 42;
    obj.stringValue = "test";

    String json = gson.toJson(obj);
    assertTrue(json.contains("\"intValue\":42"));
    assertTrue(json.contains("\"stringValue\":\"test\""));
  }

  // Tests deserialization of a basic POJO
  @Test
  public void testRead_validJson_deserializesCorrectly() {
    String json = "{\"intValue\":100,\"stringValue\":\"hello\"}";
    SimpleClass result = gson.fromJson(json, SimpleClass.class);

    assertNotNull(result);
    assertEquals(100, result.intValue);
    assertEquals("hello", result.stringValue);
  }

  // Tests serialization and deserialization with null instance
  @Test
  public void testWriteAndRead_nullInstance_handlesNullProperly() {
    String json = gson.toJson(null, SimpleClass.class);
    assertEquals("null", json);

    SimpleClass result = gson.fromJson("null", SimpleClass.class);
    assertNull(result);
  }

  // Tests @SerializedName value and alternate names on deserialization
  @Test
  public void testRead_serializedNameWithAlternates_deserializesUsingAlternate() {
    String json = "{\"alt1\":\"foundAlt\"}";
    SerializedNameClass result = gson.fromJson(json, SerializedNameClass.class);

    assertNotNull(result);
    assertEquals("foundAlt", result.mainName);

    String serialized = gson.toJson(result);
    assertTrue(serialized.contains("\"primary\":\"foundAlt\""));
    assertFalse(serialized.contains("alt1"));
  }

  // Tests inheritance hierarchy bound fields
  @Test
  public void testWriteAndRead_subclassFields_includesSuperclassFields() {
    SubClass sub = new SubClass();
    sub.superField = "parent";
    sub.subField = "child";

    String json = gson.toJson(sub);
    assertTrue(json.contains("\"superField\":\"parent\""));
    assertTrue(json.contains("\"subField\":\"child\""));

    SubClass result = gson.fromJson(json, SubClass.class);
    assertEquals("parent", result.superField);
    assertEquals("child", result.subField);
  }

  // Tests duplicate field names collision throws IllegalArgumentException
  @Test(expected = IllegalArgumentException.class)
  public void testCreate_duplicateFieldNames_throwsIllegalArgumentException() {
    factory.create(gson, TypeToken.get(DuplicateFieldClass.class));
  }

  // Tests self-referencing field avoids infinite recursion on write
  @Test
  public void testWrite_selfReferencingField_skipsSelfField() {
    SelfReferenceClass obj = new SelfReferenceClass();
    obj.self = obj;
    obj.name = "selfRef";

    String json = gson.toJson(obj);
    assertTrue(json.contains("\"name\":\"selfRef\""));
    assertFalse(json.contains("\"self\""));
  }

  // Tests excluded field with transient modifier
  @Test
  public void testExcludeField_transientModifier_returnsTrue() throws Exception {
    Field transientField = ExcludedClass.class.getDeclaredField("transientField");
    Field normalField = ExcludedClass.class.getDeclaredField("normalField");

    assertTrue(factory.excludeField(transientField, true));
    assertTrue(factory.excludeField(transientField, false));
    assertFalse(factory.excludeField(normalField, true));
    assertFalse(factory.excludeField(normalField, false));
  }

  // Tests deserialization skips unknown fields in JSON
  @Test
  public void testRead_unknownFieldsInJson_skipsSuccessfully() {
    String json = "{\"unknownField\":123,\"intValue\":55,\"extra\":{\"nested\":true}}";
    SimpleClass result = gson.fromJson(json, SimpleClass.class);

    assertNotNull(result);
    assertEquals(55, result.intValue);
  }

  // Tests reading primitive field with null value leaves default primitive value
  @Test
  public void testRead_primitiveWithNullJsonValue_preservesDefaultValue() {
    String json = "{\"intValue\":null}";
    SimpleClass result = gson.fromJson(json, SimpleClass.class);

    assertNotNull(result);
    assertEquals(0, result.intValue);
  }

  // Tests invalid json token syntax throws JsonSyntaxException
  @Test(expected = JsonSyntaxException.class)
  public void testRead_invalidJsonStructure_throwsJsonSyntaxException() {
    gson.fromJson("[\"notAnObject\"]", SimpleClass.class);
  }

  // Tests field annotated with @JsonAdapter
  @Test
  public void testWriteAndRead_jsonAdapterAnnotationOnField_usesCustomAdapter() {
    ClassWithJsonAdapter obj = new ClassWithJsonAdapter();
    obj.customField = "rawText";

    String json = gson.toJson(obj);
    assertTrue(json.contains("\"customField\":\"PREFIX_rawText\""));

    ClassWithJsonAdapter result = gson.fromJson("{\"customField\":\"PREFIX_rawText\"}", ClassWithJsonAdapter.class);
    assertEquals("rawText", result.customField);
  }

  // Tests primitive field annotated with @JsonAdapter
  @Test
  public void testWriteAndRead_jsonAdapterOnPrimitiveField_usesCustomAdapter() {
    ClassWithPrimitiveJsonAdapter obj = new ClassWithPrimitiveJsonAdapter();
    obj.primitiveField = 50;

    String json = gson.toJson(obj);
    assertTrue(json.contains("\"primitiveField\":\"50\""));

    ClassWithPrimitiveJsonAdapter result = gson.fromJson("{\"primitiveField\":\"50\"}", ClassWithPrimitiveJsonAdapter.class);
    assertEquals(50, result.primitiveField);
  }

  // Tests interface type create returns adapter with empty bound fields
  @Test
  public void testCreate_interfaceType_createsEmptyAdapter() throws IOException {
    TypeAdapter<SampleInterface> adapter = factory.create(gson, TypeToken.get(SampleInterface.class));
    assertNotNull(adapter);

    StringWriter writer = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(writer);
    adapter.write(jsonWriter, null);
    assertEquals("null", writer.toString());
  }

  // Test helper classes
  static class SimpleClass {
    int intValue;
    String stringValue;
  }

  static class SerializedNameClass {
    @SerializedName(value = "primary", alternate = {"alt1", "alt2"})
    String mainName;
  }

  static class SuperClass {
    String superField;
  }

  static class SubClass extends SuperClass {
    String subField;
  }

  static class DuplicateFieldClass {
    @SerializedName("conflict")
    String field1;
    @SerializedName("conflict")
    String field2;
  }

  static class SelfReferenceClass {
    SelfReferenceClass self;
    String name;
  }

  static class ExcludedClass {
    transient int transientField;
    int normalField;
  }

  interface SampleInterface {
  }

  static class ClassWithJsonAdapter {
    @JsonAdapter(CustomStringAdapter.class)
    String customField;
  }

  static class CustomStringAdapter extends TypeAdapter<String> {
    @Override
    public void write(JsonWriter out, String value) throws IOException {
      out.value("PREFIX_" + value);
    }

    @Override
    public String read(JsonReader in) throws IOException {
      String val = in.nextString();
      if (val != null && val.startsWith("PREFIX_")) {
        return val.substring("PREFIX_".length());
      }
      return val;
    }
  }

  static class ClassWithPrimitiveJsonAdapter {
    @JsonAdapter(CustomIntegerAdapter.class)
    int primitiveField;
  }

  static class CustomIntegerAdapter extends TypeAdapter<Integer> {
    @Override
    public void write(JsonWriter out, Integer value) throws IOException {
      out.value(String.valueOf(value));
    }

    @Override
    public Integer read(JsonReader in) throws IOException {
      return Integer.parseInt(in.nextString());
    }
  }
}