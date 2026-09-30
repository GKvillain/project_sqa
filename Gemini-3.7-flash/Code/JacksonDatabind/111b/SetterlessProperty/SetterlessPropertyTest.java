package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class SetterlessPropertyTest {

    static class CollectionHolder {
        private final List<String> items = new ArrayList<String>();

        public List<String> getItems() {
            return items;
        }
    }

    static class NullCollectionHolder {
        public List<String> getItems() {
            return null;
        }
    }

    static class MapHolder {
        private final Map<String, Object> values = new HashMap<String, Object>();

        public Map<String, Object> getValues() {
            return values;
        }
    }

    static class ThrowingGetterHolder {
        public List<String> getItems() {
            throw new IllegalStateException("Getter failed");
        }
    }

    private final ObjectMapper mapper = new ObjectMapper();

    // Tests normal deserialization into a setterless collection property
    @Test
    public void testDeserializeAndSet_validCollection_populatesList() throws Exception {
        String json = "{\"items\":[\"a\",\"b\",\"c\"]}";
        CollectionHolder holder = mapper.readValue(json, CollectionHolder.class);
        assertNotNull(holder.getItems());
        assertEquals(3, holder.getItems().size());
        assertEquals("a", holder.getItems().get(0));
        assertEquals("b", holder.getItems().get(1));
        assertEquals("c", holder.getItems().get(2));
    }

    // Tests normal deserialization into a setterless map property
    @Test
    public void testDeserializeAndSet_validMap_populatesMap() throws Exception {
        String json = "{\"values\":{\"key1\":\"val1\",\"key2\":123}}";
        MapHolder holder = mapper.readValue(json, MapHolder.class);
        assertNotNull(holder.getValues());
        assertEquals("val1", holder.getValues().get("key1"));
        assertEquals(123, holder.getValues().get("key2"));
    }

    // Tests handling of JSON null token for setterless property
    @Test
    public void testDeserializeAndSet_nullValueInJson_keepsCollectionEmpty() throws Exception {
        String json = "{\"items\":null}";
        CollectionHolder holder = mapper.readValue(json, CollectionHolder.class);
        assertNotNull(holder.getItems());
        assertTrue(holder.getItems().isEmpty());
    }

    // Tests failure when getter of setterless property returns null
    @Test(expected = JsonMappingException.class)
    public void testDeserializeAndSet_getterReturnsNull_throwsException() throws Exception {
        String json = "{\"items\":[\"a\"]}";
        mapper.readValue(json, NullCollectionHolder.class);
    }

    // Tests exception handling when getter throws an exception during invocation
    @Test(expected = JsonMappingException.class)
    public void testDeserializeAndSet_getterThrowsException_wrapsAndThrows() throws Exception {
        String json = "{\"items\":[\"a\"]}";
        mapper.readValue(json, ThrowingGetterHolder.class);
    }

    // Tests direct invocation of set() method throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testSet_directCall_throwsUnsupportedOperationException() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(CollectionHolder.class);
        SettableBeanProperty prop = mapper.getDeserializationContext()
                .findRootValueDeserializer(type)
                .findBackReference("items");
        
        // Find property directly from bean description if back reference not present
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        AnnotatedMethod method = new AnnotatedMethod(null, CollectionHolder.class.getMethod("getItems"), null, null);
        SetterlessProperty setterless = new SetterlessProperty(
                new PropertyName("items"), listType, null, null, method);

        setterless.set(new CollectionHolder(), new ArrayList<String>());
    }

    // Tests setAndReturn() throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testSetAndReturn_directCall_throwsUnsupportedOperationException() throws Exception {
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        AnnotatedMethod method = new AnnotatedMethod(null, CollectionHolder.class.getMethod("getItems"), null, null);
        SetterlessProperty setterless = new SetterlessProperty(
                new PropertyName("items"), listType, null, null, method);

        setterless.setAndReturn(new CollectionHolder(), new ArrayList<String>());
    }

    // Tests withName creates a new copy with the updated PropertyName
    @Test
    public void testWithName_newName_returnsNewInstanceWithUpdatedName() throws Exception {
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        AnnotatedMethod method = new AnnotatedMethod(null, CollectionHolder.class.getMethod("getItems"), null, null);
        SetterlessProperty prop = new SetterlessProperty(
                new PropertyName("items"), listType, null, null, method);

        PropertyName newName = new PropertyName("renamedItems");
        SettableBeanProperty renamedProp = prop.withName(newName);

        assertNotNull(renamedProp);
        assertNotSame(prop, renamedProp);
        assertEquals("renamedItems", renamedProp.getName());
    }

    // Tests withValueDeserializer returns same instance when given identical deserializer
    @Test
    public void testWithValueDeserializer_sameDeserializer_returnsSameInstance() throws Exception {
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        AnnotatedMethod method = new AnnotatedMethod(null, CollectionHolder.class.getMethod("getItems"), null, null);
        SetterlessProperty prop = new SetterlessProperty(
                new PropertyName("items"), listType, null, null, method);

        SettableBeanProperty result = prop.withValueDeserializer(prop.getValueDeserializer());
        assertSame(prop, result);
    }

    // Tests withValueDeserializer returns new instance when given different deserializer
    @Test
    public void testWithValueDeserializer_differentDeserializer_returnsNewInstance() throws Exception {
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        AnnotatedMethod method = new AnnotatedMethod(null, CollectionHolder.class.getMethod("getItems"), null, null);
        SetterlessProperty prop = new SetterlessProperty(
                new PropertyName("items"), listType, null, null, method);

        JsonDeserializer<?> newDeser = NullifyingDeserializer.instance;
        SettableBeanProperty result = prop.withValueDeserializer(newDeser);

        assertNotNull(result);
        assertNotSame(prop, result);
        assertSame(newDeser, result.getValueDeserializer());
    }

    // Tests withNullProvider returns new instance with updated NullValueProvider
    @Test
    public void testWithNullProvider_customProvider_returnsNewInstance() throws Exception {
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        AnnotatedMethod method = new AnnotatedMethod(null, CollectionHolder.class.getMethod("getItems"), null, null);
        SetterlessProperty prop = new SetterlessProperty(
                new PropertyName("items"), listType, null, null, method);

        NullValueProvider nva = NullifyingDeserializer.instance;
        SettableBeanProperty result = prop.withNullProvider(nva);

        assertNotNull(result);
        assertNotSame(prop, result);
        assertSame(nva, result.getNullValueProvider());
    }

    // Tests getMember returns the underlying AnnotatedMethod
    @Test
    public void testGetMember_returnsAnnotatedMethod() throws Exception {
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        AnnotatedMethod method = new AnnotatedMethod(null, CollectionHolder.class.getMethod("getItems"), null, null);
        SetterlessProperty prop = new SetterlessProperty(
                new PropertyName("items"), listType, null, null, method);

        assertSame(method, prop.getMember());
    }

    // Tests getAnnotation delegates properly to AnnotatedMethod
    @Test
    public void testGetAnnotation_noAnnotation_returnsNull() throws Exception {
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        AnnotatedMethod method = new AnnotatedMethod(null, CollectionHolder.class.getMethod("getItems"), null, null);
        SetterlessProperty prop = new SetterlessProperty(
                new PropertyName("items"), listType, null, null, method);

        assertNull(prop.getAnnotation(Deprecated.class));
    }
}