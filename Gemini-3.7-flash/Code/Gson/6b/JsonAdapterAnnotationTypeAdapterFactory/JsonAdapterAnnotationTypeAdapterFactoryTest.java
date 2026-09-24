package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.InstanceCreator;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class JsonAdapterAnnotationTypeAdapterFactoryTest {

  private ConstructorConstructor constructorConstructor;
  private JsonAdapterAnnotationTypeAdapterFactory factory;
  private Gson gson;

  @Before
  public void setUp() {
    constructorConstructor = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    factory = new JsonAdapterAnnotationTypeAdapterFactory(constructorConstructor);
    gson = new Gson();
  }

  // Tests null branch when class has no @JsonAdapter annotation
  @Test
  public void testCreate_unannotatedClass_returnsNull() {
    TypeAdapter<UnannotatedClass> adapter = factory.create(gson, TypeToken.get(UnannotatedClass.class));
    assertNull(adapter);
  }

  // Tests TypeAdapter branch when @JsonAdapter specifies a TypeAdapter subclass
  @Test
  public void testCreate_typeAdapterClass_returnsAdapterInstance() {
    TypeAdapter<AnnotatedWithTypeAdapter> adapter =
        factory.create(gson, TypeToken.get(AnnotatedWithTypeAdapter.class));
    assertNotNull(adapter);
  }

  // Tests TypeAdapterFactory branch when @JsonAdapter specifies a TypeAdapterFactory subclass
  @Test
  public void testCreate_typeAdapterFactoryClass_returnsCreatedAdapter() {
    TypeAdapter<AnnotatedWithTypeAdapterFactory> adapter =
        factory.create(gson, TypeToken.get(AnnotatedWithTypeAdapterFactory.class));
    assertNotNull(adapter);
  }

  // Tests exception path when @JsonAdapter specifies an invalid class
  @Test(expected = IllegalArgumentException.class)
  public void testCreate_invalidAnnotationValue_throwsIllegalArgumentException() {
    factory.create(gson, TypeToken.get(AnnotatedWithInvalidClass.class));
  }

  // Tests direct invocation of getTypeAdapter with a valid TypeAdapter class
  @Test
  public void testGetTypeAdapter_typeAdapterValue_returnsConstructedAdapter() {
    JsonAdapter annotation = AnnotatedWithTypeAdapter.class.getAnnotation(JsonAdapter.class);
    TypeAdapter<?> adapter = JsonAdapterAnnotationTypeAdapterFactory.getTypeAdapter(
        constructorConstructor, gson, TypeToken.get(AnnotatedWithTypeAdapter.class), annotation);
    assertNotNull(adapter);
  }

  // Tests direct invocation of getTypeAdapter with a valid TypeAdapterFactory class
  @Test
  public void testGetTypeAdapter_typeAdapterFactoryValue_returnsFactoryCreatedAdapter() {
    JsonAdapter annotation = AnnotatedWithTypeAdapterFactory.class.getAnnotation(JsonAdapter.class);
    TypeAdapter<?> adapter = JsonAdapterAnnotationTypeAdapterFactory.getTypeAdapter(
        constructorConstructor, gson, TypeToken.get(AnnotatedWithTypeAdapterFactory.class), annotation);
    assertNotNull(adapter);
  }

  // Tests direct invocation of getTypeAdapter with an invalid class throwing exception
  @Test(expected = IllegalArgumentException.class)
  public void testGetTypeAdapter_invalidClassValue_throwsIllegalArgumentException() {
    JsonAdapter annotation = AnnotatedWithInvalidClass.class.getAnnotation(JsonAdapter.class);
    JsonAdapterAnnotationTypeAdapterFactory.getTypeAdapter(
        constructorConstructor, gson, TypeToken.get(AnnotatedWithInvalidClass.class), annotation);
  }

  // Tests nullSafe behavior of the created adapter when serializing null
  @Test
  public void testNullSafe_serializeNull_writesNullJson() throws IOException {
    TypeAdapter<AnnotatedWithTypeAdapter> adapter =
        factory.create(gson, TypeToken.get(AnnotatedWithTypeAdapter.class));
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    adapter.write(jsonWriter, null);
    assertEquals("null", stringWriter.toString());
  }

  // Tests nullSafe behavior of the created adapter when deserializing null
  @Test
  public void testNullSafe_deserializeNull_returnsNull() throws IOException {
    TypeAdapter<AnnotatedWithTypeAdapter> adapter =
        factory.create(gson, TypeToken.get(AnnotatedWithTypeAdapter.class));
    JsonReader jsonReader = new JsonReader(new StringReader("null"));
    AnnotatedWithTypeAdapter result = adapter.read(jsonReader);
    assertNull(result);
  }

  // Tests full round-trip serialization and deserialization with TypeAdapter
  @Test
  public void testRoundTrip_typeAdapterAnnotation_serializesAndDeserializes() throws IOException {
    TypeAdapter<AnnotatedWithTypeAdapter> adapter =
        factory.create(gson, TypeToken.get(AnnotatedWithTypeAdapter.class));
    AnnotatedWithTypeAdapter original = new AnnotatedWithTypeAdapter("custom_value");

    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    adapter.write(jsonWriter, original);

    JsonReader jsonReader = new JsonReader(new StringReader(stringWriter.toString()));
    AnnotatedWithTypeAdapter parsed = adapter.read(jsonReader);

    assertNotNull(parsed);
    assertEquals("custom_value", parsed.value);
  }

  // Tests TypeAdapterFactory that returns null (Defects4J Bug 6 defect detection)
  @Test
  public void testCreate_factoryReturningNullAdapter_doesNotThrowNpeOrReturnsNull() {
    try {
      TypeAdapter<AnnotatedWithNullReturningFactory> adapter =
          factory.create(gson, TypeToken.get(AnnotatedWithNullReturningFactory.class));
      assertNull(adapter);
    } catch (NullPointerException npe) {
      fail("NullPointerException should not be thrown when TypeAdapterFactory returns null adapter: " + npe.getMessage());
    }
  }

  // Helper test structures

  static class UnannotatedClass {
    String value;
  }

  @JsonAdapter(CustomTypeAdapter.class)
  static class AnnotatedWithTypeAdapter {
    final String value;

    AnnotatedWithTypeAdapter(String value) {
      this.value = value;
    }
  }

  @JsonAdapter(CustomTypeAdapterFactory.class)
  static class AnnotatedWithTypeAdapterFactory {
    final String value;

    AnnotatedWithTypeAdapterFactory(String value) {
      this.value = value;
    }
  }

  @JsonAdapter(NullReturningTypeAdapterFactory.class)
  static class AnnotatedWithNullReturningFactory {
    String value;
  }

  @JsonAdapter(String.class)
  static class AnnotatedWithInvalidClass {
    String value;
  }

  static class CustomTypeAdapter extends TypeAdapter<AnnotatedWithTypeAdapter> {
    @Override
    public void write(JsonWriter out, AnnotatedWithTypeAdapter value) throws IOException {
      out.value(value.value);
    }

    @Override
    public AnnotatedWithTypeAdapter read(JsonReader in) throws IOException {
      return new AnnotatedWithTypeAdapter(in.nextString());
    }
  }

  static class CustomTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      if (type.getRawType() == AnnotatedWithTypeAdapterFactory.class) {
        return (TypeAdapter<T>) new TypeAdapter<AnnotatedWithTypeAdapterFactory>() {
          @Override
          public void write(JsonWriter out, AnnotatedWithTypeAdapterFactory value) throws IOException {
            out.value(value.value);
          }

          @Override
          public AnnotatedWithTypeAdapterFactory read(JsonReader in) throws IOException {
            return new AnnotatedWithTypeAdapterFactory(in.nextString());
          }
        };
      }
      return null;
    }
  }

  static class NullReturningTypeAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      return null;
    }
  }
}