package com.google.gson.internal.bind;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class ReflectiveTypeAdapterFactoryTest {

  private final Gson gson = new Gson();

  public static class CustomAdapter extends TypeAdapter<Object> {
    @Override
    public void write(JsonWriter out, Object value) throws IOException {
      if (value == null) {
        out.nullValue();
      } else {
        out.value("custom");
      }
    }

    @Override
    public Object read(JsonReader in) throws IOException {
      in.nextString();
      return null;
    }
  }

  static class ListJsonAdapterHolder {
    @JsonAdapter(CustomAdapter.class)
    List<String> list;

    ListJsonAdapterHolder(List<String> list) {
      this.list = list;
    }
  }

  static class ObjectJsonAdapterHolder {
    @JsonAdapter(CustomAdapter.class)
    Object value;

    ObjectJsonAdapterHolder(Object value) {
      this.value = value;
    }
  }

  static class Sample {
    String name;
  }

  static class PrimitiveHolder {
    int count = 42;
  }

  static class StringHolder {
    String value = "default";
  }

  static class SelfReference {
    String name;
    SelfReference self;

    SelfReference(String name) {
      this.name = name;
    }
  }

  static class ModifierHolder {
    String visible = "visible";
    transient String hidden = "hidden";
    static String staticField = "static";
  }

  static class FieldNamingModel {
    String someField = "x";
  }

  static class InheritedBase {
    String base = "base";
  }

  static class InheritedDerived extends InheritedBase {
    String derived = "derived";
  }

  static class SerialNameHolder {
    @SerializedName(value = "defaultName", alternate = {"alt1", "alt2"})
    String value;

    SerialNameHolder(String value) {
      this.value = value;
    }
  }

  static class DuplicateFieldHolder {
    @SerializedName("duplicate")
    String first = "first";

    @SerializedName("duplicate")
    String second = "second";
  }

  // Tests that a field-level @JsonAdapter is not overridden by a runtime collection adapter.
  @Test
  public void testWriteField_withJsonAdapterAndRuntimeCollectionType_usesAnnotatedAdapter() {
    ListJsonAdapterHolder holder = new ListJsonAdapterHolder(
        new ArrayList<String>(Arrays.asList("a")));
    assertEquals("{\"list\":\"custom\"}", gson.toJson(holder));
  }

  // Tests that a field-level @JsonAdapter is used for an Object-typed field.
  @Test
  public void testWriteField_withJsonAdapterAndObjectRuntimeType_usesAnnotatedAdapter() {
    ObjectJsonAdapterHolder holder = new ObjectJsonAdapterHolder("hello");
    assertEquals("{\"value\":\"custom\"}", gson.toJson(holder));
  }

  // Tests that a direct self-reference is skipped to avoid infinite recursion.
  @Test
  public void testWriteField_selfReference_skipsField() {
    SelfReference value = new SelfReference("root");
    value.self = value;
    assertEquals("{\"name\":\"root\"}", gson.toJson(value));
  }

  // Tests that transient and static fields are not serialized by default.
  @Test
  public void testWriteField_transientAndStaticFields_excluded() {
    assertEquals("{\"visible\":\"visible\"}", gson.toJson(new ModifierHolder()));
  }

  // Tests null value at adapter write level.
  @Test
  public void testWrite_nullValue_writesNull() {
    assertEquals("null", gson.toJson(null, Sample.class));
  }

  // Tests null JSON input at adapter read level.
  @Test
  public void testRead_nullJson_returnsNull() {
    assertNull(gson.fromJson("null", Sample.class));
  }

  // Tests that unknown fields are skipped and known fields are still read.
  @Test
  public void testRead_unknownField_isSkippedAndKnownFieldRead() {
    Sample sample = gson.fromJson("{\"unknown\":\"x\",\"name\":\"Alice\"}", Sample.class);
    assertEquals("Alice", sample.name);
  }

  // Tests that a JSON null for a primitive field does not overwrite its default value.
  @Test
  public void testRead_primitiveFieldWithNull_keepsDefaultValue() {
    PrimitiveHolder holder = gson.fromJson("{\"count\":null}", PrimitiveHolder.class);
    assertEquals(42, holder.count);
  }

  // Tests that a JSON null for a non-primitive field sets the field to null.
  @Test
  public void testRead_nonPrimitiveFieldWithNull_setsNull() {
    StringHolder holder = gson.fromJson("{\"value\":null}", StringHolder.class);
    assertNull(holder.value);
  }

  // Tests that only the default @SerializedName value is used for serialization.
  @Test
  public void testSerializedName_defaultNameUsedForSerialization() {
    SerialNameHolder holder = new SerialNameHolder("x");
    assertEquals("{\"defaultName\":\"x\"}", gson.toJson(holder));
  }

  // Tests that alternate @SerializedName values are used for deserialization.
  @Test
  public void testSerializedName_alternateNameUsedForDeserialization() {
    SerialNameHolder holder = gson.fromJson("{\"alt2\":\"y\"}", SerialNameHolder.class);
    assertEquals("y", holder.value);
  }

  // Tests FieldNamingPolicy interaction with unannotated fields.
  @Test
  public void testFieldNamingPolicy_lowerCaseWithUnderscores_applied() {
    Gson namingGson = new GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create();
    assertEquals("{\"some_field\":\"x\"}", namingGson.toJson(new FieldNamingModel()));
  }

  // Tests that inherited fields are included while walking the class hierarchy.
  @Test
  public void testGetBoundFields_inheritedFields_areSerialized() {
    assertEquals("{\"derived\":\"derived\",\"base\":\"base\"}",
        gson.toJson(new InheritedDerived()));
  }

  // Tests duplicate JSON field name detection.
  @Test(expected = IllegalArgumentException.class)
  public void testGetBoundFields_duplicateJsonNames_throwsIllegalArgumentException() {
    gson.getAdapter(DuplicateFieldHolder.class);
  }

  // Tests that a wrong JSON type for an object fails with JsonSyntaxException.
  @Test(expected = JsonSyntaxException.class)
  public void testRead_wrongJsonType_throwsJsonSyntaxException() {
    gson.fromJson("\"notObject\"", Sample.class);
  }
}