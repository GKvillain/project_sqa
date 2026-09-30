package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.reflect.TypeToken;
import org.junit.Before;
import org.junit.Test;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * JUnit 4 test class for JsonAdapterAnnotationTypeAdapterFactory.
 * Covers normal, boundary, exception, and regression paths, including the defect where
 * a TypeAdapterFactory returning null causes a NullPointerException (Defects4J bug 6b).
 */
public class JsonAdapterAnnotationTypeAdapterFactoryTest {

    private Gson gson;
    private JsonAdapterAnnotationTypeAdapterFactory factory;
    private ConstructorConstructor constructorConstructor;

    // --- Helper types for testing ---

    // A TypeAdapter that does not handle null; used to verify nullSafe() wrapping
    private static class NonNullSafeAdapter extends TypeAdapter<String> {
        @Override
        public void write(com.google.gson.stream.JsonWriter out, String value) throws java.io.IOException {
            if (value == null) {
                throw new NullPointerException("null not allowed");
            }
            out.value(value);
        }

        @Override
        public String read(com.google.gson.stream.JsonReader in) throws java.io.IOException {
            return in.nextString();
        }
    }

    // A TypeAdapter with no-arg constructor
    private static class MyTypeAdapter extends NonNullSafeAdapter {}

    // A TypeAdapter without default constructor (used for exception path)
    private static class NoDefaultConstructorAdapter extends NonNullSafeAdapter {
        @SuppressWarnings("unused")
        public NoDefaultConstructorAdapter(String s) {}
    }

    // A TypeAdapterFactory that returns a working adapter
    private static class MyFactory implements TypeAdapterFactory {
        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            return (TypeAdapter<T>) new MyTypeAdapter();
        }
    }

    // A TypeAdapterFactory that returns null (triggers the defect)
    private static class NullFactory implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            return null;
        }
    }

    // Invalid class: neither TypeAdapter nor TypeAdapterFactory
    private static class InvalidAdapter {}

    // Annotated test classes
    @JsonAdapter(MyTypeAdapter.class)
    private static class AnnotatedWithTypeAdapter {}

    @JsonAdapter(MyFactory.class)
    private static class AnnotatedWithFactory {}

    @JsonAdapter(InvalidAdapter.class)
    private static class AnnotatedWithInvalid {}

    @JsonAdapter(NullFactory.class)
    private static class AnnotatedWithNullFactory {}

    @JsonAdapter(NoDefaultConstructorAdapter.class)
    private static class AnnotatedWithNoDefaultConstructor {}

    @Before
    public void setUp() {
        // Use an empty instance creator map so that no-arg constructors are used.
        Map<java.lang.reflect.Type, com.google.gson.InstanceCreator<?>> noCreators = new HashMap<>();
        constructorConstructor = new ConstructorConstructor(noCreators);
        factory = new JsonAdapterAnnotationTypeAdapterFactory(constructorConstructor);
        gson = new Gson();
    }

    // Tests that create returns null when annotation is absent
    @Test
    public void testCreate_noAnnotation_returnsNull() {
        // Use a class without @JsonAdapter
        TypeAdapter<?> adapter = factory.create(gson, TypeToken.get(String.class));
        assertNull(adapter);
    }

    // Tests that create returns a non-null adapter for a TypeAdapter class and that nullSafe() works
    @Test
    public void testCreate_validTypeAdapter_returnsNullSafeAdapter() throws Exception {
        TypeAdapter<?> adapter = factory.create(gson, TypeToken.get(AnnotatedWithTypeAdapter.class));
        assertNotNull(adapter);
        // Verify null safety: write null should produce "null"
        StringWriter writer = new StringWriter();
        com.google.gson.stream.JsonWriter jsonWriter = new com.google.gson.stream.JsonWriter(writer);
        adapter.write(jsonWriter, null);
        jsonWriter.close();
        assertEquals("null", writer.toString());
    }

    // Tests that create returns a non-null adapter for a TypeAdapterFactory and that nullSafe() works
    @Test
    public void testCreate_validFactory_returnsNullSafeAdapter() throws Exception {
        TypeAdapter<?> adapter = factory.create(gson, TypeToken.get(AnnotatedWithFactory.class));
        assertNotNull(adapter);
        // Verify null safety
        StringWriter writer = new StringWriter();
        com.google.gson.stream.JsonWriter jsonWriter = new com.google.gson.stream.JsonWriter(writer);
        adapter.write(jsonWriter, null);
        jsonWriter.close();
        assertEquals("null", writer.toString());
    }

    // Tests that create throws IllegalArgumentException when annotation value is neither TypeAdapter nor TypeAdapterFactory
    @Test(expected = IllegalArgumentException.class)
    public void testCreate_invalidAnnotationValue_throwsException() {
        factory.create(gson, TypeToken.get(AnnotatedWithInvalid.class));
    }

    // Tests that create does not throw NullPointerException when the factory returns null (defect detection)
    // After fix, the result should be null; with bug it throws NPE.
    @Test
    public void testCreate_factoryReturnsNull_returnsNull() {
        TypeAdapter<?> adapter = factory.create(gson, TypeToken.get(AnnotatedWithNullFactory.class));
        assertNull(adapter);
    }

    // Tests that create throws RuntimeException when the TypeAdapter class has no default constructor
    @Test(expected = RuntimeException.class)
    public void testCreate_typeAdapterWithoutDefaultConstructor_throwsException() {
        factory.create(gson, TypeToken.get(AnnotatedWithNoDefaultConstructor.class));
    }

    // Additional branch coverage: test that getTypeAdapter (static) behaves correctly for TypeAdapter class path
    @Test
    public void testGetTypeAdapter_typeAdapterClass_returnsAdapter() {
        TypeAdapter<?> adapter = JsonAdapterAnnotationTypeAdapterFactory.getTypeAdapter(
                constructorConstructor, gson, TypeToken.get(MyTypeAdapter.class),
                AnnotatedWithTypeAdapter.class.getAnnotation(JsonAdapter.class));
        assertNotNull(adapter);
    }

    // Additional branch coverage: test that getTypeAdapter behaves correctly for TypeAdapterFactory path
    @Test
    public void testGetTypeAdapter_factoryClass_returnsAdapter() {
        TypeAdapter<?> adapter = JsonAdapterAnnotationTypeAdapterFactory.getTypeAdapter(
                constructorConstructor, gson, TypeToken.get(MyFactory.class),
                AnnotatedWithFactory.class.getAnnotation(JsonAdapter.class));
        assertNotNull(adapter);
    }

    // Additional branch coverage: test that getTypeAdapter throws for invalid class
    @Test(expected = IllegalArgumentException.class)
    public void testGetTypeAdapter_invalidClass_throwsException() {
        JsonAdapterAnnotationTypeAdapterFactory.getTypeAdapter(
                constructorConstructor, gson, TypeToken.get(InvalidAdapter.class),
                AnnotatedWithInvalid.class.getAnnotation(JsonAdapter.class));
    }
}